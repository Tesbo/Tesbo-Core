package framework;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import TestboException.NoTestStepFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import NoSuiteNameFoundException;

public class SuiteParser {



    /**
     * @param directory
     * @return give all the file inside a directory
     */
    public JSONArray getSuites(String directory) throws IOException {

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
                System.err.println(file+" file found");
                throw (new NoSuchFileException(""));
            }
        } catch (Exception e) {
            if(flag==true){
                System.err.println("Message : Please create only '.suite' file in suite directory.");
            }
            else {
                System.out.println("Message : Please Enter valid directory path.");
                System.out.println("'" + directory + "' no files found on your location.");
                e.printStackTrace();
            }
            throw e;
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
            e.printStackTrace();
        } finally {

            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (Exception ex) {
                ex.printStackTrace();
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
     * @return
     */
    public JSONArray getTestNameByTag(String tagName, StringBuffer suite) {

        String allLines[] = suite.toString().split("[\\r\\n]+");
        JSONArray testName = new JSONArray();


        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].toLowerCase().contains("test:") | allLines[i].toLowerCase().contains("test :")) {
                if (allLines[i + 1].toLowerCase().contains("#" + tagName.toLowerCase())) {
                    String testNameArray[] = allLines[i].split(":");
                    testName.add(testNameArray[1].trim());
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
     * @param tag
     * @return
     * @Discription : Get data as per the tag name
     */
    public JSONObject getTestNameByTag(String tag) throws Exception {

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
     *
     * @param suiteName
     * @return
     */
    public JSONArray getTestStepBySuiteandTestCaseName(String suiteName, String testName) {
        StringBuffer suiteDetails = readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
        JSONArray testSteps = new JSONArray();
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].toLowerCase().contains("test:") | allLines[i].toLowerCase().contains("test :")) {
                String testNameArray[] = allLines[i].split(":");
                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
            }
            if (testStarted) {
                if (allLines[i].toLowerCase().contains("end")) {
                    endpoint = i;
                    testStarted = false;
                }
            }
        }
        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("step:") | allLines[j].toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("step :") |
                    allLines[j].toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("verify:") | allLines[j].toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("verify :") | allLines[j].toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("collection:") | allLines[j].toLowerCase().replaceAll("\\s{2,}", " ").trim().contains("collection :")) {
                testSteps.add(allLines[j]);
            }
        }
        if (testSteps.size() == 0) {
            throw new NoTestStepFoundException("Steps are not defined for test : " + testName);
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
            if (allLines[i].toLowerCase().contains("test:") | allLines[i].toLowerCase().contains("test :")) {
                String testNameArray[] = allLines[i].split(":");
                if (testNameArray[1].trim().contains(testName)) {
                    startPoint = i;
                    testStarted = true;
                }
            }
            if (testStarted) {
                if (allLines[i].toLowerCase().contains("end")) {
                    endpoint = i;
                    break;
                }
            }
        }
        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].replaceAll("\\s{2,}", " ").trim().contains("DataSet :")
                    | allLines[j].replaceAll("\\s{2,}", " ").trim().contains("DataSet:")) {
                testDataSet=allLines[j];
                break;
            }
        }

        return testDataSet;
    }


    public JSONArray getGroupTestStepBySuiteandTestCaseName(String suiteName, String groupName) {
        StringBuffer suiteDetails = readSuiteFile(suiteName);
        String allLines[] = suiteDetails.toString().split("[\\r\\n]+");
        JSONArray testSteps = new JSONArray();
        int startPoint = 0;
        boolean testStarted = false;
        int endpoint = 0;
        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].toLowerCase().contains("collection name:") | allLines[i].toLowerCase().contains("collection name :")) {
                String testNameArray[] = allLines[i].split(":");
                if (testNameArray[1].trim().toLowerCase().equalsIgnoreCase(groupName)) {
                    startPoint = i;
                    testStarted = true;
                }
            }
            if (testStarted) {
                if (allLines[i].toLowerCase().contains("end")) {
                    endpoint = i;
                    testStarted = false;
                }
            }
        }
        for (int j = startPoint; j < endpoint; j++) {
            if (allLines[j].toLowerCase().contains("step:") | allLines[j].toLowerCase().contains("step :") |
                    allLines[j].toLowerCase().contains("verify:") | allLines[j].toLowerCase().contains("verify :")) {
                testSteps.add(allLines[j]);
            }
        }
        if (testSteps.size() == 0) {
            throw new NoTestStepFoundException("Steps are not defined for test : " + groupName);
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
            if (allLines[i].toLowerCase().contains("collection name:") | allLines[i].toLowerCase().contains("collection name :")) {
                /* if (allLines[i + 1].toLowerCase().contains("#" + tagName.toLowerCase())) {*/
                String testNameArray[] = allLines[i].split(":");
                testName.add(testNameArray[1].trim());
                /* }*/
            }
        }   // When No Test Available
        if (testName.size() == 0) {

            //throw new NoTestFoundException("No test found in suite file");
            return null;
        }
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
    public JSONObject getTestNameBySuite(String suitename) throws Exception {
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
            if (allLines[i].toLowerCase().contains("test:") | allLines[i].toLowerCase().contains("test :")) {
                String testNameArray[] = allLines[i].split(":");
                testName.add(testNameArray[1].trim());
            }
        }   // When No Test Available
        if (testName.size() == 0) {

            //throw new NoTestFoundException("No test found in suite file");
            return null;
        }
        return testName;
    }

}