package projects.sensor.api.util;

import io.vertx.core.json.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {

    /**
     *
     * @param year
     * @param month
     * @param date
     * @param hour
     * @return a String representing a datetime in format "y-MM-dd HH" if an hour is provided, otherwise format "y-MM-dd"
     */
    public static String getDateTimeString(String year, String month, String date, String hour) {
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

    /**
     *
     * @param jsonObject a json object with fields "year", "month", "date" and optionally "hour"
     * @return a String representing a datetime in format "y-MM-dd HH" if an hour is provided, otherwise format "y-MM-dd"
     */
    public static String getDateTimeString(JsonObject jsonObject) {
        return getDateTimeString(jsonObject.getString("year"), jsonObject.getString("month"), 
                jsonObject.getString("date"), jsonObject.getString("hour"));
    }

    /**
     *
     * @param jsonObject a json object with fields "year", "month", "date" and optionally "hour"
     * @return  a String representing a datetime in format "y-MM-dd HH" if an hour is provided, otherwise format "y-MM-dd"
     *          the returned date will be incremented by an hour if "hour" is in the jsonObject
     *          otherwise the returned date will be incremented by a calendar date.
     */
    public static String getDateTimeStringNextInterval(JsonObject jsonObject) {
        if (jsonObject.getString("hour") != null) {
            return getDateTimeStringPlusHours(jsonObject.getString("year"), jsonObject.getString("month"),
                    jsonObject.getString("date"), jsonObject.getString("hour"), 1);
        } else {
            return getDateTimeStringPlusDates(jsonObject.getString("year"), jsonObject.getString("month"),
                    jsonObject.getString("date"), 1);
        }
    }

    /**
     *
     * @param year
     * @param month
     * @param date
     * @param n the number of days to add to the given date
     * @return a String representing a datetime in format "y-MM-dd", i.e. "2024-01-02"
     *         the returned date will increment the provided date by "n" calendar days
     */
    public static String getDateTimeStringPlusDates(String year, String month, String date, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.valueOf(year.toString()), Integer.valueOf(month.toString()) -1 , Integer.valueOf(date.toString()), 0, 0, 0);
        calendar.add(Calendar.DATE, n);
        return new SimpleDateFormat("y-MM-dd").format(calendar.getTime());
    }

    /**
     *
     * @param year
     * @param month
     * @param date
     * @param hour
     * @param n the number of hour to add to the given time
     * @return a String representing a datetime in format "y-MM-dd HH", i.e. "2024-01-02 18"
     *         the returned date will increment the provided date by "n" hours
     */
    public static String getDateTimeStringPlusHours(String year, String month, String date, String hour, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.valueOf(year.toString()), Integer.valueOf(month.toString()) - 1, Integer.valueOf(date.toString()), Integer.valueOf(hour.toString()), 0, 0);
        calendar.add(Calendar.HOUR_OF_DAY, n);
        return new SimpleDateFormat("y-MM-dd HH").format(calendar.getTime());
    }
    
}
