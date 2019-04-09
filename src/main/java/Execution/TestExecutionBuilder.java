package Execution;

import DataCollector.BuildReportDataObject;
import ReportBuilder.GetJsonData;
import ReportBuilder.ReportBuilder;
import ReportBuilder.ReportLibraryFiles;
import Selenium.Commands;
import com.diogonunes.jcdp.color.api.Ansi;
import framework.*;
import logger.Logger;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    DataDrivenParser dataDrivenParser = new DataDrivenParser();
    Logger logger = new Logger();

    static GetConfiguration config=new GetConfiguration();
    public static boolean singleWindowRun= config.getSingleWindowRun();
    public static boolean isSingleWindow= config.getSingleWindowRun();
    public static WebDriver driver;
    GetJsonData data = new GetJsonData();
    ReportBuilder reportBuilder=new ReportBuilder();
    public static JSONArray failTestQueue=new JSONArray();
    String buildHistory = new File(reportBuilder.getBuildHistoryPath()).getAbsolutePath();

    public void startExecution(String[] argumentsArray) throws Exception {
        Commands cmd=new Commands();
        buildStartTime = System.currentTimeMillis();
        GetConfiguration getConfig = new GetConfiguration();
        Validation validation = new Validation();
        SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();
        setCommandLineArgument.setArgument(argumentsArray);
        Logger logger = new Logger();
        logger.titleLog("-----------------------------------------------------------------------");
        logger.titleLog("Build execution Started");
        logger.titleLog("-----------------------------------------------------------------------");

        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportBuilder reportBuilder = new ReportBuilder();
        ReportParser report = new ReportParser();
        report.generateReportDir();
        buildReportName = builder.getbuildReportName();
        ReportLibraryFiles files = new ReportLibraryFiles();
        files.createLibrary();

        TestExecutionBuilder.buildRunning = true;

        ReportAPIConfig config = new ReportAPIConfig();
        validation.configFilePathValidation();
        if(getConfig.getIsCloudIntegration() ) {
            config.getBuildKey();
        }

        BuildReportDataObject brdo = new BuildReportDataObject();
        brdo.startThread();
        //  reportBuilder.startThread();


        builder.buildExecution();

        if(! isSingleWindow && singleWindowRun) {
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
            reportBuilder.generatReport();
        }

        logger.customeLog("| Total : " + data.getCurrentBuildTotal(new File(reportBuilder.getBuildHistoryPath()).getAbsolutePath()), Ansi.FColor.NONE);
        logger.customeLog(" | Passed : " + data.getCurrentBuildPassed(buildHistory), Ansi.FColor.NONE);
        logger.customeLog(" | Failed : " + data.getCurrentBuildFailed(buildHistory), Ansi.FColor.NONE);
        logger.customeLog(" | Time : " + data.getCurrentBuildTotalTime(buildHistory) + " |\n", Ansi.FColor.NONE);


        //logger.customeLog("\nReport Generated at :" + file.getAbsolutePath() + "\n", Ansi.FColor.NONE);
        logger.titleLog("-----------------------------------------------------------------------");

    }


    public String getbuildReportName() {
        String newName;
        GetJsonData data = new GetJsonData();
        File buildHistory = new File("./htmlReport/Build History");
        JSONArray suiteFileList = new JSONArray();
        try (Stream<Path> paths = Files.walk(Paths.get(buildHistory.getAbsolutePath()))) {
            suiteFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
        }

        if (suiteFileList.size() > 0) {
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
        ReportParser report = new ReportParser();

        int tagSuiteCount = 0;

        /**
         * @Discription : Run by tag method.
         */
        try {
            validation.beforeExecutionValidation();
            File file = new File("./htmlReport/Past Fail Test Details/LastBuildFailTest.json");
            boolean runPastFailure=false;
            JSONArray lastBuildFailExecutionQueue = null;
            if (config.getRunPastFailure() && file.exists()) {
                Utility parser = new Utility();
                lastBuildFailExecutionQueue = parser.loadJsonArrayFile(file.getAbsolutePath());
                if (!lastBuildFailExecutionQueue.equals(null)) {
                    runPastFailure=true;
                }
            }
            if(runPastFailure){
                parallelBuilder(lastBuildFailExecutionQueue);
            }else {
                parallelBuilder(buildExecutionQueue());
                int failTestQueuesize = failTestQueue.size();
                lastBuildFailTestExecutionQueue(failTestQueue);
                if (failTestQueuesize > 0) {
                    failTest=0;
                    int retryAnalyserCount = Integer.parseInt(config.getRetryAnalyser());
                    if (retryAnalyserCount > 0) {

                        JSONArray TestQueue = failTestQueue;
                        for (int i = 1; i <= retryAnalyserCount; i++) {
                            failTest++;
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
        SuiteParser suiteParser = new SuiteParser();
        GetConfiguration config = new GetConfiguration();
        JSONArray completeTestObjectArray = new JSONArray();
        int tagSuiteCount = 0;

        ArrayList<String> suiteOrTaglist = null;
        boolean isTag = false;
        boolean isSuite = false;


        if (config.getRunBy().equals("tag")) {
            suiteOrTaglist = config.getTags();
            isTag = true;
        }

        if (config.getRunBy().equals("suite")) {
            suiteOrTaglist = config.getSuite();
            isSuite = true;
        }

        for (String suite : suiteOrTaglist) {

            JSONObject testNameWithSuites = null;
            if (isSuite) {
                testNameWithSuites = suiteParser.getTestNameBySuite(suite);
            }
            if (isTag) {
                testNameWithSuites = suiteParser.getTestNameByTag(suite);
            }

            for (Object suiteName : testNameWithSuites.keySet()) {
                boolean isBeforeTest = false;
                boolean isAfterTest = false;
                if(suiteParser.isBeforeTestInSuite(suiteName.toString())){
                    isBeforeTest=true;
                }
                if(suiteParser.isAfterTestInSuite(suiteName.toString())){
                    isAfterTest=true;
                }

                for (Object testName : ((JSONArray) testNameWithSuites.get(suiteName))) {
                    String dataSetName = null;
                    int dataSize = 0;
                    String dataType = null;
                    if(isBeforeTest || isAfterTest){
                        suiteParser.getAnnotationDataSetBySuite(suiteName.toString());
                    }
                    if(dataSetName==null) {
                        dataSetName = suiteParser.getTestDataSetBySuiteAndTestCaseName(suiteName.toString(), testName.toString());
                    }
                    if (dataSetName != null) {
                        ArrayList<String> columnNameList = new ArrayList<String>();
                        if(isBeforeTest){
                           // columnNameList = dataDrivenParser.getColumnNameFromTest(suiteParser.getBeforeAndAfterTestStepBySuite(suiteName.toString(),"BeforeTest"));
                        }
                        if(isAfterTest && columnNameList.size() == 0){
                            //columnNameList = dataDrivenParser.getColumnNameFromTest(suiteParser.getBeforeAndAfterTestStepBySuite(suiteName.toString(),"AfterTest"));
                        }
                        if(columnNameList.size() == 0) {
                            columnNameList = dataDrivenParser.getColumnNameFromTest(suiteParser.getTestStepBySuiteandTestCaseName(suiteName.toString(), testName.toString()));
                        }
                        if (columnNameList.size() == 0) {
                            throw new NullPointerException("Data set value is not use on 'Test: " + testName + "' steps");
                        }
                        dataType = dataDrivenParser.checkDataTypeIsExcelOrGlobleInDataset(suiteName.toString(), dataSetName.replace(" ", "").split(":")[1], columnNameList);

                        if (dataType.equalsIgnoreCase("excel")) {
                            dataSize = dataDrivenParser.getHeaderValuefromExcel(dataDrivenParser.getExcelUrl(suiteName.toString(), dataSetName.replace(" ", "").split(":")[1]), columnNameList,Integer.parseInt(dataDrivenParser.SheetNumber(suiteName.toString(), testName.toString()))).size();
                        }
                    }
                    if (dataSize != 0) {
                        for (int i = 1; i <= dataSize; i++) {
                            for (String browser : config.getBrowsers()) {
                                JSONObject completestTestObject = new JSONObject();
                                completestTestObject.put("testName", testName);
                                completestTestObject.put("suiteName", suiteName);
                                completestTestObject.put("browser", browser);
                                completestTestObject.put("dataType", dataType);
                                completestTestObject.put("row", i);
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
                            completestTestObject.put("suiteName", suiteName);
                            completestTestObject.put("browser", browser);
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