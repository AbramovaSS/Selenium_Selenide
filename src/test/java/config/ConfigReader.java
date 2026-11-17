package config;

import org.aeonbits.owner.ConfigFactory;

public class ConfigReader {
    public static final WebConfig CONFIG = ConfigFactory.create(WebConfig.class);
}
