import com.codeborne.selenide.*;
import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;

public class DndTests extends BaseTest {
    Faker faker = new Faker();
    String product = faker.commerce().productName();
    Double price = faker.number().randomDouble(2, 1, 100);

    ProductRequest productRequest = new ProductRequest(product, price);
    private Integer actualProductId;

    private SelenideElement getProductCard() {
        return $("#card-" + actualProductId);
    }

    SelenideElement cartButton = $("#open-cart-btn");
    SelenideElement removeButton = $("[data-action='remove']");
    SelenideElement cartItem = $(".cart-item");

    @DisplayName("Перетаскивание товара в корзину с помощью Drag and Drop")
    @Test
    void shouldAddItemToCartUsingDragAndDrop() {
        actualProductId = given()
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

        getProductCard().dragAndDrop(DragAndDropOptions.to(cartButton));

    }

    @DisplayName("Перетаскивание товара в корзину с помощью Drag and Drop (через Actions)")
    @Test
    void shouldAddItemToCartUsingDragAndDropActions() {
        actualProductId = given()
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
        actions().moveToElement(getProductCard())
                .clickAndHold()
                .pause(1000)
                .moveToElement(cartButton)
                .release()
                .pause(Duration.ofSeconds(2))
                .perform();
    }

    @DisplayName("Перетаскивание товара в корзину с помощью Drag and Drop (через Coordinates)")
    @Test
    void shouldAddItemToCartUsingDragAndDropXy() {
        actualProductId = given()
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

        int firstProductCardX = getProductCard().getCoordinates().inViewPort().getX() + getProductCard()
                .getSize().getWidth() / 2;
        int firstProductCardY = getProductCard().getCoordinates().inViewPort().getY() + getProductCard()
                .getSize().getHeight() / 2;
        int cartButtonX = cartButton.getCoordinates().inViewPort().getX() + cartButton.getSize().getWidth() / 2;
        int cartButtonY = cartButton.getCoordinates().inViewPort().getY() + cartButton.getSize().getHeight() / 2;

        int diffX = cartButtonX - firstProductCardX;
        int diffY = cartButtonY - firstProductCardY;

        Actions dndActions = actions().moveToElement(getProductCard()).clickAndHold();
        for (int i = 0; i < 50; i++) {
            dndActions = dndActions.moveByOffset(diffX / 50, diffY / 50);
        }
        dndActions.release().perform();
    }


    @DisplayName("Проверка, что удаленный товар не отображается в корзине")
    @Test
    void shouldRemoveProductFromCart() {
        actualProductId = given()
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

        getProductCard().dragAndDrop(DragAndDropOptions.to(cartButton));
        cartButton.click();
        removeButton.click();
        sleep(3000);
        cartItem.shouldNotBe(Condition.visible);
    }
}
