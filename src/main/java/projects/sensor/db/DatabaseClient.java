package projects.sensor.db;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClient;

import io.vertx.reactivex.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.App;

public class DatabaseClient {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

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

        logger.info("Connecting to database with config = {}", config);
        sqlClient = JDBCClient.createShared(vertx, config);
    }

    // Todo - return success failure to router
    public void logData(JsonArray queryParams) {
        logger.info(String.valueOf(queryParams));
        String query = "INSERT INTO temperature_and_humidity (sensorId, temperature, humidity, time) VALUES (?, ?, ?, ?)";

        this.sqlClient.getConnection(res -> {
            if (res.succeeded()) {

                SQLConnection connection = res.result();

                connection.updateWithParams(query, queryParams, queryResult -> {
                    if (queryResult.succeeded()) {
                        logger.info("logData - logged data successfully");
                    } else {
                        logger.error("logData - query to database failed - {}", queryResult.cause().getMessage());
                    }
                });

            } else {
                logger.error("logData - failed to connect to database - {}", res.cause());
            }
        });

    }

    // Todo
    // SELECT using params and using a prepared statement
    // Think about returned time format - "time":[2023,11,25,22,34,10,41000000], what should be used here?
    public void getData(JsonArray queryParams, HttpServerResponse response) throws RuntimeException {

        logger.info("getData - queryParams = {}", queryParams);

        String query = "SELECT * FROM temperature_and_humidity WHERE time >= ? AND time < ?";

        this.sqlClient.queryWithParams(query, queryParams, queryResult -> {
            if (queryResult.succeeded()) {
                ResultSet result = queryResult.result();
                for (JsonObject row: result.getRows()) {
                    logger.info("getData - result = {}", row);
                }
                JsonObject jsonResponse = new JsonObject().put("data", result.getRows());
                response.setStatusCode(200)
                        .setStatusMessage("OK")
                        .end(jsonResponse.toBuffer());
            } else {
                logger.error("getData - failed to query database - {}", queryResult.cause().getMessage());
                response.setStatusCode(500)
                        .setStatusMessage("Internal Server Error")
                        .end();
            }
        });
    }

    public void addSensor(JsonArray queryParams, HttpServerResponse response) {

        logger.info("addSensor - queryParams = {}", queryParams);

        String query = "INSERT INTO sensor_info(sensorId, location, creationTime) VALUES (?, ?, ?)";

        this.sqlClient.queryWithParams(query, queryParams, queryResult -> {
            if (queryResult.succeeded()) {
                logger.info("addSensor - added sensor successfully");
                response.setStatusCode(201)
                        .setStatusMessage("OK")
                        .end();
            } else {
                logger.error("addSensor - failed to add sensorId - {}", queryResult.cause().getMessage());
                response.setStatusCode(500)
                        .setStatusMessage("Internal Server Error")
                        .end();
            }
        });


    }

    public void listSensors(HttpServerResponse response) {

        logger.info("listSensors - no queryParams");

        String query = "SELECT sensorID FROM sensor_info";

        this.sqlClient.query(query, queryResult -> {
            if (queryResult.succeeded()) {
                ResultSet result = queryResult.result();
                for (JsonObject row: result.getRows()) {
                    logger.info("listSensors- result = {}", row);
                }
                JsonObject jsonResponse = new JsonObject().put("data", result.getRows());
                response.setStatusCode(200)
                        .setStatusMessage("OK")
                        .end(jsonResponse.toBuffer());
            } else {
                logger.error("listSensors - failed to query database - {}", queryResult.cause().getMessage());
                response.setStatusCode(500)
                        .setStatusMessage("Internal Server Error")
                        .end();
            }
        });
    }

    public void getSensor(JsonArray queryParams, HttpServerResponse response) {
        logger.info("listSensors - queryParams = {}", queryParams);

        String query = "SELECT * FROM sensor_info WHERE sensorId = ?";

        this.sqlClient.queryWithParams(query, queryParams, queryResult -> {
            if (queryResult.succeeded()) {
                ResultSet result = queryResult.result();
                for (JsonObject row: result.getRows()) {
                    logger.info("listSensors- result = {}", row);
                }
                JsonObject jsonResponse = new JsonObject().put("data", result.getRows());
                response.setStatusCode(200)
                        .setStatusMessage("OK")
                        .end(jsonResponse.toBuffer());
            } else {
                logger.error("listSensors - failed to query database - {}", queryResult.cause().getMessage());
                response.setStatusCode(500)
                        .setStatusMessage("Internal Server Error")
                        .end();
            }
        });
    }


}
