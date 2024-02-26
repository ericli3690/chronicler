package com.example.chronicler.datatypes;

import androidx.annotation.NonNull;

// class for storing dates in a special, custom way
public class CardDate {

    // instance vars
    public int day;
    public int month;
    public int year;

    // static, final information about months
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

    // simple constructor
    public CardDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    // for display
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

    // for comparing two dates
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

    // validate months
    public static boolean isInvalidMonth(int month) {
        if (month < 1 || month > 12) {
            return true;
        } else {
            return false;
        }
    }

    // validates days
    // includes clause for leap years
    public static boolean isInvalidDay(int day, int month, int year) {
        // the too-low test is easy enough
        if (day < 1) {
            return true;
        }

        //// but is the day too high?

        // first, is it a leap year day

        if (month == 2 && day == 29) {

            // calculate leap year
            // there are far more succinct ways of writing this
            // but i believe this shows my thinking thinking most clearly

            if (year % 4 == 0) {
                // divisible by 4: its a leap year!
                // unless... its divisble by 100?
                if (year % 100 == 0) {
                    // divisible by 100: its NOT a leap year!
                    // unless... its divisible by 400?
                    if (year % 400 == 0) {
                        // divisible by 400: its a leap year!
                        return false; // we're fine
                    } else {
                        // apparently not; its not a leap year
                        return true; // there is an error
                    }
                } else {
                    // apparently not; its a leap year
                    return false; // we're fine
                }
            } else {
                // not divisible by 4: is not a leap year
                return true; // there is an error
            }
        }

        // otherwise

        if (day > monthLengths[month-1]) {
            // too high
            return true;
        }

        // got all the way to the end!
        // no problems here
        return false;

    }
}
