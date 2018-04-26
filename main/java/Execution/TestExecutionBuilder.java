package Execution;

import ReportBuilder.GetJsonData;
import ReportBuilder.*;
import framework.*;
import logger.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestExecutionBuilder {

    public static JSONObject mainObj = new JSONObject();
    public static JSONObject reportObj = new JSONObject();
    DataDrivenParser dataDrivenParser =new DataDrivenParser();
    Logger logger = new Logger();



    public  void startExecution() throws Exception {

        Logger logger = new Logger();
        logger.titleLog("-----------------------------------------------------------------------");
        logger.titleLog("Build execution Started");
        logger.titleLog("-----------------------------------------------------------------------");

        TestExecutionBuilder builder = new TestExecutionBuilder();
        ReportBuilder reportBuilder = new ReportBuilder();
        ReportParser report = new ReportParser();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy|MM|dd HH:mm:ss");
        long startTimeSuite = System.currentTimeMillis();
        JSONArray suits =new JSONArray();


        builder.reportObj.put("suits",suits);
        builder.reportObj.put("startTime", dtf.format(LocalDateTime.now()));

        /*Build Running start*/
        builder.buildExecution();

        long stopTimeSuite = System.currentTimeMillis();
        builder.reportObj.put("endTime", dtf.format(LocalDateTime.now()));


        long elapsedTimeSuite = stopTimeSuite - startTimeSuite;
        builder.reportObj.put("totalTimeTaken", elapsedTimeSuite);

        /*Report Generation*/

        report.generateReportDir();
        report.writeJsonFile(builder.reportObj, builder.getbuildReportName());
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
        Validation validation=new Validation();
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
        Validation validation=new Validation();
        GetConfiguration config = new GetConfiguration();
        ReportParser report = new ReportParser();
        validation.beforeExecutionValidation();
        int tagSuiteCount = 0;

        /**
         * @Discription : Run by tag method.
         */
        try {
            ArrayList<String> taglist = config.getTags();
            if (taglist.isEmpty() || taglist.get(0).equals("")) {
                logger.errorLog("Please enter 'Tag' name.");
            } else {
                parallelBuilder(buildExecutionQueueByTag());
                report.newReport(mainObj, reportObj);
            }
        } catch (NullPointerException ne) {
            tagSuiteCount++;
        }

        /**
         *@Discription : Run by suit Name.
         */
        try {
            ArrayList<String> suitelist = config.getSuite();
            if (suitelist.isEmpty() || suitelist.get(0).equals("")) {
                logger.errorLog("Please enter 'suite' name.");
            } else {
                parallelBuilder(buildExecutionQueueBySuite());
                report.newReport(mainObj, reportObj);
            }
        } catch (NullPointerException ne) {
            tagSuiteCount++;
        }

        if (tagSuiteCount == 2) {
            //logger.errorLog("Please enter 'Tag name' or 'Suite name' to run test.");
        }
    }

    /**
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     * @return
     * @throws Exception
     */
    public JSONArray buildExecutionQueueBySuite() throws Exception {
        SuiteParser suiteParser = new SuiteParser();
        GetConfiguration config = new GetConfiguration();
        JSONArray completeTestObjectArray = new JSONArray();
        try{
            for (String suite : config.getSuite()) {
                JSONObject testNameWithSuites = suiteParser.getTestNameBySuite(suite);
                for (Object suiteName : testNameWithSuites.keySet()) {
                    boolean isDataSetInSuite=false;
                    if(dataDrivenParser.isDataSet(suiteName.toString())) {
                        isDataSetInSuite= dataDrivenParser.isExcel(suiteName.toString());
                    }

                    for (Object testName : ((JSONArray) testNameWithSuites.get(suiteName))) {
                        String dataSetName=null;
                        int dataSize=0;
                        for (String browser : config.getBrowsers()) {
                            if(isDataSetInSuite){
                                dataSetName=suiteParser.getTestDataSetBySuiteAndTestCaseName(suiteName.toString(),testName.toString());
                                if(dataSetName!=null) {
                                    if(dataDrivenParser.dataSetIsExistInSuite(suiteName.toString(),dataSetName.replace(" ","").split(":")[1])) {
                                        ArrayList<String> columnNameList=new ArrayList<String>();
                                        columnNameList= dataDrivenParser.getColumnNameFromTest(suiteParser.getTestStepBySuiteandTestCaseName(suiteName.toString(),testName.toString()));
                                        if (columnNameList.size() == 0) {
                                            throw new NullPointerException("Data set value is not use on 'Test: "+ testName +"' steps");
                                        }
                                        dataSize= dataDrivenParser.getHeaderValuefromExcel(dataDrivenParser.getExcelUrl(suiteName.toString(),dataSetName.replace(" ","").split(":")[1]),columnNameList).size();
                                    }
                                }
                            }
                            if(dataSize!=0){
                                for(int i=1;i<=dataSize;i++) {
                                    JSONObject completestTestObject = new JSONObject();
                                    completestTestObject.put("testName", testName);
                                    completestTestObject.put("suiteName", suiteName);
                                    completestTestObject.put("browser", browser);
                                    completestTestObject.put("row", i);
                                    completestTestObject.put("dataSetName", dataSetName.replace(" ","").split(":")[1]);
                                    completeTestObjectArray.add(completestTestObject);
                                }
                            }else {
                                JSONObject completestTestObject = new JSONObject();
                                completestTestObject.put("testName", testName);
                                completestTestObject.put("suiteName", suiteName);
                                completestTestObject.put("browser", browser);
                                completeTestObjectArray.add(completestTestObject);
                            }
                        }
                    }
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return completeTestObjectArray;
    }

    /**
     * @auther :
     * @lastModifiedBy: Ankit Mistry
     * @return
     * @throws Exception
     */
    public JSONArray buildExecutionQueueByTag() throws Exception {
        SuiteParser suiteParser = new SuiteParser();
        GetConfiguration config = new GetConfiguration();
        JSONArray completeTestObjectArray = new JSONArray();
        try {
            for (String tag : config.getTags()) {
                JSONObject testNameWithSuites = suiteParser.getTestNameByTag(tag);
                for (Object suiteName : testNameWithSuites.keySet()) {
                    boolean isDataSetInSuite=false;
                    if(dataDrivenParser.isDataSet(suiteName.toString())) {
                        isDataSetInSuite= dataDrivenParser.isExcel(suiteName.toString());
                    }
                    for (Object testName : ((JSONArray) testNameWithSuites.get(suiteName))) {
                        String dataSetName=null;
                        int dataSize=0;
                        boolean data=false;
                        for (String browser : config.getBrowsers()) {
                            if(isDataSetInSuite){
                                dataSetName=suiteParser.getTestDataSetBySuiteAndTestCaseName(suiteName.toString(),testName.toString());
                                if(dataSetName!=null) {
                                    if(dataDrivenParser.dataSetIsExistInSuite(suiteName.toString(),dataSetName.replace(" ","").split(":")[1])) {
                                        ArrayList<String> columnNameList=new ArrayList<String>();
                                        columnNameList = dataDrivenParser.getColumnNameFromTest(suiteParser.getTestStepBySuiteandTestCaseName(suiteName.toString(), testName.toString()));
                                        dataSize= dataDrivenParser.getHeaderValuefromExcel(dataDrivenParser.getExcelUrl(suiteName.toString(),dataSetName.replace(" ","").split(":")[1]),columnNameList).size();
                                        if (columnNameList.size() == 0) {
                                            throw new NullPointerException("Data set value is not use on 'Test: "+ testName +"' steps");
                                        }

                                    }
                                }
                            }
                            if(dataSize!=0){
                                for(int i=1;i<=dataSize;i++) {
                                    JSONObject completestTestObject = new JSONObject();
                                    completestTestObject.put("testName", testName);
                                    completestTestObject.put("tag", tag);
                                    completestTestObject.put("suiteName", suiteName);
                                    completestTestObject.put("browser", browser);
                                    completestTestObject.put("row", i);
                                    completestTestObject.put("dataSetName", dataSetName.replace(" ","").split(":")[1]);
                                    completeTestObjectArray.add(completestTestObject);
                                }
                            }else {
                                JSONObject completestTestObject = new JSONObject();
                                completestTestObject.put("testName", testName);
                                completestTestObject.put("tag", tag);
                                completestTestObject.put("suiteName", suiteName);
                                completestTestObject.put("browser", browser);
                                completeTestObjectArray.add(completestTestObject);
                            }
                        }

                    }
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return completeTestObjectArray;
    }
}