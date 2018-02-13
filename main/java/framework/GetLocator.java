package framework;

import org.json.simple.JSONObject;

public class GetLocator {

    public static void main(String[] args) {
        GetLocator lc = new GetLocator();
        //lc.getLocatorValue("login.suite", "gmailLink");
    }

    public String getLocatorValue(String suiteName, String LocatorName) throws Exception {
        Utility parser = new Utility();
        GetConfiguration config = new GetConfiguration();
        JSONObject main = parser.loadJsonFile(config.getLocatorDirectory() + "/" + suiteName.split(".suite")[0] + ".json");

        return main.get(LocatorName).toString();
    }


}
