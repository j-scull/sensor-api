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
import projects.sensor.api.util.TimeUtil;
import projects.sensor.db.DatabaseClient;
import projects.sensor.model.DataResponse;
import projects.sensor.model.GetSensorResponse;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class OpenApiRouter {

    private static final String SPEC_FILE = "api.yaml";
    private static final String SWAGGER_UI_DIR = "swagger-ui";
    private static final int PORT = 9090;

    private Vertx vertx;

    private DatabaseClient databaseClient;

    private final Logger LOGGER = LoggerFactory.getLogger(App.class);

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
                            .handler(routingContext -> {

                                RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter body = params.body();
                                JsonObject jsonBody = body.getJsonObject();
                                String sensorId = jsonBody.getString("sensorId");
                                String temperature = jsonBody.getString("temperature");
                                String humidity = jsonBody.getString("humidity");
                                Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
                                String dateTimeString = new SimpleDateFormat("y-MM-dd HH:mm:ss.SSS").format(timestamp);
                                LOGGER.info("logData - sensorId = {}, temperature = {}, humidity = {}, time = {}", sensorId, temperature, humidity, dateTimeString);  // Todo - change log level to debug

                                // Todo - validate data. Ensure sensorId is in the database

                                JsonArray queryParams = new JsonArray();
                                queryParams.add(sensorId);
                                queryParams.add(temperature);
                                queryParams.add(humidity);
                                queryParams.add(dateTimeString);
                                this.databaseClient.logData(queryParams).subscribe(result ->
                                                routingContext.response()
                                                    .setStatusCode(201)
                                                    .setStatusMessage("OK")
                                                    .end(),
                                                e -> routingContext.response()
                                                    .setStatusCode(500)
                                                    .setStatusMessage("Internal Server Error")
                                                    .end());


                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                LOGGER.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    routerBuilder.operation("getData")
                            .handler(routingContext -> {

                                RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter sensorId = params.pathParameter("sensorId");
                                RequestParameter year = params.queryParameter("year");
                                RequestParameter month = params.queryParameter("month");
                                RequestParameter date = params.queryParameter("date");
                                RequestParameter hour = params.queryParameter("hour");
                                if (hour != null) {
                                    LOGGER.info("getData - sensorId = {}, year = {}, month = {}, date = {}, hour = {}", sensorId, year, month, date, hour);
                                } else {
                                    LOGGER.info("getData - sensorId = {}, year = {}, month = {}, date = {}", sensorId, year, month, date);
                                }

                                // ToDo - validate the parameters

                                // If selecting entries for 2023-11-28, a "from" and "until" range is created
                                // from = '2023-11-28 00:00:00' and "until" '2023-11-29 00:00:00'
                                String from = TimeUtil.getDateTimeString(year, month, date, hour);
                                // Add one calendar date, or hour if not null
                                String until = TimeUtil.getDateTimeStringNextInterval(year, month, date, hour);

                                JsonArray queryParams = new JsonArray();
                                queryParams.add(from);
                                queryParams.add(until);

                                this.databaseClient.getData(queryParams).subscribe(resultSet -> {
                                    JsonObject jsonResponse = new JsonObject().put("data", resultSet.getRows());
                                    LOGGER.info("getData - result = {}", jsonResponse);
                                    routingContext.response()
                                            .setStatusCode(200)
                                            .setStatusMessage("OK")
                                            .end(jsonResponse.toBuffer());
                                    }, e -> routingContext.response()
                                            .setStatusCode(500)
                                            .setStatusMessage("Internal Server Error")
                                            .end());

                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                LOGGER.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    routerBuilder.operation("getDataRange")
                            .handler(routingContext -> {

                                // Todo - validate these parameters
                                RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter sensorId = params.pathParameter("sensorId");
                                RequestParameter fromYear = params.queryParameter("fromYear");
                                RequestParameter fromMonth = params.queryParameter("fromMonth");
                                RequestParameter fromDate = params.queryParameter("fromDate");
                                RequestParameter fromHour = params.queryParameter("fromHour");
                                RequestParameter untilYear = params.queryParameter("untilYear");
                                RequestParameter untilMonth = params.queryParameter("untilMonth");
                                RequestParameter untilDate = params.queryParameter("untilDate");
                                RequestParameter untilHour = params.queryParameter("untilHour");

                                if (fromHour != null && untilHour != null) {
                                    LOGGER.info("getDataRange - sensorId = {}, fromYear = {}, fromMonth = {}, fromDate = {}, fromHour = {}, untilYear = {}, untilMonth = {}, untilDate = {}, untilHour = {}",
                                            sensorId, fromYear, fromMonth, fromDate, fromHour, untilYear, untilMonth, untilDate, untilHour);
                                } else {
                                    LOGGER.info("getDataRange - sensorId = {}, fromYear = {}, fromMonth = {}, fromDate = {}, untilYear = {}, untilMonth = {}, untilDate = {}",
                                            sensorId, fromYear, fromMonth, fromDate, untilYear, untilMonth, untilDate);
                                }

                                String from = TimeUtil.getDateTimeString(fromYear, fromMonth, fromDate, fromHour);
                                // The range is inclusive of the specified untilDate/untilHour
                                String until = TimeUtil.getDateTimeStringNextInterval(untilYear, untilMonth, untilDate, untilHour);

                                JsonArray queryParams = new JsonArray();
                                queryParams.add(from);
                                queryParams.add(until);

                                this.databaseClient.getData(queryParams).subscribe(resultSet -> {
                                    JsonObject jsonResponse = new JsonObject().put("data", resultSet.getRows());
                                    LOGGER.info("getDataRange - result = {}", jsonResponse);
                                    routingContext.response().setStatusCode(200)
                                            .setStatusMessage("OK")
                                            .end(jsonResponse.toBuffer());
                                    }, e -> routingContext.response().setStatusCode(500)
                                            .setStatusMessage("Internal Server Error")
                                            .end());

                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                LOGGER.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    routerBuilder.operation("addSensor")
                            .handler(routingContext -> {

                                RequestParameters  params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter body = params.body();
                                JsonObject jsonBody = body.getJsonObject();
                                String sensorId = jsonBody.getString("sensorId");
                                String location = jsonBody.getString("location");
                                Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
                                String dateTimeString = new SimpleDateFormat("y-MM-dd HH:mm:ss.SSS").format(timestamp);

                                JsonArray queryParams = new JsonArray();
                                queryParams.add(sensorId);
                                queryParams.add(location);
                                queryParams.add(dateTimeString);

                                LOGGER.info("addSensor - sensorId = {}, location = {}, creationTime = {}", sensorId, location, dateTimeString);

                                databaseClient.addSensor(queryParams).subscribe(result -> {
                                    routingContext.response()
                                            .setStatusCode(201)
                                            .setStatusMessage("OK")
                                            .end();
                                    LOGGER.info("addSensor complete");
                                    }, e ->  routingContext.response()
                                                .setStatusCode(500)
                                                .setStatusMessage("Internal Server Error")
                                                .end());

                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                LOGGER.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    routerBuilder.operation("listSensors")
                            .handler(routingContext -> {

                                LOGGER.info("listSensors - no params");

                                databaseClient.listSensors().subscribe(resultSet -> {
                                    JsonObject jsonResponse = new JsonObject().put("data", resultSet.getRows());
                                    routingContext.response()
                                            .setStatusCode(200)
                                            .setStatusMessage("OK")
                                            .end(jsonResponse.toBuffer());
                                    }, e -> routingContext.response()
                                            .setStatusCode(500)
                                            .setStatusMessage("Internal Server Error")
                                            .end());

                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                LOGGER.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
                            });

                    routerBuilder.operation("getSensor")
                            .handler(routingContext -> {

                                RequestParameters  params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                                RequestParameter sensorId = params.pathParameter("sensorId");
                                LOGGER.info("getSensor - sensorId = {}", sensorId);

                                JsonArray queryParams = new JsonArray();
                                queryParams.add(sensorId);
                                databaseClient.getSensor(queryParams).subscribe(resultSet -> {
                                    JsonObject jsonResponse = new JsonObject().put("data", resultSet.getRows());
                                    routingContext.response()
                                            .setStatusCode(200)
                                            .setStatusMessage("OK")
                                            .end(jsonResponse.toBuffer());
                                    }, e -> routingContext.response()
                                            .setStatusCode(500)
                                            .setStatusMessage("Internal Server Error")
                                            .end());

                            }).failureHandler(routingContext -> {
                                JsonObject operation = routingContext.get("operationModel");
                                routingContext.response()
                                        .setStatusCode(400)
                                        .setStatusMessage("Bad Request")
                                        .end();
                                LOGGER.error("{} error - {}", operation.getString("operationId"), routingContext.failure());
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

                    // Start server instance
                    HttpServer server = vertx.getDelegate().createHttpServer(new HttpServerOptions()
                            .setPort(PORT)
                            .setHost("localhost"));
                    server.requestHandler(router).listen().onSuccess(r ->
                            LOGGER.info("OpenApiRouter - Started listening on port {}", PORT));

                    LOGGER.info("OpenApiRouter - Spec loaded successfully");

                })
                .onFailure(err -> {
                    // Something went wrong during router builder initialization
                    LOGGER.error("OpenApiRouter - Failed to load spec!");
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
