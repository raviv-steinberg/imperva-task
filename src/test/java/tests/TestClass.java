package tests;

import browser.Browser;
import com.aventstack.extentreports.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pages.Pages;
import utils.ExtentReport;
import utils.OsCheck;
import utils.Utils;

import java.io.IOException;

public class TestClass {

    @Before
    public void setup() throws Exception {
        ExtentReport.init();
        Browser.open();
    }

    @Test
    public void search() throws Exception {
        ExtentReport.extentTest = ExtentReport.extentReports.createTest("Search",
                "This test Search inside GitHub web page for specific word and parse data from first N results");
        Pages.HomePage().goTo();
        Pages.HomePage().search();
        Pages.HomePage().orderJSONFiles();
    }

    @After
    public void cleanup() throws Exception {
        ExtentReport.extentTest = ExtentReport.extentReports.createTest("Cleanup", "Clean system");
        Browser.close();
        ExtentReport.extentReports.flush();
    }
}
