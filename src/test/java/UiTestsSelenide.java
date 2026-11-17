import com.codeborne.selenide.*;
import com.github.javafaker.Faker;
import config.ConfigReader;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.SetValueOptions.withText;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class UiTestsSelenide extends BaseTest {

    Faker faker = new Faker();

    String product = faker.commerce().productName();
    Double price = faker.number().randomDouble(2, 1, 100);
    String login = ConfigReader.CONFIG.userLogin();
    String password = ConfigReader.CONFIG.userPassword();
    String psw = faker.numerify("######");
    String product1 = "Сумка";
    String product2 = "Мармелад";
    String product3 = "Шоколад";
    Double price1 = 50.50;
    Double price2 = 60.10;
    Double price3 = 70.10;

    public static final String START_PRODUCT_NAME = ConfigReader.CONFIG.startProductName();
    public static final Double START_PRODUCT_PRICE = ConfigReader.CONFIG.startProductPrice();


    ProductRequest productRequest = new ProductRequest(product, price);

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

        refresh();

        $("[data-action='add-to-cart'][data-id='" + actualProductId + "']")
                .shouldBe(Condition.visible)
                .click();

        $("#open-cart-btn").click();
        $("#cart-item-" + actualProductId).shouldBe(Condition.visible);

    }

    @DisplayName("Добавление товара на витрину")
    @Test
    void addNewProduct() {
        $("[href='/admin']").click();
        $("#username").shouldBe(visible).setValue(login);
        $("#password").setValue(password).pressEnter();
        $("#n-name").shouldBe(visible).setValue(START_PRODUCT_NAME);
        $("#n-price").setValue(String.valueOf(START_PRODUCT_PRICE));
        $("#add-btn").click();

        $("[href='/']").click();

        $$("#products-list .product-card").last().shouldHave(text(START_PRODUCT_NAME));

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

        $("[data-action='add-to-cart'][data-id='" + actualProductId + "']")
                .shouldBe(Condition.visible)
                .click();

        $("#cart-count").shouldHave(text("1"));

        refresh();

        $("#cart-count").shouldHave(text("1"));
    }

    @DisplayName("Проверка отображения JS Alert при добавлении товара на сумму свыше 300 руб.")
    @Test
    void jsAlertVisibilityTest() {
        ProductRequest productRequest = new ProductRequest(START_PRODUCT_NAME, START_PRODUCT_PRICE);

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

        $("[data-action='add-to-cart'][data-id='" + actualProductId + "']")
                .shouldBe(Condition.visible)
                .click();

        $("#open-cart-btn").click();
        $("#makeOrder").click();

        String alertText = confirm();

        String expectedAlert = "[SmartShop]: Денег не хватает! Сумма %d ₽ превышает лимит 300 ₽."
                .formatted(START_PRODUCT_PRICE.intValue());

        assertEquals(expectedAlert, alertText);

    }

    @DisplayName("Покупка 3х единиц товара")
    @Test
    void shouldPurchaseThreeUnitsOfProduct() {
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

        Selenide.refresh();

        SelenideElement productCard = $("#card-" + actualProductId);

        productCard.$("[data-action='add-to-cart']").click();
        productCard.$("[data-action='add-to-cart']").click();
        productCard.$("[data-action='add-to-cart']").click();
        sleep(2000);
        $("#open-cart-btn").click();
        $("#makeOrder").click();

        $(".toast").shouldHave(text("Заказ принят в обработку!"));

    }

    @DisplayName("Проверка расчета общей цены")
    @Test
    void totalPriceCalculationTest() {
        ProductRequest productRequest1 = new ProductRequest(product1, price1);
        ProductRequest productRequest2 = new ProductRequest(product2, price2);
        ProductRequest productRequest3 = new ProductRequest(product3, price3);

        Integer actualProductId1 = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(productRequest1)
                .post("/goods/add")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("data.id");

        Integer actualProductId2 = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(productRequest2)
                .post("/goods/add")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("data.id");

        Integer actualProductId3 = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(productRequest3)
                .post("/goods/add")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .path("data.id");

        Selenide.refresh();

        SelenideElement productCard1 = $("#card-" + actualProductId1);
        productCard1.$("[data-action='add-to-cart']").click();

        SelenideElement productCard2 = $("#card-" + actualProductId2);
        productCard2.$("[data-action='add-to-cart']").click();

        SelenideElement productCard3 = $("#card-" + actualProductId3);
        productCard3.$("[data-action='add-to-cart']").click();

        sleep(2000);

        $("#open-cart-btn").click();

        Double expectedTotalPrice = price1 + price2 + price3;

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

        $("[href='/admin']").click();
        $("#username").shouldBe(visible).setValue(login);
        $("#password").setValue(withText(password).sensitive()).pressEnter();


        $("[id='nm-" + actualProductId + "']").setValue(product);

        $("[data-action='update'][data-id='" + actualProductId + "']").shouldBe(visible)
                .click();

        $("[href='/']").click();

        $("[id='card-" + actualProductId + "']").shouldBe(visible)
                .shouldHave(text(product));

    }
}
