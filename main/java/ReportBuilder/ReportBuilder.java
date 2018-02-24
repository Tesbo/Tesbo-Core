package ReportBuilder;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReportBuilder {


    public static void main(String[] args) {
        ReportBuilder builder = new ReportBuilder();
        //   builder.copyReportLibrary();

        StringBuffer sb = new StringBuffer();

        sb = builder.generateHeader(sb);
        sb = builder.generateBody(sb);
        sb = builder.generateSideMenu(sb);
        sb = builder.generateTopHeader(sb);

        builder.writeReportFile("/Volumes/Viral/Projects/SelebotFinal/selebot/htmlReport/index.html", sb);


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


    /**
     *
     * @param sb
     * @return
     */


public StringBuffer generateTopHeader(StringBuffer sb)
{

    GetJsonData data = new GetJsonData();

    sb.append(" <div class=\"right_col\" role=\"main\">\n" +
            "            <!-- top tiles -->\n" +
            "            <div class=\"row tile_count\">\n" +
            "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
            "                    <span class=\"count_top\"><i class=\"fa fa-user\"></i> Total Builds</span>\n" +
            "                    <div class=\"count\">"+data.getTotalBuildCount(new File(getBuildHistoryPath()).getAbsolutePath())+"</div>\n" +
            "\n" +
            "                </div>\n" +
            "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
            "                    <span class=\"count_top\"><i class=\"fa fa-clock-o\"></i> Average Time</span>\n" +
            "                    <div class=\"count\">123.50</div>\n" +
            "\n" +
            "                </div>\n" +
            "                <div class=\"col-md-4 col-sm-4 col-xs-6 tile_stats_count\">\n" +
            "                    <span class=\"count_top\"><i class=\"fa fa-user\"></i> Total Males</span>\n" +
            "                    <div class=\"count green\">2,500</div>\n" +
            "\n" +
            "                </div>\n" +
            "\n" +
            "            </div>");


    return sb;
}


public StringBuffer generateSummaryChart(StringBuffer sb)
{



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





    return  sb;

}

    public StringBuffer generateTimeSummaryChart(StringBuffer sb)
    {


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





        return  sb;

    }


public StringBuffer generateFooter(StringBuffer sb)
{


    sb.append("<footer>\n" +
            "        <div class=\"pull-right\">\n" +
            "            Tesbo Report Powered By<a href=\"https://jsbot.io\"> JSbot</a>\n" +
            "        </div>\n" +
            "        <div class=\"clearfix\"></div>\n" +
            "    </footer>\n" +
            "    <!-- /footer content -->\n" +
            "</div>\n" +
            "</div>\n");



    return  sb;
}




}
