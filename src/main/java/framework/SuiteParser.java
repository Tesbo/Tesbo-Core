package framework;

import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONObject;

public class SuiteParser {
    TesboLogger tesboLogger = new TesboLogger();
    CommonMethods commonMethods=new CommonMethods();
    private static final Logger log = LogManager.getLogger(SuiteParser.class);

    /**
     *
     * @param fileName
     * @return
     */
    public StringBuilder readSuiteFile(String fileName) {

        GetConfiguration configuration = new GetConfiguration();
        BufferedReader br = null;
        FileReader fr = null;
        StringBuilder tests = new StringBuilder();

        try {
            fr = new FileReader(configuration.getSuitesDirectory() + "/" + fileName+".suite");
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
      * @param testsFile
     * @return
     */
    public JSONArray getTestNameFromSuiteFile(StringBuilder testsFile) {

        String[] allLines = testsFile.toString().split("[\\r\\n]+");
        JSONArray testName = new JSONArray();
        for (int i = 0; i < allLines.length; i++) {
            if(!allLines[i].equals("")) {
                String comment = String.valueOf(allLines[i].charAt(0)) + String.valueOf(allLines[i].charAt(1));
                if ((allLines[i].toLowerCase().startsWith("test")) && (!comment.equals("//"))) {
                    verifyTestStartWithValidText(allLines[i]);
                    testName.add(allLines[i].trim());
                } else {
                    if (!comment.equals("//")) {
                        commonMethods.throwTesboException("Define only test name in '.suite' file",log);
                    }
                }
            }

        }

        validationForDuplicateTestInSuiteFile(testName);
        return testName;
    }

    /**
     *
     * @param testName
     */
    public void verifyTestStartWithValidText(String testName){
        if (!testName.trim().startsWith("Test: ")) {
            commonMethods.throwTesboException("Please write valid keyword or step for this \"" + testName + "\" on suite file",log);
        }
    }

    /**
     *
     * @param testNameList
     */
    public void validationForDuplicateTestInSuiteFile(JSONArray testNameList)
    {
        for(int i=0;i<testNameList.size();i++){
            for(int j=i+1;j<testNameList.size();j++){
                if(testNameList.get(i).equals(testNameList.get(j))){
                    commonMethods.throwTesboException("'"+testNameList.get(i)+"' test is define multiple time on same suite file",log);
                }
            }
        }
    }

    /**
     *
      * @param suiteFileName
     * @return
     */
    public JSONObject getTestsFileNameUsingTestName(String suiteFileName) {

        JSONArray tampSuiteTestNameList=new JSONArray();
        StringBuilder suiteFile= readSuiteFile(suiteFileName);
        JSONArray suiteTestNameList=getTestNameFromSuiteFile(suiteFile);

        tampSuiteTestNameList.addAll(suiteTestNameList);
        JSONObject testNameWithTestsFileName=new JSONObject();
        TestsFileParser testsFileParser=new TestsFileParser();
        GetConfiguration getConfiguration=new GetConfiguration();
        JSONArray testsFileNameList= testsFileParser.getTestFiles(getConfiguration.getTestsDirectory());

        int i=1;
        for(Object suiteTestName:suiteTestNameList){
            JSONArray testList=new JSONArray();
            String testFileName = "";
            for(Object testsFileName:testsFileNameList){
                File name = new File(testsFileName.toString());
                StringBuilder testsFileDetails = testsFileParser.readTestsFile(name.getName());
                JSONArray testName= testsFileParser.getTestNameByTestsFile(testsFileDetails);
                for(Object test:testName) {
                    if(test.toString().equals(suiteTestName.toString().split(":")[1].trim())) {
                        verifySameTestNameIsExistOrNot(tampSuiteTestNameList,suiteTestName.toString());
                        testFileName=name.getName()+"_"+i++;
                        testList.add(test.toString().trim());
                        tampSuiteTestNameList.remove(suiteTestName);
                    }
                }

            }
            if(!testList.isEmpty()) {
                testNameWithTestsFileName.put(testFileName, testList);
            }
        }

        verifyTestNameIsNotExistOnAnyTestsFile(tampSuiteTestNameList);
        return testNameWithTestsFileName;
    }

    /**
     *
     * @param tampSuiteTestNameList
     * @param suiteTestName
     */
    public void verifySameTestNameIsExistOrNot(JSONArray tampSuiteTestNameList,String suiteTestName){
        boolean isExistInTampSuiteTestNameList=false;
        for(Object tampSuiteTest:tampSuiteTestNameList){
            if(suiteTestName.equals(tampSuiteTest)){
                isExistInTampSuiteTestNameList=true;
                break;
            }
        }
        if(!isExistInTampSuiteTestNameList){
            commonMethods.throwTesboException("'"+suiteTestName+"' test is found multiple time on tests file",log);
        }
    }

    /**
     *
     * @param tampSuiteTestNameList
     */
    public void verifyTestNameIsNotExistOnAnyTestsFile(JSONArray tampSuiteTestNameList){
        if(!tampSuiteTestNameList.isEmpty()){
            StringBuilder testList=new StringBuilder();
            for(Object test:tampSuiteTestNameList){
                if(testList.length()==0){ testList.append(test.toString());
                }else { testList.append(", "+test.toString()); }
            }
            commonMethods.throwTesboException("'"+testList+"' test is not found in any tests file",log);
        }
    }


    /**
     *
      * @param directory
     * @return
     */
    public JSONArray getSuiteFiles(String directory)  {

        JSONArray suiteFileList = new JSONArray();
        boolean flag=false;
        StringBuilder file=new StringBuilder();
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            suiteFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
            for(Object testsFilePath:suiteFileList) {
                String[] testsFile=testsFilePath.toString().split("\\.");
                if (testsFile.length == 2 && (!testsFile[1].equalsIgnoreCase("suite"))) {
                        flag=true;
                        if(file.length()==0)
                            file.append("'."+testsFile[1]+"'");
                        else
                            file.append(", '."+testsFile[1]+"'");
                }
            }
            if(flag){
                String errorMsg=file+" file found in suite directory";
                log.error(errorMsg);
                tesboLogger.errorLog(errorMsg);
                throw (new NoSuchFileException(""));
            }
        } catch (Exception e) {
            if(flag){
                log.error("Message : Please create only '.suite' file in tests directory.");
                tesboLogger.testFailed("Message : Please create only '.suite' file in tests directory.");
            }
            else {
                String errorMsg="'" + directory + "' no files found on your location.";
                log.error("Message : Please Enter valid directory path.");
                log.error(errorMsg);
                tesboLogger.testFailed("Message : Please Enter valid directory path.");
                tesboLogger.testFailed(errorMsg);
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
        return suiteFileList;
    }
}
