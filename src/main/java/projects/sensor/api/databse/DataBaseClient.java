package projects.sensor.api.databse;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

public interface DataBaseClient {

    /**
     *
     * @param queryParams
     * @return
     */
    Single<UpdateResult> insertData(JsonArray queryParams);

    /**
     *
     * @param queryParams
     * @return
     */
    Single<ResultSet> selectData(JsonArray queryParams);

    /**
     *
     * @param queryParams
     * @return
     */
    Single<UpdateResult> insertSensor(JsonArray queryParams);

    /**
     *
     * @return
     */
    Single<ResultSet> selectAllSensors();

    /**
     *
     * @param queryParams
     * @return
     */
    Single<ResultSet> selectSensor(JsonArray queryParams);
}
