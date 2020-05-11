package framework;

import Exception.*;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public boolean verifyThatTestsDirectoryHasOnlyTestFile(JSONArray testsFileList){
        String file=null;
        boolean flag=false;
        for(Object testsFilePath:testsFileList) {
            String[] testsFile=testsFilePath.toString().split("\\.");
            if (testsFile.length == 2) {
                if (!testsFile[1].equalsIgnoreCase("tests")) {
                    flag=true;
                    if(file==null)
                        file="'."+testsFile[1]+"'";
                    else
                        file+=", '."+testsFile[1]+"'";
                }
            }
        }
        return flag;
    }

    /**
     * @param fileName : File name with extension e.g. login.tests
     * @return whole file content as String buffer
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
     * @param tagName
     * @param testsFile
     * @return List of Test based on tests file Data
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

    public String getTestNameWhenTagNameIsExist(String[] tagArray,String tagName,String testLine){

        for(String  tag : tagArray) {
            if(!tag.equals("")) {
                if (tag.trim().equalsIgnoreCase(tagName)) {
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
        }
        return null;
    }

    /**
     * @param tag
     * @return
     * @Discription : Get data as per the tag name
     */
    public JSONObject getTestNameByTag(String tag)  {
        GetConfiguration configuration = new GetConfiguration();
        String directoryPath = configuration.getTestsDirectory();
        JSONArray testsFileList = getTestFiles(directoryPath);
        JSONObject allTestsFile = new JSONObject();

        JSONObject testNameWithTestsFile = new JSONObject();

        for (int i = 0; i < testsFileList.size(); i++) {

            File name = new File(testsFileList.get(i).toString());
            allTestsFile.put(name.getName(), readTestsFile(name.getName()));
        }

        for (Object testsFile : allTestsFile.keySet()) {
            JSONArray testNames = getTestNameByTag(tag, (StringBuilder) allTestsFile.get(testsFile));
            if (testNames != null) {
                testNameWithTestsFile.put(testsFile.toString(), testNames);
            }
        }
        return testNameWithTestsFile;
    }

    /**
     * Not completed need to work on this...
     * @lastModifiedBy: Ankit Mistry
     * @param testsFileName
     * @return
     */
    public JSONArray getTestStepByTestsFileandTestCaseName(String testsFileName, String testName) {
        StringBuilder testsFileDetails = readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        JSONArray testSteps = new JSONArray();
        Validation validation=new Validation();
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {

            if (allLines[i].startsWith(testText) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {
                String[] testNameArray = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i+1;
                    testStarted = true;
                }
                if (testStarted) {
                    testCount++;
                }
            }
            if (testStarted) {

                if (allLines[i].trim().equals("End") && !(allLines[i].contains(endText))) {
                    endpoint = i;
                    break;
                }
                if(allLines[i].replaceAll(spaceRegex, " ").trim().equals("end")){
                    log.error(endStepErrorMsg);
                    throw new TesboException(endStepErrorMsg);
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            String errorMsg=endStepNotFoundErrorMsg + testName + testSmallText;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        for (int j = startPoint; j < endpoint; j++) {
            String step=allLines[j].replaceAll(spaceRegex, " ").trim();
            if(step.split(":").length<2 && step.contains("Step:")){
                String errorMsg="Step is blank '"+allLines[j]+"'";
                log.error(errorMsg);
                throw new TesboException(errorMsg);
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
            String errorMsg="Steps are not defined for test : " + testName;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }

        return testSteps;
    }

    /**
     *
     * @auther : Ankit Mistry
     * @lastModifiedBy:
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
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(testText) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {
                String[] testNameArray = allLines[i].split(":");
                if(testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
            }
            if (testStarted) {
                if (allLines[i].trim().equals("End") && !(allLines[i].contains(endText))) {
                    endpoint = i;
                    break;
                }
            }
        }
        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].replaceAll(spaceRegex, " ").trim().startsWith(dataSetText)) {
                if (!(allLines[j].contains(dataSetText))) {
                    log.error("Write 'DataSet' keyword in test");
                    throw new TesboException("Write 'DataSet' keyword in test");
                }
                testDataSet=allLines[j];
                break;
            }
            else{
                if(allLines[j].toLowerCase().startsWith("dataset")){
                    String errorMsg="Please add valid key word for: '"+allLines[j]+"'";
                    log.error(errorMsg);
                    throw new TesboException(errorMsg);
                }
            }
        }
        return testDataSet;
    }


    public JSONArray getGroupTestStepByTestFileandTestCaseName(String groupName) {
        String testsFileName=getTestsFileNameWhoHasCollection(groupName);
        StringBuilder testsFileDetails = readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        Validation validation=new Validation();
        JSONArray testSteps = new JSONArray();
        int startPoint = 0;
        int groupCount=0;
        boolean groupStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if(groupStarted){
                if (allLines[i].startsWith(testText)) {
                    groupCount++;
                }
            }
            if (allLines[i].startsWith(collectionNameText)) {
                String[] testNameArray = allLines[i].split(":");
                if (testNameArray[1].trim().toLowerCase().equalsIgnoreCase(groupName)) {
                    startPoint = i;
                    groupStarted = true;
                }
                if (groupStarted) {
                    groupCount++;
                }
            }
            if (groupStarted) {
                if (allLines[i].trim().equals("End") && !(allLines[i].contains(endText))) {
                    endpoint = i;
                    groupStarted = false;
                }
            }
        }
        if(startPoint==0 && endpoint==0)
        {
            String errorMsg="Collection name "+ groupName +" is not found on tests file";
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        if(groupCount>=2 || endpoint==0) {
            String errorMsg=endStepNotFoundErrorMsg + groupName + testSmallText;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].startsWith(stepText) || allLines[j].startsWith(verifyText) || allLines[j].startsWith(codeText)) {
                testSteps.add(allLines[j]);
            }
            else{
                validation.keyWordValidation(allLines[j]);
            }
        }
        if (testSteps.isEmpty()) {
            String errorMsg="Steps are not defined for collection : " + groupName;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        return testSteps;
    }

    /**
     * @param testsFileDetails
     * @return
     */
    public JSONArray getGroupName(StringBuilder testsFileDetails) {
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        JSONArray testName = new JSONArray();
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(collectionNameText)) {
                String[] testNameArray = allLines[i].split(":");
                testName.add(testNameArray[1].trim());
            }
        }   // When No Test Available
        if (testName.isEmpty()) { return null; }

        return testName;
    }

    /**
     * Description : return test name
     *
     * @param testsFileName
     * @return
     */
    public JSONObject getTestNameBySuite(String testsFileName)  {
        GetConfiguration configuration = new GetConfiguration();
        String directoryPath = configuration.getTestsDirectory();

        JSONArray testsFileList = getTestFiles(directoryPath);
        JSONObject allTestsFile = new JSONObject();

        JSONObject testNameWithTestsFile = new JSONObject();

        for (int i = 0; i < testsFileList.size(); i++) {
            File name = new File(testsFileList.get(i).toString());
            allTestsFile.put(name.getName(), readTestsFile(name.getName()));
        }

        for (Object testsFile : allTestsFile.keySet()) {
            String[] testFileName = testsFile.toString().split(".tests");
            if (testFileName[0].equalsIgnoreCase(testsFileName)) {
                JSONArray testNames = getTestNameByTestsFile((StringBuilder) allTestsFile.get(testsFile));
                if (testNames != null) {
                    testNameWithTestsFile.put(testsFile.toString(), testNames);
                }
            }
        }
        return testNameWithTestsFile;
    }

    /**
     * @param testFileDetails
     * @return
     * @Description : get test name by suit.
     */
    public JSONArray getTestNameByTestsFile(StringBuilder testFileDetails) {

        String[] allLines = testFileDetails.toString().split(newLineRegex);
        JSONArray testName = new JSONArray();

        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(testText) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {

                String[] testNameArray = allLines[i].split(":");
                if(testNameArray.length<2){
                    String errorMsg="Test name is blank '"+allLines[i]+"'";
                    log.error(errorMsg);
                    throw new TesboException(errorMsg);
                }
                testName.add(testNameArray[1].trim());
            }
            else {
                if(allLines[i].trim().toLowerCase().startsWith("test")){
                    String errorMsg="Please write valid keyword for this \"" +allLines[i]+"\"";
                    log.error(errorMsg);
                    throw new TesboException(errorMsg);
                }
            }
        }   // When No Test Available
        if (testName.isEmpty()) {
            return null;
        }
        return testName;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param testsFileName
     * @param testName
     * @return
     */
    public JSONArray getSessionListFromTest(String testsFileName, String testName) {
        StringBuilder testsFileDetails = readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        JSONArray sessionName = new JSONArray();
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(testText) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {
                String[] testNameArray = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted){testCount++;}
            }
            if (testStarted) {

                if (allLines[i].trim().equals("End") && !(allLines[i].contains(endText))) {
                    endpoint = i;
                    break;
                }
            }
        }

        if(testCount>=2 || endpoint==0) {
            String errorMsg=endStepNotFoundErrorMsg + testName + testSmallText;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }

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
                String errorMsg=keywordErrorMsg +allLines[j]+"\"";
                log.error(errorMsg);
                throw new TesboException(errorMsg);
            }
        }
        return sessionName;
    }

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param test
     *
     */
    public JSONArray getSeverityAndPriority(JSONObject test) {
        StringBuilder testsFileDetails = readTestsFile(test.get("testsFileName").toString());
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        JSONArray severityAndPriority = new JSONArray();
        String testName=test.get("testName").toString();
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(testText) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {
                String[] testNameArray = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted) {
                    testCount++;
                }
            }
            if (testStarted) {

                if (allLines[i].startsWith(stepText)) {
                    endpoint = i;
                    break;
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            String errorMsg="Step is not found for '" + testName + testSmallText;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }

        for (int j = startPoint; j < endpoint; j++) {

            if (allLines[j].replaceAll(spaceRegex, " ").trim().contains("Priority: ")
                    || allLines[j].replaceAll(spaceRegex, " ").trim().contains("Severity: ")) {
                severityAndPriority.add(allLines[j]);
            }
        }

        return severityAndPriority;
    }

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
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

    public void endStepNotFound(String stepLine,String errorMsg){
        if (stepLine.startsWith(testText) && stepLine.startsWith(collectionNameText)) {
            commonMethods.throwTesboException(errorMsg,log);
        }
    }

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
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
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param testsFileName
     * @return
     */
    public JSONArray getBeforeAndAfterTestStepByTestsFile(String testsFileName, String annotationName) {
        StringBuilder testsFileDetails = readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split(newLineRegex);
        JSONArray annotationSteps = new JSONArray();
        Validation validation=new Validation();
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].equals(annotationName+":")) {

                startPoint = i;
                testStarted = true;

                if (testStarted) {
                    testCount++;
                }
            }
            if (testStarted) {

                if (allLines[i].trim().equals("End")) {
                    endpoint = i;
                    break;
                }
                if(allLines[i].replaceAll(spaceRegex, " ").trim().equals("end")){
                    log.error(endStepErrorMsg);
                    throw new TesboException(endStepErrorMsg);
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            String errorMsg=endStepNotFoundErrorMsg + annotationName + testSmallText;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        for (int j = startPoint; j < endpoint; j++) {
            if(allLines[j].replaceAll(spaceRegex, " ").split(":").length<2 && allLines[j].replaceAll(spaceRegex, " ").startsWith(stepText)){
                String errorMsg="Step is blank '"+allLines[j]+"'";
                log.error(errorMsg);
                throw new TesboException(errorMsg);
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
            String errorMsg="Steps are not defined for annotation : " + annotationName;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        return annotationSteps;
    }

    /**
     *
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     *
     * @param testsFileName
     * @return
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
            if (testStarted) {
                if (allLines[i].trim().equals("End") && !(allLines[i].contains(endText))) {
                    endpoint = i;
                    break;
                }
            }
        }
        verifyThatDataSetIsNotUseINAnnotation(startPoint,endpoint,allLines);

    }

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
     * Not completed need to work on this...
     * @lastModifiedBy: Ankit Mistry
     * @param testFileName
     * @return
     */
    public String isRetry(String testFileName, String testName) {
        StringBuilder testFileDetails = readTestsFile(testFileName);
        String[] allLines = testFileDetails.toString().split(newLineRegex);
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].startsWith(testText) && !(allLines[i].startsWith(beforeTestText) || allLines[i].startsWith(afterTestText))) {
                String[] testNameArray = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted) {
                    testCount++;
                }
            }
            if (testStarted) {

                if (allLines[i].trim().equals("End") && !(allLines[i].contains(endText))) {
                    endpoint = i;
                    break;
                }
                if(allLines[i].replaceAll(spaceRegex, " ").trim().equals("end")){
                    log.error(endStepErrorMsg);
                    throw new TesboException(endStepErrorMsg);
                }
            }
        }
        String retry="null";
        if(testCount>=2 || endpoint==0) {
            String errorMsg=endStepNotFoundErrorMsg + testName + testSmallText;
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        for (int j = startPoint; j < endpoint; j++) {
            if(allLines[j].replaceAll(spaceRegex, " ").split(":").length<2 && allLines[j].replaceAll(spaceRegex, " ").contains("Retry:")){
                String errorMsg="Retry is blank '"+allLines[j]+"'";
                log.error(errorMsg);
                throw new TesboException(errorMsg);
            }
            if (allLines[j].replaceAll(spaceRegex, " ").trim().contains("Retry:") ) {
                retry=allLines[j].split(":")[1];
            }

        }

        return retry.trim();
    }

    /**
     * Find Collection name from all tests file
     * @auther: Ankit Mistry
     * @lastModifiedBy:
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
    public void verifyCollectionIsExistOnAnyTestFile(String testsFileName,String collectionName){
        if(testsFileName.equals("")){
            commonMethods.throwTesboException("'"+collectionName+"' collection name not found in any tests file",log);
        }
    }
}
