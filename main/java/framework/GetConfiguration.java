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

    public String getConfigFilePath() {
        File file = new File("config.json");
        return file.getAbsolutePath();
    }

    public ArrayList<String> getBrowsers() throws Exception {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject browser = (JSONObject) run.get("browser");
        return (JSONArray) browser.get("name");
    }

    public JSONObject getCapabilities()  {
        Utility parser = new Utility();
        JSONObject capabilities =null;
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject browser = (JSONObject) run.get("browser");
        capabilities = (JSONObject) browser.get("capabilities");

        if(capabilities==null||capabilities.equals(""))
            return  null;
        else
            return (JSONObject) browser.get("capabilities");

    }
    public ArrayList<String> getTags() {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get("tag");
    }
    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public JSONObject getBy()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return by;
    }
    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public String getSeleniumAddress()  {
        Utility parser = new Utility();
        String seleniumAddress =null;
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        try {
             seleniumAddress = run.get("seleniumAddress").toString();

        }catch (Exception e){}
        if(seleniumAddress==null||seleniumAddress.equals(""))
            return  null;
        else
            return seleniumAddress;
    }


    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public String getExtCodeDirectory()  {
        Utility parser = new Utility();
        File jsonFile = new File("config.json");
        String[] directoryArray= parser.loadJsonFile(jsonFile.getAbsolutePath()).get("ExtCodeDirectory").toString().split("/");
        return directoryArray[directoryArray.length-1];
    }

    public ArrayList<String> getSuiteName() {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get("suite");

    }
    public String getRunBy()  {
        Utility parser = new Utility();
        String runby = ((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("by").toString();
        if (runby.toLowerCase().contains("suite")) {
            return "suite";
        } else if (runby.toLowerCase().contains("tag")) {
            return "tag";
        }
        return null;
    }
    public ArrayList<String> getByValue()  {
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
        } catch (Exception e) { }


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

    public String getSuitesDirectory()  {
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
    public ArrayList<String> getSuite()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get("suite");
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public String getRetryAnalyser()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject retryAnalyser = (JSONObject) run.get("retryAnalyser");
        return (String) retryAnalyser.get("count");
    }

}
