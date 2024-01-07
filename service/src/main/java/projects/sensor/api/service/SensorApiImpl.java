package projects.sensor.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.Main;
import projects.sensor.api.database.DataBaseClient;
import projects.sensor.model.UpdateRequest;
import projects.sensor.api.util.TimeUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import static projects.sensor.api.util.ResponseUtil.*;

/**
 * Contains the main application logic
 */
public class SensorApiImpl implements SensorApi {

    DataBaseClient databaseClient;

    private final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public SensorApiImpl(DataBaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    // Todo - write unit tests

    @Override
    public void logData(RoutingContext routingContext) {

        // Parse the request
        RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        RequestParameter body = params.body();
        UpdateRequest updateRequest = body != null ? DatabindCodec.mapper().convertValue(body.get(), new TypeReference<UpdateRequest>(){}) : null;

        // Add a timestamp to logged with the data
        Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        String dateTimeString = new SimpleDateFormat("y-MM-dd HH:mm:ss.SSS").format(timestamp);
        LOGGER.info("logData - request = {}, time = {}", updateRequest, dateTimeString);  // Todo - change log level to debug

        // Todo look into codegen - generate pojos should be using @NotNull for required parameters
        // Extract queryParams and pass to database client
        JsonArray queryParams = new JsonArray();
        queryParams.add(updateRequest.getSensorId());
        queryParams.add(updateRequest.getTemperature());
        queryParams.add(updateRequest.getHumidity());
        queryParams.add(dateTimeString);
        this.databaseClient.insertData(queryParams).subscribe(result -> createdResponse(routingContext),
                e -> internalServerError(routingContext));
    }

    @Override
    public void getDataForDate(RoutingContext routingContext) {
        RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        RequestParameter sensorId = params.pathParameter("sensorId");
        RequestParameter body = params.body();
        JsonObject jsonRequest = body.getJsonObject();
        LOGGER.info("getDataForDate - sensorId = {}, jsonRequest = {}", sensorId, jsonRequest.encodePrettily());

        // ToDo - validate the parameters - read into generated model class

        // If selecting entries for 2023-11-28, a "from" and "until" range is created
        // from = '2023-11-28 00:00:00' and "until" '2023-11-29 00:00:00'
        String from = TimeUtil.getDateTimeString(jsonRequest);
        // Add one calendar date, or hour if not null
        String until = TimeUtil.getDateTimeStringNextInterval(jsonRequest);

        JsonArray queryParams = new JsonArray();
        queryParams.add(from);
        queryParams.add(until);

        this.databaseClient.selectData(queryParams).subscribe(resultSet -> {
            JsonObject jsonResponse = new JsonObject().put("data", resultSet.getRows());
            LOGGER.info("getData - result = {}", jsonResponse.encodePrettily());
            okResponse(routingContext, jsonResponse);
        }, e -> internalServerError(routingContext));
    }

    @Override
    public void getDataForDateRange(RoutingContext routingContext) {
        // Todo - validate these parameters
        RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        RequestParameter sensorId = params.pathParameter("sensorId");
        RequestParameter body = params.body();
        JsonObject jsonRequest = body.getJsonObject();
        LOGGER.info("getDataForDateRange - sensorId = {}, jsonRequest = {}", sensorId, jsonRequest.encodePrettily());

        String from = TimeUtil.getDateTimeString(jsonRequest.getJsonObject("from"));
        // The range is inclusive of the specified untilDate/untilHour
        String until = TimeUtil.getDateTimeStringNextInterval(jsonRequest.getJsonObject("until"));

        JsonArray queryParams = new JsonArray();
        queryParams.add(from);
        queryParams.add(until);

        this.databaseClient.selectData(queryParams).subscribe(resultSet -> {
            JsonObject jsonResponse = new JsonObject().put("data", resultSet.getRows());
            LOGGER.info("getDataForDateRange - result = {}", jsonResponse.encodePrettily());
            okResponse(routingContext, jsonResponse);
        }, e -> internalServerError(routingContext));
    }

    @Override
    public void createSensor(RoutingContext routingContext) {
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

        LOGGER.info("createSensor - sensorId = {}, location = {}, creationTime = {}", sensorId, location, dateTimeString);

        databaseClient.insertSensor(queryParams).subscribe(result -> {
            createdResponse(routingContext);
            LOGGER.info("createSensor complete");
        }, e -> internalServerError(routingContext));
    }

    @Override
    public void listSensors(RoutingContext routingContext) {
        LOGGER.info("listSensors - no params");

        databaseClient.selectAllSensors().subscribe(resultSet -> {
            JsonObject jsonResponse = new JsonObject().put("data", resultSet.getRows());
            LOGGER.info("listSensors - result = {}", jsonResponse.encodePrettily());
            okResponse(routingContext, jsonResponse);
        }, e -> internalServerError(routingContext));
    }

    @Override
    public void getSensor(RoutingContext routingContext) {
        RequestParameters  params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        RequestParameter sensorId = params.pathParameter("sensorId");
        LOGGER.info("getSensor - sensorId = {}", sensorId);

        JsonArray queryParams = new JsonArray();
        queryParams.add(sensorId);
        databaseClient.selectSensor(queryParams).subscribe(resultSet -> {
            JsonObject jsonResponse = new JsonObject().put("data", resultSet.getRows());
            okResponse(routingContext, jsonResponse);
        }, e -> internalServerError(routingContext));
    }
}
