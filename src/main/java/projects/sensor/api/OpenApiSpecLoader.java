package projects.sensor.api;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.APIKeyHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import projects.sensor.model.DataPoint;
import projects.sensor.model.Sensor;

import java.util.Date;


public class OpenApiSpecLoader {

    private static final String SPEC_FILE = "api.yaml";
    private static final int PORT = 8080;

    private Vertx vertx;

    private final Logger logger = LoggerFactory.getLogger(App.class);

    public OpenApiSpecLoader() {

        vertx = Vertx.vertx();
    }

    public void loadSpec(){
        logger.info("OpenApiSpecLoader - Loading spec {}", SPEC_FILE);
        RouterBuilder.create(vertx, SPEC_FILE)
                .onSuccess(routerBuilder -> {

                    logger.info("OpenApiSpecLoader - Spec loaded successfully");

//                    routerBuilder.securityHandler("ApiKeyAuth")
//                            .bindBlocking(config ->
//                                    APIKeyHandler.create(authProvider)
//                                            .header(config.getString("name")));

                    routerBuilder.operation("logData").handler(routingContext -> {
                        RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                        RequestParameter temperature = params.queryParameter("temperature");
                        RequestParameter humidity = params.queryParameter("humidity");
                        Date dateTime = new Date();
                        logger.info("logData - temperature = {}, humidity = {}, time = {}", temperature, humidity, dateTime);
                        routingContext.response()
                                .setStatusCode(201)
                                .setStatusMessage("OK")
                                .end();
                    });

                    routerBuilder.operation("listDataPoints").handler(routingContext -> {
                        RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                        RequestParameter sensorId = params.pathParameter("sensorId");
                        RequestParameter year = params.queryParameter("year");
                        RequestParameter month = params.queryParameter("month");
                        RequestParameter date = params.queryParameter("date");
                        logger.info("listDataPoints - sensorId = {}, year = {}, month = {}, date = {}", sensorId, year, month, date);
                        routingContext.response()
                                .setStatusCode(200)
                                .setStatusMessage("OK")
                                .end(getDataPoints().toBuffer());
                    });

                    routerBuilder.operation("dataPointsRange").handler(routingContext -> {
                        RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                        RequestParameter sensorId = params.pathParameter("sensorId");
                        RequestParameter start = params.queryParameter("start");
                        RequestParameter stop = params.queryParameter("stop");
                        logger.info("listDataPoints - sensorId = {}, start = {}, stop = {}", sensorId, start, stop);

                        routingContext.response()
                                .setStatusCode(200)
                                .setStatusMessage("OK")
                                .end(getDataPoints().toBuffer());
                    });

                    routerBuilder.operation("listSensors").handler(routingContext -> {
                        RequestParameters  params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
                        logger.info("listSensors - params = {}", params);
                        routingContext.response()
                                .setStatusCode(200)
                                .setStatusMessage("OK")
                                .end(getSensors().toBuffer());
                    });

                    // Generate the router
                    Router router = routerBuilder.createRouter();

                    // Start server instance
                    HttpServer server = vertx.createHttpServer(new HttpServerOptions()
                            .setPort(PORT)
                            .setHost("localhost"));
                    server.requestHandler(router).listen().onSuccess(r ->
                            logger.info("OpenApiSpecLoader - Started listening on port {}", PORT));

                })
                .onFailure(err -> {
                    // Something went wrong during router builder initialization
                    logger.error("OpenApiSpecLoader - Failed to load spec!");
                });
    }

    private JsonObject getDataPoints() {
        int n = 4;
        DataPoint[] dataPoints = new DataPoint[n];//
        for (int i = 0; i < n; i++) {
            int temperature = (int) (Math.random() * 30);
            int humidity = (int) (Math.random() * 100);
            dataPoints[i] = new DataPoint(temperature, humidity, String.valueOf(i));
        }
        return new JsonObject().put("data", dataPoints);
    }

    private JsonObject getSensors() {
        Sensor[] sensors = new Sensor[4];
        sensors[0] = new Sensor("1", "somewhere", "01-01-2023");
        sensors[1] = new Sensor("2", "somewhere", "01-01-2023");
        sensors[2] = new Sensor("3", "somewhere else", "01-01-2023");
        sensors[3] = new Sensor("4", "somewhere slse", "01-01-2023");
        return new JsonObject().put("data", sensors);
    }

}
