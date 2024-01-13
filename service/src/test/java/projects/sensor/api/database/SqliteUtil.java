package projects.sensor.api.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * Source - https://www.sqlitetutorial.net/sqlite-java/create-database/
 */
public class SqliteUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    

    public static void createTables(String url) {

        // Sensor table
        // id - string (UUID) (PRIMARY KEY)
        // location - string
        // creationTime - timestamp
        String sensorTableQuery = "CREATE TABLE IF NOT EXISTS sensor_info (\n"
                + "	sensorId VARCHAR(36) NOT NULL PRIMARY KEY,\n"
                + "	location VARCHAR(36) NOT NULL,\n"
                + "	creationTime TEXT NOT NULL\n"        // SQLite does not support DATETIME
                + ");";

        // Data table
        // id - string (UUID) (PRIMARY KEY)
        // time - timestamp (PRIMARY KEY)
        // temperature - int
        // humidity - int
        String dataTableQuery = "CREATE TABLE IF NOT EXISTS temperature_and_humidity (\n"
                + "	sensorId VARCHAR(36) NOT NULL,\n"
                + "	temperature int NOT NULL,\n"
                + "	humidity int NOT NULL,\n"
                + "	time TEXT NOT NULL,\n"               // SQLite does not support DATETIME
                + " PRIMARY KEY (sensorId, time)\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);

             Statement stmt = conn.createStatement()) {
            // create a new table
            LOGGER.info("Executing query = {}", sensorTableQuery);
            stmt.execute(sensorTableQuery);
            LOGGER.info("Executing query = {}", dataTableQuery);
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
            LOGGER.info("Executing query = {}", dropSensorTable);
            stmt.execute(dropSensorTable);
            LOGGER.info("Executing query = {}", dropDataTable);
            stmt.execute(dropDataTable );

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}