package projects.sensor.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class ConfigReader {

    private final static String DEFAULT_CONFIG_FILE= "config/config.yaml";

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static Config getConfig() throws IOException {
        return getConfig(DEFAULT_CONFIG_FILE);
    }

    // Todo - handle invalid config
    public static Config getConfig(String configFile) throws IOException {
        return mapper.readValue(new File(configFile), Config.class);
    }

}
