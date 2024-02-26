package com.example.chronicler.functions;

import android.app.Activity;
import android.content.Context;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager<T> {
    // instance vars
    private final File file;
    private final Class<T> objectClass;
    private final Activity activity;
    private final GsonBuilder builder;
    private final Gson gson;

    public FileManager(String filepath, Class<T> objectClass, Context context, Activity activity) {
        this.file = new File(context.getFilesDir(), filepath);
        this.objectClass = objectClass;
        this.activity = activity;
        // uses gson, a package for writing between objects and json
        // source: https://github.com/google/gson
        // source: https://www.tutorialspoint.com/gson/gson_quick_guide.htm
        this.builder = new GsonBuilder();
        this.gson = builder.create();
    }

    // checks if the file already exists
    public boolean wasCreatedAlready() {
        return file.isFile();
    }

    // fires whenever an error occurs
    // a standard error message
    private void onError(int code) {
        // log a code too
            // 0: objects -> file
            // 1: file -> objects
        Snackbar.make(
                activity.findViewById(android.R.id.content), // get root
                "File Error: Please try again later. Code " + Integer.toString(code),
                BaseTransientBottomBar.LENGTH_SHORT
        ).show(); // immediately show
    }

    public void writeObjectsToFile(List<T> objects) {
        // convert each object to json using gson
        String[] toWrites = new String[objects.size()];
        for (int objectIndex = 0; objectIndex < objects.size(); objectIndex++) {
            toWrites[objectIndex] = gson.toJson(objects.get(objectIndex));
        }
        // contents are now ready
        // ensure file is gone
        file.delete();
        // then recreate
        // use error handling
        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            for (String toWrite : toWrites) {
                writer.write(toWrite + "\n"); // separate each using a newline
            }
            writer.close(); // conserve resources
        } catch (IOException e) {
            this.onError(0);
        }
    }

    public void writeSingleObjectToFile(T object) {
        List<T> filePackage = new ArrayList<T>();
        filePackage.add(object);
        this.writeObjectsToFile(filePackage);
    }

    public List<T> readObjectsFromFile() {
        // prepare output list for json strings
        // must use an arraylist because it is not known at this time how large the file is, and how many read outs there will be
        List<String> readOuts = new ArrayList<String>();
        // use error handling and read
        try {
            // using bufferedreader for more efficient reading
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (reader.ready()) { // while reader has lines
                readOuts.add(reader.readLine()); // read and append to readout list
            }
            reader.close(); // conserve resources
        } catch (IOException e) {
            // failed for whatever reason
            this.onError(1);
            return new ArrayList<T>();
        }
        // prepare output list for final output objects
        List<T> objects = new ArrayList<T>();
        // for each, convert using gson
        for (int readOutIndex = 0; readOutIndex < readOuts.size(); readOutIndex++) {
            objects.add(gson.fromJson(readOuts.get(readOutIndex), this.objectClass));
        }
        // return final list
        return objects;
    }
}
