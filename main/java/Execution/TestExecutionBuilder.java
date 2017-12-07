package Execution;

import framework.TestExecutor;
import framework.GetConfiguration;
import framework.SuiteParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestExecutionBuilder {


    public static void main(String[] args) {
        TestExecutionBuilder builder = new TestExecutionBuilder();
        builder.parallelBuilder(builder.buildExecutionQueueByTag());
    }

    public JSONArray buildExecutionQueueByTag() {
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

        for (Object a : completeTestObjectArray) {
            System.out.println(a);
        }

        return completeTestObjectArray;
    }


    public void parallelBuilder(JSONArray testExecutionQueue) {

        GetConfiguration config = new GetConfiguration();
        JSONObject parallelConfig = config.getParallel();
        int threadCount = 0;

        if (parallelConfig.get("status").toString().equals("true")) {
            threadCount = Integer.parseInt(parallelConfig.get("count").toString());
        } else {
            threadCount = 1;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        System.out.println(testExecutionQueue.size());
        for (int i = 0; i < testExecutionQueue.size(); i++) {
            Runnable worker = new TestExecutor((JSONObject) testExecutionQueue.get(i));
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }


    }


}
