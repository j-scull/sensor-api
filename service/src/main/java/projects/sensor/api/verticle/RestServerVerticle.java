package projects.sensor.api.verticle;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.Main;
import projects.sensor.api.config.ConfigReader;
import projects.sensor.api.config.Config;
import projects.sensor.api.config.database.MySQLConfig;
import projects.sensor.api.config.database.SQLiteConfig;
import projects.sensor.api.database.DatabaseClient;
import projects.sensor.api.database.MySQLClient;
import projects.sensor.api.database.SQLiteClient;
import projects.sensor.api.router.OpenApiRouter;
import projects.sensor.api.service.SensorApi;
import projects.sensor.api.service.SensorApiImpl;

import java.io.IOException;

public class RestServerVerticle extends AbstractVerticle {

    private static final int DEFAULT_PORT = 9090;
    private static final String DEFAULT_HOST = "localhost";

    private final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    @Override
    public void start() {

        // allow jackson to decode java.time.LocalDateTime
        DatabindCodec.mapper().registerModule(new JavaTimeModule());

        // Create the router, service and databaseclient from config
        try {
            Config config = ConfigReader.getConfig();
            DatabaseClient databaseClient = setupDatabaseClient(vertx, config);
            SensorApiImpl sensorApiImpl = new SensorApiImpl(databaseClient);
            OpenApiRouter openApiRouter = new OpenApiRouter(vertx, config, sensorApiImpl);
            openApiRouter.buildRouterFromSpec()
                    .andThen(routerAsyncResult -> createHttpServer(routerAsyncResult.result()))
                    .onSuccess(r -> LOGGER.info("SensorApiVerticle - Started successfully"))
                    .onFailure(e -> LOGGER.error("SensorApiVerticle - Failed to create HTTP server"));
        } catch (IOException e) {
            LOGGER.error("Unable to read from default config file, exception = {}", e.getMessage());
        }
    }

    public void createHttpServer(Router router) {
        LOGGER.info("RestServerVerticle - creating HTTP server with host={} and port={}", DEFAULT_HOST, DEFAULT_PORT);
        if (router != null) {
            // Start server instance
            HttpServer server = vertx.getDelegate().createHttpServer(new HttpServerOptions()
                    .setPort(DEFAULT_PORT)
                    .setHost(DEFAULT_HOST));
            server.requestHandler(router).listen()
                    .onSuccess(r -> LOGGER.info("RestServerVerticle - Started listening on port {}", DEFAULT_PORT))
                    .onFailure(e -> LOGGER.error("RestServerVerticle - Failed to create HTTP Server, exception = {}", e));
        } else {
            throw new RuntimeException("RestServerVerticle - Failed to create HTTP Server, router cannot be null");
        }
    }

    private DatabaseClient setupDatabaseClient(Vertx vertx, Config config) {
        if (config.getDatabaseConfig() instanceof SQLiteConfig) {
            SQLiteConfig sqLiteConfigConfig = (SQLiteConfig) config.getDatabaseConfig();
            return new SQLiteClient(vertx, sqLiteConfigConfig);
        } else {
            // There are only two  implementations of DatabaseConfig
            MySQLConfig mySQLConfig = (MySQLConfig) config.getDatabaseConfig();
            return new MySQLClient(mySQLConfig);
        }
    }

}
