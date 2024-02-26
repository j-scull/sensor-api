package projects.sensor.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projects.sensor.api.Main;

import java.io.File;
import java.io.IOException;

public class ConfigReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);

    private final static String DEFAULT_CONFIG_FILE= "/usr/share/sensor-api/config/config.yaml";

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static Config getConfig() throws IOException {
        return getConfig(DEFAULT_CONFIG_FILE);
    }

    // Todo - handle invalid config
    public static Config getConfig(String configFile) throws IOException {
        LOGGER.info("ConfigReader - reading config from {}", configFile);
        Config config = mapper.readValue(new File(configFile), Config.class);
        LOGGER.info("ConfigReader - config = {}", config);
        return config;
    }

}
