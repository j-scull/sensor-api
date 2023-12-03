package projects.sensor.api.util;

import io.vertx.ext.web.validation.RequestParameter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {
    
    public static String getDateTimeString(RequestParameter year, RequestParameter month, RequestParameter date, RequestParameter hour) {
        StringBuilder dateTimeString = new StringBuilder();
        dateTimeString.append(year);
        dateTimeString.append("-");
        dateTimeString.append(month);
        dateTimeString.append("-");
        dateTimeString.append(date);
        if (hour != null) {
            dateTimeString.append(" ");
            dateTimeString.append(hour);
        } else {
        }
        return dateTimeString.toString();
    }

    public static String getDateTimeString(RequestParameter year, RequestParameter month, RequestParameter date) {
        return getDateTimeString(year, month, date, null);
    }

    public static String getDateTimeStringNextInterval(RequestParameter year, RequestParameter month, RequestParameter date, RequestParameter hour) {
        if (hour != null) {
            return getDateTimeStringPlusHours(year, month, date, hour, 1);
        } else {
            return getDateTimeStringPlusDates(year, month, date, 1);
        }
    }

    public static String getDateTimeStringPlusDates(RequestParameter year, RequestParameter month, RequestParameter date, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.valueOf(year.toString()), Integer.valueOf(month.toString()) -1 , Integer.valueOf(date.toString()), 0, 0, 0);
        calendar.add(Calendar.DATE, n);
        return new SimpleDateFormat("y-MM-dd").format(calendar.getTime());
    }

    public static String getDateTimeStringPlusHours(RequestParameter year, RequestParameter month, RequestParameter date, RequestParameter hour, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.valueOf(year.toString()), Integer.valueOf(month.toString()) - 1, Integer.valueOf(date.toString()), Integer.valueOf(hour.toString()), 0, 0);
        calendar.add(Calendar.HOUR_OF_DAY, n);
        return new SimpleDateFormat("y-MM-dd HH").format(calendar.getTime());
    }
    
}
