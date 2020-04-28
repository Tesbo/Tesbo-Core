package framework;

import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;

import Exception.TesboException;

public class ReportParser {
    private static final Logger log = LogManager.getLogger(ReportParser.class);
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

            file.close();
        } catch (Exception e) {
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
        String regex="[{,}]";
        String testsFileName=test.get("testsFileName").toString();
        String dataType=test.get("dataType").toString();
        String dataSetName=test.get("dataSetName").toString();

        if (step.contains("{") && step.contains("}")) {
            startPoint = step.indexOf('{') + 1;
            endPoint = step.lastIndexOf('}');
            headerName = step.substring(startPoint, endPoint);

            boolean isDetaSet=false;
            if(TestExecutor.localVariable.containsKey(headerName) || step.toLowerCase().contains("define")){
                if(step.toLowerCase().contains("define") || step.toLowerCase().contains("set")){
                    step=step.replace("@","");
                    return step.replaceAll(regex, "'");
                }else {
                    textToEnter = TestExecutor.localVariable.get(headerName).toString();
                    step=step.replace("@","");
                    return step.replace(headerName, textToEnter).replaceAll(regex, "'");
                }
            }
            else {
                try {
                    if (headerName.split("\\.").length==3) {
                        isDetaSet = true;
                        try {
                            String[] dataSet = headerName.split("\\.");
                            if (dataSet.length == 3) {
                                ArrayList<String> keyName = new ArrayList<>();
                                keyName.add(dataSet[2]);
                                String dataSetType= dataDrivenParser.checkDataTypeIsExcelOrGlobleInDataset(dataSet[1],keyName);
                                if(dataSetType.equals("list") || dataSetType.equals("excel")){
                                    String errorMsg="Array list and Excel data set can't be use in inline data set '"+ headerName +"'.";
                                    log.error(errorMsg);
                                    throw new TesboException(errorMsg);
                                }
                                if ((step.toLowerCase().contains("get ") && (step.toLowerCase().contains(" set ") || step.toLowerCase().contains(" put ") || step.toLowerCase().contains(" assign ")))) {
                                    textToEnter = dataSet[2];
                                } else {
                                    textToEnter = dataDrivenParser.getGlobalDataValue(testsFileName, dataSet[0], dataSet[1], dataSet[2]).get(dataSet[2]).toString();
                                }
                            } else {
                                String errorMsg="Please enter DataSet in: '" + step + "'";
                                log.error(errorMsg);
                                throw new TesboException(errorMsg);
                            }

                        } catch (StringIndexOutOfBoundsException e) {
                            String errorMsg="'"+headerName+"' Variable is not define.";
                            log.error(errorMsg);
                            throw new TesboException(errorMsg);
                        }
                    }
                } catch (Exception e) {
                    throw e;
                }

                if (!isDetaSet) {
                    try {

                        if (dataType.equalsIgnoreCase("excel")) {
                            try {
                                textToEnter = dataDrivenParser.getcellValuefromExcel(dataDrivenParser.getExcelUrl(dataSetName), headerName, (Integer) test.get("row"), Integer.parseInt(dataDrivenParser.sheetNumber(testsFileName, test.get("testName").toString())));

                            } catch (StringIndexOutOfBoundsException e) {
                                tesboLogger.stepLog(step);
                                log.error(step);
                                log.error("no string to enter. Create a separate exeception here");
                                tesboLogger.testFailed("no string to enter. Create a separate exeception here");
                                throw e;
                            }
                        }
                    } catch (Exception e) {
                        String errorMsg="'"+headerName+"' Variable is not define or DataSet is not define in test.";
                        log.error(errorMsg);
                        throw new TesboException(errorMsg);
                    }
                    try {
                        if (dataType.equalsIgnoreCase("global")) {
                            boolean isStep= (step.toLowerCase().contains(" set ") || step.toLowerCase().contains(" put ") || step.toLowerCase().contains(" assign "));
                            if (step.toLowerCase().contains("get ") && isStep) {
                                textToEnter = headerName;
                            } else {
                                textToEnter = dataDrivenParser.getGlobalDataValue(testsFileName, null, dataSetName, headerName).get(headerName).toString();
                            }
                        }
                    } catch (Exception e) {
                        String errorMsg="Key name " + headerName + " is not found in " + dataSetName + " data set";
                        log.error(errorMsg);
                        throw new TesboException(errorMsg);
                    }
                    if(dataType.equalsIgnoreCase("list")){
                        textToEnter=dataDrivenParser.getDataSetListValue(dataSetName, headerName,Integer.parseInt(test.get("row").toString()));
                    }
                }
            }
        } else {
            startPoint = step.indexOf('\'') + 1;
            endPoint = step.lastIndexOf('\'');
            try {

                textToEnter = step.substring(startPoint, endPoint);

            } catch (StringIndexOutOfBoundsException e) {
                log.error("No string found to enter.");
                throw new TesboException("No string found to enter.");
            }
        }
        step=step.replace("@","");
        return step.replace("{"+headerName+"}", "{"+textToEnter+"}").replaceAll(regex,"'");
    }

    /**
     * @auther : Ankit Mistry
     * @param stepReportObject
     * @param step
     * @return
     */
    public JSONObject addScreenshotUrlInReport(JSONObject stepReportObject, String step)  {
        if (step.toLowerCase().contains("capture screenshot") && StepParser.screenShotURL !=null) {
            stepReportObject.remove("steps");
            stepReportObject.put("steps", "Screenshot: <a href=\"../"+StepParser.screenShotURL+"\" target=\"_blank\">/"+StepParser.screenShotURL+"</a>");
        }
        return stepReportObject;
    }

}
