package projects.sensor.api;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.impl.RequestParameterImpl;

import org.junit.Test;

import projects.sensor.api.util.TimeUtil;

import static org.junit.Assert.assertEquals;

public class TestTimeUtil {

    @Test
    public void testGetTimeString_WithoutHour() {
        JsonObject jsonObject = createJsonDateTimeWithoutHour("2023", "12","03");
        String dt = TimeUtil.getDateTimeString(jsonObject);
        assertEquals("2023-12-03", dt);
    }

    @Test
    public void testGetTimeString_WithHour() {
        JsonObject jsonObject = createJsonDateTime("2023", "12","03","15");
        String dt = TimeUtil.getDateTimeString(jsonObject);
        assertEquals("2023-12-03 15", dt);
    }

    @Test
    public void testGetDateTimeStringNextInterval_WithoutHour() {
        JsonObject jsonObject = createJsonDateTimeWithoutHour("2023", "12","03");
        String dt = TimeUtil.getDateTimeStringNextInterval(jsonObject);
        assertEquals("2023-12-04", dt);
    }

    @Test
    public void testGetDateTimeStringNextInterval_WithHour() {
        JsonObject jsonObject = createJsonDateTime("2023", "12","03","15");
        String dt = TimeUtil.getDateTimeStringNextInterval(jsonObject);
        assertEquals("2023-12-03 16", dt);
    }

    @Test
    public void testGetDateTimeStringPlusDates() {
        // Increment 1 month (31 days)
        String dt = TimeUtil.getDateTimeStringPlusDates("2023", "12", "03",  31);
        assertEquals("2024-01-03", dt);
    }

    @Test
    public void testGetDateTimeStringPlusHours() {
        // Increment 12 hours
        String dt = TimeUtil.getDateTimeStringPlusHours("2023", "12", "03", "15", 12);
        assertEquals("2023-12-04 03", dt);
    }

    private JsonObject createJsonDateTime(String year, String month, String date, String hour) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("year", year);
        jsonObject.put("month", month);
        jsonObject.put("date", date);
        jsonObject.put("hour", hour);
        return jsonObject;
    }

    private JsonObject createJsonDateTimeWithoutHour(String year, String month, String date) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("year", year);
        jsonObject.put("month", month);
        jsonObject.put("date", date);
        return jsonObject;
    }


}
