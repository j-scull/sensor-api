package projects.sensor.api.config;

import org.junit.Test;
import projects.sensor.api.config.database.DatabaseConfig;
import projects.sensor.api.config.database.MySQLConfig;
import projects.sensor.api.config.database.SQLiteConfig;

import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestConfig {

    @Test
    public void testDefaultConfig() throws IOException{
        String path = getResourcePath("config/swaggerUiDisabled.yaml");
        Config config = ConfigReader.getConfig(path);
        assertFalse(config.isEnableSwaggerUi());
    }

    @Test
    public void testMySQLConfig() throws IOException {
        String path = getResourcePath("config/mySQLConfig.yaml");
        Config config = ConfigReader.getConfig(path);
        assertTrue(config.isEnableSwaggerUi());
        DatabaseConfig databaseConfig = config.getDatabaseConfig();
        assertEquals(databaseConfig.getUrl(), "jdbc:mysql://10.10.10.10:1234/database");
        assertTrue(databaseConfig instanceof MySQLConfig);
        assertEquals(((MySQLConfig)databaseConfig).getHost(), "10.10.10.10");
        assertEquals(((MySQLConfig)databaseConfig).getPort(), 1234);
        assertEquals(((MySQLConfig)databaseConfig).getDatabase(), "database");
        assertEquals(((MySQLConfig)databaseConfig).getPoolOptionsMaxSize(), 4);
    }

    @Test
    public void testSQLiteConfig() throws IOException {
        String path = getResourcePath("config/sqLiteConfig.yaml");
        Config config = ConfigReader.getConfig(path);
        assertTrue(config.isEnableSwaggerUi());
        DatabaseConfig databaseConfig = config.getDatabaseConfig();
        assertEquals(databaseConfig.getUrl(), "jdbc:sqlite:/sensor-api/service/target/db/test.db");
        assertTrue(databaseConfig instanceof SQLiteConfig);
        assertEquals(((SQLiteConfig)databaseConfig).getDriverClass(), "org.sqlite.JDBC");
    }

    // Todo - add test cases for invalid config

    private String getResourcePath(String fileName) {
        return Objects.requireNonNull(ConfigReader.class.getClassLoader().getResource(fileName)).getPath();
    }
}
