import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class UiTestsSelenium {

    WebDriver driver;

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        driver.get("http://localhost:8080");
    }

    @DisplayName("Добавление товара на витрину")
    @Test
    void addNewProductViaAdmin() {
        Faker faker = new Faker();

        String product = faker.commerce().productName();
        String price = faker.commerce().price();

        driver.findElement(By.xpath("//a[@href='/admin']")).click();
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("secret123");
        driver.findElement(By.xpath("//button[text()='Sign in']")).click();
        driver.findElement(By.id("n-name")).sendKeys(product);
        driver.findElement(By.id("n-price")).sendKeys(price);
        driver.findElement(By.id("add-btn")).click();
        driver.findElement(By.xpath("//a[@href='/']")).click();

        List<WebElement> productElements = driver.findElements(By.cssSelector("#products-list .product-card h4"));
        String lastProductName = productElements.get(productElements.size() - 1).getText();

        assertThat(lastProductName)
                .as("На витрину добавлен товар " + product)
                .isEqualTo(product);
    }

    @DisplayName("Проверка, что товар добавляется в корзину")
    @Test
    void addToCart() {

        Faker faker = new Faker();

        String product = faker.commerce().productName();
        String price = faker.commerce().price();

        //Добавление товара на витрину
        driver.findElement(By.xpath("//a[@href='/admin']")).click();
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("secret123");
        driver.findElement(By.xpath("//button[text()='Sign in']")).click();
        driver.findElement(By.id("n-name")).sendKeys(product);
        driver.findElement(By.id("n-price")).sendKeys(price);
        driver.findElement(By.id("add-btn")).click();
        driver.findElement(By.xpath("//a[@href='/']")).click();

        List<WebElement> productElements = driver.findElements(By.cssSelector("#products-list .product-card h4"));
        String lastProductName = productElements.get(productElements.size() - 1).getText();

        //Добавление товара в корзину
        driver.findElement(By.xpath("//*[@data-action='add-to-cart'][@data-name='" + lastProductName + "']")).click();
        driver.findElement(By.id("open-cart-btn")).click();

        String contentCart = driver.findElement(By.xpath("//div[@class='cart-item']//b[text()='" + lastProductName + "']")).getText();

        assertThat(contentCart)
                .as("Товар " + product + " успешно добавляется в корзину")
                .isEqualTo(product);

    }

    @DisplayName("Вход в админку с неверным логином/паролем")
    @Test
    void loginAdminWithInvalidCredentialsTest() {

        driver.findElement(By.xpath("//a[@href='/admin']")).click();
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("secret1234");
        driver.findElement(By.xpath("//button[text()='Sign in']")).click();

        String expectedErrorMessage = driver.findElement(By.cssSelector(".alert.alert-danger")).getText();

        assertThat(expectedErrorMessage)
                .as("Появляется текст ошибки")
                .isEqualTo("Неверные учетные данные пользователя");
    }

    @DisplayName("Проверка сохранения товаров в корзине после обновления страницы")
    @Test
    void productsStorageOnPageReloadTest() {

        Faker faker = new Faker();

        String product = faker.commerce().productName();
        String price = faker.commerce().price();

        //Добавление товара на витрину
        driver.findElement(By.xpath("//a[@href='/admin']")).click();
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("secret123");
        driver.findElement(By.xpath("//button[text()='Sign in']")).click();
        driver.findElement(By.id("n-name")).sendKeys(product);
        driver.findElement(By.id("n-price")).sendKeys(price);
        driver.findElement(By.id("add-btn")).click();
        driver.findElement(By.xpath("//a[@href='/']")).click();

        List<WebElement> productElements = driver.findElements(By.cssSelector("#products-list .product-card h4"));
        String lastProductName = productElements.get(productElements.size() - 1).getText();

        //Добавление товара в корзину
        driver.findElement(By.xpath("//*[@data-action='add-to-cart'][@data-name='" + lastProductName + "']")).click();
        driver.findElement(By.id("open-cart-btn")).click();

        //Проверка добавления товара в корзину
        String countCart = driver.findElement(By.id("cart-count")).getText();

        assertThat(countCart)
                .as("В корзине отображается 1 товар")
                .isEqualTo("1");

        //Обновление страницы
        driver.navigate().refresh();

        String newCountCart = driver.findElement(By.id("cart-count")).getText();

        assertThat(newCountCart)
                .as("В корзине отображается 1 товар")
                .isEqualTo("1");

    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}
