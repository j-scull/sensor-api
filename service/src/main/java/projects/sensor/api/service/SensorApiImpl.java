package projects.sensor.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import projects.sensor.api.config.Config;
import projects.sensor.api.database.DatabaseClient;
import projects.sensor.api.service.exceptions.NotFoundException;
import projects.sensor.model.CreateSensorRequest;
import projects.sensor.model.UpdateRequest;
import projects.sensor.api.util.TimeUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.TimeZone;

import static projects.sensor.api.util.ResponseUtil.*;

/**
 * Contains the main application logic
 */
public class SensorApiImpl implements SensorApi {

    private Config config;
    private DatabaseClient databaseClient;

    private ObjectMapper mapper;

    private final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final String TIME_ZONE = "UTC";

    private static final String SENSOR_ID = "sensorId";
    private static final String DATE_TIME = "dateTime";
    private static final String FROM = "from";
    private static final String UNTIL = "until";

    public SensorApiImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
        // Todo - extract to util class
        this.mapper = DatabindCodec.mapper();
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.registerModule(new JavaTimeModule());
        LOGGER.info("SensorApiImpl - successfully created");
    }

    @Override
    public void logData(RoutingContext routingContext) {

        // Parse the request, vertx will validate requests against the api specification
        RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        RequestParameter body = params.body();
        UpdateRequest updateRequest = body != null ? mapper.convertValue(body.get(), new TypeReference<UpdateRequest>(){}) : null;

        // Add a timestamp to logged with the data
        Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        String dateTimeString = simpleDateFormat.format(timestamp);
        LOGGER.info("logData - request = {}, time = {}", updateRequest, dateTimeString);  // Todo - change log level to debug

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
        RequestParameter sensorId = params.pathParameter(SENSOR_ID);
        OffsetDateTime dateTime = OffsetDateTime.parse(params.queryParameter(DATE_TIME).getString());

        LOGGER.info("getDataForDate - sensorId = {}, dateTime = {}", sensorId, dateTime);

        // If selecting entries for any time on date 2023-11-28, a "from" and "until" range is created
        // from = '2023-11-28T00:00:00.000Z' and until = '2023-11-29T00:00:00.000Z'
        String from = TimeUtil.getDateWithoutHours(dateTime);
        String until = TimeUtil.getNextDate(dateTime);

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
        RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        RequestParameter sensorId = params.pathParameter(SENSOR_ID);
        OffsetDateTime from = OffsetDateTime.parse(params.queryParameter(FROM).getString());
        OffsetDateTime until = OffsetDateTime.parse(params.queryParameter(UNTIL).getString());

        LOGGER.info("getDataForDateRange - sensorId = {}, from = {}, until = {}", sensorId, from, until);

        if (!from.isBefore(until)) {
            LOGGER.error("getDataForDateRange - logically invalid times: \'from\' does not specify a time before 'until'");
            // The failure handler will return a 400 response
            routingContext.fail(new IllegalArgumentException("Invalid request parameters: \'from\' must specify a time before 'until'"));
        } else {

            JsonArray queryParams = new JsonArray();
            queryParams.add(from);
            queryParams.add(until);
            this.databaseClient.selectData(queryParams).subscribe(resultSet -> {
                JsonObject jsonResponse = new JsonObject().put("data", resultSet.getRows());
                LOGGER.info("getDataForDateRange - result = {}", jsonResponse.encodePrettily());
                okResponse(routingContext, jsonResponse);
            }, e -> internalServerError(routingContext));
        }
    }

    @Override
    public void createSensor(RoutingContext routingContext) {
        RequestParameters  params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY);
        RequestParameter body = params.body();

        // ToDo - handle case where sensorID already exists - database will throw primary key exception
        // Should sensor-id have a max length?
        // Should location be a time zones?
        CreateSensorRequest createSensorRequest = body != null ? mapper.convertValue(body.get(), new TypeReference<CreateSensorRequest>(){}) : null;

        Timestamp timestamp = new java.sql.Timestamp(System.currentTimeMillis());
        String dateTimeString = new SimpleDateFormat("y-MM-dd HH:mm:ss.SSS").format(timestamp);
        LOGGER.info("createSensor - request = {}, creationTime = {}", createSensorRequest, dateTimeString);

        JsonArray queryParams = new JsonArray();
        queryParams.add(createSensorRequest.getSensorId());
        queryParams.add(createSensorRequest.getLocation());
        queryParams.add(dateTimeString);

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
            // Fail with status 404 if sensorId not found
            if (resultSet.getRows().isEmpty()) {
                String message = messageBuilder("getSensor - sensorID = ", sensorId.getString(), " not found");
                LOGGER.error(message);
                routingContext.fail(new NotFoundException(message));
            } else {
                JsonObject jsonResponse = new JsonObject().put("data", resultSet.getRows());
                okResponse(routingContext, jsonResponse);
            }
        }, e -> internalServerError(routingContext));
    }

    // Handle different failure scenarios
    // Should this go here?
    public void failureHandler(RoutingContext routingContext) {
        if (routingContext.failure() instanceof NotFoundException) {
            notFoundResponse(routingContext);
        } else {
            badRequestResponse(routingContext);
        }
    }

    // Todo - extract to centralized util class
    private String messageBuilder(String ... values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String value : values) {
            stringBuilder.append(value);
        }
        return  stringBuilder.toString();
    }
}
