package projects.sensor.api.database;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.Main;
import projects.sensor.api.config.database.MySQLConfig;

public class MySQLClient implements DatabaseClient{

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private final String insertDataQuery = "INSERT INTO temperature_and_humidity (sensorId, temperature, humidity, time) VALUES (?, ?, ?, ?)";
    private final String selectDataQuery = "SELECT * FROM temperature_and_humidity WHERE time >= ? AND time < ?";
    private final String insertSensorQuery = "INSERT INTO sensor_info(sensorId, location, creationTime) VALUES (?, ?, ?)";
    private final String selectAllSensorsQuery = "SELECT * FROM sensor_info";
    private final String selectSensorQuery = "SELECT * FROM sensor_info WHERE sensorId = ?";

    private final MySQLConnectOptions connectOptions;
    private final PoolOptions poolOptions;

//    private Pool sqlClient;

    public MySQLClient(MySQLConfig mySQLConfig) {
        LOGGER.info("MySQLClient - creating client with configuration = {}", mySQLConfig);
        connectOptions = new MySQLConnectOptions()
                .setPort(mySQLConfig.getPort())
                .setHost(mySQLConfig.getHost())
                .setDatabase(mySQLConfig.getDatabase())
                .setUser(System.getenv("user"))
                .setPassword(System.getenv("secret"));

        poolOptions = new PoolOptions()
                .setMaxSize(mySQLConfig.getPoolOptionsMaxSize());
    }

    public void createPooledClient(Handler<Pool> handler) {
        Pool sqlClient = MySQLBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .build();
        LOGGER.info("MySQLClient - created pooled client");
        handler.handle(sqlClient);
        sqlClient.close();
    }

    // Todo - implement methods

    @Override
    public Single<UpdateResult> insertData(JsonArray queryParams) {
        LOGGER.info("MySQLClient - insertData - query parameters = {}", queryParams);
        return Single.create(singleEmitter -> {
            // Todo - refactor this, probably most can be re-used by different operations
            Pool sqlClient = MySQLBuilder.pool()
                    .with(poolOptions)
                    .connectingTo(connectOptions)
                    .build();
            sqlClient.preparedQuery(insertDataQuery)
                    .execute(Tuple.of(queryParams))
                    .onComplete(result -> {
                        if (result.succeeded()) {
                            // Using sql UpdateResult for now
                            singleEmitter.onSuccess(new UpdateResult().setUpdated(1));
                        } else {
                            singleEmitter.onError(result.cause());
                        }
                        sqlClient.close();
                    });
        });
    }

    @Override
    public Single<ResultSet> selectData(JsonArray queryParams) {
        LOGGER.debug("MySQLClient - selectData - query parameters = {}", queryParams);
//        return Single.create(singleEmitter -> {
//            sqlClient.preparedQuery(insertDataQuery)
//                    .execute(Tuple.of(queryParams))
//                    .onComplete(result -> {
//                        if (result.succeeded()) {
//                            RowSet<Row> r = result.result();
//                            singleEmitter.onSuccess(result.result());
//                        } else {
//                            singleEmitter.onError(result.cause());
//                        }
//                    });
//        });
        return null;
    }

    @Override
    public Single<UpdateResult> insertSensor(JsonArray queryParams) {
        LOGGER.debug("MySQLClient - insertSensor - query parameters = {}", queryParams);
        return null;
    }

    @Override
    public Single<ResultSet> selectAllSensors() {
        LOGGER.debug("MySQLClient - selectAllSensors - no query params");
        return null;
    }

    @Override
    public Single<ResultSet> selectSensor(JsonArray queryParams) {
        LOGGER.debug("MySQLClient - selectSensor - query parameters = {}", queryParams);
        return null;
    }
}
