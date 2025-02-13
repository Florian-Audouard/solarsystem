package fr.univtln.faudouard595.solarsystem.utils.collection;

public class SpeedList {
    private int minute = 60;
    private int hour = 60 * minute;
    private int day = 24 * hour;
    private int week = 7 * day;
    private int month = 30 * day;
    private long year = 365l * day;
    private int currentIndex = 0;
    private int flowOfTime = 1;
    private long[] possibleTime = { 1, 3, 5, 8, 10, 20, 30, 40, 50, // seconds
            1 * minute, 3 * minute, 5 * minute, 8 * minute, 10 * minute, 20 * minute, 30 * minute, 40 * minute,
            50 * minute, // minutes
            1 * hour, 3 * hour, 5 * hour, 8 * hour, 10 * hour, 13 * hour, 16 * hour, 18 * hour, 21 * hour, // hours
            1 * day, 2 * day, 3 * day, 5 * day, 6 * day, // days
            1 * week, 3 * week, // weeks
            1 * month, 2 * month, 4 * month, 6 * month, 8 * month, 10 * month, // months
            1 * year, 3 * year, 4 * year, 5 * year, 10 * year, 20 * year, 50 * year, 100 * year, // years

    };

    public long getCurrentSpeed() {
        return flowOfTime * possibleTime[currentIndex];
    }

    public long increaseSpeed() {
        currentIndex += flowOfTime;
        if (currentIndex < 0) {
            currentIndex = 0;
            flowOfTime *= -1;
        }
        if (currentIndex > possibleTime.length - 1) {
            currentIndex = possibleTime.length - 1;
        }
        return getCurrentSpeed();
    }

    public long decreaseSpeed() {
        currentIndex -= flowOfTime;
        if (currentIndex < 0) {
            currentIndex = 0;
            flowOfTime *= -1;
        }
        if (currentIndex > possibleTime.length - 1) {
            currentIndex = possibleTime.length - 1;
        }
        return getCurrentSpeed();
    }

    public String getFormatedSpeed() {
        int res;
        String unit;
        long speed = possibleTime[currentIndex];
        if (speed < minute) {
            res = (int) speed;
            unit = "second";
        } else if (speed < hour) {
            res = (int) (speed / minute);
            unit = "minute";
        } else if (speed < day) {
            res = (int) (speed / hour);
            unit = "hour";
        } else if (speed < week) {
            res = (int) (speed / day);
            unit = "day";
        } else if (speed < month) {
            res = (int) (speed / week);
            unit = "week";
        } else if (speed < year) {
            res = (int) (speed / month);
            unit = "month";
        } else {
            res = (int) (speed / year);
            unit = "year";
        }
        unit += res > 1 ? "s" : "";
        if (res == 1 && unit.equals("second")) {
            return "real time";
        }
        return res * flowOfTime + " " + unit + "/s";
    }
}
