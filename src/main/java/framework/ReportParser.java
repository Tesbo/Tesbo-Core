package framework;

import Execution.Tesbo;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;

import Exception.TesboException;

public class ReportParser {
    private static final Logger log = LogManager.getLogger(Tesbo.class);
    /**
     * @return
     * @throws Exception
     * @Description : Get test file names.
     */
    public JSONArray getSuiteName() {
        GetConfiguration configuration = new GetConfiguration();
        TestsFileParser testsFileParser = new TestsFileParser();
        String directoryPath = configuration.getTestsDirectory();

        JSONArray testsFileList = testsFileParser.getTestFiles(directoryPath);
        JSONArray allTestFile = new JSONArray();

        for (int i = 0; i < testsFileList.size(); i++) {
            File name = new File(testsFileList.get(i).toString());
            allTestFile.add(name.getName());
        }

        return allTestFile;
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
            if(TestExecutor.localVariable.containsKey(headerName) | step.toLowerCase().contains("define")){
                if(step.toLowerCase().contains("define") | step.toLowerCase().contains("set")){
                    step=step.replace("@","");
                    return step.replaceAll("[{,}]", "'");
                }else {
                    textToEnter = TestExecutor.localVariable.get(headerName).toString();
                    step=step.replace("@","");
                    return step.replace(headerName, textToEnter).replaceAll("[{,}]", "'");
                }
            }
            else {
                try {
                    //if (headerName.contains("DataSet.")) {
                    if (headerName.split("\\.").length==3) {
                        isDetaSet = true;
                        try {
                            String[] dataSet = headerName.split("\\.");
                            if (dataSet.length == 3) {
                                ArrayList<String> keyName = new ArrayList<>();
                                keyName.add(dataSet[2]);
                                String DataSetType= dataDrivenParser.checkDataTypeIsExcelOrGlobleInDataset(dataSet[1],keyName);
                                if(DataSetType.equals("list") || DataSetType.equals("excel")){
                                    log.error("Array list and Excel data set can't be use in inline data set '"+ headerName +"'.");
                                    throw new TesboException("Array list and Excel data set can't be use in inline data set '"+ headerName +"'.");
                                }
                                if ((step.toLowerCase().contains("get ") && (step.toLowerCase().contains(" set ") | step.toLowerCase().contains(" put ") | step.toLowerCase().contains(" assign ")))) {
                                    textToEnter = dataSet[2];
                                } else {
                                    textToEnter = dataDrivenParser.getGlobalDataValue(test.get("testsFileName").toString(), dataSet[0], dataSet[1], dataSet[2]).get(dataSet[2]).toString();
                                }
                            } else {
                                log.error("Please enter DataSet in: '" + step + "'");
                                throw new TesboException("Please enter DataSet in: '" + step + "'");
                            }

                        } catch (StringIndexOutOfBoundsException e) {
                            log.error("'"+headerName+"' Variable is not define.");
                            throw new TesboException("'"+headerName+"' Variable is not define.");
                        }
                    }
                } catch (Exception e) {
                    throw e;
                }

                if (!isDetaSet) {
                    try {

                        if (test.get("dataType").toString().equalsIgnoreCase("excel")) {
                            try {
                                textToEnter = dataDrivenParser.getcellValuefromExcel(dataDrivenParser.getExcelUrl(test.get("dataSetName").toString()), headerName, (Integer) test.get("row"), Integer.parseInt(dataDrivenParser.SheetNumber(test.get("testsFileName").toString(), test.get("testName").toString())));

                            } catch (StringIndexOutOfBoundsException e) {
                                tesboLogger.stepLog(step);
                                log.error(step);
                                log.error("no string to enter. Create a separate exeception here");
                                tesboLogger.testFailed("no string to enter. Create a separate exeception here");
                                throw e;
                            }
                        }
                    } catch (Exception e) {
                        log.error("'"+headerName+"' Variable is not define or DataSet is not define in test.");
                        throw new TesboException("'"+headerName+"' Variable is not define or DataSet is not define in test.");
                    }
                    try {
                        if (test.get("dataType").toString().equalsIgnoreCase("global")) {
                            if (step.toLowerCase().contains("get ") && (step.toLowerCase().contains(" set ") | step.toLowerCase().contains(" put ") | step.toLowerCase().contains(" assign "))) {
                                textToEnter = headerName;
                            } else {
                                textToEnter = dataDrivenParser.getGlobalDataValue(test.get("testsFileName").toString(), null, test.get("dataSetName").toString(), headerName).get(headerName).toString();
                            }
                        }
                    } catch (Exception e) {
                        log.error("Key name " + headerName + " is not found in " + test.get("dataSetName").toString() + " data set");
                        throw new TesboException("Key name " + headerName + " is not found in " + test.get("dataSetName").toString() + " data set");
                    }
                    if(test.get("dataType").toString().equalsIgnoreCase("list")){
                        textToEnter=dataDrivenParser.getDataSetListValue(test.get("dataSetName").toString(), headerName,Integer.parseInt(test.get("row").toString()));
                    }
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
        step=step.replace("@","");
        return step.replace(headerName, textToEnter).replaceAll("[{,}]","'");
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
