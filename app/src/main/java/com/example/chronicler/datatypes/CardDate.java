package com.example.chronicler.datatypes;

import androidx.annotation.NonNull;

public class CardDate {

    public int day;
    public int month;
    public int year;

    private static final String[] monthNames = {
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
    private static final int[] monthLengths = {
            31, // january
            28, // february
            31, // march
            30, // april
            31, // may
            30, // june
            31, // july
            31, // august
            30, // september
            31, // october
            30, // november
            31  // december
    };

    public CardDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    @NonNull
    @Override
    public String toString() {
        // future feature: can maybe add support for MDY or YMD
        // uses DMY
        return String.format(
                "%s%s%s%s",
                day != -1 ? day + " " : "", // if no day, dont show it
                month != -1 ? monthNames[month-1] + " " : "", // taking account of the fact that month will be stored as 1-12 and converting that into an array index; and if there is no month, dont show it
                Math.abs(year), // there will always be a year
                year < 0 ? " BCE" : "" // BCE or CE
        );
    }

    public int isLaterThan(CardDate other) {

        // returns 1 (yes), -1 (no), and 0 (they are the same)

        // could use Math.signum() to make this more elegant
        // but i think this is more clear and readable

        if (this.year > other.year) {
            return 1;
        } else if (this.year < other.year) {
            return -1;
        } else {

            // check that month of both exist
            if (this.month == -1 || other.month == -1) {
                return 0;
            }

            if (this.month > other.month) {
                return 1;
            } else if (this.month < other.month) {
                return -1;
            } else {

                // check that day of both exist
                if (this.day == -1 || other.day == -1) {
                    return 0;
                }

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

    public static boolean isInvalidMonth(int month) {
        if (month < 1 || month > 12) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isInvalidDay(int day, int month, int year) {
        if (day < 1) {
            return true;
        }
        if (month == 2 && day == 29) {
            // calculate leap year
            // there are far more succinct ways of writing this
            // but i believe this is most readable
            if (year % 4 == 0) {
                if (year % 100 == 0) {
                    if (year % 400 == 0) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
        // otherwise
        if (day > monthLengths[month-1]) {
            return true;
        }
        // got all the way to the end!
        return false;
    }
}
