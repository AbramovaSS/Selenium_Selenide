import com.codeborne.selenide.*;
import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;


public class UiTestsSelenide {

    RequestSpecification requestSpec = new RestApiBuilder().requestSpecification;

    Faker faker = new Faker();

    String product = faker.commerce().productName();
    Double price = faker.number().randomDouble(2, 1, 100);
    String login = "admin";
    String password = "secret123";
    String psw = faker.numerify("######");
    private Integer idForDelete;
    double expectedTotalPrice = 176.72;

    ProductRequest productRequest = new ProductRequest(product, price);

    @BeforeEach
    void setup() {
        Selenide.open("http://localhost:8080/");
    }

    @DisplayName("Проверка, что товар добавляется в корзину")
    @Test
    void addToCartWithApi() {
        Integer actualProductId = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(productRequest)
                .post("/goods/add")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("data.id");

        idForDelete = actualProductId;

        refresh();

        $$("[data-action='add-to-cart']").last().click();
        $("#open-cart-btn").click();

        $("#cart-item-" + actualProductId).shouldBe(Condition.visible);

    }

    @DisplayName("Добавление товара на витрину")
    @Test
    void addNewProduct() {
        $("[href='/admin']").click();
        $("#username").shouldBe(visible).setValue(login);
        $("#password").setValue(password).pressEnter();
        $("#n-name").shouldBe(visible).setValue(product);
        $("#n-price").setValue(String.valueOf(price));
        $("#add-btn").click();

        $("[href='/']").click();

        $$("#products-list .product-card").last().shouldHave(text(product));

    }

    @DisplayName("Вход в админку с неверным логином/паролем")
    @Test
    void loginAdminWithInvalidCredentials() {

        $("[href='/admin']").click();
        $("#username").shouldBe(visible).setValue(login);
        $("#password").setValue(psw).pressEnter();

        $(".alert.alert-danger")
                .shouldHave(text("Неверные учетные данные пользователя"));
    }

    @DisplayName("Проверка сохранения товаров в корзине после обновления страницы")
    @Test
    void productsStorageOnPageReload() {
        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(productRequest)
                .post("/goods/add")
                .then()
                .log().all()
                .statusCode(200);

        $$("[data-action='add-to-cart']").last().click();
        $("#cart-count").shouldHave(text("1"));

        refresh();

        $("#cart-count").shouldHave(text("1"));
    }

    @DisplayName("Проверка отображения JS Alert")
    @Test
    void jsAlertVisibilityTest() {

        $$("[data-action='add-to-cart']").last().click();
        $("#open-cart-btn").click();
        $("#makeOrder").click();
        confirm();

    }

    @DisplayName("Покупка 3х единиц товара")
    @Test
    void shouldPurchaseThreeUnitsOfProduct() {
        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(productRequest)
                .post("/goods/add")
                .then()
                .log().all()
                .statusCode(200);

        ElementsCollection qtyChange = $$("[data-step='1']");
        qtyChange.last().click();
        qtyChange.last().click();
        $$("[data-action='add-to-cart']").last().click();
        $("#open-cart-btn").click();
        $("#makeOrder").click();

        $(".toast").shouldHave(text("Заказ принят в обработку!"));

    }

    @DisplayName("Проверка расчета общей цены")
    @Test
    void totalPriceCalculationTest() {

        ElementsCollection buttons = $$("[data-action='add-to-cart']");
        buttons.get(0).click();
        buttons.get(1).click();
        buttons.get(2).click();

        $("#open-cart-btn").click();
        $("#total-price").shouldHave(text(String.valueOf(expectedTotalPrice)));
    }

    @DisplayName("Проверка уведомления после добавления товара")
    @Test
    void productAddedNotificationTest() {

        $("[href='/admin']").click();
        $("#username").shouldBe(visible).setValue(login);
        $("#password").setValue(password).pressEnter();
        $("#n-name").shouldBe(visible).setValue(product);
        $("#n-price").setValue(String.valueOf(price));
        $("#add-btn").click();

        $(".toast").shouldHave(text("Товар успешно добавлен!"));
    }

    @DisplayName("Редактирование товара")
    @Test
    void shouldSuccessfullyEditProduct() {

        $("[href='/admin']").click();
        $("#username").shouldBe(visible).setValue(login);
        $("#password").setValue(password).pressEnter();

        ElementsCollection inputs = $$("[style='width:150px']");
        inputs.get(0).setValue(product);

        ElementsCollection buttons = $$("[data-action='update']");
        buttons.get(0).click();

        $("[href='/']").click();

        $$(".product-card").first().shouldHave(text(product));

    }

    @AfterEach
    void tearDown() {
        if (idForDelete != null) {
            given()
                    .spec(requestSpec)
                    .pathParam("id", idForDelete)
                    .delete("/goods/{id}")
                    .then()
                    .log().all()
                    .statusCode(200);

            idForDelete = null;
        }
    }
}
