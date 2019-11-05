package DataCollector;


import Execution.TestExecutionBuilder;
import framework.ReportParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BuildReportDataObject implements Runnable {


    public static JSONObject mainReportObject = new JSONObject();
    static JSONArray browserArray = new JSONArray();
    static int buildTotalPassed = 0;
    static int buildTotalFailed = 0;


    public void startThread() {
        BuildReportDataObject brdo = new BuildReportDataObject();
        Thread t1 = new Thread(brdo);
        t1.start();
    }

    public void addDataInMainObject(String browser, String testFileName, String testName, JSONObject testResultObject) {
        JSONObject testFileObject = getBrowserObject(browser);
        Boolean flag = false;
        if(testFileObject!=null) {
            JSONArray testsFileArray = (JSONArray) testFileObject.get("testsFile");
            JSONObject tempTestFileObject = null;
            int size = testsFileArray.size();
            JSONArray testArray = null;

            for (int i = 0; i < size; i++) {
                tempTestFileObject = (JSONObject) testsFileArray.get(i);
                testArray = (JSONArray) tempTestFileObject.get("tests");
                for (int t = 0; t < testArray.size(); t++) {
                    if (testResultObject.get("testName").equals(((JSONObject) testArray.get(t)).get("testName")) && testResultObject.get("status").equals("failed")) {flag = true; }
                    if (testResultObject.get("testName").equals(((JSONObject) testArray.get(t)).get("testName")) && testResultObject.get("status").equals("passed") && ((JSONObject) testArray.get(t)).get("status").equals("failed")) { buildTotalFailed--; }

                }
            }
        }
        if(!flag) {
            if (testResultObject.get("status").equals("passed")) { buildTotalPassed++; }
            if (testResultObject.get("status").equals("failed")) { buildTotalFailed++; }
        }
        checkForTheBrowser(browser);
        checkForTheTestsFile(browser, testFileName);
        checkForTheTest(browser, testFileName, testResultObject);

        mainReportObject.put("browser", browserArray);
        mainReportObject.put("startTime", TestExecutionBuilder.buildStartTime);
        mainReportObject.put("totalPassed", buildTotalPassed);
        mainReportObject.put("totalFailed", buildTotalFailed);



    }


    public void checkForTheBrowser(String browserName) {

        JSONObject browserObject = new JSONObject();
        JSONObject testsFileObject = new JSONObject();
        JSONArray testsFileArray = new JSONArray();

        if (browserArray.size() == 0) {

            testsFileObject.put("testsFile", testsFileArray);
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

                    } catch (NullPointerException e) {
                    }
                }

                if (!browserFlag) {
                    testsFileObject.put("testsFile", testsFileArray);
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
            } catch (NullPointerException e) { }
        }
        return testsFileObject;
    }

    public JSONObject setBrowserObject(String browserName, JSONArray testsFileArray) {
        JSONObject testsFileObject = null;
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

            } catch (NullPointerException e) {
            }
        }
        if (!browserFlag) {

            testsFileObject.put("testsFile", testsFileArray);
            browserObject.put(browserName, testsFileObject);
            browserArray.set(i, browserObject);

        }

        return testsFileObject;
    }


    public void checkForTheTestsFile(String browserName, String testsFile) {

        JSONObject testsFileObject = getBrowserObject(browserName);

        JSONArray testsFileArray = (JSONArray) testsFileObject.get("testsFile");


        JSONArray testArray = new JSONArray();
        JSONObject individualTestsFileObject = new JSONObject();


        if (testsFileArray.size() == 0) {

            individualTestsFileObject.put("testsFileName", testsFile);
            individualTestsFileObject.put("tests", testArray);
            individualTestsFileObject.put("totalTime", 0);
            individualTestsFileObject.put("totalFailed", 0);
            individualTestsFileObject.put("totalPassed", 0);
            testsFileArray.add(individualTestsFileObject);

        } else {

            synchronized (this) {
                int size = testsFileArray.size();
                boolean testFileFlag = false;
                for (int i = 0; i < size; i++) {
                    try {

                        JSONObject testsFileTempObject = (JSONObject) ((JSONObject) testsFileArray.get(i));


                        if (testsFileTempObject.get("testsFileName").equals(testsFile)) {
                            testFileFlag = true;
                            break;
                        }

                    } catch (NullPointerException e) {
                    }
                }

                if (!testFileFlag) {
                    individualTestsFileObject.put("tests", testArray);
                    individualTestsFileObject.put("testsFileName", testsFile);
                    individualTestsFileObject.put("totalTime", 0);
                    individualTestsFileObject.put("totalFailed", 0);
                    individualTestsFileObject.put("totalPassed", 0);

                    testsFileArray.add(individualTestsFileObject);
                }
            }

        }
        setBrowserObject(browserName, testsFileArray);
    }


    public void checkForTheTest(String browserName, String testsFile, JSONObject testResult) {


        JSONObject testsFileObject = getBrowserObject(browserName);

        JSONArray testsFileArray = (JSONArray) testsFileObject.get("testsFile");

        JSONArray newTestsFileArray = new JSONArray();

        int size = testsFileArray.size();

        int totalPassed = 0, totalFailed = 0;
        double totalTime = 0.0;
        JSONObject tempTestsFileObject = null;
        int testsFileCount = 0;

        /*test objects*/
        JSONArray testArray = null;

        /*test step objects*/

        //lopping for finding testsFile
        for (int i = 0; i < size; i++) {


            tempTestsFileObject = (JSONObject) testsFileArray.get(i);
            String testsFileName = tempTestsFileObject.get("testsFileName").toString();


            if (testsFileName.equals(testsFile)) {
                testArray = (JSONArray) tempTestsFileObject.get("tests");


                int testArraySize = testArray.size();

                if (testArraySize == 0) {
                    if (testResult.get("status").toString().equals("passed")) {
                        totalPassed = Integer.parseInt(tempTestsFileObject.get("totalPassed").toString()) + 1;
                        totalFailed = Integer.parseInt(tempTestsFileObject.get("totalFailed").toString());
                    }
                    if (testResult.get("status").toString().equals("failed")) {
                        totalPassed = Integer.parseInt(tempTestsFileObject.get("totalPassed").toString());
                        totalFailed = Integer.parseInt(tempTestsFileObject.get("totalFailed").toString()) + 1;
                    }
                    totalTime = Double.parseDouble(tempTestsFileObject.get("totalTime").toString()) + Double.parseDouble(testResult.get("totalTime").toString());

                }

                boolean flag=false;
                for (int t = 0; t < testArray.size(); t++) {

                    if (testResult.get("testName").equals(((JSONObject) testArray.get(t)).get("testName")) && ((JSONObject) testArray.get(t)).get("status").equals("failed") && ((JSONObject) testArray.get(t)).get("testsFileName").equals(testsFile)) {
                        testArray.set(t, testResult);
                        flag = true;
                    }
                    if(testResult.get("testName").equals(((JSONObject)testArray.get(t)).get("testName")) &&((JSONObject) testArray.get(t)).get("testsFileName").equals(testsFile))
                    {
                        if (testResult.get("status").toString().equals("passed")) {
                            totalPassed = Integer.parseInt(tempTestsFileObject.get("totalPassed").toString()) + 1;
                            totalFailed = Integer.parseInt(tempTestsFileObject.get("totalFailed").toString())-1;
                        }
                        else{
                            totalPassed = Integer.parseInt(tempTestsFileObject.get("totalPassed").toString());
                            totalFailed = Integer.parseInt(tempTestsFileObject.get("totalFailed").toString());
                        }
                        totalTime = Double.parseDouble(tempTestsFileObject.get("totalTime").toString()) + Double.parseDouble(testResult.get("totalTime").toString());
                    }

                }

                if(!flag) {

                    if (testResult.get("status").toString().equals("passed")) {
                        totalPassed = Integer.parseInt(tempTestsFileObject.get("totalPassed").toString()) + 1;
                        totalFailed = Integer.parseInt(tempTestsFileObject.get("totalFailed").toString());
                    }
                    if (testResult.get("status").toString().equals("failed")) {
                        totalPassed = Integer.parseInt(tempTestsFileObject.get("totalPassed").toString());
                        totalFailed = Integer.parseInt(tempTestsFileObject.get("totalFailed").toString()) + 1;
                    }
                    totalTime = Double.parseDouble(tempTestsFileObject.get("totalTime").toString()) + Double.parseDouble(testResult.get("totalTime").toString());
                    testArray.add(testResult);
                }
                testsFileCount = i;
            }
        }
        for (int i = 0; i < size; i++) {

            tempTestsFileObject = (JSONObject) testsFileArray.get(i);
            String testsFileName = tempTestsFileObject.get("testsFileName").toString();

            if (testsFileName.equals(testsFile)) {
                try {
                    tempTestsFileObject.put("totalTime", totalTime);
                    tempTestsFileObject.put("totalFailed", totalFailed);
                    tempTestsFileObject.put("totalPassed", totalPassed);
                    tempTestsFileObject.put("tests", testArray);
                    newTestsFileArray.set(testsFileCount, tempTestsFileObject);
                } catch (Exception e) {
                    newTestsFileArray.add(tempTestsFileObject);
                }
                setBrowserObject(browserName, newTestsFileArray);
            }
        }


    }

    /**
     * @author Viral P.
     */

    @Override
    public void run() {
        ReportParser pr = new ReportParser();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (TestExecutionBuilder.buildRunning) {

            pr.writeJsonFile(mainReportObject, TestExecutionBuilder.buildReportName);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        pr.writeJsonFile(mainReportObject, TestExecutionBuilder.buildReportName);
        TestExecutionBuilder.repotFileGenerated = true;
    }
}



