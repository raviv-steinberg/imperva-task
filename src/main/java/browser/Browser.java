package browser;

import com.aventstack.extentreports.Status;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ExtentReport;
import utils.OsCheck;
import utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Browser {
    private static WebDriver driver;

    public static WebDriver driver() {
        return driver;
    }

    private static WebDriverWait webDriverWait30Sec;

    public static WebDriverWait webDriverWait30Sec() {
        return webDriverWait30Sec;
    }

    private static WebDriverWait webDriverWait60Sec;

    public static WebDriverWait webDriverWait60Sec() {
        return webDriverWait60Sec;
    }

    private static WebDriverWait webDriverWait120Sec;

    public static WebDriverWait webDriverWait120Sec() {
        return webDriverWait120Sec;
    }

    private static Actions actions;

    public static Actions actions() {
        return actions;
    }

    private static JavascriptExecutor javascriptExecutor;

    public static JavascriptExecutor javascriptExecutor() {
        return javascriptExecutor;
    }

    private static TakesScreenshot takesScreenshot;

    public static TakesScreenshot takesScreenshot() {
        return takesScreenshot;
    }

    public static void open() throws Exception {
        // Define default browser.
        BrowserType browserType = BrowserType.Chrome;
        String browser = Utils.getProperty("browser");
        ExtentReport.extentTest.log(Status.INFO, String.format("Require browser: '%s'", browser));

        // Read browser type from configuration file.
        String propertName = "";
        if (browser.toLowerCase().equals("chrome")) {
            browserType = BrowserType.Chrome;
            propertName = "webdriver.chrome.driver";

        } else if (browser.toLowerCase().equals("firefox")) {
            browserType = BrowserType.Firefox;
            propertName = "webdriver.gecko.driver";
        }else {
            throw new Exception(String.format("Not supported browser type (current value is \'%s\')", browser));
        }

        switch (OsCheck.getOperatingSystemType()) {
            case Windows:
                System.setProperty(propertName, Utils.getCurrentRootLocation() + "\\src\\main\\java\\drivers\\windows\\" + browserType.toString() + ".exe");
                ExtentReport.extentTest.log(Status.INFO, "OS type: Windows");
                break;

            case Mac:
                System.setProperty(propertName, Utils.getCurrentRootLocation() + "/src/main/java/drivers/mac/" + browserType.toString());
                ExtentReport.extentTest.log(Status.INFO, "OS type: MAC");
                break;

            case Linux:
                System.setProperty(propertName, Utils.getCurrentRootLocation() + "/src/main/java/drivers/linux/" + browserType.toString());
                ExtentReport.extentTest.log(Status.INFO, "OS type: Linux");
                break;

            case Other:
                throw new Exception("Cannot read OS type");
        }

        if (browserType == BrowserType.Chrome){
            Boolean isheadless = Boolean.valueOf(Utils.getProperty("isheadless"));
            if(isheadless)
            {
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--headless");
                chromeOptions.addArguments("--window-size=1920,1080");
                driver = new ChromeDriver(chromeOptions);
            }
            else
                driver = new ChromeDriver();
        }
        else if (browserType == BrowserType.Firefox)
            driver = new FirefoxDriver();

        int timeOut = 30;
        driver.manage().timeouts().pageLoadTimeout(timeOut, TimeUnit.SECONDS);
        ExtentReport.extentTest.log(Status.INFO, String.format("Set page load timeout to %s seconds", timeOut));

        webDriverWait30Sec = new WebDriverWait(driver, 30);
        webDriverWait60Sec = new WebDriverWait(driver, 60);
        webDriverWait120Sec = new WebDriverWait(driver, 120);
        actions = new Actions(driver);
        javascriptExecutor = (JavascriptExecutor) driver;
        takesScreenshot = (TakesScreenshot) driver;

        ExtentReport.extentTest.log(Status.INFO, "Maximize window");
        driver.manage().window().maximize();

    }

    public static void refresh() {
        ExtentReport.extentTest.log(Status.INFO, "Refresh page...");
        driver.navigate().refresh();
    }

    public static void close() {
        if (driver != null) {
            ExtentReport.extentTest.log(Status.INFO, "Close browser");
            driver.close();
        }
    }

    public static void openLinkInNewTab(WebElement hrefLink) {
        actions.keyDown(Keys.SHIFT).click(hrefLink).keyUp(Keys.SHIFT).build().perform();

        // Handle windows change.
        ArrayList<String> tabs = new ArrayList<String>(Browser.driver().getWindowHandles());

        // Switch to the new tab.
        if (tabs.size() > 1)
            driver.switchTo().window(tabs.get(1));
    }

    public static void openLinkInNewTab(String hrefLink) {
        // Open new tab.
        javascriptExecutor.executeScript(String.format("window.open('%s');", hrefLink));

        // Handle windows change.
        ArrayList<String> tabs = new ArrayList<String>(Browser.driver().getWindowHandles());

        // Switch to the new tab.
        Browser.driver().switchTo().window(tabs.get(1));
    }

    public static void closeLastTab() {
        // Close current tab.
        Browser.driver().close();

        // Handle windows change.
        ArrayList<String> tabs = new ArrayList<String>(Browser.driver().getWindowHandles());

        // Switch back to main tab.
        Browser.driver().switchTo().window(tabs.get(0));
    }

    public static boolean isPageLoad(String url) {
        try {
            webDriverWait30Sec.until(ExpectedConditions.urlToBe(url));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String captureScreenshot(String testName) throws IOException {
        File screenshotFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
        String path = Utils.getScreenshotPath() + testName + ".png";
        FileUtils.copyFile(screenshotFile, new File(path));
        return path;
    }
}