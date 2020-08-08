package reportapi;

import execution.SetCommandLineArgument;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import framework.GetConfiguration;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import exception.TesboException;

public class ReportAPIConfig {

    String applicationJsonText="application/json";
    String contentTypeText="Content-Type";
    String screenShotText="screenShot";
    String url="http://report.tesbo.io";
    public static String buildID;
    private static final Logger log = LogManager.getLogger(ReportAPIConfig.class);
    StringWriter sw = new StringWriter();


    public void createBuild() {
        GetConfiguration config = new GetConfiguration();
        String buildName=null;
        JSONObject userDetails = config.getCloudIntegration();
        if(SetCommandLineArgument.buildName != null ){
            buildName=SetCommandLineArgument.buildName;
        }
        else{
            buildName=userDetails.get("buildName").toString();
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse(applicationJsonText);
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"buildName\": \""+buildName+"\",\n\t\"projectID\" : \""+userDetails.get("projectKey")+"\",\n\t\"userID\" : \""+userDetails.get("apiKey")+"\"\n}");
        Request request = new Request.Builder()
                .url(url+"/createBuild/Tesbo")
                .method("POST", body)
                .addHeader(contentTypeText, applicationJsonText)
                .build();
        try {
            Response response = client.newCall(request).execute();
            JSONParser parser = new JSONParser();
            JSONObject object = null;
            object = (JSONObject) parser.parse(response.body().string());
            if(object.get("errors")!= null){
                throw new TesboException(object.get("message").toString());
            }
            buildID = (object.get("buildID")).toString();
            response.close();

        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }

    }

    public void updateBuild() {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse(applicationJsonText);
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"buildKey\": \""+buildID+"\"\n}");
        Request request = new Request.Builder()
                .url(url+"/updateBuild")
                .method("POST", body)
                .addHeader(contentTypeText, applicationJsonText)
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();

        } catch (IOException e) {
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
    }




    public void createTests(JSONObject testObject) {
        GetConfiguration config = new GetConfiguration();
        JSONObject userDetails = config.getCloudIntegration();

        try {
            String screenShotUrl = cloudinaryScreenshotUpload(testObject.get(screenShotText).toString());
            testObject.remove(screenShotText);
            testObject.put(screenShotText, screenShotUrl);
        } catch (Exception e) { log.error("");}


        StringBuilder fullStackTrace=new StringBuilder();
        if(testObject.get("fullStackTrace")!= null){
            String[] stackTrace=testObject.get("fullStackTrace").toString().split("\\r?\\n");
            for (String stack:stackTrace){
                if(fullStackTrace.length()==0){fullStackTrace.append(stack.trim());}
                else{fullStackTrace.append(","+stack.trim());}
            }
        }
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse(applicationJsonText);
            RequestBody body = RequestBody.create(mediaType, "{\n\t\"buildKey\": \""+buildID+"\",\n\t\"tesboTestKey\" : \""+null+"\",\n\t\"testFile\":\""+(testObject.get("testsFileName").toString()).split(".tests")[0]+"\",\n\t\"tags\" : [\""+userDetails.get("tagName")+"\"],\n\t\"userKey\":\""+userDetails.get("apiKey")+"\",\n\t\"browser\":\""+testObject.get("browserName")+"\",\n\t\"browserVersion\":\""+testObject.get("browserVersion")+"\",\n\t\"startTime\":\""+testObject.get("startTime")+"\",\n\t\"endTime\":\""+testObject.get("endTime")+"\",\n\t\"osName\":\""+testObject.get("osName")+"\",\n\t\"testName\":\""+testObject.get("testName")+"\",\n\t\"status\":\""+testObject.get("status")+"\",\n\t\"screenShot\":\""+testObject.get(screenShotText)+"\",\n\t\"fullStackTrace\" :\""+fullStackTrace+"\",\n\t\"steps\" : "+testObject.get("testStep")+"\n\t\n}");
            Request request = new Request.Builder()
                    .url(url+"/createTests")
                    .method("POST", body)
                    .addHeader(contentTypeText, applicationJsonText)
                    .build();
            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
    }

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
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
        return screenshotNam;
    }


}
