package reportAPI;

import Execution.SetCommandLineArgument;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import framework.GetConfiguration;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import Exception.TesboException;

public class ReportAPIConfig {


    public static String URL="http://v2.tesbo.io:7000";
    public static String buildID;

    public static void main(String[] args) {
        ReportAPIConfig reportAPIConfig=new ReportAPIConfig();
    }


    public void createBuild() {
        GetConfiguration config = new GetConfiguration();
        SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();
        String buildName=null;
        JSONObject userDetails = config.getCloudIntegration();
        if(setCommandLineArgument.buildName != null ){
            buildName=setCommandLineArgument.buildName;
        }
        else{
            buildName=userDetails.get("buildName").toString();
        }


        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"buildName\": \""+buildName+"\",\n\t\"projectID\" : \""+userDetails.get("projectKey")+"\",\n\t\"userID\" : \""+userDetails.get("apiKey")+"\"\n}");
        Request request = new Request.Builder()
                .url(URL+"/createBuild")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println("response: ======> "+response);

            JSONParser parser = new JSONParser();
            JSONObject object = null;
            try {
                object = (JSONObject) parser.parse(response.body().string());
                System.out.println("======> : "+object);
                if(object.get("errors")!= null){
                    throw new TesboException(object.get("message").toString());
                }
                buildID = (object.get("buildID")).toString();
                System.out.println("buildID ====> "+buildID);
                response.close();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*public void getBuildKey() {
        GetConfiguration config = new GetConfiguration();
        SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();
        String buildName=null;
        JSONObject userDetails = config.getCloudIntegration();
        if(setCommandLineArgument.buildName != null ){
            buildName=setCommandLineArgument.buildName;
        }
        else{
            buildName=userDetails.get("buildName").toString();
        }
        OkHttpClient client = new OkHttpClient();

        JSONObject content = new JSONObject();
        content.put("query", "mutation{createBuild(buildInput:{apiKey:\"" + userDetails.get("apiKey") + "\",projectId:\"" + userDetails.get("projectKey") + "\",name:\"" + buildName + "\",startTime:\"" + System.currentTimeMillis() + "\"}){_id name} }");

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
                if(object.get("errors")!= null){
                    throw new TesboException(((JSONObject)((JSONArray) (object.get("errors"))).get(0)).get("message").toString());
                }
                buildID = ((JSONObject) ((JSONObject) (object.get("data"))).get("createBuild")).get("_id").toString();
                response.close();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/


    /*public void updateEndTime() {
        GetConfiguration config = new GetConfiguration();
        JSONObject userDetails = config.getCloudIntegration();

        JSONObject content = new JSONObject();
        content.put("query", "mutation{updateBuild(buildInput:{apiKey:\"" + userDetails.get("apiKey") + "\", id:\"" + buildID + "\",endTime:\"" + System.currentTimeMillis() + "\"}){_id name} }");
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, content.toJSONString());
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public void updateBuild() {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"buildKey\": \""+buildID+"\"\n}");
        Request request = new Request.Builder()
                .url(URL+"/updateBuild")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println("response: ======> "+response);
            response.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*public void organiazeDataForCloudReport(JSONObject testObject) {
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
                if(fullStackTrace==null){fullStackTrace=stack.trim();}
                else{fullStackTrace=fullStackTrace+","+stack.trim();}
            }
        }

        String data = "mutation{createTest(testInput:{apiKey:\"" + userDetails.get("apiKey") + "\",build:\"" + buildID + "\", browserVersion:\"" + testObject.get("browserVersion") + "\", browserName:\"" + testObject.get("browserName") + "\", totalTime:" + testObject.get("totalTime") + ", startTime:\"" + testObject.get("startTime") + "\", testStep:\"\"\"" + testObject.get("testStep") + "\"\"\", osName:\"" + testObject.get("osName") + "\", testName:\"" + testObject.get("testName") + "\", suiteName:\"" + testObject.get("testsFileName") + "\", status:\"" + testObject.get("status") + "\",priority:\"" + testObject.get("Priority") + "\",severity:\"" + testObject.get("Severity") + "\",screenShot:\"" + testObject.get("screenShot") + "\",fullStackTrace:\"\"\"" + fullStackTrace + "\"\"\"}){_id} }";
        content.put("query", data);
        try {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, content.toJSONString());
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void createTests(JSONObject testObject) {
        JSONObject content = new JSONObject();
        GetConfiguration config = new GetConfiguration();
        JSONObject userDetails = config.getCloudIntegration();

        try {
            String screenShotUrl = cloudinaryScreenshotUpload(testObject.get("screenShot").toString());
            testObject.remove("screenShot");
            testObject.put("screenShot", screenShotUrl);
        } catch (Exception e) {
        }


        String fullStackTrace=null;
        if(testObject.get("fullStackTrace")!= null){
            String stackTrace[]=testObject.get("fullStackTrace").toString().split("\\r?\\n");
            for (String stack:stackTrace){
                if(fullStackTrace==null){fullStackTrace=stack.trim();}
                else{fullStackTrace=fullStackTrace+","+stack.trim();}
            }
        }

        String data="{\n\t\"buildKey\": \""+buildID+"\",\n\t\"tesboTestKey\" : \""+null+"\",\n\t\"testFile\":\""+testObject.get("testsFileName")+"\",\n\t\"tags\" : [\"ABC\"],\n\t\"userKey\":\""+userDetails.get("apiKey")+"\",\n\t\"browser\":\""+testObject.get("browserName")+"\",\n\t\"browserVersion\":\""+testObject.get("browserVersion")+"\",\n\t\"startTime\":\""+testObject.get("startTime")+"\",\n\t\"endTime\":\"1582202003473\",\n\t\"osName\":\""+testObject.get("osName")+"\",\n\t\"testName\":\""+testObject.get("testName")+"\",\n\t\"status\":\""+testObject.get("status")+"\",\n\t\"screenShot\":\""+testObject.get("screenShot")+"\",\n\t\"fullStackTrace\" :\""+fullStackTrace+"\",\n\t\"steps\" : "+testObject.get("testStep")+"\n\t\n}";
        System.out.println("======> Data: "+data);
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\n\t\"buildKey\": \""+buildID+"\",\n\t\"tesboTestKey\" : \""+null+"\",\n\t\"testFile\":\""+testObject.get("testsFileName")+"\",\n\t\"tags\" : [\"ABC\"],\n\t\"userKey\":\""+userDetails.get("apiKey")+"\",\n\t\"browser\":\""+testObject.get("browserName")+"\",\n\t\"browserVersion\":\""+testObject.get("browserVersion")+"\",\n\t\"startTime\":\""+testObject.get("startTime")+"\",\n\t\"endTime\":\"1582202003473\",\n\t\"osName\":\""+testObject.get("osName")+"\",\n\t\"testName\":\""+testObject.get("testName")+"\",\n\t\"status\":\""+testObject.get("status")+"\",\n\t\"screenShot\":\""+testObject.get("screenShot")+"\",\n\t\"fullStackTrace\" :\""+fullStackTrace+"\",\n\t\"steps\" : "+testObject.get("testStep")+"\n\t\n}");
            Request request = new Request.Builder()
                    .url(URL+"/createTests")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            System.out.println("response: ======> "+response);
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /*public String awsScreenshotUpload(String screenshotUrl) {
        try {
            String screenshot[] = screenshotUrl.split(File.pathSeparator);
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
        }catch (Exception e){
            e.printStackTrace();
        }

       return null;

    }*/

    public String cloudinaryScreenshotUpload(String screenshotUrl){
        File file = new File(screenshotUrl);
        String path = file.getAbsolutePath();
        String screenshotNam = null;
        Map config = new HashMap();
        config.put("cloud_name", "dqyuanngf");
        config.put("api_key", "836742259538521");
        config.put("api_secret", "TiENTOSlmxRwp-aXfNGi5DVm-zQ");
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dqyuanngf",
                "api_key", "836742259538521",
                "api_secret", "TiENTOSlmxRwp-aXfNGi5DVm-zQ"));
        try {
            Map uploadResult = cloudinary.uploader().upload(new File(path), ObjectUtils.emptyMap());
            screenshotNam=uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return screenshotNam;
    }


}
