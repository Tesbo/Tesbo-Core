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

    public boolean isExcel(String suiteName) throws FileNotFoundException {
        SuiteParser suiteParser=new SuiteParser();
        StringBuffer suite= suiteParser.readSuiteFile(suiteName);
        String allLines[] = suite.toString().split("[\\r\\n]+");
        boolean flag=false;
        for (int i = 0; i < allLines.length; i++) {
            if(allLines[i].contains("Test :") | allLines[i].contains("Test:"))
                break;

            if(allLines[i].toLowerCase().contains("\"excelfile\" :") | allLines[i].toLowerCase().contains("\"excelfile\":")){
                if (!(allLines[i].contains("\"excelFile\" :") | allLines[i].contains("\"excelFile\":")))
                    throw new TesboException("Write 'excelFile' keyword in Data Set");

                String[] excelUrl= allLines[i].replaceAll("[\"| |,]","").split(":",2);
                if(excelUrl.length==2)
                {
                    String filePath=excelUrl[1];
                    File file = new File(filePath);
                    String ext = Files.getFileExtension(filePath);
                    if(file.exists()) {
                        flag = true;
                    }else {
                        throw new FileNotFoundException("Excel file Not Found On " + filePath);
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

    public boolean isDataSet(String suiteName) throws Exception {
        SuiteParser suiteParser=new SuiteParser();
        StringBuffer suite= suiteParser.readSuiteFile(suiteName);
        String allLines[] = suite.toString().split("[\\r\\n]+");
        boolean flag=false;
        for (int i = 0; i < allLines.length; i++) {

            if(allLines[i].contains("DataSet :") | allLines[i].contains("DataSet:")){
                flag = true;
                break;

            }
        }
        return flag;
    }

    public String dataSetIsExistInSuite(String suiteName, String dataSetName,ArrayList<String> keyName) {
        SuiteParser suiteParser=new SuiteParser();
        StringBuffer suite= suiteParser.readSuiteFile(suiteName);
        String allLines[] = suite.toString().split("[\\r\\n]+");
        boolean flag=false,isDataSetName=false;
        String type=null;
        for (int i = 0; i < allLines.length; i++) {
            if(allLines[i].contains("\""+dataSetName+"\" :") | allLines[i].contains("\""+dataSetName+"\":")){
                isDataSetName=true;
            }
            if(isDataSetName) {
                if (allLines[i].contains("\"excelFile\" :") | allLines[i].contains("\"excelFile\":")) {
                    type="excel";
                    flag=true;
                    break;
                }
                if(keyName.size()>0) {
                    for(String key:keyName) {
                        if (allLines[i].contains("\""+key+"\" :") | allLines[i].contains("\""+key+"\":")) {
                            type = "global";
                            flag = true;
                            break;
                        }
                    }

                }
                if(allLines[i].contains("}"))
                    break;
            }

        }

        if(!isDataSetName )
            throw new TesboException("'"+dataSetName + "' is not found in Data Set");

        if(type.equalsIgnoreCase("excel"))
            if(!flag)
                throw new TesboException("Excel File url is not found in " + dataSetName + " Data Set");

        if(type.equalsIgnoreCase("global"))
            if(!flag)
                throw new TesboException("Data is not found in " + dataSetName + " Data Set");

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
                if (allLines[i].contains("\"excelFile\" :") | allLines[i].contains("\"excelFile\":")) {
                    String[] excelUrl = allLines[i].replaceAll("[\"| |,]", "").split(":", 2);
                    filePath = excelUrl[1];
                    break;
                }
        }
        }
        return filePath;
    }


    public ArrayList<String> isDetaSetOfExcelIsUseInSuite(String suiteName) {
        ArrayList<String> excelHeaderList=new ArrayList<String>();
        SuiteParser suiteParser=new SuiteParser();
        StringBuffer suite= suiteParser.readSuiteFile(suiteName);
        String allLines[] = suite.toString().split("[\\r\\n]+");
        boolean flag=false;
        for (int i = 0; i < allLines.length; i++) {
            if(allLines[i].toLowerCase().contains("dataset.excelfile")){
                String[] steps=allLines[i].split(" ");
                for(String step:steps)
                {
                    if(step.toLowerCase().contains("dataset.excelfile")){
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

    public JSONArray getHeaderValuefromExcel(String url,ArrayList<String> dataSetValues)
    {
        String filePath=url;
        JSONArray excelData=new JSONArray();
        FileInputStream file = null;
        try {
            file = new FileInputStream(new File(filePath));

        XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
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
            e.printStackTrace();
        }
        return excelData;
    }

    public String getcellValuefromExcel(String url,String headerName,int rowNum)
    {
        String filePath=url;
        String CellData=null;
        FileInputStream file = null;
        try {
            file = new FileInputStream(new File(filePath));
            String columnIndex=null;
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row rows = rowIterator.next();
                if (rows.getCell(0) != null) {
                    DataFormatter formatter = new DataFormatter();
                    if (rows.getRowNum() == 0) {
                        Iterator<Cell> cellIterator = rows.cellIterator();
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            if(headerName.equalsIgnoreCase(formatter.formatCellValue(cell))){
                                columnIndex= String.valueOf(cell.getColumnIndex());
                            }
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
            e.printStackTrace();
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
            if(allLines[i].contains("\""+dataSetName+"\" :") | allLines[i].contains("\""+dataSetName+"\":")){
                isDataSetName=true;
            }
            if(isDataSetName) {
                if (allLines[i].contains("\""+keyName+"\" :") | allLines[i].contains("\""+keyName+"\":")) {
                    try{
                        KeyValue=allLines[i].replaceAll("[\"| |,]","").split(":")[1];
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


        return KeyValue;

    }


}
