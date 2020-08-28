package execution;

import datacollector.BuildReportDataObject;
import reportbuilder.GetJsonData;
import reportbuilder.ReportBuilder;
import reportbuilder.ReportLibraryFiles;
import selenium.Commands;
import com.diogonunes.jcdp.color.api.Ansi;
import framework.*;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import reportapi.ReportAPIConfig;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestExecutionBuilder {


    public static boolean buildRunning;
    public static String buildReportName;
    public static long buildStartTime;
    public static long buildEndTime;
    public static int failTest=0;
    public static boolean repotFileGenerated = false;
    public static JSONObject dataSetVariable=new JSONObject();
    DataDrivenParser dataDrivenParser = new DataDrivenParser();
    TesboLogger logger = new TesboLogger();
    String buildHistoryPath="./htmlReport/Build History/";
    SuiteParser suiteParser=new SuiteParser();
    TestsFileParser testsFileParser = new TestsFileParser();
    static GetConfiguration config=new GetConfiguration();
    public static boolean singleWindowRun= config.getSingleWindowRun();
    public static boolean isSingleWindow= config.getSingleWindowRun();
    public static WebDriver driver;
    GetJsonData data = new GetJsonData();
    ReportBuilder reportBuilder=new ReportBuilder();
    public static JSONArray failTestQueue=new JSONArray();
    String buildHistory = new File(buildHistoryPath).getAbsolutePath();
    private static final Logger log = LogManager.getLogger(TestExecutionBuilder.class);
    String lastBuildFailTestURL="./htmlReport/Past Fail Test Details/LastBuildFailTest.json";
    String suiteNameTest="suiteName";
    String tagNameText="tagName";
    String dataSizeText="dataSize";
    String dataTypeText="dataType";

    /**
     *
     * @param argumentsArray
     */
    public void startExecution(String[] argumentsArray) {
        Commands cmd=new Commands();
        buildStartTime = System.currentTimeMillis();
        GetConfiguration getConfig = new GetConfiguration();
        Validation validation = new Validation();
        SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();
        setCommandLineArgument.setArgument(argumentsArray);
        String lineMsg="-----------------------------------------------------------------------";
        log.info(lineMsg);
        log.info("Build execution Started");
        log.info(lineMsg);

        logger.titleLog(lineMsg);
        logger.titleLog("Build execution Started");
        logger.titleLog(lineMsg);

        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportParser report = new ReportParser();
        log.info("Generate Report Directory");
        report.generateReportDir();
        buildReportName = builder.getbuildReportName();
        String buildReportNameMsg="Get build report name: "+buildReportName;
        log.info(buildReportNameMsg);
        ReportLibraryFiles files = new ReportLibraryFiles();
        files.createLibrary();

        TestExecutionBuilder.buildRunning = true;
        ReportAPIConfig apiConfig = new ReportAPIConfig();
        log.info("Verify config file path validation");
        validation.configFilePathValidation();
        if(getConfig.getIsCloudIntegration() ) {
            log.info("Get cloud integration build key");
            apiConfig.createBuild();
        }

        BuildReportDataObject brdo = new BuildReportDataObject();
        brdo.startThread();
        reportBuilder.startThread();

        if(SetCommandLineArgument.environment!=null) {
            String environmentName="Environment Name: "+SetCommandLineArgument.environment;
            log.info(environmentName);
            if (SetCommandLineArgument.environment.equalsIgnoreCase("all")) {
                for (Object env : getConfig.getEnvironmentList().keySet()) {
                    failTest=0;
                    SetCommandLineArgument.environment = env.toString();
                    String environmentNameMsg="Start build execution with "+SetCommandLineArgument.environment+" environment";
                    log.info(environmentNameMsg);
                    builder.buildExecution();
                }
            } else {
                String environmentNameMsg="Start build execution with "+SetCommandLineArgument.environment+" environment";
                log.info(environmentNameMsg);
                builder.buildExecution();
            }
        }
        else{
            log.info("Environment is null");
            log.info("Start build execution with base URL");
            builder.buildExecution();
        }

        if(! isSingleWindow && singleWindowRun) {
            log.info("Driver is quit.");
            driver.quit();
        }

        TestExecutionBuilder.buildRunning = false;
        buildEndTime = System.currentTimeMillis();
        BuildReportDataObject.mainReportObject.put("endTime", TestExecutionBuilder.buildEndTime);
        BuildReportDataObject.mainReportObject.put("totalTimeTaken", (TestExecutionBuilder.buildEndTime - TestExecutionBuilder.buildStartTime));
        logger.titleLog(lineMsg);
        logger.titleLog("Build Execution Completed");
        logger.titleLog("-----------------------------------------------------------------------\n");

        if(getConfig.getIsCloudIntegration()) {
            apiConfig.updateBuild();
        }

        while(!repotFileGenerated) {
            cmd.pause(3);
            log.info("Start to generate report");
            reportBuilder.generatReport();
            log.info("Report is generated");
        }
        log.info("***********************************************************************************************************************");

        logger.customeLog("| Total : " + data.getCurrentBuildTotal(new File(buildHistoryPath).getAbsolutePath()), Ansi.FColor.NONE);
        logger.customeLog(" | Passed : " + data.getCurrentBuildPassed(buildHistory), Ansi.FColor.NONE);
        logger.customeLog(" | Failed : " + data.getCurrentBuildFailed(buildHistory), Ansi.FColor.NONE);
        logger.customeLog(" | Time : " + data.getCurrentBuildTotalTime(buildHistory) + " |\n", Ansi.FColor.NONE);

        String totalRunTest="Total Run Test: " + data.getCurrentBuildTotal(new File(buildHistoryPath).getAbsolutePath());
        String totalPassedTest="Total Passed Test: " + data.getCurrentBuildPassed(buildHistory);
        String totalFailedTest="Total Failed Test: " + data.getCurrentBuildFailed(buildHistory);
        String totalTimeToRunTest="Total Time To Run Test: " + data.getCurrentBuildTotalTime(buildHistory) + " |\n";
        log.info(totalRunTest, Ansi.FColor.NONE);
        log.info(totalPassedTest, Ansi.FColor.NONE);
        log.info(totalFailedTest, Ansi.FColor.NONE);
        log.info(totalTimeToRunTest, Ansi.FColor.NONE);


        logger.titleLog(lineMsg);
        log.info("*********************************************** Build Execution Completed ***********************************************");

        String reportFileName=getConfig.getReportFileName();
        if(reportFileName.equals("")){
            reportFileName="currentBuildResult";
        }
        
        reportBuilder.copyReport(reportFileName);
    
    }

    /**
     *
     * @return
     */
    public String getbuildReportName() {
        String newName;
        File buildHistoryFolderPath = new File("./htmlReport/Build History");
        JSONArray testsFileList = new JSONArray();
        try (Stream<Path> paths = Files.walk(Paths.get(buildHistoryFolderPath.getAbsolutePath()))) {
            testsFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
        }

        if (!testsFileList.isEmpty()) {
            File lastModifiedfile = data.getLastModifiedJsonFile(buildHistoryFolderPath.getAbsolutePath());
            String getFile = lastModifiedfile.getName();
            String[] getCount = getFile.split("_")[1].split(".json");
            newName = "buildResult_" + (Integer.parseInt(getCount[0]) + 1);

        } else {
            newName = "buildResult_1";
        }

        return newName;
    }

    /**
     *
     * @param testExecutionQueue
     */
    public void parallelBuilder(JSONArray testExecutionQueue) {
        Validation validation = new Validation();

        JSONObject parallelConfig = config.getParallel();
        int threadCount = 0;
        validation.endStepValidation(testExecutionQueue);
        if (parallelConfig.get("status").toString().equals("true")) {
            threadCount = Integer.parseInt(parallelConfig.get("count").toString());
        } else {
            threadCount = 1;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < testExecutionQueue.size(); i++) {
            Runnable worker = new TestExecutor((JSONObject) testExecutionQueue.get(i));
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    /**
     *
     */
    public void buildExecution(){
        Validation validation = new Validation();

        /**
         * @Discription : Run by tag method.
         */
        try {
            log.info("Start verify necessary validation before start execution");
            validation.beforeExecutionValidation();
            File file = new File(lastBuildFailTestURL);
            boolean runPastFailure=false;
            JSONArray lastBuildFailExecutionQueue = null;
            if (config.getRunPastFailure() && file.exists()) {
                log.info("Get last build fail execution queue details");
                lastBuildFailExecutionQueue = Utility.loadJsonArrayFile(file.getAbsolutePath());
                if (lastBuildFailExecutionQueue != null) {
                    runPastFailure=true;
                }
            }
            if(runPastFailure){
                log.info("Run only that test whose going to fail in last build execution");
                parallelBuilder(lastBuildFailExecutionQueue);
            }else {
                log.info("Start run parallel execution builder");
                parallelBuilder(buildExecutionQueue());
                failBuildExecution();
            }
        } catch (Exception ne) {
            StringWriter sw = new StringWriter();
            ne.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
            log.error(sw.toString());
        }

    }


    /**
     *
     */
    public void failBuildExecution(){

        int failTestQueuesize = failTestQueue.size();
        log.info("Create fail test queue list");
        lastBuildFailTestExecutionQueue(failTestQueue);
        String totalFailTestCount="Number of fail test is "+failTestQueuesize;
        log.info(totalFailTestCount);
        if (failTestQueuesize > 0) {
            failTest=0;
            int retryAnalyserCount = Integer.parseInt(config.getRetryAnalyser());
            String totalNumberOfRetryAnalyser="Number retry analyser is "+retryAnalyserCount;
            log.info(totalNumberOfRetryAnalyser);
            if (retryAnalyserCount > 0) {

                JSONArray testQueue = failTestQueue;
                for (int i = 1; i <= retryAnalyserCount; i++) {
                    failTest++;
                    String runRetryAnalyser="Run retry analyser: "+i;
                    log.info(runRetryAnalyser);
                    parallelBuilder(testQueue);
                    if (failTestQueuesize != failTestQueue.size()) {
                        testQueue = failTestQueue;
                        failTestQueuesize = failTestQueue.size();
                    }
                }
            }
        }
    }

    /**
     * @return
     * @throws Exception
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     */
    public JSONArray buildExecutionQueue() {


        JSONArray completeTestObjectArray = new JSONArray();

        JSONObject suiteOrTagListData=getSuiteOrTagList();
        boolean isSuite= (boolean) suiteOrTagListData.get("isSuite");
        boolean isTag = (boolean) suiteOrTagListData.get("isTag");
        List<String> suiteOrTaglist= (List<String>) suiteOrTagListData.get("suiteOrTaglist");

        for (String suite : suiteOrTaglist) {

            int suiteTestNumber=1;
            JSONObject testNameWithTestsFile= getTestNameWithTestsFileName(isSuite,isTag,suite);
            String suiteName=testNameWithTestsFile.get(suiteNameTest).toString();
            String tagName=testNameWithTestsFile.get(tagNameText).toString();
            JSONObject testNameWithTestsFileName = (JSONObject) testNameWithTestsFile.get("testNameWithTestsFileName");

            for (Object testsFileName : testNameWithTestsFileName.keySet()) {
                String testsFileNAME=getTestsFileName(isSuite,suiteTestNumber,testsFileName.toString(),testNameWithTestsFileName);

                boolean isBeforeTest = isBeforeTestInTestsFile(testsFileNAME);
                boolean isAfterTest = isAfterTestInTestsFile(testsFileNAME);

                for (Object testName : ((JSONArray) testNameWithTestsFileName.get(testsFileName))) {
                    if(isSuite){
                        testsFileName=testsFileName.toString().substring(0, testsFileName.toString().length() - (String.valueOf(suiteTestNumber).length()+1));
                        suiteTestNumber++;
                    }
                    String dataSetName = null;

                    ifIsBeforeAndAfterInTestsFileThenAnnotationDataSetByTestsFile(isBeforeTest,isAfterTest,testsFileName.toString());

                    dataSetName = testsFileParser.getTestDataSetByTestsFileAndTestCaseName(testsFileName.toString(), testName.toString());

                    JSONObject dataSizeAndDataType= getDataTypeAndDataSize(dataSetName, testsFileName.toString(), testName.toString());
                    int dataSize= (int) dataSizeAndDataType.get(dataSizeText);

                    if (dataSize != 0) {
                        completeTestObjectArray= getTestObjectArrayWhenTestHasDataSet(testName, testsFileName.toString(), suiteName, tagName, dataSetName, testsFileNAME, completeTestObjectArray);
                    } else {
                        completeTestObjectArray=  getTestObjectArray(testName, testsFileName.toString(), suiteName, tagName, dataSetName, testsFileNAME, completeTestObjectArray);
                    }
                }
            }
        }
        return completeTestObjectArray;
    }


    /**
     *
     * @return
     */
    public JSONObject getSuiteOrTagList(){
        List<String> suiteOrTaglist = null;
        boolean isTag = false;
        boolean isSuite = false;
        if (config.getRunBy().equals("tag")) {
            suiteOrTaglist = config.getTags();
            log.info("Test is run by tag");
            String tagListMsg="Tag List: "+suiteOrTaglist;
            log.info(tagListMsg);
            isTag = true;
        }

        if (config.getRunBy().equals("suite")) {
            suiteOrTaglist = config.getSuite();
            log.info("Test is run by suite");
            String suiteListMsg="Suite List: "+suiteOrTaglist;
            log.info(suiteListMsg);
            isSuite = true;
        }
        JSONObject suiteOrTagListData=new JSONObject();
        suiteOrTagListData.put("isSuite",isSuite);
        suiteOrTagListData.put("isTag",isTag);
        suiteOrTagListData.put("suiteOrTaglist",suiteOrTaglist);

        return suiteOrTagListData;
    }

    /**
     *
     * @param testName
     * @param testsFileName
     * @param suiteName
     * @param tagName
     * @param dataSetName
     * @param testsFileNAME
     * @param completeTestObjectArray
     * @return
     */
    public JSONArray getTestObjectArrayWhenTestHasDataSet(Object testName, String testsFileName, String suiteName, String tagName, String dataSetName,String testsFileNAME, JSONArray completeTestObjectArray){
        boolean isBeforeTest = isBeforeTestInTestsFile(testsFileNAME);
        boolean isAfterTest = isAfterTestInTestsFile(testsFileNAME);

        JSONObject dataSizeAndDataType= getDataTypeAndDataSize(dataSetName, testsFileName, testName.toString());
        String dataType =dataSizeAndDataType.get(dataTypeText).toString();
        int dataSize= (int) dataSizeAndDataType.get(dataSizeText);

        for (int i = 1; i <= dataSize; i++) {
            for (String browser : config.getBrowsers()) {
                JSONObject completestTestObject = new JSONObject();
                completestTestObject.put("testName", testName);
                completestTestObject.put("testsFileName", testsFileName);
                completestTestObject.put("browser", browser.toLowerCase());
                completestTestObject.put(dataTypeText, dataType);
                completestTestObject.put("row", i);
                completestTestObject.put(suiteNameTest, suiteName);
                completestTestObject.put(tagNameText, tagName);
                completestTestObject.put("dataSetName", dataSetName.replace(" ", "").split(":")[1]);
                if(isBeforeTest){completestTestObject.put("BeforeTest", true);}
                if(isAfterTest){completestTestObject.put("afterTest", true);}
                completeTestObjectArray.add(completestTestObject);
            }
        }
        return completeTestObjectArray;
    }

    /**
     *
     * @param testName
     * @param testsFileName
     * @param suiteName
     * @param tagName
     * @param dataSetName
     * @param testsFileNAME
     * @param completeTestObjectArray
     * @return
     */
    public JSONArray getTestObjectArray(Object testName, String testsFileName, String suiteName, String tagName, String dataSetName,String testsFileNAME, JSONArray completeTestObjectArray){
        boolean isBeforeTest = isBeforeTestInTestsFile(testsFileNAME);
        boolean isAfterTest = isAfterTestInTestsFile(testsFileNAME);

        JSONObject dataSizeAndDataType= getDataTypeAndDataSize(dataSetName, testsFileName, testName.toString());
        String dataType =dataSizeAndDataType.get(dataTypeText).toString();


        for (String browser : config.getBrowsers()) {
            JSONObject completestTestObject = new JSONObject();
            completestTestObject.put("testName", testName);
            completestTestObject.put("testsFileName", testsFileName);
            completestTestObject.put("browser", browser.toLowerCase());
            completestTestObject.put(suiteNameTest, suiteName);
            completestTestObject.put(tagNameText, tagName);
            if(isBeforeTest){completestTestObject.put("BeforeTest", true);}
            if(isAfterTest){completestTestObject.put("afterTest", true);}
            try {
                if (dataType.equalsIgnoreCase("global")) {
                    completestTestObject.put(dataTypeText, dataType);
                    completestTestObject.put("dataSetName", dataSetName.replace(" ", "").split(":")[1]);
                }
            } catch (Exception e) {log.error("");}

            completeTestObjectArray.add(completestTestObject);
        }

        return completeTestObjectArray;
    }

    /**
     *
     * @param dataSetName
     * @param testsFileName
     * @param testName
     * @return
     */
    public JSONObject getDataTypeAndDataSize(String dataSetName, String testsFileName, String testName){
        String dataType = "";
        int dataSize = 0;

        if (dataSetName != null) {
            List<String> columnNameList = new LinkedList<>();
            columnNameList = dataDrivenParser.getColumnNameFromTest(testsFileParser.getTestStepByTestsFileandTestCaseName(testsFileName, testName));
            if (columnNameList.isEmpty()) {
                throw new NullPointerException("Data set value is not use on 'Test: " + testName + "' steps");
            }
            dataType = dataDrivenParser.checkDataTypeIsExcelOrGlobleInDataset(dataSetName.replace(" ", "").split(":")[1], columnNameList);
            if (dataType.equalsIgnoreCase("excel")) {
                dataSize = dataDrivenParser.getHeaderValuefromExcel(dataDrivenParser.getExcelUrl(dataSetName.replace(" ", "").split(":")[1]), columnNameList,Integer.parseInt(dataDrivenParser.sheetNumber(testsFileName, testName))).size();
            }
            else if(dataType.equalsIgnoreCase("list")){
                dataSize=dataDrivenParser.getDataSetListSize(dataSetName.replace(" ", "").split(":")[1]);
            }
        }
        JSONObject dataSizeAndDataType=new JSONObject();
        dataSizeAndDataType.put(dataTypeText,dataType);
        dataSizeAndDataType.put(dataSizeText,dataSize);

        return dataSizeAndDataType;

    }

    /**
     *
     * @param isBeforeTest
     * @param isAfterTest
     * @param testsFileName
     */
    public void ifIsBeforeAndAfterInTestsFileThenAnnotationDataSetByTestsFile(boolean isBeforeTest, boolean isAfterTest, String testsFileName){
        if(isBeforeTest || isAfterTest){
            testsFileParser.getAnnotationDataSetByTestsFile(testsFileName);
        }
    }

    /**
     *
     * @param testsFileNAME
     * @return
     */
    public boolean isBeforeTestInTestsFile(String testsFileNAME){
        return testsFileParser.isBeforeTestInTestsFile(testsFileNAME);
    }

    /**
     *
     * @param testsFileNAME
     * @return
     */
    public boolean isAfterTestInTestsFile(String testsFileNAME){
        return testsFileParser.isAfterTestInTestsFile(testsFileNAME);
    }

    /**
     *
     * @param isSuite
     * @param suiteTestNumber
     * @param testsFileName
     * @param testNameWithTestsFileName
     * @return
     */
    public String getTestsFileName(boolean isSuite,int suiteTestNumber,String testsFileName, JSONObject testNameWithTestsFileName){
        if(isSuite){
            Set testsFileNameForSuite=testNameWithTestsFileName.keySet();
            for(Object test:testsFileNameForSuite){
                if(test.toString().endsWith("_"+suiteTestNumber)){
                    testsFileName= test.toString();
                    break;
                }
            }
        }
        String testsFileNAME;
        if(isSuite){
            testsFileNAME=testsFileName.substring(0, testsFileName.length() - (String.valueOf(suiteTestNumber).length()+1));
        }else
        {
            testsFileNAME=testsFileName;
        }
        return testsFileNAME;
    }

    /**
     *
     * @param isSuite
     * @param isTag
     * @param suite
     * @return
     */
    public JSONObject getTestNameWithTestsFileName(boolean isSuite, boolean isTag,String suite){
        JSONObject testNameWithTestsFileName = null;
        String suiteName="";
        String tagName="";
        if (isSuite) {
            suiteName=suite;
            testNameWithTestsFileName = suiteParser.getTestsFileNameUsingTestName(suite);
        }
        if (isTag) {
            tagName=suite;
            testNameWithTestsFileName = testsFileParser.getTestNameByTag(suite);
        }
        JSONObject testNameWithTestsFile=new JSONObject();
        testNameWithTestsFile.put(suiteNameTest,suiteName);
        testNameWithTestsFile.put(tagNameText,tagName);
        testNameWithTestsFile.put("testNameWithTestsFileName",testNameWithTestsFileName);

        return testNameWithTestsFile;
    }

    /**
     *
     * @param testQueue
     */
    public void failTestExecutionQueue(JSONObject testQueue) {
        if(failTestQueue.isEmpty()) {
            failTestQueue.add(testQueue);
        }
        else{
            boolean flag=false;
            for(Object test:failTestQueue){
                if(test.equals(testQueue)){
                    flag=true;
                }
            }
            if(!flag){
                failTestQueue.add(testQueue);
            }
        }
    }

    /**
     *
     * @param failTestQueue
     */
    public void lastBuildFailTestExecutionQueue(JSONArray failTestQueue) {
        ReportLibraryFiles reportLibraryFiles=new ReportLibraryFiles();
        reportLibraryFiles.generateDir("./htmlReport/Past Fail Test Details");
        reportLibraryFiles.deleteFile(lastBuildFailTestURL);
        reportLibraryFiles.generatefile(lastBuildFailTestURL);
        StringBuilder currentBuildResult = new StringBuilder();
        currentBuildResult.append(failTestQueue);
        reportBuilder.writeReportFile(lastBuildFailTestURL,currentBuildResult);

    }

}