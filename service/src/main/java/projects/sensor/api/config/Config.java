package projects.sensor.api.config;

public class Config {

    /**
     * The url for a database, used for testing purposes
     * The service will read from this if env variable DATABASE_URL is not set
     */
    private String databaseUrl;

    /**
     * The driver used for the database
     */
    private String databaseDriverClass;

    /**
     * Enable or disable the swagger-ui
     * Enabled by default
     */
    private boolean enableSwaggerUi = true;

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }
    public String getDatabaseDriverClass() {
        return databaseDriverClass;
    }

    public void setDatabaseDriverClass(String databaseDriverClass) {
        this.databaseDriverClass = databaseDriverClass;
    }

    public boolean isEnableSwaggerUi() {
        return enableSwaggerUi;
    }

    public void setEnableSwaggerUi(boolean enableSwaggerUi) {
        this.enableSwaggerUi = enableSwaggerUi;
    }
}
