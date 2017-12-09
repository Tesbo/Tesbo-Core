package framework;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import java.selebot.Exception.NoSuiteNameFoundException;

public class SuiteParser {


    /**
     * @param directory
     * @return give all the file inside a directory
     */
    public JSONArray getSuites(String directory) {

        JSONArray suiteFileList = new JSONArray();

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            suiteFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            e.printStackTrace();
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


    public JSONObject getTestNameByTag(String tag) {

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
     * @param suitesData
     * @param suiteName
     * @return
     */
    public Map<String, ArrayList<String>> getTestStepBySuiteandTestCaseName(Map<String, StringBuffer> suitesData, String suiteName, String testCase) {

        Map<String, StringBuffer> map = suitesData;

        Map<String, ArrayList<String>> stepByTestName = new HashMap<>();

        String allLines[] = map.get(suiteName).toString().split("[\\r\\n]+");
        String testName = null;


        for (int i = 0; i < allLines.length; i++) {
            if (allLines[i].toLowerCase().contains("test:") | allLines[i].toLowerCase().contains("test :")) {
                String testNameArray[] = allLines[i].split(":");
                testName = testNameArray[1].trim();

                Map<String, ArrayList<String>> suitesTest = stepByTestName;
                boolean flag = false;
                for (Map.Entry<String, ArrayList<String>> test : suitesTest.entrySet()) {
                    if (test.getKey().equalsIgnoreCase(testName)) {
                        flag = true;
                    }
                }


                if (!flag) {
                    ArrayList<String> stepList = new ArrayList<String>();
                    for (int j = i + 1; j < allLines.length; j++) {
                        if (allLines[j].toLowerCase().contains("end:") | allLines[j].toLowerCase().contains("end :")) {
                            break;
                        }
                        if (allLines[j].toLowerCase().contains("test:") | allLines[j].toLowerCase().contains("test :")) {
                      //      throw new NoEndStepFoundException("End Step not found in test");
                        }

                        if (allLines[j].toLowerCase().contains("step:") | allLines[j].toLowerCase().contains("step :"))
                            stepList.add(allLines[j]);
                        if (allLines[j].toLowerCase().contains("verify:") | allLines[j].toLowerCase().contains("verify :"))
                            stepList.add(allLines[j]);
                    }


                    if (testName.equalsIgnoreCase(testCase)) {
                        stepByTestName.put(testName, stepList);

                    }


                } else {
                  // throw new DuplicateTestNameException("Suite file has found duplicate test name");
                }
            }

        }


        return stepByTestName;
    }


}
