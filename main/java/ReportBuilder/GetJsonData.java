package ReportBuilder;

import com.diogonunes.jcdp.color.api.Ansi;
import logger.Logger;
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

    Logger logger = new Logger();

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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }

        int totalTests = 0;

        for (Object a : suiteFileList) {
            JSONObject parser = readJsonFile(new File(a.toString()).getAbsolutePath());
            try {
                totalTests = totalTests + Integer.parseInt(parser.get("totalPassed").toString()) + Integer.parseInt(parser.get("totalFailed").toString());
            } catch (Exception e) {

            }
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

            try {

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
            } catch (Exception e) {
                logger.customeLog("Seems Like Report History File Correpted", Ansi.FColor.RED);
            }
        }

        return last10BuildDataArray;

    }


    public int getCurrentBuildTotal(String dir) {

        int total = 0;
        try {
            File currentBuildReport = getLastModifiedJsonFile(dir);
            JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());

            total = Integer.parseInt(parser.get("totalPassed").toString()) + Integer.parseInt(parser.get("totalFailed").toString());

        } catch (Exception e) {

        }

        return total;
    }

    public int getCurrentBuildPassed(String dir) {


        int total = 0;
        try {
            File currentBuildReport = getLastModifiedJsonFile(dir);
            JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());

            total = Integer.parseInt(parser.get("totalPassed").toString());

        } catch (Exception e) {

        }


        return total;
    }

    public int getCurrentBuildFailed(String dir) {


        int total = 0;
        try {

            File currentBuildReport = getLastModifiedJsonFile(dir);
            JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());

            total = Integer.parseInt(parser.get("totalFailed").toString());

        } catch (Exception e) {

        }


        return total;
    }

    public String getCurrentBuildTotalTime(String dir) {
        int total = 0;
        try {
            File currentBuildReport = getLastModifiedJsonFile(dir);
            JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());

            total = Integer.parseInt(parser.get("totalTimeTaken").toString());

        } catch (Exception e) {

        }


        return parseTime(total);
    }


    public String getCurrentBuildStartTime(String dir) {
        JSONObject parser = null;
        File currentBuildReport = getLastModifiedJsonFile(dir);
        parser = readJsonFile(currentBuildReport.getAbsolutePath());

        return parser.get("startTime").toString();
    }


    public String getCurrentBuildEndTime(String dir) {
        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
        return parser.get("endTime").toString();
    }


    public JSONObject getCurrentBuildBrowserWiseData(String dir, String browserName) {

        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());


        JSONArray suiteArray = (JSONArray) parser.get("browser");


        int totalPass = 0;
        int totalFail = 0;

        JSONObject passFailData = new JSONObject();


        for (int i = 0; i < suiteArray.size(); i++) {


            JSONObject browser = (JSONObject) suiteArray.get(i);

            try {
                JSONObject chromeSuite = (JSONObject) browser.get(browserName);
                JSONArray suiteList = (JSONArray) chromeSuite.get("suits");

                for (Object suiteDetails : suiteList) {
                    JSONObject suite = (JSONObject) suiteDetails;

                    totalPass = (int) (totalPass + Double.parseDouble(suite.get("totalPassed").toString()));
                    totalFail = (int) (totalFail + Double.parseDouble(suite.get("totalFailed").toString()));

                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        passFailData.put("totalPassed", totalPass);
        passFailData.put("totalFailed", totalFail);

        return passFailData;
    }


    public JSONArray getModuleWiseData(String dir, String browserName) {

        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());

        JSONArray suiteArray = (JSONArray) parser.get("browser");


        JSONArray suiteList = null;

        for (int i = 0; i < suiteArray.size(); i++) {


            JSONObject browser = (JSONObject) suiteArray.get(i);

            try {
                JSONObject chromeSuite = (JSONObject) browser.get(browserName);
                suiteList = (JSONArray) chromeSuite.get("suits");


            } catch (Exception e) {
                e.printStackTrace();
            }


        }


        return suiteList;
    }


    public JSONArray getBrowserExecutionReport(String dir) {

        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());


        JSONArray suiteArray = (JSONArray) parser.get("browser");


        return suiteArray;

    }


    public JSONArray browserResult(int pass, int fail) {
        JSONArray browserResult = new JSONArray();

        browserResult.add(pass);
        browserResult.add(fail);

        return browserResult;
    }


}
