package framework;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Get Configuration class will contains all the methods that will read
 * the configuration from the runner file and run according to that
 */
public class GetConfiguration {

    public static void main(String[] args) {
        GetConfiguration get = new GetConfiguration();
        System.out.println(get.getBaseUrl());
    }

    public String getConfigFilePath() {
        File file = new File("config.json");
        return file.getAbsolutePath();
    }

    public ArrayList<String> getBrowsers() {
        Utility parser = new Utility();
        return (JSONArray) ((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("browser");
    }


    public ArrayList<String> getTags() {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get("tag");
    }

    public ArrayList<String> getSuiteName() {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get("suite");

    }

    public String getRunBy() {
        Utility parser = new Utility();
        String runby = ((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("by").toString();
        if (runby.toLowerCase().contains("suite")) {
            return "suite";
        } else if (runby.toLowerCase().contains("tag")) {
            return "tag";
        }
        return null;
    }

    public ArrayList<String> getByValue() {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get("suite");
    }

    public String getBaseUrl() {
        Utility parser = new Utility();

        String baseUrl = "";


        try {
            baseUrl = ((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("baseUrl").toString();
        } catch (Exception e) {
            System.out.println("Base URL is not defined");
        }


        return baseUrl;
    }

    public JSONObject getParallel() {
        Utility parser = new Utility();
        JSONObject parallelData = (JSONObject) ((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("parallel");
        JSONObject dataOfparallel = new JSONObject();

        try {

            boolean IsTrue = (boolean) parallelData.get("status");
            if (IsTrue) {
                dataOfparallel.put("status", parallelData.get("status").toString());
                dataOfparallel.put("count", parallelData.get("count").toString());
                return dataOfparallel;

            } else {
                dataOfparallel.put("status", parallelData.get("status").toString());
                return dataOfparallel;

            }


        } catch (Exception e) {
            dataOfparallel.put("status", "false");

        }

        return dataOfparallel;
    }

    public String getSuitesDirectory() {
        Utility parser = new Utility();
        File jsonFile = new File("config.json");
        return parser.loadJsonFile(jsonFile.getAbsolutePath()).get("SuiteDirectory").toString();
    }


    public String getLocatorDirectory() {
        Utility parser = new Utility();
        File jsonFile = new File("config.json");
        return parser.loadJsonFile(jsonFile.getAbsolutePath()).get("locatorDirectory").toString();
    }

    /**
     * @Description : get suite name from config file.
     * @return : suite names.
     */
    public ArrayList<String> getSuite() {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get("suite");
    }

}
