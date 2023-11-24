package projects.sensor.db;

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
    public void logData(JsonObject jsonObject) {
        logger.info(String.valueOf(jsonObject));
//        String sql = "INSERT INTO temperature_and_humidity (sensorId, temperature, humidity, time) VALUES (?, ?, ?, ?)";

        // Todo - pass sql statement and parameters separately
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO temperature_and_humidity (sensorId, temperature, humidity, time) VALUES (");
        sql.append(jsonObject.getString("sensorId"));
        sql.append(", ");
        sql.append(jsonObject.getString("temperature"));
        sql.append(", ");
        sql.append(jsonObject.getString("humidity"));
        sql.append(", ");
        sql.append(jsonObject.getString("time"));
        sql.append(")");

        this.sqlClient.getConnection(res -> {
            if (res.succeeded()) {

                SQLConnection connection = res.result();
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(jsonObject);

                connection.query(sql.toString(), queryResult -> {
                    if (queryResult.succeeded()) {
                        logger.info("logData - logged data successfully");
                    } else {
                        logger.error("logData - query to database failed - {}", queryResult.cause());
                    }
                });

            } else {
                logger.error("logData - failed to connect to database - {}", res.cause());
            }
        });

    }

    // What is the return value? Data or DataResponse
    public void getData() {
        String sql = "SELECT * FROM temperature_and_humidity";

        this.sqlClient.query(sql, ar -> {
            if (ar.succeeded()) {
                ResultSet result = ar.result();
                for (JsonArray jsonArray: result.getResults()) {
                    logger.info("getData - result = {}", jsonArray);
                }
            } else {
                logger.error("getData - failed to query database - {}", ar.cause());
            }

        });
    }

    // What is the return value? List<Data> or List<DataResponse>
    public void getDataRange() {

    }

    // return List<GetSensorResponse>
    public void listSensors() {

    }

    // GetSensorResponse
    public void getSensor() {

    }

    public void addSensor() {

    }
}
