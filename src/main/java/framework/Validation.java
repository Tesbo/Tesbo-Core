package framework;



import Execution.SetCommandLineArgument;
import Execution.Tesbo;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Exception.TesboException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Validation {

    TestsFileParser testsFileParser=new TestsFileParser();
    GetConfiguration getCofig=new GetConfiguration();
    TesboLogger tesboLogger =new TesboLogger();
    private static final Logger log = LogManager.getLogger(Validation.class);

    public void beforeExecutionValidation() throws Exception {

        //Validation for Project Directory Path is empty
        log.info("Validation for Project Directory Path is empty or not");
        projectDirectoryPathValidation();

        log.info("Validation for TestsDirectory Path is empty or not");
        //Validation for TestsDirectory Path
        testsDirectoryPathValidation();

        log.info("Validation for locatorDirectory Path is empty or not");
        //Validation for locatorDirectory Path
        locatorDirectoryPathValidation();

        log.info("Validation for baseUrl");
        //Validation for baseUrl
        baseUrlValidation();

        log.info("Validation for browser details");
        //Validation for browser
        browserValidation();

        log.info("Validation for Tag and suite is exist or not");
        //Validation for Tag and suite
        byTagAndBySuiteValidation();
        tagNameAndSuiteNameValidation();

        log.info("Validation for parallel execution");
        //Validation for parallel execution
        parallelExecutionValidation();

        log.info("Validation for locator types");
        //Validation for locator types
        locatorTypesValidation();
    }

    public void testsDirectoryPathValidation() {

        //Validation for TestesDirectory Path is empty
        String testsDirectoryPath=null;
        try {
            testsDirectoryPath=getCofig.getTestsDirectory();

        }catch (Exception e){
            log.error("'config.json' file not found in projet");
            throw new TesboException("'config.json' file not found in project");
        }
        File file = new File(testsDirectoryPath);
        if(testsDirectoryPath.equals("")) {
            log.error("Tests directory path is not define in config file.");
            throw new TesboException("Tests directory path is not define in config file.");
        }
        if(Files.notExists(Paths.get(testsDirectoryPath))){
            log.error("Please enter valid tests directory path.");
            throw new TesboException("Please enter valid tests directory path.");
        }
        if(file.list().length==0){
            log.error("Tests directory is empty");
            throw new TesboException("Tests directory is empty");
        }

    }

    public void configFilePathValidation() {

        //config File Path is exist or not

        SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();
        String configName;
        if(setCommandLineArgument.configFile !=null){
            configName=setCommandLineArgument.configFile;
        }
        else {
            configName="config.json";
        }
        File file = new File(configName);
        Path path = Paths.get(file.getAbsolutePath());
        if(!Files.exists(path)){
            log.error("\""+file.getAbsolutePath()+"\""+ " config file is not found in project");
            throw new TesboException("\""+file.getAbsolutePath()+"\""+ " config file is not found in project");
        }

    }

    public void projectDirectoryPathValidation() {

        //Validation for Project Directory Path is empty
        String projectDirectoryPath=null;
        try {
            projectDirectoryPath=getCofig.getProjectDirectory();
        }catch (Exception e){
            log.error("'config.json' file not found in project");
            throw new TesboException("'config.json' file not found in project");
        }
        File file = new File(projectDirectoryPath);

        File[] files = file.listFiles();
        if (files == null) {
            log.error("Project directory is empty or not found: \""+projectDirectoryPath+"\"");
            throw new TesboException("Project directory is empty or not found: \""+projectDirectoryPath+"\"");
        } else {
            int count=0;
            for (File aFile : files) {
                if(aFile.getName().equals("tests")){count++;}
                if(aFile.getName().equals("locator")){count++;}
                if(aFile.getName().equalsIgnoreCase("runner")){count++;}
            }
            if(count != 3){
                log.error("Project directory has not found 'tests' OR 'locator' OR 'runner' package");
                throw new TesboException("Project directory has not found 'tests' OR 'locator' OR 'runner' package");
            }
        }


        if(projectDirectoryPath.equals("")) {
            log.error("Project directory path is not define in config file.");
            throw new TesboException("Project directory path is not define in config file.");
        }
        if(Files.notExists(Paths.get(projectDirectoryPath))){
            log.error("Please enter valid project directory path.");
            throw new TesboException("Please enter valid project directory path.");
        }
        if(file.list().length==0){
            log.error("Project directory is empty");
            throw new TesboException("Project directory is empty");
        }

    }

    public void locatorDirectoryPathValidation() throws Exception {

        String locatorDirectory=null;
        locatorDirectory=getCofig.getLocatorDirectory();
        File file = new File(locatorDirectory);
        if(locatorDirectory.equals("")) {
            log.error("Locator directory path is not define in config file.");
            throw new TesboException("Locator directory path is not define in config file.");
        }

        if(Files.notExists(Paths.get(locatorDirectory))){
            log.error("Please enter valid locator directory path.");
            throw new TesboException("Please enter valid locator directory path.");
        }
        if(file.list().length==0){
            log.error("Locator directory is empty");
            throw new TesboException("Locator directory is empty");
        }
    }

    public void baseUrlValidation() {

        String baseUrl=getCofig.getBaseUrl();
        if(baseUrl.equals("")) {
            log.error("Base Url is not define in config file.");
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
            tesboLogger.testFailed(sw.toString());
        }

        if(browserList.size()==0) {
            log.error("Browser name is not define on config file.");
            throw new TesboException("Browser name is not define on config file.");
        }
        if(browserList.size()>0){
            boolean flag=false;
            for (String browser:browserList){
                if(browser.equalsIgnoreCase("opera") || browser.equalsIgnoreCase("firefox") || browser.equalsIgnoreCase("chrome") ||browser.equalsIgnoreCase("ie") ){
                    flag=true;
                }
                if(browser.equalsIgnoreCase("Internet Explorer") || browser.equalsIgnoreCase("InternetExplorer")){
                    log.error("Please enter 'IE' instead of 'Internet Explorer'");
                    throw new TesboException("Please enter 'IE' instead of 'Internet Explorer'");
                }
            }
            if(!flag){
                log.error("Only support Firefox, Chrome and IE browser");
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
            if(!isByValue) {
                log.error("'by.tag' or 'by.suite' is incorrect.");
                throw new TesboException("'by.tag' or 'by.suite' is incorrect.");
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
    }

    public void tagNameAndSuiteNameValidation() {

        SuiteParser suiteParser=new SuiteParser();
        JSONArray tagName= (JSONArray) getCofig.getTags();
        JSONArray suiteName= (JSONArray) getCofig.getSuite();

        if(tagName!=null) {
            if (tagName.size() > 0) {
                for (Object tag : tagName) {
                    JSONObject testName = testsFileParser.getTestNameByTag(tag.toString());
                    if (testName.size() == 0) {
                        log.error("Test not found for '" + tag.toString() + "' tag.");
                        throw new TesboException("Test not found for '" + tag.toString() + "' tag.");
                    }
                }
            } else {
                log.error("Tag name is not found in config file.");
                throw new TesboException("Tag name is not found in config file.");
            }
        }
        else if(suiteName!=null) {
            if (suiteName.size() > 0) {
                for (Object suite : suiteName) {
                    boolean isSuite=false;
                    JSONArray SuiteList= suiteParser.getSuiteFiles(getCofig.getSuitesDirectory());
                    for(Object suitePath : SuiteList){
                        File file=new File(suitePath.toString());
                        if(suite.toString().equalsIgnoreCase(file.getName().split("\\.")[0])){ isSuite=true; }
                    }
                    if (!isSuite) {
                        log.error("'"+suite+ "' suite is not found in suite directory");
                        throw new TesboException("'"+suite+ "' suite is not found in suite directory");
                    }
                    JSONArray testNameBySuite = suiteParser.getTestNameFromSuiteFile(suiteParser.readSuiteFile(suite.toString()));
                    if (testNameBySuite.size() == 0) {
                        log.error("'" + suite.toString() + "' suite file is empty. There is no test defined in it");
                        throw new TesboException("'" + suite.toString() + "' suite file is empty. There is no test defined in it");
                    }
                }
            } else {
                log.error("Please enter 'Tag name' or 'Suite name' to run test.");
                throw new TesboException("Please enter 'Tag name' or 'Suite name' to run test.");
            }
        }


    }

    public void parallelExecutionValidation() {
        JSONObject parallelExecution = null;
        try {
            parallelExecution=  getCofig.getParallel();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
        }
        if(parallelExecution.get("status").toString().equalsIgnoreCase("true"))
        {
            if(parallelExecution.get("count").toString().equalsIgnoreCase("0")) {
                log.error("Enter count greater than zero in parallel execution.");
                throw new TesboException("Enter count greater than zero in parallel execution.");
            }
        }
    }

    public void endStepValidation(JSONArray testExecutionQueue) {

        TestsFileParser testsFileParser=new TestsFileParser();
        IfStepParser ifStepParser=new IfStepParser();
        JSONArray listOfSession;
        //Validation for end step
        if(testExecutionQueue.size()>0){
            for (int i = 0; i < testExecutionQueue.size(); i++) {
                JSONObject test= (JSONObject) testExecutionQueue.get(i);
                JSONArray steps= testsFileParser.getTestStepByTestsFileandTestCaseName(test.get("testsFileName").toString(),test.get("testName").toString());
                listOfSession = testsFileParser.getSessionListFromTest(test.get("testsFileName").toString(), test.get("testName").toString());
                if (listOfSession.size() > 0) {
                    sessionDefineValidation(test.get("testsFileName").toString(), test.get("testName").toString(),listOfSession);
                    sessionNotDeclareOnTest(steps, listOfSession);
                    sessionNotDefineOnTest(steps, listOfSession);
                }
                if(ifStepParser.isTestsHasIFCondition(steps)){
                    ifStepParser.isEndStepForIfCondition(steps,test.get("testName").toString());
                }
                collectionValidation(test.get("testsFileName").toString(), test.get("testName").toString());
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
            log.error("Session is not found on test");
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
                    log.error("Session '" + step.toString().replaceAll("\\[|\\]", "") + "' is not declare.");
                    throw new TesboException("Session '" + step.toString().replaceAll("\\[|\\]", "") + "' is not declare.");
                }
            }
        }

    }

    public void sessionDefineValidation(String testsFileName, String testName,JSONArray listOfSession) {
        StringBuffer testsFileNameDetails =testsFileParser.readTestsFile(testsFileName);
        String allLines[] = testsFileNameDetails.toString().split("[\\r\\n]+");
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
            log.error("End Step is not found for '" + testName + "' test");
            throw new TesboException("End Step is not found for '" + testName + "' test");
        }

        for (int j = startPoint; j < endpoint; j++) {

            for (Object session : listOfSession) {
                if (allLines[j].replaceAll("\\s{2,}", " ").trim().toString().replaceAll("\\[|\\]", "").equals(session.toString())) {
                    if (!allLines[j].replaceAll("\\s{2,}", " ").trim().toString().equals("[" + session.toString() + "]")) {
                        log.error("Session must be define in '[]' square bracket");
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
                    log.error("Session '" + sessionClose + "' is not available.");
                    throw new TesboException("Session '" + sessionClose + "' is not available.");
                }

            }

            if(allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[Close:")){
                String closedSession=  allLines[j].replaceAll("\\s{2,}", " ").trim().toString().replaceAll("\\[|\\]", "").split(":")[1].trim();

                for (int i = j+1; i < endpoint; i++) {
                    for (Object session : listOfSession) {
                        if (allLines[i].replaceAll("\\s{2,}", " ").trim().toString().replaceAll("\\[|\\]", "").equals(session.toString())) {

                            if (allLines[i].replaceAll("\\s{2,}", " ").trim().toString().equals("[" + closedSession + "]")) {
                                log.error("Closed session '"+closedSession+"' not define in test");
                                throw new TesboException("Closed session '"+closedSession+"' not define in test");
                            }
                        }
                    }
                }
            }
        }

    }

    public void collectionValidation(String testsFileName, String testName) {
        StringBuffer testsFileNameDetails =testsFileParser.readTestsFile(testsFileName);
        StepParser stepParser=new StepParser();
        String allLines[] = testsFileNameDetails.toString().split("[\\r\\n]+");
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
            log.error("End Step is not found for '" + testName + "' test");
            throw new TesboException("End Step is not found for '" + testName + "' test");
        }

        for (int j = startPoint; j < endpoint; j++) {

            if(allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Collection:")){
                String collectionName=stepParser.getCollectionName(allLines[j].toString());
                if(collectionName.contains("'") |collectionName.contains("\"")){
                    log.error("Collection name not define properly on :"+allLines[j]);
                    throw new TesboException("Collection name not define properly on :"+allLines[j]);
                }
                testsFileParser.getGroupTestStepByTestFileandTestCaseName(testsFileName, collectionName);
            }
        }

    }

    public void keyWordValidation(String step) {
        if(step.replaceAll("\\s{2,}", " ").trim().contains("Step :") | step.replaceAll("\\s{2,}", " ").trim().contains("step:") | step.replaceAll("\\s{2,}", " ").trim().contains("step :")
                | step.replaceAll("\\s{2,}", " ").trim().contains("Verify :") | step.replaceAll("\\s{2,}", " ").trim().contains("verify:") | step.replaceAll("\\s{2,}", " ").trim().contains("verify :")
                | step.replaceAll("\\s{2,}", " ").trim().contains("Collection :") | step.replaceAll("\\s{2,}", " ").trim().contains("collection:") | step.replaceAll("\\s{2,}", " ").trim().contains("collection :")
                | step.replaceAll("\\s{2,}", " ").trim().contains("if::") | step.replaceAll("\\s{2,}", " ").trim().contains("IF::") | step.replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("if ::") | step.replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("if:") | step.replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("if :")
                | step.replaceAll("\\s{2,}", " ").trim().contains("else if::") | step.replaceAll("\\s{2,}", " ").trim().contains("ELSE IF::") | step.replaceAll("\\s{2,}", " ").trim().contains("ELSE::") | step.replaceAll("\\s{2,}", " ").trim().contains("Else ::") | step.replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("else:") | step.replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("else :")
                | step.replaceAll("\\s{2,}", " ").trim().contains("End :") | step.replaceAll("\\s{2,}", " ").trim().contains("End:") | step.replaceAll("\\s{2,}", " ").trim().contains("End ::") | step.replaceAll("\\s{2,}", " ").trim().contains("end ::") | step.replaceAll("\\s{2,}", " ").trim().contains("end::") | step.replaceAll("\\s{2,}", " ").trim().contains("END ::") | step.replaceAll("\\s{2,}", " ").trim().contains("END::")
                | step.replaceAll("\\s{2,}", " ").trim().contains("Code :") | step.replaceAll("\\s{2,}", " ").trim().contains("code :") | step.replaceAll("\\s{2,}", " ").trim().contains("code:") | step.replaceAll("\\s{2,}", " ").trim().contains("ExtCode :") | step.replaceAll("\\s{2,}", " ").trim().contains("ExtCode:")
                | step.replaceAll("\\s{2,}", " ").trim().contains("[Close :") | step.replaceAll("\\s{2,}", " ").trim().contains("[close:") | step.replaceAll("\\s{2,}", " ").trim().contains("[close :")
                | step.replaceAll("\\s{2,}", " ").trim().contains("Close :") | step.replaceAll("\\s{2,}", " ").trim().contains("close:") | step.replaceAll("\\s{2,}", " ").trim().contains("close :")
                | step.replaceAll("\\s{2,}", " ").trim().contains("Close:")|
                ( step.replaceAll("\\s{2,}", " ").trim().contains("[Close:") && !(step.replaceAll("\\s{2,}", " ").trim().contains("]"))) ){
            log.error("Please write valid keyword for this step \"" +step+"\"");
            throw new TesboException("Please write valid keyword for this step \"" +step+"\"");
        }
    }

    public boolean severityAndPriorityValidation(JSONObject test) {

        TestsFileParser testsFileParser=new TestsFileParser();
        JSONArray steps= testsFileParser.getSeverityAndPriority(test);
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
                                log.error("Enter valid priority name: '"+step+"'");
                                throw new TesboException("Enter valid priority name: '"+step+"'");
                            }
                        }
                        if (step.toString().replaceAll("\\s{2,}", " ").trim().contains("Severity:")) {
                            String severity=step.toString().replaceAll("\\s{2,}", " ").trim().split(":")[1];
                            if (!(severity.trim().equalsIgnoreCase("critical") || severity.trim().equalsIgnoreCase("major")
                                    || severity.trim().equalsIgnoreCase("medium") || severity.trim().equalsIgnoreCase("minor"))) {
                                log.error("Enter valid severity name: '"+step+"'");
                                throw new TesboException("Enter valid severity name: '"+step+"'");
                            }
                        }
                    }
                }catch (Exception e){
                    log.error("Write step properly: '"+step+"'");
                    throw new TesboException("Write step properly: '"+step+"'");
                }

            }
        }

        StringBuffer testsFileDetails = testsFileParser.readTestsFile(test.get("testsFileName").toString());
        String allLines[] = testsFileDetails.toString().split("[\\r\\n]+");
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
            log.error("Step is not found for '" + test.get("testName").toString() + "' test");
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

    public void locatorTypesValidation() {
        ArrayList<String> locatorTypes=new ArrayList<>();
        locatorTypes=getCofig.getLocatorPreference();
        if(locatorTypes!=null){
            if(locatorTypes.size()==0){
                log.error("Please enter locator types");
                throw new TesboException("Please enter locator types");
            }
        }
    }

}
