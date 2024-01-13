package projects.sensor.api;

import org.junit.*;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import projects.sensor.api.router.OpenApiRouter;
import projects.sensor.api.database.SqliteUtil;
import projects.sensor.api.verticle.RestServerVerticle;

import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class TestBase {

    private OpenApiRouter router;

    private static String databasePath = System.getProperty("user.dir") + "/target/db/test.db";
    private static String databaseUrl = "jdbc:sqlite:" + databasePath;
    private static final String databaseDriverClass = "org.sqlite.jdbcDriver";

    @Before
    public void setup(){
//        // Todo - Fix and expand tests
        SqliteUtil.createTables(databaseUrl);
//        RestServerVerticle restServerVerticle = new RestServerVerticle();
//        restServerVerticle.start();
    }

    @Test
    public void shouldAnswerWithTrue() {
        assertTrue( true );
    }


//    @After
//    public void tearDownDB() {
//        SqliteUtil.dropTables(databaseUrl);
//    }

}
