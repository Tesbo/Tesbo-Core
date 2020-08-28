package framework;

import execution.SetCommandLineArgument;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    String endPointText="endpoint";
    String startPointText="startPoint";
    CommonMethods commonMethods=new CommonMethods();
    SuiteParser suiteParser=new SuiteParser();

    /**
     *
     */
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

    /**
     *
     */
    public void testsDirectoryPathValidation() {

        //Validation for TestesDirectory Path is empty
        String testsDirectoryPath="";
        try {
            testsDirectoryPath=getCofig.getTestsDirectory();

        }catch (Exception e){
            commonMethods.throwTesboException("'config.json' file not found in projet",log);
        }
        File file = new File(testsDirectoryPath);
        if(testsDirectoryPath.equals("")) {
            commonMethods.throwTesboException("Tests directory path is not define in config file.",log);
        }
        if(Files.notExists(Paths.get(testsDirectoryPath))){
            commonMethods.throwTesboException("Please enter valid tests directory path.",log);
        }
        if(file.list().length==0){
            commonMethods.throwTesboException("Tests directory is empty",log);
        }

    }

    /**
     *
     */
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
            commonMethods.throwTesboException("\""+file.getAbsolutePath()+"\""+ " config file is not found in project",log);
        }

    }

    /**
     *
     */
    public void projectDirectoryPathValidation() {

        //Validation for Project Directory Path is empty
        String projectDirectoryPath="";
        try {
            projectDirectoryPath=getCofig.getProjectDirectory();
        }catch (Exception e){
            commonMethods.throwTesboException("'config.json' file not found in project",log);
        }
        File file = new File(projectDirectoryPath);

        File[] files = file.listFiles();
        if (files == null) {
            commonMethods.throwTesboException("Project directory is empty or not found: \""+projectDirectoryPath+"\"",log);
        } else {
            verifyThatProjectDirectoryHasTestsLocatorAndRunnerDirectoryISExistOrNot(files);
        }


        if(projectDirectoryPath.equals("")) {
            commonMethods.throwTesboException("Project directory path is not define in config file.",log);
        }
        if(Files.notExists(Paths.get(projectDirectoryPath))){
            commonMethods.throwTesboException("Please enter valid project directory path.",log);
        }
        if(file.list().length==0){
            commonMethods.throwTesboException("Project directory is empty",log);
        }

    }

    /**
     *
     * @param files
     */
    public void verifyThatProjectDirectoryHasTestsLocatorAndRunnerDirectoryISExistOrNot(File[] files){
        int count=0;
        for (File aFile : files) {
            if(aFile.getName().equals("tests")){count++;}
            if(aFile.getName().equals("locator")){count++;}
            if(aFile.getName().equalsIgnoreCase("runner")){count++;}
        }
        if(count != 3){
            commonMethods.throwTesboException("Project directory has not found 'tests' OR 'locator' OR 'runner' package",log);
        }
    }

    /**
     *
     */
    public void locatorDirectoryPathValidation() {

        String locatorDirectory=null;
        locatorDirectory=getCofig.getLocatorDirectory();
        File file = new File(locatorDirectory);
        if(locatorDirectory.equals("")) {
            commonMethods.throwTesboException("Locator directory path is not define in config file.",log);
        }

        if(Files.notExists(Paths.get(locatorDirectory))){
            commonMethods.throwTesboException("Please enter valid locator directory path.",log);
        }
        if(file.list().length==0){
            commonMethods.throwTesboException("Locator directory is empty",log);
        }
    }

    /**
     *
     */
    public void baseUrlValidation() {

        String baseUrl=getCofig.getBaseUrl();
        if(baseUrl.equals("")) {
            commonMethods.throwTesboException("Base Url is not define in config file.",log);
        }
    }

    /**
     *
     */
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
            commonMethods.throwTesboException("Browser name is not define on config file.",log);
        }
        else {
            boolean flag=false;
            for (String browser:browserList){
                if(browser.equalsIgnoreCase("opera") || browser.equalsIgnoreCase("firefox") || browser.equalsIgnoreCase("chrome") ||browser.equalsIgnoreCase("ie") ){
                    flag=true;
                }
                if(browser.equalsIgnoreCase("Internet Explorer") || browser.equalsIgnoreCase("InternetExplorer")){
                    commonMethods.throwTesboException("Please enter 'IE' instead of 'Internet Explorer'",log);
                }
            }
            if(!flag){
                commonMethods.throwTesboException("Only support Firefox, Chrome and IE browser",log);
            }
        }
    }

    /**
     *
     */
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
                commonMethods.throwTesboException("'by.tag' or 'by.suite' is incorrect.",log);
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
    }

    /**
     *
     */
    public void tagNameAndSuiteNameValidation() {

        JSONArray tagName= (JSONArray) getCofig.getTags();
        JSONArray suiteName= (JSONArray) getCofig.getSuite();

        if(tagName!=null) {
            verifyTestFoundWithTagName(tagName);
        }
        else if(suiteName!=null) {
            verifyTestFoundWithSuiteName(suiteName);
        }


    }

    /**
     *
     * @param tagName
     */
    public void verifyTestFoundWithTagName(JSONArray tagName){
        if (!tagName.isEmpty()) {
            for (Object tag : tagName) {
                JSONObject testNameByTag = testsFileParser.getTestNameByTag(tag.toString());
                if (testNameByTag.size() == 0) {
                    commonMethods.throwTesboException("Test not found for '" + tag.toString() + "' tag.",log);
                }
            }
        } else {
            commonMethods.throwTesboException("Tag name is not found in config file.",log);
        }
    }

    /**
     *
     * @param suiteName
     */
    public void verifyTestFoundWithSuiteName(JSONArray suiteName){

        if (!suiteName.isEmpty()) {
            for (Object suite : suiteName) {
                boolean isSuite=false;
                JSONArray suiteList= suiteParser.getSuiteFiles(getCofig.getSuitesDirectory());
                for(Object suitePath : suiteList){
                    File file=new File(suitePath.toString());
                    if(suite.toString().equalsIgnoreCase(file.getName().split("\\.")[0])){ isSuite=true; }
                }

                verifySuiteFileIsExistOrNotInDirectory(isSuite, suite.toString());
            }
        } else {
            commonMethods.throwTesboException("Please enter 'Tag name' or 'Suite name' to run test.",log);
        }
    }

    /**
     *
     * @param isSuite
     * @param suite
     */
    public void verifySuiteFileIsExistOrNotInDirectory(boolean isSuite, String suite){
        if (!isSuite) {
            commonMethods.throwTesboException("'"+suite+ "' suite is not found in suite directory",log);
        }
        JSONArray testNameBySuite = suiteParser.getTestNameFromSuiteFile(suiteParser.readSuiteFile(suite));
        if (testNameBySuite.isEmpty()) {
            commonMethods.throwTesboException("'" + suite + "' suite file is empty. There is no test defined in it",log);
        }
    }

    /**
     *
     */
    public void parallelExecutionValidation() {
        JSONObject parallelExecution = new JSONObject();
        try {
            parallelExecution=  getCofig.getParallel();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
        }
        if(parallelExecution.get("status").toString().equalsIgnoreCase("true") && parallelExecution.get("count").toString().equalsIgnoreCase("0")) {
            commonMethods.throwTesboException("Enter count greater than zero in parallel execution.",log);
        }
    }

    /**
     *
     * @param testExecutionQueue
     */
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

    /**
     *
     * @param steps
     * @param listOfSession
     */
    public void sessionNotDeclareOnTest(JSONArray steps,JSONArray listOfSession) {
        boolean isSessionInTest=false;
        for(Object session:listOfSession)
        {
            for(Object step:steps){
                if( step.toString().replaceAll(spaceRegex, " ").trim().contains("[") && step.toString().replaceAll(spaceRegex, " ").trim().contains("]") && (step.toString().replaceAll(regexForBrackets,"").equals(session.toString()))){
                    isSessionInTest=true;
                }
            }
        }
        if(!isSessionInTest){
            commonMethods.throwTesboException("Session is not found on test",log);
        }
    }

    /**
     *
     * @param steps
     * @param listOfSession
     */
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
                    commonMethods.throwTesboException("Session '" + step.toString().replaceAll(regexForBrackets, "") + "' is not declare.",log);
                }
            }
        }

    }

    /**
     *
     * @param testsFileName
     * @param testName
     * @param listOfSession
     */
    public void sessionDefineValidation(String testsFileName, String testName,JSONArray listOfSession) {
        StringBuilder testsFileNameDetails =testsFileParser.readTestsFile(testsFileName);
        String[] allLines = testsFileNameDetails.toString().split(newLineRegex);
        int startPoint = 0;
        int endpoint = 0;

        JSONObject startAndEndPoint=testsFileParser.getTestStartAndEndPoint(allLines,testName,"End Step is not found for '" + testName + testErrorMsg);
        startPoint= Integer.parseInt(startAndEndPoint.get(startPointText).toString());
        endpoint= Integer.parseInt(startAndEndPoint.get(endPointText).toString());

        for (int j = startPoint; j < endpoint; j++) {

            verifySessionIsDefineInSquareBracketOrNot(listOfSession,allLines,j);

            verifySessionIsDefineOnTestOrNot(listOfSession,allLines,j);

            verifyClosedSessionIsDefineOnTestOrNot(listOfSession,allLines,j,endpoint);

        }

    }

    /**
     *
     * @param listOfSession
     * @param allLines
     * @param j
     */
    public void verifySessionIsDefineInSquareBracketOrNot(JSONArray listOfSession,String[] allLines,int j){
        for (Object session : listOfSession) {
            if (allLines[j].replaceAll(spaceRegex, " ").trim().replaceAll(regexForBrackets, "").equals(session.toString()) && (!allLines[j].replaceAll(spaceRegex, " ").trim().equals("[" + session.toString() + "]"))) {
                commonMethods.throwTesboException("Session must be define in '[]' square bracket",log);
            }

        }
    }

    /**
     *
     * @param listOfSession
     * @param allLines
     * @param j
     */
    public void verifySessionIsDefineOnTestOrNot(JSONArray listOfSession,String[] allLines,int j){
        if(allLines[j].replaceAll(spaceRegex, " ").trim().contains(closeText)){
            String sessionClose=  allLines[j].replaceAll(spaceRegex, " ").trim().replaceAll(regexForBrackets, "").split(":")[1].trim();
            boolean isSessionClose=false;
            for (Object session : listOfSession) {
                if(sessionClose.equals(session)){
                    isSessionClose=true;
                }
            }
            if(!isSessionClose){
                commonMethods.throwTesboException("Session '" + sessionClose + "' is not available.",log);
            }

        }
    }

    /**
     *
     * @param listOfSession
     * @param allLines
     * @param j
     * @param endpoint
     */
    public void verifyClosedSessionIsDefineOnTestOrNot(JSONArray listOfSession,String[] allLines,int j,int endpoint){
        if(allLines[j].replaceAll(spaceRegex, " ").trim().contains(closeText)){
            String closedSession=  allLines[j].replaceAll(spaceRegex, " ").trim().replaceAll(regexForBrackets, "").split(":")[1].trim();

            for (int i = j+1; i < endpoint; i++) {
                for (Object session : listOfSession) {
                    if (allLines[i].replaceAll(spaceRegex, " ").trim().replaceAll(regexForBrackets, "").equals(session.toString()) && allLines[i].replaceAll(spaceRegex, " ").trim().equals("[" + closedSession + "]")) {
                            commonMethods.throwTesboException("Closed session '"+closedSession+"' not define in test",log);
                    }
                }
            }
        }
    }

    /**
     *
     * @param testsFileName
     * @param testName
     */
    public void collectionValidation(String testsFileName, String testName) {
        StringBuilder testsFileNameDetails =testsFileParser.readTestsFile(testsFileName);
        StepParser stepParser=new StepParser();
        String[] allLines = testsFileNameDetails.toString().split(newLineRegex);
        int startPoint = 0;
        int endpoint = 0;

        JSONObject startAndEndPoint=testsFileParser.getTestStartAndEndPoint(allLines,testName,"End Step is not found for '" + testName + testErrorMsg);
        startPoint= Integer.parseInt(startAndEndPoint.get(startPointText).toString());
        endpoint= Integer.parseInt(startAndEndPoint.get(endPointText).toString());

        for (int j = startPoint; j < endpoint; j++) {
            if(allLines[j].replaceAll(spaceRegex, " ").trim().startsWith("Collection: ")){
                String collectionName=stepParser.getCollectionName(allLines[j]);
                if(collectionName.contains("'") || collectionName.contains("\"")){
                    commonMethods.throwTesboException("Collection name not define properly on :"+allLines[j],log);
                }
                testsFileParser.getGroupTestStepByTestFileandTestCaseName(collectionName);
            }
        }
    }

    /**
     *
     * @param step
     */
    public void keyWordValidation(String step) {
        String newStep=step.replaceAll(spaceRegex, " ").trim().toLowerCase();
        if(newStep.startsWith("step") || newStep.startsWith("verify") || newStep.startsWith("code") || newStep.startsWith("collection")
                || newStep.startsWith("if") || newStep.startsWith("else if") || newStep.startsWith("end")
                || newStep.startsWith("[close") || newStep.startsWith("close")
                || ( step.replaceAll(spaceRegex, " ").trim().contains(closeText) && !(step.replaceAll(spaceRegex, " ").trim().contains("]"))) ){
            commonMethods.throwTesboException("Please write valid keyword for this step \"" +step+"\"",log);
        }
    }

    /**
     *
     * @param test
     * @return
     */
    public boolean severityAndPriorityValidation(JSONObject test) {

        JSONArray steps= testsFileParser.getSeverityAndPriority(test);
        if(!steps.isEmpty()){
            for(int i = 0; i < steps.size(); i++) {
                Object step = steps.get(i);

                try{
                    if(step.toString().replaceAll(spaceRegex, " ").trim().split(":").length==2) {
                        verifyTypeOfPriority(step.toString());
                        verifyTypeOfSeverity(step.toString());
                    }
                }catch (Exception e){
                    commonMethods.throwTesboException("Write step properly: '"+step+"'",log);
                }
            }
        }

        StringBuilder testsFileDetails = testsFileParser.readTestsFile(test.get("testsFileName").toString());
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        boolean isSeverityOrPriority=false;
        int startPoint = 0;
        int endpoint = 0;

        JSONObject startAndEndPoint=testsFileParser.getTestStartAndEndPoint(allLines,test.get(testNameText).toString(),"Step is not found for '" + test.get(testNameText).toString() + testErrorMsg);
        startPoint= Integer.parseInt(startAndEndPoint.get(startPointText).toString());
        endpoint= Integer.parseInt(startAndEndPoint.get(endPointText).toString());

        for (int j = startPoint; j < endpoint; j++) {

            if (allLines[j].replaceAll(spaceRegex, " ").trim().contains("Priority: ")
                    || allLines[j].replaceAll(spaceRegex, " ").trim().contains("Severity: ")) {
                isSeverityOrPriority=true;
                break;
            }
        }

        return isSeverityOrPriority;
    }

    /**
     *
     * @param step
     */
    public void verifyTypeOfPriority(String step){
        if (step.replaceAll(spaceRegex, " ").trim().contains("Priority: ")) {
            String priority=step.replaceAll(spaceRegex, " ").trim().split(":")[1];
            if (!(priority.trim().equalsIgnoreCase("high")
                    || priority.trim().equalsIgnoreCase("medium")
                    || priority.trim().equalsIgnoreCase("low"))) {
                commonMethods.throwTesboException("Enter valid priority name: '"+step+"'",log);
            }
        }
    }

    /**
     *
     * @param step
     */
    public void verifyTypeOfSeverity(String step){
        if (step.replaceAll(spaceRegex, " ").trim().contains("Severity: ")) {
            String severity=step.replaceAll(spaceRegex, " ").trim().split(":")[1];
            if (!(severity.trim().equalsIgnoreCase("critical") || severity.trim().equalsIgnoreCase("major")
                    || severity.trim().equalsIgnoreCase("medium") || severity.trim().equalsIgnoreCase("minor"))) {
                commonMethods.throwTesboException("Enter valid severity name: '"+step+"'",log);
            }
        }
    }

    /**
     *
     */
    public void locatorTypesValidation() {
        List<String> locatorTypes;

        locatorTypes=getCofig.getLocatorPreference();
        if(locatorTypes!=null && locatorTypes.isEmpty()){
            commonMethods.throwTesboException("Please enter locator types",log);
        }
    }

}
