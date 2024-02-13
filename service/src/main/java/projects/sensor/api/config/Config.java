package projects.sensor.api.config;

import projects.sensor.api.config.database.DatabaseConfig;

public class Config {

    /**
     * The Database configuration used by the service
     */
    private DatabaseConfig databaseConfig;

    /**
     * Enable or disable the swagger-ui
     * Enabled by default
     */
    private boolean enableSwaggerUi = true;

    public DatabaseConfig getDatabaseConfig() {
        return this.databaseConfig;
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public boolean isEnableSwaggerUi() {
        return enableSwaggerUi;
    }

    public void setEnableSwaggerUi(boolean enableSwaggerUi) {
        this.enableSwaggerUi = enableSwaggerUi;
    }

    @Override
    public String toString() {
        return "Config=[enableSwaggerUi=" + enableSwaggerUi + ", " + databaseConfig + "]";
    }

}
