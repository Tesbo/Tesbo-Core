package ReportBuilder;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReportBuilder {


    GetJsonData data = new GetJsonData();
    JSONArray dataArray = data.getLastBuildResultData(new File(getBuildHistoryPath()).getAbsolutePath());

    String buildHistory = new File(getBuildHistoryPath()).getAbsolutePath();

    public static void main(String[] args) {
        ReportBuilder builder = new ReportBuilder();
           builder.copyReportLibrary();

        StringBuffer indexfile = new StringBuffer();
        //index.html file generator
        indexfile = builder.generateHeader(indexfile);
        indexfile = builder.generateBody(indexfile);
        indexfile = builder.generateSideMenu(indexfile);
        indexfile = builder.generateTopHeader(indexfile);
        indexfile = builder.generateSummaryChart(indexfile);
        indexfile = builder.generateTimeSummaryChart(indexfile);
        indexfile = builder.generateFooter(indexfile);
        indexfile = builder.generateLatestBuildResultData(indexfile);
        indexfile = builder.generateTimeSummaryData(indexfile);


        File file = new File("./htmlReport/index.html");


        builder.writeReportFile(file.getAbsolutePath(), indexfile);


        //currentbuildresultGenerator
        StringBuffer currentBuildResult = new StringBuffer();
        currentBuildResult = builder.generateHeader(currentBuildResult);
        currentBuildResult = builder.generateBody(currentBuildResult);
        currentBuildResult = builder.generateSideMenu(currentBuildResult);
        currentBuildResult = builder.generateCurrentBuildSummary(currentBuildResult);

        currentBuildResult = builder.generatePieAndBarChart(currentBuildResult);

        currentBuildResult = builder.generateModuleWiseSummary(currentBuildResult);

        currentBuildResult = builder.generateModuleSummary(currentBuildResult);
        currentBuildResult = builder.generateBrowserWiseChartData(currentBuildResult);

        currentBuildResult = builder.generateDonutChartData(currentBuildResult);


        File currentBuildFile = new File("./htmlReport/currentBuildResult.html");

        builder.writeReportFile(currentBuildFile.getAbsolutePath(), currentBuildResult);


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

    public void copyReportLibrary() {
        String source = "./ReportLib/";
        File srcDir = new File(source);

        String destination = "./htmlReport/";
        File destDir = new File(destination);

        try {
            FileUtils.copyDirectory(srcDir, destDir);
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
                "    <link rel=\"stylesheet\" href=\"//cdnjs.cloudflare.com/ajax/libs/morris.js/0.5.1/morris.css\">\n" +
                "    <script src=\"//ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js\"></script>\n" +
                "    <script src=\"//cdnjs.cloudflare.com/ajax/libs/raphael/2.1.0/raphael-min.js\"></script>\n" +
                "    <script src=\"//cdnjs.cloudflare.com/ajax/libs/morris.js/0.5.1/morris.min.js\"></script>\n" +
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
                "        <div class=\"col-md-3 left_col\">\n" +
                "            <div class=\"left_col scroll-view\">\n" +
                "                <div class=\"navbar nav_title\" style=\"border: 0;\">\n" +
                "                    <a href=\"index.html\" class=\"site_title\"> <span>Tesbo Report</span></a>\n" +
                "                </div>\n" +
                "                <br/>\n" +
                "                <br/>\n" +
                "                <br/>\n" +
                "\n" +
                "                <!-- sidebar menu -->\n" +
                "                <div id=\"sidebar-menu\" class=\"main_menu_side hidden-print main_menu\">\n" +
                "                    <div class=\"menu_section\">\n" +
                "                        <ul class=\"nav side-menu\">\n" +
                "                            <li><a href=\"index.html\"><i class=\"fa fa-home\"></i> Home </a>\n" +
                "                            </li>\n" +
                "                            </li>\n" +
                "                            <li><a href=\"currentBuildResult.html\"><i class=\"fa fa-bar-chart-o\"></i> Current Build Report</a>\n" +
                "                            </li>\n" +
                "\n" +
                "                        </ul>\n" +
                "                    </div>\n" +
                "\n" +
                "\n" +
                "                </div>\n" +
                "\n" +
                "            </div>\n" +
                "        </div>\n");

        return sb;

    }

    public StringBuffer generateTopHeader(StringBuffer sb) {

        GetJsonData data = new GetJsonData();

        sb.append(" <div class=\"right_col\" role=\"main\">\n" +
                "            <!-- top tiles -->\n" +
                "            <div class=\"row tile_count\">\n" +
                "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "                    <span class=\"count_top\"><i class=\"fa fa-user\"></i> Total Builds</span>\n" +
                "                    <div class=\"count\">" + data.getTotalBuildCount(new File(getBuildHistoryPath()).getAbsolutePath()) + "</div>\n" +
                "\n" +
                "                </div>\n" +
                "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "                    <span class=\"count_top\"><i class=\"fa fa-clock-o\"></i> Average Time</span>\n" +
                "                    <div class=\"count\">" + data.getTotalBuildCount(new File(getBuildHistoryPath()).getAbsolutePath()) + "</div>\n" +
                "\n" +
                "                </div>\n" +
                "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "                    <span class=\"count_top\"><i class=\"fa fa-user\"></i> Total Test Run</span>\n" +
                "                    <div class=\"count green\">" + data.getTotalTestOfTheBuild(new File(getBuildHistoryPath()).getAbsolutePath()) + "</div>\n" +
                "\n" +
                "                </div>\n" +
                "\n" +
                "            </div>");


        return sb;
    }

    public StringBuffer generateSummaryChart(StringBuffer sb) {


        sb.append("  <div class=\"row\">\n" +
                "                <!-- bar charts group -->\n" +
                "                <div class=\"col-md-12 col-sm-6 col-xs-12\">\n" +
                "                    <div class=\"x_panel\">\n" +
                "                        <div class=\"x_title\">\n" +
                "                            <h2>Last 10 Build Summary</h2>\n" +
                "                            <div class=\"clearfix\"></div>\n" +
                "                        </div>\n" +
                "                        <div class=\"x_content1\">\n" +
                "                            <div id=\"lastBuildResult\" style=\"width:100%; height:280px;\"></div>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                <div class=\"clearfix\"></div>\n");


        return sb;

    }

    public StringBuffer generateTimeSummaryChart(StringBuffer sb) {


        sb.append("           <div class=\"col-md-12 col-sm-6 col-xs-12\">\n" +
                "                    <div class=\"x_panel\">\n" +
                "                        <div class=\"x_title\">\n" +
                "                            <h2>Last 10 Build Time Summary</h2>\n" +
                "\n" +
                "                            <div class=\"clearfix\"></div>\n" +
                "                        </div>\n" +
                "                        <div class=\"x_content1\">\n" +
                "                            <div id=\"lastBuildTimeResult\" style=\"width:100%; height:280px;\"></div>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                <div class=\"clearfix\"></div>\n" +
                "\n" +
                "\n" +
                "            </div>\n" +
                "\n" +
                "\n" +
                "            <br/>\n" +
                "        </div>\n" +
                "    </div>");


        return sb;

    }

    public StringBuffer generateFooter(StringBuffer sb) {

        sb.append("<footer>\n" +
                "        <div class=\"pull-right\">\n" +
                "            Tesbo Report Powered By<a href=\"https://jsbot.io\"> JSbot</a>\n" +
                "        </div>\n" +
                "        <div class=\"clearfix\"></div>\n" +
                "    </footer>\n" +
                "    <!-- /footer content -->\n" +
                "</div>\n" +
                "</div>\n");


        return sb;
    }

    public StringBuffer generateLatestBuildResultData(StringBuffer sb) {


        sb.append("\n" +
                "<script>\n" +
                "\n" +
                "    Morris.Bar({\n" +
                "        element: 'lastBuildResult',\n" +
                "        data: [\n");
        for (int i = 0; i < 10; i++) {

            JSONObject obj = (JSONObject) dataArray.get(i);
            sb.append(" {y: '" + obj.get("name") + "', a: " + obj.get("totalPassed") + ", b: " + obj.get("totalFailed") + "},\n ");

        }
        sb.append("         ],\n" +
                "        xkey: 'y',\n" +
                "        ykeys: ['a', 'b'],\n" +
                "        barColors: ['#a1d99b', '#fc9272'],\n" +
                "        labels: ['pass', 'failed']\n" +
                "    });" +
                " </script>\n");


        return sb;
    }

    public StringBuffer generateTimeSummaryData(StringBuffer sb) {


        sb.append("<script>\n" +
                "\n" +
                "    Morris.Line({\n" +
                "        element: 'lastBuildTimeResult',\n" +
                "        data: [\n" );


        for (int i = 0; i < 10; i++) {

            JSONObject obj = (JSONObject) dataArray.get(i);
            sb.append(" {y: '" + obj.get("buildRunDate") + "', a: "+obj.get("totalTimeTaken")+ "},\n ");

        }

               sb.append(
                "        ],\n" +
                "        xkey: 'y',\n" +
                "        ykeys: ['a'],\n" +
                "        labels: ['Time']\n" +
                "    });\n" +
                "</script>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n");


        return sb;
    }


    //------------------------------------------------------------------------------------------------------------


    public StringBuffer generateCurrentBuildSummary(StringBuffer sb) {
        sb.append("     <div class=\"right_col\" role=\"main\">\n" +
                "            <!-- top tiles -->\n" +
                "            <div class=\"row tile_count\">\n" +
                "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\" style=\"border-left : 2px solid #ADB2B5  \">\n" +
                "                    <span class=\"count_top\"> Total </span>\n" +
                "                    <div class=\"count\">"+data.getCurrentBuildTotal(new File(getBuildHistoryPath()).getAbsolutePath())+"</div>\n" +
                "\n" +
                "                </div>\n" +
                "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "                    <span class=\"count_top\"> Passed</span>\n" +
                "                    <div class=\"count\">"+data.getCurrentBuildPassed(buildHistory)+"</div>\n" +
                "\n" +
                "                </div>\n" +
                "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "                    <span class=\"count_top\"> Failed</span>\n" +
                "                    <div class=\"count \">"+data.getCurrentBuildFailed(buildHistory)+"</div>\n" +
                "\n" +
                "                </div>\n" +
                "\n" +
                "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "                    <span class=\"count_top\"> Total Time</span>\n" +
                "                    <div class=\"count \">"+data.getCurrentBuildTotalTime(buildHistory)+"</div>\n" +
                "\n" +
                "                </div>\n" +
                "\n" +
                "\n" +
                "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "                    <span class=\"count_top\"> Start Time</span>\n" +
                "                    <div class=\"count \">"+data.getCurrentBuildStartTime(buildHistory)+"</div>\n" +
                "\n" +
                "                </div>\n" +
                "\n" +
                "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
                "                    <span class=\"count_top\"> End Time</span>\n" +
                "                    <div class=\"count \">"+data.getCurrentBuildEndTime(buildHistory)+"</div>\n" +
                "\n" +
                "                </div>\n" +
                "\n" +
                "\n" +
                "            </div>\n" +
                "   ");


        return sb;
    }


    public StringBuffer generatePieAndBarChart(StringBuffer sb) {


        sb.append("   <div class=\"row\">\n" +
                "\n" +
                "\n" +
                "                <div class=\"col-md-4 col-sm-6 col-xs-12\">\n" +
                "                    <div class=\"x_panel\">\n" +
                "                        <div class=\"x_title\">\n" +
                "                            <h2>Pie Chart\n" +
                "\n" +
                "                            </h2>\n" +
                "\n" +
                "                            <div class=\"clearfix\"></div>\n" +
                "                        </div>\n" +
                "                        <div class=\"x_content1\">\n" +
                "                            <div id=\"buildSummary\" style=\"width:100%; height:280px;\"></div>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                    <div class=\"clearfix\"></div>\n" +
                "                </div>\n" +
                "\n" +
                "                <div class=\"col-md-8 col-sm-6 col-xs-12\">\n" +
                "                    <div class=\"x_panel\">\n" +
                "                        <div class=\"x_title\">\n" +
                "                            <h2>Browser Wise Report</h2>\n" +
                "\n" +
                "                            <div class=\"clearfix\"></div>\n" +
                "                        </div>\n" +
                "                        <div class=\"x_content1\">\n" +
                "                            <div id=\"browserReport\" style=\"width:100%; height:280px;\"></div>\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "                <div class=\"clearfix\"></div>\n" +
                "\n" +
                "\n" +
                "            </div>\n" +
                "\n" +
                "\n" +
                "            <br/>\n");

        return sb;
    }

    public StringBuffer generateModuleWiseSummary(StringBuffer sb) {

        sb.append(" <div class=\"row\">\n" +
                "\n" +
                "\n" +
                "                <div class=\"col-md-12 col-sm-6 col-xs-12\">\n" +
                "                    <div class=\"x_panel\">\n" +
                "                        <div class=\"x_title\">\n" +
                "                            <h2>Module Wise Summary\n" +
                "                            </h2>\n" +
                "                            <div class=\"clearfix\"></div>\n" +
                "                        </div>\n" +
                "                        <div class=\"x_content\">\n" +
                "                            <table class=\"table table-striped\">\n" +
                "                                <thead>\n" +
                "                                <tr>\n" +
                "                                    <th>#</th>\n" +
                "                                    <th>Module Name</th>\n" +
                "                                    <th>Total</th>\n" +
                "                                    <th>Passed</th>\n" +
                "                                    <th>Failed</th>\n" +
                "                                </tr>\n" +
                "                                </thead>\n" +
                "                                <tbody>\n" +
                "                                <tr>\n" +
                "                                    <th scope=\"row\">1</th>\n" +
                "                                    <td>Mark</td>\n" +
                "                                    <td>Otto</td>\n" +
                "                                    <td>@mdo</td>\n" +
                "                                    <td>@mdo</td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <th scope=\"row\">2</th>\n" +
                "                                    <td>Jacob</td>\n" +
                "                                    <td>Thornton</td>\n" +
                "\n" +
                "                                    <td>@mdo</td>\n" +
                "                                    <td>@fat</td>\n" +
                "                                </tr>\n" +
                "                                <tr>\n" +
                "                                    <th scope=\"row\">3</th>\n" +
                "                                    <td>Larry</td>\n" +
                "                                    <td>the Bird</td>\n" +
                "\n" +
                "                                    <td>@mdo</td>\n" +
                "                                    <td>@twitter</td>\n" +
                "                                </tr>\n" +
                "                                </tbody>\n" +
                "                            </table>\n" +
                "\n" +
                "                        </div>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "\n" +
                "            </div>\n" +
                "\n" +
                "            \n" +
                "        </div>\n" +
                "           ");


        return sb;
    }


    /**
     * I think here required the module data as well so it get the identify that how much module we have and how much test in that module
     *
     * @return
     */
    public StringBuffer generateModuleSummary(StringBuffer sb) {
        return sb;
    }


    public StringBuffer generateBrowserWiseChartData(StringBuffer sb) {

        sb.append("<script>\n" +
                "\n" +
                "    Morris.Bar({\n" +
                "        element: 'browserReport',\n" +
                "        data: [\n" +
                "            {y: 'Chrome', a: 100, b: 90},\n" +
                "            {y: 'Firefox', a: 75, b: 65},\n" +
                "            {y: 'Ie', a: 50, b: 40},\n" +
                "            {y: 'Opera', a: 50, b: 40},\n" +
                "            {y: 'Safari', a: 50, b: 40},\n" +
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
                "            {label: \"passed\", value: "+data.getCurrentBuildPassed(buildHistory)+"},\n" +
                "            {label: \"failed\", value: "+data.getCurrentBuildFailed(buildHistory)+"}\n" +
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


}
