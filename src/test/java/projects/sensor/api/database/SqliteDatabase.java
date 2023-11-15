package projects.sensor.api.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.App;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * Source - https://www.sqlitetutorial.net/sqlite-java/create-database/
 */
public class SqliteDatabase {

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    /**
     * Connect to a sample database
     *
     * @param fileName the database file name
     */
    public static void createNewDatabase(String fileName) {

        String testDatabasePath = System.getProperty("user.dir") + "/target/db/" + fileName;
        String url = "jdbc:sqlite:" + testDatabasePath;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                logger.info("Database driver name is " + meta.getDriverName());
                logger.info("Created database at {}", testDatabasePath);
            }

        } catch (SQLException e) {
            logger.error("Failed to create database = {}, Exception = {}", url, e);
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        createNewDatabase("test.db");
    }
}