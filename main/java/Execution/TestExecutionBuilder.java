package Execution;

import framework.GetConfiguration;
import framework.SuiteParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TestExecutionBuilder {


    public static void main(String[] args) {
        TestExecutionBuilder builder = new TestExecutionBuilder();
        builder.parallelBuilder();
    }

    public void buildExecutionQueueByTag() {
        SuiteParser suiteParser = new SuiteParser();
        GetConfiguration config = new GetConfiguration();
        JSONArray completeTestObjectArray = new JSONArray();

        for (String tag : config.getTags()) {
            JSONObject testNameWithSuites = suiteParser.getTestNameByTag(tag);
            for (Object suiteName : testNameWithSuites.keySet()) {
                for (Object testName : ((JSONArray) testNameWithSuites.get(suiteName))) {
                    for (String browser : config.getBrowsers()) {
                        JSONObject completestTestObject = new JSONObject();
                        completestTestObject.put("testName", testName);
                        completestTestObject.put("tag", tag);
                        completestTestObject.put("suiteName", suiteName);
                        completestTestObject.put("browser", browser);
                        completeTestObjectArray.add(completestTestObject);
                    }
                }

            }

        }

    }


    public void parallelBuilder(/*JSONArray testExecutionQueue*/)
    {


        GetConfiguration config = new GetConfiguration();

        JSONObject parallelConfig = config.getParallel();


        if(parallelConfig.get("status").toString().equals("true"))
        {




        }else {

        }






    }


}
