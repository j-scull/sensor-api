package projects.sensor.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.App;

import java.sql.*;

/**
 *
 * Source - https://www.sqlitetutorial.net/sqlite-java/create-database/
 */
public class SqliteUtil {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void createTables(String url) {

        // Sensor table
        // id - string (UUID) (PRIMARY KEY)
        // location - string
        // creationTime - timestamp
        String sensorTableQuery = "CREATE TABLE IF NOT EXISTS sensor_info (\n"
                + "	id VARCHAR(36) NOT NULL PRIMARY KEY,\n"
                + "	location VARCHAR(36) NOT NULL,\n"
                + "	creationTime TIMESTAMP NOT NULL\n"
                + ");";

        // Data table
        // id - string (UUID) (PRIMARY KEY)
        // time - timestamp (PRIMARY KEY)
        // temperature - int
        // humidity - int
        String dataTableQuery = "CREATE TABLE IF NOT EXISTS temperature_and_humidity (\n"
                + "	id VARCHAR(36) NOT NULL,\n"
                + "	time TIMESTAMP NOT NULL,\n"
                + "	temperature int NOT NULL,\n"
                + "	humidity int NOT NULL,\n"
                + " PRIMARY KEY (id, time)\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);

            Statement stmt = conn.createStatement()) {
            // create a new table
            logger.info("Executing query = {}", sensorTableQuery);
            stmt.execute(sensorTableQuery);
            logger.info("Executing query = {}", dataTableQuery);
            stmt.execute(dataTableQuery);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void dropTables(String url) {

        String dropSensorTable = "DROP TABLE sensor_info";
        String dropDataTable = "DROP TABLE temperature_and_humidity;";

        try (Connection conn = DriverManager.getConnection(url);

             Statement stmt = conn.createStatement()) {
            // create a new table
            logger.info("Executing query = {}", dropSensorTable);
            stmt.execute(dropSensorTable);
            logger.info("Executing query = {}", dropDataTable);
            stmt.execute(dropDataTable );

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


//    /**
//     * Connect to a sample database
//     *
//     * @param fileName the database file name
//     */
//    public static void createNewDatabase(String fileName) {
//
//        String testDatabasePath = System.getProperty("user.dir") + "/target/db/" + fileName;
//        String url = "jdbc:sqlite:" + testDatabasePath;
//
//        try (Connection conn = DriverManager.getConnection(url)) {
//            if (conn != null) {
//                DatabaseMetaData meta = conn.getMetaData();
//                logger.info("Database driver name is " + meta.getDriverName());
//                logger.info("Created database at {}", testDatabasePath);
//            }
//
//        } catch (SQLException e) {
//            logger.error("Failed to create database = {}, Exception = {}", url, e);
//            System.out.println(e.getMessage());
//        }
//    }
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        createNewDatabase("test.db");
//    }
}