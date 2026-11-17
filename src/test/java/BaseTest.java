
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SimpleReport;
import config.ConfigReader;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.given;

public class BaseTest {

    protected RequestSpecification requestSpec;
    protected Integer idForDelete;

    private final SimpleReport report = new SimpleReport();

    @BeforeAll
    static void configureSelenide() {
        Configuration.baseUrl = ConfigReader.CONFIG.baseUrl();
        Configuration.browser = ConfigReader.CONFIG.browserName();
        Configuration.browserSize = ConfigReader.CONFIG.browserSize();
        ConfigReader.CONFIG.userLogin();
        ConfigReader.CONFIG.userPassword();
        Configuration.timeout = ConfigReader.CONFIG.timeout();
        ConfigReader.CONFIG.startProductName();
        ConfigReader.CONFIG.startProductPrice();

    }

    @BeforeEach
    void setup() {

        if (ConfigReader.CONFIG.enableDetailedLogging()) {
            report.start();
        }

        this.requestSpec = RestApiBuilder.getRequestSpec();

        Selenide.open("/");
    }


    @AfterEach
    void tearDown() {
        try {
            if (ConfigReader.CONFIG.enableDetailedLogging()) {
                report.finish("Итоговый отчет по шагам UI");
            }

            if (idForDelete != null) {
                given()
                        .spec(requestSpec)
                        .pathParam("id", idForDelete)
                        .delete("/goods/{id}")
                        .then()
                        .log().all()
                        .statusCode(200);
            }
        } finally {
            idForDelete = null;
            Selenide.closeWebDriver();
        }
    }
}
