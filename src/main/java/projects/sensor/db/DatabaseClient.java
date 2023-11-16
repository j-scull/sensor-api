package projects.sensor.db;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.App;

public class DatabaseClient {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private DatabaseClient() {}
    private static class DatabaseClientHolder{
        public static final DatabaseClient instance = new DatabaseClient();
    }

    public static DatabaseClient getInstance() {
        return DatabaseClientHolder.instance;
    }

    public SQLClient connectToDatabase(Vertx vertx, String url, String driverClass) {

        JsonObject config = new JsonObject()
                .put("url", url)
                .put("driver_class", driverClass)
                .put("max_pool_size", 30);

        logger.info("Connecting to database with config = {}", config);

        return JDBCClient.createShared(vertx, config);
    }
}
