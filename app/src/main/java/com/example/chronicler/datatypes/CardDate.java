package com.example.chronicler.datatypes;

public class CardDate {
    // instance vars
    private int day;
    private int month;
    private int year;
    private static String[] monthNames = {
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
    };

    // constructor
    public CardDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public String getDisplay() {
        // future feature: can maybe add support for MDY or YMD
        // uses DMY
        return String.format(
                "%s %s %s",
                day,
                monthNames[month-1], // taking account of the fact that month will be stored as 1-12 and converting that into an array index
                year
        );
    }

    public int isLaterThan(CardDate other) {

        // returns 1 (yes), -1 (no), and 0 (they are the same)

        // could use Math.signum() to make this more elegant
        // but i think this is more clear and readable

        // year
        if (this.year > other.year) {
            return 1;
        } else if (this.year < other.year) {
            return -1;
        } else {

            // check month of both exist
            if (this.month == -1 || other.month == -1) {
                return 0;
            }

            // month
            if (this.month > other.month) {
                return 1;
            } else if (this.month < other.month) {
                return -1;
            } else {

                // check day of both exist
                if (this.day == -1 || other.day == -1) {
                    return 0;
                }

                // day
                if (this.day > other.day) {
                    return 1;
                } else if (this.day < other.day) {
                    return -1;
                } else {
                    return 0; // is actually the same
                }

            }

        }

    }
}
