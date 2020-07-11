package me.schooltests.divergentenvoy.util;

import java.util.regex.Pattern;

public class TimeUtil {
    public static int getSecondsFromTimestamp(String timestamp) { return getSecondsFromTimestamp(timestamp, ", "); }
    public static int getSecondsFromTimestamp(String timestamp, String split) throws NumberFormatException {
        String[] parts = timestamp.split(Pattern.quote(split));
        int seconds = 0;
        for (String part : parts) {
            int multi = Integer.parseInt(part.substring(0, part.length() - 1));
            char unit = part.charAt(part.length() - 1);
            switch (unit) {
                case 'd':
                    seconds += (multi * 86400);
                    break;
                case 'h':
                    seconds += (multi * 3600);
                    break;
                case 'm':
                    seconds += (multi * 60);
                    break;
                case 's':
                    seconds += multi;
                    break;
            }
        }

        return seconds;
    }

    public static String getTimestampFromSeconds(int seconds) { return getTimestampFromSeconds(seconds, ", "); }
    public static String getTimestampFromSeconds(int seconds, String join) {
        StringBuilder timestamp = new StringBuilder();
        int days = 0, hours = 0, minutes = 0;
        while (seconds >= 86400) {
            days++;
            seconds -= 86400;
        }

        while (seconds >= 3600) {
            hours++;
            seconds -= 3600;
        }

        while (seconds >= 60) {
            minutes++;
            seconds -= 60;
        }

        if (days > 0) timestamp.append(days + "d" + join);
        if (hours > 0) timestamp.append(hours + "h" + join);
        if (minutes > 0) timestamp.append(minutes + "m" + join);
        if (seconds > 0) timestamp.append(seconds + "s" + join);
        return timestamp.toString().substring(0, timestamp.toString().length() - join.length());
    }
}
