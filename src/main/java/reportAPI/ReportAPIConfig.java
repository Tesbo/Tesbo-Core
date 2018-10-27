package reportAPI;

import framework.GetConfiguration;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class ReportAPIConfig {


    public static String buildKey;
    public static String buildID;

    public void getBuildKey() {


        GetConfiguration config = new GetConfiguration();
        JSONObject userDetails = config.getCloudIntegration();


        OkHttpClient client = new OkHttpClient();


        String content = "{\n" +
                "\t\"email\": \""+ userDetails.get("userName") + "\",\n" +
                "\t\"password\": \"" + userDetails.get("password")+"\",\n" +
                "\t\"projectId\": \"" + userDetails.get("projectKey")+ "\",\n" +
                "\t\"name\": \"" + userDetails.get("projectKey")+ "\",\n" +
                "\t\"startTime\": \"" + System.currentTimeMillis()+"\"\n" +
                "}";




        MediaType mediaType = MediaType.parse("application/json");
       RequestBody body = RequestBody.create(mediaType,content);
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

                object = (JSONObject) parser.parse(response.body().string());
                buildKey = ((JSONObject) (object.get("data"))).get("buildKey").toString();
                buildID = ((JSONObject) (object.get("data"))).get("_id").toString();

            } catch (ParseException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
}



    public void updateEndTime() {


        GetConfiguration config = new GetConfiguration();
        JSONObject userDetails = config.getCloudIntegration();


        OkHttpClient client = new OkHttpClient();


        String content = "{\n" +
                "\t\"email\": \""+ userDetails.get("userName") + "\",\n" +
                "\t\"password\": \"" + userDetails.get("password")+"\",\n" +
                "\t\"id\": \"" + buildID+ "\",\n" +
                "\t\"endTime\": \"" + System.currentTimeMillis()+"\"\n" +
                "}";



        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,content);
        Request request = new Request.Builder()
                .url("http://206.189.208.236:3000/api/builds/update")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();

            JSONParser parser = new JSONParser();
            JSONObject object = null;
            try {

                object = (JSONObject) parser.parse(response.body().string());
                buildKey = ((JSONObject) (object.get("data"))).get("buildKey").toString();


            } catch (ParseException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

       }
    public void organiazeDataForCloudReport(JSONObject testObject)
    {


        OkHttpClient client = new OkHttpClient();

        String content = testObject.toJSONString();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, content);
        Request request = new Request.Builder()
                .url("http://206.189.208.236:3000/api/tests/create")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "047b73bc-0968-4463-92ce-25c41d0cced0")
                .build();

        try {
            Response response = client.newCall(request).execute();
   } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Test Updated on Cloud Report");



    }
    public static void main(String[] args) {
        ReportAPIConfig config = new ReportAPIConfig();
        config.getBuildKey();
    }


}
