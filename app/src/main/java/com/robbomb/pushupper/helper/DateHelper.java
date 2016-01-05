package com.robbomb.pushupper.helper;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

/**
 * Created by NewRob on 1/4/2016.
 */
public class DateHelper {
    private static DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
    public static String humanFormat = "EEEE, MMMM d, yyyy";

    public static String format(DateTime dateTime) {
        return formatter.print(dateTime);
    }

    public static DateTime parse(String s) {
        return formatter.parseDateTime(s);
    }

}
