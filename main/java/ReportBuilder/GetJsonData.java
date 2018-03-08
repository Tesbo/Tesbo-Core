package ReportBuilder;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetJsonData {


    private static final String FORMAT = "%02d H %02d M %02d S";

    public static void main(String[] args) {

        GetJsonData data = new GetJsonData();
        ReportBuilder rb = new ReportBuilder();
        data.getLastBuildResultData(rb.getBuildHistoryPath());

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


            FileReader reader = new FileReader(filePath);
            jsonObject = (JSONObject) parser.parse(reader);

        } catch (Exception e) {
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


    public JSONArray getLastBuildResultData(String directory) {


        JSONArray last10BuildDataArray = new JSONArray();


        File directory1 = new File(directory);
        File[] files = directory1.listFiles((FileFilter) FileFileFilter.FILE);

        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);


        String startTime = "";
        int totalPassed = 0;
        int totalFailed = 0;
        int totalTimeTaken = 0;


        for (File a : files) {
            JSONObject parser = readJsonFile(new File(a.toString()).getAbsolutePath());
            JSONObject individualBuildData = new JSONObject();

            startTime = parser.get("startTime").toString().substring(0, 10);
            totalPassed = Integer.parseInt(parser.get("totalPassed").toString());
            totalFailed = Integer.parseInt(parser.get("totalFailed").toString());
            totalTimeTaken = Integer.parseInt(parser.get("totalTimeTaken").toString());

            individualBuildData.put("name", (a.getName().split(".json")[0]).replace("Result_", " ").toUpperCase() + " " + startTime);

            individualBuildData.put("totalPassed", totalPassed);
            individualBuildData.put("totalFailed", totalFailed);

            individualBuildData.put("buildRunDate", startTime.replace("|", "-"));
            individualBuildData.put("totalTimeTaken", TimeUnit.MILLISECONDS.toMinutes(totalTimeTaken));


            last10BuildDataArray.add(individualBuildData);
        }




        return last10BuildDataArray;

    }


    public int getCurrentBuildTotal(String dir) {

        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
        int total = Integer.parseInt(parser.get("totalPassed").toString()) + Integer.parseInt(parser.get("totalFailed").toString());

        return total;
    }

    public int getCurrentBuildPassed(String dir) {

        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
        int total = Integer.parseInt(parser.get("totalPassed").toString());

        return total;
    }

    public int getCurrentBuildFailed(String dir) {

        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
        int total = Integer.parseInt(parser.get("totalFailed").toString());

        return total;
    }

   public String getCurrentBuildTotalTime (String dir)
   {
       File currentBuildReport = getLastModifiedJsonFile(dir);
       JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
       int total = Integer.parseInt(parser.get("totalTimeTaken").toString());


return parseTime(total);
   }


    public String getCurrentBuildStartTime (String dir)
    {
        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());

        return parser.get("startTime").toString().replace("|","-");
    }


    public String getCurrentBuildEndTime (String dir)
    {
        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
        return parser.get("endTime").toString().replace("|","-");
    }


    public void getCuurrentBuildBrowserWiseData(String dir)
    {
        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());

    }






}
