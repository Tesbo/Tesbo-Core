package framework;

import Execution.TestExecutionBuilder;
import com.google.gson.JsonArray;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

public class ReportParser {

    TestExecutionBuilder build = new TestExecutionBuilder();


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

    /*public void report() throws Exception {
        JSONObject mainObject = readJsonFile();
        JSONArray suiteName = getSuiteName();
        JSONArray testDetais = build.buildExecutionQueueByTag();
        getbuildTotalData(mainObject, suiteName, testDetais);

    }*/

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
            System.out.println("in exception");
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
            Object obj = parser.parse(new FileReader("C:\\Users\\jsbot\\Desktop\\test.json"));

            JSONObject jsonObject = (JSONObject) obj;
            //System.out.println(jsonObject);
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
            /*for(Object suitName : suite){
                System.out.println();
            }*/
            //System.out.println(testData.size());
            //System.out.println(suitename.get(j));
            //System.out.println("Ros : "+mainObject.get(suitename.get(j)));
            if ((mainObject.get(suitename.get(j))) != null) {

                //System.out.println("Roshan : "+mainObject.get(suitename.get(j)));

                JSONObject suitData = (JSONObject) mainObject.get(suitename.get(j));
                JSONArray testArray = new JSONArray();
                int F = 0;
                int P = 0;
                int T = 0;
                for (int i = 0; i <= testData.size() - 1; i++) {
                    //System.out.println(testData.get(i));
                    JSONObject data = (JSONObject) testData.get(i);
                    //System.out.println(data.get("testName"));

                    //System.out.println("Suit Data : "+suitData.get(data.get("testName")));

                    if ((suitData.get(data.get("testName"))) != null) {
                        //System.out.println(suitData.get(data.get("testName")));
                        JSONObject testObj = (JSONObject) suitData.get(data.get("testName"));
                        testArray.add(testObj);
                        //System.out.println(testObj.get("totalTime"));

                        //System.out.println(testObj.get("status"));
                        if ((testObj.get("status")).equals("pass")) {
                            P++;
                            T += Integer.parseInt(testObj.get("totalTime").toString());
                        } else if ((testObj.get("status")).equals("fail")) {
                            F++;
                            //System.out.println(testObj);
                            T += Integer.parseInt(testObj.get("totalTime").toString());
                        }

                        suiteResult.put("suiteName", testObj.get("suiteName").toString());

                    } else {
                        //System.out.println("null");
                    }
                }
                //System.out.println("totalPassed : "+P);
                suitData.put("totalPassed", +P);
                //System.out.println("totalFailed : "+F);
                suitData.put("totalFailed", +F);
                //System.out.println("totaltime : "+T);
                suitData.put("totalTime", +T);

                suiteResult.put("tests",testArray);
                suiteResult.put("totalPassed", +P);
                suiteResult.put("totalFailed", +F);
                suiteResult.put("totalTime", +T);

                suite.add(suiteResult);
            }
        }
        //System.out.println(mainObject);
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
                //System.out.println("Ros : "+mainObject.get(suitename.get(j)));
                JSONObject suitData = (JSONObject) mainObject.get(suitename.get(j));
                //System.out.println(suitData.get("totalFailed"));
                F += Integer.parseInt(suitData.get("totalFailed").toString());
                //System.out.println(suitData.get("totalPassed"));
                P += Integer.parseInt(suitData.get("totalPassed").toString());
            }
        }
        //System.out.println(F);
        mainObject.put("totalFailed", F);
        //System.out.println(P);
        mainObject.put("totalPassed", P);
        //System.out.println(mainObject);
        TestExecutionBuilder main = new TestExecutionBuilder();
        main.reportObj.put("totalFailed", F);
        main.reportObj.put("totalFailed", F);
    }
}
