package com.betomaluje.miband;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by betomaluje on 7/16/15.
 */
public class DateUtils {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static String convertString(Calendar cal) {
        return sdf.format(cal.getTime());

    }

}
