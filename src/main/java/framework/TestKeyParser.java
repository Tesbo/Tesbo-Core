package framework;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TestKeyParser {

    TestsFileParser testsFileParser=new TestsFileParser();

    public JSONObject getListOfTestWhoHasTestKey(String testsFileName)
    {

        StringBuffer testsFileDetails = testsFileParser.readTestsFile(testsFileName);
        String[] allLines = testsFileDetails.toString().split("[\\r\\n]+");

        JSONObject testNameWithTestKey = new JSONObject();
        JSONArray testKey=new JSONArray();
        int totalTest=0;
        int totalKey=0;

        for (int i = 0; i < allLines.length; i++) {

            if(allLines[i].startsWith("Test: ")){
                if(i!=0){
                    if(allLines[i-1].startsWith("TestID: ")){
                        String testKeyName=allLines[i-1].split(":")[1].trim();
                        testNameWithTestKey.put(allLines[i],testKeyName);
                        testKey.add(testKeyName);
                        totalTest++;
                        totalKey++;
                    }
                    else{totalTest++; }
                }
                else{totalTest++; }

            }

        }
        testNameWithTestKey.put("TestKey",testKey);
        testNameWithTestKey.put("TotalTest",totalTest);
        testNameWithTestKey.put("TotalKey",totalKey);

        return testNameWithTestKey;
    }

    public JSONObject addTestKeyNumberOnTest(String testsFileName, JSONObject testNameWithTestKey){

        GetConfiguration configuration = new GetConfiguration();
        JSONObject listOfTestNameWithTestKey=new JSONObject();
        int testNum=1;
        String testKeyName=testsFileName.split("\\.")[0].trim()+"_";

       /* if(Integer.parseInt(testNameWithTestKey.get("TotalTest").toString()) != Integer.parseInt(testNameWithTestKey.get("TotalKey").toString())) {
*/
            Path path = Paths.get(configuration.getTestsDirectory() + "/" + testsFileName);

            try {
                List<String> steps = Files.readAllLines(path, StandardCharsets.UTF_8);

                for (int i = 0; i < steps.size(); i++) {
                    while (true) {
                        if (isTestKeyExist((JSONArray) testNameWithTestKey.get("TestKey"), testKeyName + testNum)) {
                            testNum++;
                        } else {
                            break;
                        }
                    }

                    if (steps.get(i).startsWith("Test: ")) {
                        if (i == 0) {
                            listOfTestNameWithTestKey.put(steps.get(i).split("Test:")[1].trim(),testKeyName + testNum);
                            steps.add(i, "TestID: " + testKeyName + testNum);
                            testNum++;
                            i++;
                        } else {
                            if (!steps.get(i - 1).startsWith("TestID: ")) {
                                listOfTestNameWithTestKey.put(steps.get(i).split("Test:")[1].trim(),testKeyName + testNum);
                                steps.add(i, "TestID: " + testKeyName + testNum);
                                testNum++;
                                i++;
                            }
                            else{

                                listOfTestNameWithTestKey.put(steps.get(i).split("Test:")[1].trim(),steps.get(i - 1).split("TestID:")[1].trim());

                            }
                        }
                    }
                }


                Files.write(path, steps, StandardCharsets.UTF_8);

            } catch (IOException e) { e.printStackTrace(); }

       /* }*/
        return listOfTestNameWithTestKey;
    }

    public boolean isTestKeyExist(JSONArray TestKey, String newKey){
        for(Object key:TestKey){
            if(key.equals(newKey)){
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        TestKeyParser parser=new TestKeyParser();
        parser.addTestKeyNumberOnTest("writeDemo.tests", parser.getListOfTestWhoHasTestKey("writeDemo.tests"));
    }
}
