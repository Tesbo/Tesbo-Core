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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Exception.TesboException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class DataDrivenParser {

    private static final Logger log = LogManager.getLogger(DataDrivenParser.class);
     TesboLogger tesboLogger = new TesboLogger();
     String excelFileDataSet="excelFile";
     String notFoundMsg="' is not found in '";
     CommonMethods commonMethods=new CommonMethods();
     boolean isJSONArray=false;

        /**
         *
         * @param dataSetName
         * @param keyName
         * @return
         */
    public String checkDataTypeIsExcelOrGlobleInDataset(String dataSetName,List<String> keyName) {

        String type = null;
        JSONArray dataSetFileList=getDataSetFileList();

        commonMethods.verifyJsonArrayIsEmpty(dataSetFileList,"DataSet directory is empty.",log);

        for (Object dataSetFile : dataSetFileList) {
            JSONObject main = Utility.loadJsonFile(dataSetFile.toString());

            if(main.containsKey(dataSetName)){
                isJSONArray=false;
                if(main.get(dataSetName) instanceof JSONArray){
                    isJSONArray=true;
                }
                else if(main.get(dataSetName) instanceof JSONObject){
                    type=checkDataSetDataTypeWhenItHasJsonObject(main,dataSetName,keyName);
                }

                if(isJSONArray){
                    type=checkDataSetDataTypeWhenItHasJsonArray(main,dataSetName,keyName);
                }
            }
            if(type != null){
                break;
            }
        }

        if(type==null) {
            commonMethods.throwTesboException("'" + dataSetName + "' is not found in any Data Set file",log);
        }

        return type;
    }

    public String checkDataSetDataTypeWhenItHasJsonObject(JSONObject main, String dataSetName, List<String> keyName) {
        String type=null;
        if(((JSONObject) main.get(dataSetName)).containsKey(excelFileDataSet)){
            type = "excel";
        }
        else {

            JSONObject dataSetList = (JSONObject) main.get(dataSetName);

            isJSONArray = isDataSetListHasJsonArray(dataSetList, keyName);

            if (!isJSONArray) {
                type = checkDataSetDataTypeIsGlobalOrNot(dataSetList, dataSetName, keyName);
            }
        }

       return  type;
    }

    public boolean isDataSetListHasJsonArray(JSONObject dataSetList, List<String> keyName) {
        for(String key: keyName){
            if(dataSetList.get(key) instanceof JSONArray){
                return true;
            }
        }
        return false;
    }

    public String checkDataSetDataTypeIsGlobalOrNot(JSONObject dataSetList, String dataSetName, List<String> keyName) {
        String type=null;
        for(String key: keyName){
            if(!(dataSetList.get(key) instanceof JSONArray)){
                if(dataSetList.containsKey(key)){
                    type=  "global";
                }
                else{
                    if(key.split("\\.").length!=3) {
                        commonMethods.throwTesboException("'"+key + notFoundMsg + dataSetName + "' Data Set",log);
                    }
                }
            }
            else{
                commonMethods.throwTesboException("'"+key + "' key has array list value.",log);
            }
        }

        return  type;
    }

    public String checkDataSetDataTypeWhenItHasJsonArray(JSONObject main, String dataSetName, List<String> keyName) {
        String type=null;
        if(main.get(dataSetName) instanceof JSONArray){
            JSONArray dataSetList= (JSONArray) main.get(dataSetName);
            type=verifyJsonArrayListHasSameNumberOfArrayListAndAllKeyValuePresent(dataSetList,dataSetName,keyName);
        }
        else{
            type=verifyJsonObjectHasJsonArrayListAndAllObjectHasSameNumberOfArrayListAndAllKeyValuePresent(main,dataSetName,keyName);
        }
        return  type;
    }

    public String verifyJsonArrayListHasSameNumberOfArrayListAndAllKeyValuePresent(JSONArray dataSetList, String dataSetName, List<String> keyName) {
        String type=null;
        int arraySize=0;
            for(String key: keyName){
                for(Object DataSet: dataSetList){
                    if(arraySize==0){
                        arraySize=((JSONObject)DataSet).entrySet().size();
                    }
                    else {
                        if(arraySize!=((JSONObject)DataSet).entrySet().size()){
                            commonMethods.throwTesboException("'"+dataSetName+"' data set has not same number of value in all array list.",log);
                        }
                    }
                    if(((JSONObject)DataSet).containsKey(key)){
                        type = "list";
                    }
                    else{
                        if(key.split("\\.").length!=3) {
                            commonMethods.throwTesboException("'"+key + notFoundMsg + dataSetName + "' Data Set'",log);
                        }
                    }
                }
            }
        return type;
    }

    public String verifyJsonObjectHasJsonArrayListAndAllObjectHasSameNumberOfArrayListAndAllKeyValuePresent(JSONObject main, String dataSetName, List<String> keyName) {
        String type=null;
        int arraySize=0;
        for(String key: keyName) {
            JSONObject dataSetList = (JSONObject) main.get(dataSetName);
            if (dataSetList.containsKey(key)) {
                if (((JSONObject) main.get(dataSetName)).get(key) instanceof JSONArray){
                    if(arraySize==0){
                        JSONArray arrayList=((JSONArray) ((JSONObject) main.get(dataSetName)).get(key));
                        arraySize=arrayList.size();
                    }
                    else {
                        JSONArray arrayList=((JSONArray) ((JSONObject) main.get(dataSetName)).get(key));
                        if(arraySize != arrayList.size()){
                            commonMethods.throwTesboException("'"+key + "' key array size is different than others.",log);
                        }
                    }
                    type = "list";
                }
                else {
                    commonMethods.throwTesboException("'"+key + "' key has not JSONArray value, Please enter array list in it.",log);
                }
            } else {
                if (key.split("\\.").length != 3) {
                    commonMethods.throwTesboException("'"+key + notFoundMsg + dataSetName + "' Data Set",log);
                }
            }
        }
        return type;
    }

    /**
         * @auther : Ankit Mistry
         * @lastModifiedBy :
         * @return : List of data set file list
         */
    public JSONArray getDataSetFileList() {
        GetConfiguration getConfiguration = new GetConfiguration();
        JSONArray dataSetFileList = new JSONArray();

        File dataSetDirectory =new File(getConfiguration.getDataSetDirectory());
        String errorMsg="DataSet directory is not found in project: '"+ dataSetDirectory.getAbsolutePath()+"'";
        if(!dataSetDirectory.exists()){
            commonMethods.throwTesboException(errorMsg,log);
        }
        try (Stream<Path> paths = Files.walk(Paths.get(dataSetDirectory.getAbsolutePath()))) {

            dataSetFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            commonMethods.throwTesboException(errorMsg,log);
        }


        return dataSetFileList;
    }

    public List<String> getColumnNameFromTest(List<String> testSteps){

        List<String> columnNameList = new LinkedList<>();
        for(String step:testSteps)
        {
            String[] splitStep;
            boolean isDefine=false;
            if(step.toLowerCase().contains("define")) {
                isDefine=true;
            }
            if (step.contains("{") && step.contains("}")) {
                if (step.startsWith("Code: ")) {
                    splitStep = step.split("\\(")[1].split(",");
                } else {
                    splitStep = step.split("\\s");
                }
                columnNameList.add(getColumnNameList(splitStep,isDefine));

            }
        }
        return columnNameList;
    }

    public String getColumnNameList(String[] splitStep, boolean isDefine){

        String columnName = null;
        JSONArray defineList=new JSONArray();

        for (String calName : splitStep) {
            if (calName.contains("{") && calName.contains("}")) {
                if(isDefine){
                    defineList.add(calName.replaceAll("[{}()]", "").trim());
                }else {
                    boolean flag=false;
                    for(int i=0;i<defineList.size();i++){
                        if(calName.toLowerCase().contains(defineList.get(i).toString().toLowerCase())){
                            flag=true;
                        }
                    }
                    if(!flag) {
                        columnName=calName.replaceAll("[{}()]", "").trim();
                    }
                }
            }
        }
        return columnName;
    }

    public String getExcelUrl(String dataSetName) {

        JSONArray dataSetFileList=getDataSetFileList();
        String filePath=null;

        for (Object dataSetFile : dataSetFileList) {
            JSONObject main = Utility.loadJsonFile(dataSetFile.toString());
            if (main.get(dataSetName) != null) {
                String errorMsg="'excelFile' is not found in " + dataSetName + " Data Set";
                if (((JSONObject) main.get(dataSetName)).get(excelFileDataSet) == null) {
                    commonMethods.throwTesboException(errorMsg,log);
                }
                filePath = ((JSONObject) main.get(dataSetName)).get(excelFileDataSet).toString();
                if(filePath.equals("")){
                    commonMethods.throwTesboException(errorMsg,log);
                }
                break;
            }
        }

        return filePath;
    }

    public JSONArray getHeaderValuefromExcel(String url,List<String> dataSetValues,int sheetNo)
    {
        String filePath=url;
        JSONArray excelData;
        FileInputStream file = null;
        try {
            file = new FileInputStream(new File(filePath));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(sheetNo);
            Iterator<Row> rowIterator = sheet.iterator();
            List<String> cellNums=getHeaderName(rowIterator, dataSetValues);

            rowIterator = sheet.iterator();
            excelData=getValueFromHeaderName(rowIterator,cellNums);
            file.close();
        } catch (Exception e) {
            throw new TesboException(e.getMessage());
        }
        return excelData;
    }

    public List<String> getHeaderName(Iterator<Row> rowIterator, List<String> dataSetValues){
        ArrayList<String> cellNums=new ArrayList<>();
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
                                cellNums.add(formatter.formatCellValue(cell) + ":" + cell.getColumnIndex());
                            }
                        }
                    }
                }
            }
        }
        return cellNums;
    }

    public JSONArray getValueFromHeaderName(Iterator<Row> rowIterator, List<String> cellNums){
        JSONArray excelData=new JSONArray();
        while (rowIterator.hasNext()) {
            Row rows = rowIterator.next();
            if (rows.getCell(0) != null) {
                DataFormatter formatter = new DataFormatter();
                if (rows.getRowNum() != 0) {
                    JSONObject dataObj=new JSONObject();
                    for(String callNum:cellNums) {
                        Cell cellNumber = rows.getCell(Integer.parseInt(callNum.split(":")[1]));
                        String cellData = formatter.formatCellValue(cellNumber);
                        dataObj.put(callNum.split(":")[0],cellData);
                    }
                    excelData.add(dataObj);
                }
            }
        }
        return excelData;
    }



    public String getcellValuefromExcel(String url,String headerName,int rowNum,int sheetNo) {
        String filePath=url;
        String cellData=null;
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
                            String errorMsg="Please enter valid headerName: "+headerName;
                            log.error(errorMsg);
                            throw new TesboException(errorMsg);
                        }
                    }
                }
            }
            rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row rows = rowIterator.next();

                    DataFormatter formatter = new DataFormatter();
                    if (rows.getRowNum() != 0 && rows.getRowNum() == rowNum) {
                        Cell cellNumber = rows.getCell(Integer.parseInt(columnIndex));
                        cellData = formatter.formatCellValue(cellNumber);
                    }

            }

            file.close();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new TesboException(e.getMessage());
        }
        return cellData;
    }

    public JSONObject getGlobalDataValue(String testsFileName,String dataSetFileName, String dataSetName,String keyName) {
        JSONObject keyValue=new JSONObject();
        boolean isVariable=false;

        if(TestExecutionBuilder.dataSetVariable.containsKey(testsFileName)){

            JSONObject dataSetList;
            dataSetList= (JSONObject) TestExecutionBuilder.dataSetVariable.get(testsFileName);

            if(dataSetList.containsKey(dataSetName)){
                JSONObject variableList;
                variableList= (JSONObject) dataSetList.get(dataSetName);

                if(variableList.containsKey(keyName)){
                    isVariable=true;
                    keyValue.put(keyName,variableList.get(keyName));
                }
            }
        }

        if(!isVariable) {
            JSONArray dataSetFileList=getDataSetFileList();
            boolean isInline=false;
            boolean isGlobal=true;
            for (Object dataSetFile : dataSetFileList) {

                if(dataSetFileName!=null){
                    isGlobal=false;
                    String pattern = Pattern.quote(System.getProperty("file.separator"));
                    int size=dataSetFile.toString().split(pattern).length;
                   String fileName= dataSetFile.toString().split(pattern)[size-1];
                    if(dataSetFileName.equals(fileName.split("\\.")[0])){
                        isInline=true;
                    }
                }

                if(isInline || isGlobal) {
                    JSONObject main = Utility.loadJsonFile(dataSetFile.toString());
                    if (main.get(dataSetName) != null) {
                        if (((JSONObject) main.get(dataSetName)).get(keyName) == null) {
                            String errorMsg="'" + keyName + "' is not found in " + dataSetName + " Data Set";
                            log.error(errorMsg);
                            throw new TesboException(errorMsg);
                        }
                        if (((JSONObject) main.get(dataSetName)).get(keyName).toString().equals("")) {
                            keyValue.put(keyName, "true");
                        } else {
                            keyValue.put(keyName, ((JSONObject) main.get(dataSetName)).get(keyName));
                        }
                        break;
                    }
                    else{
                        if(isInline){
                            String errorMsg="'" + dataSetName + notFoundMsg+ dataSetFileName +".json' DataSet.";
                            log.error(errorMsg);
                            throw new TesboException(errorMsg);
                        }
                    }
                }
            }
            if(dataSetFileName!=null && !isInline){
                String errorMsg="'" + dataSetFileName + ".json' is not found in DataSet directory";
                log.error(errorMsg);
                throw new TesboException(errorMsg);
            }

        }
        if(keyValue.size()<=0){
            String errorMsg="Key name " + keyName + " is not found in " + dataSetName + " data set";
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        return keyValue;

    }

    public String sheetNumber(String testsFileName,String testName){
        TestsFileParser testsFileParser=new TestsFileParser();
        String dataSetName = testsFileParser.getTestDataSetByTestsFileAndTestCaseName(testsFileName, testName).split(":")[1];
        int startPoint = dataSetName.indexOf('[') + 1;
        int endPoint = dataSetName.lastIndexOf(']');
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
        String testsFileName= test.get("testsFileName").toString();
        String dataSetName=test.get("dataSetName").toString();

        int startPoint = 0;
        int endPoint = 0;

        if (step.contains("{") && step.contains("}")) {
            startPoint = step.indexOf('{') + 1;
            endPoint = step.lastIndexOf('}');
            String headerName = step.substring(startPoint, endPoint);

            boolean isDetaSet=false;

            JSONArray elementText=new JSONArray();

            if(step.toLowerCase().contains(" define ")){
                if(step.toLowerCase().contains(" set ") || step.toLowerCase().contains(" put ") || step.toLowerCase().contains(" assign ")) {
                    elementText.add(stepParser.parseTextToEnter(test, step));
                    if (getLocalVariableFromGlobalVariable(testsFileName, headerName)) {
                        String errorMsg="'" + headerName + "' variable is exist on global data set";
                        log.error(errorMsg);
                        throw new TesboException(errorMsg);
                    }
                }
                else{
                    elementText.add("");
                }
                setLocalVariableValue(headerName,elementText);
            }
            else {
                if(test.containsKey("dataType") && (dataSetName.equals("excel") || dataSetName.equals("list"))) {
                    String errorMsg="Array list and Excel DataSet can't be use in set variable '" + step + "'";
                    log.error(errorMsg);
                    throw new TesboException(errorMsg);
                }

                if(step.toLowerCase().contains(" text ")){
                    if(step.toLowerCase().contains(" list ")){
                        variableType="list";
                        List<WebElement> elementList = cmd.findElements(driver, locator.getLocatorValue(testsFileName, stepParser.parseElementName(step)));
                        for (WebElement element : elementList) {
                            elementText.add(element.getText());
                        }
                    }
                    else{
                        elementText.add(cmd.findElement(driver, locator.getLocatorValue(testsFileName, stepParser.parseElementName(step))).getText());
                    }
                }
                else if(step.toLowerCase().contains(" size ")){
                    elementText.add(cmd.findElements(driver, locator.getLocatorValue(testsFileName, stepParser.parseElementName(step))).size());
                }
                else if(step.toLowerCase().contains(" page title ")){
                    elementText.add(driver.getTitle());
                }
                else if(step.toLowerCase().contains(" current url ")){
                    elementText.add(driver.getCurrentUrl());
                }
                else if(step.toLowerCase().contains(" attribute ")){
                    WebElement element=cmd.findElement(driver, locator.getLocatorValue(testsFileName, stepParser.parseElementName(step)));
                    if(element.getAttribute(stepParser.parseTextToEnter(test, step))==null){
                        String errorMsg="'"+stepParser.parseTextToEnter(test, step)+"' attribute is not fount.";
                        log.error(errorMsg);
                        throw new TesboException(errorMsg);
                    }
                    elementText.add(element.getAttribute(stepParser.parseTextToEnter(test, step)));
                }
                else if(step.toLowerCase().contains(" css value ")){
                    elementText.add(cmd.findElement(driver, locator.getLocatorValue(testsFileName, stepParser.parseElementName(step))).getCssValue(stepParser.parseTextToEnter(test, step)));
                }

                try {
                    if (headerName.split("\\.").length==3) {
                        isDetaSet=true;
                        String[] dataSet=headerName.split("\\.");
                        if(dataSet.length==3) {
                            getGlobalDataValue(testsFileName,dataSet[0], dataSet[1],dataSet[2]);
                            setVariableValue(testsFileName, dataSet[1], dataSet[2], elementText, variableType);
                        }
                        else{
                            String errorMsg="Please enter DataSet in: '"+step+"'";
                            log.info(errorMsg);
                            throw new TesboException(errorMsg);
                        }

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
                            getGlobalDataValue(testsFileName,null, dataSetName,headerName);
                            setVariableValue(testsFileName, dataSetName,headerName, elementText,variableType);
                        }
                    } catch (Exception e) {
                        try{
                            if(TestExecutor.localVariable.containsKey(headerName)){
                                if(variableType.equals("list")){
                                    TestExecutor.localVariable.put(headerName, elementText);
                                }else {
                                    TestExecutor.localVariable.put(headerName, elementText.get(0));
                                }
                            }
                        }
                        catch (Exception ex) {
                            String errorMsg="Key name " + headerName + " is not found in " + dataSetName + " data set";
                            log.error(errorMsg);
                            throw new TesboException(errorMsg);

                        }
                    }
                }
            }
        }
    }

    private void setVariableValue(String testsFileName, String dataSetName, String keyName, JSONArray elementText, String variableType){
        JSONObject variables=new JSONObject();
        JSONObject dataSetNames=new JSONObject();
        JSONObject testDataSet;
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
    }

    public void setLocalVariableValue(String keyName,JSONArray elementText){

        TestExecutor.localVariable.put(keyName,elementText.get(0));

    }

    public boolean getLocalVariableFromGlobalVariable(String testsFileName,String keyName){
        boolean isVariableExist=false;
        JSONObject dataSetList = (JSONObject) TestExecutionBuilder.dataSetVariable.get(testsFileName);
        if(dataSetList!=null) {
            for (Object dataSet : dataSetList.keySet()) {
                JSONObject dataSetValues = (JSONObject) dataSetList.get(dataSet);
                if (dataSetValues.containsKey(keyName)) {
                    isVariableExist = true;
                }
            }
        }

        if(!isVariableExist){
            TestsFileParser testsFileParser=new TestsFileParser();
            StringBuffer testsFile= testsFileParser.readTestsFile(testsFileName);
            String[] allLines = testsFile.toString().split("[\\r\\n]+");
            boolean isDataSetName=false;
            boolean isBreak=false;
            for (int i = 0; i < allLines.length; i++) {
                if (allLines[i].contains("DataSet:")) {
                    isDataSetName = true;
                }
                if(isDataSetName) {

                    if(allLines[i].contains("\"" + keyName + "\":")) {
                        isVariableExist = true;
                        isBreak=true;
                    }
                    if(allLines[i].startsWith("Test: ")) {isBreak=true;}

                    if(isBreak){break;}

                }
            }
        }
        return isVariableExist;
    }

    public int getDataSetListSize(String dataSetName){
        int dataSetListSize=0;
        JSONArray dataSetFileList=getDataSetFileList();

        for (Object dataSetFile : dataSetFileList) {
            JSONObject main = Utility.loadJsonFile(dataSetFile.toString());
            if (main.containsKey(dataSetName)) {
                if (main.get(dataSetName) instanceof JSONArray) {
                    JSONArray dataSetList = (JSONArray) main.get(dataSetName);
                    dataSetListSize = dataSetList.size();
                } else {
                    JSONObject dataSetList = (JSONObject) main.get(dataSetName);
                    for (Object key : dataSetList.keySet()) {
                        dataSetListSize = ((JSONArray) dataSetList.get(key)).size();
                        if(dataSetListSize!=0) {break;}
                    }

                }
            }
        }
        return dataSetListSize;
    }

    public String getDataSetListValue(String dataSetName, String keyName, int row){
        String keyValue=null;
        JSONArray dataSetFileList=getDataSetFileList();
        for (Object dataSetFile : dataSetFileList) {
            JSONObject main = Utility.loadJsonFile(dataSetFile.toString());
            if (main.containsKey(dataSetName)) {
                if (main.get(dataSetName) instanceof JSONArray) {
                    JSONArray dataSetList = (JSONArray) main.get(dataSetName);
                    JSONObject dataSet=(JSONObject)dataSetList.get(row-1);
                    keyValue=dataSet.get(keyName).toString();
                }
                else {
                    JSONObject dataSet = (JSONObject) main.get(dataSetName);
                    JSONArray dataSetList = (JSONArray)dataSet.get(keyName);
                    keyValue=dataSetList.get(row-1).toString();
                }
                break;
            }

        }

        return keyValue;
    }
}