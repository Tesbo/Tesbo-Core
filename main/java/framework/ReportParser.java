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
    public void writeJsonFile(JSONObject obj, String fileName) {


        try  {
            FileWriter file = new FileWriter("./htmlReport/Build History/" + fileName + ".json");

            file.write(obj.toJSONString());
          //  file.flush();

file.close();
        } catch (Exception e) {
            e.printStackTrace();
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


    }
