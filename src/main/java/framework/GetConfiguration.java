package framework;

import execution.SetCommandLineArgument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import exception.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * Get Configuration class will contains all the methods that will read
 * the configuration from the runner file and run according to that
 */
public class GetConfiguration {

    String suiteName="suite";
    String cloudIntegration="cloudIntegration";
    String status="status";
    String count="count";
    JSONObject main = Utility.loadJsonFile(getConfigFilePath());
    private static final Logger log = LogManager.getLogger(GetConfiguration.class);


    public String getConfigFilePath() {
        String configName;
        if(SetCommandLineArgument.configFile !=null){
            configName=SetCommandLineArgument.configFile;
        }
        else {
            configName="config.json";
        }
        File file = new File(configName);
        return file.getAbsolutePath();
    }

    public List<String> getBrowsers() {

        if(SetCommandLineArgument.browser !=null && !(SetCommandLineArgument.browser.equalsIgnoreCase("all"))){
            String browserList=SetCommandLineArgument.browser;
            String[] browsers=browserList.split(",");
            StringBuilder browser = new StringBuilder();
            for(int i=0;i<browsers.length;i++){
                if(i==0){
                    browser.append("\""+browsers[i]+"\"");
                }
                else {
                    browser.append(",\""+browsers[i]+"\"");
                }
            }
            String browserArray="["+browser+"]";
            JSONArray browserJsonArray = null;
            JSONParser parsers = new JSONParser();
            try {
                browserJsonArray = (JSONArray) parsers.parse(browserArray);
            } catch (ParseException e) {log.error("");}

            return  browserJsonArray;
        }
        else {
            return getBrowserFromConfig();
        }
    }

    public List<String> getBrowserFromConfig() {
        JSONObject run = (JSONObject) main.get("run");
        JSONObject browser = (JSONObject) run.get("browser");
        return (JSONArray) browser.get("name");
    }


    public JSONObject getCapabilities(String browserName)  {
        JSONObject capabilities =null;
        try {
            JSONObject run = (JSONObject) main.get("run");
            JSONObject browser = (JSONObject) run.get("capabilities");
            capabilities = (JSONObject) browser.get(browserName);
            return capabilities;
        }catch (Exception e) {
            return null;
        }

    }

    public List<String> getTags() {
        if(SetCommandLineArgument.byTag !=null){
            JSONArray tag=new JSONArray();
            tag.add(SetCommandLineArgument.byTag);
            return tag;
        }
        else {
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
        JSONObject run = (JSONObject) main.get("run");
        return (JSONObject) run.get("by");
    }
    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public String getSeleniumAddress()  {
        String seleniumAddress =null;
        JSONObject run = (JSONObject) main.get("run");
        try {
            seleniumAddress = run.get("seleniumAddress").toString();

        }catch (Exception e){log.error("");}
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
    public String getCustomStepDirectory()  {
        File file = null;
        try {
            String customStepDirectory=Paths.get("").toAbsolutePath().toString()+"/src/test/java/CustomStep";
            file = new File(customStepDirectory);
            if(!file.exists()){
                throw new TesboException("Custom Step Directory is not found: "+file);
            }
            return customStepDirectory;
        }catch (Exception e){
            throw new TesboException("Custom Step Directory is not found: "+file);
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public String getProjectDirectory()  {
        try {
            return Paths.get("").toAbsolutePath().toString()+"/src/test/java/";
        }catch (Exception e){
            throw new TesboException("projectDIR is not define on config");
        }
    }


    public String getRunBy()  {
        String runby = ((JSONObject) main.get("run")).get("by").toString();
        if(runby.contains("Suite") || runby.contains("SUITE"))
        {throw new TesboException("Enter 'suite' in small case on config file");}

        if(runby.contains("Tag") || runby.contains("TAG") && SetCommandLineArgument.byTag ==null)
        {throw new TesboException("Enter 'tag' in small case on config file");}

        if (runby.contains("tag") || SetCommandLineArgument.byTag !=null) {
            return "tag";
        } else if (runby.contains(suiteName)) {
            return suiteName;
        }
        return null;
    }


    /**
     * @auther : Viral Patel
     * @lastModifiedBy:
     * @return
     */
    public JSONObject getCloudIntegration() {
        try {

            return (JSONObject) main.get(cloudIntegration);


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
            if (SetCommandLineArgument.baseUrl != null) {
                baseUrl = SetCommandLineArgument.baseUrl;
            } else {
                try {
                    baseUrl = ((JSONObject) main.get("run")).get("baseUrl").toString();
                } catch (Exception e) { log.error("");}
            }
        }
        return baseUrl;
    }

    public JSONObject getParallel() {
        JSONObject parallelData = (JSONObject) ((JSONObject) main.get("run")).get("parallel");
        JSONObject dataOfparallel = new JSONObject();

        try {

            boolean isTrue = (boolean) parallelData.get(status);
            if (isTrue) {
                dataOfparallel.put(status, parallelData.get(status).toString());
                dataOfparallel.put(count, parallelData.get(count).toString());
                return dataOfparallel;

            } else {
                dataOfparallel.put(status, parallelData.get(status).toString());
                return dataOfparallel;

            }


        } catch (Exception e) {
            dataOfparallel.put(status, "false");

        }

        return dataOfparallel;
    }

    public String getSuitesDirectory()  {
        return Paths.get("").toAbsolutePath().toString()+"/src/test/java/suite";
    }

    public String getTestsDirectory()  {
        return Paths.get("").toAbsolutePath().toString()+"/src/test/java/tests";
    }

    public String getLocatorDirectory() {
        return Paths.get("").toAbsolutePath().toString()+"/src/test/java/locator";
    }

    public String getDataSetDirectory() {
        return Paths.get("").toAbsolutePath().toString()+"/src/test/java/DataSet";
    }

    /**
     * @Description : get suite name from config file.
     * @return : suite names.
     */
    public List<String> getSuite()  {
        JSONObject run = (JSONObject) main.get("run");
        JSONObject by = (JSONObject) run.get("by");
        return (JSONArray) by.get(suiteName);
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public String getRetryAnalyser()  {
        JSONObject run = (JSONObject) main.get("run");
        JSONObject retryAnalyser = (JSONObject) run.get("retryAnalyser");
        try {
            return (String) retryAnalyser.get(count);
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
        JSONObject run = (JSONObject) main.get("run");
        JSONObject browsersPath = (JSONObject) run.get("binaries");
        try {
            if(browsersPath.get(browser).toString()==null || browsersPath.get(browser).toString().trim().equals("")){
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
        JSONObject run = (JSONObject) main.get("run");
        try {
            return (boolean) run.get("isGrid");
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
        JSONObject environment = (JSONObject) ((JSONObject) main.get("run")).get("environment");
        if(SetCommandLineArgument.environment !=null){
            return (String) environment.get(SetCommandLineArgument.environment);
        }
        return null;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public JSONObject getEnvironmentList() {
        return (JSONObject) ((JSONObject) main.get("run")).get("environment");
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public boolean getRunPastFailure()  {

        JSONObject run = (JSONObject) main.get("run");

        try {
            if(SetCommandLineArgument.runPastFailure !=null){
                return Boolean.parseBoolean(SetCommandLineArgument.runPastFailure);
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
        try {
            JSONObject cloudIntegrations = (JSONObject) main.get(cloudIntegration);
            return (boolean) cloudIntegrations.get("report");

        }catch (Exception e){
            return false;
        }
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public List<String> getLocatorPreference()  {
        JSONObject run = (JSONObject) main.get("run");
        return (JSONArray) run.get("locatorPreference");
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public String getBuildName()  {
        JSONObject cloudIntegrations = (JSONObject) main.get(cloudIntegration);
        return cloudIntegrations.get("buildName").toString();

    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @return
     */
    public String getReportFileName()  {
        try {
            return main.get("reportFileName").toString();
        }catch (Exception e)
        {
            return "";
        }
    }
}
