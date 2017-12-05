package Framework;

import Framework.Utility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.*;

/**
 * Get Configuration class will contains all the methods that will read
 * the configuration from the runner file and run according to that
 */
public class GetConfiguration {

    public static void main(String[] args) {
        GetConfiguration get = new GetConfiguration();

        System.out.println(get.getBrowserName());
    }

    public String getConfigFilePath() {
        File file = new File("src/test/java/config/config.json");
        return file.getAbsolutePath();
    }

    public ArrayList<String> getBrowserName() {
        Utility parser = new Utility();
        return (JSONArray) ((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("browser");
    }


    public ArrayList<String> getTagName() {
        Utility parser = new Utility();
        JSONObject main= parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get("tag");
    }

    public ArrayList<String> getSuiteName() {
        Utility parser = new Utility();
        JSONObject main= parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get("suite");

    }

    public String getRunBy() {
        Utility parser = new Utility();
        String runby=((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("by").toString();
        if(runby.toLowerCase().contains("suite"))
        {
            return "suite";
        }
        else if(runby.toLowerCase().contains("tag"))
        {
            return "tag";
        }
        return null;
    }

    public ArrayList<String> getByValue() {
        Utility parser = new Utility();
        JSONObject main= parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get("suite");
    }

    public String getBaseUrl() {
        Utility parser = new Utility();
        return ((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("baseUrl").toString();
    }

    public Map<String, String> getParallel() {
        Utility parser = new Utility();
        JSONObject parallelData=(JSONObject) ((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("parallel");
        boolean IsTrue=(boolean) parallelData.get("status");
        Map<String,String> dataOfparallel=new HashMap<> ();
        if(IsTrue)
        {
            dataOfparallel.put("status",parallelData.get("status").toString());
            dataOfparallel.put("by",parallelData.get("by").toString());
            dataOfparallel.put("count",parallelData.get("count").toString());
            return dataOfparallel;

        }
        else
        {
            dataOfparallel.put("status",parallelData.get("status").toString());
            return dataOfparallel;

        }

    }

    public String getSuitesDirectory() {
        Utility parser = new Utility();
        File jsonFile = new File("config.json");
        return parser.loadJsonFile(jsonFile.getAbsolutePath()).get("SuiteDirectory").toString();
    }

}
