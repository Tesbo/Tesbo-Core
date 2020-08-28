package reportbuilder;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetJsonData {


    private static final String FORMAT = "%02d H %02d M %02d S";
    String totalTimeTakenText="totalTimeTaken";
    String totalPassedText="totalPassed";
    String totalFailedText="totalFailed";
    String browserText="browser";
    private static final Logger log = LogManager.getLogger(GetJsonData.class);
    StringWriter sw = new StringWriter();

    /**
     *
     * @param milliseconds
     * @return
     */
    public String parseTime(long milliseconds) {
        Date date = new Date(milliseconds);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

        return formatter.format(date)/*String.format(FORMAT,
                TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(milliseconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(milliseconds)))*/;
    }

    /**
     *
     * @param milliseconds
     * @return
     */
    public String parseTime(int milliseconds) {


        return String.format(FORMAT,
                TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(milliseconds)), TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    /**
     *
     * @param dirPath
     * @return
     */
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

    /**
     *
     * @param directory
     * @return
     */
    public int getTotalBuildCount(String directory) {

        JSONArray testsFileList = new JSONArray();
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            testsFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }

        return testsFileList.size();

    }

    /**
     *
     * @param filePath
     * @return
     */
    public JSONObject readJsonFile(String filePath) {
        JSONObject jsonObject = null;

        JSONParser parser = new JSONParser();
        try {
            FileReader reader = new FileReader(filePath);
            jsonObject = (JSONObject) parser.parse(reader);

        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }
        return jsonObject;
    }

    /**
     *
     * @param directory
     * @return
     */
    public String getAvaerageTimeoftheBuild(String directory) {

        JSONArray testsFileList = new JSONArray();

        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            testsFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }

        int totalTime = 0;

        for (Object a : testsFileList) {
            JSONObject parser = readJsonFile(new File(a.toString()).getAbsolutePath());
            totalTime = totalTime + Integer.parseInt(parser.get(totalTimeTakenText).toString());
        }

        int totalAvgTimeInMillis = totalTime / testsFileList.size();

        return parseTime(totalAvgTimeInMillis);
    }

    /**
     *
     * @param directory
     * @return
     */
    public int getTotalTestOfTheBuild(String directory) {

        JSONArray testsFileList = new JSONArray();
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {

            testsFileList.addAll(paths
                    .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(sw));
            log.error(sw.toString());
        }

        int totalTests = 0;

        for (Object a : testsFileList) {
            JSONObject parser = readJsonFile(new File(a.toString()).getAbsolutePath());
            try {
                totalTests = totalTests + Integer.parseInt(parser.get(totalPassedText).toString()) + Integer.parseInt(parser.get(totalFailedText).toString());
            } catch (Exception e) {log.error("");}
        }

        return totalTests;
    }

    /**
     *
     * @param directory
     * @return
     */
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
                totalPassed = Integer.parseInt(parser.get(totalPassedText).toString());
                totalFailed = Integer.parseInt(parser.get(totalFailedText).toString());
                totalTimeTaken = Integer.parseInt(parser.get(totalTimeTakenText).toString());

                individualBuildData.put("name", (a.getName().split(".json")[0]).replace("Result_", " ").toUpperCase() + " " + startTime);
                individualBuildData.put(totalPassedText, totalPassed);
                individualBuildData.put(totalFailedText, totalFailed);
                individualBuildData.put("buildRunDate", startTime.replace("|", "-"));
                individualBuildData.put(totalTimeTakenText, TimeUnit.MILLISECONDS.toMinutes(totalTimeTaken));
                last10BuildDataArray.add(individualBuildData);
            } catch (Exception e) { log.error("");}
        }

        return last10BuildDataArray;
    }

    /**
     *
     * @param dir
     * @return
     */
    public int getCurrentBuildTotal(String dir) {
        int total = 0;

        try {
            File currentBuildReport = getLastModifiedJsonFile(dir);
            JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
            total = Integer.parseInt(parser.get(totalPassedText).toString()) + Integer.parseInt(parser.get(totalFailedText).toString());

        } catch (Exception e) { log.error("");}

        return total;
    }

    /**
     *
     * @param dir
     * @return
     */
    public int getCurrentBuildPassed(String dir) {
        int total = 0;

        try {
            File currentBuildReport = getLastModifiedJsonFile(dir);
            JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());

            total = Integer.parseInt(parser.get(totalPassedText).toString());

        } catch (Exception e) { log.error("");}

        return total;
    }

    /**
     *
     * @param dir
     * @return
     */
    public int getCurrentBuildFailed(String dir) {
        int total = 0;

        try {
            File currentBuildReport = getLastModifiedJsonFile(dir);
            JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
            total = Integer.parseInt(parser.get(totalFailedText).toString());

        } catch (Exception e) { log.error("");}

        return total;
    }

    /**
     *
     * @param dir
     * @return
     */
    public String getCurrentBuildTotalTime(String dir) {
        int total = 0;
        try {
            File currentBuildReport = getLastModifiedJsonFile(dir);
            JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());

            total = Integer.parseInt(parser.get(totalTimeTakenText).toString());

        } catch (Exception e) { log.error("");}
        return parseTime(total);
    }

    /**
     *
     * @param dir
     * @return
     */
    public String getCurrentBuildStartTime(String dir) {
        JSONObject parser = null;
        File currentBuildReport = getLastModifiedJsonFile(dir);
        parser = readJsonFile(currentBuildReport.getAbsolutePath());
        return parseTime(Long.parseLong(parser.get("startTime").toString()));
    }

    /**
     *
     * @param dir
     * @return
     */
    public String getCurrentBuildEndTime(String dir) {
        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
        return parseTime(Long.parseLong(parser.get("endTime").toString()));
    }

    /**
     *
     * @param dir
     * @param browserName
     * @return
     */
    public JSONObject getCurrentBuildBrowserWiseData(String dir, String browserName) {

        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
        JSONArray testsFileArray = (JSONArray) parser.get(browserText);
        int totalPass = 0;
        int totalFail = 0;
        JSONObject passFailData = new JSONObject();

        for (int i = 0; i < testsFileArray.size(); i++) {
            JSONObject browser = (JSONObject) testsFileArray.get(i);

            try {
                JSONObject chromeTestsFile = (JSONObject) browser.get(browserName);
                JSONArray suiteList = (JSONArray) chromeTestsFile.get("testsFile");

                for (Object suiteDetails : suiteList) {
                    JSONObject suite = (JSONObject) suiteDetails;
                    totalPass = totalPass + Integer.parseInt(suite.get(totalPassedText).toString());
                    totalFail = totalFail + Integer.parseInt(suite.get(totalFailedText).toString());
                }
            } catch (Exception e) {log.error("");}
        }
        passFailData.put(totalPassedText, totalPass);
        passFailData.put(totalFailedText, totalFail);

        return passFailData;
    }

    /**
     *
     * @param dir
     * @param browserName
     * @return
     */
    public JSONArray getModuleWiseData(String dir, String browserName) {

        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
        JSONArray suiteArray = (JSONArray) parser.get(browserText);
        JSONArray suiteList = null;

        for (int i = 0; i < suiteArray.size(); i++) {
            JSONObject browser = (JSONObject) suiteArray.get(i);
            try {
                JSONObject chromeSuite = (JSONObject) browser.get(browserName);
                suiteList = (JSONArray) chromeSuite.get("testsFileName");
            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(sw));
                log.error(sw.toString());
            }
        }
        return suiteList;
    }

    /**
     *
     * @param dir
     * @return
     */
    public JSONArray getBrowserExecutionReport(String dir) {

        File currentBuildReport = getLastModifiedJsonFile(dir);
        JSONObject parser = readJsonFile(currentBuildReport.getAbsolutePath());
        return (JSONArray) parser.get(browserText);
    }

    /**
     *
     * @param pass
     * @param fail
     * @return
     */
    public JSONArray browserResult(int pass, int fail) {
        JSONArray browserResult = new JSONArray();
        browserResult.add(pass);
        browserResult.add(fail);
        return browserResult;
    }


}
