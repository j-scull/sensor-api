package projects.sensor.api.config.database;

public class SQLiteConfig implements DatabaseConfig {

    /**
     * The url for a database, used for testing purposes
     */
    private String url;

    /**
     * The driver used for the database
     */
    private String driverClass;

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

}
