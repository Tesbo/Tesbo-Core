package framework;

import customstep.ExternalCode;
import customstep.Step;
import execution.SetCommandLineArgument;
import execution.TestExecutionBuilder;
import selenium.Commands;
import io.github.bonigarcia.wdm.WebDriverManager;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import reportapi.ReportAPIConfig;
import reportapi.Reporter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class TestExecutorUtility{

    Commands cmd=new Commands();
    GetConfiguration config=new GetConfiguration();
    TesboLogger tesboLogger =new TesboLogger();
    private static final Logger log = LogManager.getLogger(TestExecutorUtility.class);
    StepParser stepParser=new StepParser();
    ReportParser reportParser=new ReportParser();
    CommonMethods commonMethods=new CommonMethods();
    TestsFileParser testsFileParser=new TestsFileParser();
    ExternalCode externalCode=new ExternalCode();
    VerifyParser verifyParser = new VerifyParser();
    StringWriter sw = new StringWriter();


    String firefoxText="firefox";
    String chromeText="chrome";
    String browserNameText="browserName";
    String whiteSpace="\\s{2,}";
    String stepsText="steps";
    String startTimeText="startTime";
    String stepIndexText="stepIndex";
    String statusText="status";
    String passedText="passed";
    String failedText="failed";
    String failedTextMsg="Failed";
    String printText="print";
    String pauseText="pause";
    String randomText="random";
    String stepText="Step: ";
    String verifyText="Verify: ";
    String codeText="Code: ";
    String exceptionAsString = null;
    String screenShotPathText="screenShotPath";
    String testResultText="testResult";
    String sessionListText="sessionList";
    String stepReportObjectText="stepReportObject";
    String driverText="driver";
    String testsFileNameText="testsFileName";
    String testNameText="testName";
    String capabilityText="capability";
    String testStepArrayText="testStepArray";
    String stepPassedText="stepPassed";
    String isSessionText="isSession";


    public String getSeleniumAddress(){
        String seleniumAddress = null;
        boolean isGridFromCommandLineArgument= Boolean.parseBoolean(SetCommandLineArgument.isGrid);
        if(config.getIsGrid() || isGridFromCommandLineArgument) {
            seleniumAddress = cmd.getSeleniumAddress();
        }
        return seleniumAddress;
    }

    public JSONObject browserInitialization(WebDriver driver,String browserName,String seleniumAddress){
        DesiredCapabilities capability = new DesiredCapabilities();
        JSONObject browserData=new JSONObject();

        if(config.getBinaryPath(browserName+"Path")!=null){
            String binaryPathLog="Binary path of "+browserName+": "+config.getBinaryPath(browserName+"Path");
            log.info(binaryPathLog);
            log.info("Initialize browser using binary path");
            driver=initializeBrowserFromBinaryPath(browserName,driver);
        }
        else {
            capability = setCapability(browserName,seleniumAddress,capability);

            JSONObject browserDetails= browserInitialize(driver,browserName,seleniumAddress,capability);
            capability= (DesiredCapabilities) browserDetails.get(capabilityText);
            driver= (WebDriver) browserDetails.get(driverText);
        }
        browserData.put(capabilityText,capability);
        browserData.put(driverText,driver);

        return browserData;

    }
    public JSONObject browserInitialize(WebDriver driver,String browserName,String seleniumAddress,DesiredCapabilities capability){
        JSONObject browserData=new JSONObject();
        if (browserName.equalsIgnoreCase(firefoxText)) {
            capability.setCapability(browserNameText,firefoxText);
            WebDriverManager.firefoxdriver().setup();
            System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
            System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
            if (seleniumAddress == null) {
                driver = new FirefoxDriver();
            }
        }
        if (browserName.equalsIgnoreCase(chromeText)) {
            capability.setCapability(browserNameText,chromeText);
            WebDriverManager.chromedriver().setup();
            if (seleniumAddress == null) {
                driver = new ChromeDriver();
            }
        }
        if (browserName.equalsIgnoreCase("ie")) {
            capability.setCapability(browserNameText,"internetExplorer");
            WebDriverManager.iedriver().setup();
            if (seleniumAddress == null) {
                driver = new InternetExplorerDriver();
            }

        }
        browserData.put(capabilityText,capability);
        browserData.put(driverText,driver);

        return browserData;
    }


    public DesiredCapabilities setCapability(String browserName,String seleniumAddress,DesiredCapabilities capability){
        JSONObject capabilities = null;
        if (cmd.isCapabilities(browserName) && seleniumAddress != null) {
            capabilities = cmd.getCapabilities(browserName);
            if (capabilities != null){
                capability = cmd.setCapabilities(capabilities,capability);
            }
        }
        return capability;
    }

    public Map<String, WebDriver> setSessionList(WebDriver driver,Object session, Map<String, WebDriver> sessionList){
        if (session != null) {
            sessionList.put(session.toString(), driver);
        }
        return sessionList;
    }

    public JSONObject setRemoteBrowser(Object session,WebDriver driver,String seleniumAddress, DesiredCapabilities capability,Map<String, WebDriver> sessionList){
        JSONObject remoteBrowserDetails=new JSONObject();
        if (seleniumAddress != null) {
            driver = cmd.openRemoteBrowser(driver, capability, seleniumAddress);
            if (session != null){ sessionList.put(session.toString(), driver);}
            String seleniumAddressLog="Start test with selenium address: "+seleniumAddress;
            log.info(seleniumAddressLog);
        }
        remoteBrowserDetails.put(driverText,driver);
        remoteBrowserDetails.put(sessionListText,sessionList);
        return remoteBrowserDetails;
    }

    public void openBaseURL(WebDriver driver){
        try {
            if (!config.getBaseUrl().equals("") || (config.getBaseUrl()!=null)) {
                driver.get(config.getBaseUrl());
                String startBrowserUrlLog="Start browser with '"+config.getBaseUrl()+"' URL";
                log.info(startBrowserUrlLog);
            }
        } catch (Exception e) { log.error(""); }
    }

    /**
     * @param step
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public JSONObject initializeSessionRunTime(WebDriver driver, Object step, Map<String, WebDriver> sessionList, JSONArray listOfSession,String browser) {
        JSONObject browserDetails=new JSONObject();
        if (step.toString().replaceAll(whiteSpace, " ").trim().contains("[") && step.toString().replaceAll(whiteSpace, " ").trim().contains("]")) {
            String testStep = step.toString().replace("[", "").replace("]", "");
            for (Object session : listOfSession) {
                if (testStep.equalsIgnoreCase(session.toString())) {
                    browserDetails=initializeSessionBrowser(driver,session,sessionList,testStep,browser);
                }
            }
            tesboLogger.stepLog(step.toString());
            log.info(step);
        }
        return browserDetails;
    }

    public JSONObject initializeSessionBrowser(WebDriver driver, Object session, Map<String, WebDriver> sessionList, String testStep,String browser){
        JSONObject browserDetails=new JSONObject();
        boolean isInSessionList = false;
        for (Map.Entry map : sessionList.entrySet()) {
            if (map.getKey().toString().equalsIgnoreCase(testStep)) {
                isInSessionList = true;
                driver = (WebDriver) map.getValue();
            }
        }
        if (!isInSessionList) {
            browserDetails = initializeBrowser(session,driver,browser,sessionList);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
            }
        }
        return browserDetails;
    }

        public JSONObject addPrintAnsRandomStepForReport(WebDriver driver, String step, JSONObject stepReportObject, JSONObject test){
        if (!(step.contains("{") && step.contains("}") && step.contains(printText) && step.contains(randomText)))  {
            stepReportObject.put(stepsText, step.replace("@",""));
        }
        if (step.contains(printText))  {
            try {
                stepReportObject.put(stepsText,stepParser.printStep(driver,step,test));
            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
            }
        }
        return stepReportObject;
    }

    public JSONObject addStepForReport(WebDriver driver, String step, JSONObject stepReportObject, JSONObject test){
        if (step.replaceAll(whiteSpace, " ").trim().startsWith(stepText)) {
            if (step.toLowerCase().contains(printText)) {
                try {
                    stepReportObject.put(stepsText, stepParser.printStep(driver, step, test));
                } catch (Exception e) {log.error("");}
            }
            if (step.contains("{") && step.contains("}")) {

                stepReportObject.put(stepsText, reportParser.dataSetStepReplaceValue(test, step));
            }

            String newStep= "";
            try {
                newStep = stepParser.parseStep(driver, test, step);
            } catch (IOException e) { log.error("");}

            if (step.toLowerCase().contains(randomText)) {
                stepReportObject.put(stepsText,newStep.replace("@",""));
            }
        }

        if (step.replaceAll(whiteSpace, " ").trim().startsWith(verifyText)) {
            sendVerifyStep(driver,step,test);

        }
        return stepReportObject;
    }

    /**
     * @param step
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public void sendVerifyStep(WebDriver driver,String step,JSONObject test) {
        int count = (int) step.chars().filter(ch -> ch == '@').count();
        String verifyStep = stepParser.removedVerificationTextFromSteps(step);
        if (count >= 2) {
            if (!(verifyStep.toLowerCase().contains(" and ") || verifyStep.toLowerCase().contains(" or "))) {
                executeVerifyStepWhenItHasMultipleElement(step,count,test,driver);
            }else if (verifyStep.toLowerCase().contains(" and "))
            {
                executeVerifyStepWhenItHasAndVerifier(step,test,driver);
            }
            else if (verifyStep.toLowerCase().contains(" or "))
            {
                executeVerifyStepWhenItHasOrVerifier(step,test,driver);
            }
        }
        else {
            verifyParser.parseVerify(driver, test, step);
        }
    }

    public void executeVerifyStepWhenItHasAndVerifier(String step,JSONObject test,WebDriver driver){
        for(String newStep:stepParser.listOfStepWhoHasSameVerifier(step,"and")){
            verifyParser.parseVerify(driver, test,newStep);
        }
    }

    public void executeVerifyStepWhenItHasOrVerifier(String step,JSONObject test,WebDriver driver){
        int failCount = 0;
        for (String orConditionStep : stepParser.listOfStepWhoHasSameVerifier(step,"or")) {
            try {
                verifyParser.parseVerify(driver, test, orConditionStep);
            } catch (Exception e) {
                failCount++;
                if (failCount == stepParser.listOfStepWhoHasSameVerifier(step,"or").size()) {
                    commonMethods.throwAssertException("'" + step + "' step is not verified",log);
                }
            }
        }
    }

    public void executeVerifyStepWhenItHasMultipleElement(String step,int count,JSONObject test,WebDriver driver){
        stepParser.listOfSteps(step, count);
        for (String newStep : stepParser.listOfSteps(step, count)) {
            verifyParser.parseVerify(driver, test, newStep);
        }
    }

    public JSONObject getSeverityAndPriorityIfExist(JSONObject test,JSONObject testReportObject){
        String msgLogText="Get severity and priority for test is: "+stepParser.isSeverityOrPriority(test);
        log.info(msgLogText);
        if(stepParser.isSeverityOrPriority(test)){
            JSONArray severityAndPrioritySteps=testsFileParser.getSeverityAndPriority(test);
            for (int i = 0; i < severityAndPrioritySteps.size(); i++) {
                Object step = severityAndPrioritySteps.get(i);
                if(step.toString().replaceAll(whiteSpace, " ").trim().contains("Priority: ")) {

                    tesboLogger.stepLog(step.toString());
                    log.info(step);
                    testReportObject.put("Priority", step.toString().replaceAll(whiteSpace, " ").trim().split(":")[1].trim());
                }
                if(step.toString().replaceAll(whiteSpace, " ").trim().contains("Severity: ")) {
                    tesboLogger.stepLog(step.toString());
                    log.info(step);
                    testReportObject.put("Severity", step.toString().replaceAll(whiteSpace, " ").trim().split(":")[1].trim());
                }
            }
        }
        return testReportObject;
    }

    public JSONObject executeBeforeTestStepIfExistInTestsFile(WebDriver driver,String testsFileName,int stepIndex,JSONArray testStepArray,JSONObject test,String testResult,String screenShotPath){
        String beforeTextMsg="Before test functionality is exist or not in tests file: "+testsFileParser.isBeforeTestInTestsFile(testsFileName);
        log.info(beforeTextMsg);
        if(testsFileParser.isBeforeTestInTestsFile(testsFileName)){
            JSONArray annotationSteps = testsFileParser.getBeforeAndAfterTestStepByTestsFile(testsFileName, "BeforeTest");
            for (int i = 0; i < annotationSteps.size(); i++) {

                JSONObject stepReportObject = new JSONObject();
                long startTimeStep = System.currentTimeMillis();
                Object step = annotationSteps.get(i);

                if(step.toString().toLowerCase().contains(pauseText) ) {
                    if(config.getPauseStepDisplay()){ stepReportObject.put(stepIndexText, ++stepIndex); }
                }
                else{ stepReportObject.put(stepIndexText, ++stepIndex); }

                stepReportObject.put(startTimeText, startTimeStep);

                JSONObject stepReportDetails=addStepExecutionOfAnnotation(driver,stepReportObject,step.toString(),test);
                testResult= (String) stepReportDetails.get(testResultText);
                screenShotPath= (String) stepReportDetails.get(screenShotPathText);
                stepReportObject= (JSONObject) stepReportDetails.get(stepReportObjectText);

                testStepArray= addStepReportObjectToTestStepArray(testStepArray,stepReportObject,step.toString());

                if (stepReportObject.get(statusText).equals(failedText)) { break; }
            }
        }
        JSONObject testStepDetails=new JSONObject();
        testStepDetails.put(testResultText,testResult);
        testStepDetails.put(screenShotPathText,screenShotPath);
        testStepDetails.put("testSteps",testStepArray);
        testStepDetails.put(stepIndexText,stepIndex);
        return testStepDetails;
    }

    public JSONArray addStepReportObjectToTestStepArray(JSONArray testStepArray,JSONObject stepReportObject,String step){
        if(stepReportObject.size()!=0) {
            if(step.toLowerCase().contains(pauseText) )
            {
                if(config.getPauseStepDisplay()){ testStepArray.add(stepReportObject); }
            }
            else { testStepArray.add(stepReportObject); }
        }
        return testStepArray;
    }

    public JSONObject executeAfterTestStepIfExistInTestsFile(WebDriver driver,String testsFileName,int stepIndex,JSONArray testStepArray,JSONObject test,String testResult,String screenShotPath){
        String beforeTextMsg="Before test functionality is exist or not in tests file: "+testsFileParser.isBeforeTestInTestsFile(testsFileName);
        log.info(beforeTextMsg);
        if (testsFileParser.isAfterTestInTestsFile(testsFileName)) {
            JSONArray annotationSteps = testsFileParser.getBeforeAndAfterTestStepByTestsFile(testsFileName, "AfterTest");
            for (int i = 0; i < annotationSteps.size(); i++) {

                JSONObject stepReportObject = new JSONObject();
                long startTimeStep = System.currentTimeMillis();
                Object step = annotationSteps.get(i);

                if(step.toString().toLowerCase().contains(pauseText) ) {
                    if(config.getPauseStepDisplay()){ stepReportObject.put(stepIndexText, ++stepIndex); }
                }
                else{ stepReportObject.put(stepIndexText, ++stepIndex); }
                stepReportObject.put(startTimeText, startTimeStep);

                JSONObject stepReportDetails = addStepExecutionOfAnnotation(driver, stepReportObject, step.toString(),test);
                testResult= (String) stepReportDetails.get(testResultText);
                screenShotPath= (String) stepReportDetails.get(screenShotPathText);
                stepReportObject= (JSONObject) stepReportDetails.get(stepReportObjectText);

                testStepArray= addStepReportObjectToTestStepArray(testStepArray,stepReportObject,step.toString());

                if (stepReportObject.get(statusText).equals(failedText)) { break; }

            }
        }
        JSONObject testReportDetails=new JSONObject();
        testReportDetails.put(testResultText,testResult);
        testReportDetails.put(screenShotPathText,screenShotPath);
        testReportDetails.put(testStepArrayText,testStepArray);


        return testReportDetails;
    }

    public JSONObject executeTestSteps(WebDriver driver,JSONArray testStepArray, JSONObject test,Map<String, WebDriver> sessionList,JSONArray listOfSession,JSONObject testReportDetails){
        String testsFileName=test.get(testsFileNameText).toString();
        String testName=test.get(testNameText).toString();
        String browser=test.get("browser").toString();
        int stepIndex= (int) testReportDetails.get(stepIndexText);
        boolean isSession= (boolean) testReportDetails.get(isSessionText);
        String testResult= testReportDetails.get(testResultText).toString();
        String screenShotPath=testReportDetails.get(screenShotPathText).toString();

        /*Getting step using testsFileName and Testcase Name*/
        String stepLogInfo="Get steps for "+testName+" test from "+testsFileName+" tests file";
        log.info(stepLogInfo);
        JSONArray steps = testsFileParser.getTestStepByTestsFileandTestCaseName(testsFileName, testName);

        int j = 0;
        String testInfoMsg=testName+" test has "+steps.size()+" steps";
        log.info(testInfoMsg);

        for(int i = 0; i < steps.size(); i++) {
            boolean stepPassed = true;
            JSONObject stepReportObject = new JSONObject();
            long startTimeStep = System.currentTimeMillis();
            Object step = steps.get(i);
            stepPassed=throwErrorWhenIfConditionIsNotFoundForElseOrElseIf(step.toString(),stepPassed);
            JSONObject ifConditionStepDetails= executeIfConditionIfTestHas(step.toString(),stepPassed,test,driver,steps,i);
            i = ((int) ifConditionStepDetails.get("i"));
            stepPassed= (boolean) ifConditionStepDetails.get(stepPassedText);
            steps= (JSONArray) ifConditionStepDetails.get(stepsText);
            if((boolean)ifConditionStepDetails.get("isContinue")){ continue;}

            stepReportObject=addTestStepToStepReportObject(step.toString(),stepReportObject,stepIndex,startTimeStep,driver,test);
            stepIndex= (int) stepReportObject.get(stepIndexText);

            if (isSession) {
                String startSessionLog="Start session for "+step;
                log.info(startSessionLog);
                JSONObject browserDetails  =initializeSessionRunTime(driver, step,sessionList,listOfSession,browser);
                driver=(WebDriver) browserDetails.get(driverText);
                sessionList= (Map<String, WebDriver>) browserDetails.get(sessionListText);
            }
            try {

                stepReportObject=addStepToStepReportObjectWhenItHasDataSet(step.toString(),stepReportObject,test,driver);
                stepReportObject=addVerifyStepToStepReportObjectWhenItHasDataSet(step.toString(),stepReportObject,test,driver);

            } catch (Exception ae)
            {
                stepReportObject=addDataSetStepOnStepReportObjectWhenItThrowError(stepReportObject,step.toString());
                ae.printStackTrace(new PrintWriter(sw));
                exceptionAsString = sw.toString();
                commonMethods.logErrorMsg(failedTextMsg,log);
                commonMethods.logErrorMsg(exceptionAsString,log);
                stepPassed = false;
            }

            JSONObject stepsDetails=new JSONObject();
            stepsDetails.put(stepIndexText,stepIndex);
            stepsDetails.put(isSessionText,isSession);
            stepsDetails.put(testResultText,testResult);
            stepsDetails.put(screenShotPathText,screenShotPath);
            stepsDetails.put(stepPassedText,stepPassed);
            stepsDetails.put(testStepArrayText,testStepArray);
            stepsDetails.put("j",j);
            stepsDetails.put(stepReportObjectText,stepReportObject);

            JSONObject stepRepDetail=executeCodeStepCloseStepAndCollectionStep(driver,test,step.toString(),sessionList,stepsDetails);
            stepIndex= (int) stepRepDetail.get(stepIndexText);
            isSession= (boolean) stepRepDetail.get(isSessionText);
            testResult= stepRepDetail.get(testResultText).toString();
            screenShotPath=stepRepDetail.get(screenShotPathText).toString();
            stepPassed=(boolean) stepRepDetail.get(stepPassedText);
            testStepArray=(JSONArray) stepRepDetail.get(testStepArrayText);
            j= (int) stepRepDetail.get("j");
            stepReportObject=(JSONObject) stepRepDetail.get(stepReportObjectText);
            sessionList= (Map<String, WebDriver>) stepRepDetail.get(sessionListText);

            reportParser.addScreenshotUrlInReport(stepReportObject, step.toString());

            if (stepReportObject.size() != 0) {
                JSONObject stepDetails = addStepResultInReport(driver, stepReportObject, stepPassed,testsFileName,testName);
                stepReportObject= (JSONObject) stepDetails.get(stepReportObjectText);
                testResult= (String) stepDetails.get(testResultText);
                screenShotPath= (String)  stepDetails.get(screenShotPathText);
                testStepArray=addPauseStepOnTestStepArray(step.toString(),testStepArray,stepReportObject);
            }
            if (!stepPassed) {
                break;
            }

            JSONObject codeStepDetails=printExternalCodeStep(step.toString(),stepIndex,testStepArray, j);

            stepIndex= (int) codeStepDetails.get(stepIndexText);
            testStepArray= (JSONArray) codeStepDetails.get(testStepArrayText);
        }

        JSONObject testStepDetails=new JSONObject();

        testStepDetails.put(testResultText,testResult);
        testStepDetails.put(screenShotPathText,screenShotPath);
        testStepDetails.put(driverText,driver);
        testStepDetails.put("testSteps",testStepArray);
        testStepDetails.put(stepIndexText,stepIndex);
        testStepDetails.put("exceptionAsString",exceptionAsString);

        return testStepDetails;
    }

    public JSONObject executeCodeStepCloseStepAndCollectionStep(WebDriver driver,JSONObject test,String step,Map<String, WebDriver> sessionList,JSONObject stepsDetails){
        int stepIndex= (int) stepsDetails.get(stepIndexText);
        boolean isSession= (boolean) stepsDetails.get(isSessionText);
        String testResult= stepsDetails.get(testResultText).toString();
        String screenShotPath=stepsDetails.get(screenShotPathText).toString();
        boolean stepPassed=(boolean) stepsDetails.get(stepPassedText);
        JSONArray testStepArray=(JSONArray) stepsDetails.get(testStepArrayText);
        int j= (int) stepsDetails.get("j");
        JSONObject stepReportObject=(JSONObject) stepsDetails.get(stepReportObjectText);

        if (step.replaceAll(whiteSpace, " ").trim().contains("Close:")) {
            sessionList=executeSessionClosedStep(step, sessionList,driver,isSession);

        } else if (step.replaceAll(whiteSpace, " ").trim().startsWith(codeText))
        {
            try {
                executeExternalCode(step,test,driver);
            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(sw));
                exceptionAsString = sw.toString();
                commonMethods.logErrorMsg(failedTextMsg,log);
                commonMethods.logErrorMsg(sw.toString(),log);
                stepPassed = false;

            }

        } else if (step.replaceAll(whiteSpace, " ").trim().startsWith("Collection: ")) {
            String startStepsLog="Start "+step;
            log.info(startStepsLog);
            JSONArray groupSteps = new JSONArray();
            try {
                String stepInfoLog="Get steps for "+step;
                log.info(stepInfoLog);
                groupSteps = testsFileParser.getGroupTestStepByTestFileandTestCaseName(stepParser.getCollectionName(step));
            } catch (Exception e)
            {
                j++;
                e.printStackTrace(new PrintWriter(sw));
                exceptionAsString = sw.toString();
                commonMethods.logErrorMsg(failedTextMsg,log);
                commonMethods.logErrorMsg(sw.toString(),log);
                stepPassed = false;
            }
            JSONObject collectionReportDetails=new JSONObject();
            collectionReportDetails.put(stepIndexText,stepIndex);
            collectionReportDetails.put("groupSteps",groupSteps);
            collectionReportDetails.put(testStepArrayText,testStepArray);
            collectionReportDetails.put(testResultText,testResult);
            collectionReportDetails.put(screenShotPathText,screenShotPath);

            JSONObject collectionReportData =executeCollectionStep(driver,test,j,stepReportObject,collectionReportDetails);
            testResult= (String) collectionReportData.get(testResultText);
            screenShotPath= (String) collectionReportData.get(screenShotPathText);
            testStepArray= (JSONArray) collectionReportData.get(testStepArrayText);
            j= (int) collectionReportData.get("j");
            stepIndex= (int) collectionReportData.get(stepIndexText);
        }
        stepsDetails.put(stepIndexText,stepIndex);
        stepsDetails.put(isSessionText,isSession);
        stepsDetails.put(testResultText,testResult);
        stepsDetails.put(screenShotPathText,screenShotPath);
        stepsDetails.put(stepPassedText,stepPassed);
        stepsDetails.put(testStepArrayText,testStepArray);
        stepsDetails.put("j",j);
        stepsDetails.put(stepReportObjectText,stepReportObject);
        stepsDetails.put(sessionListText,sessionList);

        return stepsDetails;
    }

    public JSONArray addPauseStepOnTestStepArray(String step,JSONArray testStepArray,JSONObject stepReportObject){
        if(step.toLowerCase().contains(pauseText))
        {
            if(config.getPauseStepDisplay()){ testStepArray.add(stepReportObject); }
        }
        else { testStepArray.add(stepReportObject); }

        return testStepArray;
    }

    public JSONObject addDataSetStepOnStepReportObjectWhenItThrowError(JSONObject stepReportObject,String step){
        if (step.contains("{") && step.contains("}")) {
            stepReportObject.put(stepsText, step.replaceAll("[{,}]", "'").replace("@", ""));
        }
        return stepReportObject;
    }

    public JSONObject printExternalCodeStep(String step,int stepIndex,JSONArray testStepArray,int j){
        if (step.replaceAll(whiteSpace, " ").trim().startsWith(codeText) && (!Reporter.printStepReportObject.isEmpty())) {
            for (int k = 0; k < Reporter.printStepReportObject.size(); k++){
                JSONObject printExtStep = new JSONObject();
                JSONObject extStep=(JSONObject)Reporter.printStepReportObject.get(j);
                printExtStep.put(stepIndexText, ++stepIndex);
                printExtStep.put(stepsText,extStep.get(stepsText));
                printExtStep.put(statusText,passedText);
                tesboLogger.stepLog(extStep.get(stepsText).toString());
                log.info(extStep.get(stepsText));
                testStepArray.add(printExtStep);
            }

            Reporter.printStepReportObject = new JSONArray();
        }
        JSONObject codeStepDetails=new JSONObject();
        codeStepDetails.put(stepIndexText, stepIndex);
        codeStepDetails.put(testStepArrayText, testStepArray);

        return codeStepDetails;
    }

    public JSONObject executeCollectionStep(WebDriver driver,JSONObject test,int j,JSONObject stepReportObject,JSONObject collectionReportDetails){
        String testsFileName=test.get(testsFileNameText).toString();
        String testName=test.get(testNameText).toString();
        int stepIndex= (int) collectionReportDetails.get(stepIndexText);
        JSONArray groupSteps= (JSONArray) collectionReportDetails.get("groupSteps");
        JSONArray testStepArray= (JSONArray) collectionReportDetails.get(testStepArrayText);
        String testResult= (String) collectionReportDetails.get(testResultText);
        String screenShotPath= (String) collectionReportDetails.get(screenShotPathText);
        boolean stepPassed=true;

        for (int s = 0; s <= groupSteps.size() - 1; s++) {
            Object groupStep = groupSteps.get(s);

            long startTimeStep = System.currentTimeMillis();
            stepReportObject.put(stepIndexText, ++stepIndex);
            stepReportObject.put(startTimeText, startTimeStep);
            stepReportObject.put(stepsText, groupStep.toString().replace("@", ""));

            if (groupStep.toString().startsWith(stepText)) {
                try {
                    stepParser.parseStep(driver, test, groupStep.toString());
                } catch (Exception ae) {
                    j++;
                    ae.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    commonMethods.logErrorMsg(failedTextMsg,log);
                    commonMethods.logErrorMsg(sw.toString(),log);
                    stepPassed = false;
                }
            } else if (groupStep.toString().startsWith(verifyText)) {
                try {
                    sendVerifyStep(driver,groupStep.toString(),test);
                } catch (Exception ne) {
                    j++;
                    ne.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    commonMethods.logErrorMsg(failedTextMsg,log);
                    commonMethods.logErrorMsg(sw.toString(),log);
                    stepPassed = false;
                }
            }
            else if (groupStep.toString().replaceAll(whiteSpace, " ").trim().startsWith(codeText)) {

                try {
                    executeExternalCode(groupStep.toString(),test,driver);

                } catch (Exception e) {
                    e.printStackTrace(new PrintWriter(sw));
                    exceptionAsString = sw.toString();
                    commonMethods.logErrorMsg(failedTextMsg,log);
                    commonMethods.logErrorMsg(sw.toString(),log);
                    stepPassed = false;
                }
            }
            reportParser.addScreenshotUrlInReport(stepReportObject, groupStep.toString());

            JSONObject stepDetails = addStepResultInReport(driver, stepReportObject, stepPassed,testsFileName,testName);
            stepReportObject= (JSONObject) stepDetails.get(stepReportObjectText);
            testResult= (String) stepDetails.get(testResultText);
            screenShotPath= (String)  stepDetails.get(screenShotPathText);
            testStepArray.add(stepReportObject);
            stepReportObject = new JSONObject();
        }
        JSONObject collectionReportData=new JSONObject();
        collectionReportData.put(testResultText,testResult);
        collectionReportData.put(screenShotPathText,screenShotPath);
        collectionReportData.put(testStepArrayText,testStepArray);
        collectionReportData.put("j",j);
        collectionReportData.put(stepIndexText,stepIndex);


        return collectionReportData;
    }

    public void executeExternalCode(String step,JSONObject test,WebDriver driver) throws Exception {
        if (step.contains("{") && step.contains("}")) {
            String replaceStepArgsLog=stepParser.replaceArgsOfCodeStep(test,step);
            tesboLogger.stepLog(replaceStepArgsLog);
            log.info(replaceStepArgsLog);
        }else {
            tesboLogger.stepLog(step);
            log.info(step);
        }
        externalCode.runAllAnnotatedWith(Step.class, step,test, driver);
    }

    public Map<String, WebDriver> executeSessionClosedStep(String step,Map<String, WebDriver> sessionList,WebDriver driver,boolean isSession){
        String sessionName = step.split(":")[1].trim().replace("]", "");
        boolean isSessions = false;
        for (Map.Entry session : sessionList.entrySet()) {
            if (session.getKey().toString().equals(sessionName)) {
                isSessions = true;
                break;
            }
        }
        if (isSessions) {
            sessionList=stepToExecuteAfterTest(driver,sessionName,sessionList,isSession);
            String sessionClosedLog=sessionName+" session is closed";
            log.info(sessionClosedLog);
        }
        return sessionList;
    }

    public JSONObject addStepToStepReportObjectWhenItHasDataSet(String step,JSONObject stepReportObject,JSONObject test,WebDriver driver) throws IOException {
        if (step.replaceAll(whiteSpace, " ").trim().startsWith(stepText)) {
            if (step.contains("{") && step.contains("}")) {
                stepReportObject.put(stepsText, reportParser.dataSetStepReplaceValue(test, step));
            }
            String stepNew = stepParser.parseStep(driver, test, step);

            if (step.toLowerCase().contains(randomText)) {
                stepReportObject.put(stepsText, stepNew.replace("@", ""));
            }
        }
        return stepReportObject;
    }

    public JSONObject addVerifyStepToStepReportObjectWhenItHasDataSet(String step,JSONObject stepReportObject,JSONObject test,WebDriver driver) {
        if (step.replaceAll(whiteSpace, " ").trim().startsWith(verifyText)) {
            if (step.contains("{") && step.contains("}")) {
                stepReportObject.put(stepsText, reportParser.dataSetStepReplaceValue(test, step));
            }
            sendVerifyStep(driver,step,test);
        }
        return stepReportObject;
    }


    public boolean throwErrorWhenIfConditionIsNotFoundForElseOrElseIf(String step,boolean stepPassed){
        if((step.toLowerCase().startsWith("else::") || step.toLowerCase().startsWith("else if:: ") || step.toLowerCase().startsWith("end::")))
        {
            try {
                commonMethods.throwTesboException("If condition is not found for '" + step + "' step.",log);
            }catch (Exception e){
                e.printStackTrace(new PrintWriter(sw));
                exceptionAsString = sw.toString();
                commonMethods.logErrorMsg(failedTextMsg,log);
                commonMethods.logErrorMsg(exceptionAsString,log);
                stepPassed = false;
            }
        }
        return stepPassed;
    }

    public JSONObject executeIfConditionIfTestHas(String step,boolean stepPassed,JSONObject test,WebDriver driver,JSONArray steps,int i){
        IfStepParser ifStepParser=new IfStepParser();
        JSONObject detailsOfIfConditionStep=new JSONObject();
        boolean isContinue=false;
        if(step.startsWith("If:: ") && !(step.toLowerCase().startsWith("else if:: "))){
            try{
                steps= ifStepParser.getStepsOfTestWhoHasIfCondition(driver,test,steps);
            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(sw));
                exceptionAsString = sw.toString();
                commonMethods.logErrorMsg(failedTextMsg,log);
                commonMethods.logErrorMsg(exceptionAsString,log);
                stepPassed = false;
            }
            try {
                if (step.startsWith("If:: ")) {
                    i--;
                    isContinue=true;
                }
            }catch (Exception e1){ isContinue=true; }
        }
        detailsOfIfConditionStep.put("i",i);
        detailsOfIfConditionStep.put(stepPassedText,stepPassed);
        detailsOfIfConditionStep.put("isContinue",isContinue);
        detailsOfIfConditionStep.put(stepsText,steps);

        return detailsOfIfConditionStep;
    }

    public JSONObject addTestStepToStepReportObject(String step, JSONObject stepReportObject,int stepIndex,long startTimeStep,WebDriver driver,JSONObject test){
        if (!step.replaceAll(whiteSpace, " ").trim().startsWith("Collection: ")) {
            if(step.toLowerCase().contains(pauseText) ) {
                if(config.getPauseStepDisplay()){ stepReportObject.put(stepIndexText, ++stepIndex); }
            }
            else{
                stepReportObject.put(stepIndexText, ++stepIndex);
            }

            stepReportObject.put(startTimeText, startTimeStep);

            stepReportObject=addSimpleTestStepToStepReportObject(step,stepReportObject,test);

            if (step.toLowerCase().contains(printText)) {
                try {
                    stepReportObject.put(stepsText, stepParser.printStep(driver, step, test));
                } catch (Exception e) {log.error("");}
            }

        }
        return stepReportObject;
    }

    public String getRemoveContent(String[] stepsWord){
        String removeContent="";
        for(String word:stepsWord){
            if(word.contains("@") && !(word.contains("'"))){
                removeContent= word.trim().replace("@","");
            }
        }
        return removeContent;
    }

    public JSONObject addSimpleTestStepToStepReportObject(String step,JSONObject stepReportObject,JSONObject test){
        if ( !(step.contains("{") && step.contains("}") && step.contains(printText) && step.contains(randomText))) {
            stepReportObject=addPauseStepToStepReportObject(step,stepReportObject);

            if(step.contains("{") && step.contains("}") && step.contains("Code")){
                stepReportObject.put(stepsText, stepParser.replaceArgsOfCodeStep(test,step));
            }
            else {
                if(step.contains("@")){
                    String[] stepsWord=step.split(" ");
                    boolean flag=false;
                    String removeContent=getRemoveContent(stepsWord);
                    if(removeContent!=null){flag=true;}
                    stepReportObject=removeAtSignAndAddStepToStepReportObject(removeContent,flag,stepReportObject,step);
                }
                else {
                    stepReportObject.put(stepsText, step.replace("@", ""));
                }
            }
        }
        return stepReportObject;
    }

    public JSONObject addPauseStepToStepReportObject(String step,JSONObject stepReportObject){
        if(step.toLowerCase().contains(pauseText) && config.getPauseStepDisplay()){
            stepReportObject.put(stepsText, step.replace("@", ""));
        }
        return stepReportObject;
    }

    public JSONObject removeAtSignAndAddStepToStepReportObject(String removeContent,boolean flag,JSONObject stepReportObject,String step){
        if(!removeContent.equals("") && !flag) {
            if (removeContent.contains(".")) {
                stepReportObject.put(stepsText, step.replace("@" + removeContent, removeContent.split("\\.")[1]));
            } else {
                stepReportObject.put(stepsText, step.replace("@" + removeContent, removeContent));
            }
        }
        else{
            if(flag){
                stepReportObject.put(stepsText, step.replace("@" + removeContent, removeContent));
            }
        }
        return stepReportObject;
    }

    public void addReportOnCloud(String testName,String testsFileName,JSONObject testReportObject,String testResult){
        ReportAPIConfig reportAPIConfig = new ReportAPIConfig();
        if(config.getIsCloudIntegration()) {
            boolean isAddOnCloud=false;
            if(testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("null") || testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("false")){
                isAddOnCloud=isRetryAnalyserIsGreaterThenZero(testName,testsFileName,testResult);
            }
            else {
                if(TestExecutionBuilder.failTest == Integer.parseInt(config.getRetryAnalyser()) || testResult.equalsIgnoreCase(passedText)){
                    isAddOnCloud=true;
                }
            }
            if (isAddOnCloud) {
                testReportObject.put("endTime", System.currentTimeMillis());
                reportAPIConfig.createTests(testReportObject);
            }
        }
    }

    public boolean isRetryAnalyserIsGreaterThenZero(String testName,String testsFileName,String testResult){
        if((Integer.parseInt(config.getRetryAnalyser())!=0) || testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("false")){
            return true;
        }
        else {
            if(TestExecutionBuilder.failTest == Integer.parseInt(config.getRetryAnalyser()) || (testResult.equalsIgnoreCase(passedText))){
                return true;
            }
        }
        return false;
    }

    public void manageFailTestExecutionQueue(String testResult,String testName,String testsFileName,JSONObject test,JSONObject classTest){
        TestExecutionBuilder testExecutionBuilder=new TestExecutionBuilder();
        if(testResult.equalsIgnoreCase(failedText)){
            if(testsFileParser.isRetry(testsFileName, testName).equalsIgnoreCase("null")){
                testExecutionBuilder.failTestExecutionQueue(test);
            }
        }
        else {
            if(!TestExecutionBuilder.failTestQueue.isEmpty()){
                Object removeTest=removeTestName(classTest);
                if(removeTest!=null){
                    TestExecutionBuilder.failTestQueue.remove(test);
                }
            }
        }
    }

    public Object removeTestName(JSONObject classTest){
        for(Object failTest:TestExecutionBuilder.failTestQueue){
            if(failTest.equals(classTest)){
                return failTest;
            }
        }
        return null;
    }

    public JSONArray addTestFirstStepOfOpenBaseUrlToReportObject(JSONArray testStepArray,int stepIndex){
        if(stepIndex==0){
            JSONObject stepReportObject = new JSONObject();
            String logStepInfoMsg="Step: Open "+config.getBaseUrl();
            tesboLogger.testLog(logStepInfoMsg);
            log.info(logStepInfoMsg);
            stepReportObject.put(stepIndexText, ++stepIndex);
            stepReportObject.put(stepsText, logStepInfoMsg);
            stepReportObject.put(statusText, passedText);
            testStepArray.add(stepReportObject);

        }
        return testStepArray;
    }

    public JSONObject addTestDetailsOnReportObject(JSONObject testReportObject,JSONObject test,String testName,String testsFileName,String browser){
        long startTime = System.currentTimeMillis();
        /*Adding data into the report*/
        testReportObject.put(startTimeText, startTime);
        testReportObject.put(browserNameText, browser);
        testReportObject.put(testNameText, testName);
        testReportObject.put(testsFileNameText, testsFileName);
        testReportObject.put("suiteName", test.get("suiteName").toString());
        testReportObject.put("tagName", test.get("tagName").toString());
        String logInfoMsg="Test: "+testName;
        tesboLogger.testLog(logInfoMsg);
        log.info(logInfoMsg);

        return testReportObject;
    }

    public JSONObject getSessionListIfTestHasAnsInitializeBrowser(String testsFileName,String testName,boolean isSession,WebDriver driver,String browser,Map<String, WebDriver> sessionList){
        JSONObject testSessionDetails=new JSONObject();
        JSONArray listOfSession = testsFileParser.getSessionListFromTest(testsFileName, testName);
        if (!listOfSession.isEmpty()) {
            isSession = true;
            log.info("Test is run with multiple session");
        } else {
            log.info("Test is run with single session");
            JSONObject browserDetails=initializeBrowser(null,driver,browser,sessionList);
            driver=(WebDriver) browserDetails.get(driverText);
            sessionList= (Map<String, WebDriver>) browserDetails.get(sessionListText);
        }
        testSessionDetails.put(sessionListText,sessionList);
        testSessionDetails.put(driverText,driver);
        testSessionDetails.put("listOfSession",listOfSession);
        testSessionDetails.put(isSessionText,isSession);

        return testSessionDetails;
    }

    public Map<String, WebDriver> stepToExecuteAfterTest(WebDriver driver,String sessionName,Map<String, WebDriver> sessionList,boolean isSession){
        if (sessionName != null) {
            for (Map.Entry session : sessionList.entrySet()) {
                if (sessionName.equals(session.getKey().toString())) {
                    String logInfoText="Close browser for "+sessionName+" session";
                    log.info(logInfoText);
                    driver = (WebDriver) session.getValue();
                    driver.quit();
                    sessionList.remove(session.getKey());
                    log.info("Remove session from list");
                    break;
                }
            }
        } else {
            if (isSession) {
                for (Map.Entry session : sessionList.entrySet()) {
                    driver = (WebDriver) session.getValue();
                    driver.quit();
                }
                log.info("Close all session browser");
            } else {
                driver.quit();
                log.info("Close browser");
            }
        }
        return sessionList;
    }

    public JSONObject addBrowserAndOsDetailsOnReportObject(JSONObject testReportObject, Capabilities caps){
        testReportObject.put("browserVersion", caps.getVersion());
        String osName= caps.getPlatform().toString();
        if(osName.equalsIgnoreCase("xp")) { osName = "windows"; }
        testReportObject.put("osName", osName);
        return testReportObject;
    }

    public JSONObject addTestStepStatusAndEndTimeToReportObject(JSONObject testReportObject,JSONArray testStepArray,String testResult,String screenShotPath,long startTime){
        long stopTimeTest = System.currentTimeMillis();
        testReportObject.put("testStep", testStepArray);

        if (testResult.equals(failedText)) {
            testReportObject.put("fullStackTrace", exceptionAsString);
            testReportObject.put("screenShot", screenShotPath);
        }
        testReportObject.put("totalTime", stopTimeTest - startTime);
        testReportObject.put(statusText, testResult);


        return testReportObject;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param browserName
     * @return
     */
    public WebDriver initializeBrowserFromBinaryPath(String browserName,WebDriver driver) {
        if(config.getBinaryPath(browserName+"Path")!=null){
            if (browserName.equalsIgnoreCase(firefoxText)) {
                System.setProperty("webdriver.gecko.driver", config.getBinaryPath(browserName+"Path"));
                driver = new FirefoxDriver();
            }
            if (browserName.equalsIgnoreCase(chromeText)) {
                System.setProperty("webdriver.chrome.driver", config.getBinaryPath(browserName+"Path"));
                driver = new ChromeDriver();
            }
            if (browserName.equalsIgnoreCase("ie")) {
                System.setProperty("webdriver.ie.driver", config.getBinaryPath(browserName+"Path"));
                driver = new InternetExplorerDriver();
            }
            if (browserName.equalsIgnoreCase("opera")) {
                System.setProperty("webdriver.opera.driver", config.getBinaryPath(browserName+"Path"));
                driver = new OperaDriver();
            }
        }
        return driver;
    }

    /**
     * @auther : Ankit Mistry
     * @param driver
     * @param stepReportObject
     * @param stepPassed
     * @return
     */
    public JSONObject addStepResultInReport(WebDriver driver, JSONObject stepReportObject, boolean stepPassed,String testsFileName,String testName)  {
        String testResult="";
        String screenShotPath="";
        if(stepReportObject.size()!=0) {
            if (!stepPassed) {
                stepReportObject.put(statusText, failedText);
                testResult = failedText;
                screenShotPath = cmd.captureScreenshot(driver, testsFileName, testName);
                String screenshotMsg="Capture screenshot: "+screenShotPath;
                log.error(screenshotMsg);
            } else {
                testResult = passedText;
                stepReportObject.put(statusText, passedText);
            }
            long stepEndTime = System.currentTimeMillis();
            stepReportObject.put("endTime", stepEndTime);
        }
        JSONObject stepDetails=new JSONObject();
        stepDetails.put(stepReportObjectText,stepReportObject);
        stepDetails.put(testResultText,testResult);
        stepDetails.put(screenShotPathText,screenShotPath);

        return stepDetails;
    }

    /**
     * @param session
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public JSONObject initializeBrowser(Object session,WebDriver driver,String browser,Map<String, WebDriver> sessionList) {

        String seleniumAddress =getSeleniumAddress();
        String browserName = browser;
        String startBrowserLog="Start Browser: "+browserName;
        log.info(startBrowserLog);
        DesiredCapabilities capability;
        try {

            JSONObject initializeDetails=browserInitialization(driver,browserName,seleniumAddress);
            driver= (WebDriver) initializeDetails.get(driverText);
            capability= (DesiredCapabilities) initializeDetails.get(capabilityText);

            sessionList=setSessionList(driver,session, sessionList);

            JSONObject remoteBrowserDetails=openRemoteBrowserIfSeleniumAddressExist(session,seleniumAddress,capability,sessionList,driver);
            if(remoteBrowserDetails!=null) {
                driver = (WebDriver) remoteBrowserDetails.get(driverText);
                sessionList = (Map<String, WebDriver>) remoteBrowserDetails.get(sessionListText);
            }

            driver.manage().window().maximize();

            openBaseURL(driver);

        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        }
        JSONObject browserDetails=new JSONObject();
        browserDetails.put(driverText,driver);
        browserDetails.put(sessionListText,sessionList);

        return browserDetails;
    }

    /**
     * @auther : Ankit Mistry
     * @param driver
     * @param stepReportObject
     * @param step
     * @return
     */
    public JSONObject addStepExecutionOfAnnotation(WebDriver driver, JSONObject stepReportObject,String step,JSONObject test)  {

        String testsFileName=test.get(testsFileNameText).toString();
        String testName=test.get(testNameText).toString();

        boolean stepPassed = true;
        stepReportObject=addPrintAnsRandomStepForReport(driver,step,stepReportObject,test);

        try {
            stepReportObject=addStepForReport(driver,step,stepReportObject,test);
        } catch (Exception ae) {
            if (step.contains("{") && step.contains("}")) {
                stepReportObject.put(stepsText, step.replaceAll("[{,}]","'").replace("@",""));
            }
            ae.printStackTrace(new PrintWriter(sw));
            exceptionAsString = sw.toString();
            tesboLogger.testFailed(failedTextMsg);
            tesboLogger.testFailed(exceptionAsString);
            log.error(failedTextMsg);
            log.error(exceptionAsString);
            stepPassed = false;
        }

        if (step.replaceAll(whiteSpace, " ").trim().startsWith(codeText)) {
            try {
                externalCode.runAllAnnotatedWith(Step.class, step,test, driver);
            }catch (Exception e){
                e.printStackTrace(new PrintWriter(sw));
                exceptionAsString = sw.toString();
                tesboLogger.testFailed(failedTextMsg);
                tesboLogger.testFailed(sw.toString());
                log.error(failedTextMsg);
                log.error(sw.toString());
                stepPassed = false;
            }
        }
        JSONObject stepReportDetails=new JSONObject();
        reportParser.addScreenshotUrlInReport(stepReportObject, step);
        if(stepReportObject.size()!=0) {
            JSONObject stepDetails = addStepResultInReport(driver, stepReportObject, stepPassed,testsFileName,testName);
            stepReportObject= (JSONObject) stepDetails.get(stepReportObjectText);
            stepReportDetails.put(testResultText, stepDetails.get(testResultText));
            stepReportDetails.put(screenShotPathText, stepDetails.get(stepReportObjectText));
        }
        stepReportDetails.put(stepReportObjectText,stepReportObject);
        return stepReportObject;
    }

    public JSONObject openRemoteBrowserIfSeleniumAddressExist(Object session,String seleniumAddress, DesiredCapabilities capability,Map<String, WebDriver> sessionLists, WebDriver driver){
        if (seleniumAddress != null) {
            return setRemoteBrowser(session,driver,seleniumAddress,capability,sessionLists);
        }

        return null;
    }

}
