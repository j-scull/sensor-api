package projects.sensor.api.database;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;

import io.vertx.sqlclient.SqlClient;
import projects.sensor.api.config.database.MySQLConfig;

public class MySQLClient implements DatabaseClient{

    SqlClient sqlClient;

    public MySQLClient(MySQLConfig mySQLConfig) {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(mySQLConfig.getPort())
                .setHost(mySQLConfig.getHost())
                .setDatabase(mySQLConfig.getDatabase())
                .setUser(System.getenv("user"))
                .setPassword(System.getenv("secret"));

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(mySQLConfig.getPoolOptionsMaxSize());

        sqlClient = MySQLBuilder
                .client()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .build();
    }

    // Todo - implement methods
    @Override
    public Single<UpdateResult> insertData(JsonArray queryParams) {
        return null;
    }

    @Override
    public Single<ResultSet> selectData(JsonArray queryParams) {
        return null;
    }

    @Override
    public Single<UpdateResult> insertSensor(JsonArray queryParams) {
        return null;
    }

    @Override
    public Single<ResultSet> selectAllSensors() {
        return null;
    }

    @Override
    public Single<ResultSet> selectSensor(JsonArray queryParams) {
        return null;
    }
}
