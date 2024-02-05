package projects.sensor.api.config.database;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        defaultImpl = MySQLConfig.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MySQLConfig.class, name = "mysql"),
        @JsonSubTypes.Type(value = SQLiteConfig.class, name = "sqlite")
})
public interface DatabaseConfig {

    /**
     *
     * @return - the url for the database connection
     */
    String getUrl();

}
