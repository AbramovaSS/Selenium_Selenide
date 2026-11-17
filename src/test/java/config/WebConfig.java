package config;

import org.aeonbits.owner.Config;

@Config.Sources({
        "classpath:config.properties"
})

public interface WebConfig extends Config {
    @Key("baseUrl")
    @DefaultValue("http://localhost:8080")
    String baseUrl();

    @Key("browserSize")
    @DefaultValue("1920x1080")
    String browserSize();

    @Key("browserName")
    @DefaultValue("chrome")
    String browserName();

    @Key("userLogin")
    @DefaultValue("admin")
    String userLogin();

    @Key("userPassword")
    @DefaultValue("secret123")
    String userPassword();

    @Key("timeout")
    @DefaultValue("4000") // Дефолт на случай, если в файле забудут прописать ключ
    long timeout();

    @Key("enableDetailedLogging")
    @DefaultValue("false")
    boolean enableDetailedLogging();

    @Key("startProductName")
    @DefaultValue("new product")
    String startProductName();

    @Key("startProductPrice")
    @DefaultValue("350.00")
    Double startProductPrice();

}
