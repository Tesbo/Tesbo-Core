package reportapi;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Reporter {
    public static JSONArray printStepReportObject = new JSONArray();

    public void printStep(String step) {
        JSONObject stepReportObject = new JSONObject();

        stepReportObject.put("steps", step);
        printStepReportObject.add(stepReportObject);
    }
}
