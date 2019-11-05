package framework;

import Execution.Tesbo;
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
    private static final Logger log = LogManager.getLogger(Tesbo.class);
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
            if(flag==true){
                log.error(file+" file not found");
                tesboLogger.errorLog(file+" file not found");
                throw (new NoSuchFileException(""));
            }
        } catch (Exception e) {
            if(flag==true){
                log.error("Message : Please create only '.tests' file in tests directory.");
                tesboLogger.testFailed("Message : Please create only '.tests' file in tests directory.");
            }
            else {
                log.error("Message : Please Enter valid directory path.");
                log.error("'" + directory + "' no files found on your location.");
                tesboLogger.testFailed("Message : Please Enter valid directory path.");
                tesboLogger.testFailed("'" + directory + "' no files found on your location.");
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
            }
            try {
                throw e;
            } catch (IOException ie) {
                StringWriter sw = new StringWriter();
                ie.printStackTrace(new PrintWriter(sw));
                tesboLogger.testFailed(sw.toString());
                log.error(sw.toString());
            }
        }
        return testsFileList;
    }

    /**
     * @param fileName : File name with extension e.g. login.tests
     * @return whole file content as String buffer
     */
    public StringBuffer readTestsFile(String fileName) {

        GetConfiguration configuration = new GetConfiguration();
        BufferedReader br = null;
        FileReader fr = null;
        StringBuffer tests = new StringBuffer();

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

    public String[] getTestsData(StringBuffer sb) {
        String allLines[] = sb.toString().split("[\\r\\n]+");
        return allLines;
    }

    /**
     * @param tagName
     * @param testsFile
     * @return List of Test based on tests file Data
     */
    public JSONArray getTestNameByTag(String tagName, StringBuffer testsFile) {

        String allLines[] = testsFile.toString().split("[\\r\\n]+");
        JSONArray testName = new JSONArray();


        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].toLowerCase().contains("test:") | allLines[i].toLowerCase().contains("test :")) {
                String tagLine = allLines[i + 1].toLowerCase();
                String[] tagArray = tagLine.replaceAll("\\s+","").split("#");
                for(String  tag : tagArray)
                {
                    if(!tag.equals("")) {
                        if (tag.toLowerCase().trim().equals(tagName.toLowerCase())) {
                            if (allLines[i].contains("Test :") | allLines[i].contains("test:") | allLines[i].contains("test :")) {
                                log.error("Please write valid keyword for this \"" + allLines[i] + "\"");
                                throw new TesboException("Please write valid keyword for this \"" + allLines[i] + "\"");
                            }
                            String testNameArray[] = allLines[i].split(":");
                            if(testNameArray.length<2){
                                log.error("Test name is blank '"+allLines[i]+"'");
                                throw new TesboException("Test name is blank '"+allLines[i]+"'");
                            }
                            testName.add(testNameArray[1].trim());
                        }
                    }
                }

            }

        }
        // When No Test Available
        if (testName.size() == 0) {
            //throw new NoTestFoundException("No test found in tests file");
            return null;
        }
        return testName;
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
            JSONArray testNames = getTestNameByTag(tag, (StringBuffer) allTestsFile.get(testsFile));
            if (testNames != null) {
                testNameWithTestsFile.put(testsFile.toString(), testNames);
            }
        }
        return testNameWithTestsFile;
    }

    /**
     * Not completed need to work on this...
     * @lastModifiedBy: Ankit Mistry
     * @param TestsFileName
     * @return
     */
    public JSONArray getTestStepByTestsFileandTestCaseName(String TestsFileName, String testName) {
        StringBuffer testsFileDetails = readTestsFile(TestsFileName);
        String allLines[] = testsFileDetails.toString().split("[\\r\\n]+");
        JSONArray testSteps = new JSONArray();
        Validation validation=new Validation();
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        boolean isIf=false;
        for (int i = 0; i < allLines.length; i++) {

            if (allLines[i].contains("Test:") && !(allLines[i].contains("BeforeTest:") || allLines[i].contains("AfterTest:"))) {
                String testNameArray[] = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted) {
                    testCount++;
                }
            }
            if (testStarted) {

                if(allLines[i].contains("If::") && isIf==true){isIf=true;}

                if (allLines[i].contains("End") && !(allLines[i].contains("End::"))) {
                    endpoint = i;
                    break;
                }
                if(allLines[i].replaceAll("\\s{2,}", " ").trim().equals("end")){
                    log.error("Please define end step in a correct way: End");
                    throw new TesboException("Please define end step in a correct way: End");
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            log.error("End Step is not found for '" + testName + "' test");
            throw new TesboException("End Step is not found for '" + testName + "' test");
        }
        for (int j = startPoint; j < endpoint; j++) {
            if(allLines[j].replaceAll("\\s{2,}", " ").split(":").length<2 && allLines[j].toString().replaceAll("\\s{2,}", " ").contains("Step:")){
                log.error("Step is blank '"+allLines[j]+"'");
                throw new TesboException("Step is blank '"+allLines[j]+"'");
            }
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Step:") | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Verify:") |
                    allLines[j].replaceAll("\\s{2,}", " ").trim().contains("If::") | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Else::") | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("If Else::") | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("End::") |
                    allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Collection:") | (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[Close:") && allLines[j].replaceAll("\\s{2,}", " ").trim().contains("]")) |
                    (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Code:") && !allLines[j].replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("extcode:")) |
                    ( allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[") && allLines[j].replaceAll("\\s{2,}", " ").trim().contains("]") && !(allLines[j].replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("[close")))) {

                testSteps.add(allLines[j]);
            }
            else{
                validation.keyWordValidation(allLines[j]);
            }
        }
        if (testSteps.size() == 0) {
            log.error("Steps are not defined for test : " + testName);
            throw new TesboException("Steps are not defined for test : " + testName);
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
        StringBuffer testsFileDetails = readTestsFile(testsFileName);
        String allLines[] = testsFileDetails.toString().split("[\\r\\n]+");
        String testDataSet = null;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("Test:") && !(allLines[i].contains("BeforeTest:") || allLines[i].contains("AfterTest:"))) {
                String testNameArray[] = allLines[i].split(":");
                if(testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
            }
            if (testStarted) {
                if (allLines[i].contains("End")) {
                    endpoint = i;
                    break;
                }
            }
        }
        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("DataSet:")) {
                if (!(allLines[j].contains("DataSet:"))) {
                    log.error("Write 'DataSet' keyword in test");
                    throw new TesboException("Write 'DataSet' keyword in test");
                }
                testDataSet=allLines[j];
                break;
            }
            else{
                if(allLines[j].toLowerCase().contains("dataset:") || allLines[j].toLowerCase().contains("dataset :")){
                    log.error("Please add valid key word for: '"+allLines[j]+"'");
                    throw new TesboException("Please add valid key word for: '"+allLines[j]+"'");
                }
            }
        }
        return testDataSet;
    }


    public JSONArray getGroupTestStepByTestFileandTestCaseName(String testsFileName, String groupName) {
        StringBuffer testsFileDetails = readTestsFile(testsFileName);
        String allLines[] = testsFileDetails.toString().split("[\\r\\n]+");
        Validation validation=new Validation();
        JSONArray testSteps = new JSONArray();
        int startPoint = 0;
        int groupCount=0;
        boolean groupStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if(groupStarted){
                if (allLines[i].contains("Test:")) {
                    groupCount++;
                }
            }
            if (allLines[i].contains("Collection Name:")) {
                String testNameArray[] = allLines[i].split(":");
                if (testNameArray[1].trim().toLowerCase().equalsIgnoreCase(groupName)) {
                    startPoint = i;
                    groupStarted = true;
                }
                if (groupStarted) {
                    groupCount++;
                }
            }
            if (groupStarted) {
                if (allLines[i].contains("End")) {
                    endpoint = i;
                    groupStarted = false;
                }
            }
        }
        if(startPoint==0)
        {
            log.error("Collection name "+ groupName +" is not found on tests file");
            throw new TesboException("Collection name "+ groupName +" is not found on tests file");
        }
        if(groupCount>=2 || endpoint==0) {
            log.error("End Step is not found for '" + groupName + "' collection");
            throw new TesboException("End Step is not found for '" + groupName + "' collection");
        }
        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].contains("Step:") | allLines[j].contains("Verify:") | (allLines[j].contains("Code:") & !allLines[j].replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("extcode:"))) {
                testSteps.add(allLines[j]);
            }
            else{
                validation.keyWordValidation(allLines[j]);
            }
        }
        if (testSteps.size() == 0) {
            log.error("Steps are not defined for collection : " + groupName);
            throw new TesboException("Steps are not defined for collection : " + groupName);
        }
        return testSteps;
    }

    /**
     * @param testsFileDetails
     * @return
     */
    public JSONArray getGroupName(StringBuffer testsFileDetails) {
        String allLines[] = testsFileDetails.toString().split("[\\r\\n]+");
        JSONArray testName = new JSONArray();
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("Collection Name:")) {
                String testNameArray[] = allLines[i].split(":");
                testName.add(testNameArray[1].trim());
            }
        }   // When No Test Available
        if (testName.size() == 0) { return null; }

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
            String testFileName[] = testsFile.toString().split(".tests");
            if (testFileName[0].toLowerCase().equals(testsFileName.toLowerCase())) {
                JSONArray testNames = getTestNameByTestsFile((StringBuffer) allTestsFile.get(testsFile));
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
    public JSONArray getTestNameByTestsFile(StringBuffer testFileDetails) {

        String allLines[] = testFileDetails.toString().split("[\\r\\n]+");
        JSONArray testName = new JSONArray();

        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("Test:") && !(allLines[i].contains("BeforeTest:") || allLines[i].contains("AfterTest:"))) {

                String testNameArray[] = allLines[i].split(":");
                if(testNameArray.length<2){
                    log.error("Test name is blank '"+allLines[i]+"'");
                    throw new TesboException("Test name is blank '"+allLines[i]+"'");
                }
                testName.add(testNameArray[1].trim());
            }
            else {
                if(allLines[i].contains("Test :") | allLines[i].contains("test:") | allLines[i].contains("test :")){
                    log.error("Please write valid keyword for this \"" +allLines[i]+"\"");
                    throw new TesboException("Please write valid keyword for this \"" +allLines[i]+"\"");
                }
            }
        }   // When No Test Available
        if (testName.size() == 0) {
            //throw new NoTestFoundException("No test found in tests file");
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
        StringBuffer testsFileDetails = readTestsFile(testsFileName);
        String allLines[] = testsFileDetails.toString().split("[\\r\\n]+");
        JSONArray sessionName = new JSONArray();
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("Test:") && !(allLines[i].contains("BeforeTest:") || allLines[i].contains("AfterTest:"))) {
                String testNameArray[] = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted){testCount++;}
            }
            if (testStarted) {

                if (allLines[i].contains("End")) {
                    endpoint = i;
                    break;
                }
            }
        }

        if(testCount>=2 || endpoint==0) {
            log.error("End Step is not found for '" + testName + "' test");
            throw new TesboException("End Step is not found for '" + testName + "' test");
        }

        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Session:") ) {
                String[] SessionStep=allLines[j].split("[:|,]");
                for (String session:SessionStep)
                {
                    if(!(session.equals("Session")))
                    {
                        sessionName.add(session.trim());
                    }
                }
            }
            else if(allLines[j].replaceAll("\\s{2,}", " ").trim().contains("session:") | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("session :") | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Session :")){
                log.error("Please write valid keyword for this step \"" +allLines[j]+"\"");
                throw new TesboException("Please write valid keyword for this step \"" +allLines[j]+"\"");
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
        StringBuffer testsFileDetails = readTestsFile(test.get("testsFileName").toString());
        String allLines[] = testsFileDetails.toString().split("[\\r\\n]+");
        JSONArray severityAndPriority = new JSONArray();
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("Test:") && !(allLines[i].contains("BeforeTest:") || allLines[i].contains("AfterTest:"))) {
                String testNameArray[] = allLines[i].split(":");

                if (testNameArray[1].trim().contains(test.get("testName").toString())) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted) {
                    testCount++;
                }
            }
            if (testStarted) {

                if (allLines[i].contains("Step:")) {
                    endpoint = i;
                    break;
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            log.error("Step is not found for '" + test.get("testName").toString() + "' test");
            throw new TesboException("Step is not found for '" + test.get("testName").toString() + "' test");
        }

        for (int j = startPoint; j < endpoint; j++) {

            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Priority:")
                    | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Severity:")) {
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
        StringBuffer testFileDetails = readTestsFile(testFileName);
        String allLines[] = testFileDetails.toString().split("[\\r\\n]+");
        boolean isBeforeTest=false;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].trim().equals("BeforeTest:")) {
                isBeforeTest=true;
            }
            if(isBeforeTest) {
                if (allLines[i].trim().equals("End")) {
                    break;
                }
                if (allLines[i].contains("Test:") && allLines[i].contains("Collection Name:")) {
                    log.error("End Step is not found for BeforeTest");
                    throw new TesboException("End Step is not found for BeforeTest");
                }

            }

            if (allLines[i].trim().equals("BeforeTest :") || allLines[i].trim().equals("beforeTest:") || allLines[i].trim().equals("beforeTest :")
                    || allLines[i].trim().equals("beforetest:") || allLines[i].trim().equals("beforetest :")
                    || allLines[i].trim().equals("Beforetest:") || allLines[i].trim().equals("Beforetest :")) {
                log.error("Please write valid keyword for this step \"" +allLines[i]+"\"");
                throw new TesboException("Please write valid keyword for this step \"" +allLines[i]+"\"");
            }
        }

        return isBeforeTest;
    }

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param testFileName
     * @return
     */
    public boolean isAfterTestInTestsFile(String testFileName) {

        StringBuffer testFileDetails = readTestsFile(testFileName);
        String allLines[] = testFileDetails.toString().split("[\\r\\n]+");
        boolean isAfterTest=false;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].trim().equals("AfterTest:")) {
                isAfterTest=true;
            }
            if(isAfterTest) {
                if (allLines[i].trim().equals("End")) {
                    break;
                }
                if (allLines[i].contains("Test:") && allLines[i].contains("Collection Name:")) {
                    log.error("End Step is not found for BeforeTest");
                    throw new TesboException("End Step is not found for BeforeTest");
                }
            }
            if (allLines[i].trim().equals("AfterTest :") || allLines[i].trim().equals("afterTest:") || allLines[i].trim().equals("afterTest :")
                    || allLines[i].trim().equals("aftertest:") || allLines[i].trim().equals("aftertest :")
                    || allLines[i].trim().equals("Aftertest:") || allLines[i].trim().equals("Aftertest :")) {
                log.error("Please write valid keyword for this step \"" +allLines[i]+"\"");
                throw new TesboException("Please write valid keyword for this step \"" +allLines[i]+"\"");

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
        StringBuffer testsFileDetails = readTestsFile(testsFileName);
        String allLines[] = testsFileDetails.toString().split("[\\r\\n]+");
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

                if (allLines[i].contains("End")) {
                    endpoint = i;
                    break;
                }
                if(allLines[i].replaceAll("\\s{2,}", " ").trim().equals("end")){
                    log.error("Please define end step in a correct way: End");
                    throw new TesboException("Please define end step in a correct way: End");
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            log.error("End Step is not found for '" + annotationName + "' test");
            throw new TesboException("End Step is not found for '" + annotationName + "' test");
        }
        for (int j = startPoint; j < endpoint; j++) {
            if(allLines[j].replaceAll("\\s{2,}", " ").split(":").length<2 && allLines[j].toString().replaceAll("\\s{2,}", " ").contains("Step:")){
                log.error("Step is blank '"+allLines[j]+"'");
                throw new TesboException("Step is blank '"+allLines[j]+"'");
            }
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Step:") | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Verify:") |
                    allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Collection:") | (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[Close:") && allLines[j].replaceAll("\\s{2,}", " ").trim().contains("]")) |
                    allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Code:") |
                    ( allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[") && allLines[j].replaceAll("\\s{2,}", " ").trim().contains("]") && !(allLines[j].replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("[close")))) {

                annotationSteps.add(allLines[j]);
            }
            else{
                validation.keyWordValidation(allLines[j]);
            }
        }
        if (annotationSteps.size() == 0) {
            log.error("Steps are not defined for annotation : " + annotationName);
            throw new TesboException("Steps are not defined for annotation : " + annotationName);
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
        StringBuffer testsFileDetails = readTestsFile(testsFileName);
        String allLines[] = testsFileDetails.toString().split("[\\r\\n]+");
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("BeforeTest:") || allLines[i].contains("AfterTest:")) {
                startPoint = i;
                testStarted = true;
            }
            if (testStarted) {
                if (allLines[i].contains("End")) {
                    endpoint = i;
                    break;
                }
            }
        }
        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("DataSet:")) {
                log.error("DataSet is not use in BeforeTest and AfterTest annotation");
                throw new TesboException("DataSet is not use in BeforeTest and AfterTest annotation");
            }
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("{") && !(allLines[j].replaceAll("\\s{2,}", " ").trim().contains("{DataSet."))) {
                log.error("DataSet value not use directly in BeforeTest and AfterTest annotation");
                throw new TesboException("DataSet value not use directly in BeforeTest and AfterTest annotation");
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
        StringBuffer testFileDetails = readTestsFile(testFileName);
        String allLines[] = testFileDetails.toString().split("[\\r\\n]+");
        int testCount=0;
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("Test:") && !(allLines[i].contains("BeforeTest:") || allLines[i].contains("AfterTest:"))) {
                String testNameArray[] = allLines[i].split(":");

                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
                if (testStarted) {
                    testCount++;
                }
            }
            if (testStarted) {

                if (allLines[i].contains("End")) {
                    endpoint = i;
                    break;
                }
                if(allLines[i].replaceAll("\\s{2,}", " ").trim().equals("end")){
                    log.error("Please define end step in a correct way: End");
                    throw new TesboException("Please define end step in a correct way: End");
                }
            }
        }
        String retry="null";
        if(testCount>=2 || endpoint==0) {
            log.error("End Step is not found for '" + testName + "' test");
            throw new TesboException("End Step is not found for '" + testName + "' test");
        }
        for (int j = startPoint; j < endpoint; j++) {
            if(allLines[j].replaceAll("\\s{2,}", " ").split(":").length<2 && allLines[j].toString().replaceAll("\\s{2,}", " ").contains("Retry:")){
                log.error("Retry is blank '"+allLines[j]+"'");
                throw new TesboException("Retry is blank '"+allLines[j]+"'");
            }
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Retry:") ) {
                retry=allLines[j].split(":")[1];
            }

        }

        return retry.trim();
    }

}
