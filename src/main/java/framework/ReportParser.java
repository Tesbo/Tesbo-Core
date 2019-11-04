package framework;

import Execution.Tesbo;
import Execution.TestExecutionBuilder;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import Exception.TesboException;

public class ReportParser {
    private static final Logger log = LogManager.getLogger(Tesbo.class);
    /**
     * @return
     * @throws Exception
     * @Description : Get suit Names.
     */
    public JSONArray getSuiteName() throws Exception {
        GetConfiguration configuration = new GetConfiguration();
        TestsFileParser testsFileParser = new TestsFileParser();
        String directoryPath = configuration.getTestsDirectory();

        JSONArray suiteFileList = testsFileParser.getTestFiles(directoryPath);
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

    /**
     * @param test
     * @param step
     * @return
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     */
    public String dataSetStepReplaceValue(JSONObject test, String step) {

        DataDrivenParser dataDrivenParser=new DataDrivenParser();
        TesboLogger tesboLogger = new TesboLogger();
        String textToEnter = "";
        int startPoint = 0;
        int endPoint = 0;
        String headerName="";
        if (step.contains("{") && step.contains("}")) {
            startPoint = step.indexOf("{") + 1;
            endPoint = step.lastIndexOf("}");
            headerName = step.substring(startPoint, endPoint);

            boolean isDetaSet=false;
            try {
                if (headerName.contains("DataSet.")) {
                    isDetaSet=true;
                    try {
                        String dataSet[]=headerName.split("\\.");
                        if(dataSet.length==3) {
                            textToEnter = dataDrivenParser.getGlobalDataValue(test.get("testsFileName").toString(), dataSet[1], dataSet[2]);
                        }
                        else{
                            log.error("Please enter DataSet in: '"+step+"'");
                            throw new TesboException("Please enter DataSet in: '"+step+"'");
                        }

                    } catch (StringIndexOutOfBoundsException e) {
                        throw e;

                    }
                }
                else if(headerName.contains("Dataset.") || headerName.contains("dataSet.") || headerName.contains("dataset.")){
                    log.error("Please enter valid DataSet in: '"+step+"'");
                    throw new TesboException("Please enter valid DataSet in: '"+step+"'");
                }
            } catch (Exception e) {
                throw e;
            }

            if(!isDetaSet) {
                try {
                    if (test.get("dataType").toString().equalsIgnoreCase("excel")) {
                        try {
                            textToEnter = dataDrivenParser.getcellValuefromExcel(dataDrivenParser.getExcelUrl(test.get("testsFileName").toString(), test.get("dataSetName").toString()), headerName, (Integer) test.get("row"), Integer.parseInt(dataDrivenParser.SheetNumber(test.get("testsFileName").toString(), test.get("testName").toString())));

                        } catch (StringIndexOutOfBoundsException e) {
                            tesboLogger.stepLog(step);
                            log.error(step);
                            log.error("no string to enter. Create a separate exeception here");
                            tesboLogger.testFailed("no string to enter. Create a separate exeception here");
                            throw e;
                        }
                    }
                } catch (Exception e) {
                    throw e;
                }
                try {
                    if (test.get("dataType").toString().equalsIgnoreCase("global")) {
                        textToEnter = dataDrivenParser.getGlobalDataValue(test.get("testsFileName").toString(), test.get("dataSetName").toString(), headerName);
                    }
                } catch (Exception e) {
                    log.error("Key name " + headerName + " is not found in " + test.get("dataSetName").toString() + " data set");
                    throw new TesboException("Key name " + headerName + " is not found in " + test.get("dataSetName").toString() + " data set");
                }
            }
        } else {
            startPoint = step.indexOf("'") + 1;
            endPoint = step.lastIndexOf("'");
            try {

                textToEnter = step.substring(startPoint, endPoint);

            } catch (StringIndexOutOfBoundsException e) {
                log.error("No string found to enter.");
                throw new TesboException("No string found to enter.");
            }
        }
        return step.replace("{"+headerName+"}", textToEnter).replace("@","").replaceAll("[{,}]","'");
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
