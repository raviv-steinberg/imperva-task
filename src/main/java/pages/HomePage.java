package pages;

import browser.Browser;
import com.aventstack.extentreports.Status;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ExtentReport;
import utils.Utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HomePage {
    private String url = "https://github.com/";

    public String Url() {
        return url;
    }

    private WebElement searchTextBox() {
        return Browser.webDriverWait60Sec().until(ExpectedConditions.visibilityOfElementLocated(By.name("q")));
    }

    private List<WebElement> searchResults() {
        return Browser.webDriverWait60Sec().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("ul.repo-list li")));
    }

    private String getTags(WebElement webElement) {
        ArrayList<String> tags = new ArrayList<String>();
        try {
            List<WebElement> list = webElement.findElements(By.cssSelector("ul.repo-list li div.mt-n1 a.topic-tag.topic-tag-link.f6.px-2.mx-0"));
            for (int i = 0; i < list.size(); i++)
                tags.add(list.get(i).getText());
        } catch (Exception ex) {
            return null;
        }

        return parseTags(tags);
    }

    private String parseTags(ArrayList<String> arr) {

        // In case no tags found.
        if (arr.size() == 0)
            return "N/A";

        return String.join(",", arr);
    }

    private String getTitle(WebElement webElement) {
        WebElement element = webElement.findElement(By.cssSelector("a.v-align-middle"));
        return element.getText();
    }

    private String getDescription(WebElement webElement) {
        try {
            WebElement element = webElement.findElement(By.cssSelector("p.mb-1"));
            return element.getText();
        } catch (Exception ex) {
            return "N/A";
        }
    }

    private String getTime(WebElement webElement) {
        try {
            WebElement element = webElement.findElement(By.cssSelector("div.d-flex.flex-wrap relative-time"));
            return element.getText();
        } catch (Exception ex) {
            return "N/A";
        }
    }

    private WebElement getHrefLink(WebElement webElement) {
        return webElement.findElement(By.cssSelector("a.v-align-middle"));
    }

    private String getHrefURL(WebElement webElement) {
        return webElement.getAttribute("href");
    }

    private String getLanguage(WebElement webElement) {
        try {
            WebElement element = webElement.findElement(By.cssSelector("span[itemprop=programmingLanguage]"));
            return element.getText();
        } catch (Exception ex) {
            return "N/A";
        }
    }

    private WebElement nextPageLink() {
        return Browser.webDriverWait60Sec().until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.next_page")));
    }

    private boolean isNextPageExist() {
        try {
            return nextPageLink() != null;
        } catch (TimeoutException ex) {
            return false;
        }
    }

    private String getStars(WebElement webElement) {

        try {
            return webElement.findElement(By.cssSelector("a.muted-link")).getText();
        } catch (Exception ex) {
            return "N/A";
        }
    }

    public void goTo() {
        Browser.driver().navigate().to(url);
        ExtentReport.extentTest.log(Status.INFO, String.format("Navigate to '%s'", url));
    }

    public void search() throws Exception {
        int numberOfResults = Integer.parseInt(Utils.getProperty("numberofresults"));
        if (numberOfResults <= 0)
            throw new Exception(String.format("Number of results value need to be positive value and bigger then 0 (current value is %s)", numberOfResults));


        long start = System.currentTimeMillis();
        String query = Utils.getProperty("query");

        searchTextBox().clear();
        searchTextBox().sendKeys(query);
        searchTextBox().sendKeys(Keys.ENTER);
        ExtentReport.extentTest.log(Status.INFO, String.format("Search for '%s'", query));
        ExtentReport.extentTest.log(Status.INFO, String.format("Take the first %s results...", numberOfResults));

        List<WebElement> results = searchResults();
        ExtentReport.extentTest.log(Status.INFO, String.format("Search return '%s' results at this page", results.size()));

        long finish = System.currentTimeMillis();
        long output = finish - start;

        ExtentReport.extentTest.log(Status.INFO, String.format("Search query time takes %s milliseconds", output));

        int handles = 0;
        int page = 1;

        // Create JSON array list.
        JSONArray resultsList = new JSONArray();

        while (isNextPageExist()) {
            ExtentReport.extentTest.log(Status.INFO, String.format("Page number %s, Start to parse results (%s left)", page, numberOfResults - handles));
            for (int i = 0; i < results.size(); i++) {
                String title = getTitle(results.get(i));
                String description = getDescription(results.get(i));
                String language = getLanguage(results.get(i));
                String stars = getStars(results.get(i));
                String tags = getTags(results.get(i));
                String time = getTime(results.get(i));
                WebElement hrefLink = getHrefLink(results.get(i));
                String url = getHrefURL(hrefLink);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", title);
                jsonObject.put("description", description);
                jsonObject.put("language", language);
                jsonObject.put("stars", stars);
                jsonObject.put("tags", tags);
                jsonObject.put("time", time);
                jsonObject.put("url", url);

                resultsList.add(jsonObject);

                ExtentReport.extentTest.log(
                        Status.INFO,
                        String.format(
                                "<b>Create new JSON result object:</b><br/>%s",
                                "{<br/>" +
                                        "&nbsp&nbsp&nbsp&nbsp&nbsp&nbspTitle = title,<br/>".replace("title", title) +
                                        "&nbsp&nbsp&nbsp&nbsp&nbsp&nbspDescription = description,<br/>".replace("description", description) +
                                        "&nbsp&nbsp&nbsp&nbsp&nbsp&nbspLanguage = language,<br/>".replace("language", language) +
                                        "&nbsp&nbsp&nbsp&nbsp&nbsp&nbspStars = stars,<br/>".replace("stars", stars) +
                                        "&nbsp&nbsp&nbsp&nbsp&nbsp&nbspTags = tags,<br/>".replace("tags", tags) +
                                        "&nbsp&nbsp&nbsp&nbsp&nbsp&nbspTime = time,<br/>".replace("time", time) +
                                        "}"));

                // Increase current search counter.
                handles++;

                // In case we reach number of results we want.
                if (handles == numberOfResults) {
                    writeJson(resultsList);
                    long testFinish = System.currentTimeMillis();
                    ExtentReport.extentTest.log(Status.INFO, String.format("Total test running time is %s milliseconds", testFinish - start));
                    return;
                }
            }

            // Get next page.
            ExtentReport.extentTest.log(Status.INFO, String.format("Go to the next page"));
            nextPageLink().click();
            page++;

            // Delay between pages.
            ExtentReport.extentTest.log(Status.INFO, String.format("Sleep for %s milliseconds", Utils.getProperty("delaybetweenpages")));
            Thread.sleep(Integer.parseInt(Utils.getProperty("delaybetweenpages")));
            results = searchResults();
        }
    }

    private String pageLoadElapsedTime(WebElement hrefLink, String href) {
        // start measure time until page load.
        long start = System.currentTimeMillis();

        // Open link in a new tab.
        Browser.openLinkInNewTab(hrefLink);

        // Wait until page load.
        if (Browser.isPageLoad(href)) {
            // Page successfully load, stop measure time.
            return Long.toString(System.currentTimeMillis() - start) + " milliseconds";
        } else {
            //Fail.
            return "BROKEN LINK";
        }
    }

    private void writeJson(JSONArray resultsList) throws IOException {

        //Write JSON file
        String path = Utils.getCurrentRootLocation() + Utils.getOutputPath() + Utils.getProperty("jsonfile").replace("TimeStamp", Utils.getCurrentTimeStamp());
        ExtentReport.extentTest.log(Status.INFO, String.format("Create new result file: \'%s\'", path));
        try (FileWriter file = new FileWriter(path, true)) {
            for (int i = 0; i < resultsList.size(); i++)
                file.append(resultsList.get(i).toString() + "\n");
            file.flush();
            ExtentReport.extentTest.log(Status.INFO, String.format("Successfully append %s lines", resultsList.size()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void orderJSONFiles() {
        try {
            Thread.sleep(2000);
            File directory = new File(Utils.getCurrentRootLocation() + "\\output\\");
            File[] files = directory.listFiles();

            // Create JSON array list.
            JSONArray resultsList = new JSONArray();

            for (int i = 0; i < files.length; i++) {
                BufferedReader br = new BufferedReader(new FileReader(files[i]));
                String line;

                while ((line = br.readLine()) != null) {
                    Object obj = new JSONParser().parse(line);

                    // Typecasting obj to JSONObject.
                    JSONObject jsonObject = (JSONObject) obj;

                    // Pares require fields.
                    String title = (String) jsonObject.get("title");
                    String tags = (String) jsonObject.get("tags");
                    String language = (String) jsonObject.get("language");

                    // Create JSON object.
                    jsonObject = new JSONObject();
                    jsonObject.put("title", title);
                    jsonObject.put("tags", tags);
                    jsonObject.put("language", language);
                    resultsList.add(jsonObject);
                }

                //Write JSON file
                String path = Utils.getCurrentRootLocation() + Utils.getOrderPath() + Utils.getProperty("orderjsonfile").replace("TimeStamp", Utils.getCurrentTimeStamp());

                FileWriter file = new FileWriter(path, true);
                ExtentReport.extentTest.log(Status.INFO, String.format("Create new JSON file: %s", path));
                for (int j = 0; j < resultsList.size(); j++)
                    file.append(resultsList.get(j).toString() + "\n");
                ExtentReport.extentTest.log(Status.INFO, String.format("Successfully append %s lines", resultsList.size()));
                resultsList.clear();
                Thread.sleep(1000);
                file.flush();
                file.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void clearJsonFile() throws IOException {
        ExtentReport.extentTest.log(Status.INFO, "Clear JSON file...");
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(Utils.getProperty("jsonfile")));
        writer.write("");
        writer.flush();
        writer.close();
        ExtentReport.extentTest.log(Status.INFO, String.format("File \'%s\' successfully cleared", Paths.get(Utils.getProperty("jsonfile"))));
    }
}
