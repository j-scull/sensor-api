package projects.sensor.api.database;

import io.vertx.reactivex.core.Vertx;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestDatabaseClient {

    @TestSubject
    private DatabaseClient databaseClient;

    private Vertx vertx;

    private static final String DATABASE_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/target/db/test.db";
    private static final String DATABASE_DRIVER_CLASS = "org.sqlite.JDBC";

    @Before
    public void setup(){
        // Set up SQLite database for testing
        SqliteUtil.createTables(DATABASE_URL);

        // Test will cover the functionality in SensorApiImpl and SQLiteClient classes
        vertx = Vertx.vertx();
        databaseClient = new SQLiteClient(vertx, DATABASE_URL, DATABASE_DRIVER_CLASS);
    }

    @Test
    public void insertData() {

    }

    @Test
    public void selectData() {

    }

    @Test
    public void insertSensor() {

    }

    @Test
    public void selectAllSensors() {

    }

    @Test
    public void selectSensor() {

    }
}
