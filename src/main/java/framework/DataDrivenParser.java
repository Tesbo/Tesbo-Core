package framework;

import com.google.common.io.Files;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import Exception.TesboException;



public class DataDrivenParser {

    public boolean isExcel(String suiteName) {
        SuiteParser suiteParser=new SuiteParser();
        StringBuffer suite= suiteParser.readSuiteFile(suiteName);
        String allLines[] = suite.toString().split("[\\r\\n]+");
        boolean flag=false;
        for (int i = 0; i < allLines.length; i++) {
            if(allLines[i].contains("Test:"))
                break;

            if(allLines[i].contains("\"excelFile\":")){
                String[] excelUrl= allLines[i].replaceAll("[\"| |,]","").split(":",2);
                if(excelUrl.length==2)
                {
                    String filePath=excelUrl[1];
                    File file = new File(filePath);
                    String ext = Files.getFileExtension(filePath);
                    if(file.exists()) {
                        flag = true;
                    }else {
                        throw new TesboException("Excel file Not Found On " + filePath);
                    }
                    if(!ext.equalsIgnoreCase("xlsx"))
                        throw new TesboException("Required only '.xlsx' file : "+filePath);
                }else{
                    throw new TesboException("Please Enter File Path");
                }

            }
        }
        return flag;
    }

    public boolean isDataSet(String suiteName) {
        SuiteParser suiteParser=new SuiteParser();
        StringBuffer suite= suiteParser.readSuiteFile(suiteName);
        String allLines[] = suite.toString().split("[\\r\\n]+");
        boolean flag=false;
        for (int i = 0; i < allLines.length; i++) {

            if(allLines[i].contains("DataSet:")){
                if (!(allLines[i].contains("DataSet:")))
                    throw new TesboException("Write 'DataSet' keyword in suite");
                flag = true;
                break;
            }
            else{
                if(allLines[i].toLowerCase().contains("dataset:") || allLines[i].toLowerCase().contains("dataset :")){
                    throw new TesboException("Please add valid key word for: '"+allLines[i]+"'");
                }
            }
        }
        return flag;
    }


    /**
     *
     * @param suiteName
     * @param dataSetName
     * @param keyName
     * @return
     */
    public String checkDataTypeIsExcelOrGlobleInDataset(String suiteName, String dataSetName,ArrayList<String> keyName) {
        SuiteParser suiteParser = new SuiteParser();
        StringBuffer suite = suiteParser.readSuiteFile(suiteName);
        String allLines[] = suite.toString().split("[\\r\\n]+");
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
            throw new TesboException("'" + dataSetName + "' is not found in Data Set");
        }

        if (!isExcel && !isGlobal){ throw new TesboException("Excel File url is not found in " + dataSetName + " Data Set");}

        return type;

    }

    public ArrayList<String> getColumnNameFromTest(ArrayList<String> testSteps){

        ArrayList<String> columnNameList=new ArrayList<String>();
        for(String step:testSteps)
        {
            if(step.contains("{")&& step.contains("}")){
                String[] splitStep=step.split("\\s");
                for(String calName:splitStep)
                {
                    if(calName.contains("{")&& calName.contains("}"))
                        columnNameList.add(calName.replaceAll("[{}]", ""));
                }
            }
        }
        return columnNameList;
    }

    public String getExcelUrl(String suiteName,String dataSetName) {
        SuiteParser suiteParser=new SuiteParser();
        StringBuffer suite= suiteParser.readSuiteFile(suiteName);
        String allLines[] = suite.toString().split("[\\r\\n]+");
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


    public ArrayList<String> isDetaSetOfExcelIsUseInSuite(String suiteName) {
        ArrayList<String> excelHeaderList=new ArrayList<String>();
        SuiteParser suiteParser=new SuiteParser();
        StringBuffer suite= suiteParser.readSuiteFile(suiteName);
        String allLines[] = suite.toString().split("[\\r\\n]+");
        boolean flag=false;
        for (int i = 0; i < allLines.length; i++) {
            if(allLines[i].contains("DataSet.excelFile")){
                String[] steps=allLines[i].split(" ");
                for(String step:steps)
                {
                    if(step.contains("DataSet.excelFile")){
                        if(excelHeaderList.size()==0) {
                            excelHeaderList.add(step.split("\\.")[2]);
                        }
                        else{
                            boolean isHeader=false;
                            for(int j=0;j<excelHeaderList.size();j++) {
                                if(excelHeaderList.get(j).equalsIgnoreCase(step.split("\\.")[2]))
                                    isHeader=true;
                            }
                            if(!isHeader)
                                excelHeaderList.add(step.split("\\.")[2]);
                        }
                    }
                }
                flag=true;
            }
        }
        if(flag) {
            return excelHeaderList;
        }else
            throw new TesboException("Excel data is not used in suite file.");
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
            throw new TesboException(e.getMessage());
        }
        return CellData;
    }

    public String getGlobalDataValue(String suiteName, String dataSetName,String keyName) {
        SuiteParser suiteParser=new SuiteParser();
        StringBuffer suite= suiteParser.readSuiteFile(suiteName);
        String allLines[] = suite.toString().split("[\\r\\n]+");
        boolean isDataSetName=false,isKeyName=false;
        String KeyValue=null;

        for (int i = 0; i < allLines.length; i++) {
            if(allLines[i].contains("\""+dataSetName+"\":")){
                isDataSetName=true;
            }
            if(isDataSetName) {
                if (allLines[i].contains("\""+keyName+"\":")) {
                    try{

                        KeyValue=allLines[i].replaceAll("[\"|,]","").split(":")[1].trim();
                        isKeyName=true;
                        break;
                    }catch (Exception E){
                        throw new TesboException("Key value is not found in dataSet");
                    }
                }
                if(allLines[i].contains("}"))
                    break;
            }
        }
        if(KeyValue.equals(null)){
            throw new TesboException("Key name " + keyName + " is not found in " + dataSetName + " data set");
        }

        return KeyValue;

    }

    public void isHeader(JSONObject test,String step){
        SuiteParser suiteParser=new SuiteParser();
        String dataSetName = suiteParser.getTestDataSetBySuiteAndTestCaseName(test.get("suiteName").toString(), test.get("testName").toString());

        ArrayList<String> columnNameList = new ArrayList<String>();
        columnNameList = getColumnNameFromTest(suiteParser.getTestStepBySuiteandTestCaseName(test.get("suiteName").toString(), test.get("testName").toString()));


        JSONArray  listOfHeader=getHeaderValuefromExcel(getExcelUrl(test.get("suiteName").toString(), dataSetName.replace(" ", "").split(":")[1]), columnNameList,Integer.parseInt(SheetNumber(test.get("suiteName").toString(), test.get("testName").toString())));

        for(Object h:listOfHeader){

            String[] splitStep=step.split("\\s");
            String HeaderName = null;
            for(String calName:splitStep)
            {
                if(calName.contains("{")&& calName.contains("}"))
                    HeaderName=calName.replaceAll("[{}]", "");
            }

            if(((JSONObject) h).get(HeaderName)==null) {
               throw new TesboException("Please enter valid headerName: "+HeaderName);
            }
        }

    }

    public String SheetNumber(String suiteName,String testName){
        SuiteParser suiteParser=new SuiteParser();
        String dataSetName = suiteParser.getTestDataSetBySuiteAndTestCaseName(suiteName, testName).split(":")[1];
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

}
