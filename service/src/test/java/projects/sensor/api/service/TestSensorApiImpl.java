package projects.sensor.api.service;

import io.reactivex.Single;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;

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
    public void getDataForDate_Success_ReturnsData() {

        setup();

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("dateTime", "2024-01-19T18:29:00.000Z");
        // Create path parameter
        Map<String, RequestParameter> pathParameter = createPathParameter("sensorId", "123");
        // Query has json body and path parameter
        RequestParameters requestParameters = createBodyAndPathParameters(jsonBody, pathParameter);

        // Todo - extract to helper method
        // Database query result
        JsonArray results = new JsonArray()
                .add("123")
                .add(12)
                .add(80)
                .add("2024-01-21T18:00:00.000Z");
        List<JsonArray> resultsList = new ArrayList<>();
        resultsList.add(results);
        ResultSet resultSet = new ResultSet()
                .setColumnNames(Arrays.asList("sensorId", "temperature", "humidity", "time"))
                .setResults(resultsList);

        // Todo - extract to helper method
        // Response data
        JsonObject responseData = new JsonObject()
                .put("sensorId", "123")
                .put("temperature", 12)
                .put("humidity", 80)
                .put("time", "2024-01-21T18:00:00.000Z");
        List<JsonObject> responseList = new ArrayList<>();
        responseList.add(responseData);
        JsonObject response = new JsonObject()
                .put("data", responseList);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful response with data
        expect(dataBaseClient.selectData(anyObject())).andReturn(Single.just(resultSet));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(200)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.end(response.toBuffer())).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getDataForDate(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);

    }

    @Test
    public void getDataForDate_Success_ReturnsEmpty() {

        setup();

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("dateTime", "2024-01-19T18:29:00.000Z");
        // Create path parameter
        Map<String, RequestParameter> pathParameter = createPathParameter("sensorId", "123");
        // Query has json body and path parameter
        RequestParameters requestParameters = createBodyAndPathParameters(jsonBody, pathParameter);

        // Database query result - empty
        ResultSet resultSet = new ResultSet()
                .setColumnNames(Arrays.asList("sensorId", "temperature", "humidity", "time"))
                .setResults(new ArrayList<>());
        JsonObject response = new JsonObject()
                .put("data", new ArrayList<>());

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful empty response
        expect(dataBaseClient.selectData(anyObject())).andReturn(Single.just(resultSet));
        // Set up the Created response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(200)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("OK")).andReturn(httpServerResponse);
        expect(httpServerResponse.end(response.toBuffer())).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getDataForDate(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);

    }


    @Test
    public void getDataForDate_DatabaseClientError() {
        setup();

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.put("dateTime", "2024-01-19T18:29:00.000Z");
        // Create path parameter
        Map<String, RequestParameter> pathParameter = createPathParameter("sensorId", "123");
        // Query has json body and path parameter
        RequestParameters requestParameters = createBodyAndPathParameters(jsonBody, pathParameter);

        // Set up routing context
        expect(routingContext.get(anyString())).andReturn(requestParameters);
        // Set up database client to return successful empty response
        expect(dataBaseClient.selectData(anyObject())).andReturn(Single.error(new Exception("Error!")));

        // Set up the Internal Server Error response
        expect(routingContext.response()).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusCode(500)).andReturn(httpServerResponse);
        expect(httpServerResponse.setStatusMessage("Internal Server Error")).andReturn(httpServerResponse);
        expect(httpServerResponse.end()).andReturn(null);

        replay(routingContext, dataBaseClient, httpServerResponse);

        sensorApiImpl.getDataForDate(routingContext);

        verify(routingContext, dataBaseClient, httpServerResponse);
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

    private RequestParameters createBodyAndPathParameters(JsonObject jsonObject, Map<String, RequestParameter> pathParameters) {
        RequestParametersImpl requestParameters = new RequestParametersImpl();
        RequestParameterImpl requestParameter = new RequestParameterImpl(jsonObject);
        requestParameters.setBody(requestParameter);
        requestParameters.setPathParameters(pathParameters);
        return requestParameters;
    }

    private Map<String, RequestParameter> createPathParameter(String parameterName, String value) {
        Map<String, RequestParameter> parameterMap = new HashMap<>();
        RequestParameterImpl requestParameter = new RequestParameterImpl(value);
        parameterMap.put(parameterName, requestParameter);
        return parameterMap;
    }
}
