package Execution;

import DataCollector.BuildReportDataObject;
import ReportBuilder.GetJsonData;
import ReportBuilder.ReportBuilder;
import ReportBuilder.ReportLibraryFiles;
import Selenium.Commands;
import com.diogonunes.jcdp.color.api.Ansi;
import framework.*;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import reportAPI.ReportAPIConfig;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    static GetConfiguration config=new GetConfiguration();
    public static boolean singleWindowRun= config.getSingleWindowRun();
    public static boolean isSingleWindow= config.getSingleWindowRun();
    public static WebDriver driver;
    GetJsonData data = new GetJsonData();
    ReportBuilder reportBuilder=new ReportBuilder();
    public static JSONArray failTestQueue=new JSONArray();
    String buildHistory = new File(reportBuilder.getBuildHistoryPath()).getAbsolutePath();
    private static final Logger log = LogManager.getLogger(Tesbo.class);

    public void startExecution(String[] argumentsArray) throws Exception {
        Commands cmd=new Commands();
        buildStartTime = System.currentTimeMillis();
        GetConfiguration getConfig = new GetConfiguration();
        Validation validation = new Validation();
        SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();
        setCommandLineArgument.setArgument(argumentsArray);
        TesboLogger logger = new TesboLogger();
        log.info("-----------------------------------------------------------------------");
        log.info("Build execution Started");
        log.info("-----------------------------------------------------------------------");

        logger.titleLog("-----------------------------------------------------------------------");
        logger.titleLog("Build execution Started");
        logger.titleLog("-----------------------------------------------------------------------");

        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportBuilder reportBuilder = new ReportBuilder();
        ReportParser report = new ReportParser();
        log.info("Generate Report Directory");
        report.generateReportDir();
        buildReportName = builder.getbuildReportName();
        log.info("Get build report name: "+buildReportName);
        ReportLibraryFiles files = new ReportLibraryFiles();
        files.createLibrary();

        TestExecutionBuilder.buildRunning = true;
        ReportAPIConfig config = new ReportAPIConfig();
        log.info("Verify config file path validation");
        validation.configFilePathValidation();
        if(getConfig.getIsCloudIntegration() ) {
            log.info("Get cloud integration build key");
            config.getBuildKey();
        }

        BuildReportDataObject brdo = new BuildReportDataObject();
        brdo.startThread();
        reportBuilder.startThread();

        if(setCommandLineArgument.Environment!=null) {
            log.info("Environment Name: "+setCommandLineArgument.Environment);
            if (setCommandLineArgument.Environment.equalsIgnoreCase("all")) {
                for (Object env : getConfig.getEnvironmentList().keySet()) {
                    failTest=0;
                    setCommandLineArgument.Environment = env.toString();
                    log.info("Start build execution with "+setCommandLineArgument.Environment+" environment");
                    builder.buildExecution();
                }
            } else {
                log.info("Start build execution with "+setCommandLineArgument.Environment+" environment");
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
        logger.titleLog("-----------------------------------------------------------------------");
        logger.titleLog("Build Execution Completed");
        logger.titleLog("-----------------------------------------------------------------------\n");

        if(getConfig.getIsCloudIntegration()) {
            config.updateEndTime();
        }

        while(!repotFileGenerated) {
            cmd.pause(3);
            log.info("Start to generate report");
            reportBuilder.generatReport();
            log.info("Report is generated");
        }
        log.info("***********************************************************************************************************************");

        logger.customeLog("| Total : " + data.getCurrentBuildTotal(new File(reportBuilder.getBuildHistoryPath()).getAbsolutePath()), Ansi.FColor.NONE);
        logger.customeLog(" | Passed : " + data.getCurrentBuildPassed(buildHistory), Ansi.FColor.NONE);
        logger.customeLog(" | Failed : " + data.getCurrentBuildFailed(buildHistory), Ansi.FColor.NONE);
        logger.customeLog(" | Time : " + data.getCurrentBuildTotalTime(buildHistory) + " |\n", Ansi.FColor.NONE);

        log.info("Total Run Test: " + data.getCurrentBuildTotal(new File(reportBuilder.getBuildHistoryPath()).getAbsolutePath()), Ansi.FColor.NONE);
        log.info("Total Passed Test: " + data.getCurrentBuildPassed(buildHistory), Ansi.FColor.NONE);
        log.info("Total Failed Test: " + data.getCurrentBuildFailed(buildHistory), Ansi.FColor.NONE);
        log.info("Total Time To Run Test: " + data.getCurrentBuildTotalTime(buildHistory) + " |\n", Ansi.FColor.NONE);


        //logger.customeLog("\nReport Generated at :" + file.getAbsolutePath() + "\n", Ansi.FColor.NONE);
        logger.titleLog("-----------------------------------------------------------------------");
        log.info("*********************************************** Build Execution Completed ***********************************************");

        String reportFileName=getConfig.getReportFileName();
        if(reportFileName.equals("")){
            reportFileName="currentBuildResult";
        }
        
        reportBuilder.copyReport(reportFileName);
    
    }


    public String getbuildReportName() {
        String newName;
        GetJsonData data = new GetJsonData();
        File buildHistory = new File("./htmlReport/Build History");
        JSONArray testsFileList = new JSONArray();
        try (Stream<Path> paths = Files.walk(Paths.get(buildHistory.getAbsolutePath()))) {
            testsFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
        }

        if (testsFileList.size() > 0) {
            File lastModifiedfile = data.getLastModifiedJsonFile(buildHistory.getAbsolutePath());
            String getFile = lastModifiedfile.getName();
            String getCount[] = getFile.split("_")[1].split(".json");
            newName = "buildResult_" + (Integer.parseInt(getCount[0]) + 1);

        } else {
            newName = "buildResult_1";
        }

        return newName;
    }

    public void parallelBuilder(JSONArray testExecutionQueue) {
        Validation validation = new Validation();
        GetConfiguration config = new GetConfiguration();
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

    public void buildExecution() throws Exception {
        Validation validation = new Validation();
        GetConfiguration config = new GetConfiguration();

        /**
         * @Discription : Run by tag method.
         */
        try {
            log.info("Start verify necessary validation before start execution");
            validation.beforeExecutionValidation();
            File file = new File("./htmlReport/Past Fail Test Details/LastBuildFailTest.json");
            boolean runPastFailure=false;
            JSONArray lastBuildFailExecutionQueue = null;
            if (config.getRunPastFailure() && file.exists()) {
                Utility parser = new Utility();
                log.info("Get last build fail execution queue details");
                lastBuildFailExecutionQueue = parser.loadJsonArrayFile(file.getAbsolutePath());
                if (!lastBuildFailExecutionQueue.equals(null)) {
                    runPastFailure=true;
                }
            }
            if(runPastFailure){
                log.info("Run only that test whose going to fail in last build execution");
                parallelBuilder(lastBuildFailExecutionQueue);
            }else {
                log.info("Start run parallel execution builder");
                parallelBuilder(buildExecutionQueue());
                int failTestQueuesize = failTestQueue.size();
                log.info("Create fail test queue list");
                lastBuildFailTestExecutionQueue(failTestQueue);
                log.info("Number of fail test is "+failTestQueuesize);
                if (failTestQueuesize > 0) {
                    failTest=0;
                    int retryAnalyserCount = Integer.parseInt(config.getRetryAnalyser());
                    log.info("Number retry analyser is "+retryAnalyserCount);
                    if (retryAnalyserCount > 0) {

                        JSONArray TestQueue = failTestQueue;
                        for (int i = 1; i <= retryAnalyserCount; i++) {
                            failTest++;
                            log.info("Run retry analyser: "+i);
                            parallelBuilder(TestQueue);
                            if (failTestQueuesize != failTestQueue.size()) {
                                TestQueue = failTestQueue;
                                failTestQueuesize = failTestQueue.size();
                            }
                        }
                    }
                }
            }
        } catch (Exception ne) {
            StringWriter sw = new StringWriter();
            ne.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
            log.error(sw.toString());
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
        TestsFileParser testsFileParser = new TestsFileParser();
        SuiteParser suiteParser=new SuiteParser();
        GetConfiguration config = new GetConfiguration();
        JSONArray completeTestObjectArray = new JSONArray();

        ArrayList<String> suiteOrTaglist = null;
        boolean isTag = false;
        boolean isSuite = false;

        if (config.getRunBy().equals("tag")) {
            suiteOrTaglist = config.getTags();
            log.info("Test is run by tag");
            log.info("Tag List: "+suiteOrTaglist);
            isTag = true;
        }

        if (config.getRunBy().equals("suite")) {
            suiteOrTaglist = config.getSuite();
            log.info("Test is run by suite");
            log.info("Suite List: "+suiteOrTaglist);
            isSuite = true;
        }

        for (String suite : suiteOrTaglist) {

            JSONObject testNameWithTestsFileName = null;
            String suiteName="";
            String tagName="";
            int suiteTestNumber=1;
            if (isSuite) {
                suiteName=suite;
                testNameWithTestsFileName = suiteParser.getTestsFileNameUsingTestName(suite);
            }
            if (isTag) {
                tagName=suite;
                testNameWithTestsFileName = testsFileParser.getTestNameByTag(suite);
            }

            for (Object testsFileName : testNameWithTestsFileName.keySet()) {
                if(isSuite){
                    Set testsFileNameForSuite=testNameWithTestsFileName.keySet();
                    for(Object test:testsFileNameForSuite){
                        if(test.toString().endsWith("_"+suiteTestNumber)){
                            testsFileName=test;
                            break;
                        }
                    }
                }
                boolean isBeforeTest = false;
                boolean isAfterTest = false;
                String testsFileNAME;
                if(isSuite){
                    testsFileNAME=testsFileName.toString().substring(0, testsFileName.toString().length() - (String.valueOf(suiteTestNumber).length()+1));
                }else
                {
                    testsFileNAME=testsFileName.toString();
                }

                if(testsFileParser.isBeforeTestInTestsFile(testsFileNAME)){
                    isBeforeTest=true;
                }
                if(testsFileParser.isAfterTestInTestsFile(testsFileNAME)){
                    isAfterTest=true;
                }

                for (Object testName : ((JSONArray) testNameWithTestsFileName.get(testsFileName))) {
                    if(isSuite){
                        testsFileName=testsFileName.toString().substring(0, testsFileName.toString().length() - (String.valueOf(suiteTestNumber).length()+1));
                        suiteTestNumber++;
                    }
                    String dataSetName = null;
                    int dataSize = 0;
                    String dataType = null;
                    if(isBeforeTest || isAfterTest){
                        testsFileParser.getAnnotationDataSetByTestsFile(testsFileName.toString());
                    }
                    if(dataSetName==null) {
                        dataSetName = testsFileParser.getTestDataSetByTestsFileAndTestCaseName(testsFileName.toString(), testName.toString());
                    }
                    if (dataSetName != null) {
                        ArrayList<String> columnNameList = new ArrayList<String>();
                        if(isBeforeTest){
                           // columnNameList = dataDrivenParser.getColumnNameFromTest(suiteParser.getBeforeAndAfterTestStepByTestsFile(testsFileName.toString(),"BeforeTest"));
                        }
                        if(isAfterTest && columnNameList.size() == 0){
                            //columnNameList = dataDrivenParser.getColumnNameFromTest(suiteParser.getBeforeAndAfterTestStepByTestsFile(testsFileName.toString(),"AfterTest"));
                        }
                        if(columnNameList.size() == 0) {
                            columnNameList = dataDrivenParser.getColumnNameFromTest(testsFileParser.getTestStepByTestsFileandTestCaseName(testsFileName.toString(), testName.toString()));
                        }
                        if (columnNameList.size() == 0) {
                            throw new NullPointerException("Data set value is not use on 'Test: " + testName + "' steps");
                        }
                        dataType = dataDrivenParser.checkDataTypeIsExcelOrGlobleInDataset(dataSetName.replace(" ", "").split(":")[1], columnNameList);
                        if (dataType.equalsIgnoreCase("excel")) {
                            dataSize = dataDrivenParser.getHeaderValuefromExcel(dataDrivenParser.getExcelUrl(dataSetName.replace(" ", "").split(":")[1]), columnNameList,Integer.parseInt(dataDrivenParser.SheetNumber(testsFileName.toString(), testName.toString()))).size();
                        }
                        else if(dataType.equalsIgnoreCase("list")){
                            dataSize=dataDrivenParser.getDataSetListSize(dataSetName.replace(" ", "").split(":")[1]);
                        }
                    }
                    if (dataSize != 0) {
                        for (int i = 1; i <= dataSize; i++) {
                            for (String browser : config.getBrowsers()) {
                                JSONObject completestTestObject = new JSONObject();
                                completestTestObject.put("testName", testName);
                                completestTestObject.put("testsFileName", testsFileName);
                                completestTestObject.put("browser", browser);
                                completestTestObject.put("dataType", dataType);
                                completestTestObject.put("row", i);
                                completestTestObject.put("suiteName", suiteName);
                                completestTestObject.put("tagName", tagName);
                                completestTestObject.put("dataSetName", dataSetName.replace(" ", "").split(":")[1]);
                                if(isBeforeTest){completestTestObject.put("BeforeTest", true);}
                                if(isAfterTest){completestTestObject.put("afterTest", true);}
                                completeTestObjectArray.add(completestTestObject);
                            }
                        }
                    } else {
                        for (String browser : config.getBrowsers()) {
                            JSONObject completestTestObject = new JSONObject();
                            completestTestObject.put("testName", testName);
                            completestTestObject.put("testsFileName", testsFileName);
                            completestTestObject.put("browser", browser);
                            completestTestObject.put("suiteName", suiteName);
                            completestTestObject.put("tagName", tagName);
                            if(isBeforeTest){completestTestObject.put("BeforeTest", true);}
                            if(isAfterTest){completestTestObject.put("afterTest", true);}
                            try {
                                if (dataType.equalsIgnoreCase("global")) {
                                    completestTestObject.put("dataType", dataType);
                                    completestTestObject.put("dataSetName", dataSetName.replace(" ", "").split(":")[1]);
                                }
                            } catch (Exception e) {
                            }

                            completeTestObjectArray.add(completestTestObject);
                        }
                    }
                }
            }
        }
        return completeTestObjectArray;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param testQueue
     */
    public void failTestExecutionQueue(JSONObject testQueue) {
        if(failTestQueue.size()==0) {
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
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param failTestQueue
     */
    public void lastBuildFailTestExecutionQueue(JSONArray failTestQueue) {
        ReportLibraryFiles reportLibraryFiles=new ReportLibraryFiles();
        ReportBuilder reportBuilder=new ReportBuilder();
        reportLibraryFiles.generateDir("./htmlReport/Past Fail Test Details");
        reportLibraryFiles.deleteFile("./htmlReport/Past Fail Test Details/LastBuildFailTest.json");
        reportLibraryFiles.generatefile("./htmlReport/Past Fail Test Details/LastBuildFailTest.json");
        StringBuffer currentBuildResult = new StringBuffer();
        currentBuildResult.append(failTestQueue);
        reportBuilder.writeReportFile("./htmlReport/Past Fail Test Details/LastBuildFailTest.json",currentBuildResult);

    }

}