package framework;

import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;

import java.io.*;

import Exception.*;
import org.json.simple.JSONObject;

public class SuiteParser {
    TesboLogger tesboLogger = new TesboLogger();
    private static final Logger log = LogManager.getLogger(SuiteParser.class);

    /**
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param fileName : File name with extension e.g. smoke.suite
     * @return whole file content as String buffer
     */
    public StringBuffer readSuiteFile(String fileName) {

        GetConfiguration configuration = new GetConfiguration();
        BufferedReader br = null;
        FileReader fr = null;
        StringBuffer tests = new StringBuffer();

        try {
            fr = new FileReader(configuration.getSuitesDirectory() + "/" + fileName);
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
     * @auther: Ankit Mistry
     * @lastModifiedBy:
     * @param testsFile
     * @return
     */
    public JSONArray getTestNameFromSuiteFile(StringBuffer testsFile) {

        String allLines[] = testsFile.toString().split("[\\r\\n]+");
        JSONArray testName = new JSONArray();

        for (int i = 0; i < allLines.length; i++) {
            String comment=String.valueOf(allLines[i].charAt(0))+String.valueOf(allLines[i].charAt(1));
            if (allLines[i].toLowerCase().contains("test:") | allLines[i].toLowerCase().contains("test :")) {
                if (allLines[i].contains("Test :") | allLines[i].contains("test:") | allLines[i].contains("test :")) {
                    log.error("Please write valid keyword for this \"" + allLines[i] + "\"");
                    throw new TesboException("Please write valid keyword for \"" + allLines[i] + "\" on suite file");
                }
                testName.add(allLines[i].trim());
            }
            else{
                if(!comment.equals("//")) {
                    log.error("Define only test name in '.suite' file");
                    throw new TesboException("Define only test name in '.suite' file");
                }
            }

        }
        // When No Test Available
        if (testName.size() == 0) {
            return null;
        }
        return testName;
    }

    public JSONObject getTestsFileNameUsingTestName(JSONArray suiteTestNameList) {

        JSONArray tampSuiteTestNameList=new JSONArray();
        tampSuiteTestNameList.addAll(suiteTestNameList);
        JSONObject testNameWithTestsFileName=new JSONObject();
        TestsFileParser testsFileParser=new TestsFileParser();
        GetConfiguration getConfiguration=new GetConfiguration();
        JSONArray testsFileNameList= testsFileParser.getTestFiles(getConfiguration.getTestsDirectory());

        for(Object testsFileName:testsFileNameList){
            File name = new File(testsFileName.toString());
            StringBuffer testsFileDetails = testsFileParser.readTestsFile(name.getName());
            JSONArray testName= testsFileParser.getTestNameByTestsFile(testsFileDetails);
            JSONArray TestList=new JSONArray();
            for(Object suiteTestName:suiteTestNameList){
                for(Object test:testName) {
                    if(test.toString().equals(suiteTestName.toString().split(":")[1].trim())) {
                        TestList.add(suiteTestName);
                        tampSuiteTestNameList.remove(suiteTestName);
                    }
                }
            }
            if(!TestList.isEmpty()) {
                testNameWithTestsFileName.put(name.getName(), TestList);
            }
        }
        if(tampSuiteTestNameList.size()!=0){
            String testList="";
            for(Object test:tampSuiteTestNameList){
                if(testList.equals("")){ testList=test.toString();
                }else { testList=testList+", "+test.toString(); }
            }
            log.error("'"+testList+"' test is not found in any tests file");
            throw new TesboException("'"+testList+"' test is not found in any tests file");
        }

        return testNameWithTestsFileName;
    }


    public static void main(String[] args) {
        SuiteParser suiteParser=new SuiteParser();
        StringBuffer suiteFile= suiteParser.readSuiteFile("smoke.suite");
        System.out.println("====================================================================================");

        System.out.println(suiteParser.getTestNameFromSuiteFile(suiteFile));
        System.out.println(suiteParser.getTestsFileNameUsingTestName(suiteParser.getTestNameFromSuiteFile(suiteFile)));
        System.out.println("====================================================================================");

        System.out.println("====================================================================================");

    }
}
