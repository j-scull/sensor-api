package projects.sensor.api.databse;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.Main;

public class DatabaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    SQLClient sqlClient;

    private DatabaseClient() {}
    private static class DatabaseClientHolder{
        public static final DatabaseClient instance = new DatabaseClient();
    }

    public static DatabaseClient getInstance() {
        return DatabaseClientHolder.instance;
    }

    public void connectToDatabase(Vertx vertx, String url, String driverClass) {

        JsonObject config = new JsonObject()
                .put("url", url)
                .put("driver_class", driverClass)
                .put("max_pool_size", 30);

        LOGGER.info("Connecting to database with config = {}", config);
        sqlClient = JDBCClient.createShared(vertx, config);
    }

    // Should the database be queried first to verify sensorId is registered in sensor_info?
    public Single<UpdateResult> logData(JsonArray queryParams) {
        LOGGER.info(String.valueOf(queryParams));
        String query = "INSERT INTO temperature_and_humidity (sensorId, temperature, humidity, time) VALUES (?, ?, ?, ?)";
        return this.sqlClient.rxUpdateWithParams(query, queryParams)
                .doOnSuccess(s -> LOGGER.info("logData - query to database completed successfully"))
                .doOnError(e -> LOGGER.info("logData - query to database failed - {}", e.getMessage()));
    }

    public Single<ResultSet> getData(JsonArray queryParams) {
        LOGGER.info("getData - queryParams = {}", queryParams);
        String query = "SELECT * FROM temperature_and_humidity WHERE time >= ? AND time < ?";
        return this.sqlClient.rxQueryWithParams(query, queryParams)
                .doOnSuccess(s -> LOGGER.info("getData - query to database completed successfully"))
                .doOnError(e -> LOGGER.info("getData - query to database failed - {}", e.getMessage()));
    }

    public Single<UpdateResult> createSensor(JsonArray queryParams) {
        LOGGER.info("createSensor - queryParams = {}", queryParams);
        String query = "INSERT INTO sensor_info(sensorId, location, creationTime) VALUES (?, ?, ?)";
        return this.sqlClient.rxUpdateWithParams(query, queryParams)
                .doOnSuccess(r -> LOGGER.info("createSensor - query to database completed successfully"))
                .doOnError(e -> LOGGER.error("createSensor - query to database failed - {}", e.getMessage()));
    }

    public Single<ResultSet> listSensors() {
        LOGGER.info("listSensors - no queryParams");
        String query = "SELECT * FROM sensor_info";
        return this.sqlClient.rxQuery(query)
                .doOnSuccess(r -> LOGGER.info("listSensor - query to database completed successfully"))
                .doOnError(e -> LOGGER.error("listSensor - query to database failed - {}", e.getMessage()));
    }

    public Single<ResultSet> getSensor(JsonArray queryParams) {
        LOGGER.info("getSensor - queryParams = {}", queryParams);
        String query = "SELECT * FROM sensor_info WHERE sensorId = ?";
        return this.sqlClient.rxQueryWithParams(query, queryParams)
                .doOnSuccess(r -> LOGGER.info("getSensor - query to database completed successfully"))
                .doOnError(e -> LOGGER.error("getSensor - query to database failed - {}", e.getMessage()));
    }

    // Todo
    // a deregister sensor Api
    // a count entries for sensor Api
    // a count total sensors Api
    //
    // Use generated model Classes
    //
    // Refactor this class as Service.class, contain main logic and handle requests passed from router

}
