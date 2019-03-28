package framework;

import Execution.SetCommandLineArgument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Exception.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.util.ArrayList;

/**
 * Get Configuration class will contains all the methods that will read
 * the configuration from the runner file and run according to that
 */
public class GetConfiguration {

    SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();

    public String getConfigFilePath() {
        String configName;
        if(setCommandLineArgument.configFile !=null){
            configName=setCommandLineArgument.configFile;
        }
        else {
            configName="config.json";
        }
        File file = new File(configName);
        return file.getAbsolutePath();
    }

    public ArrayList<String> getBrowsers() {
        if(setCommandLineArgument.browser !=null){
            String browserList=setCommandLineArgument.browser;
            String browsers[]=browserList.split(",");
            String browser = null;
            for(int i=0;i<browsers.length;i++){
                if(i==0){
                    browser="\""+browsers[i]+"\"";
                }
                else {
                    browser=browser+",\""+browsers[i]+"\"";
                }

            }
            String browserArray="["+browser+"]";
            JSONArray browserJsonArray = null;
            JSONParser parser = new JSONParser();
            try {
                browserJsonArray = (JSONArray) parser.parse(browserArray);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return  browserJsonArray;
        }
        else {
            Utility parser = new Utility();
            JSONObject main = parser.loadJsonFile(getConfigFilePath());
            JSONObject run = (JSONObject) main.get("run");
            JSONObject browser = (JSONObject) run.get("browser");
            return (JSONArray) browser.get("name");
        }
    }

    public JSONObject getCapabilities(String browserName)  {
        Utility parser = new Utility();
        JSONObject capabilities =null;
        try {
            JSONObject main = parser.loadJsonFile(getConfigFilePath());
            JSONObject run = (JSONObject) main.get("run");
            JSONObject browser = (JSONObject) run.get("capabilities");
            capabilities = (JSONObject) browser.get(browserName);
            return capabilities;
        }catch (Exception e) {
            return null;
        }

    }

    public ArrayList<String> getTags() {
        if(setCommandLineArgument.byTag !=null){
            JSONArray tag=new JSONArray();
            tag.add(setCommandLineArgument.byTag);
            return tag;
        }
        else {
            Utility parser = new Utility();
            JSONObject main = parser.loadJsonFile(getConfigFilePath());
            JSONObject run = (JSONObject) main.get("run");
            JSONObject by = (JSONObject) run.get("by");
            return (JSONArray) by.get("tag");
        }
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
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        try {
            String ExtCodeDirectory=main.get("projectDIR").toString()+"/ExtTestCode";
            return ExtCodeDirectory;
        }catch (Exception e){
            throw new TesboException("ExtCodeDirectory is not define on config");
        }
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
        if(runby.contains("Suite") || runby.contains("SUITE"))
        {throw new TesboException("Enter 'suite' in small case on config file");}

        if(runby.contains("Tag") || runby.contains("TAG") && setCommandLineArgument.byTag ==null)
        {throw new TesboException("Enter 'tag' in small case on config file");}

        if (runby.contains("tag") || setCommandLineArgument.byTag !=null) {
            return "tag";
        } else if (runby.contains("suite")) {
            return "suite";
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

    /**
     * @auther : Viral Patel
     * @lastModifiedBy:
     * @return
     */
    public JSONObject getCloudIntegration() {
        Utility parser = new Utility();
        try {

            return (JSONObject) parser.loadJsonFile(getConfigFilePath()).get("cloudIntegration");


        }catch (Exception e){
            throw new TesboException("Cloud Integration is not defined");
        }
    }

    public String getBaseUrl() {
        String baseUrl = "";
        if(getEnvironment()!=null){
            baseUrl=getEnvironment();
        }
        else {
            if (setCommandLineArgument.baseUrl != null) {
                baseUrl = setCommandLineArgument.baseUrl;
            } else {
                Utility parser = new Utility();
                try {
                    baseUrl = ((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("baseUrl").toString();
                } catch (Exception e) {
                }
            }
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

    public String getSuitesDirectory()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        String SuiteDirectory=main.get("projectDIR").toString()+"/suite";
        return SuiteDirectory;
    }


    public String getLocatorDirectory() {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        String locatorDirectory=main.get("projectDIR").toString()+"/locator";
        return locatorDirectory;
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
        try {
            return (String) retryAnalyser.get("count");
        }catch (Exception e)
        {
            return "0";
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param browser
     * @return
     */
    public String getBinaryPath(String browser)  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        JSONObject browsersPath = (JSONObject) run.get("binaries");
        try {
            if(browsersPath.get(browser).toString()==null | browsersPath.get(browser).toString().trim().equals("")){
                return null;
            }
            else {
                return (String) browsersPath.get(browser);
            }
        }catch (Exception e){
            return null;
        }
    }


    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public boolean getSingleWindowRun()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        try {
            return (boolean) run.get("SingleWindowRun");
        }catch (Exception e)
        {
            return false;
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public boolean getHighlightElement()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        try {
            return (boolean) run.get("highlightElement");
        }catch (Exception e)
        {
            return false;
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public boolean getPauseStepDisplay()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        try {
            return (boolean) run.get("printPause");
        }catch (Exception e)
        {
            return true;
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public boolean getBrowserClose()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        try {
            return (boolean) run.get("browserClose");
        }catch (Exception e)
        {
            return true;
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public boolean getIsGrid()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        try {
            return (boolean) run.get("IsGrid");
        }catch (Exception e)
        {
            return false;
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public String getEnvironment() {
        Utility parser = new Utility();
        JSONObject Environment = (JSONObject) ((JSONObject) parser.loadJsonFile(getConfigFilePath()).get("run")).get("Environment");
        if(setCommandLineArgument.Environment !=null){
            return (String) Environment.get(setCommandLineArgument.Environment);
        }
        return null;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public boolean getRunPastFailure()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");

        try {
            if(setCommandLineArgument.runPastFailure !=null){
                return Boolean.parseBoolean(setCommandLineArgument.runPastFailure);
            }
            else {
                return (boolean) run.get("runPastFailure");
            }
        }catch (Exception e)
        {
            return false;
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public boolean getIsCloudIntegration() {
        Utility parser = new Utility();
        try {


            JSONObject main = parser.loadJsonFile(getConfigFilePath());
            JSONObject cloudIntegration = (JSONObject) main.get("cloudIntegration");
            return (boolean) cloudIntegration.get("report");

        }catch (Exception e){
            throw new TesboException("Cloud Integration is not defined");
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public ArrayList<String> getLocatorPreference()  {
        Utility parser = new Utility();
        JSONObject main = parser.loadJsonFile(getConfigFilePath());
        JSONObject run = (JSONObject) main.get("run");
        return (JSONArray) run.get("locatorPreference");
    }

}
