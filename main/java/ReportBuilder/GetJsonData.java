package ReportBuilder;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetJsonData {


    private static final String FORMAT = "%02d H %02d M %02d S";

    public static void main(String[] args) {

        GetJsonData data = new GetJsonData();

        ReportBuilder rb = new ReportBuilder();


        data.getAvaerageTimeoftheBuild(rb.getBuildHistoryPath());


    }

    public static String parseTime(long milliseconds) {
        return String.format(FORMAT,
                TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(milliseconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    public File getLastModifiedJsonFile(String dirPath) {

        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;

    }

    public int getTotalBuildCount(String directory) {

        JSONArray suiteFileList = new JSONArray();

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            suiteFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
        }

        return suiteFileList.size();

    }

    public JSONObject readJsonFile(String filePath) {
        JSONObject jsonObject = null;

        JSONParser parser = new JSONParser();
        try {

            System.out.println("file path" + filePath);

            FileReader reader = new FileReader(filePath);
            jsonObject = (JSONObject) parser.parse(reader);

        } catch (Exception e) {
            System.out.println("here");
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String getAvaerageTimeoftheBuild(String directory) {

        JSONArray suiteFileList = new JSONArray();

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            suiteFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
        }

        int totalTime = 0;

        for (Object a : suiteFileList) {
            JSONObject parser = readJsonFile(new File(a.toString()).getAbsolutePath());
            totalTime = totalTime + Integer.parseInt(parser.get("totalTimeTaken").toString());
        }

        int totalAvgTimeInMillis = totalTime / suiteFileList.size();

        return parseTime(totalAvgTimeInMillis);
    }

    public int getTotalTestOfTheBuild(String directory) {

        JSONArray suiteFileList = new JSONArray();

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            suiteFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
        }

        int totalTests = 0;

        for (Object a : suiteFileList) {
            JSONObject parser = readJsonFile(new File(a.toString()).getAbsolutePath());
            totalTests = totalTests + Integer.parseInt(parser.get("totalPassed").toString()) + Integer.parseInt(parser.get("totalFailed").toString());
        }

        return totalTests;
    }


}
