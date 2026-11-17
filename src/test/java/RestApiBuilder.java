import config.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;

public class RestApiBuilder {
    public static RequestSpecification getRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigReader.CONFIG.baseUrl())
                .setAuth(RestAssured.basic(
                        ConfigReader.CONFIG.userLogin(),
                        ConfigReader.CONFIG.userPassword()
                ))
                .log(LogDetail.ALL)
                .build();
    }
}
