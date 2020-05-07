package framework;

import Execution.SetCommandLineArgument;
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
import java.util.LinkedList;
import java.util.List;

public class Validation {

    TestsFileParser testsFileParser=new TestsFileParser();
    GetConfiguration getCofig=new GetConfiguration();
    TesboLogger tesboLogger =new TesboLogger();
    private static final Logger log = LogManager.getLogger(Validation.class);

    String testNameValidation="Test: ";
    String testNameText="testName";
    String spaceRegex="\\s{2,}";
    String regexForBrackets="\\[|\\]";
    String newLineRegex="[\\r\\n]+";
    String beforeTestText="BeforeTest:";
    String afterTestText="AfterTest:";
    String testErrorMsg="' test";
    String closeText="[Close:";

    public void beforeExecutionValidation(){

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
            String errorMsg="'config.json' file not found in projet";
            log.error(errorMsg);
            throw new TesboException(errorMsg);
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

        String configName;
        if(SetCommandLineArgument.configFile !=null){
            configName=SetCommandLineArgument.configFile;
        }
        else {
            configName="config.json";
        }
        File file = new File(configName);
        Path path = Paths.get(file.getAbsolutePath());
        if(!Files.exists(path)){
            String errorMsg="\""+file.getAbsolutePath()+"\""+ " config file is not found in project";
            log.error(errorMsg);
            throw new TesboException(errorMsg);
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
            String errorMsg="Project directory is empty or not found: \""+projectDirectoryPath+"\"";
            log.error(errorMsg);
            throw new TesboException(errorMsg);
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

    public void locatorDirectoryPathValidation() {

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

        List<String> browserList=new LinkedList<>();
        try {
            browserList=getCofig.getBrowsers();

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
        }

        if(browserList.isEmpty()) {
            log.error("Browser name is not define on config file.");
            throw new TesboException("Browser name is not define on config file.");
        }
        else {
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
            if (!tagName.isEmpty()) {
                for (Object tag : tagName) {
                    JSONObject testNameByTag = testsFileParser.getTestNameByTag(tag.toString());
                    if (testNameByTag.size() == 0) {
                        String errorMsg="Test not found for '" + tag.toString() + "' tag.";
                        log.error(errorMsg);
                        throw new TesboException(errorMsg);
                    }
                }
            } else {
                log.error("Tag name is not found in config file.");
                throw new TesboException("Tag name is not found in config file.");
            }
        }
        else if(suiteName!=null) {
            if (!suiteName.isEmpty()) {
                for (Object suite : suiteName) {
                    boolean isSuite=false;
                    JSONArray suiteList= suiteParser.getSuiteFiles(getCofig.getSuitesDirectory());
                    for(Object suitePath : suiteList){
                        File file=new File(suitePath.toString());
                        if(suite.toString().equalsIgnoreCase(file.getName().split("\\.")[0])){ isSuite=true; }
                    }
                    if (!isSuite) {
                        String errorMsg="'"+suite+ "' suite is not found in suite directory";
                        log.error(errorMsg);
                        throw new TesboException(errorMsg);
                    }
                    JSONArray testNameBySuite = suiteParser.getTestNameFromSuiteFile(suiteParser.readSuiteFile(suite.toString()));
                    if (testNameBySuite.isEmpty()) {
                        String errorMsg="'" + suite.toString() + "' suite file is empty. There is no test defined in it";
                        log.error(errorMsg);
                        throw new TesboException(errorMsg);
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

        IfStepParser ifStepParser=new IfStepParser();
        JSONArray listOfSession;

        //Validation for end step
        if(!testExecutionQueue.isEmpty()){
            for (int i = 0; i < testExecutionQueue.size(); i++) {
                JSONObject test= (JSONObject) testExecutionQueue.get(i);
                String testsFileName=test.get("testsFileName").toString();
                String testNameFromTest=test.get(testNameText).toString();

                JSONArray steps= testsFileParser.getTestStepByTestsFileandTestCaseName(testsFileName,testNameFromTest);
                listOfSession = testsFileParser.getSessionListFromTest(testsFileName, testNameFromTest);
                if (!listOfSession.isEmpty()) {
                    sessionDefineValidation(testsFileName, testNameFromTest,listOfSession);
                    sessionNotDeclareOnTest(steps, listOfSession);
                    sessionNotDefineOnTest(steps, listOfSession);
                }
                if(ifStepParser.isTestsHasIFCondition(steps)){
                    ifStepParser.isEndStepForIfCondition(steps,testNameFromTest);
                }
                collectionValidation(testsFileName, testNameFromTest);
                severityAndPriorityValidation(test);
            }
        }
    }

    public void sessionNotDeclareOnTest(JSONArray steps,JSONArray listOfSession) {
        boolean isSessionInTest=false;
        for(Object session:listOfSession)
        {
            for(Object step:steps){
                if( step.toString().replaceAll(spaceRegex, " ").trim().contains("[") && step.toString().replaceAll(spaceRegex, " ").trim().contains("]")) {
                    if(step.toString().replaceAll(regexForBrackets,"").equals(session.toString())){
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
            if (step.toString().replaceAll(spaceRegex, " ").trim().contains("[") && step.toString().replaceAll(spaceRegex, " ").trim().contains("]") &&
                    !(step.toString().replaceAll(spaceRegex, " ").trim().contains("[Close") || step.toString().replaceAll(spaceRegex, " ").trim().contains("DataSet:")) ) {
                boolean isSessionInTest = false;
                for (Object session : listOfSession) {
                    if (step.toString().replaceAll(regexForBrackets, "").equals(session.toString())) {
                        isSessionInTest = true;
                    }
                }
                if (!isSessionInTest) {
                    String errorMsg="Session '" + step.toString().replaceAll(regexForBrackets, "") + "' is not declare.";
                    log.error(errorMsg);
                    throw new TesboException(errorMsg);
                }
            }
        }

    }

    public void sessionDefineValidation(String testsFileName, String testName,JSONArray listOfSession) {
        StringBuilder testsFileNameDetails =testsFileParser.readTestsFile(testsFileName);
        String[] allLines = testsFileNameDetails.toString().split(newLineRegex);
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(testNameValidation) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {
                String[] testNameArray = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted)
                    testCount++;
            }
            if (testStarted) {

                if (allLines[i].trim().equals("End") && !(allLines[i].trim().equals("End::"))) {
                    endpoint = i;
                    break;
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            String errorMsg="End Step is not found for '" + testName + testErrorMsg;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }

        for (int j = startPoint; j < endpoint; j++) {

            for (Object session : listOfSession) {
                if (allLines[j].replaceAll(spaceRegex, " ").trim().replaceAll(regexForBrackets, "").equals(session.toString())) {
                    if (!allLines[j].replaceAll(spaceRegex, " ").trim().equals("[" + session.toString() + "]")) {
                        log.error("Session must be define in '[]' square bracket");
                        throw new TesboException("Session must be define in '[]' square bracket");
                    }
                }

            }
            if(allLines[j].replaceAll(spaceRegex, " ").trim().contains(closeText)){
                String sessionClose=  allLines[j].replaceAll(spaceRegex, " ").trim().replaceAll(regexForBrackets, "").split(":")[1].trim();
                boolean isSessionClose=false;
                for (Object session : listOfSession) {
                    if(sessionClose.equals(session)){
                        isSessionClose=true;
                    }
                }
                if(!isSessionClose){
                    String errorMsg="Session '" + sessionClose + "' is not available.";
                    log.error(errorMsg);
                    throw new TesboException(errorMsg);
                }

            }

            if(allLines[j].replaceAll(spaceRegex, " ").trim().contains(closeText)){
                String closedSession=  allLines[j].replaceAll(spaceRegex, " ").trim().replaceAll(regexForBrackets, "").split(":")[1].trim();

                for (int i = j+1; i < endpoint; i++) {
                    for (Object session : listOfSession) {
                        if (allLines[i].replaceAll(spaceRegex, " ").trim().replaceAll(regexForBrackets, "").equals(session.toString())) {

                            if (allLines[i].replaceAll(spaceRegex, " ").trim().equals("[" + closedSession + "]")) {
                                String errorMsg="Closed session '"+closedSession+"' not define in test";
                                log.error(errorMsg);
                                throw new TesboException(errorMsg);
                            }
                        }
                    }
                }
            }
        }

    }

    public void collectionValidation(String testsFileName, String testName) {
        StringBuilder testsFileNameDetails =testsFileParser.readTestsFile(testsFileName);
        StepParser stepParser=new StepParser();
        String[] allLines = testsFileNameDetails.toString().split(newLineRegex);
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(testNameValidation) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {
                String[] testNameArray = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted)
                    testCount++;
            }
            if (testStarted) {

                if (allLines[i].trim().equals("End") && !(allLines[i].trim().equals("End::"))) {
                    endpoint = i;
                    break;
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            String errorMsg="End Step is not found for '" + testName + testErrorMsg;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }

        for (int j = startPoint; j < endpoint; j++) {

            if(allLines[j].replaceAll(spaceRegex, " ").trim().startsWith("Collection: ")){
                String collectionName=stepParser.getCollectionName(allLines[j]);
                if(collectionName.contains("'") || collectionName.contains("\"")){
                    String errorMsg="Collection name not define properly on :"+allLines[j];
                    log.error(errorMsg);
                    throw new TesboException(errorMsg);
                }
                testsFileParser.getGroupTestStepByTestFileandTestCaseName(collectionName);
            }
        }

    }

    public void keyWordValidation(String step) {
        String newStep=step.replaceAll(spaceRegex, " ").trim().toLowerCase();
        if(newStep.startsWith("step") || newStep.startsWith("verify") || newStep.startsWith("code") || newStep.startsWith("collection")
                || newStep.startsWith("if") || newStep.startsWith("else if") || newStep.startsWith("end")
                || newStep.startsWith("[close") || newStep.startsWith("close")
                || ( step.replaceAll(spaceRegex, " ").trim().contains(closeText) && !(step.replaceAll(spaceRegex, " ").trim().contains("]"))) ){
            String errorMsg="Please write valid keyword for this step \"" +step+"\"";
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
    }


    public boolean severityAndPriorityValidation(JSONObject test) {

        JSONArray steps= testsFileParser.getSeverityAndPriority(test);
        if(!steps.isEmpty()){
            for(int i = 0; i < steps.size(); i++) {
                Object step = steps.get(i);

                try{
                    if(step.toString().replaceAll(spaceRegex, " ").trim().split(":").length==2) {

                        if (step.toString().replaceAll(spaceRegex, " ").trim().contains("Priority: ")) {
                            String priority=step.toString().replaceAll(spaceRegex, " ").trim().split(":")[1];
                            if (!(priority.trim().equalsIgnoreCase("high")
                                    || priority.trim().equalsIgnoreCase("medium")
                                    || priority.trim().equalsIgnoreCase("low"))) {
                                String errorMsg="Enter valid priority name: '"+step+"'";
                                log.error(errorMsg);
                                throw new TesboException(errorMsg);
                            }
                        }
                        if (step.toString().replaceAll(spaceRegex, " ").trim().contains("Severity: ")) {
                            String severity=step.toString().replaceAll(spaceRegex, " ").trim().split(":")[1];
                            if (!(severity.trim().equalsIgnoreCase("critical") || severity.trim().equalsIgnoreCase("major")
                                    || severity.trim().equalsIgnoreCase("medium") || severity.trim().equalsIgnoreCase("minor"))) {
                                String errorMsg="Enter valid severity name: '"+step+"'";
                                log.error(errorMsg);
                                throw new TesboException(errorMsg);
                            }
                        }
                    }
                }catch (Exception e){
                    String errorMsg="Write step properly: '"+step+"'";
                    log.error(errorMsg);
                    throw new TesboException(errorMsg);
                }

            }
        }

        StringBuilder testsFileDetails = testsFileParser.readTestsFile(test.get("testsFileName").toString());
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        boolean isSeverityOrPriority=false;
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(testNameValidation) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {
                String[] testNameArray = allLines[i].split(":");

                if (testNameArray[1].trim().contains(test.get(testNameText).toString())) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted) {
                    testCount++;
                }
            }
            if (testStarted) {

                if (allLines[i].startsWith("Step: ")) {
                    endpoint = i;
                    break;
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            String errorMsg="Step is not found for '" + test.get(testNameText).toString() + testErrorMsg;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }

        for (int j = startPoint; j < endpoint; j++) {

            if (allLines[j].replaceAll(spaceRegex, " ").trim().contains("Priority: ")
                    || allLines[j].replaceAll(spaceRegex, " ").trim().contains("Severity: ")) {
                isSeverityOrPriority=true;
                break;
            }
        }

        return isSeverityOrPriority;
    }

    public void locatorTypesValidation() {
        List<String> locatorTypes;

        locatorTypes=getCofig.getLocatorPreference();
        if(locatorTypes!=null){
            if(locatorTypes.isEmpty()){
                log.error("Please enter locator types");
                throw new TesboException("Please enter locator types");
            }
        }
    }

}
