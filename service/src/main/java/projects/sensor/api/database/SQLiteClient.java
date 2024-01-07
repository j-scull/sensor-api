package projects.sensor.api.database;

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

public class SQLiteClient implements DataBaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final String insertDataQuery = "INSERT INTO temperature_and_humidity (sensorId, temperature, humidity, time) VALUES (?, ?, ?, ?)";
    private final String selectDataQuery = "SELECT * FROM temperature_and_humidity WHERE time >= ? AND time < ?";
    private final String insertSensorQuery = "INSERT INTO sensor_info(sensorId, location, creationTime) VALUES (?, ?, ?)";
    private final String selectAllSensorsQuery = "SELECT * FROM sensor_info";
    private final String selectSensorQuery = "SELECT * FROM sensor_info WHERE sensorId = ?";

    SQLClient sqlClient;

    public SQLiteClient(Vertx vertx, String url, String driverClass) {
        JsonObject config = new JsonObject()
                .put("url", url)
                .put("driver_class", driverClass)
                .put("max_pool_size", 30);
        LOGGER.info("Connecting to database with config = {}",config);
        sqlClient = JDBCClient.createShared(vertx,config);
    }

    // Should the database be queried first to verify sensorId is registered in sensor_info?
    public Single<UpdateResult> insertData(JsonArray queryParams) {
        LOGGER.info(String.valueOf(queryParams));
        return this.sqlClient.rxUpdateWithParams(insertDataQuery, queryParams)
                .doOnSuccess(s -> LOGGER.info("logData - query to database completed successfully"))
                .doOnError(e -> LOGGER.info("logData - query to database failed - {}", e.getMessage()));
    }

    public Single<ResultSet> selectData(JsonArray queryParams) {
        LOGGER.info("getData - queryParams = {}", queryParams);
        return this.sqlClient.rxQueryWithParams(selectDataQuery, queryParams)
                .doOnSuccess(s -> LOGGER.info("getData - query to database completed successfully"))
                .doOnError(e -> LOGGER.info("getData - query to database failed - {}", e.getMessage()));
    }

    public Single<UpdateResult> insertSensor(JsonArray queryParams) {
        LOGGER.info("createSensor - queryParams = {}", queryParams);
        return this.sqlClient.rxUpdateWithParams(insertSensorQuery, queryParams)
                .doOnSuccess(r -> LOGGER.info("createSensor - query to database completed successfully"))
                .doOnError(e -> LOGGER.error("createSensor - query to database failed - {}", e.getMessage()));
    }

    public Single<ResultSet> selectAllSensors() {
        LOGGER.info("listSensors - no queryParams");
        return this.sqlClient.rxQuery(selectAllSensorsQuery)
                .doOnSuccess(r -> LOGGER.info("listSensor - query to database completed successfully"))
                .doOnError(e -> LOGGER.error("listSensor - query to database failed - {}", e.getMessage()));
    }

    public Single<ResultSet> selectSensor(JsonArray queryParams) {
        LOGGER.info("getSensor - queryParams = {}", queryParams);
        return this.sqlClient.rxQueryWithParams(selectSensorQuery, queryParams)
                .doOnSuccess(r -> LOGGER.info("getSensor - query to database completed successfully"))
                .doOnError(e -> LOGGER.error("getSensor - query to database failed - {}", e.getMessage()));
    }

    // Todo
    // a deregister sensor Api
    // a count entries for sensor Api
    // a count total sensors Api
    //
    // Use generated model Classes

}
