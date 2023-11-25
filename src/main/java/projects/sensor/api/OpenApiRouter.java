package projects.sensor.api;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.reactivex.Single;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;
import io.vertx.reactivex.core.Vertx;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import projects.sensor.api.util.FileUtil;
import projects.sensor.db.DatabaseClient;
import projects.sensor.model.DataResponse;
import projects.sensor.model.GetSensorResponse;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;

public class OpenApiRouter {

    private static final String SPEC_FILE = "api.yaml";
    private static final String SWAGGER_UI_DIR = "swagger-ui";
    private static final int PORT = 9090;

    private Vertx vertx;

    private DatabaseClient databaseClient;

    private final Logger logger = LoggerFactory.getLogger(App.class);

    public OpenApiRouter() {
        vertx = Vertx.vertx();

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

    public void loadSpec() {
        logger.info("OpenApiRouter - Loading spec {}", SPEC_FILE);
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
                            .handler(routingContext -> {
                                RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter body = params.body();
                                JsonObject jsonBody = body.getJsonObject();
                                String sensorId = jsonBody.getString("sensorId");
                                String temperature = jsonBody.getString("temperature");
                                String humidity = jsonBody.getString("humidity");
                                long time = new Timestamp(System.currentTimeMillis()).getTime();
                                logger.info("logData - sensorId = {}, temperature = {}, humidity = {}, time = {}", sensorId, temperature, humidity, time);  // Todo - change log level to debug
                                jsonBody.put("time", time);
                                this.databaseClient.logData(jsonBody);
                                routingContext.response()
                                        .setStatusCode(201)
                                        .setStatusMessage("OK")
                                        .end();
                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                logger.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    routerBuilder.operation("getData")
                            .handler(routingContext -> {
                                RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter sensorId = params.pathParameter("sensorId");
                                RequestParameter year = params.queryParameter("year");
                                RequestParameter month = params.queryParameter("month");
                                RequestParameter date = params.queryParameter("date");
                                RequestParameter hour = params.queryParameter("hour");
                                JsonArray dbParams = new JsonArray().add(sensorId).add(year).add(month).add(date);
                                if (hour != null) {
                                    dbParams.add(hour);
                                    logger.info("getData - sensorId = {}, year = {}, month = {}, date = {}, hour = {}", sensorId, year, month, date, hour);
                                } else {
                                    logger.info("getData - sensorId = {}, year = {}, month = {}, date = {}", sensorId, year, month, date);
                                }
                                this.databaseClient.getData(routingContext.response(), dbParams);


                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                logger.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    routerBuilder.operation("getDataRange")
                            .handler(routingContext -> {
                                RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter sensorId = params.pathParameter("sensorId");
                                RequestParameter start = params.queryParameter("start");
                                RequestParameter stop = params.queryParameter("stop");
                                logger.info("getDataRange - sensorId = {}, start = {}, stop = {}", sensorId, start, stop);

                                routingContext.response()
                                        .setStatusCode(200)
                                        .setStatusMessage("OK")
                                        .end(getDataMock().toBuffer());
                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                logger.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    routerBuilder.operation("listSensors")
                            .handler(routingContext -> {
                                RequestParameters  params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                logger.info("listSensors - params = {}", params);
                                routingContext.response()
                                        .setStatusCode(200)
                                        .setStatusMessage("OK")
                                        .end(listSensorsMock().toBuffer());
                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                logger.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    routerBuilder.operation("getSensor")
                            .handler(routingContext -> {
                                RequestParameters  params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter sensorId = params.pathParameter("sensorId");
                                logger.info("getSensor - params = {}", params);
                                routingContext.response()
                                        .setStatusCode(200)
                                        .setStatusMessage("OK")
                                        .end(getSensorMock(sensorId.getString()).toBuffer());
                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                logger.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    routerBuilder.operation("addSensor")
                            .handler(routingContext -> {
                                RequestParameters  params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter body = params.body();
                                JsonObject jsonBody = body.getJsonObject();
                                logger.info("addSensor - sensorId = {}, location = {}", jsonBody.getString("sensorId"), jsonBody.getString("location"));
                                routingContext.response()
                                        .setStatusCode(201)
                                        .setStatusMessage("OK")
                                        .end();
                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                logger.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    // Generate the router
                    Router router = routerBuilder.createRouter();

                    /**
                     * Moves swagger-ui webjar files into a swagger-ui directory
                     * Moves the api.yaml into this directory
                     * Replaces swagger-initializer.js with a version that points to api.yaml
                     * Creates the swagger-ui endpoint
                     */
                    loadWebJars("META-INF/resources/webjars/swagger-ui/5.9.0", SWAGGER_UI_DIR)
                            .doOnError(e -> logger.error("Failed to create swagger-ui endpoint, exception = {}", e))
                            .subscribe(r -> {
                                Single.concat(
                                        FileUtil.replaceFile(vertx.fileSystem(), "swagger-initializer-override.js", SWAGGER_UI_DIR + "/swagger-initializer.js"),
                                        FileUtil.copyFile(vertx.fileSystem(), SPEC_FILE, SWAGGER_UI_DIR + "/" + SPEC_FILE)
                                ).subscribe(res -> {
                                    router.route("/*").handler(StaticHandler.create(SWAGGER_UI_DIR));
                                    logger.info("Created swagger-ui endpoint successfully");
                                }, e -> logger.error("Failed to create swagger-ui endpoint, exception = {}", e));
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

                    // Start server instance
                    HttpServer server = vertx.getDelegate().createHttpServer(new HttpServerOptions()
                            .setPort(PORT)
                            .setHost("localhost"));
                    server.requestHandler(router).listen().onSuccess(r ->
                            logger.info("OpenApiRouter - Started listening on port {}", PORT));

                    logger.info("OpenApiRouter - Spec loaded successfully");

                })
                .onFailure(err -> {
                    // Something went wrong during router builder initialization
                    logger.error("OpenApiRouter - Failed to load spec!");
                });

    }

    private void dbResponseHandler(List<JsonObject> dbResponseJson) {


    }

//    private void dbResponseHandler(RoutingContext routingContext, List<JsonObject> dbResponseJson) {
//        routingContext.response()
//                .setStatusCode(200)
//                .setStatusMessage("OK")
//                .end((Buffer)dbResponseJson);
//    }

    // Mock a response
    private JsonObject getDataMock() {
        int n = 4;
        DataResponse[] dataPoints = new DataResponse[n];//
        for (int i = 0; i < n; i++) {
            int temperature = (int) (Math.random() * 30);
            int humidity = (int) (Math.random() * 100);
            dataPoints[i] = new DataResponse(temperature, humidity, String.valueOf(i));
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

    private Single<File> loadWebJars(String source, String dest) {
        return FileUtil.createDirectory(vertx.fileSystem(), dest)
                .flatMap(fileSystem -> FileUtil.fileExists(vertx.fileSystem(), source))
                .flatMap(exist -> {
                    if(exist) {
                        logger.info("Copying from {} to {}", source, dest);
                        return FileUtil.copyFiles(vertx.fileSystem(), source, dest);
                    } else {
                        logger.error("Directory {} not found", source);
                        return null;
                    }
                });
    }




}
