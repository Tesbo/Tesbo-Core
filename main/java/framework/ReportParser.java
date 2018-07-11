package framework;

import Execution.TestExecutionBuilder;
import logger.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import Exception.TesboException;

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

    /**
     * @param test
     * @param step
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public String dataSetStepReplaceValue(JSONObject test, String step) {

        DataDrivenParser dataDrivenParser=new DataDrivenParser();
        Logger logger = new Logger();
        String textToEnter = "";
        int startPoint = 0;
        int endPoint = 0;
        String headerName="";
        if (step.contains("{") && step.contains("}")) {
            startPoint = step.indexOf("{") + 1;
            endPoint = step.lastIndexOf("}");
            headerName = step.substring(startPoint, endPoint);
            try {
                if (test.get("dataType").toString().equalsIgnoreCase("excel")) {
                    try {
                        textToEnter = dataDrivenParser.getcellValuefromExcel(dataDrivenParser.getExcelUrl(test.get("suiteName").toString(), test.get("dataSetName").toString()), headerName, (Integer) test.get("row"));

                    } catch (StringIndexOutOfBoundsException e) {
                        logger.stepLog(step);
                        logger.testFailed("no string to enter. Create a separate exeception here");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (test.get("dataType").toString().equalsIgnoreCase("global")) {
                    textToEnter = dataDrivenParser.getGlobalDataValue(test.get("suiteName").toString(), test.get("dataSetName").toString(), headerName);
                }
            } catch (Exception e) {
                throw new TesboException("Key name " + headerName + " is not found in " + test.get("dataSetName").toString() + " data set");
            }
        } else {
            startPoint = step.indexOf("'") + 1;
            endPoint = step.lastIndexOf("'");
            try {

                textToEnter = step.substring(startPoint, endPoint);

            } catch (StringIndexOutOfBoundsException e) {
                throw new TesboException("No string found to enter.");
            }
        }
        return step.replace(headerName, textToEnter);
    }

    /**
     * @auther : Ankit Mistry
     * @param stepReportObject
     * @param step
     * @return
     */
    public JSONObject addScreenshotUrlInReport(JSONObject stepReportObject, String step)  {
        StepParser stepParser=new StepParser();
        if (step.toString().toLowerCase().contains("capture screenshot")) {
            if(stepParser.screenShotURL !=null){
                stepReportObject.remove("steps");
                stepReportObject.put("steps", "Screenshot: <a href=\"../"+stepParser.screenShotURL+"\" target=\"_blank\">/"+stepParser.screenShotURL+"</a>");
            }
        }
        return stepReportObject;
    }

}
