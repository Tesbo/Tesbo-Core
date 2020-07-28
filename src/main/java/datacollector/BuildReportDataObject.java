package datacollector;


import execution.TestExecutionBuilder;
import framework.ReportParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class BuildReportDataObject implements Runnable {

    private static final Logger log = LogManager.getLogger(BuildReportDataObject.class);

    String testsFileText="testsFile";
    String testsText="tests";
    String testNameText="testName";
    String statusText="status";
    String failedText="failed";
    String passedText="passed";
    String totalPassedText="totalPassed";
    String totalFailedText="totalFailed";
    String testsFileNameText="testsFileName";
    String totalTimeText="totalTime";
    StringWriter sw = new StringWriter();


    public static JSONObject mainReportObject = new JSONObject();
    static JSONArray browserArray = new JSONArray();
    static int buildTotalPassed = 0;
    static int buildTotalFailed = 0;


    public void startThread() {
        BuildReportDataObject brdo = new BuildReportDataObject();
        Thread t1 = new Thread(brdo);
        t1.start();
    }

    public void addDataInMainObject(String browser, String testFileName, JSONObject testResultObject) {
        JSONObject testFileObject = getBrowserObject(browser);
        boolean flag = false;
        if(testFileObject!=null) {
            flag=isTestResultIsPassOrFail(testFileObject,testResultObject);
        }
        if(!flag) {
            if (testResultObject.get(statusText).equals(passedText)) { buildTotalPassed++; }
            if (testResultObject.get(statusText).equals(failedText)) { buildTotalFailed++; }
        }
        checkForTheBrowser(browser);
        checkForTheTestsFile(browser, testFileName);
        checkForTheTest(browser, testFileName, testResultObject);

        mainReportObject.put("browser", browserArray);
        mainReportObject.put("startTime", TestExecutionBuilder.buildStartTime);
        mainReportObject.put(totalPassedText, buildTotalPassed);
        mainReportObject.put(totalFailedText, buildTotalFailed);

    }

    public boolean isTestResultIsPassOrFail(JSONObject testFileObject,JSONObject testResultObject){
        boolean flag = false;
        JSONArray testsFileArray = (JSONArray) testFileObject.get(testsFileText);
        JSONObject tempTestFileObject = null;
        int size = testsFileArray.size();
        JSONArray testArray = null;

        for (int i = 0; i < size; i++) {
            tempTestFileObject = (JSONObject) testsFileArray.get(i);
            testArray = (JSONArray) tempTestFileObject.get(testsText);
            for (int t = 0; t < testArray.size(); t++) {
                if (testResultObject.get(testNameText).equals(((JSONObject) testArray.get(t)).get(testNameText)) && testResultObject.get(statusText).equals(failedText)) {flag = true; }
                if (testResultObject.get(testNameText).equals(((JSONObject) testArray.get(t)).get(testNameText)) && testResultObject.get(statusText).equals(passedText) && ((JSONObject) testArray.get(t)).get(statusText).equals(failedText)) { buildTotalFailed--; }

            }
        }
        return flag;
    }


    public void checkForTheBrowser(String browserName) {

        JSONObject browserObject = new JSONObject();
        JSONObject testsFileObject = new JSONObject();
        JSONArray testsFileArray = new JSONArray();

        if (browserArray.isEmpty()) {

            testsFileObject.put(testsFileText, testsFileArray);
            browserObject.put(browserName, testsFileObject);
            browserArray.add(browserObject);

        } else {

            synchronized (this) {
                int size = browserArray.size();
                boolean browserFlag = false;
                for (int i = 0; i < size; i++) {
                    try {

                        JSONObject testsFileTempObject = (JSONObject) ((JSONObject) browserArray.get(i)).get(browserName);

                        if (testsFileTempObject.size() > 0) {
                            browserFlag = true;
                            break;
                        }

                    } catch (NullPointerException e) { log.error(""); }
                }

                if (!browserFlag) {
                    testsFileObject.put(testsFileText, testsFileArray);
                    browserObject.put(browserName, testsFileObject);
                    browserArray.add(browserObject);


                }

            }
        }

    }


    public JSONObject getBrowserObject(String browserName) {
        JSONObject testsFileObject = null;
        int size = browserArray.size();

        for (int i = 0; i < size; i++) {
            try {
                testsFileObject = (JSONObject) ((JSONObject) browserArray.get(i)).get(browserName);
                if (testsFileObject.size() > 0) {
                    break;
                }
            } catch (NullPointerException e) { log.error("");}
        }
        return testsFileObject;
    }

    public JSONObject setBrowserObject(String browserName, JSONArray testsFileArray) {
        JSONObject testsFileObject = new JSONObject();
        int size = browserArray.size();
        JSONObject browserObject = new JSONObject();

        boolean browserFlag = false;

        int i;
        for (i = 0; i < size; i++) {
            try {
                testsFileObject = (JSONObject) ((JSONObject) browserArray.get(i)).get(browserName);

                if (testsFileObject.size() > 0) {
                    browserFlag = true;
                    break;
                }

            } catch (NullPointerException e) {log.error(""); }
        }
        if (!browserFlag) {

            testsFileObject.put(testsFileText, testsFileArray);
            browserObject.put(browserName, testsFileObject);
            browserArray.set(i, browserObject);

        }

        return testsFileObject;
    }


    public void checkForTheTestsFile(String browserName, String testsFile) {

        JSONObject testsFileObject = getBrowserObject(browserName);

        JSONArray testsFileArray = (JSONArray) testsFileObject.get(testsFileText);


        JSONArray testArray = new JSONArray();
        JSONObject individualTestsFileObject = new JSONObject();


        if (testsFileArray.isEmpty()) {

            individualTestsFileObject.put(testsFileNameText, testsFile);
            individualTestsFileObject.put(testsText, testArray);
            individualTestsFileObject.put(totalTimeText, 0);
            individualTestsFileObject.put(totalFailedText, 0);
            individualTestsFileObject.put(totalPassedText, 0);
            testsFileArray.add(individualTestsFileObject);

        } else {

            synchronized (this) {
                int size = testsFileArray.size();
                boolean testFileFlag = false;
                for (int i = 0; i < size; i++) {
                    try {

                        JSONObject testsFileTempObject = (JSONObject) testsFileArray.get(i);


                        if (testsFileTempObject.get(testsFileNameText).equals(testsFile)) {
                            testFileFlag = true;
                            break;
                        }

                    } catch (NullPointerException e) {log.error(""); }
                }

                if (!testFileFlag) {
                    individualTestsFileObject.put(testsText, testArray);
                    individualTestsFileObject.put(testsFileNameText, testsFile);
                    individualTestsFileObject.put(totalTimeText, 0);
                    individualTestsFileObject.put(totalFailedText, 0);
                    individualTestsFileObject.put(totalPassedText, 0);

                    testsFileArray.add(individualTestsFileObject);
                }
            }

        }
        setBrowserObject(browserName, testsFileArray);
    }


    public void checkForTheTest(String browserName, String testsFile, JSONObject testResult) {

        JSONObject testsFileObject = getBrowserObject(browserName);
        JSONArray testsFileArray = (JSONArray) testsFileObject.get(testsFileText);
        JSONArray newTestsFileArray = new JSONArray();

        int size = testsFileArray.size();

        int totalPassed = 0;
        int totalFailed = 0;
        double totalTime = 0.0;
        JSONObject tempTestsFileObject = null;
        int testsFileCount = 0;

        /*test objects*/
        JSONArray testArray = null;

        //lopping for finding testsFile
        for (int i = 0; i < size; i++) {

            tempTestsFileObject = (JSONObject) testsFileArray.get(i);
            String testsFileName = tempTestsFileObject.get(testsFileNameText).toString();

            if (testsFileName.equals(testsFile)) {
                testArray = (JSONArray) tempTestsFileObject.get(testsText);

                JSONObject testResultAndTime=getTotalPassFailTestCountAndTotalTimeWhenTestArraySizeIsZero(testArray,testResult,tempTestsFileObject);

                testResultAndTime=getTotalPassFailTestCountAndTotalTimeWhenRetryAnalyserTrue(testArray,testResult,tempTestsFileObject,testResultAndTime,testsFile);
                totalPassed= (int) testResultAndTime.get(totalPassedText);
                totalFailed= (int) testResultAndTime.get(totalFailedText);
                totalTime= (double) testResultAndTime.get(totalTimeText);

                if(!((boolean)testResultAndTime.get("flag"))) {

                    JSONObject testPassFailResult=getTotalPassFailTestCount( testResult, tempTestsFileObject,totalPassed,totalFailed);
                    totalPassed= (int) testPassFailResult.get(totalPassedText);
                    totalFailed= (int) testPassFailResult.get(totalFailedText);
                    totalTime = Double.parseDouble(tempTestsFileObject.get(totalTimeText).toString()) + Double.parseDouble(testResult.get(totalTimeText).toString());
                    testArray.add(testResult);
                }
                testsFileCount = i;
            }
        }
        for (int i = 0; i < size; i++) {

            tempTestsFileObject = (JSONObject) testsFileArray.get(i);
            String testsFileName = tempTestsFileObject.get(testsFileNameText).toString();

            if (testsFileName.equals(testsFile)) {
                try {
                    tempTestsFileObject.put(totalTimeText, totalTime);
                    tempTestsFileObject.put(totalFailedText, totalFailed);
                    tempTestsFileObject.put(totalPassedText, totalPassed);
                    tempTestsFileObject.put(testsText, testArray);
                    newTestsFileArray.set(testsFileCount, tempTestsFileObject);
                } catch (Exception e) {
                    newTestsFileArray.add(tempTestsFileObject);
                }
                setBrowserObject(browserName, newTestsFileArray);
            }
        }


    }

    public JSONObject getTotalPassFailTestCountAndTotalTimeWhenTestArraySizeIsZero(JSONArray testArray,JSONObject testResult,JSONObject tempTestsFileObject){
        int testArraySize = testArray.size();
        int totalPassed = 0;
        int totalFailed = 0;
        double totalTime = 0.0;

        if (testArraySize == 0) {
            if (testResult.get(statusText).toString().equals(passedText)) {
                totalPassed = Integer.parseInt(tempTestsFileObject.get(totalPassedText).toString()) + 1;
                totalFailed = Integer.parseInt(tempTestsFileObject.get(totalFailedText).toString());
            }
            if (testResult.get(statusText).toString().equals(failedText)) {
                totalPassed = Integer.parseInt(tempTestsFileObject.get(totalPassedText).toString());
                totalFailed = Integer.parseInt(tempTestsFileObject.get(totalFailedText).toString()) + 1;
            }
            totalTime = Double.parseDouble(tempTestsFileObject.get(totalTimeText).toString()) + Double.parseDouble(testResult.get(totalTimeText).toString());

        }
        JSONObject testResultAndTime=new JSONObject();
        testResultAndTime.put(totalPassedText,totalPassed);
        testResultAndTime.put(totalFailedText,totalFailed);
        testResultAndTime.put(totalTimeText,totalTime);

        return testResultAndTime;
    }

    public JSONObject getTotalPassFailTestCountAndTotalTimeWhenRetryAnalyserTrue(JSONArray testArray,JSONObject testResult,JSONObject tempTestsFileObject,JSONObject testResultAndTime,String testsFile){
        boolean flag=false;
        int totalPassed= (int) testResultAndTime.get(totalPassedText);
        int totalFailed= (int) testResultAndTime.get(totalFailedText);
        double totalTime= (double) testResultAndTime.get(totalTimeText);

        for (int t = 0; t < testArray.size(); t++) {

            if (testResult.get(testNameText).equals(((JSONObject) testArray.get(t)).get(testNameText)) && ((JSONObject) testArray.get(t)).get(statusText).equals(failedText) && ((JSONObject) testArray.get(t)).get(testsFileNameText).equals(testsFile)) {
                testArray.set(t, testResult);
                flag = true;
            }
            if(testResult.get(testNameText).equals(((JSONObject)testArray.get(t)).get(testNameText)) &&((JSONObject) testArray.get(t)).get(testsFileNameText).equals(testsFile))
            {
                if (testResult.get(statusText).toString().equals(passedText)) {
                    totalPassed = Integer.parseInt(tempTestsFileObject.get(totalPassedText).toString()) + 1;
                    totalFailed = Integer.parseInt(tempTestsFileObject.get(totalFailedText).toString())-1;
                }
                else{
                    totalPassed = Integer.parseInt(tempTestsFileObject.get(totalPassedText).toString());
                    totalFailed = Integer.parseInt(tempTestsFileObject.get(totalFailedText).toString());
                }
                totalTime = Double.parseDouble(tempTestsFileObject.get(totalTimeText).toString()) + Double.parseDouble(testResult.get(totalTimeText).toString());
            }

        }

        testResultAndTime.put(totalPassedText,totalPassed);
        testResultAndTime.put(totalFailedText,totalFailed);
        testResultAndTime.put(totalTimeText,totalTime);
        testResultAndTime.put("flag",flag);


        return testResultAndTime;
    }

    public JSONObject getTotalPassFailTestCount(JSONObject testResult,JSONObject tempTestsFileObject,int totalPassed,int totalFailed){
        if (testResult.get(statusText).toString().equals(passedText)) {
            totalPassed = Integer.parseInt(tempTestsFileObject.get(totalPassedText).toString()) + 1;
            totalFailed = Integer.parseInt(tempTestsFileObject.get(totalFailedText).toString());
        }
        if (testResult.get(statusText).toString().equals(failedText)) {
            totalPassed = Integer.parseInt(tempTestsFileObject.get(totalPassedText).toString());
            totalFailed = Integer.parseInt(tempTestsFileObject.get(totalFailedText).toString()) + 1;
        }
        JSONObject testPassFailResult=new JSONObject();
        testPassFailResult.put(totalPassedText,totalPassed);
        testPassFailResult.put(totalFailedText,totalFailed);
        return testPassFailResult;
    }

    /**
     * @author Viral P.
     */

    @Override
    public void run() {
        ReportParser pr = new ReportParser();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
        while (TestExecutionBuilder.buildRunning) {

            pr.writeJsonFile(mainReportObject, TestExecutionBuilder.buildReportName);

            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(sw));
                log.error(sw.toString());
            }

        }

        pr.writeJsonFile(mainReportObject, TestExecutionBuilder.buildReportName);
        TestExecutionBuilder.repotFileGenerated = true;
    }
}



