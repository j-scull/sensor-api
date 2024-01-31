package projects.sensor.api.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestConfig {

    @Test
    public void testDefaultConfig() {
        Config config = ConfigReader.getConfig();
        assertEquals("myDatabaseUrl", config.getDatabaseUrl());
        assertEquals("myDatabaseDriverClass", config.getDatabaseDriverClass());
        assertFalse(config.isEnableSwaggerUi());
    }

    // Todo - add test cases for invalid config
}
