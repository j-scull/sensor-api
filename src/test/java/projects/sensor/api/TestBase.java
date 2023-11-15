package projects.sensor.api;

import org.junit.Before;
import org.junit.Test;

import projects.sensor.api.database.SqliteDatabase;

import static org.junit.Assert.assertTrue;


public class TestBase {

    @Before
    public void setup(){
        SqliteDatabase.createNewDatabase("test.db");
    }

    @Test
    public void shouldAnswerWithTrue() {
        assertTrue( true );
    }

    // Todo - Create table in the Sqlite database and populate with data
    protected void setupDatabase() {

    }

}
