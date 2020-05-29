package framework;

import execution.TestExecutionBuilder;
import selenium.Commands;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import exception.TesboException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class DataDrivenParser {

    private static final Logger log = LogManager.getLogger(DataDrivenParser.class);
     TesboLogger tesboLogger = new TesboLogger();
     String excelFileDataSet="excelFile";
     String notFoundMsg="' is not found in '";
     CommonMethods commonMethods=new CommonMethods();
     boolean isJSONArray=false;
    String variableType="text";

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
                    type=getDataSetTypeIsList(((JSONObject)DataSet),key,dataSetName);
                }
            }
        return type;
    }

    public String getDataSetTypeIsList(JSONObject dataSet,String key, String dataSetName){
        if((dataSet).containsKey(key)){
            return "list";
        }
        else{
            if(key.split("\\.").length!=3) {
                commonMethods.throwTesboException("'"+key + notFoundMsg + dataSetName + "' Data Set'",log);
            }
        }
        return null;
    }

    public String verifyJsonObjectHasJsonArrayListAndAllObjectHasSameNumberOfArrayListAndAllKeyValuePresent(JSONObject main, String dataSetName, List<String> keyName) {
        String type=null;
        int arraySize=0;
        for(String key: keyName) {
            JSONObject dataSetList = (JSONObject) main.get(dataSetName);
            if (dataSetList.containsKey(key)) {
                if (((JSONObject) main.get(dataSetName)).get(key) instanceof JSONArray){
                    arraySize=getJsonArraySizeOfJsonObject(arraySize,((JSONArray) ((JSONObject) main.get(dataSetName)).get(key)), key);

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

    public int getJsonArraySizeOfJsonObject(int arraySize, JSONArray arrayList, String key){
        if(arraySize==0){
                arraySize=arrayList.size();
        }
        else {
            if(arraySize != arrayList.size()){
                commonMethods.throwTesboException("'"+key + "' key array size is different than others.",log);
            }
        }
        return arraySize;
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
                    boolean flag=verifyColumnNameIsExistOnDefineList(defineList, calName);
                    if(!flag) {
                        columnName=calName.replaceAll("[{}()]", "").trim();
                    }
                }
            }
        }
        return columnName;
    }

    public boolean verifyColumnNameIsExistOnDefineList(JSONArray defineList,String calName){
        for(int i=0;i<defineList.size();i++){
            if(calName.toLowerCase().contains(defineList.get(i).toString().toLowerCase())){
                return true;
            }
        }
        return false;
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
        List<String> cellNums=new LinkedList<>();
        while (rowIterator.hasNext()) {
            Row rows = rowIterator.next();
            if (rows.getCell(0) != null) {
                DataFormatter formatter = new DataFormatter();
                if (rows.getRowNum() == 0) {
                    for(String header:dataSetValues){
                        Iterator<Cell> cellIterator = rows.cellIterator();
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            cellNums=setHeaderNameInList(cellNums, header,cell,formatter);
                        }
                    }
                }
            }
        }
        return cellNums;
    }

    public List<String> setHeaderNameInList(List<String> cellNums, String header,Cell cell,DataFormatter formatter){
        if(header.equalsIgnoreCase(formatter.formatCellValue(cell))){
            cellNums.add(formatter.formatCellValue(cell) + ":" + cell.getColumnIndex());
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

        List<String> headerNames=new LinkedList<>();
        headerNames.add(headerName);

        JSONArray cellData;
        FileInputStream file = null;
        try {
            file = new FileInputStream(new File(url));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(sheetNo);
            Iterator<Row> rowIterator = sheet.iterator();

            List<String> cellNums=getHeaderName(rowIterator, headerNames);

            rowIterator = sheet.iterator();
            cellData=getValueFromHeaderName(rowIterator,cellNums);

            commonMethods.verifyJsonArrayIsEmpty(cellData,"Please enter valid headerName: "+headerName,log);

            file.close();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new TesboException(e.getMessage());
        }
        return ((JSONObject) cellData.get(rowNum-1)).get(headerName).toString();
    }

    public JSONObject getGlobalDataValue(String testsFileName,String dataSetFileName, String dataSetName,String keyName) {
        JSONObject keyValue=new JSONObject();
        boolean isVariable=false;

        if(TestExecutionBuilder.dataSetVariable.containsKey(testsFileName)){
            keyValue=getGlobalDataValueFromVariable(testsFileName,dataSetName,keyName);
            isVariable=true;
        }

        if(!isVariable) {
            JSONArray dataSetFileList=getDataSetFileList();
            boolean isInline=false;
            boolean isGlobal=true;
            for (Object dataSetFile : dataSetFileList) {

                if(dataSetFileName!=null){
                    isGlobal=false;
                    isInline=isDataSetInline(dataSetFileName,dataSetFile.toString());
                }

                if(isInline || isGlobal) {
                    keyValue=getGlobalDataValueFromJson(dataSetFile.toString(),dataSetName,keyName,dataSetFileName,isInline);
                    if(keyValue.size()!=0) {break;}

                }
            }
            verifyDataSetFileNameNotNull(dataSetFileName,isInline);
        }
        verifyKeyValueNotZero(keyValue.size(),keyName,dataSetName);
        return keyValue;

    }

    public void verifyDataSetFileNameNotNull(String dataSetFileName,boolean isInline){
        if(dataSetFileName!=null && !isInline){ commonMethods.throwTesboException("'" + dataSetFileName + ".json' is not found in DataSet directory",log); }
    }

    public void verifyKeyValueNotZero(int keyValueSize,String keyName,String dataSetName){
        if(keyValueSize<=0){commonMethods.throwTesboException("Key name " + keyName + " is not found in " + dataSetName + " data set",log);}
    }

    public JSONObject getGlobalDataValueFromVariable(String testsFileName, String dataSetName,String keyName){
        JSONObject keyValue=new JSONObject();
        JSONObject dataSetList;
        dataSetList= (JSONObject) TestExecutionBuilder.dataSetVariable.get(testsFileName);

        if(dataSetList.containsKey(dataSetName)){
            JSONObject variableList;
            variableList= (JSONObject) dataSetList.get(dataSetName);

            if(variableList.containsKey(keyName)){
                keyValue.put(keyName,variableList.get(keyName));
            }
        }
        return keyValue;
    }

    public boolean isDataSetInline(String dataSetFileName, String dataSetFile){
        if(dataSetFileName!=null){
            String pattern = Pattern.quote(System.getProperty("file.separator"));
            int size=dataSetFile.split(pattern).length;
            String fileName= dataSetFile.split(pattern)[size-1];
            if(dataSetFileName.equals(fileName.split("\\.")[0])){
                return true;
            }
        }
        return false;
    }

    public JSONObject getGlobalDataValueFromJson(String dataSetFile, String dataSetName,String keyName,String dataSetFileName, boolean isInline){
        JSONObject keyValue=new JSONObject();
        JSONObject main = Utility.loadJsonFile(dataSetFile);
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
        }
        else{
            if(isInline){ commonMethods.throwTesboException("'" + dataSetName + notFoundMsg+ dataSetFileName +".json' DataSet.",log); }
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

    public void setValueInDataSetVariable(WebDriver driver, JSONObject test, String step) {
        variableType="text";
        String testsFileName= test.get("testsFileName").toString();

        int startPoint = 0;
        int endPoint = 0;

        if (step.contains("{") && step.contains("}")) {
            startPoint = step.indexOf('{') + 1;
            endPoint = step.lastIndexOf('}');
            String headerName = step.substring(startPoint, endPoint);
            boolean isDataSet;

            JSONArray elementText;

            if(step.toLowerCase().contains(" define ")){
                elementText= getLocalDataSetValueFromStep(step,testsFileName,headerName, test);
                setLocalVariableValue(headerName,elementText);
            }
            else {
                String dataSetName=test.get("dataSetName").toString();
                verifyVariableValueHasNotArrayListAndExcelType(test.containsKey("dataType"), dataSetName, step);
                elementText=getElementValueFromStep(driver,step,test,testsFileName);
                isDataSet=setInlineVariableValue(headerName,testsFileName,elementText,step);

                if(!isDataSet) {
                    setVariableValueWhenItHasNotDataSet(test.get("dataType").toString(),testsFileName,dataSetName,headerName,elementText);
                }
            }
        }
    }

    public JSONArray getLocalDataSetValueFromStep(String step,String testsFileName,String headerName, JSONObject test){
        JSONArray elementText=new JSONArray();
        StepParser stepParser=new StepParser();
        if(step.toLowerCase().contains(" set ") || step.toLowerCase().contains(" put ") || step.toLowerCase().contains(" assign ")) {
            elementText.add(stepParser.parseTextToEnter(test, step));
            if (getLocalVariableFromGlobalVariable(testsFileName, headerName)) {
                commonMethods.throwTesboException("'" + headerName + "' variable is exist on global data set",log);
            }
        }
        else{ elementText.add(""); }
        return elementText;
    }

    public void verifyVariableValueHasNotArrayListAndExcelType(boolean key,String dataSetName, String step){
        if(key && (dataSetName.equals("excel") || dataSetName.equals("list"))) {
            commonMethods.throwTesboException("Array list and Excel DataSet can't be use in set variable '" + step + "'",log);
        }
    }

    public JSONArray getElementValueFromStep(WebDriver driver,String step,JSONObject test,String testsFileName) {
        Commands cmd = new Commands();
        StepParser stepParser=new StepParser();
        GetLocator locator = new GetLocator();
        JSONArray elementText=new JSONArray();

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
        return elementText;
    }

    public boolean setInlineVariableValue(String headerName,String testsFileName,JSONArray elementText,String step){
        boolean isDataSet=false;
        try {
            if (headerName.split("\\.").length==3) {
                isDataSet=true;
                String[] dataSet=headerName.split("\\.");
                if(dataSet.length==3) {
                    getGlobalDataValue(testsFileName,dataSet[0], dataSet[1],dataSet[2]);
                    setVariableValue(testsFileName, dataSet[1], dataSet[2], elementText, variableType);
                }
                else{ commonMethods.throwTesboException("Please enter DataSet in: '"+step+"'",log); }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
            throw e;
        }
        return isDataSet;
    }

    public void setVariableValueWhenItHasNotDataSet(String dataType,String testsFileName,String dataSetName,String headerName,JSONArray elementText){
        try {
            if (dataType.equalsIgnoreCase("global")) {
                getGlobalDataValue(testsFileName,null, dataSetName,headerName);
                setVariableValue(testsFileName, dataSetName,headerName, elementText,variableType);
            }
        } catch (Exception e) {
            try{
                if(TestExecutor.localVariable.containsKey(headerName)){
                    if(variableType.equals("list")){ TestExecutor.localVariable.put(headerName, elementText); }
                    else { TestExecutor.localVariable.put(headerName, elementText.get(0)); }
                }
            }
            catch (Exception ex) {
                commonMethods.throwTesboException("Key name " + headerName + " is not found in " + dataSetName + " data set",log);
            }
        }
    }

    private void setVariableValue(String testsFileName, String dataSetName, String keyName, JSONArray elementText, String variableType){
        JSONObject variables;
        JSONObject dataSetNames=new JSONObject();

        if(TestExecutionBuilder.dataSetVariable.size()==0) {
            variables=setVariableValueInJson(keyName,variableType,elementText);
            dataSetNames.put(dataSetName, variables);
            TestExecutionBuilder.dataSetVariable.put(testsFileName, dataSetNames);
        }
        else {
            if(TestExecutionBuilder.dataSetVariable.containsKey(testsFileName)){
                setVariableValueWhenDataSetVariableAlreadyExist(testsFileName,dataSetName,keyName,variableType,elementText);
            }
            else{
                variables=setVariableValueInJson(keyName,variableType,elementText);
                dataSetNames.put(dataSetName, variables);
                TestExecutionBuilder.dataSetVariable.put(testsFileName, dataSetNames);
            }
        }
    }

    public JSONObject setVariableValueInJson(String keyName,String variableType,JSONArray elementText){
        JSONObject variables=new JSONObject();
        if(variableType.equals("list")){ variables.put(keyName, elementText); }
        else{variables.put(keyName, elementText.get(0));}
        return variables;
    }

    public void setVariableValueWhenDataSetVariableAlreadyExist(String testsFileName, String dataSetName,String keyName,String variableType,JSONArray elementText){
        JSONObject testDataSet= (JSONObject) TestExecutionBuilder.dataSetVariable.get(testsFileName);
        JSONObject variables;
        JSONObject dataSetNames;

        if(testDataSet.containsKey(dataSetName)){
            dataSetNames= (JSONObject) testDataSet.get(dataSetName);
            if(variableType.equals("list")){ dataSetNames.put(keyName, elementText); }
            else{ dataSetNames.put(keyName, elementText.get(0)); }
            testDataSet.put(dataSetName, dataSetNames);
            TestExecutionBuilder.dataSetVariable.put(testsFileName, testDataSet);
        }else{
            variables=setVariableValueInJson(keyName,variableType,elementText);
            testDataSet.put(dataSetName, variables);
            TestExecutionBuilder.dataSetVariable.put(testsFileName, testDataSet);
        }
    }

    public void setLocalVariableValue(String keyName,JSONArray elementText){

        TestExecutor.localVariable.put(keyName,elementText.get(0));
    }

    public boolean getLocalVariableFromGlobalVariable(String testsFileName,String keyName){
        boolean isVariableExist=false;
        JSONObject dataSetList = (JSONObject) TestExecutionBuilder.dataSetVariable.get(testsFileName);
        if(dataSetList!=null) {
            HashMap<String,Object> result =new HashMap<>();
            try {
                result = new ObjectMapper().readValue(dataSetList.toJSONString(), HashMap.class);
            } catch (IOException e) { }
            for (Map.Entry<String,Object> entry : result.entrySet())
            {
                JSONObject dataSetValues = (JSONObject) dataSetList.get(entry.getKey());
                if (dataSetValues.containsKey(keyName)) {
                    isVariableExist = true;
                }
            }
        }
        if(!isVariableExist){
            isVariableExist=isDataSerValueIsExistOnTestsFile(testsFileName,keyName);
        }
        return isVariableExist;
    }

    public boolean isDataSerValueIsExistOnTestsFile(String testsFileName,String keyName){
        TestsFileParser testsFileParser=new TestsFileParser();
        boolean isVariableExist=false;
        StringBuilder testsFile= testsFileParser.readTestsFile(testsFileName);
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
                    dataSetListSize=getDataSetListSizeWhenItHasJsonObject(dataSetList);
                }
            }
        }
        return dataSetListSize;
    }

    public int getDataSetListSizeWhenItHasJsonObject(JSONObject dataSetList){
        int dataSetListSize=0;
        HashMap<String,Object> result =new HashMap<>();
        try {
            result = new ObjectMapper().readValue(dataSetList.toJSONString(), HashMap.class);
        } catch (IOException e) { }
        for (Map.Entry<String,Object> entry : result.entrySet())
        {
            dataSetListSize = ((ArrayList)entry.getValue()).size();
            if(dataSetListSize!=0) {break;}
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