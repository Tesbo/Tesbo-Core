package reportAPI;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import framework.GetConfiguration;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ReportAPIConfig {


    public static String URL="http://api-stagging.tesbo.io:4848/graphql";
    public static String buildID;


    public void getBuildKey() {

        GetConfiguration config = new GetConfiguration();
        JSONObject userDetails = config.getCloudIntegration();
        OkHttpClient client = new OkHttpClient();

        JSONObject content = new JSONObject();
        content.put("query", "mutation{createBuild(buildInput:{apiKey:\"" + userDetails.get("apiKey") + "\",projectId:\"" + userDetails.get("projectKey") + "\",name:\"" + userDetails.get("buildName") + "\",startTime:\"" + System.currentTimeMillis() + "\"}){_id name} }");

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, content.toJSONString());
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();
            JSONParser parser = new JSONParser();
            JSONObject object = null;
            try {

                object = (JSONObject) parser.parse(response.body().string());
                buildID = ((JSONObject) ((JSONObject) (object.get("data"))).get("createBuild")).get("_id").toString();

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

        JSONObject content = new JSONObject();
        content.put("query", "mutation{updateBuild(buildInput:{apiKey:\"" + userDetails.get("apiKey") + "\", id:\"" + buildID + "\",endTime:\"" + System.currentTimeMillis() + "\"}){_id name} }");

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, content.toJSONString());
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();
    }


    public void organiazeDataForCloudReport(JSONObject testObject) {

        JSONObject content = new JSONObject();
        GetConfiguration config = new GetConfiguration();
        JSONObject userDetails = config.getCloudIntegration();

        try {
            String screenShotUrl = awsScreenshotUpload(testObject.get("screenShot").toString());
            testObject.remove("screenShot");
            testObject.put("screenShot", screenShotUrl);
        } catch (Exception e) {
        }
        String fullStackTrace=null;
        if(testObject.get("fullStackTrace")!= null){
            String stackTrace[]=testObject.get("fullStackTrace").toString().split("\\r?\\n");
            for (String stack:stackTrace){
                if(fullStackTrace==null){fullStackTrace=fullStackTrace+stack.trim();}
                else{fullStackTrace=fullStackTrace+","+stack.trim();}
            }
        }

        String data = "mutation{createTest(testInput:{apiKey:\"" + userDetails.get("apiKey") + "\",build:\"" + buildID + "\", browserVersion:\"" + testObject.get("browserVersion") + "\", browserName:\"" + testObject.get("browserName") + "\", totalTime:" + testObject.get("totalTime") + ", startTime:\"" + testObject.get("totalTime") + "\", testStep:\"\"\"" + testObject.get("testStep") + "\"\"\", osName:\"" + testObject.get("osName") + "\", testName:\"" + testObject.get("testName") + "\", suiteName:\"" + testObject.get("suiteName") + "\", status:\"" + testObject.get("status") + "\",priority:\"" + testObject.get("Priority") + "\",severity:\"" + testObject.get("Severity") + "\",screenShot:\"" + testObject.get("screenShot") + "\",fullStackTrace:\"" + fullStackTrace + "\"}){_id} }";
        content.put("query", data);

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, content.toJSONString());
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

    }

    public String awsScreenshotUpload(String screenshotUrl) {

        String screenshot[] = screenshotUrl.split("/");
        String screenshotName = screenshot[screenshot.length - 1];
        File file = new File(screenshotUrl);
        String path = file.getAbsolutePath();
        AWSCredentials credentials = new BasicAWSCredentials(
                "AKIAJOHN37HIKYIC77IA",
                "cpA90V9UMSvpwdRH8EfZnpUrLK/3QQ+hLKaHNZ9n");
        String bucketName = "place-pass";
        AmazonS3 s3client = new AmazonS3Client(credentials);
        s3client.putObject(new PutObjectRequest(bucketName, screenshotName,
                new File(path))
                .withCannedAcl(CannedAccessControlList.PublicRead));
        String awsScreenshotUrl = "https://s3.amazonaws.com/" + bucketName + "/" + screenshotName;
        return awsScreenshotUrl;

    }


}
