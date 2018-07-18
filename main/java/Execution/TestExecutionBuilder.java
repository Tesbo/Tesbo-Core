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

import java.io.File;
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
    public static boolean repotFileGenerated = false;
    DataDrivenParser dataDrivenParser = new DataDrivenParser();
    Logger logger = new Logger();
    public static JSONArray failTestQueue=new JSONArray();

    public void startExecution() throws Exception {
        Commands cmd=new Commands();
        buildStartTime = System.currentTimeMillis();

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
        BuildReportDataObject brdo = new BuildReportDataObject();
        brdo.startThread();
      //  reportBuilder.startThread();


        builder.buildExecution();


        TestExecutionBuilder.buildRunning = false;
        buildEndTime = System.currentTimeMillis();

        BuildReportDataObject.mainReportObject.put("endTime", TestExecutionBuilder.buildEndTime);
        BuildReportDataObject.mainReportObject.put("totalTimeTaken", (TestExecutionBuilder.buildEndTime - TestExecutionBuilder.buildStartTime));

        logger.titleLog("-----------------------------------------------------------------------");
        logger.titleLog("Build Execution Completed");
        logger.titleLog("-----------------------------------------------------------------------\n");


        while(!repotFileGenerated) {
            cmd.pause(3);
            reportBuilder.generatReport();
        }



        /*logger.customeLog("| Total : " + data.getCurrentBuildTotal(new File(getBuildHistoryPath()).getAbsolutePath()), Ansi.FColor.NONE);
        logger.customeLog(" | Passed : " + data.getCurrentBuildPassed(buildHistory), Ansi.FColor.NONE);
        logger.customeLog(" | Failed : " + data.getCurrentBuildFailed(buildHistory), Ansi.FColor.NONE);
        logger.customeLog(" | Time : " + data.getCurrentBuildTotalTime(buildHistory) + " |\n", Ansi.FColor.NONE);


        logger.customeLog("\nReport Generated at :" + file.getAbsolutePath() + "\n", Ansi.FColor.NONE);
        logger.titleLog("-----------------------------------------------------------------------");
*/
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

            e.printStackTrace();
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
            parallelBuilder(buildExecutionQueue());
            int failTestQueuesize= failTestQueue.size();
            if(failTestQueuesize>0){
                int retryAnalyserCount=Integer.parseInt(config.getRetryAnalyser());
                if(retryAnalyserCount>0){

                    JSONArray TestQueue=failTestQueue;
                    for(int i=1;i<=retryAnalyserCount;i++) {
                        parallelBuilder(TestQueue);
                        if(failTestQueuesize!=failTestQueue.size()){
                            TestQueue=failTestQueue;
                            failTestQueuesize=failTestQueue.size();
                        }
                    }
                }
            }

        } catch (Exception ne) {
            ne.printStackTrace();

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
                    boolean isDataSetInSuite = false;
                    if (dataDrivenParser.isDataSet(suiteName.toString())) {
                        isDataSetInSuite = dataDrivenParser.isExcel(suiteName.toString());
                    }

                    for (Object testName : ((JSONArray) testNameWithSuites.get(suiteName))) {
                        String dataSetName = null;
                        int dataSize = 0;
                        String dataType = null;
                        if (isDataSetInSuite) {
                            dataSetName = suiteParser.getTestDataSetBySuiteAndTestCaseName(suiteName.toString(), testName.toString());
                            if (dataSetName != null) {
                                ArrayList<String> columnNameList = new ArrayList<String>();
                                columnNameList = dataDrivenParser.getColumnNameFromTest(suiteParser.getTestStepBySuiteandTestCaseName(suiteName.toString(), testName.toString()));
                                if (columnNameList.size() == 0) {
                                    throw new NullPointerException("Data set value is not use on 'Test: " + testName + "' steps");
                                }

                                dataType = dataDrivenParser.checkDataTypeIsExcelOrGlobleInDataset(suiteName.toString(), dataSetName.replace(" ", "").split(":")[1], columnNameList);

                                if (dataType.equalsIgnoreCase("excel")) {

                                    dataSize = dataDrivenParser.getHeaderValuefromExcel(dataDrivenParser.getExcelUrl(suiteName.toString(), dataSetName.replace(" ", "").split(":")[1]), columnNameList).size();

                                }
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
                                    completeTestObjectArray.add(completestTestObject);
                                }
                            }
                        } else {
                            for (String browser : config.getBrowsers()) {
                                JSONObject completestTestObject = new JSONObject();
                                completestTestObject.put("testName", testName);
                                completestTestObject.put("suiteName", suiteName);
                                completestTestObject.put("browser", browser);
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


}