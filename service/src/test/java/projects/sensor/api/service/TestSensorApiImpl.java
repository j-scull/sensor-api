package projects.sensor.api.service;

import io.reactivex.Single;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameter;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.impl.RequestParameterImpl;
import io.vertx.ext.web.validation.impl.RequestParametersImpl;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import projects.sensor.api.database.DatabaseClient;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class TestSensorApiImpl {


    @TestSubject
    private SensorApiImpl sensorApiImpl;
//    @TestSubject
    private DatabaseClient dataBaseClient;

    // Mocks
    private RoutingContext routingContext;
    private HttpServerResponse httpServerResponse;

    @Before
    public void setup() {

        // Set up mocks
        dataBaseClient = createMock(DatabaseClient.class);
        routingContext = createMock(RoutingContext.class);
        httpServerResponse = createMock(HttpServerResponse.class);

        sensorApiImpl = new SensorApiImpl(dataBaseClient);
    }

    @Test
    public void testLogData_Success() {

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("sensorId", "123");
        jsonBody.put("temperature", "9");
        jsonBody.put("humidity", "87");

        // Query has json body and path parameter
        RequestParameters requestParameters = createRequestBody(jsonBody);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful empty response
        expect(dataBaseClient.insertData(anyObject())).andReturn(Single.just(new UpdateResult(1, new JsonArray())));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(201)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.logData(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void testLogData_DatabaseClientError() {

        reset(routingContext, dataBaseClient, httpServerResponse);

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("sensorId", "123");
        jsonBody.put("temperature", "9");
        jsonBody.put("humidity", "87");

        // Query has json body and path parameter
        RequestParameters requestParameters = createRequestBody(jsonBody);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return an error
        expect(dataBaseClient.insertData(anyObject())).andReturn(Single.error(new Exception("Error!")));

        // Set up the Internal Server Error response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(500)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("Internal Server Error")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.logData(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
    }

    @Test
    public void getDataForDate_Success() {

        setup();

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("year", "2023");
        jsonBody.put("month", "12");
        jsonBody.put("date", "17");

        // Create path parameter
        Map<String, RequestParameter> pathParameter = createPathParameter("sensorId", "123");

        // Query has json body and path parameter
        RequestParameters requestParameters = createBodyAndPathParameters(jsonBody, pathParameter);


    }

    @Test
    public void getDataForDate_DatabaseClientError() {

        setup();

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("year", "2023");
        jsonBody.put("month", "12");
        jsonBody.put("date", "17");

        // Create path parameter
        Map<String, RequestParameter> pathParameter = createPathParameter("sensorId", "123");

        // Query has json body and path parameter
        RequestParameters requestParameters = createBodyAndPathParameters(jsonBody, pathParameter);


    }

    @Test
    public void getDataForDateRange_Success() {

    }

    @Test
    public void getDataForDateRange_DatabaseClientError() {

    }

    @Test
    public void createSensor_Success() {

    }

    @Test
    public void createSensor_DatabaseClientError() {

    }

    @Test
    public void listSensors_Success() {

    }

    @Test
    public void listSensors_DatabaseClientError() {

    }

    @Test
    public void getSensor_Success() {

    }

    @Test
    public void getSensor_DatabaseClientError() {

    }

    private RequestParameters createPathParameters(Map<String, RequestParameter> parameters) {
        RequestParametersImpl requestParameters = new RequestParametersImpl();
        requestParameters.setPathParameters(parameters);
        return requestParameters;
    }

    private RequestParameters createQueryParameters(Map<String, RequestParameter> parameters) {
        RequestParametersImpl requestParameters = new RequestParametersImpl();
        requestParameters.setQueryParameters(parameters);
        return requestParameters;
    }

    private RequestParameters createRequestBody(JsonObject jsonObject) {
        RequestParameterImpl requestParameter = new RequestParameterImpl(jsonObject);
        RequestParametersImpl requestParameters = new RequestParametersImpl();
        requestParameters.setBody(requestParameter);
        return requestParameters;
    }

    private RequestParameters createBodyAndPathParameters(JsonObject jsonObject, Map<String, RequestParameter> parameters) {
        RequestParametersImpl requestParameters = new RequestParametersImpl();
        RequestParameterImpl requestParameter = new RequestParameterImpl(jsonObject);
        requestParameters.setBody(requestParameter);
        requestParameters.setPathParameters(parameters);
        return requestParameters;
    }

    private Map<String, RequestParameter> createPathParameter(String parameterName, String value) {
        Map<String, RequestParameter> parameterMap = new HashMap<>();
        RequestParameterImpl requestParameter = new RequestParameterImpl(value);
        parameterMap.put(parameterName, requestParameter);
        return parameterMap;
    }
}
