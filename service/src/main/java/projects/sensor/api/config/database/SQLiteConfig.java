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

    /**
     * The maximum database connection pool size
     */
    private int maxPoolSize = 30;

    /**
     * Get the database url
     * @return the full database url
     */
    @Override
    public String getUrl() {
        return url;
    }

    /**
     * Set the database url
     * @param url - the full database url
     * @return a handle to this for fluency
     */
    public SQLiteConfig setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get the database client driver class
     * @return the name of the database client driver class
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * Set the database client driver class
     * @param driverClass
     * @return a handle to this for fluency
     */
    public SQLiteConfig setDriverClass(String driverClass) {
        this.driverClass = driverClass;
        return this;
    }

    /**
     * Get the maxPoolSize
     * @return the maxPoolSize
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Set the maxPoolsize
     * @param maxPoolSize
     * @return a handle to this for fluency
     */
    public SQLiteConfig setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

}
