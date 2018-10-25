package reportAPI;

import framework.GetConfiguration;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class ReportAPIConfig
{


    public static String buildKey;
    public void getBuildKey()
{


    GetConfiguration config = new GetConfiguration();
    JSONObject userDetails = config.getCloudIntegration();



    OkHttpClient client = new OkHttpClient();

    MediaType mediaType = MediaType.parse("application/json");
    RequestBody body = RequestBody.create(mediaType, "{\n\t\"email\": \""+userDetails.get("userName")+"\",\n\t\"password\": "+userDetails.get("password\")+\"\",\n\t\"projectId\": \""+userDetails.get("projectKey")+"\",\n\t\"startTime\": "+System.currentTimeMillis()+"\n}"));
    Request request = new Request.Builder()
            .url("http://206.189.208.236:3000/api/builds/create")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("cache-control", "no-cache")
            .build();

    try {
        Response response = client.newCall(request).execute();


        JSONParser parser = new JSONParser();
        JSONObject object = null;
        try {
            object= (JSONObject) parser.parse(response.body().string());
        } catch (ParseException e) {
            e.printStackTrace();
        }

      buildKey = ((JSONObject)(object.get("data"))).get("buildKey").toString();

    } catch (IOException e) {
        e.printStackTrace();
    }


}

    public static void main(String[] args) {
        ReportAPIConfig config = new ReportAPIConfig();
        config.getBuildKey();
    }



}
