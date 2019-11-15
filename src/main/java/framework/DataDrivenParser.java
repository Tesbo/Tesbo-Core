package framework;

import Execution.TestExecutionBuilder;
import Selenium.Commands;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.*;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import Exception.TesboException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class DataDrivenParser {

    private static final Logger log = LogManager.getLogger(DataDrivenParser.class);
     TesboLogger tesboLogger = new TesboLogger();

    /**
     *
     * @param testsFileName
     * @param dataSetName
     * @param keyName
     * @return
     */
    public String checkDataTypeIsExcelOrGlobleInDataset(String testsFileName, String dataSetName,ArrayList<String> keyName) {
        TestsFileParser testsFileParser = new TestsFileParser();
        StringBuffer testsFile = testsFileParser.readTestsFile(testsFileName);
        String allLines[] = testsFile.toString().split("[\\r\\n]+");
        boolean isExcel = false, isGlobal = false, isDataSetName = false;
        String type = null;
        for (int i = 0; i < allLines.length; i++) {

            if (allLines[i].contains("\"" + dataSetName + "\":")) {
                isDataSetName = true;
            }
            if (isDataSetName) {
                if (allLines[i].contains("\"excelFile\":")) {
                    type = "excel";
                    isExcel = true;
                    break;
                }

                if (allLines[i].contains("}"))
                    break;
            }

        }

        if (!isExcel) {
            isDataSetName = false;
            for (int i = 0; i < allLines.length; i++) {
                if (allLines[i].contains("\"" + dataSetName + "\":")) {
                    isDataSetName = true;
                }
                if (isDataSetName) {
                    if (keyName.size() > 0) {
                        for (String key : keyName) {
                            if(key.contains("DataSet.")){
                                key=key.split("\\.")[2].toString().trim();
                            }
                            isGlobal = false;
                            for (int j = i; j < allLines.length; j++) {
                                if (allLines[j].contains("\"" + key + "\":")) {
                                    type = "global";
                                    isGlobal = true;
                                    break;
                                }
                                if (allLines[j].contains("}")){ break;}
                            }

                            if (!isGlobal){ throw new TesboException(key+" is not found in " + dataSetName + " Data Set");}
                        }

                    }
                    break;
                }
                if (allLines[i].contains("}"))
                    break;
            }

        }

        if(!isDataSetName ) {
            log.error("'" + dataSetName + "' is not found in Data Set");
            throw new TesboException("'" + dataSetName + "' is not found in Data Set");
        }

        if (!isExcel && !isGlobal){ throw new TesboException("Excel File url is not found in " + dataSetName + " Data Set");}

        return type;

    }

    public ArrayList<String> getColumnNameFromTest(ArrayList<String> testSteps){

        ArrayList<String> columnNameList=new ArrayList<String>();
        for(String step:testSteps)
        {
            String[] splitStep;
            if(step.contains("{")&& step.contains("}")){
                if(step.contains("Code:")){
                    splitStep = step.split("\\(")[1].split(",");
                }else {
                    splitStep = step.split("\\s");
                }

                for (String calName : splitStep) {
                    if (calName.contains("{") && calName.contains("}")) {
                        columnNameList.add(calName.replaceAll("[{}()]", "").trim());
                    }
                }

            }
        }
        return columnNameList;
    }

    public String getExcelUrl(String testsFileName,String dataSetName) {
        TestsFileParser testsFileParser=new TestsFileParser();
        StringBuffer testsFile= testsFileParser.readTestsFile(testsFileName);
        String allLines[] = testsFile.toString().split("[\\r\\n]+");
        boolean flag=false;
        String filePath=null;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("\"" + dataSetName + "\""))
                flag = true;

            if (flag){
                if (allLines[i].contains("\"excelFile\":")) {
                    String[] excelUrl = allLines[i].replaceAll("[\"||,]", "").split(":", 2);
                    filePath = excelUrl[1];
                    break;
                }
            }
        }
        return filePath.trim();
    }

    public JSONArray getHeaderValuefromExcel(String url,ArrayList<String> dataSetValues,int sheetNo)
    {
        String filePath=url;
        JSONArray excelData=new JSONArray();
        FileInputStream file = null;
        try {
            file = new FileInputStream(new File(filePath));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(sheetNo);
            Iterator<Row> rowIterator = sheet.iterator();
            ArrayList<String> CellNums=new ArrayList<String>();
            while (rowIterator.hasNext()) {
                Row rows = rowIterator.next();
                if (rows.getCell(0) != null) {
                    DataFormatter formatter = new DataFormatter();
                    if (rows.getRowNum() == 0) {
                        for(String header:dataSetValues){
                            Iterator<Cell> cellIterator = rows.cellIterator();
                            while (cellIterator.hasNext()) {
                                Cell cell = cellIterator.next();
                                if(header.equalsIgnoreCase(formatter.formatCellValue(cell))){
                                    CellNums.add(formatter.formatCellValue(cell) + ":" + cell.getColumnIndex());
                                }
                            }
                        }

                    }
                }
            }
            rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row rows = rowIterator.next();
                if (rows.getCell(0) != null) {
                    DataFormatter formatter = new DataFormatter();
                    if (rows.getRowNum() != 0) {
                        JSONObject dataObj=new JSONObject();
                        for(String callNum:CellNums) {
                            Cell CellNumber = rows.getCell(Integer.parseInt(callNum.split(":")[1]));
                            String CellData = formatter.formatCellValue(CellNumber);
                            dataObj.put(callNum.split(":")[0],CellData);
                        }
                        excelData.add(dataObj);
                    }
                }
            }
            file.close();
        } catch (Exception e) {
            throw new TesboException(e.getMessage());
        }
        return excelData;
    }

    public String getcellValuefromExcel(String url,String headerName,int rowNum,int sheetNo) {
        String filePath=url;
        String CellData=null;
        FileInputStream file = null;
        try {
            file = new FileInputStream(new File(filePath));
            String columnIndex=null;
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(sheetNo);
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row rows = rowIterator.next();
                if (rows.getCell(0) != null) {
                    DataFormatter formatter = new DataFormatter();
                    if (rows.getRowNum() == 0) {
                        Iterator<Cell> cellIterator = rows.cellIterator();
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            if(headerName.equals(formatter.formatCellValue(cell))){
                                columnIndex= String.valueOf(cell.getColumnIndex());
                            }
                        }
                        if(columnIndex==null){
                            log.error("Please enter valid headerName: "+headerName);
                            throw new TesboException("Please enter valid headerName: "+headerName);
                        }
                    }
                }
            }
            rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row rows = rowIterator.next();

                    DataFormatter formatter = new DataFormatter();
                    if (rows.getRowNum() != 0) {
                        if (rows.getRowNum() == rowNum) {
                            Cell CellNumber = rows.getCell(Integer.parseInt(columnIndex));
                             CellData = formatter.formatCellValue(CellNumber);
                        }
                    }

            }

            file.close();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new TesboException(e.getMessage());
        }
        return CellData;
    }

    public JSONObject getGlobalDataValue(String testsFileName, String dataSetName,String keyName) {
        TestsFileParser testsFileParser=new TestsFileParser();
        StringBuffer testsFile= testsFileParser.readTestsFile(testsFileName);
        String[] allLines = testsFile.toString().split("[\\r\\n]+");
        boolean isDataSetName=false;
        JSONObject KeyValue=new JSONObject();
        boolean isVariable=false;

        if(TestExecutionBuilder.dataSetVariable.containsKey(testsFileName)){

            JSONObject dataSetList;
            dataSetList= (JSONObject) TestExecutionBuilder.dataSetVariable.get(testsFileName);

            if(dataSetList.containsKey(dataSetName)){
                JSONObject VariableList;
                VariableList= (JSONObject) dataSetList.get(dataSetName);

                if(VariableList.containsKey(keyName)){
                    isVariable=true;
                    KeyValue.put(keyName,VariableList.get(keyName));
                }
            }
        }

        if(!isVariable) {
            for (int i = 0; i < allLines.length; i++) {
                if (allLines[i].contains("\"" + dataSetName + "\":")) {
                    isDataSetName = true;
                }
                if (isDataSetName) {
                    if (allLines[i].contains("\"" + keyName + "\":")) {
                        try {
                            if (allLines[i].replaceAll(",", "").split(":")[1].equals("\"\"")) {
                                KeyValue.put(keyName,"true");
                                break;
                            } else {
                                KeyValue.put(keyName,allLines[i].replaceAll("[\"|,]", "").split(":")[1].trim());
                                //KeyValue = allLines[i].replaceAll("[\"|,]", "").split(":")[1].trim();
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new TesboException("Key value is not found in dataSet");
                        }
                    }
                    if (allLines[i].contains("}"))
                        break;
                }
            }
        }
        if(KeyValue.size()<=0){
            log.error("Key name " + keyName + " is not found in " + dataSetName + " data set");
            throw new TesboException("Key name " + keyName + " is not found in " + dataSetName + " data set");
        }

        return KeyValue;

    }

    public String SheetNumber(String testsFileName,String testName){
        TestsFileParser testsFileParser=new TestsFileParser();
        String dataSetName = testsFileParser.getTestDataSetByTestsFileAndTestCaseName(testsFileName, testName).split(":")[1];
        int startPoint = dataSetName.indexOf("[") + 1;
        int endPoint = dataSetName.lastIndexOf("]");
        String sheetNo = null;
        if(startPoint!=0 && endPoint!=-1) {
            sheetNo = dataSetName.substring(startPoint, endPoint);
        }
        else{
            sheetNo="0";
        }
        return sheetNo;
    }

    public void setValueInDataSetVariable(WebDriver driver, JSONObject test, String step) throws Exception {
        Commands cmd = new Commands();
        StepParser stepParser=new StepParser();
        GetLocator locator = new GetLocator();
        String variableType="text";

        int startPoint = 0;
        int endPoint = 0;

        if (step.contains("{") && step.contains("}")) {
            startPoint = step.indexOf("{") + 1;
            endPoint = step.lastIndexOf("}");
            String headerName = step.substring(startPoint, endPoint);
            boolean isDetaSet=false;

            JSONArray elementText=new JSONArray();

            if(step.toLowerCase().contains(" define ")){
                elementText.add(stepParser.parseTextToEnter(test, step));
                if(getLocalVariableFromGlobalVariable(test.get("testsFileName").toString(),headerName)){
                    log.error("'"+headerName+"' variable is exist on global data set");
                    throw new TesboException("'"+headerName+"' variable is exist on global data set");
                }
                setLocalVariableValue(headerName,elementText);
            }
            else {

                if(step.toLowerCase().contains(" text ")){
                    if(step.toLowerCase().contains(" list ")){
                        variableType="list";
                        List<WebElement> elementList = cmd.findElements(driver, locator.getLocatorValue(test.get("testsFileName").toString(), stepParser.parseElementName(step)));
                        for (WebElement element : elementList) {
                            elementText.add(element.getText());
                        }
                    }
                    else{
                        elementText.add(cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), stepParser.parseElementName(step))).getText());
                    }
                }
                else if(step.toLowerCase().contains(" size ")){
                    elementText.add(cmd.findElements(driver, locator.getLocatorValue(test.get("testsFileName").toString(), stepParser.parseElementName(step))).size());
                }
                else if(step.toLowerCase().contains(" page title ")){
                    elementText.add(driver.getTitle());
                }
                else if(step.toLowerCase().contains(" current url ")){
                    elementText.add(driver.getCurrentUrl());
                }
                else if(step.toLowerCase().contains(" attribute ")){
                    elementText.add(cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), stepParser.parseElementName(step))).getAttribute(stepParser.parseTextToEnter(test, step)));
                }
                else if(step.toLowerCase().contains(" css value ")){
                    elementText.add(cmd.findElement(driver, locator.getLocatorValue(test.get("testsFileName").toString(), stepParser.parseElementName(step))).getCssValue(stepParser.parseTextToEnter(test, step)));
                }

                try {
                    if (headerName.contains("DataSet.")) {
                        isDetaSet=true;
                        try {
                            String dataSet[]=headerName.split("\\.");
                            if(dataSet.length==3) {
                                getGlobalDataValue(test.get("testsFileName").toString(), dataSet[1],dataSet[2]);
                                setVariableValue(test.get("testsFileName").toString(), dataSet[1], dataSet[2], elementText, variableType);
                            }
                            else{
                                log.info("Please enter DataSet in: '"+step+"'");
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
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    tesboLogger.testFailed(sw.toString());
                    log.error(sw.toString());
                    throw e;
                }

                if(!isDetaSet) {
                    try {
                        if (test.get("dataType").toString().equalsIgnoreCase("global")) {
                            getGlobalDataValue(test.get("testsFileName").toString(), test.get("dataSetName").toString(),headerName);
                            setVariableValue(test.get("testsFileName").toString(), test.get("dataSetName").toString(),headerName, elementText,variableType);
                        }
                    } catch (Exception e) {
                        log.error("Key name " + headerName + " is not found in " + test.get("dataSetName").toString() + " data set");
                        throw new TesboException("Key name " + headerName + " is not found in " + test.get("dataSetName").toString() + " data set");
                    }
                }
            }
        }
    }

    private void setVariableValue(String testsFileName, String dataSetName, String keyName, JSONArray elementText, String variableType){
        JSONObject variables=new JSONObject();
        JSONObject dataSetNames=new JSONObject();
        JSONObject testDataSet=new JSONObject();
        if(TestExecutionBuilder.dataSetVariable.size()==0) {
            if(variableType.equals("list")){ variables.put(keyName, elementText); }
            else{variables.put(keyName, elementText.get(0));}
            dataSetNames.put(dataSetName, variables);
            TestExecutionBuilder.dataSetVariable.put(testsFileName, dataSetNames);
        }
        else {
            if(TestExecutionBuilder.dataSetVariable.containsKey(testsFileName)){
                testDataSet= (JSONObject) TestExecutionBuilder.dataSetVariable.get(testsFileName);
                if(testDataSet.containsKey(dataSetName)){
                    dataSetNames= (JSONObject) testDataSet.get(dataSetName);
                    if(variableType.equals("list")){ dataSetNames.put(keyName, elementText); }
                    else{ dataSetNames.put(keyName, elementText.get(0)); }
                    testDataSet.put(dataSetName, dataSetNames);
                    TestExecutionBuilder.dataSetVariable.put(testsFileName, testDataSet);
                }else{
                    if(variableType.equals("list")){ variables.put(keyName, elementText); }
                    else{variables.put(keyName, elementText.get(0));}
                    testDataSet.put(dataSetName, variables);
                    TestExecutionBuilder.dataSetVariable.put(testsFileName, testDataSet);
                }
            }
            else{
                if(variableType.equals("list")){ variables.put(keyName, elementText); }
                else{variables.put(keyName, elementText.get(0));}
                dataSetNames.put(dataSetName, variables);
                TestExecutionBuilder.dataSetVariable.put(testsFileName, dataSetNames);
            }
        }
        System.out.println("=====>: "+TestExecutionBuilder.dataSetVariable);
    }

    public void setLocalVariableValue(String keyName,JSONArray elementText){

        TestExecutor.localVariable.put(keyName,elementText.get(0));

    }

    public boolean getLocalVariableFromGlobalVariable(String testsFileName,String keyName){
        boolean isVariableExist=false;
        JSONObject dataSetList = (JSONObject) TestExecutionBuilder.dataSetVariable.get(testsFileName);
        for(Object dataSet:dataSetList.keySet()){
            JSONObject dataSetValues=(JSONObject) dataSetList.get(dataSet);
            if(dataSetValues.containsKey(keyName)){
                isVariableExist=true;
            }
        }

        if(!isVariableExist){

            TestsFileParser testsFileParser=new TestsFileParser();
            StringBuffer testsFile= testsFileParser.readTestsFile(testsFileName);
            String[] allLines = testsFile.toString().split("[\\r\\n]+");
            for (int i = 0; i < allLines.length; i++) {
                boolean isDataSetName=false;
                if (allLines[i].contains("DataSet:")) {
                    isDataSetName = true;
                }
                if(isDataSetName) {
                    if (allLines[i].contains("\"" + keyName + "\":")) {
                        isVariableExist = true;
                        break;
                    }
                    if (allLines[i].contains("Test:"))
                        break;
                }
            }
        }

        return isVariableExist;
    }
}
