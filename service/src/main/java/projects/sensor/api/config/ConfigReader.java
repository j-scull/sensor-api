package projects.sensor.api.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ConfigReader {

    private final static String DEFAULT_CONFIG_FILE= "config/config.yaml";

    public static Config getConfig() {
        return getConfig(DEFAULT_CONFIG_FILE);
    }

    // Todo - handle invalid config
    public static Config getConfig(String configFile) {
        InputStream inputStream = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream(configFile);
        Yaml yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
        return yaml.load(inputStream);
    }
}
