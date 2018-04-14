package framework;

import Execution.TestExecutionBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;

public class ReportParser {

    TestExecutionBuilder build = new TestExecutionBuilder();

    /*public static void main(String[] args) throws Exception {
        ReportParser report = new ReportParser();
        JSONObject browserData = report.getBrowser();
        report.newBrowser(browserData);

    }*/
    public void newReport(JSONObject mainObject, JSONObject reportObj) throws Exception {
        JSONObject browserData = getBrowser(mainObject, reportObj);
        newBrowser(browserData);
    }

    /*public void report() throws Exception {
        JSONObject mainObject = readJsonFile();
        JSONArray suiteName = getSuiteName();
        JSONArray testDetais = build.buildExecutionQueueByTag();
        getbuildTotalData(mainObject, suiteName, testDetais);

    }*/

    public void reportForTag(JSONObject mainObject) throws Exception {
        //JSONObject mainObject = readJsonFile();
        JSONArray suiteName = getSuiteName();
        JSONArray testDetais = build.buildExecutionQueueByTag();
        //System.out.println("Obj 1 : "+mainObject);
        getSuiteTotalData(mainObject, suiteName, testDetais);
        getbuildTotalData(mainObject, suiteName, testDetais);
    }

    public void reportForSuit(JSONObject mainObject) throws Exception {
        //JSONObject mainObject = readJsonFile();
        JSONArray suiteName = getSuiteName();
        JSONArray testDetais = build.buildExecutionQueueBySuite();
        //System.out.println("Obj 1 : "+mainObject);
        getSuiteTotalData(mainObject, suiteName, testDetais);
        getbuildTotalData(mainObject, suiteName, testDetais);
    }

    /**
     * @return
     * @throws Exception
     * @Description : Get suit Names.
     */
    public JSONArray getSuiteName() throws Exception {
        GetConfiguration configuration = new GetConfiguration();
        SuiteParser suite = new SuiteParser();
        String directoryPath = configuration.getSuitesDirectory();

        JSONArray suiteFileList = suite.getSuites(directoryPath);
        JSONArray allSuite = new JSONArray();

        for (int i = 0; i < suiteFileList.size(); i++) {
            File name = new File(suiteFileList.get(i).toString());
            allSuite.add(name.getName());
        }

        return allSuite;
    }

    /**
     * @param obj
     * @throws IOException
     * @Decription : Create JSON file.
     */
    public void writeJsonFile(JSONObject obj, String fileName) throws IOException {


        try (FileWriter file = new FileWriter("./htmlReport/Build History/" + fileName + ".json")) {


            file.write(obj.toJSONString());
            file.flush();


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public void generateReportDir() {
        File htmlReportMainDir = new File("./htmlReport");

        if (!htmlReportMainDir.exists()) {
            htmlReportMainDir.mkdir();
        }


        File buildHistory = new File("./htmlReport/Build History");

        if (!buildHistory.exists()) {
            buildHistory.mkdir();
        }


    }

    public JSONObject readJsonFile() throws IOException, ParseException {

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader("C:\\Users\\jsbot\\Desktop\\buildResult_4.json"));

            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (ParseException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * @param mainObject
     * @param suitename
     * @param testData
     * @Description : Get total suit data and add into the main JSON object.
     */
    public void getSuiteTotalData(JSONObject mainObject, JSONArray suitename, JSONArray testData) {
        for (int j = 0; j <= suitename.size() - 1; j++) {
            TestExecutionBuilder main = new TestExecutionBuilder();
            JSONArray suite = (JSONArray) main.reportObj.get("suits");


            JSONObject suiteResult = new JSONObject();
            if ((mainObject.get(suitename.get(j))) != null) {


                JSONObject suitData = (JSONObject) mainObject.get(suitename.get(j) + "firefox");
                JSONArray testArray = new JSONArray();
                int F = 0;
                int P = 0;
                int T = 0;
                for (int i = 0; i <= testData.size() - 1; i++) {
                    JSONObject data = (JSONObject) testData.get(i);

                    if ((suitData.get(data.get("testName"))) != null) {
                        JSONObject testObj = (JSONObject) suitData.get(data.get("testName"));
                        testArray.add(testObj);
                        if ((testObj.get("status")).equals("pass")) {
                            P++;
                            T += Integer.parseInt(testObj.get("totalTime").toString());
                        } else if ((testObj.get("status")).equals("fail")) {
                            F++;
                            T += Integer.parseInt(testObj.get("totalTime").toString());
                        }

                        suiteResult.put("suiteName", testObj.get("suiteName").toString());

                    } else {
                    }
                }
                suitData.put("totalPassed", +P);
                suitData.put("totalFailed", +F);
                suitData.put("totalTime", +T);

                suiteResult.put("tests", testArray);
                suiteResult.put("totalPassed", +P);
                suiteResult.put("totalFailed", +F);
                suiteResult.put("totalTime", +T);

                suite.add(suiteResult);
            }
        }
    }

    /**
     * @param mainObject
     * @param suitename
     * @param testData
     * @Description : Create total data for then current build and that data add into the main JSON.
     */
    public void getbuildTotalData(JSONObject mainObject, JSONArray suitename, JSONArray testData) {
        int F = 0;
        int P = 0;
        int T = 0;
        for (int j = 0; j <= suitename.size() - 1; j++) {
            if ((mainObject.get(suitename.get(j))) != null) {
                JSONObject suitData = (JSONObject) mainObject.get(suitename.get(j));
                F += Integer.parseInt(suitData.get("totalFailed").toString());
                P += Integer.parseInt(suitData.get("totalPassed").toString());
            }
        }
        mainObject.put("totalFailed", F);

        mainObject.put("totalPassed", P);
        TestExecutionBuilder main = new TestExecutionBuilder();
        main.reportObj.put("totalFailed", F);
        main.reportObj.put("totalPassed", P);
    }

    public JSONObject getBrowser(JSONObject mainObj, JSONObject newObj1) throws Exception {
        JSONObject newObj = new JSONObject();
        int F = 0;
        int P = 0;
        JSONArray testCase = (JSONArray) mainObj.get("testCase");
        for (int TC = 0; TC < testCase.size(); TC++) {
            JSONObject TC1 = (JSONObject) testCase.get(TC);
            if ((TC1.get("status")).equals("pass")) {
                P++;
            } else if ((TC1.get("status")).equals("fail")) {
                F++;
            }
        }
        newObj1.put("totalFailed", F);
        newObj1.put("totalPassed", P);
        GetConfiguration config = new GetConfiguration();
        for (String Browser : config.getBrowsers()) {
            for (int TC = 0; TC < testCase.size(); TC++) {
                JSONObject TC1 = (JSONObject) testCase.get(TC);
                if (Browser.equalsIgnoreCase(TC1.get("browserName").toString())) {
                    if ((newObj.get(Browser)) == null) {
                        JSONArray TCData = new JSONArray();
                        TCData.add(TC1);
                        newObj.put(Browser, TCData);
                    } else {
                        JSONArray test = (JSONArray) newObj.get(Browser);
                        test.add(TC1);
                    }
                }
            }
        }
        JSONArray newBrowserDataInArray = new JSONArray();
        for (String Browser : config.getBrowsers()) {
            newObj.get(Browser);
            JSONObject br = new JSONObject();
            br.put(Browser, newObj.get(Browser));
            newBrowserDataInArray.add(br);
        }
        newObj1.put("browser", newBrowserDataInArray);
        return newObj1;

    }

    public void newBrowser(JSONObject mainObj) throws Exception {
        GetConfiguration config = new GetConfiguration();
        JSONArray browserDataArreay = (JSONArray) mainObj.get("browser");

        for (int brw = 0; brw < browserDataArreay.size(); brw++) {
            JSONObject browser = (JSONObject) browserDataArreay.get(brw);

            for (String Browser : config.getBrowsers()) {
                JSONObject newTestData = new JSONObject();
                if (browser.get(Browser) != null) {
                    JSONArray TCDataArray = (JSONArray) browser.get(Browser);
                    ArrayList<String> suitName = new ArrayList<>();
                    for (int TC = 0; TC < TCDataArray.size(); TC++) {
                        JSONObject Test = (JSONObject) TCDataArray.get(TC);
                        suitName.add(Test.get("suiteName").toString());
                    }
                    ArrayList<String> onlySuitsName = removeDuplicateFromArray(suitName);
                    for (int TC = 0; TC < TCDataArray.size(); TC++) {
                        JSONObject Test = (JSONObject) TCDataArray.get(TC);
                        for (String suite : onlySuitsName) {
                            if (suite.toString().equalsIgnoreCase(Test.get("suiteName").toString())) {
                                if ((newTestData.get(suite)) == null) {
                                    JSONArray TCData = new JSONArray();
                                    TCData.add(Test);
                                    newTestData.put(suite, TCData);
                                } else {
                                    JSONArray test = (JSONArray) newTestData.get(suite);
                                    test.add(Test);
                                }
                            }
                        }
                    }

                    JSONArray newTestDataInArray = new JSONArray();
                    for (String suite : onlySuitsName) {
                        JSONObject br = new JSONObject();
                        int F = 0;
                        int P = 0;
                        int T = 0;
                        JSONArray testCase = (JSONArray) newTestData.get(suite);;
                        for (int TC = 0; TC < testCase.size(); TC++) {
                            JSONObject TC1 = (JSONObject) testCase.get(TC);
                            T += Integer.parseInt(TC1.get("totalTime").toString());
                            if ((TC1.get("status")).equals("pass")) {
                                P++;
                            } else if ((TC1.get("status")).equals("fail")) {
                                F++;
                            }
                        }
                        br.put("totalFailed", F);
                        br.put("totalPassed", P);
                        br.put("totalTime", T);
                        br.put("tests", newTestData.get(suite));
                        br.put("suiteName", suite);
                        newTestDataInArray.add(br);
                    }
                    JSONObject testCash = new JSONObject();
                    testCash.put("suits", newTestDataInArray);
                    browser.put(Browser, testCash);
                }
            }
        }
      }

    public ArrayList<String> removeDuplicateFromArray(ArrayList<String> dataArray) {
        ArrayList<String> resultArray = new ArrayList<String>();
        for (String data : dataArray) {
            if (resultArray.size() == 0)
                resultArray.add(data);
            else {
                boolean flag = false;
                for (String result : resultArray) {
                    if (data.equalsIgnoreCase(result)) {
                        flag = true;
                        break;
                    }
                }
                if (flag == false) {
                    resultArray.add(data);
                }
            }
        }
        return resultArray;

    }
}