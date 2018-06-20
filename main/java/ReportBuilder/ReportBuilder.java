package ReportBuilder;

import Execution.TestExecutionBuilder;
import com.diogonunes.jcdp.color.api.Ansi;
import logger.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class ReportBuilder implements Runnable {


    GetJsonData data = new GetJsonData();

    String buildHistory = new File(getBuildHistoryPath()).getAbsolutePath();
    JSONArray dataArray = null;
    Logger logger = new Logger();

    public static void main(String[] args) {
        ReportBuilder builder = new ReportBuilder();
        builder.generatReport();
    }


    public void generatReport() {
        logger.titleLog("-----------------------------------------------------------------------");
        logger.titleLog("Build Execution Completed");
        logger.titleLog("-----------------------------------------------------------------------\n");


        dataArray = data.getLastBuildResultData(new File(getBuildHistoryPath()).getAbsolutePath());
        ReportBuilder builder = new ReportBuilder();

        StringBuffer indexfile = new StringBuffer();
        //index.html file generator
        File file = new File("./htmlReport/index.html");
        File currentBuildFile = new File("./htmlReport/currentBuildResult.html");
        StringBuffer currentBuildResult = null;
        try {

         /*   indexfile = builder.generateHeader(indexfile);

            indexfile = builder.generateBody(indexfile);

            indexfile = builder.generateSideMenu(indexfile);

            indexfile = builder.generateTopHeader(indexfile);

            indexfile = builder.generateSummaryChart(indexfile);

            indexfile = builder.generateTimeSummaryChart(indexfile);

            indexfile = builder.generateFooter(indexfile);

            indexfile = builder.generateLatestBuildResultData(indexfile);

            indexfile = builder.generateTimeSummaryData(indexfile);


            builder.writeReportFile(file.getAbsolutePath(), indexfile);
*/
            //currentbuildresultGenerator
            currentBuildResult = new StringBuffer();
            currentBuildResult = builder.generateHeader(currentBuildResult);
            currentBuildResult = builder.generateBody(currentBuildResult);
            currentBuildResult = builder.generateSideMenu(currentBuildResult);
            currentBuildResult = builder.generateCurrentBuildSummary(currentBuildResult);
            currentBuildResult = builder.generatePieAndBarChart(currentBuildResult);
            currentBuildResult = builder.generateModuleSummary(currentBuildResult);
            currentBuildResult = builder.generateBrowserWiseChartData(currentBuildResult);
            currentBuildResult = builder.generateDonutChartData(currentBuildResult);
            builder.writeReportFile(currentBuildFile.getAbsolutePath(), currentBuildResult);

        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.writeReportFile(currentBuildFile.getAbsolutePath(), currentBuildResult);

        logger.customeLog("\nReport Generated at :" + file.getAbsolutePath() + "\n", Ansi.FColor.NONE);
        logger.titleLog("-----------------------------------------------------------------------");


    }

    public String getReportLibPath() {

        return "./htmlReport/";
    }

    public String getBuildHistoryPath() {

        return "./htmlReport/Build History/";
    }

    public void writeReportFile(String filePath, StringBuffer fileContent) {

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filePath, false), 8192 * 4);
            writer.write(fileContent.toString() + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeReportFile(String filePath, StringBuilder fileContent) {

        BufferedWriter writer = null;


        try {
            writer = new BufferedWriter(new FileWriter(filePath, false), 8192 * 4);
            writer.write(fileContent.toString() + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public StringBuffer generateHeader(StringBuffer sb) {


        sb.append("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "    <!-- Meta, title, CSS, favicons, etc. -->\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "\n" +
                "\n" +
                "    <title>Tesbo Test Report| </title>\n" +
                "    <link rel=\"stylesheet\" href=\"http://cdnjs.cloudflare.com/ajax/libs/morris.js/0.5.1/morris.css\">\n" +
                "    <script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js\"></script>\n" +
                "    <script src=\"http://cdnjs.cloudflare.com/ajax/libs/raphael/2.1.0/raphael-min.js\"></script>\n" +
                "    <script src=\"http://cdnjs.cloudflare.com/ajax/libs/morris.js/0.5.1/morris.min.js\"></script>\n" +
                "    <!-- Bootstrap -->\n" +
                "    <link href=\"lib/bootstrap/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
                "    <!-- Font Awesome -->\n" +
                "    <link href=\"../lib/font-awesome/css/font-awesome.min.css\" rel=\"stylesheet\">\n" +
                "    <!-- NProgress -->\n" +
                "\n" +
                "    <!-- Custom Theme Style -->\n" +
                "    <link href=\"lib/build/css/custom.min.css\" rel=\"stylesheet\">\n" +
                "</head>");


        return sb;
    }

    public StringBuffer generateBody(StringBuffer sb) {


        sb.append("<body class=\"nav-md\">\n" +
                "<div class=\"container body\">\n" +
                "    <div class=\"main_container\">");

        return sb;

    }

    public StringBuffer generateSideMenu(StringBuffer sb) {


        sb.append("\n" +
                "<div class=\"col-md-3 left_col\">\n" +
                "  <div class=\"left_col scroll-view\">\n" +
                "   <div class=\"navbar nav_title\" style=\"border: 0;\">\n" +
                "     <a href=\"index.html\" class=\"site_title\"> <span>Tesbo Report</span></a>\n" +
                "     </div>\n" +
                "     <br/>\n" +
                "     <br/>\n" +
                "     <br/>\n" +
                "\n" +
                "  <!-- sidebar menu -->\n" +
                "<div id=\"sidebar-menu\" class=\"main_menu_side hidden-print main_menu\">\n" +
                " <div class=\"menu_section\">\n" +
                "  <ul class=\"nav side-menu\">\n" +
                "   <li><a href=\"index.html\"><i class=\"fa fa-home\"></i> Home </a>\n" +
                "   </li>\n" +
                "   </li>\n" +
                "   <li><a href=\"currentBuildResult.html\"><i class=\"fa fa-bar-chart-o\"></i> Current Build Report</a>\n" +
                "   </li>\n" +
                "\n" +
                "  </ul>\n" +
                " </div>\n" +
                "\n" +
                "\n" +
                "</div>\n" +
                "\n" +
                "</div>\n" +
                "</div>\n");

        return sb;

    }

    public StringBuffer generateTopHeader(StringBuffer sb) {

        GetJsonData data = new GetJsonData();

        sb.append("<div class=\"right_col\" role=\"main\">\n" +
                "<!-- top tiles -->\n" +
                "<div class=\"row tile_count\">\n" +
                " <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "  <span class=\"count_top\"><i class=\"fa fa-user\"></i> Total Builds</span>\n" +
                "  <div class=\"count\">" + data.getTotalBuildCount(new File(getBuildHistoryPath()).getAbsolutePath()) + "</div>\n" +
                "\n" +
                "  </div>\n" +
                " <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "  <span class=\"count_top\"><i class=\"fa fa-clock-o\"></i> Average Time</span>\n" +
                "   <div class=\"count\">" + data.getTotalBuildCount(new File(getBuildHistoryPath()).getAbsolutePath()) + "</div>\n" +
                "\n" +
                "   </div>\n" +
                "  <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "   <span class=\"count_top\"><i class=\"fa fa-user\"></i> Total Test Run</span>\n" +
                "   <div class=\"count green\">" + data.getTotalTestOfTheBuild(new File(getBuildHistoryPath()).getAbsolutePath()) + "</div>\n" +
                "\n" +
                "  </div>\n" +
                "\n" +
                " </div>");
        return sb;
    }

    public StringBuffer generateSummaryChart(StringBuffer sb) {


        sb.append("<div class=\"row\">\n" +
                "                <!-- bar charts group -->\n" +
                "<div class=\"col-md-12 col-sm-6 col-xs-12\">\n" +
                "  <div class=\"x_panel\">\n" +
                "  <div class=\"x_title\">\n" +
                "  <h2>Last 10 Build Summary</h2>\n" +
                "  <div class=\"clearfix\"></div>\n" +
                "  </div>\n" +
                "  <div class=\"x_content1\">\n" +
                "  <div id=\"lastBuildResult\" style=\"width:100%; height:280px;\"></div>\n" +
                "  </div>\n" +
                "  </div>\n" +
                "  </div>\n" +
                "  <div class=\"clearfix\"></div>\n");
        return sb;

    }

    public StringBuffer generateTimeSummaryChart(StringBuffer sb) {


        sb.append("<div class=\"col-md-12 col-sm-6 col-xs-12\">\n" +
                "  <div class=\"x_panel\">\n" +
                "   <div class=\"x_title\">\n" +
                "    <h2>Last 10 Build Time Summary</h2>\n" +
                "\n" +
                "   <div class=\"clearfix\"></div>\n" +
                "   </div>\n" +
                "   <div class=\"x_content1\">\n" +
                "   <div id=\"lastBuildTimeResult\" style=\"width:100%; height:280px;\"></div>\n" +
                "   </div>\n" +
                "   </div>\n" +
                "   </div>\n" +
                "   <div class=\"clearfix\"></div>\n" +
                "</div>\n" +
                "<br/>\n" +
                "</div>\n" +
                "</div>");
        return sb;

    }

    public StringBuffer generateFooter(StringBuffer sb) {

        sb.append("<footer>\n" +
                "<div class=\"pull-right\">\n" +
                "Tesbo Report<a href=\"\"> </a>\n" +
                "</div>\n" +
                "<div class=\"clearfix\"></div>\n" +
                "</footer>\n" +
                "<!-- /footer content -->\n" +
                "</div>\n" +
                "</div>\n");

        return sb;
    }

    public StringBuffer generateLatestBuildResultData(StringBuffer sb) {

        sb.append("\n" +
                "<script>\n" +
                "\n" +
                "Morris.Bar({\n" +
                "element: 'lastBuildResult',\n" +
                "data: [\n");
        for (int i = 9; i >= 0; i--) {
            try {

                JSONObject obj = (JSONObject) data.getLastBuildResultData(new File(getBuildHistoryPath()).getAbsolutePath()).get(i);
                sb.append(" {y: '" + obj.get("name") + "', a: " + obj.get("totalPassed") + ", b: " + obj.get("totalFailed") + "},\n ");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        sb.append("],\n" +
                "xkey: 'y',\n" +
                "ykeys: ['a', 'b'],\n" +
                "barColors: ['#a1d99b', '#fc9272'],\n" +
                "labels: ['pass', 'failed']\n" +
                "});" +
                " </script>\n");


        return sb;
    }

    public StringBuffer generateTimeSummaryData(StringBuffer sb) {


        sb.append("<script>\n" +
                "\n" +
                "Morris.Line({\n" +
                "element: 'lastBuildTimeResult',\n" +
                "data: [\n");


        for (int i = 0; i < 10; i++) {
            try {
                JSONObject obj = (JSONObject) data.getLastBuildResultData(new File(getBuildHistoryPath()).getAbsolutePath()).get(i);
                sb.append(" {y: '" + obj.get("buildRunDate") + "', a: " + obj.get("totalTimeTaken") + "},\n ");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        sb.append(
                "],\n" +
                        "xkey: 'y',\n" +
                        "ykeys: ['a'],\n" +
                        "labels: ['Time']\n" +
                        "});\n" +
                        "</script>\n" +
                        "\n" +
                        "</body>\n" +
                        "</html>\n");


        return sb;
    }


    //------------------------------------------------------------------------------------------------------------


    public StringBuffer generateCurrentBuildSummary(StringBuffer sb) {


        logger.customeLog("| Total : " + data.getCurrentBuildTotal(new File(getBuildHistoryPath()).getAbsolutePath()), Ansi.FColor.NONE);
        logger.customeLog(" | Passed : " + data.getCurrentBuildPassed(buildHistory), Ansi.FColor.NONE);
        logger.customeLog(" | Failed : " + data.getCurrentBuildFailed(buildHistory), Ansi.FColor.NONE);
        logger.customeLog(" | Time : " + data.getCurrentBuildTotalTime(buildHistory) + " |\n", Ansi.FColor.NONE);


        sb.append("<div class=\"right_col\" role=\"main\">\n" +
                "<!-- top tiles -->\n" +
                "<div class=\"row tile_count\">\n" +
                "<div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\" style=\"border-left : 2px solid #ADB2B5  \">\n" +
                "<span class=\"count_top\"> Total </span>\n" +
                "<div class=\"count\">" + data.getCurrentBuildTotal(new File(getBuildHistoryPath()).getAbsolutePath()) + "</div>\n" +
                "\n" +
                "</div>\n" +
                "<div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "<span class=\"count_top\"> Passed</span>\n" +
                "<div class=\"count\">" + data.getCurrentBuildPassed(buildHistory) + "</div>\n" +
                "\n" +
                "</div>\n" +
                "<div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "<span class=\"count_top\"> Failed</span>\n" +
                "<div class=\"count \">" + data.getCurrentBuildFailed(buildHistory) + "</div>\n" +
                "\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "<span class=\"count_top\"> Total Time</span>\n" +
                "<div class=\"count \">" + data.getCurrentBuildTotalTime(buildHistory) + "</div>\n" +
                "\n" +
                "</div>\n" +
                "\n" +
                "\n" +
                "<div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "<span class=\"count_top\"> Start Time</span>\n" +
                "<div class=\"count \">" + data.getCurrentBuildStartTime(buildHistory)+ "</div>\n" +
                "\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "<span class=\"count_top\"> End Time</span>\n" +
                "<div class=\"count \">" + data.getCurrentBuildEndTime(buildHistory) + "</div>\n" +
                "\n" +
                "</div>\n" +
                "\n" +
                "\n" +
                "</div>\n" +
                "");


        return sb;
    }


    public StringBuffer generatePieAndBarChart(StringBuffer sb) {


        sb.append("<div class=\"row\">\n" +
                "\n" +
                "\n" +
                "<div class=\"col-md-4 col-sm-6 col-xs-12\">\n" +
                "<div class=\"x_panel\">\n" +
                "<div class=\"x_title\">\n" +
                "<h2>Pie Chart\n" +
                "\n" +
                "</h2>\n" +
                "\n" +
                "<div class=\"clearfix\"></div>\n" +
                "</div>\n" +
                "<div class=\"x_content1\">\n" +
                "<div id=\"buildSummary\" style=\"width:100%; height:280px;\"></div>\n" +
                "</div>\n" +
                "</div>\n" +
                "<div class=\"clearfix\"></div>\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"col-md-8 col-sm-6 col-xs-12\">\n" +
                "<div class=\"x_panel\">\n" +
                "<div class=\"x_title\">\n" +
                "<h2>Browser Wise Report</h2>\n" +
                "\n" +
                "<div class=\"clearfix\"></div>\n" +
                "</div>\n" +
                "<div class=\"x_content1\">\n" +
                "<div id=\"browserReport\" style=\"width:100%; height:280px;\"></div>\n" +
                "</div>\n" +
                "</div>\n" +
                "</div>\n" +
                "<div class=\"clearfix\"></div>\n" +
                "\n" +
                "\n" +
                "</div>\n" +
                "\n" +
                "\n" +
                "<br/>\n");

        return sb;
    }

    public StringBuffer generateModuleWiseSummary(StringBuffer sb) {


        JSONArray chromeSuite = data.getModuleWiseData(buildHistory, "chrome");
        JSONArray firefoxSuite = data.getModuleWiseData(buildHistory, "firefox");
        JSONArray operaSuite = data.getModuleWiseData(buildHistory, "opera");
        JSONArray ieSuite = data.getModuleWiseData(buildHistory, "ie");
        JSONArray safariSuite = data.getModuleWiseData(buildHistory, "safari");


        sb = generatePerModuleSummary(sb, chromeSuite, "Chrome");
        sb = generatePerModuleSummary(sb, firefoxSuite, "Firefox");
        sb = generatePerModuleSummary(sb, ieSuite, "IE");
        sb = generatePerModuleSummary(sb, operaSuite, "Opera");
        sb = generatePerModuleSummary(sb, safariSuite, "Safari");


        return sb;
    }


    public StringBuffer generatePerModuleSummary(StringBuffer sb, JSONArray suiteArray, String browserName) {

        try {


            if (suiteArray.size() > 0) {

                sb.append("<div class=\"row\">\n" +
                        "\n" +
                        "\n" +
                        "<div class=\"col-md-12 col-sm-6 col-xs-12\">\n" +
                        "<div class=\"x_panel\">\n" +
                        "<div class=\"x_title\">\n" +
                        "<h2>Module Wise Summary : " + browserName + "\n" +
                        "</h2>\n" +
                        "<div class=\"clearfix\"></div>\n" +
                        "</div>\n" +
                        "<div class=\"x_content\">\n" +
                        "<table class=\"table table-striped\">\n" +
                        "<thead>\n" +
                        "<tr>\n" +
                        "<th>#</th>\n" +
                        "<th>Module Name</th>\n" +
                        "<th>Total</th>\n" +
                        "<th>Passed</th>\n" +
                        "<th>Failed</th>\n" +
                        "</tr>\n" +
                        "</thead>\n" +
                        "<tbody>\n");


                for (int i = 0; i < suiteArray.size(); i++) {

                    sb.append(" <tr>\n" +
                            " <th scope=\"row\">" + (i + 1) + "</th>\n" +
                            " <td>" + ((JSONObject) suiteArray.get(i)).get("suiteName") + "</td>\n" +
                            " <td>" + (Integer.parseInt(((JSONObject) suiteArray.get(i)).get("totalFailed").toString()) + Integer.parseInt(((JSONObject) suiteArray.get(i)).get("totalPassed").toString())) + "</td>\n" +
                            " <td>" + ((JSONObject) suiteArray.get(i)).get("totalPassed") + "</td>\n" +
                            " <td>" + ((JSONObject) suiteArray.get(i)).get("totalFailed") + "</td>\n" +
                            " </tr>");
                }


                sb.append("</tbody>\n" +
                        "</table>\n" +
                        "\n" +
                        "</div>\n" +
                        "</div>\n" +
                        "</div>\n" +
                        "\n" +
                        "</div>\n" +
                        "\n");

            }


        } catch (Exception e) {


        }


        return sb;

    }

    /**
     * I think here required the module data as well so it get the identify that how much module we have and how much test in that module
     *
     * @return
     */
    public StringBuffer generateModuleSummary(StringBuffer sb) {


        JSONArray browserArray = data.getBrowserExecutionReport(buildHistory);


        //array of all the browser

        sb.append("<div class=\"row\">\n" +
                "<div class=\"col-md-12 col-sm-6 col-xs-12\">\n" +
                "<div class=\"x_panel\">\n" +
                "<div class=\"x_title\">\n" +
                "<h2>Browser Wise Execution Report\n" +
                "</h2>\n" +
                "<div class=\"clearfix\"></div>\n" +
                "</div>\n" +
                "<br>");

        for (Object singleBrowser : browserArray) {

            String browser = " ";

            Set keys = ((JSONObject) singleBrowser).keySet();
            Iterator iter = keys.iterator();

            ArrayList browserList = new ArrayList();
            int i = 0;
            while (iter.hasNext()) {

                browser = iter.next().toString();

                if (!browser.equals(" ")) {
                    browserList.add(browser);
                    browser = " ";
                }

            }

            browser = browserList.get(0).toString();

            sb.append("<!-- Browser : " + browser + "-->");

            sb.append("<div style=\"border : 3px solid #E6E9ED ;padding:5px \">\n" +
                    "<a class=\"panel-heading\" role=\"tab\"\n" +
                    "data-toggle=\"collapse\"\n" +
                    "data-parent=\"#accordion\" href=\"#" + browser + "\"\n" +
                    "aria-expanded=\"false\"\n" +
                    "aria-controls=\"collapseOne\">\n" +
                    " <h4 class=\"panel-title\">\n" +
                    "\n" +
                    "<div name=\"browserLogo\" align=\"center\" class=\"accordion\" role=\"tablist\"\n" +
                    "aria-multiselectable=\"true\">\n" +
                    "\n" +
                    "<img src=\"../htmlReport/lib/Icon/" + browser + ".svg\"\n" +
                    "style=\"max-width: 100%;height: 20px;\">\n" +
                    "<h5>" + browser.toUpperCase() + "</h5>\n" +
                    "\n" +
                    "</div>\n" +
                    "\n" +
                    "</h4>\n" +
                    "</a>\n");


            //array of the all the suites


            JSONArray suiteList = ((JSONArray) ((JSONObject) ((JSONObject) singleBrowser).get(browser)).get("suits"));


            sb.append("<div id=\"" + browser + "\" class=\"panel-collapse collapse\">\n");


            for (Object suite : suiteList)

            {


                sb.append("<!-- Suite : " + ((JSONObject) suite).get("suiteName") + "-->");


                JSONArray testList = (JSONArray) ((JSONObject) suite).get("tests");
                sb.append(

                        "<div class=\"x_panel\" class=\"panel-collapse collapse\"\n" +
                                "role=\"tabpanel\"\n" +
                                "aria-labelledby=\"headingOne\">\n" +
                                "<div class=\"x_title\">\n" +
                                "<h2><i class=\"fa fa-align-left\"></i> " + ((JSONObject) suite).get("suiteName") + "\n" +
                                "</h2>\n" +
                                " <div class=\"nav navbar-right\" style=\"padding-top : 5px \">\n" +
                                "<font>Total  : <b>" + (Double.parseDouble(((JSONObject) suite).get("totalPassed").toString()) + Double.parseDouble(((JSONObject) suite).get("totalFailed").toString())) + "</b> |</font>\n" +
                                "<font>Passed : <b>" + ((JSONObject) suite).get("totalPassed") + "</b> |</font>\n" +
                                "<font>Failed : <b>" + ((JSONObject) suite).get("totalFailed") + "</b>  |</font>\n" +
                                " </div>\n" +
                                "  <div class=\"clearfix\"></div>\n" +
                                " </div>\n" +
                                "                      ");

                sb.append("<div class=\"x_content\">\n" +
                        "\n");

                for (Object test : testList) {


                    sb.append("<!-- test : " + test + "-->");


                    JSONObject testDetails = (JSONObject) test;

                    boolean isTestFailed = false;
                    String osName = "";
                    if (((JSONObject) test).get("osName").toString().toLowerCase().contains("win")) {
                        osName = "windows";
                    }

                    if (((JSONObject) test).get("osName").toString().toLowerCase().contains("linux")) {
                        osName = "linux";
                    }


                    if (((JSONObject) test).get("osName").toString().toLowerCase().contains("ubantu")) {
                        osName = "ubantu";
                    }

                    if (((JSONObject) test).get("osName").toString().toLowerCase().contains("mac")) {
                        osName = "mac";
                    }


                    String fontColor = "";
                    if (((JSONObject) test).get("status").toString().toLowerCase().contains("fail")) {
                        fontColor = "fc9272";
                        isTestFailed = true;
                    }

                    if (((JSONObject) test).get("status").toString().toLowerCase().contains("pass")) {
                        fontColor = "a1d99b";

                    }


                    String stacktrace = "";
                    String screenShotpath = "";


                    //System.out.println("Test " + test);


                    //System.out.println(((JSONObject) test).get("testName") + "" + isTestFailed);


                    if (isTestFailed) {

                        try {
                            stacktrace = ((JSONObject) test).get("fullStackTrace").toString();
                        } catch (Exception e) {
                            logger.errorLog("StackTrace Not Found");

                        }
                        try {
                            screenShotpath = ((JSONObject) test).get("screenshot").toString();
                            //System.out.println("StackTrace " + stacktrace);

                        } catch (Exception e) {
                            logger.errorLog("Screenshot Not Found");

                        }
                    }


                    sb.append("<!-- start accordion -->\n" +
                            "<div class=\"accordion\" id=\"" + browser + testDetails.get("testName") + "\" role=\"tablist\"\n" +
                            "aria-multiselectable=\"true\">\n" +
                            "<div class=\"panel\">\n" +
                            "<a class=\"panel-heading\" role=\"tab\"\n" +
                            "data-toggle=\"collapse\"\n" +
                            "data-parent=\"#accordion\" href=\"#" + browser + testDetails.get("testName").toString().replace(" ", "") + "\"\n" +
                            "aria-expanded=\"true\"\n" +
                            "aria-controls=\"collapseOne\">\n" +
                            "<h4 class=\"panel-title\">\n" +
                            "<font color=\"#" + fontColor + "\"> " + testDetails.get("testName").toString() + "</font>\n" +
                            "<div class=\"nav navbar-right \">\n" +
                            "<img src=\"../htmlReport/lib/Icon/" + browser + ".svg\"\n" +
                            "style=\"max-width: 100%;height: 20px;\"\n" +
                            "data-toggle=\"tooltip\" data-placement=\"left\"\n" +
                            "title=\"" + testDetails.get("browserVersion").toString() + "\">\n" +
                            "<img src=\"../htmlReport/lib/Icon/" + osName + ".svg\"\n" +
                            "style=\"max-width: 100%;height: 25px;\"\n" +
                            "\n" +
                            "data-toggle=\"tooltip\" data-placement=\"left\"\n" +
                            "title=\"" + osName + "\">\n" +
                            "</div>\n" +
                            "</h4>\n" +

                            "</a>\n" +
                            "<div id=\"" + browser + testDetails.get("testName").toString().replace(" ", "") + "\" class=\"panel-collapse collapse\"\n" +
                            "role=\"tabpanel\"\n" +
                            "aria-labelledby=\"headingOne\">\n" +
                            "<div class=\"panel-body\">\n" +
                            "<table class=\"table table-bordered\">\n" +
                            "<thead>\n" +
                            "<tr>\n" +
                            "<th>#</th>\n" +
                            "<th>Step</th>\n" +
                            "<th>Status</th>\n" +
                            "</tr>\n" +
                            "</thead>\n" +
                            "<tbody>\n");


                    JSONArray stepList = (JSONArray) ((JSONObject) test).get("testStep");


                    for (Object step : stepList) {


                        sb.append("<!-- Step : " + step + "-->");


                        JSONObject stepDetails = (JSONObject) step;
                        sb.append(" <tr>\n" +
                                " <th scope=\"row\">" + stepDetails.get("stepIndex") + "</th>\n" +
                                " <td>" + stepDetails.get("steps") + "</td>\n" +
                                " <td>" + stepDetails.get("status") + "</td>\n" +
                                "</tr>");

                    }


                    sb.append(
                            "</tbody>\n" +
                                    "</table>\n" +
                                    "</div>\n" +
                                    "\n" +
                                    "\n");


                    if (isTestFailed) {
                        sb.append(
                                "<div class=\"panel-body\" style=\"border-style: dotted;\">\n" +
                                        "<p><strong>ScreenShot</strong>\n" +
                                        "</p>\n" +
                                        "\n" +
                                        "<img src=\" " + screenShotpath + " \" \n" +
                                        "style=\"max-width: 100%;height: auto;\">\n" +

                                        " </div>\n" +
                                        "\n");
                    }

                    if (isTestFailed) {
                        sb.append(
                                "<br>\n" +

                                        "<div class=\"panel-body\" style=\"border-style: dotted;\">\n" +
                                        "<p><strong>Stack Trace</strong>\n" +
                                        "</p>\n" +
                                        stacktrace +
                                        "</div>\n" +
                                        "\n");

                    }


                    sb.append(
                            "</div>\n" +

                                    "</div>\n" +
                                    "</div>\n");

                }


                sb.append(" </div>\n" +
                        "</div>\n" +
                        "<br>");

            }

            //array of the all the tests

            sb.append("<div></div></div>\n" +
                    " </div>\n" +
                    "<br>\n");


        }
        sb.append("<div>");

        return sb;
    }


    public StringBuffer generateBrowserWiseChartData(StringBuffer sb) {


        sb.append("<script>\n" +
                "\n" +
                "    Morris.Bar({\n" +
                "        element: 'browserReport',\n" +
                "        data: [\n" +
                "            {y: 'Chrome', a: " + data.getCurrentBuildBrowserWiseData(buildHistory, "chrome").get("totalPassed") + ", b: " + data.getCurrentBuildBrowserWiseData(buildHistory, "chrome").get("totalFailed") + "},\n" +
                "            {y: 'Firefox', a: " + data.getCurrentBuildBrowserWiseData(buildHistory, "firefox").get("totalPassed") + ", b: " + data.getCurrentBuildBrowserWiseData(buildHistory, "firefox").get("totalFailed") + "},\n" +
                "            {y: 'Ie', a: " + data.getCurrentBuildBrowserWiseData(buildHistory, "ie").get("totalPassed") + ", b: " + data.getCurrentBuildBrowserWiseData(buildHistory, "ie").get("totalFailed") + "},\n" +
                "            {y: 'Opera', a: " + data.getCurrentBuildBrowserWiseData(buildHistory, "opera").get("totalPassed") + ", b: " + data.getCurrentBuildBrowserWiseData(buildHistory, "opera").get("totalFailed") + "},\n" +
                "            {y: 'Safari', a: " + data.getCurrentBuildBrowserWiseData(buildHistory, "safari").get("totalPassed") + ", b: " + data.getCurrentBuildBrowserWiseData(buildHistory, "safari").get("totalFailed") + "},\n" +
                "\n" +
                "        ],\n" +
                "        xkey: 'y',\n" +
                "        ykeys: ['a', 'b'],\n" +
                "        barColors: ['#a1d99b', '#fc9272'],\n" +
                "        labels: ['pass', 'failed']\n" +
                "    });\n" +
                "\n" +
                "\n" +
                "</script>");

        return sb;
    }


    public StringBuffer generateDonutChartData(StringBuffer sb)

    {

        sb.append("<script>\n" +
                "\n" +
                "    Morris.Donut({\n" +
                "        element: 'buildSummary',\n" +
                "        colors: ['#a1d99b', '#fc9272'],\n" +
                "        data: [\n" +
                "            {label: \"passed\", value: " + data.getCurrentBuildPassed(buildHistory) + "},\n" +
                "            {label: \"failed\", value: " + data.getCurrentBuildFailed(buildHistory) + "}\n" +
                "        ]\n" +
                "\n" +
                "    });\n" +
                "\n" +
                "\n" +
                "</script>\n" +
                "<script src=\"lib/jquery/dist/jquery.min.js\"></script>\n" +
                "<script src=\"lib/bootstrap/dist/js/bootstrap.min.js\"></script>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n");

        return sb;
    }


    public void startThread() {
        System.out.println("insider Start thread");
        ReportBuilder rb = new ReportBuilder();

        Thread t1 = new Thread(rb);

        t1.start();
    }


    @Override
    public void run() {

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (TestExecutionBuilder.buildRunning) {
            try {

                generatReport();

                try {
                    Thread.sleep(13000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            } catch (Exception e) {
            }
        }


    }
}
