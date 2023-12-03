package projects.sensor.api;

import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.impl.RequestParameterImpl;

import org.junit.Test;

import projects.sensor.api.util.TimeUtil;

import static org.junit.Assert.assertEquals;

public class TestTimeUtil {

    @Test
    public void testGetTimeString_WithoutHour() {
        RequestParameter year = new RequestParameterImpl("2023");
        RequestParameter month = new RequestParameterImpl("12");
        RequestParameter date = new RequestParameterImpl("03");
        String dt = TimeUtil.getDateTimeString(year, month, date);
        assertEquals("2023-12-03 00:00:00", dt);
    }

    @Test
    public void testGetTimeString_WithHour() {
        RequestParameter year = new RequestParameterImpl("2023");
        RequestParameter month = new RequestParameterImpl("12");
        RequestParameter date = new RequestParameterImpl("03");
        RequestParameter hour = new RequestParameterImpl("15");
        String dt = TimeUtil.getDateTimeString(year, month, date, hour);
        assertEquals("2023-12-03 15:00:00", dt);
    }

    @Test
    public void testGetDateTimeStringNextInterval_WithoutHour() {
        RequestParameter year = new RequestParameterImpl("2023");
        RequestParameter month = new RequestParameterImpl("12");
        RequestParameter date = new RequestParameterImpl("03");
        String dt = TimeUtil.getDateTimeStringNextInterval(year, month, date, null);
        assertEquals("2023-12-04 00:00:00", dt);
    }

    @Test
    public void testGetDateTimeStringNextInterval_WithHour() {
        RequestParameter year = new RequestParameterImpl("2023");
        RequestParameter month = new RequestParameterImpl("12");
        RequestParameter date = new RequestParameterImpl("03");
        RequestParameter hour = new RequestParameterImpl("15");
        String dt = TimeUtil.getDateTimeStringNextInterval(year, month, date, hour);
        assertEquals("2023-12-03 16:00:00", dt);
    }

    @Test
    public void testGetDateTimeStringPlusDates() {
        RequestParameter year = new RequestParameterImpl("2023");
        RequestParameter month = new RequestParameterImpl("12");
        RequestParameter date = new RequestParameterImpl("03");
        // Increment 1 month (31 days)
        String dt = TimeUtil.getDateTimeStringPlusDates(year, month, date, 31);
        assertEquals("2024-01-03 00:00:00", dt);
    }

    @Test
    public void testGetDateTimeStringPlusHours() {
        RequestParameter year = new RequestParameterImpl("2023");
        RequestParameter month = new RequestParameterImpl("12");
        RequestParameter date = new RequestParameterImpl("03");
        RequestParameter hour = new RequestParameterImpl("15");
        // Increment 12 hours
        String dt = TimeUtil.getDateTimeStringPlusHours(year, month, date, hour, 12);
        assertEquals("2023-12-04 03:00:00", dt);
    }

}
