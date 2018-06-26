package framework;



import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Exception.TesboException;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class Validation {

    SuiteParser suiteParser=new SuiteParser();
    GetConfiguration getCofig=new GetConfiguration();

    public void beforeExecutionValidation() throws Exception {


        //Validation for SuiteDirectory Path
        suiteDirectoryPathValidation();

        //Validation for locatorDirectory Path
        locatorDirectoryPathValidation();

        //Validation for baseUrl
        baseUrlValidation();

        //Validation for browser
        browserValidation();

        //Validation for Tag and suite
        byTagAndBySuiteValidation();
        tagNameAndSuiteNameValidation();

        //Validation for parallel execution
        parallelExecutionValidation();

    }

    public void suiteDirectoryPathValidation() {

        //Validation for SuiteDirectory Path is empty
        String suiteDirectoryPath=null;
        suiteDirectoryPath=getCofig.getSuitesDirectory();
        File file = new File(suiteDirectoryPath);
        if(suiteDirectoryPath.equals("")) {
            throw new TesboException("Suite directory path is not define in config file.");
        }
        if(Files.notExists(Paths.get(suiteDirectoryPath))){
            throw new TesboException("Please enter valid suite directory path.");
        }
        if(file.list().length==0){
            throw new TesboException("Suite directory is empty");
        }

    }

    public void locatorDirectoryPathValidation() throws Exception {

        String locatorDirectory=null;
        locatorDirectory=getCofig.getLocatorDirectory();
        File file = new File(locatorDirectory);
        if(locatorDirectory.equals("")) {
            throw new TesboException("Locator directory path is not define in config file.");
        }

        if(Files.notExists(Paths.get(locatorDirectory))){
            throw new TesboException("Please enter valid locator directory path.");
        }
        if(file.list().length==0){
            throw new TesboException("Locator directory is empty");
        }
    }

    public void baseUrlValidation() {

        String baseUrl=getCofig.getBaseUrl();
        if(baseUrl.equals("")) {
            throw new TesboException("Base Url is not define in config file.");
        }
    }

    public void browserValidation() {

        ArrayList<String> browserList=new ArrayList<>();

        try {
            browserList=getCofig.getBrowsers();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(browserList.size()==0) {
            throw new TesboException("Browser name is not define on config file.");
        }
        if(browserList.size()>0){
            boolean flag=false;
            for (String browser:browserList){
                if(browser.equalsIgnoreCase("firefox") || browser.equalsIgnoreCase("chrome") ||browser.equalsIgnoreCase("ie") ){
                    flag=true;
                }
                if(browser.equalsIgnoreCase("Internet Explorer") || browser.equalsIgnoreCase("InternetExplorer")){
                    throw new TesboException("Please enter 'IE' instead of 'Internet Explorer'");
                }
            }
            if(!flag){
                throw new TesboException("Only support Firefox, Chrome and IE browser");
            }
        }
    }

    public void byTagAndBySuiteValidation() {
        try {

            JSONObject byValues= getCofig.getBy();
            boolean isByValue=false;
            for(Object byValue:byValues.keySet())
            {
                if(byValue.toString().equalsIgnoreCase("tag") || byValue.toString().equalsIgnoreCase("suite"))
                    isByValue= true;
            }
            if(!isByValue)
                throw new TesboException("'by.tag' or 'by.suite' is incorrect.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tagNameAndSuiteNameValidation() {
        try {
            JSONArray tagName= (JSONArray) getCofig.getTags();
            JSONArray suiteName= (JSONArray) getCofig.getSuite();

            if(tagName!=null) {
                if (tagName.size() > 0) {
                    for (Object tag : tagName) {
                        JSONObject testName = suiteParser.getTestNameByTag(tag.toString());
                        if (testName.size() == 0) { throw new TesboException("Test not found for '" + tag.toString() + "' tag."); }
                    }
                } else {
                    throw new TesboException("Tag name is not found in config file.");
                }
            }
            else if(suiteName!=null) {
                if (suiteName.size() > 0) {
                    for (Object suite : suiteName) {
                        boolean isSuite=false;
                        JSONArray SuiteList= suiteParser.getSuites(getCofig.getSuitesDirectory());
                        for(Object suitePath : SuiteList){
                            String[] suites=suitePath.toString().split("\\\\");
                            if(suite.toString().equalsIgnoreCase(suites[suites.length-1].split("\\.")[0])){ isSuite=true; }
                        }
                        if (!isSuite) { throw new TesboException("'"+suite+ "' suite is not found in suite directory"); }
                        JSONObject testNameBySuite = suiteParser.getTestNameBySuite(suite.toString());
                        if (testNameBySuite.size() == 0) { throw new TesboException("Test is not found in '" + suite.toString() + "' suite."); }
                    }
                } else { throw new TesboException("Please enter 'Tag name' or 'Suite name' to run test."); }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parallelExecutionValidation() {
        JSONObject parallelExecution = null;
        try {
             parallelExecution=  getCofig.getParallel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(parallelExecution.get("status").toString().equalsIgnoreCase("true"))
        {
            if(parallelExecution.get("count").toString().equalsIgnoreCase("0")) {
                throw new TesboException("Enter count greater than zero in parallel execution.");
            }
        }
    }

    public void endStepValidation(JSONArray testExecutionQueue) {

        SuiteParser suiteParser=new SuiteParser();

        //Validation for end step
        if(testExecutionQueue.size()>0){
            for (int i = 0; i < testExecutionQueue.size(); i++) {
                JSONObject test= (JSONObject) testExecutionQueue.get(i);
                suiteParser.getTestStepBySuiteandTestCaseName(test.get("suiteName").toString(),test.get("testName").toString());
            }
        }


    }

    public void sessionNotDeclareOnTest(JSONArray steps,JSONArray listOfSession) {
        boolean isSessionInTest=false;
        for(Object session:listOfSession)
        {
            for(Object step:steps){
                if( step.toString().replaceAll("\\s{2,}", " ").trim().contains("[") && step.toString().replaceAll("\\s{2,}", " ").trim().contains("]")) {
                    if(step.toString().replaceAll("\\[|\\]","").equals(session.toString())){
                        isSessionInTest=true;
                    }
                }
            }
        }
        if(!isSessionInTest){
            throw new TesboException("Session is not found on test");
        }
    }

    public void sessionNotDefineOnTest(JSONArray steps, JSONArray listOfSession) {

        for (Object step : steps) {
            if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("[") && step.toString().replaceAll("\\s{2,}", " ").trim().contains("]") &&
                    !(step.toString().replaceAll("\\s{2,}", " ").trim().contains("[Close")) ) {
                boolean isSessionInTest = false;
                for (Object session : listOfSession) {
                    if (step.toString().replaceAll("\\[|\\]", "").equals(session.toString())) {
                        isSessionInTest = true;
                    }
                }
                if (!isSessionInTest) {
                    throw new TesboException("Session '" + step.toString().replaceAll("\\[|\\]", "") + "' is not declare.");
                }
            }
        }

    }

    public void keyWordValidation(String step) {

        if(step.replaceAll("\\s{2,}", " ").trim().contains("Step :") | step.replaceAll("\\s{2,}", " ").trim().contains("step:") | step.replaceAll("\\s{2,}", " ").trim().contains("step :")
           | step.replaceAll("\\s{2,}", " ").trim().contains("Verify :") | step.replaceAll("\\s{2,}", " ").trim().contains("verify:") | step.replaceAll("\\s{2,}", " ").trim().contains("verify :")
           | step.replaceAll("\\s{2,}", " ").trim().contains("Collection :") | step.replaceAll("\\s{2,}", " ").trim().contains("collection:") | step.replaceAll("\\s{2,}", " ").trim().contains("collection :")
           | step.replaceAll("\\s{2,}", " ").trim().contains("ExtCode :") | step.replaceAll("\\s{2,}", " ").trim().contains("extCode:") | step.replaceAll("\\s{2,}", " ").trim().contains("extCode :")
           | step.replaceAll("\\s{2,}", " ").trim().contains("extcode :") | step.replaceAll("\\s{2,}", " ").trim().contains("extcode:") | step.replaceAll("\\s{2,}", " ").trim().contains("Extcode :") | step.replaceAll("\\s{2,}", " ").trim().contains("Extcode:")
           | step.replaceAll("\\s{2,}", " ").trim().contains("[Close :") | step.replaceAll("\\s{2,}", " ").trim().contains("[close:") | step.replaceAll("\\s{2,}", " ").trim().contains("[close :") |
                ( step.replaceAll("\\s{2,}", " ").trim().contains("[Close:") && !(step.replaceAll("\\s{2,}", " ").trim().contains("]"))) ){
            throw new TesboException("Please write valid keyword for this step \"" +step+"\"");
        }

    }

}
