package projects.sensor.api.router;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.reactivex.Single;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import projects.sensor.api.App;
import projects.sensor.api.service.SensorApiImpl;
import projects.sensor.api.util.FileUtil;
import projects.sensor.api.util.TimeUtil;
import projects.sensor.api.databse.DatabaseClient;
import projects.sensor.model.GetDataResponse;
import projects.sensor.model.GetSensorResponse;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class OpenApiRouter extends AbstractVerticle {

    private static final String SPEC_FILE = "api.yaml";
    private static final String SWAGGER_UI_DIR = "swagger-ui";
    private static final int PORT = 9090;

    private Vertx vertx;

    private DatabaseClient databaseClient;
    private SensorApiImpl sensorApiImpl;

    private final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public OpenApiRouter(Vertx vertx, SensorApiImpl sensorApiImpl) {
        this.vertx = vertx;
        this.sensorApiImpl = sensorApiImpl;

        // allow jackson to decode java.time.LocalDateTime
        DatabindCodec.mapper().registerModule(new JavaTimeModule());

        // Todo - these fields should be read from config
        String databasePath = System.getProperty("user.dir") + "/target/db/test.db";
        String databaseUrl = "jdbc:sqlite:" + databasePath;
        String databaseDriverClass = "org.sqlite.JDBC";

        // Todo keep all database implementation within DataBaseClient
        databaseClient = DatabaseClient.getInstance();
        databaseClient.connectToDatabase(vertx, databaseUrl, databaseDriverClass);
    }

    public Single<Router> buildRouterFromSpec() {
        LOGGER.info("OpenApiRouter - Loading spec {}", SPEC_FILE);
        RouterBuilder.create(vertx.getDelegate(), SPEC_FILE)
                .onSuccess(routerBuilder -> {


//                    routerBuilder.securityHandler("ApiKeyAuth")
//                            .bindBlocking(config ->
//                                    APIKeyHandler.create(authProvider)
//                                            .header(config.getString("name")));

                    RouterBuilderOptions options = new RouterBuilderOptions();
                    options.setOperationModelKey("operationModel");
                    routerBuilder.setOptions(options);

                    routerBuilder.operation("logData")
                            .handler(routingContext -> sensorApiImpl.logData(routingContext))
                            .failureHandler(routingContext -> badRequestResponse(routingContext));

                    routerBuilder.operation("getDataForDate")
                            .handler(routingContext -> sensorApiImpl.getDataForDate(routingContext))
                            .failureHandler(routingContext -> badRequestResponse(routingContext));

                    routerBuilder.operation("getDataForDateRange")
                            .handler(routingContext -> sensorApiImpl.getDataForDateRange(routingContext))
                            .failureHandler(routingContext -> badRequestResponse(routingContext));

                    routerBuilder.operation("createSensor")
                            .handler(routingContext -> sensorApiImpl.createSensor(routingContext))
                            .failureHandler(routingContext -> badRequestResponse(routingContext));

                    routerBuilder.operation("listSensors")
                            .handler(routingContext -> sensorApiImpl.getSensor(routingContext))
                            .failureHandler(routingContext -> badRequestResponse(routingContext));

                    routerBuilder.operation("getSensor")
                            .handler(routingContext -> sensorApiImpl.getSensor(routingContext))
                            .failureHandler(routingContext -> badRequestResponse(routingContext));

                    // Generate the router
                    Router router = routerBuilder.createRouter();

                    /**
                     * Moves swagger-ui webjar files into a swagger-ui directory
                     * Moves the api.yaml into this directory
                     * Replaces swagger-initializer.js with a version that points to api.yaml
                     * Creates the swagger-ui endpoint
                     */
                    loadWebJars("META-INF/resources/webjars/swagger-ui/5.9.0", SWAGGER_UI_DIR)
                            .doOnError(e -> LOGGER.error("Failed to create swagger-ui endpoint, exception = {}", e))
                            .subscribe(r -> {
                                Single.concat(
                                        FileUtil.replaceFile(vertx.fileSystem(), "swagger-initializer-override.js", SWAGGER_UI_DIR + "/swagger-initializer.js"),
                                        FileUtil.copyFile(vertx.fileSystem(), SPEC_FILE, SWAGGER_UI_DIR + "/" + SPEC_FILE, true)
                                ).subscribe(res -> {
                                    router.route("/*").handler(StaticHandler.create(SWAGGER_UI_DIR));
                                    LOGGER.info("Created swagger-ui endpoint successfully");
                                }, e -> LOGGER.error("Failed to create swagger-ui endpoint, exception = {}", e));
                            });

                    router.errorHandler(404, routingContext -> {
                        JsonObject errorObject = new JsonObject();
                        errorObject.put("code", 404);
                        errorObject.put("message",
                                (routingContext.failure() != null) ? routingContext.failure().getMessage() : "Not Found"
                        );
                        routingContext.response()
                                .setStatusCode(404)
                                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .end(errorObject.encode());
                    });

//                    // Start server instance
//                    HttpServer server = vertx.getDelegate().createHttpServer(new HttpServerOptions()
//                            .setPort(PORT)
//                            .setHost("localhost"));
//                    server.requestHandler(router).listen().onSuccess(r ->
//                            LOGGER.info("OpenApiRouter - Started listening on port {}", PORT));

                    LOGGER.info("OpenApiRouter - Spec loaded successfully");



                })
                .onFailure(err -> {
                    // Something went wrong during router builder initialization
                    LOGGER.error("OpenApiRouter - Failed to load spec!");
                });

    }

    // Todo - Move to class with all responses
    private void badRequestResponse(RoutingContext routingContext) {
        JsonObject operation = routingContext.get("operationModel");
        routingContext.response()
                .setStatusCode(400)
                .setStatusMessage("Bad Request")
                .end();
        LOGGER.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
    }


    // Mock a response
    private JsonObject getDataMock() {
        int n = 4;
        GetDataResponse[] dataPoints = new GetDataResponse[n];//
        for (int i = 0; i < n; i++) {
            int temperature = (int) (Math.random() * 30);
            int humidity = (int) (Math.random() * 100);
            dataPoints[i] = new GetDataResponse(temperature, humidity, String.valueOf(i));
        }
        return new JsonObject().put("data", dataPoints);
    }

    // Mock a reponse
    private JsonObject listSensorsMock() {
        GetSensorResponse[] sensors = new GetSensorResponse[4];
        sensors[0] = new GetSensorResponse("1", "somewhere", "01-01-2023");
        sensors[1] = new GetSensorResponse("2", "somewhere", "01-01-2023");
        sensors[2] = new GetSensorResponse("3", "somewhere else", "01-01-2023");
        sensors[3] = new GetSensorResponse("4", "somewhere slse", "01-01-2023");
        return new JsonObject().put("data", sensors);
    }

    private JsonObject getSensorMock(String sensorId) {
        GetSensorResponse sensor = new GetSensorResponse(sensorId, "somewhere", "0/0/0 00:00:00");
        return new JsonObject().put("data", sensor);
    }

    // Todo - test this method with mock fileSystem
    private Single<File> loadWebJars(String source, String dest) {
            return FileUtil.fileExists(vertx.fileSystem(), dest)
                    .flatMap(destExists -> {
                        // If destination doesn't exist, we indicate that files are simply to be copied
                        // Else it exists, so we indicate that the files should be replaced
                        if (!destExists) {
                            return FileUtil.createDirectory(vertx.fileSystem(), dest)
                                    .map(file -> false);
                        } else {
                            LOGGER.info("Destination directory {} already exists", dest);
                            return Single.just(true);
                        }
                    })
                    .flatMap(replaceExisting -> FileUtil.fileExists(vertx.fileSystem(), source)
                            .flatMap(sourceExist -> {
                                if (sourceExist) {
                                    LOGGER.info("Copying from {} to {}", source, dest);
                                    return FileUtil.copyFiles(vertx.fileSystem(), source, dest, replaceExisting);
                                } else {
                                    LOGGER.error("Source directory {} not found", source);
                                    return null;
                                }
                            }));
    }




}
