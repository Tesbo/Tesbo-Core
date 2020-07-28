package framework;

import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;

public class ReportParser {
    private static final Logger log = LogManager.getLogger(ReportParser.class);
    String regex="[{,}]";
    CommonMethods commonMethods=new CommonMethods();
    DataDrivenParser dataDrivenParser=new DataDrivenParser();
    TesboLogger tesboLogger = new TesboLogger();


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
            log.error("");
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

        String textToEnter = "";
        int startPoint = 0;
        int endPoint = 0;
        String headerName="";

        String testsFileName=test.get("testsFileName").toString();

        if (step.contains("{") && step.contains("}")) {
            startPoint = step.indexOf('{') + 1;
            endPoint = step.lastIndexOf('}');
            headerName = step.substring(startPoint, endPoint);

            boolean isDataSet=false;

            if(TestExecutor.localVariable.containsKey(headerName) || step.toLowerCase().contains("define")){
                return getStepWhenItHasLocalVariable(step,headerName);
            }
            else {
                try {
                    if (headerName.split("\\.").length==3) {
                        isDataSet = true;
                        textToEnter= getTextValueOfStepFromDataSet(headerName,step,testsFileName);
                    }
                } catch (Exception e) {
                    throw e;
                }

                textToEnter=getTextValueWhenStepHesNotDataSetValue(isDataSet,test,headerName,step,testsFileName);
            }
        } else {
            textToEnter=getTextValueOfStepHasPlainText(step);
        }
        step=step.replace("@","");
        return step.replace("{"+headerName+"}", "{"+textToEnter+"}").replaceAll(regex,"'");
    }

    public String getTextValueWhenStepHesNotDataSetValue(boolean isDataSet,JSONObject test,String headerName,String step,String testsFileName){
        String textToEnter = "";

        if (!isDataSet) {
            String dataType=test.get("dataType").toString();
            String dataSetName=test.get("dataSetName").toString();
            try {

                textToEnter=getTextValueOfStepWhenDataSetIsExcel(headerName,step,testsFileName,dataType,dataSetName,test);
            } catch (Exception e) {
                commonMethods.throwTesboException("'"+headerName+"' Variable is not define or DataSet is not define in test.",log);
            }
            try {
                textToEnter=getTextValueOfStepWhenDataSetIsGlobal(headerName,step,testsFileName,dataType,dataSetName);
            } catch (Exception e) {
                commonMethods.throwTesboException("Key name " + headerName + " is not found in " + dataSetName + " data set",log);
            }
            if(dataType.equalsIgnoreCase("list")){
                textToEnter=dataDrivenParser.getDataSetListValue(dataSetName, headerName,Integer.parseInt(test.get("row").toString()));
            }
        }

        return textToEnter;
    }

    public String getStepWhenItHasLocalVariable(String step,String headerName){
        if(step.toLowerCase().contains("define") || step.toLowerCase().contains("set")){
            step=step.replace("@","");
            return step.replaceAll(regex, "'");
        }else {
            String textToEnter = TestExecutor.localVariable.get(headerName).toString();
            step=step.replace("@","");
            return step.replace(headerName, textToEnter).replaceAll(regex, "'");
        }
    }

    public String getTextValueOfStepFromDataSet(String headerName,String step,String testsFileName){
        String textToEnter = "";
        try {
            String[] dataSet = headerName.split("\\.");
            if(dataSet.length == 3) {
                ArrayList<String> keyName = new ArrayList<>();
                keyName.add(dataSet[2]);
                String dataSetType= dataDrivenParser.checkDataTypeIsExcelOrGlobleInDataset(dataSet[1],keyName);
                if(dataSetType.equals("list") || dataSetType.equals("excel")){
                    commonMethods.throwTesboException("Array list and Excel data set can't be use in inline data set '"+ headerName +"'.",log);
                }
                if ((step.toLowerCase().contains("get ") && (step.toLowerCase().contains(" set ") || step.toLowerCase().contains(" put ") || step.toLowerCase().contains(" assign ")))) {
                    textToEnter = dataSet[2];
                } else {
                    textToEnter = dataDrivenParser.getGlobalDataValue(testsFileName, dataSet[0], dataSet[1], dataSet[2]).get(dataSet[2]).toString();
                }
            } else {
                commonMethods.throwTesboException("Please enter DataSet in: '" + step + "'",log);
            }

        } catch (StringIndexOutOfBoundsException e) {
            commonMethods.throwTesboException("'"+headerName+"' Variable is not define.",log);
        }
        return textToEnter;
    }

    public String getTextValueOfStepWhenDataSetIsExcel(String headerName,String step,String testsFileName,String dataType,String dataSetName,JSONObject test){
        String textToEnter = "";

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

        return textToEnter;
    }

    public String getTextValueOfStepWhenDataSetIsGlobal(String headerName,String step,String testsFileName,String dataType,String dataSetName){
        String textToEnter = "";

        if (dataType.equalsIgnoreCase("global")) {
            boolean isStep= (step.toLowerCase().contains(" set ") || step.toLowerCase().contains(" put ") || step.toLowerCase().contains(" assign "));
            if (step.toLowerCase().contains("get ") && isStep) {
                textToEnter = headerName;
            } else {
                textToEnter = dataDrivenParser.getGlobalDataValue(testsFileName, null, dataSetName, headerName).get(headerName).toString();
            }
        }

        return textToEnter;
    }

    public String getTextValueOfStepHasPlainText(String step){
        String textToEnter = "";

        int startPoint = step.indexOf('\'') + 1;
        int endPoint = step.lastIndexOf('\'');
        try {
            textToEnter = step.substring(startPoint, endPoint);

        } catch (StringIndexOutOfBoundsException e) {
            commonMethods.throwTesboException("No string found to enter.",log);
        }

        return textToEnter;
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
