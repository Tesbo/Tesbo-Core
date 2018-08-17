package framework;



import logger.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Exception.TesboException;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class Validation {

    SuiteParser suiteParser=new SuiteParser();
    GetConfiguration getCofig=new GetConfiguration();
    Logger logger=new Logger();

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
        try {
            suiteDirectoryPath=getCofig.getSuitesDirectory();

        }catch (Exception e){
            throw new TesboException("'config.json' file not found in project");
        }
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
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
        }

        if(browserList.size()==0) {
            throw new TesboException("Browser name is not define on config file.");
        }
        if(browserList.size()>0){
            boolean flag=false;
            for (String browser:browserList){
                if(browser.equalsIgnoreCase("opera") || browser.equalsIgnoreCase("firefox") || browser.equalsIgnoreCase("chrome") ||browser.equalsIgnoreCase("ie") ){
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
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
        }
    }

    public void tagNameAndSuiteNameValidation() {

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
                            File file=new File(suitePath.toString());
                            if(suite.toString().equalsIgnoreCase(file.getName().split("\\.")[0])){ isSuite=true; }
                        }
                        if (!isSuite) { throw new TesboException("'"+suite+ "' suite is not found in suite directory"); }
                        JSONObject testNameBySuite = suiteParser.getTestNameBySuite(suite.toString());
                        if (testNameBySuite.size() == 0) { throw new TesboException("Test is not found in '" + suite.toString() + "' suite."); }
                    }
                } else { throw new TesboException("Please enter 'Tag name' or 'Suite name' to run test."); }
            }


    }

    public void parallelExecutionValidation() {
        JSONObject parallelExecution = null;
        try {
             parallelExecution=  getCofig.getParallel();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
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
        JSONArray listOfSession;
        //Validation for end step
        if(testExecutionQueue.size()>0){
            for (int i = 0; i < testExecutionQueue.size(); i++) {
                JSONObject test= (JSONObject) testExecutionQueue.get(i);
                JSONArray steps= suiteParser.getTestStepBySuiteandTestCaseName(test.get("suiteName").toString(),test.get("testName").toString());
                listOfSession = suiteParser.getSessionListFromTest(test.get("suiteName").toString(), test.get("testName").toString());
                if (listOfSession.size() > 0) {
                    sessionDefineValidation(test.get("suiteName").toString(), test.get("testName").toString(),listOfSession);
                    sessionNotDeclareOnTest(steps, listOfSession);
                    sessionNotDefineOnTest(steps, listOfSession);
                }
                collectionValidation(test.get("suiteName").toString(), test.get("testName").toString());
                severityAndPriorityValidation(test);
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
                    !(step.toString().replaceAll("\\s{2,}", " ").trim().contains("[Close") || step.toString().replaceAll("\\s{2,}", " ").trim().contains("DataSet:")) ) {
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

    public void sessionDefineValidation(String suiteName, String testName,JSONArray listOfSession) {
        StringBuffer suiteDetails =suiteParser.readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("Test:")) {
                String testNameArray[] = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted)
                    testCount++;
            }
            if (testStarted) {

                if (allLines[i].contains("End")) {
                    endpoint = i;
                    break;
                }
            }
        }
        if(testCount>=2 || endpoint==0)
            throw new TesboException("End Step is not found for '"+testName+ "' test");

        for (int j = startPoint; j < endpoint; j++) {

            for (Object session : listOfSession) {
                if (allLines[j].replaceAll("\\s{2,}", " ").trim().toString().replaceAll("\\[|\\]", "").equals(session.toString())) {
                    if (!allLines[j].replaceAll("\\s{2,}", " ").trim().toString().equals("[" + session.toString() + "]")) {
                        throw new TesboException("Session must be define in '[]' square bracket");
                    }
                }

            }
            if(allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[Close:")){
                String sessionClose=  allLines[j].replaceAll("\\s{2,}", " ").trim().toString().replaceAll("\\[|\\]", "").split(":")[1].trim();
                boolean isSessionClose=false;
                for (Object session : listOfSession) {
                    if(sessionClose.equals(session)){
                        isSessionClose=true;
                    }
                }
                if(!isSessionClose){
                    throw new TesboException("Session '" + sessionClose + "' is not available.");
                }

            }

            if(allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[Close:")){
                String closedSession=  allLines[j].replaceAll("\\s{2,}", " ").trim().toString().replaceAll("\\[|\\]", "").split(":")[1].trim();

                for (int i = j+1; i < endpoint; i++) {
                    for (Object session : listOfSession) {
                        if (allLines[i].replaceAll("\\s{2,}", " ").trim().toString().replaceAll("\\[|\\]", "").equals(session.toString())) {

                            if (allLines[i].replaceAll("\\s{2,}", " ").trim().toString().equals("[" + closedSession + "]")) {
                                throw new TesboException("Closed session '"+closedSession+"' not define in test");
                            }
                        }
                    }
                }
            }
        }

    }

    public void collectionValidation(String suiteName, String testName) {
        StringBuffer suiteDetails =suiteParser.readSuiteFile(suiteName);
        StepParser stepParser=new StepParser();
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("Test:") && !(allLines[i].contains("BeforeTest:") || allLines[i].contains("AfterTest:"))) {
                String testNameArray[] = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted)
                    testCount++;
            }
            if (testStarted) {

                if (allLines[i].contains("End")) {
                    endpoint = i;
                    break;
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            throw new TesboException("End Step is not found for '" + testName + "' test");
        }

        for (int j = startPoint; j < endpoint; j++) {

            if(allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Collection:")){
                String collectionName=stepParser.getCollectionName(allLines[j].toString());
                if(collectionName.contains("'") |collectionName.contains("\"")){
                    throw new TesboException("Collection name not define properly on :"+allLines[j]);
                }
                suiteParser.getGroupTestStepBySuiteandTestCaseName(suiteName, collectionName);
            }
        }

    }

    public void keyWordValidation(String step) {

        if(step.replaceAll("\\s{2,}", " ").trim().contains("Step :") | step.replaceAll("\\s{2,}", " ").trim().contains("step:") | step.replaceAll("\\s{2,}", " ").trim().contains("step :")
           | step.replaceAll("\\s{2,}", " ").trim().contains("Verify :") | step.replaceAll("\\s{2,}", " ").trim().contains("verify:") | step.replaceAll("\\s{2,}", " ").trim().contains("verify :")
           | step.replaceAll("\\s{2,}", " ").trim().contains("Collection :") | step.replaceAll("\\s{2,}", " ").trim().contains("collection:") | step.replaceAll("\\s{2,}", " ").trim().contains("collection :")
           | step.replaceAll("\\s{2,}", " ").trim().contains("ExtCode :") | step.replaceAll("\\s{2,}", " ").trim().contains("extCode:") | step.replaceAll("\\s{2,}", " ").trim().contains("extCode :")
           | step.replaceAll("\\s{2,}", " ").trim().contains("extcode :") | step.replaceAll("\\s{2,}", " ").trim().contains("extcode:") | step.replaceAll("\\s{2,}", " ").trim().contains("Extcode :") | step.replaceAll("\\s{2,}", " ").trim().contains("Extcode:")
           | step.replaceAll("\\s{2,}", " ").trim().contains("[Close :") | step.replaceAll("\\s{2,}", " ").trim().contains("[close:") | step.replaceAll("\\s{2,}", " ").trim().contains("[close :")
           | step.replaceAll("\\s{2,}", " ").trim().contains("Close :") | step.replaceAll("\\s{2,}", " ").trim().contains("close:") | step.replaceAll("\\s{2,}", " ").trim().contains("close :")
           | step.replaceAll("\\s{2,}", " ").trim().contains("Close:")|
                ( step.replaceAll("\\s{2,}", " ").trim().contains("[Close:") && !(step.replaceAll("\\s{2,}", " ").trim().contains("]"))) ){
            throw new TesboException("Please write valid keyword for this step \"" +step+"\"");
        }

    }

    public boolean severityAndPriorityValidation(JSONObject test) {

        SuiteParser suiteParser=new SuiteParser();
        JSONArray steps= suiteParser.getSeverityAndPriority(test);
        if(steps.size()>0){
            for (int i = 0; i < steps.size(); i++) {
                Object step = steps.get(i);

                try{
                    if(step.toString().replaceAll("\\s{2,}", " ").trim().split(":").length==2) {

                        if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Priority:")) {
                            String priority=step.toString().replaceAll("\\s{2,}", " ").trim().split(":")[1];
                            if (!(priority.trim().equalsIgnoreCase("high")
                                    || priority.trim().equalsIgnoreCase("medium")
                                    || priority.trim().equalsIgnoreCase("low"))) {
                                throw new TesboException("Enter valid priority name: '"+step+"'");
                            }
                        }
                        if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Severity:")) {
                            String severity=step.toString().replaceAll("\\s{2,}", " ").trim().split(":")[1];
                            if (!(severity.trim().equalsIgnoreCase("critical") || severity.trim().equalsIgnoreCase("major")
                                    || severity.trim().equalsIgnoreCase("medium") || severity.trim().equalsIgnoreCase("minor"))) {
                                throw new TesboException("Enter valid severity name: '"+step+"'");
                            }
                        }
                    }
                }catch (Exception e){
                    throw new TesboException("Write step properly: '"+step+"'");
                }

            }
        }





        StringBuffer suiteDetails = suiteParser.readSuiteFile(test.get("suiteName").toString());
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
        boolean isSeverityOrPriority=false;
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("Test:") && !(allLines[i].contains("BeforeTest:") || allLines[i].contains("AfterTest:"))) {
                String testNameArray[] = allLines[i].split(":");

                if (testNameArray[1].trim().contains(test.get("testName").toString())) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted) {
                    testCount++;
                }
            }
            if (testStarted) {

                if (allLines[i].contains("Step:")) {
                    endpoint = i;
                    break;
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            throw new TesboException("Step is not found for '" + test.get("testName").toString() + "' test");
        }

        for (int j = startPoint; j < endpoint; j++) {

            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Priority:")
                    | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Severity:")) {
                isSeverityOrPriority=true;
                break;
            }
        }

        return isSeverityOrPriority;
    }

}
