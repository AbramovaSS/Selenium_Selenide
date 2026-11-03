import com.codeborne.selenide.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.interactions.Actions;

import javax.swing.*;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

public class DndTests {
    SelenideElement firstProductCard = $$(".product-card").first();
    SelenideElement cartButton = $("#open-cart-btn");
    SelenideElement removeButton = $("[data-action='remove']");
    SelenideElement cartItem = $(".cart-item");
    @BeforeEach
            void setup() {
        open("http://localhost:8080/");
    }

    @Test
    void shouldAddItemToCartUsingDragAndDrop() {
        firstProductCard.dragAndDrop(DragAndDropOptions.to(cartButton));

    }

    @Test
    void shouldAddItemToCartUsingDragAndDropActions() {
        actions().moveToElement(firstProductCard)
                .clickAndHold()
                .pause(1000)
                .moveToElement(cartButton)
                .release()
                .pause(Duration.ofSeconds(2))
                .perform();
    }

    @Test
    void shouldAddItemToCartUsingDragAndDropXy() {
        int firstProductCardX = firstProductCard.getCoordinates().inViewPort().getX() + firstProductCard.getSize().getWidth()/2;
        int firstProductCardY = firstProductCard.getCoordinates().inViewPort().getY() + firstProductCard.getSize().getHeight()/2;
        int cartButtonX = cartButton.getCoordinates().inViewPort().getX() + cartButton.getSize().getWidth()/2;
        int cartButtonY = cartButton.getCoordinates().inViewPort().getY() + cartButton.getSize().getHeight()/2;

        int diffX = cartButtonX - firstProductCardX;
        int diffY = cartButtonY - firstProductCardY;

        Actions dndActions = actions().moveToElement(firstProductCard).clickAndHold();
        for (int i = 0; i < 50; i++) {
            dndActions = dndActions.moveByOffset(diffX/50, diffY/50);
        }
        dndActions.release().perform();
    }

    @Test
    void shouldRemoveProductFromCart() {
        firstProductCard.dragAndDrop(DragAndDropOptions.to(cartButton));
        cartButton.click();
        removeButton.click();
        sleep(3000);
        cartItem.shouldNotBe(Condition.visible);
    }
}
