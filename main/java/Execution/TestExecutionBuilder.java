package Execution;

import DataCollector.BuildReportDataObject;
import ReportBuilder.GetJsonData;
import ReportBuilder.ReportBuilder;
import ReportBuilder.ReportLibraryFiles;
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
    DataDrivenParser dataDrivenParser = new DataDrivenParser();
    Logger logger = new Logger();

    public void startExecution() throws Exception {

        Logger logger = new Logger();
        logger.titleLog("-----------------------------------------------------------------------");
        logger.titleLog("Build execution Started");
        logger.titleLog("-----------------------------------------------------------------------");

        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportBuilder reportBuilder = new ReportBuilder();
        ReportParser report = new ReportParser();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy|MM|dd HH:mm:ss");
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

        reportBuilder.generatReport();
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

    public void parallelBuilder(JSONArray testExecutionQueue) throws Exception {
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
//        validation.beforeExecutionValidation();
        int tagSuiteCount = 0;

        /**
         * @Discription : Run by tag method.
         */
        try {

            parallelBuilder(buildExecutionQueue());

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

        System.out.println(suiteOrTaglist + "" + isTag);

        try {
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
                                    e.printStackTrace();
                                }
                                completeTestObjectArray.add(completestTestObject);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return completeTestObjectArray;
    }


}