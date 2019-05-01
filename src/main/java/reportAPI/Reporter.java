package reportAPI;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Reporter {
    public static JSONArray printStepReportObject = new JSONArray();

    public void printStep(String Step) {
        JSONObject stepReportObject = new JSONObject();

        stepReportObject.put("steps", Step);
        printStepReportObject.add(stepReportObject);
    }
}
