package framework;

import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.*;

public class TestsFileParser {

    TesboLogger tesboLogger = new TesboLogger();
    String testText="Test: ";
    String collectionNameText="Collection Name: ";
    String newLineRegex="[\\r\\n]+";
    String beforeTestText="BeforeTest:";
    String afterTestText="AfterTest:";
    String endText="End::";
    String endStepErrorMsg="Please define end step in a correct way: End";
    String endStepNotFoundErrorMsg="End Step is not found for '";
    String beforeTestEndStepErrorMsg="End Step is not found for BeforeTest";
    String keywordErrorMsg="Please write valid keyword for this step \"";
    String spaceRegex="\\s{2,}";
    String codeText="Code: ";
    String stepText="Step: ";
    String testSmallText="' test";
    String verifyText="Verify: ";
    String dataSetText="DataSet: ";
    String endPointText="endpoint";
    String startPointText="startPoint";

    private static final Logger log = LogManager.getLogger(TestsFileParser.class);
    CommonMethods commonMethods=new CommonMethods();
    /**
     * @param directory
     * @return give all the file inside a directory
     */
    public JSONArray getTestFiles(String directory)  {

        JSONArray testsFileList = new JSONArray();
        boolean flag=false;
        String file=null;
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            testsFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
            flag=verifyThatTestsDirectoryHasOnlyTestFile(testsFileList);
            if(flag){
                commonMethods.logErrorMsg(file+" file found in tests directory",log);
                throw (new NoSuchFileException(""));
            }
        } catch (Exception e) {
            if(flag){
                commonMethods.logErrorMsg("Message : Please create only '.tests' file in tests directory.",log);
            }
            else {
                commonMethods.logErrorMsg("'" + directory + "' no files found on your location.",log);
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                commonMethods.logErrorMsg(sw.toString(),log);
            }
            try {
                throw e;
            } catch (IOException ie) {
                StringWriter sw = new StringWriter();
                ie.printStackTrace(new PrintWriter(sw));
                commonMethods.logErrorMsg(sw.toString(),log);
            }
        }
        return testsFileList;
    }

    /**
     *
     * @param testsFileList
     * @return
     */
    public boolean verifyThatTestsDirectoryHasOnlyTestFile(JSONArray testsFileList){
        StringBuilder file=new StringBuilder();
        boolean flag=false;
        for(Object testsFilePath:testsFileList) {
            String[] testsFile=testsFilePath.toString().split("\\.");
            if (testsFile.length == 2 && (!testsFile[1].equalsIgnoreCase("tests"))) {
                flag=true;
                if(file.length()==0)
                    file.append("'."+testsFile[1]+"'");
                else
                    file.append(", '."+testsFile[1]+"'");
            }
        }
        return flag;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public StringBuilder readTestsFile(String fileName) {

        GetConfiguration configuration = new GetConfiguration();
        BufferedReader br = null;
        FileReader fr = null;
        StringBuilder tests = new StringBuilder();

        try {
            fr = new FileReader(configuration.getTestsDirectory() + "/" + fileName);
            br = new BufferedReader(fr);
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                tests.append(sCurrentLine + "\n");
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            tesboLogger.testFailed(sw.toString());
            log.error(sw.toString());
        } finally {

            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
            }
        }
        return tests;
    }

    /**
     *
     * @param tagName
     * @param testsFile
     * @return
     */
    public JSONArray getTestNameByTag(String tagName, StringBuilder testsFile) {

        String[] allLines = testsFile.toString().split(newLineRegex);
        JSONArray testName = new JSONArray();

        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].toLowerCase().startsWith("test: ") || allLines[i].toLowerCase().startsWith("test")) {
                String tagLine = allLines[i + 1].toLowerCase();
                String[] tagArray = tagLine.replaceAll("\\s+","").split("#");

                String test=getTestNameWhenTagNameIsExist(tagArray,tagName,allLines[i]);
                if(test!=null){
                    testName.add(test);
                }
            }
        }
        // When No Test Available
        if (testName.isEmpty()) {
            return null;
        }
        return testName;
    }

    /**
     *
     * @param tagArray
     * @param tagName
     * @param testLine
     * @return
     */
    public String getTestNameWhenTagNameIsExist(String[] tagArray,String tagName,String testLine){

        for(String  tag : tagArray) {
            if(!tag.equals("") && (tag.trim().equalsIgnoreCase(tagName))) {
                if (!testLine.trim().startsWith(testText)) {
                    commonMethods.throwTesboException("Please write valid keyword or step for this \"" + testLine + "\"",log);
                }
                String[] testNameArray = testLine.split(":");
                if(testNameArray.length<2){
                    commonMethods.throwTesboException("Test name is blank '"+testLine+"'",log);
                }
                return testNameArray[1].trim();
            }
        }
        return null;
    }

    /**
     *
     * @param tag
     * @return
     */
    public JSONObject getTestNameByTag(String tag)  {
        GetConfiguration configuration = new GetConfiguration();
        String directoryPath = configuration.getTestsDirectory();
        JSONArray testsFileList = getTestFiles(directoryPath);
        Map<String, StringBuilder> allTestsFile = new HashMap<>();

        JSONObject testNameWithTestsFile = new JSONObject();

        for (int i = 0; i < testsFileList.size(); i++) {
            File name = new File(testsFileList.get(i).toString());
            allTestsFile.put(name.getName(), readTestsFile(name.getName()));
        }

        for (Map.Entry entry : allTestsFile.entrySet()) {
            JSONArray testNames = getTestNameByTag(tag, (StringBuilder) entry.getValue());
            if (testNames != null) {
                testNameWithTestsFile.put(entry.getKey().toString(), testNames);
            }
        }

        return testNameWithTestsFile;
    }

    /**
     *
      * @param testsFileName
     * @param testName
     * @return
     */
    public JSONArray getTestStepByTestsFileandTestCaseName(String testsFileName, String testName) {
        StringBuilder testsFileDetails = readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        JSONArray testSteps = new JSONArray();
        Validation validation=new Validation();
        int startPoint = 0;
        int endpoint = 0;
        JSONObject startAndEndPoint=getTestStartAndEndPoint(allLines,testName,endStepNotFoundErrorMsg + testName + testSmallText);
        startPoint= Integer.parseInt(startAndEndPoint.get(startPointText).toString());
        endpoint= Integer.parseInt(startAndEndPoint.get(endPointText).toString());

        for (int j = startPoint; j < endpoint; j++) {
            String step=allLines[j].replaceAll(spaceRegex, " ").trim();
            if(step.split(":").length<2 && step.contains("Step:")){
                commonMethods.throwTesboException("Step is blank '"+allLines[j]+"'",log);
            }
            if (step.startsWith(stepText) || step.startsWith(verifyText) ||
                    step.startsWith("If:: ") || step.equals("Else::") || step.startsWith("Else If:: ") || step.equals(endText) ||
                    step.startsWith("Collection: ") || (step.contains("[Close:") && step.contains("]")) ||
                    step.startsWith(codeText) ||
                    ( step.contains("[") && step.contains("]") && !(step.toLowerCase().contains("[close")) && !(step.startsWith(dataSetText)))) {

                testSteps.add(allLines[j]);
            }
            else{
                validation.keyWordValidation(allLines[j]);
            }
        }
        if (testSteps.isEmpty()) {
            commonMethods.throwTesboException("Steps are not defined for test : " + testName,log);

        }

        return testSteps;
    }

    /**
     *
     * @param testsFileName
     * @param testName
     * @return
     */
    public String getTestDataSetByTestsFileAndTestCaseName(String testsFileName, String testName) {
        StringBuilder testsFileDetails = readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        String testDataSet = null;
        int startPoint = 0;
        int endpoint = 0;

        JSONObject startAndEndPoint=getTestStartAndEndPoint(allLines,testName,endStepNotFoundErrorMsg + testName + testSmallText);
        startPoint= Integer.parseInt(startAndEndPoint.get(startPointText).toString());
        endpoint= Integer.parseInt(startAndEndPoint.get(endPointText).toString());

        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].replaceAll(spaceRegex, " ").trim().startsWith(dataSetText)) {
                if (!(allLines[j].contains(dataSetText))) {
                    commonMethods.throwTesboException("Write 'DataSet' keyword in test",log);
                }
                testDataSet=allLines[j];
                break;
            }
            else{
                if(allLines[j].toLowerCase().startsWith("dataset")){
                    commonMethods.throwTesboException("Please add valid key word for: '"+allLines[j]+"'",log);
                }
            }
        }
        return testDataSet;
    }

    /**
     *
     * @param groupName
     * @return
     */
    public JSONArray getGroupTestStepByTestFileandTestCaseName(String groupName) {
        String testsFileName=getTestsFileNameWhoHasCollection(groupName);
        StringBuilder testsFileDetails = readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        Validation validation=new Validation();
        JSONArray testSteps = new JSONArray();
        int startPoint = 0;
        int endpoint = 0;
        JSONObject startAndEndPoint=getTestStartAndEndPointForGroupTest(allLines,groupName);
        startPoint= Integer.parseInt(startAndEndPoint.get(startPointText).toString());
        endpoint= Integer.parseInt(startAndEndPoint.get(endPointText).toString());

        if(startPoint==0 && endpoint==0) { commonMethods.throwTesboException("Collection name "+ groupName +" is not found on tests file",log); }

        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].startsWith(stepText) || allLines[j].startsWith(verifyText) || allLines[j].startsWith(codeText)) {
                testSteps.add(allLines[j]);
            }
            else{
                validation.keyWordValidation(allLines[j]);
            }
        }
        if (testSteps.isEmpty()) {
            commonMethods.throwTesboException("Steps are not defined for collection : " + groupName,log);
        }
        return testSteps;
    }

    /**
     *
     * @param allLines
     * @param groupName
     * @return
     */
    public JSONObject getTestStartAndEndPointForGroupTest(String[] allLines, String groupName){
        JSONObject startAndEndPoint=new JSONObject();
        int groupCount=0;
        boolean groupStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            groupCount=groupCount(groupStarted,groupCount,allLines[i]);
            if (allLines[i].startsWith(collectionNameText)) {
                String[] testNameArray = allLines[i].split(":");
                if (testNameArray[1].trim().toLowerCase().equalsIgnoreCase(groupName)) {
                    startAndEndPoint.put(startPointText,i);
                    groupStarted = true;
                }
                groupCount =testCount(groupCount,groupStarted);

            }
            if (groupStarted) {
                JSONObject endPoints=getTestEndStep(allLines[i],i);
                if(endPoints.size()!=0){
                    startAndEndPoint.put(endPointText,i);
                    endpoint= i;
                    break;
                }
            }
        }

        if(groupCount>=2 || endpoint==0) { commonMethods.throwTesboException(endStepNotFoundErrorMsg + groupName + testSmallText,log); }


        return startAndEndPoint;
    }

    /**
     *
     * @param groupStarted
     * @param groupCount
     * @param step
     * @return
     */
    public int groupCount(boolean groupStarted,int groupCount,String step){
        if(groupStarted && step.startsWith(testText)) {
            groupCount++;
        }
        return groupCount;
    }

    /**
     *
      * @param testFileDetails
     * @return
     */
    public JSONArray getTestNameByTestsFile(StringBuilder testFileDetails) {

        String[] allLines = testFileDetails.toString().split(newLineRegex);
        JSONArray testName = new JSONArray();

        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(testText) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {

                String[] testNameArray = allLines[i].split(":");
                if(testNameArray.length<2){
                    commonMethods.throwTesboException("Test name is blank '"+allLines[i]+"'",log);
                }
                testName.add(testNameArray[1].trim());
            }
            else {
                if(allLines[i].trim().toLowerCase().startsWith("test")){
                    commonMethods.throwTesboException("Please write valid keyword for this \"" +allLines[i]+"\"",log);
                }
            }
        }   // When No Test Available
        if (testName.isEmpty()) {
            return null;
        }
        return testName;
    }

    /**
     *
      * @param testsFileName
     * @param testName
     * @return
     */
    public JSONArray getSessionListFromTest(String testsFileName, String testName) {
        StringBuilder testsFileDetails = readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        JSONArray sessionName = new JSONArray();
        int startPoint = 0;
        int endpoint = 0;
        JSONObject startAndEndPoint=getTestStartAndEndPoint(allLines,testName,endStepNotFoundErrorMsg + testName + testSmallText);
        startPoint= Integer.parseInt(startAndEndPoint.get(startPointText).toString());
        endpoint= Integer.parseInt(startAndEndPoint.get(endPointText).toString());

        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].replaceAll(spaceRegex, " ").trim().startsWith("Session: ") ) {
                String[] sessionStep=allLines[j].split("[:|,]");
                for (String session:sessionStep)
                {
                    if(!(session.equals("Session")))
                    {
                        sessionName.add(session.trim());
                    }
                }
            }
            else if(allLines[j].replaceAll(spaceRegex, " ").trim().toLowerCase().startsWith("session")){
                commonMethods.throwTesboException(keywordErrorMsg +allLines[j]+"\"",log);
            }
        }
        return sessionName;
    }

    /**
     *
      * @param test
     * @return
     */
    public JSONArray getSeverityAndPriority(JSONObject test) {
        StringBuilder testsFileDetails = readTestsFile(test.get("testsFileName").toString());
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        JSONArray severityAndPriority = new JSONArray();
        String testName=test.get("testName").toString();
        int startPoint = 0;
        int endpoint = 0;
        JSONObject startAndEndPoint=getTestStartAndEndPoint(allLines,testName,"Step is not found for '" + testName + testSmallText);
        startPoint= Integer.parseInt(startAndEndPoint.get(startPointText).toString());
        endpoint= Integer.parseInt(startAndEndPoint.get(endPointText).toString());

        for (int j = startPoint; j < endpoint; j++) {

            if (allLines[j].replaceAll(spaceRegex, " ").trim().contains("Priority: ")
                    || allLines[j].replaceAll(spaceRegex, " ").trim().contains("Severity: ")) {
                severityAndPriority.add(allLines[j]);
            }
        }

        return severityAndPriority;
    }


    /**
     *
      * @param testFileName
     * @return
     */
    public boolean isBeforeTestInTestsFile(String testFileName) {
        StringBuilder testFileDetails = readTestsFile(testFileName);
        String[] allLines = testFileDetails.toString().split(newLineRegex);
        boolean isBeforeTest=false;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].trim().equals(beforeTestText)) {
                isBeforeTest=true;
            }
            if(isBeforeTest) {
                if (allLines[i].trim().equals("End") && !(allLines[i].contains(endText))) {
                    break;
                }
                endStepNotFound(allLines[i],beforeTestEndStepErrorMsg);
            }
            if(allLines[i].trim().equalsIgnoreCase("beforetest") && !(allLines[i].trim().equalsIgnoreCase(beforeTestText))) {
                commonMethods.throwTesboException(keywordErrorMsg +allLines[i]+"\"",log);
            }
        }

        return isBeforeTest;
    }

    /**
     *
     * @param stepLine
     * @param errorMsg
     */
    public void endStepNotFound(String stepLine,String errorMsg){
        if (stepLine.startsWith(testText) && stepLine.startsWith(collectionNameText)) {
            commonMethods.throwTesboException(errorMsg,log);
        }
    }

    /**
     *
      * @param testFileName
     * @return
     */
    public boolean isAfterTestInTestsFile(String testFileName) {
        StringBuilder testFileDetails = readTestsFile(testFileName);
        String[] allLines = testFileDetails.toString().split(newLineRegex);
        boolean isAfterTest=false;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].trim().equals(afterTestText)) {
                isAfterTest=true;
            }
            if(isAfterTest) {
                if (allLines[i].trim().equals("End") && !(allLines[i].contains(endText))) {
                    break;
                }
                endStepNotFound(allLines[i],"End Step is not found for AfterTest");
            }
            if(allLines[i].trim().equalsIgnoreCase("aftertest") && !(allLines[i].trim().equalsIgnoreCase(afterTestText))){
                commonMethods.throwTesboException(keywordErrorMsg +allLines[i]+"\"",log);
            }
        }

        return isAfterTest;
    }

    /**
     *
      * @param testsFileName
     * @param annotationName
     * @return
     */
    public JSONArray getBeforeAndAfterTestStepByTestsFile(String testsFileName, String annotationName) {
        StringBuilder testsFileDetails = readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        JSONArray annotationSteps = new JSONArray();
        Validation validation=new Validation();
        int startPoint = 0;
        int endpoint = 0;
        JSONObject startAndEndPoint=getTestStartAndEndPointForBeforeAndAfterTest(allLines,annotationName,endStepNotFoundErrorMsg + annotationName + testSmallText);
        startPoint= Integer.parseInt(startAndEndPoint.get(startPointText).toString());
        endpoint= Integer.parseInt(startAndEndPoint.get(endPointText).toString());

        for (int j = startPoint; j < endpoint; j++) {
            if(allLines[j].replaceAll(spaceRegex, " ").split(":").length<2 && allLines[j].replaceAll(spaceRegex, " ").startsWith(stepText)){
                commonMethods.throwTesboException("Step is blank '"+allLines[j]+"'",log);
            }
            if (allLines[j].replaceAll(spaceRegex, " ").trim().startsWith(stepText) || allLines[j].replaceAll(spaceRegex, " ").trim().startsWith(verifyText) ||
                    allLines[j].replaceAll(spaceRegex, " ").trim().startsWith("Collection: ") || (allLines[j].replaceAll(spaceRegex, " ").trim().contains("[Close:") && allLines[j].replaceAll(spaceRegex, " ").trim().contains("]")) ||
                    allLines[j].replaceAll(spaceRegex, " ").trim().startsWith(codeText) ||
                    ( allLines[j].replaceAll(spaceRegex, " ").trim().contains("[") && allLines[j].replaceAll(spaceRegex, " ").trim().contains("]") && !(allLines[j].replaceAll(spaceRegex, " ").trim().toLowerCase().contains("[close")))) {

                annotationSteps.add(allLines[j]);
            }
            else{
                validation.keyWordValidation(allLines[j]);
            }
        }
        if (annotationSteps.isEmpty()) {
            commonMethods.throwTesboException("Steps are not defined for annotation : " + annotationName,log);
        }
        return annotationSteps;
    }

    /**
     *
     * @param allLines
     * @param annotationName
     * @param errorMsg
     * @return
     */
    public JSONObject getTestStartAndEndPointForBeforeAndAfterTest(String[] allLines, String annotationName,String errorMsg){
        JSONObject startAndEndPoint=new JSONObject();

        int testCount=0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].equals(annotationName+":")) {

                startAndEndPoint.put(startPointText,i);
                testStarted = true;

                testCount=testCount(testCount,testStarted);

            }
            if (testStarted) {

                JSONObject endPoints=getTestEndStep(allLines[i],i);
                if(endPoints.size()!=0){
                    startAndEndPoint.put(endPointText,i);
                    endpoint= i;
                    break;
                }
            }
        }

        if(testCount>=2 || endpoint==0) {
            commonMethods.throwTesboException(errorMsg,log);
        }

        return startAndEndPoint;
    }

    /**
     *
      * @param testsFileName
     */
    public void getAnnotationDataSetByTestsFile(String testsFileName) {
        StringBuilder testsFileDetails = readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].equals(beforeTestText) || allLines[i].equals(afterTestText)) {
                startPoint = i;
                testStarted = true;
            }
            if(testStarted && (allLines[i].trim().equals("End") && !(allLines[i].contains(endText)))) {
                endpoint = i;
                break;
            }
        }
        verifyThatDataSetIsNotUseINAnnotation(startPoint,endpoint,allLines);

    }

    /**
     *
     * @param startPoint
     * @param endpoint
     * @param allLines
     */
    public void verifyThatDataSetIsNotUseINAnnotation(int startPoint,int endpoint,String[] allLines){
        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].replaceAll(spaceRegex, " ").trim().contains("DataSet:")) {
                commonMethods.throwTesboException("DataSet is not use in BeforeTest and AfterTest annotation",log);
            }
            if (allLines[j].replaceAll(spaceRegex, " ").trim().contains("{") && !(allLines[j].replaceAll(spaceRegex, " ").trim().contains("{DataSet."))) {
                commonMethods.throwTesboException("DataSet value not use directly in BeforeTest and AfterTest annotation",log);
            }
        }
    }

    /**
     *
      * @param testFileName
     * @param testName
     * @return
     */
    public String isRetry(String testFileName, String testName) {
        StringBuilder testFileDetails = readTestsFile(testFileName);
        String[] allLines = testFileDetails.toString().split(newLineRegex);
        int startPoint = 0;
        int endpoint = 0;
        JSONObject startAndEndPoint=getTestStartAndEndPoint(allLines,testName,endStepNotFoundErrorMsg + testName + testSmallText);
        startPoint= Integer.parseInt(startAndEndPoint.get(startPointText).toString());
        endpoint= Integer.parseInt(startAndEndPoint.get(endPointText).toString());
        String retry="null";

        for (int j = startPoint; j < endpoint; j++) {
            if(allLines[j].replaceAll(spaceRegex, " ").split(":").length<2 && allLines[j].replaceAll(spaceRegex, " ").contains("Retry:")){
                commonMethods.throwTesboException("Retry is blank '"+allLines[j]+"'",log);
            }
            if (allLines[j].replaceAll(spaceRegex, " ").trim().contains("Retry:") ) {
                retry=allLines[j].split(":")[1];
            }
        }

        return retry.trim();
    }

    /**
     *
     * @param allLines
     * @param testName
     * @param errorMsg
     * @return
     */
    public JSONObject getTestStartAndEndPoint(String[] allLines, String testName,String errorMsg){
        JSONObject startAndEndPoint=new JSONObject();

        int testCount=0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(testText) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {
                String[] testNameArray = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startAndEndPoint.put(startPointText,i);
                    testStarted = true;
                }
                testCount=testCount(testCount,testStarted);
            }
            if (testStarted) {
                JSONObject endPoints=getTestEndStep(allLines[i],i);
                if(endPoints.size()!=0){
                    startAndEndPoint.put(endPointText,i);
                    endpoint= i;
                    break;
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            commonMethods.throwTesboException(errorMsg,log);
        }

        return startAndEndPoint;
    }

    /**
     *
     * @param testCount
     * @param testStarted
     * @return
     */
    public int testCount(int testCount,boolean testStarted){
        if (testStarted) {
            testCount++;
        }
        return testCount;
    }

    /**
     *
     * @param step
     * @param i
     * @return
     */
    public JSONObject getTestEndStep(String step,int i){
        JSONObject endPoint=new JSONObject();
        if (step.trim().equals("End") && !(step.contains(endText))) {
            endPoint.put(endPointText,i);
            endPoint.put("isBreak",true);
        }
        if(step.replaceAll(spaceRegex, " ").trim().equals("end")){
            commonMethods.throwTesboException(endStepErrorMsg,log);
        }

        return endPoint;
    }

    /**
     *
      * @param collectionName
     * @return
     */
    public String getTestsFileNameWhoHasCollection(String collectionName){
        GetConfiguration configuration = new GetConfiguration();
        String directoryPath = configuration.getTestsDirectory();
        JSONArray testsFileList = getTestFiles(directoryPath);
        String testsFileName="";
        int numberOfCollectionFound=0;
        for(Object testsFile:testsFileList){
            File name = new File(testsFile.toString());
            StringBuilder testsFileDetails = readTestsFile(name.getName());
            String[] allLines = testsFileDetails.toString().split(newLineRegex);

            for (int i = 0; i < allLines.length; i++) {
                if (allLines[i].startsWith(collectionNameText)) {
                    String[] collectionNameArray = allLines[i].split(":");
                    if (collectionNameArray[1].trim().equals(collectionName)) {
                        numberOfCollectionFound++;
                        if(numberOfCollectionFound>=2){
                            commonMethods.throwTesboException("Multiple '"+collectionNameArray[1].trim()+"' collection name found in tests file",log);
                        }
                        testsFileName=name.getName();
                    }
                }
            }

        }
        verifyCollectionIsExistOnAnyTestFile(testsFileName,collectionName);
        return testsFileName;
    }

    /**
     *
     * @param testsFileName
     * @param collectionName
     */
    public void verifyCollectionIsExistOnAnyTestFile(String testsFileName,String collectionName){
        if(testsFileName.equals("")){
            commonMethods.throwTesboException("'"+collectionName+"' collection name not found in any tests file",log);
        }
    }
}
