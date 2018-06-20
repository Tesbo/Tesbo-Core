package DataCollector;


import Execution.TestExecutionBuilder;
import framework.ReportParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BuildReportDataObject implements Runnable {


    static JSONArray browserArray = new JSONArray();
    public static JSONObject mainReportObject = new JSONObject();
    static int buildTotalPassed = 0;
    static int buildTotalFailed = 0;


    public void startThread() {
        BuildReportDataObject brdo = new BuildReportDataObject();
        Thread t1 = new Thread(brdo);
        t1.start();
    }


    public void addDataInMainObject(String browser, String suite, String testName, JSONObject testResultObject) {

        if (testResultObject.get("status").equals("passed")) {
            buildTotalPassed++;
        }

        if (testResultObject.get("status").equals("failed")) {
            buildTotalFailed++;
        }

        checkForTheBrowser(browser);
        checkForTheSuite(browser, suite);
        checkForTheTest(browser, suite, testResultObject);

        mainReportObject.put("browser", browserArray);
        mainReportObject.put("totalTimeTaken", (TestExecutionBuilder.buildEndTime - TestExecutionBuilder.buildStartTime));
        mainReportObject.put("startTime", TestExecutionBuilder.buildStartTime);


        mainReportObject.put("totalPassed", buildTotalPassed);

        mainReportObject.put("totalFailed", buildTotalFailed);


    }


    public void checkForTheBrowser(String browserName) {

        JSONObject browserObject = new JSONObject();
        JSONObject suiteObject = new JSONObject();
        JSONArray suiteArray = new JSONArray();

        if (browserArray.size() == 0) {

            suiteObject.put("suits", suiteArray);
            browserObject.put(browserName, suiteObject);
            browserArray.add(browserObject);

        } else {

            synchronized (this) {
                int size = browserArray.size();
                boolean browserFlag = false;
                for (int i = 0; i < size; i++) {
                    try {

                        JSONObject suiteTempObject = (JSONObject) ((JSONObject) browserArray.get(i)).get(browserName);

                        if (suiteTempObject.size() > 0) {
                            browserFlag = true;
                            break;
                        }

                    } catch (NullPointerException e) {
                    }
                }

                if (!browserFlag) {
                    suiteObject.put("suits", suiteArray);
                    browserObject.put(browserName, suiteObject);
                    browserArray.add(browserObject);


                }

            }
        }

    }


    public JSONObject getBrowserObject(String browserName) {
        JSONObject suiteObject = null;
        int size = browserArray.size();
        boolean browserFlag = false;

        for (int i = 0; i < size; i++) {
            try {

                suiteObject = (JSONObject) ((JSONObject) browserArray.get(i)).get(browserName);

                if (suiteObject.size() > 0) {
                    break;
                }

            } catch (NullPointerException e) {
            }
        }
        return suiteObject;
    }

    public JSONObject setBrowserObject(String browserName, JSONArray suiteArray) {
        JSONObject suiteObject = null;
        int size = browserArray.size();
        JSONObject browserObject = new JSONObject();

        boolean browserFlag = false;

        int i;
        for (i = 0; i < size; i++) {
            try {
                suiteObject = (JSONObject) ((JSONObject) browserArray.get(i)).get(browserName);

                if (suiteObject.size() > 0) {
                    browserFlag = true;
                    break;
                }

            } catch (NullPointerException e) {
            }
        }
        if (!browserFlag) {

            suiteObject.put("suits", suiteArray);
            browserObject.put(browserName, suiteObject);
            browserArray.set(i, browserObject);

        }

        return suiteObject;
    }


    public void checkForTheSuite(String browserName, String suite) {

        JSONObject suiteObject = getBrowserObject(browserName);

        JSONArray suitesArray = (JSONArray) suiteObject.get("suits");
        JSONArray testArray = new JSONArray();
        JSONObject individualSuiteObject = new JSONObject();


        if (suitesArray.size() == 0) {

            individualSuiteObject.put("suiteName", suite);
            individualSuiteObject.put("tests", testArray);
            individualSuiteObject.put("totalTime", 0);
            individualSuiteObject.put("totalFailed", 0);
            individualSuiteObject.put("totalPassed", 0);
            suitesArray.add(individualSuiteObject);

        } else {

            synchronized (this) {
                int size = suitesArray.size();
                boolean suiteFlag = false;
                for (int i = 0; i < size; i++) {
                    try {

                        JSONObject suiteTempObject = (JSONObject) ((JSONObject) suitesArray.get(i));


                        if (suiteTempObject.get("suiteName").equals(suite)) {
                            suiteFlag = true;
                            break;
                        }

                    } catch (NullPointerException e) {
                    }
                }

                if (!suiteFlag) {
                    individualSuiteObject.put("tests", testArray);
                    individualSuiteObject.put("suiteName", suite);
                    individualSuiteObject.put("totalTime", 0);
                    individualSuiteObject.put("totalFailed", 0);
                    individualSuiteObject.put("totalPassed", 0);

                    suitesArray.add(individualSuiteObject);
                }
            }

        }
        setBrowserObject(browserName, suitesArray);
    }


    public void checkForTheTest(String browserName, String suite, JSONObject testResult) {


        JSONObject suiteObject = getBrowserObject(browserName);

        JSONArray suitesArray = (JSONArray) suiteObject.get("suits");

        JSONArray newSuiteArray = new JSONArray();

        int size = suitesArray.size();

        int totalPassed = 0, totalFailed = 0;
        double totalTime = 0.0;
        JSONObject tempSuiteObject = null;
        int suiteCount = 0;

        /*test objects*/
        JSONArray testArray = null;



        /*test step objects*/


        //lopping for finding suite
        for (int i = 0; i < size; i++) {


            tempSuiteObject = (JSONObject) suitesArray.get(i);
            String suiteName = tempSuiteObject.get("suiteName").toString();


            if (suiteName.equals(suite)) {
                testArray = (JSONArray) tempSuiteObject.get("tests");


                int testArraySize = testArray.size();


                if (testArraySize == 0) {
                    if (testResult.get("status").toString().equals("passed")) {
                        totalPassed = Integer.parseInt(tempSuiteObject.get("totalPassed").toString()) + 1;
                        totalFailed = Integer.parseInt(tempSuiteObject.get("totalFailed").toString());
                    }

                    if (testResult.get("status").toString().equals("failed")) {

                        totalPassed = Integer.parseInt(tempSuiteObject.get("totalPassed").toString());
                        totalFailed = Integer.parseInt(tempSuiteObject.get("totalFailed").toString()) + 1;
                    }
                    totalTime = Double.parseDouble(tempSuiteObject.get("totalTime").toString()) + Double.parseDouble(testResult.get("totalTime").toString());

                }


                for (int t = 0; t <= testArraySize; t++) {

                    try {
                        if (testResult.get("status").toString().equals("passed")) {
                            totalPassed = Integer.parseInt(tempSuiteObject.get("totalPassed").toString()) + 1;
                            totalFailed = Integer.parseInt(tempSuiteObject.get("totalFailed").toString());
                        }
                        if (testResult.get("status").toString().equals("failed")) {
                            totalPassed = Integer.parseInt(tempSuiteObject.get("totalPassed").toString());
                            totalFailed = Integer.parseInt(tempSuiteObject.get("totalFailed").toString()) + 1;
                        }
                        totalTime = Double.parseDouble(tempSuiteObject.get("totalTime").toString()) + Double.parseDouble(testResult.get("totalTime").toString());


                    } catch (Exception e)

                    {
                    /*    if (testResult.get("status").toString().equals("passed")) {
                            totalPassed = Double.parseDouble(tempSuiteObject.get("totalPassed").toString()) + 1;
                        }

                        if (testResult.get("status").toString().equals("failed")) {
                            totalFailed = Double.parseDouble(tempSuiteObject.get("totalFailed").toString()) + 1;
                        }
                        totalTime = Double.parseDouble(tempSuiteObject.get("totalTime").toString()) + Double.parseDouble(testResult.get("totalTime").toString());
*/
                    }


                }

                testArray.add(testResult);
                suiteCount = i;

            }


        }

        try {
            tempSuiteObject.put("totalTime", totalTime);
            tempSuiteObject.put("totalFailed", totalFailed);
            tempSuiteObject.put("totalPassed", totalPassed);
            tempSuiteObject.put("tests", testArray);
            newSuiteArray.set(suiteCount, tempSuiteObject);
        } catch (Exception e) {
            newSuiteArray.add(tempSuiteObject);

        }

        setBrowserObject(browserName, newSuiteArray);


    }


    @Override
    public void run() {
        ReportParser pr = new ReportParser();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (TestExecutionBuilder.buildRunning) {
            System.out.println("---------------------------- thread running");
            System.out.println(mainReportObject);
            pr.writeJsonFile(mainReportObject, TestExecutionBuilder.buildReportName);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        pr.writeJsonFile(mainReportObject, TestExecutionBuilder.buildReportName);

        System.out.println("---------------------------- thread stopped");


    }
}



