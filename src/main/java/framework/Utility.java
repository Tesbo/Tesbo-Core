package framework;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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

}
