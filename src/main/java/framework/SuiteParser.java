package framework;

import logger.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Exception.TesboException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import NoSuiteNameFoundException;

public class SuiteParser {

    Logger logger = new Logger();

    /**
     * @param directory
     * @return give all the file inside a directory
     */
    public JSONArray getSuites(String directory)  {

        JSONArray suiteFileList = new JSONArray();
        boolean flag=false;
        String file=null;
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            suiteFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
            for(Object suitePath:suiteFileList) {
                String[] suite=suitePath.toString().split("\\.");
                if (suite.length == 2) {
                    if (!suite[1].equalsIgnoreCase("suite")) {
                        flag=true;
                        if(file==null)
                            file="'."+suite[1]+"'";
                        else
                            file+=", '."+suite[1]+"'";
                    }
                }
            }
            if(flag==true){
                logger.errorLog(file+" file found");
                throw (new NoSuchFileException(""));
            }
        } catch (Exception e) {
            if(flag==true){
                logger.testFailed("Message : Please create only '.suite' file in suite directory.");
            }
            else {
                logger.testFailed("Message : Please Enter valid directory path.");
                logger.testFailed("'" + directory + "' no files found on your location.");
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.testFailed(sw.toString());
            }
            try {
                throw e;
            } catch (IOException ie) {
                StringWriter sw = new StringWriter();
                ie.printStackTrace(new PrintWriter(sw));
                logger.testFailed(sw.toString());
            }
        }

        return suiteFileList;
    }

    /**
     * @param fileName : File name with extension e.g. login.suite
     * @return whole file content as String buffer
     */
    public StringBuffer readSuiteFile(String fileName) {

        GetConfiguration configuration = new GetConfiguration();

        BufferedReader br = null;
        FileReader fr = null;

        StringBuffer suites = new StringBuffer();

        try {
            fr = new FileReader(configuration.getSuitesDirectory() + "/" + fileName);
            br = new BufferedReader(fr);
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                suites.append(sCurrentLine + "\n");
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
        } finally {

            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                logger.testFailed(sw.toString());
            }

        }
        return suites;
    }

    public String[] getSuiteData(StringBuffer sb) {
        String allLines[] = sb.toString().split("[\\r\\n]+");
        return allLines;
    }

    /**
     * @param tagName
     * @param suite
     * @return List of Test based on suite Data
     */
    public JSONArray getTestNameByTag(String tagName, StringBuffer suite) {

        String allLines[] = suite.toString().split("[\\r\\n]+");
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
                                throw new TesboException("Please write valid keyword for this \"" + allLines[i] + "\"");
                            }
                            String testNameArray[] = allLines[i].split(":");
                            if(testNameArray.length<2){
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

            //throw new NoTestFoundException("No test found in suite file");
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
        String directoryPath = configuration.getSuitesDirectory();
        JSONArray suiteFileList = getSuites(directoryPath);
        JSONObject allSuite = new JSONObject();

        JSONObject testNameWithSuites = new JSONObject();

        for (int i = 0; i < suiteFileList.size(); i++) {

            File name = new File(suiteFileList.get(i).toString());
            SuiteParser suiteName = new SuiteParser();
            allSuite.put(name.getName(), suiteName.readSuiteFile(name.getName()));
        }

        for (Object suite : allSuite.keySet()) {
            JSONArray testNames = getTestNameByTag(tag, (StringBuffer) allSuite.get(suite));
            if (testNames != null) {
                testNameWithSuites.put(suite.toString(), testNames);
            }
        }
        return testNameWithSuites;
    }

    /**
     * Not completed need to work on this...
     * @lastModifiedBy: Ankit Mistry
     * @param suiteName
     * @return
     */
    public JSONArray getTestStepBySuiteandTestCaseName(String suiteName, String testName) {
        StringBuffer suiteDetails = readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
        JSONArray testSteps = new JSONArray();
        Validation validation=new Validation();
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
                    throw new TesboException("Please define end step in a correct way: End");
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            throw new TesboException("End Step is not found for '" + testName + "' test");
        }
        for (int j = startPoint; j < endpoint; j++) {
            if(allLines[j].replaceAll("\\s{2,}", " ").split(":").length<2 && allLines[j].toString().replaceAll("\\s{2,}", " ").contains("Step:")){
                throw new TesboException("Step is blank '"+allLines[j]+"'");
            }
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Step:") | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Verify:") |
                    allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Collection:") | (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[Close:") && allLines[j].replaceAll("\\s{2,}", " ").trim().contains("]")) |
                    allLines[j].replaceAll("\\s{2,}", " ").trim().contains("ExtCode:") |
                    ( allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[") && allLines[j].replaceAll("\\s{2,}", " ").trim().contains("]") && !(allLines[j].replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("[close")))) {

                testSteps.add(allLines[j]);
            }
            else{
                validation.keyWordValidation(allLines[j]);
            }
        }
        if (testSteps.size() == 0) {
            throw new TesboException("Steps are not defined for test : " + testName);
        }


        return testSteps;
    }

    /**
     *
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     *
     * @param suiteName
     * @param testName
     * @return
     */
    public String getTestDataSetBySuiteAndTestCaseName(String suiteName, String testName) {
        StringBuffer suiteDetails = readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
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
                if (!(allLines[j].contains("DataSet:")))
                    throw new TesboException("Write 'DataSet' keyword in test");
                testDataSet=allLines[j];
                break;
            }
            else{
                if(allLines[j].toLowerCase().contains("dataset:") || allLines[j].toLowerCase().contains("dataset :")){
                    throw new TesboException("Please add valid key word for: '"+allLines[j]+"'");
                }
            }
        }

        return testDataSet;
    }


    public JSONArray getGroupTestStepBySuiteandTestCaseName(String suiteName, String groupName) {
        StringBuffer suiteDetails = readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
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
            throw new TesboException("Collection name "+ groupName +" is not found on suite");
        }
        if(groupCount>=2 || endpoint==0) {
            throw new TesboException("End Step is not found for '" + groupName + "' collection");
        }
        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].contains("Step:") | allLines[j].contains("Verify:") | allLines[j].contains("ExtCode:")) {
                testSteps.add(allLines[j]);
            }
        }
        if (testSteps.size() == 0) {
            throw new TesboException("Steps are not defined for collection : " + groupName);
        }
        return testSteps;
    }

    /**
     * @param suite
     * @return
     */
    public JSONArray getGroupName(StringBuffer suite) {
        String allLines[] = suite.toString().split("[\\r\\n]+");
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
     * @return
     * @throws Exception
     */
    public JSONObject getgroupName() throws Exception {

        GetConfiguration configuration = new GetConfiguration();
        String directoryPath = configuration.getSuitesDirectory();

        JSONArray suiteFileList = getSuites(directoryPath);
        JSONObject allSuite = new JSONObject();

        JSONObject testNameWithSuites = new JSONObject();

        for (int i = 0; i < suiteFileList.size(); i++) {

            File name = new File(suiteFileList.get(i).toString());
            SuiteParser suiteName = new SuiteParser();
            allSuite.put(name.getName(), suiteName.readSuiteFile(name.getName()));
        }

        for (Object suite : allSuite.keySet()) {
            for (String suiteName : configuration.getSuite()) {
                if (suite.toString().contains(suiteName)) {
                    /*JSONArray testNames = getTestNameByTag(tag, (StringBuffer) allSuite.get(suite));*/
                    JSONArray testNames = getGroupName((StringBuffer) allSuite.get(suite));
                    if (testNames != null) {
                        testNameWithSuites.put(suite.toString(), testNames);
                    }
                }
            }

        }


        return testNameWithSuites;
    }

    /**
     * Description : return test name
     *
     * @param suitename
     * @return
     */
    public JSONObject getTestNameBySuite(String suitename)  {
        GetConfiguration configuration = new GetConfiguration();
        String directoryPath = configuration.getSuitesDirectory();

        JSONArray suiteFileList = getSuites(directoryPath);
        JSONObject allSuite = new JSONObject();

        JSONObject testNameWithSuites = new JSONObject();

        for (int i = 0; i < suiteFileList.size(); i++) {
            File name = new File(suiteFileList.get(i).toString());
            SuiteParser suiteName = new SuiteParser();
            allSuite.put(name.getName(), suiteName.readSuiteFile(name.getName()));
        }

        for (Object suite : allSuite.keySet()) {
            String suiteName[] = suite.toString().split(".suite");
            if (suiteName[0].toString().toLowerCase().equals(suitename.toLowerCase())) {
                JSONArray testNames = getTestNameBysuit((StringBuffer) allSuite.get(suite));
                if (testNames != null) {
                    testNameWithSuites.put(suite.toString(), testNames);
                }
            }
        }
        return testNameWithSuites;
    }

    /**
     * @param suite
     * @return
     * @Description : get test name by suit.
     */
    public JSONArray getTestNameBysuit(StringBuffer suite) {

        String allLines[] = suite.toString().split("[\\r\\n]+");
        JSONArray testName = new JSONArray();


        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].contains("Test:") && !(allLines[i].contains("BeforeTest:") || allLines[i].contains("AfterTest:"))) {

                String testNameArray[] = allLines[i].split(":");
                if(testNameArray.length<2){
                    throw new TesboException("Test name is blank '"+allLines[i]+"'");
                }
                testName.add(testNameArray[1].trim());
            }
            else {
                if(allLines[i].contains("Test :") | allLines[i].contains("test:") | allLines[i].contains("test :")){
                    throw new TesboException("Please write valid keyword for this \"" +allLines[i]+"\"");
                }
            }
        }   // When No Test Available
        if (testName.size() == 0) {

            //throw new NoTestFoundException("No test found in suite file");
            return null;
        }
        return testName;
    }

    /**
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param suiteName
     * @param testName
     * @return
     */
    public JSONArray getSessionListFromTest(String suiteName, String testName) {
        StringBuffer suiteDetails = readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
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
                if (testStarted)
                    testCount++;
            }
            if (testStarted) {

                if (allLines[i].contains("End")) {
                    endpoint = i;
                    break;
                }
            }
        }

        if(testCount>=2 || endpoint==0) {
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
        SuiteParser suiteParser=new SuiteParser();

        StringBuffer suiteDetails = suiteParser.readSuiteFile(test.get("suiteName").toString());
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
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
     * @param suiteName
     * @return
     */
    public boolean isBeforeTestInSuite(String suiteName) {
        StringBuffer suiteDetails = readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
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
                    throw new TesboException("End Step is not found for BeforeTest");
                }

            }

            if (allLines[i].trim().equals("BeforeTest :") || allLines[i].trim().equals("beforeTest:") || allLines[i].trim().equals("beforeTest :")
                    || allLines[i].trim().equals("beforetest:") || allLines[i].trim().equals("beforetest :")
                    || allLines[i].trim().equals("Beforetest:") || allLines[i].trim().equals("Beforetest :")) {
                throw new TesboException("Please write valid keyword for this step \"" +allLines[i]+"\"");

            }
        }

        return isBeforeTest;
    }

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param suiteName
     * @return
     */
    public boolean isAfterTestInSuite(String suiteName) {

        StringBuffer suiteDetails = readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
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
                    throw new TesboException("End Step is not found for BeforeTest");
                }
            }
            if (allLines[i].trim().equals("AfterTest :") || allLines[i].trim().equals("afterTest:") || allLines[i].trim().equals("afterTest :")
                    || allLines[i].trim().equals("aftertest:") || allLines[i].trim().equals("aftertest :")
                    || allLines[i].trim().equals("Aftertest:") || allLines[i].trim().equals("Aftertest :")) {
                throw new TesboException("Please write valid keyword for this step \"" +allLines[i]+"\"");

            }
        }

        return isAfterTest;
    }

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param suiteName
     * @return
     */
    public JSONArray getBeforeAndAfterTestStepBySuite(String suiteName, String annotationName) {
        StringBuffer suiteDetails = readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
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
                    throw new TesboException("Please define end step in a correct way: End");
                }
            }
        }
        if(testCount>=2 || endpoint==0) {
            throw new TesboException("End Step is not found for '" + annotationName + "' test");
        }
        for (int j = startPoint; j < endpoint; j++) {
            if(allLines[j].replaceAll("\\s{2,}", " ").split(":").length<2 && allLines[j].toString().replaceAll("\\s{2,}", " ").contains("Step:")){
                throw new TesboException("Step is blank '"+allLines[j]+"'");
            }
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Step:") | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Verify:") |
                    allLines[j].replaceAll("\\s{2,}", " ").trim().contains("Collection:") | (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[Close:") && allLines[j].replaceAll("\\s{2,}", " ").trim().contains("]")) |
                    allLines[j].replaceAll("\\s{2,}", " ").trim().contains("ExtCode:") |
                    ( allLines[j].replaceAll("\\s{2,}", " ").trim().contains("[") && allLines[j].replaceAll("\\s{2,}", " ").trim().contains("]") && !(allLines[j].replaceAll("\\s{2,}", " ").trim().toLowerCase().contains("[close")))) {

                annotationSteps.add(allLines[j]);
            }
            else{
                validation.keyWordValidation(allLines[j]);
            }
        }
        if (annotationSteps.size() == 0) {
            throw new TesboException("Steps are not defined for annotation : " + annotationName);
        }


        return annotationSteps;
    }

    /**
     *
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     *
     * @param suiteName
     * @return
     */
    public void getAnnotationDataSetBySuite(String suiteName) {
        StringBuffer suiteDetails = readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
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
                throw new TesboException("DataSet is not use in BeforeTest and AfterTest annotation");
            }
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("{") && !(allLines[j].replaceAll("\\s{2,}", " ").trim().contains("{DataSet."))) {
                throw new TesboException("DataSet value not use directly in BeforeTest and AfterTest annotation");
            }

        }

    }
}