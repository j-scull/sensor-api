package projects.sensor.api.config.database;

public class MySQLConfig implements DatabaseConfig {

    private String host;
    private int port;
    private String database;
    private int poolOptionsMaxSize;


    @Override
    public String getUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database;
    }

    // user and password will be set as environment variables

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return this.database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public int getPoolOptionsMaxSize() {
        return this.poolOptionsMaxSize;
    }

    public void setPoolOptionsMaxSize(int poolOptionsMaxSize) {
        this.poolOptionsMaxSize = poolOptionsMaxSize;
    }

}
