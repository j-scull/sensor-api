package projects.sensor.api.verticle;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.reactivex.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.Main;
import projects.sensor.api.databse.SQLiteClient;
import projects.sensor.api.router.OpenApiRouter;
import projects.sensor.api.service.SensorApiImpl;

public class RestServerVerticle extends AbstractVerticle {

    private static final int DEFAULT_PORT = 9090;
    private static final String DEFAULT_HOST = "localhost";

    private final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    @Override
    public void start() {

        // allow jackson to decode java.time.LocalDateTime
        DatabindCodec.mapper().registerModule(new JavaTimeModule());

        // Todo - these fields should be read from config
        String databasePath = System.getProperty("user.dir") + "/target/db/test.db";
        String databaseUrl = "jdbc:sqlite:" + databasePath;
        String databaseDriverClass = "org.sqlite.JDBC";
        SQLiteClient databaseClient = new SQLiteClient(vertx, databaseUrl, databaseDriverClass);

        SensorApiImpl sensorApiImpl = new SensorApiImpl(databaseClient);

        OpenApiRouter openApiRouter = new OpenApiRouter(vertx, sensorApiImpl);
        openApiRouter.buildRouterFromSpec()
                .andThen(routerAsyncResult -> createHttpServer(routerAsyncResult.result()))
                .onSuccess(r -> LOGGER.info("SensorApiVerticle - Started successfully"))
                .onFailure(e -> LOGGER.error("SensorApiVerticle - Failed to create HTTP server"));
    }

    public void createHttpServer(Router router) {
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

}
