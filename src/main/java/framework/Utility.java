package framework;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class Utility {


    public static JSONObject loadJsonFile(String jsonFilePath){
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            Object obj = parser.parse(new FileReader(jsonFilePath));
            jsonObject = (JSONObject) obj;
        } catch (Exception e) {

        }
        return jsonObject;
    }

    public static JSONArray loadJsonArrayFile(String jsonFilePath){
        JSONParser parser = new JSONParser();
        JSONArray jsonObject = null;
        try {
            Object obj = parser.parse(new FileReader(jsonFilePath));
            jsonObject = (JSONArray) obj;
        } catch (Exception e) {

        }
        return jsonObject;
    }

}
