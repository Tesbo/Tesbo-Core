package ReportBuilder;

import Execution.TestExecutionBuilder;
import framework.GetConfiguration;
import logger.TesboLogger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class ReportBuilder implements Runnable {


    GetJsonData data = new GetJsonData();

    String buildHistory = new File(getBuildHistoryPath()).getAbsolutePath();
    JSONArray dataArray = null;
    TesboLogger tesboLogger = new TesboLogger();

    public static void main(String[] args) {
        ReportBuilder builder = new ReportBuilder();
        builder.generatReport();
    }


    public void generatReport() {

        GetConfiguration getConfiguration=new GetConfiguration();
        dataArray = data.getLastBuildResultData(new File(getBuildHistoryPath()).getAbsolutePath());
        ReportBuilder builder = new ReportBuilder();

        StringBuffer indexfile = new StringBuffer();
        //index.html file generator
        File file = new File("./htmlReport/index.html");

        String reportFileName=getConfiguration.getReportFileName();

        if(reportFileName.equals("")){
            reportFileName="currentBuildResult";
        }
        File currentBuildFile = new File("./htmlReport/"+reportFileName+".html");
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
            //currentBuildResult = builder.generateSideMenu(currentBuildResult);
            currentBuildResult = builder.generateCurrentBuildSummary(currentBuildResult);
            currentBuildResult = builder.generatePieAndBarChart(currentBuildResult);
            currentBuildResult = builder.generateModuleSummary(currentBuildResult);
            currentBuildResult = builder.generateBrowserWiseChartData(currentBuildResult);
            currentBuildResult = builder.generateDonutChartData(currentBuildResult);
            builder.writeReportFile(currentBuildFile.getAbsolutePath(), currentBuildResult);

        } catch (Exception e) {
        }

        builder.writeReportFile(currentBuildFile.getAbsolutePath(), currentBuildResult);

    }

    public void copyReport(String reportFileName){
        File source = new File("./htmlReport/"+reportFileName+".html");

        File files = new File("./htmlReport/Report History");
        if (!files.exists()) {
            files.mkdirs();
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
        LocalDateTime now = LocalDateTime.now();
        File dest = new File("./htmlReport/Report History/"+reportFileName+"_"+dtf.format(now)+".html");
        try {
            Files.copy(source.toPath(), dest.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                "<style>   .x_title h2,table.tile_info td p{white-space:nowrap;text-overflow:ellipsis}.site_title,.x_title h2,table.tile_info td p{text-overflow:ellipsis}.detail a,.expand,.jqstooltip,.paging_full_numbers a:hover,.site_title:focus,.site_title:hover,a,a:focus,a:hover{text-decoration:none}.byline,.main_menu .fa{-webkit-font-smoothing:antialiased}.daterangepicker .ranges li{color:#73879C}.daterangepicker .ranges li.active,.daterangepicker .ranges li:hover{background:#536A7F;border:1px solid #536A7F;color:#fff}.daterangepicker .input-mini{background-color:#eee;border:1px solid #ccc;box-shadow:none!important}.daterangepicker .input-mini.active{border:1px solid #ccc}.daterangepicker select.ampmselect,.daterangepicker select.hourselect,.daterangepicker select.minuteselect,.daterangepicker select.monthselect,.daterangepicker select.secondselect,.daterangepicker select.yearselect{font-size:12px;padding:1px;margin:0;cursor:default;height:30px;border:1px solid #ADB2B5;line-height:30px;border-radius:0!important}.daterangepicker select.monthselect{margin-right:2%}.daterangepicker td.in-range{background:#E4E7EA;color:#73879C}.daterangepicker td.active,.daterangepicker td.active:hover{background-color:#536A7F;color:#fff}.daterangepicker th.available:hover{background:#eee;color:#34495E}.daterangepicker:after,.daterangepicker:before{content:none}.daterangepicker .calendar.single{margin:0 0 4px}.daterangepicker .calendar.single .calendar-table{width:224px;padding:0 0 4px!important}.daterangepicker .calendar.single .calendar-table thead tr:first-child th{padding:8px 5px}.daterangepicker .calendar.single .calendar-table thead th{border-radius:0}.daterangepicker.picker_1{color:#fff;background:#34495E}.daterangepicker.picker_1 .calendar-table{background:#34495E}.daterangepicker.picker_1 .calendar-table thead tr{background:#213345}.daterangepicker.picker_1 .calendar-table thead tr:first-child{background:#1ABB9C}.daterangepicker.picker_1 .calendar-table td.off{background:#34495E;color:#999}.daterangepicker.picker_1 .calendar-table td.available:hover{color:#34495E}.daterangepicker.picker_2 .calendar-table thead tr{color:#1ABB9C}.daterangepicker.picker_2 .calendar-table thead tr:first-child{color:#73879C}.daterangepicker.picker_3 .calendar-table thead tr:first-child{color:#fff;background:#1ABB9C}.daterangepicker.picker_4 .calendar-table thead tr:first-child{color:#fff;background:#34495E}.daterangepicker.picker_4 .calendar-table td,.daterangepicker.picker_4 .calendar-table td.off{background:#ECF0F1;border:1px solid #fff;border-radius:0}.daterangepicker.picker_4 .calendar-table td.active{background:#34495E}.calendar-exibit .show-calendar{float:none;display:block;position:relative;background-color:#fff;border:1px solid #ccc;margin-bottom:20px;border:1px solid rgba(0,0,0,.15);overflow:hidden}.calendar-exibit .show-calendar .calendar{margin:0 0 4px}.calendar-exibit .show-calendar.picker_1{background:#34495E}.calendar-exibit .calendar-table{padding:0 0 4px}.left_col{background:#2A3F54}.nav-sm .container.body .col-md-3.left_col{min-height:100%;width:70px;padding:0;z-index:9999;position:absolute}.nav-sm .container.body .col-md-3.left_col.menu_fixed{position:fixed;height:100%}.nav-sm .container.body .col-md-3.left_col .mCSB_container,.nav-sm .container.body .col-md-3.left_col .mCustomScrollBox{overflow:visible}.overflow_hidden,.sidebar-widget,.site_title,.tile,.weather-days .col-sm-2,.x_title h2,table.tile_info td p{overflow:hidden}.nav-sm .hidden-small{visibility:hidden}.nav-sm .container.body .right_col{padding:10px 20px;margin-left:70px;z-index:2}.nav-sm .navbar.nav_title{width:70px}.nav-sm .navbar.nav_title a span{display:none}.nav-sm .navbar.nav_title a i{font-size:27px;margin:13px 0 0 3px}.site_title i{border:1px solid #EAEAEA;padding:5px 6px;border-radius:50%}.nav-sm .main_container .top_nav{display:block;margin-left:70px;z-index:2}.nav-sm .nav.side-menu li a{text-align:center!important;font-weight:400;font-size:10px;padding:10px 5px}.nav-sm .nav.child_menu li.active,.nav-sm .nav.side-menu li.active-sm{border-right:5px solid #1ABB9C}.nav-sm .nav.side-menu li.active-sm ul ul,.nav-sm ul.nav.child_menu ul{position:static;width:200px;background:0 0}.nav-sm>.nav.side-menu>li.active-sm>a{color:#1ABB9C!important}.nav-sm .nav.side-menu li a i.toggle-up{display:none!important}.nav-sm .menu_section h3,.nav-sm .profile,.nav-sm .menu_section span.fa{display:none}.nav-sm .nav.side-menu li a i{font-size:25px!important;text-align:center;width:100%!important;margin-bottom:5px}.nav-sm ul.nav.child_menu{left:100%;position:absolute;top:0;width:210px;z-index:4000;background:#3E5367;display:none}.nav-sm ul.nav.child_menu li{padding:0 10px}.nav-sm ul.nav.child_menu li a{text-align:left!important}.menu_section{margin-bottom:35px}.menu_section h3{padding-left:15px;color:#fff;text-transform:uppercase;letter-spacing:.5px;font-weight:700;font-size:11px;margin-bottom:0;margin-top:0;text-shadow:1px 1px #000}.menu_section>ul{margin-top:10px}.profile_pic{width:35%;float:left}.img-circle.profile_img{width:70%;background:#fff;margin-left:15%;z-index:1000;position:inherit;margin-top:20px;border:1px solid rgba(52,73,94,.44);padding:4px}.profile_info{padding:25px 10px 10px;width:65%;float:left}.profile_info span{font-size:13px;line-height:30px;color:#BAB8B8}.profile_info h2{font-size:14px;color:#ECF0F1;margin:0;font-weight:300}.profile.img_2{text-align:center}.profile.img_2 .profile_pic{width:100%}.profile.img_2 .profile_pic .img-circle.profile_img{width:50%;margin:10px 0 0}.profile.img_2 .profile_info{padding:15px 10px 0;width:100%;margin-bottom:10px;float:left}.main_menu span.fa{float:right;text-align:center;margin-top:5px;font-size:10px;min-width:inherit;color:#C4CFDA}.active a span.fa{text-align:right!important;margin-right:4px}.nav-sm .menu_section{margin:0}.nav-sm li li span.fa{display:inline-block}.nav_menu{float:left;background:#EDEDED;border-bottom:1px solid #D9DEE4;margin-bottom:10px;width:100%;position:relative}@media (min-width:480px){.nav_menu{position:static}}.nav-md .container.body .col-md-3.left_col{min-height:100%;width:230px;padding:0;position:absolute;display:-ms-flexbox;display:flex;z-index:1}.nav-md .container.body .col-md-3.left_col.menu_fixed{height:100%;position:fixed}body .container.body .right_col{background:#F7F7F7}.nav-md .container.body .right_col{padding:10px 20px 0;margin-left:230px}.nav_title{width:230px;float:left;background:#2A3F54;border-radius:0;height:57px}@media (max-width:991px){.nav-md .container.body .right_col,.nav-md .container.body .top_nav{width:100%;margin:0}.nav-md .container.body .col-md-3.left_col{display:none}.nav-md .container.body .right_col{width:100%;padding-right:0}.right_col{padding:10px!important}}@media (max-width:1200px){.x_title h2{width:62%;font-size:17px}.graph,.tile{zoom:85%;height:inherit}}@media (max-width:1270px) and (min-width:192px){.x_title h2 small{display:none}}.left_col .mCSB_scrollTools{width:6px}.left_col .mCSB_dragger{max-height:400px!important}.blue{color:#3498DB}.purple{color:#9B59B6}.green{color:#1ABB9C}.aero{color:#9CC2CB}.red{color:#E74C3C}.dark{color:#34495E}.border-blue{border-color:#3498DB!important}.border-purple{border-color:#9B59B6!important}.border-green{border-color:#1ABB9C!important}.border-aero{border-color:#9CC2CB!important}.border-red{border-color:#E74C3C!important}.border-dark{border-color:#34495E!important}.bg-white{background:#fff!important;border:1px solid #fff!important;color:#73879C}.bg-green{background:#1ABB9C!important;border:1px solid #1ABB9C!important;color:#fff}.bg-red{background:#E74C3C!important;border:1px solid #E74C3C!important;color:#fff}.bg-blue{background:#3498DB!important;border:1px solid #3498DB!important;color:#fff}.bg-orange{background:#F39C12!important;border:1px solid #F39C12!important;color:#fff}.bg-purple{background:#9B59B6!important;border:1px solid #9B59B6!important;color:#fff}.bg-blue-sky{background:#50C1CF!important;border:1px solid #50C1CF!important;color:#fff}.container{width:100%;padding:0}.top_nav .nav .open>a,.top_nav .nav .open>a:focus,.top_nav .nav .open>a:hover,.top_nav .nav>li>a:focus,.top_nav .nav>li>a:hover{background:#D9DEE4}body{color:#73879C;background:#2A3F54;font-family:\"Helvetica Neue\",Roboto,Arial,\"Droid Sans\",sans-serif;font-size:13px;font-weight:400;line-height:1.471}.main_container .top_nav{display:block;margin-left:230px}.no-padding{padding:0!important}.page-title{width:100%;height:65px;padding:10px 0}.page-title .title_left{width:45%;float:left;display:block}.page-title .title_left h3{margin:9px 0}.page-title .title_right{width:55%;float:left;display:block}.page-title .title_right .pull-right{margin:10px 0}.fixed_height_320{height:320px}.fixed_height_390{height:390px}.fixed_height_200{height:200px}.progress-bar-dark{background-color:#34495E!important}.progress-bar-gray{background-color:#BDC3C7!important}table.no-margin .progress{margin-bottom:0}.main_content{padding:10px 20px}.col-md-55{width:50%;margin-bottom:10px}@media (min-width:768px){.col-md-55{width:20%}}@media (min-width:992px){.col-md-55{width:20%}}@media (min-width:1200px){.col-md-55{width:20%}}@media (min-width:192px) and (max-width:1270px){table.tile_info span.right{margin-right:7px;float:left}}.center-margin{margin:0 auto;float:none!important}.col-lg-1,.col-lg-10,.col-lg-11,.col-lg-12,.col-lg-2,.col-lg-3,.col-lg-4,.col-lg-5,.col-lg-6,.col-lg-7,.col-lg-8,.col-lg-9,.col-md-1,.col-md-10,.col-md-11,.col-md-12,.col-md-2,.col-md-3,.col-md-4,.col-md-5,.col-md-55,.col-md-6,.col-md-7,.col-md-8,.col-md-9,.col-sm-1,.col-sm-10,.col-sm-11,.col-sm-12,.col-sm-2,.col-sm-3,.col-sm-4,.col-sm-5,.col-sm-6,.col-sm-7,.col-sm-8,.col-sm-9,.col-xs-1,.col-xs-10,.col-xs-11,.col-xs-12,.col-xs-2,.col-xs-3,.col-xs-4,.col-xs-5,.col-xs-6,.col-xs-7,.col-xs-8,.col-xs-9{position:relative;min-height:1px;float:left;padding-right:10px;padding-left:10px}.row{margin-right:-10px;margin-left:-10px}.grid_slider .col-md-6{padding:0 40px}.h1,.h2,.h3,h1,h2,h3{margin-top:10px;margin-bottom:10px}a{color:#5A738E}.btn.active.focus,.btn.active:focus,.btn.focus,.btn:active.focus,.btn:active:focus,.btn:focus,:active,:focus,:visited,a,a:active,a:focus,a:visited{outline:0}.navbar{margin-bottom:0}.navbar-header{background:#34495E}.navbar-right{margin-right:0}.top_nav .navbar-right{margin:0;width:70%;float:right}.top_nav .navbar-right li{display:inline-block;float:right;position:static}@media (min-width:480px){.top_nav .navbar-right li{position:relative}}.top_nav .dropdown-menu li{width:100%}.top_nav .dropdown-menu li a{width:100%;padding:12px 20px}.top_nav li a i{font-size:15px}.navbar-static-top{position:fixed;top:0;width:100%}.sidebar-header{border-bottom:0;margin-top:46px}.sidebar-header:first-of-type{margin-top:0}.nav.side-menu>li{position:relative;display:block;cursor:pointer}.nav.side-menu>li>a{margin-bottom:6px}.nav.side-menu>li>a:hover{color:#F2F5F7!important}.nav.side-menu>li>a:hover,.nav>li>a:focus{text-decoration:none;background:0 0}.nav.child_menu{display:none}.nav.child_menu li.active,.nav.child_menu li:hover{background-color:rgba(255,255,255,.06)}.nav.child_menu li{padding-left:36px}.nav-md ul.nav.child_menu li:before{background:#425668;bottom:auto;content:\"\";height:8px;left:23px;margin-top:15px;position:absolute;right:auto;width:8px;z-index:1;border-radius:50%}.nav-md ul.nav.child_menu li:after{border-left:1px solid #425668;bottom:0;content:\"\";left:27px;position:absolute;top:0}.nav.top_menu>li>a,.nav>li>a{position:relative;display:block}.nav.child_menu>li>a,.nav.side-menu>li>a{color:#E7E7E7;font-weight:500}.nav li li.current-page a,.nav.child_menu li li a.active,.nav.child_menu li li a:hover{color:#fff}.nav.child_menu li li.active,.nav.child_menu li li:hover{background:0 0}.nav>li>a{padding:13px 15px 12px}.nav.side-menu>li.active,.nav.side-menu>li.current-page{border-right:5px solid #1ABB9C}.nav li.current-page{background:rgba(255,255,255,.05)}.nav li li li.current-page{background:0 0}.navbar-brand,.navbar-nav>li>a,.site_title{color:#ECF0F1!important;margin-left:0!important}.nav.side-menu>li.active>a{text-shadow:rgba(0,0,0,.25) 0 -1px 0;background:linear-gradient(#334556,#2C4257),#2A3F54;box-shadow:rgba(0,0,0,.25) 0 1px 0,inset rgba(255,255,255,.16) 0 1px 0}.navbar-brand,.navbar-nav>li>a{font-weight:500;line-height:32px}.site_title{font-weight:400;font-size:22px;width:100%;line-height:59px;display:block;height:55px;margin:0;padding-left:10px}.nav.navbar-nav>li>a{color:#515356!important}.nav.top_menu>li>a{padding:10px 15px;color:#34495E!important}.nav>li>a:focus,.nav>li>a:hover{background-color:transparent}.top_search{padding:0}.top_search .form-control{box-shadow:inset 0 1px 0 rgba(0,0,0,.075);border-radius:25px 0 0 25px;padding-left:20px;border:1px solid rgba(221,226,232,.49)}.top_search .form-control:focus{border:1px solid rgba(221,226,232,.49);border-right:0}.top_search .input-group-btn button{border-radius:0 25px 25px 0;border:1px solid rgba(221,226,232,.49);border-left:0;box-shadow:inset 0 1px 1px rgba(0,0,0,.075);color:#93A2B2;margin-bottom:0!important}.tiles,.top_tiles{margin-bottom:0}.toggle{float:left;margin:0;padding-top:16px;width:70px}.toggle a{padding:15px 15px 0;margin:0;cursor:pointer}.toggle a i{font-size:26px}.nav.child_menu>li>a{color:rgba(255,255,255,.75);font-size:12px;padding:9px}.panel_toolbox{float:right;min-width:70px}.panel_toolbox>li{float:left;cursor:pointer}.panel_toolbox>li>a{padding:5px;color:#C5C7CB;font-size:14px}.panel_toolbox>li>a:hover{background:#F5F7FA}.line_30{line-height:30px}.main_menu_side{padding:0}.bs-docs-sidebar .nav>li>a{display:block;padding:4px 6px}footer{background:#fff;padding:15px 20px;display:block}.nav-sm footer{margin-left:70px}.footer_fixed footer{position:fixed;left:0;bottom:0;width:100%}.degrees:after,.x_content,.x_panel{position:relative}@media (min-width:768px){.footer_fixed .nav-sm footer,.footer_fixed footer{margin-left:0}}.tile-stats.sparkline{padding:10px;text-align:center}.jqstooltip{background:#34495E!important;width:30px!important;height:22px!important}.tooltip{display:block!important}.tiles{border-top:1px solid #ccc;margin-top:15px;padding-top:5px}.top_tiles .tile h2{font-size:30px;line-height:30px;margin:3px 0 7px;font-weight:700}article.media{width:100%}*,:after,:before{box-sizing:border-box}#integration-list{width:100%;margin:0 auto;display:table}#integration-list ul{padding:0;margin:20px 0;color:#555}#integration-list ul>li{list-style:none;border-top:1px solid #ddd;display:block;padding:15px;overflow:hidden}#integration-list ul:last-child{border-bottom:1px solid #ddd}#integration-list ul>li:hover{background:#efefef}.expand{display:block;color:#555;cursor:pointer}.expand h2{width:85%;float:left}h2{font-size:18px;font-weight:400}#left,#right{display:table}#sup{display:table-cell;vertical-align:middle;width:80%}.detail a{color:#C0392B;border:1px solid #C0392B;padding:6px 10px 5px;font-size:13px;margin-right:7px}.detail{margin:10px 0;display:none;line-height:22px;height:150px}.detail span{margin:0}.right-arrow{width:10px;float:right;font-weight:700;font-size:20px}.accordion .panel{margin-bottom:5px;border-radius:0;border-bottom:1px solid #efefef}.x_panel,.x_title{margin-bottom:10px}.accordion .panel-heading{background:#F2F5F7;padding:13px;width:100%;display:block}.accordion .panel:hover{background:#F2F5F7}.x_panel{width:100%;padding:10px 17px;display:inline-block;background:#fff;border:1px solid #E6E9ED;-webkit-column-break-inside:avoid;-moz-column-break-inside:avoid;column-break-inside:avoid;opacity:1;transition:all .2s ease}.x_title{border-bottom:2px solid #E6E9ED;padding:1px 5px 6px}.x_title .filter{width:40%;float:right}.x_content,table.tile td ul li a,table.tile_info{width:100%}.x_title h2{margin:5px 0 6px;float:left;display:block}.x_title h2 small{margin-left:10px}.x_title span{color:#BDBDBD}.x_content{padding:0 5px 6px;float:left;clear:both;margin-top:5px}.x_content h4{font-size:16px;font-weight:500}legend{padding-bottom:7px}.demo-placeholder{height:280px}.profile_details:nth-child(3n){clear:both}.profile_details .profile_view{display:inline-block;padding:10px 0 0;background:#fff}.profile_details .profile_view .divider{border-top:1px solid #e5e5e5;padding-top:5px;margin-top:5px}.profile_details .profile_view .ratings{margin-bottom:0;text-align:left;font-size:16px}.profile_details .profile_view .bottom{background:#F2F5F7;padding:9px 0;border-top:1px solid #E6E9ED}.profile_details .profile_view .left{margin-top:20px}.profile_details .profile_view .left p{margin-bottom:3px}.profile_details .profile_view .right{margin-top:0;padding:10px}.profile_details .profile_view .img-circle{border:1px solid #E6E9ED;padding:2px}.profile_details .profile_view h2{margin:5px 0}.profile_details .profile_view .brief{margin:0;font-weight:300}.profile_details .profile_left{background:#fff}.pagination.pagination-split li{display:inline-block;margin-right:3px}.pagination.pagination-split li a{border-radius:4px;color:#768399;-moz-border-radius:4px;-webkit-border-radius:4px}table.tile h3,table.tile h4,table.tile span{font-weight:700;vertical-align:middle!important}table.tile td,table.tile th{text-align:center}table.tile th{border-bottom:1px solid #E6ECEE}table.tile td{padding:5px 0}table.tile td ul{text-align:left;padding-left:0}table.tile td ul li{list-style:none;width:100%}table.tile td ul li a big{right:0;float:right;margin-right:13px}table.tile_info td{text-align:left;padding:1px;font-size:15px}table.tile_info td p{margin:0;line-height:28px}table.tile_info td i{margin-right:8px;font-size:17px;float:left;width:18px;line-height:28px}table.tile_info td:first-child{width:83%}td span{line-height:28px}.error-number{font-size:90px;line-height:90px;margin:20px 0}.col-middle{margin-top:5%}.mid_center{width:370px;margin:0 auto;text-align:center;padding:10px 20px}h3.degrees{font-size:22px;font-weight:400;text-align:center}.degrees:after{content:\"o\";top:-12px;font-size:13px;font-weight:300}.daily-weather .day{font-size:14px;border-top:2px solid rgba(115,135,156,.36);text-align:center;border-bottom:2px solid rgba(115,135,156,.36);padding:5px 0}.weather-days .col-sm-2{width:16.66666667%}.weather .row{margin-bottom:0}.bulk-actions{display:none}table.countries_list{width:100%}table.countries_list td{padding:0 10px;line-height:30px;border-top:1px solid #eee}.dataTables_paginate a{padding:6px 9px!important;background:#ddd!important;border-color:#ddd!important}.paging_full_numbers a.paginate_active{background-color:rgba(38,185,154,.59)!important;border-color:rgba(38,185,154,.59)!important}a.DTTT_button,button.DTTT_button,div.DTTT_button{border:1px solid #E7E7E7!important;background:#E7E7E7!important;box-shadow:none!important}table.jambo_table{border:1px solid rgba(221,221,221,.78)}table.jambo_table thead{background:rgba(52,73,94,.94);color:#ECF0F1}table.jambo_table tbody tr:hover td{background:rgba(38,185,154,.07);border-top:1px solid rgba(38,185,154,.11);border-bottom:1px solid rgba(38,185,154,.11)}table.jambo_table tbody tr.selected{background:rgba(38,185,154,.16)}table.jambo_table tbody tr.selected td{border-top:1px solid rgba(38,185,154,.4);border-bottom:1px solid rgba(38,185,154,.4)}.dataTables_wrapper{position:relative;clear:both;zoom:1}.dataTables_processing{position:absolute;top:50%;left:50%;width:250px;height:30px;margin-left:-125px;margin-top:-15px;padding:14px 0 2px;border:1px solid #ddd;text-align:center;color:#999;font-size:14px;background-color:#fff}td.details,td.group{background-color:#d1cfd0}.dataTables_length{width:40%;float:left}.dataTables_filter{width:50%;float:right;text-align:right}.dataTables_info{width:60%;float:left}.dataTables_paginate{float:right;text-align:right}.dataTables_empty,table.display td.center{text-align:center}table.dataTable td.focus,table.dataTable th.focus{outline:#1ABB9C solid 2px!important;outline-offset:-1px}.paging_full_numbers a:active,table.display thead td:active,table.display thead th:active{outline:0}table.display{margin:0 auto;clear:both;width:100%}table.display thead th{padding:8px 18px 8px 10px;border-bottom:1px solid #000;font-weight:700;cursor:pointer}table.display tfoot th{padding:3px 18px 3px 10px;border-top:1px solid #000;font-weight:700}table.display tr.heading2 td{border-bottom:1px solid #aaa}table.display td{padding:3px 10px}.dataTables_scroll{clear:both}.dataTables_scrollBody{-webkit-overflow-scrolling:touch}.top .dataTables_info{float:none}.clear{clear:both}tfoot input{margin:.5em 0;width:100%;color:#444}tfoot input.search_init{color:#999}td.group{border-bottom:2px solid #A19B9E;border-top:2px solid #A19B9E}td.details{border:2px solid #A19B9E}.example_alt_pagination div.dataTables_info{width:40%}.paging_full_numbers{width:400px;height:22px;line-height:22px}.paging_full_numbers a.paginate_active,.paging_full_numbers a.paginate_button{border:1px solid #aaa;-webkit-border-radius:5px;-moz-border-radius:5px;padding:2px 5px;margin:0 3px;cursor:pointer}.paging_full_numbers a.paginate_button{background-color:#ddd}.paging_full_numbers a.paginate_button:hover{background-color:#ccc;text-decoration:none!important}.login_content .btn-default:hover,.login_content a,.tagsinput span.tag a,.tile-stats>.dash-box-footer,.tile:hover,.view a.info,span.tag{text-decoration:none}table.display tr.even.row_selected td{background-color:#B0BED9}table.display tr.odd.row_selected td{background-color:#9FAFD1}div.box{height:100px;padding:10px;overflow:auto;border:1px solid #8080FF;background-color:#E5E5FF}ul.msg_list li{background:#f7f7f7;padding:5px;display:-ms-flexbox;display:flex;margin:6px 6px 0;width:96%!important}ul.msg_list li:last-child{margin-bottom:6px;padding:10px}ul.msg_list li a{padding:3px 5px!important}ul.msg_list li a .image img{border-radius:2px;-webkit-border-radius:2px;float:left;margin-right:10px;width:11%}ul.msg_list li a .time{font-size:11px;font-style:italic;font-weight:700;position:absolute;right:35px}ul.msg_list li a .message{display:block!important;font-size:11px}.dropdown-menu.msg_list span{white-space:normal}.tile_count .tile_stats_count,ul.quick-list li{white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.dropdown-menu{box-shadow:none;display:none;float:left;font-size:12px;left:0;list-style:none;padding:0;position:absolute;text-shadow:none;top:100%;z-index:9998;border:1px solid #D9DEE4;border-top-left-radius:0;border-top-right-radius:0}.dropdown-menu>li>a{color:#5A738E}.navbar-nav .open .dropdown-menu{position:absolute;background:#fff;margin-top:0;border:1px solid #D9DEE4;-webkit-box-shadow:none;right:0;left:auto;width:220px}.navbar-nav .open .dropdown-menu.msg_list{width:300px}.info-number .badge{font-size:10px;font-weight:400;line-height:13px;padding:2px 6px;position:absolute;right:2px;top:8px}ul.to_do{padding:0}ul.to_do li{background:#f3f3f3;border-radius:3px;position:relative;padding:7px;margin-bottom:5px;list-style:none}ul.to_do p{margin:0}.dashboard-widget{background:#f6f6f6;border-top:5px solid #79C3DF;border-radius:3px;padding:5px 10px 10px}.dashboard-widget .dashboard-widget-title{font-weight:400;border-bottom:1px solid #c1cdcd;margin:0 0 10px;padding-bottom:5px;padding-left:40px;line-height:30px}.dashboard-widget .dashboard-widget-title i{font-size:100%;margin-left:-35px;margin-right:10px;color:#33a1c9;padding:3px 6px;border:1px solid #abd9ea;border-radius:5px;background:#fff}ul.quick-list{width:45%;padding-left:0;display:inline-block}ul.quick-list li{padding-left:10px;list-style:none;margin:0;padding-bottom:6px;padding-top:4px}ul.quick-list li i{padding-right:10px;color:#757679}.dashboard-widget-content{padding-top:9px}.dashboard-widget-content .sidebar-widget{width:50%;display:inline-block;vertical-align:top;background:#fff;border:1px solid #abd9ea;border-radius:5px;text-align:center;float:right;padding:2px;margin-top:10px}.widget_summary{width:100%;display:-ms-inline-flexbox;display:inline-flex}.widget_summary .w_left{float:left;text-align:left}.widget_summary .w_center{float:left}.widget_summary .w_right{float:left;text-align:right}.widget_summary .w_right span{font-size:20px}.w_20{width:20%}.w_25{width:25%}.w_55{width:55%}h5.graph_title{text-align:left;margin-left:10px}h5.graph_title i{margin-right:10px;font-size:17px}span.right{float:right;font-size:14px!important}.tile_info a{text-overflow:ellipsis}.sidebar-footer{bottom:0;clear:both;display:block;padding:5px 0 0;position:fixed;width:230px;background:#2A3F54;z-index:999}.sidebar-footer a{padding:7px 0 3px;text-align:center;width:25%;font-size:17px;display:block;float:left;background:#172D44;cursor:pointer}.sidebar-footer a:hover{background:#425567}.tile_count{margin-bottom:20px;margin-top:20px}.tile_count .tile_stats_count{border-bottom:1px solid #D9DEE4;padding:0 10px 0 20px;position:relative}.tile_count .tile_stats_count:before{content:\"\";position:absolute;left:0;height:65px;border-left:2px solid #ADB2B5;margin-top:10px}@media (min-width:992px){footer{margin-left:230px}.tile_count .tile_stats_count{margin-bottom:10px;border-bottom:0;padding-bottom:10px}.tile_count .tile_stats_count:first-child:before{border-left:0}}.tile_count .tile_stats_count .count{font-size:30px;line-height:47px;font-weight:600}@media (min-width:768px){.tile_count .tile_stats_count .count{font-size:25px}}@media (min-width:992px) and (max-width:1100px){.tile_count .tile_stats_count .count{font-size:30px}}.tile_count .tile_stats_count span{font-size:12px}@media (min-width:768px){.tile_count .tile_stats_count span{font-size:13px}}.tile_count .tile_stats_count .count_bottom i{width:12px}.dashboard_graph{background:#fff;padding:7px 10px}.dashboard_graph .col-md-3,.dashboard_graph .col-md-9{padding:0}a.user-profile{color:#5E6974!important}.user-profile img{width:29px;height:29px;border-radius:50%;margin-right:10px}ul.top_profiles{height:330px;width:100%}ul.top_profiles li{margin:0;padding:3px 5px}ul.top_profiles li:nth-child(odd){background-color:#eee}.media .profile_thumb{border:1px solid;width:50px;height:50px;margin:5px 10px 5px 0;border-radius:50%;padding:9px 12px}.media .profile_thumb i{font-size:30px}.media .date{background:#ccc;width:52px;margin-right:10px;border-radius:10px;padding:5px}.media .date .day,.media .date .month{margin:0;text-align:center;color:#fff}.media .date .day{font-size:27px;line-height:27px;font-weight:700}.event .media-body a.title{font-weight:700}.event .media-body p{margin-bottom:0}h4.graph_title{margin:7px;text-align:center}.fontawesome-icon-list .fa-hover a:hover{background-color:#ddd;color:#fff;text-decoration:none}.fontawesome-icon-list .fa-hover a{display:block;line-height:32px;height:32px;padding-left:10px;border-radius:4px}.fontawesome-icon-list .fa-hover a:hover .fa{font-size:28px;vertical-align:-6px}.fontawesome-icon-list .fa-hover a .fa{width:32px;font-size:16px;display:inline-block;text-align:right;margin-right:10px}.main_menu .fa{width:26px;opacity:.99;display:inline-block;font-family:FontAwesome;font-style:normal;font-weight:400;font-size:18px;-moz-osx-font-smoothing:grayscale}.tile-stats{position:relative;display:block;margin-bottom:12px;border:1px solid #E4E4E4;-webkit-border-radius:5px;overflow:hidden;padding-bottom:5px;-webkit-background-clip:padding-box;-moz-border-radius:5px;-moz-background-clip:padding;border-radius:5px;background:#FFF;transition:all .3s ease-in-out}.tile-stats:hover .icon i{animation-name:transformAnimation;animation-duration:.5s;animation-iteration-count:1;color:rgba(58,58,58,.41);animation-timing-function:ease;animation-fill-mode:forwards;-webkit-animation-name:transformAnimation;-webkit-animation-duration:.5s;-webkit-animation-iteration-count:1;-webkit-animation-timing-function:ease;-webkit-animation-fill-mode:forwards;-moz-animation-name:transformAnimation;-moz-animation-duration:.5s;-moz-animation-iteration-count:1;-moz-animation-timing-function:ease;-moz-animation-fill-mode:forwards}.tile-stats .icon{width:20px;height:20px;color:#BAB8B8;position:absolute;right:53px;top:22px;z-index:1}.tile-stats .icon i{margin:0;font-size:60px;line-height:0;vertical-align:bottom;padding:0}.tile-stats .count{font-size:38px;font-weight:700;line-height:1.65857}.tile-stats .count,.tile-stats h3,.tile-stats p{position:relative;margin:0 0 0 10px;z-index:5;padding:0}.tile-stats h3{color:#BAB8B8}.tile-stats p{margin-top:5px;font-size:12px}.tile-stats>.dash-box-footer{position:relative;text-align:center;margin-top:5px;padding:3px 0;color:#fff;color:rgba(255,255,255,.8);display:block;z-index:10;background:rgba(0,0,0,.1)}.tile-stats>.dash-box-footer:hover{color:#fff;background:rgba(0,0,0,.15)}table.tile_info{padding:10px 15px}table.tile_info span.right{margin-right:0;float:right;position:absolute;right:4%}.tile_header{border-bottom:transparent;padding:7px 15px;margin-bottom:15px;background:#E7E7E7}.tile_head h4{margin-top:0;margin-bottom:5px}.tiles-bottom{padding:5px 10px;margin-top:10px;background:rgba(194,194,194,.3);text-align:left}a.star{color:#428bca!important}.mail_content{background:#FFF;border-radius:4px;margin-top:20px;min-height:500px;padding:10px 11px;width:100%}.list-btn-mail{margin-bottom:15px}.list-btn-mail.active{border-bottom:1px solid #39B3D7;padding:0 0 14px}.list-btn-mail>i{float:left;font-size:18px;font-style:normal;width:33px}.list-btn-mail>.cn{background:#39B3D7;border-radius:12px;color:#FFF;float:right;font-style:normal;padding:0 5px}.byline,.view p{font-style:italic}.button-mail{margin:0 0 15px!important;text-align:left;width:100%}.btn,.buttons,.modal-footer .btn+.btn,button{margin-bottom:5px;margin-right:5px}.btn-group .btn,.btn-group-vertical .btn{margin-bottom:0;margin-right:0}.mail_list_column,.mail_view{border-left:1px solid #DBDBDB}.mail_list{width:100%;border-bottom:1px solid #DBDBDB;margin-bottom:2px;display:inline-block}.mail_list .left{width:5%;float:left;margin-right:3%}.mail_list .right{width:90%;float:left}.mail_list h3{font-size:15px;font-weight:700;margin:0 0 6px}.mail_list h3 small{float:right;color:#ADABAB;font-size:11px;line-height:20px}.mail_list .badge{padding:3px 6px;font-size:8px;background:#BAB7B7}@media (max-width:767px){.mail_list{margin-bottom:5px;display:inline-block}}.mail_heading h4{font-size:18px;border-bottom:1px solid #ddd;padding-bottom:10px;margin-top:20px}.attachment{margin-top:30px}.attachment ul{width:100%;list-style:none;padding-left:0;display:inline-block;margin-bottom:30px}.attachment ul li{float:left;width:150px;margin-right:10px;margin-bottom:10px}.attachment ul li img{height:150px;border:1px solid #ddd;padding:5px;margin-bottom:10px}.attachment ul li span{float:right}.attachment .file-name{float:left}.attachment .links{width:100%;display:inline-block}.compose{padding:0;position:fixed;bottom:0;right:0;background:#fff;border:1px solid #D9DEE4;border-right:0;border-bottom:0;border-top-left-radius:5px;z-index:9999;display:none}.compose .compose-header{padding:5px;background:#169F85;color:#fff;border-top-left-radius:5px}.compose .compose-header .close{text-shadow:0 1px 0 #fff;line-height:.8}.compose .compose-body .editor.btn-toolbar{margin:0}.compose .compose-body .editor-wrapper{height:100%;min-height:50px;max-height:180px;border-radius:0;border-left:none;border-right:none;overflow:auto}.compose .compose-footer{padding:10px}.editor.btn-toolbar{zoom:1;background:#F7F7F7;margin:5px 2px;padding:3px 0;border:1px solid #EFEFEF}.input-group{margin-bottom:10px}.ln_solid{border-top:1px solid #e5e5e5;color:#fff;background-color:#fff;height:1px;margin:20px 0}span.section{display:block;width:100%;padding:0;margin-bottom:20px;font-size:21px;line-height:inherit;color:#333;border:0;border-bottom:1px solid #e5e5e5}.form-control{border-radius:0;width:100%}.form-horizontal .control-label{padding-top:8px}.form-control:focus{border-color:#CCD0D7;box-shadow:none!important}legend{font-size:18px;color:inherit}.form-horizontal .form-group{margin-right:0;margin-left:0}.form-control-feedback{margin-top:8px;height:23px;color:#bbb;line-height:24px;font-size:15px}.form-control-feedback.left{border-right:1px solid #ccc;left:13px}.form-control-feedback.right{border-left:1px solid #ccc;right:13px}.form-control.has-feedback-left{padding-left:45px}.form-control.has-feedback-right{padding-right:45px}.form-group{margin-bottom:10px}.validate{margin-top:10px}.invalid-form-error-message{margin-top:10px;padding:5px}.invalid-form-error-message.filled{border-left:2px solid #E74C3C}p.parsley-success{color:#468847;background-color:#DFF0D8;border:1px solid #D6E9C6}p.parsley-error{color:#B94A48;background-color:#F2DEDE;border:1px solid #EED3D7}ul.parsley-errors-list{list-style:none;color:#E74C3C;padding-left:0}input.parsley-error,select.parsley-error,textarea.parsley-error{background:#FAEDEC;border:1px solid #E85445}.btn-group .parsley-errors-list{display:none}.bad input,.bad select,.bad textarea{border:1px solid #CE5454;box-shadow:0 0 4px -2px #CE5454;position:relative;left:0;-moz-animation:.7s 1 shake linear;-webkit-animation:.7s 1 shake linear}.item input,.item textarea{transition:.42s}.item .alert{float:left;margin:0 0 0 20px;padding:3px 10px;color:#FFF;border-radius:3px 4px 4px 3px;background-color:#CE5454;max-width:170px;white-space:pre;position:relative;left:-15px;opacity:0;z-index:1;transition:.15s ease-out}.item .alert::after{content:'';display:block;height:0;width:0;border-color:transparent #CE5454 transparent transparent;border-style:solid;border-width:11px 7px;position:absolute;left:-13px;top:1px}.item.bad .alert{left:0;opacity:1}.inl-bl{display:inline-block}.well .markup{background:#fff;color:#777;position:relative;padding:45px 15px 15px;margin:15px 0 0;border-radius:0 0 4px 4px;box-shadow:none}.well .markup::after{content:\"Example\";position:absolute;top:15px;left:15px;font-size:12px;font-weight:700;color:#bbb;text-transform:uppercase;letter-spacing:1px}.autocomplete-suggestions{border:1px solid #e4e4e4;background:#F4F4F4;cursor:default;overflow:auto}.autocomplete-suggestion{padding:2px 5px;font-size:1.2em;white-space:nowrap;overflow:hidden}.autocomplete-selected{background:#f0f0f0}.autocomplete-suggestions strong{color:#39f;font-weight:bolder}.btn{border-radius:3px}a.btn-danger,a.btn-primary,a.btn-success,a.btn-warning{color:#fff}.btn-success{background:#26B99A;border:1px solid #169F85}.btn-success.active,.btn-success:active,.btn-success:focus,.btn-success:hover,.open .dropdown-toggle.btn-success{background:#169F85}.btn-dark{color:#E9EDEF;background-color:#4B5F71;border-color:#364B5F}.btn-dark.active,.btn-dark:active,.btn-dark:focus,.btn-dark:hover,.open .dropdown-toggle.btn-dark{color:#FFF;background-color:#394D5F;border-color:#394D5F}.btn-round{border-radius:30px}.btn.btn-app{position:relative;padding:15px 5px;margin:0 0 10px 10px;min-width:80px;height:60px;box-shadow:none;border-radius:0;text-align:center;color:#666;border:1px solid #ddd;background-color:#fafafa;font-size:12px}.btn.btn-app>.fa,.btn.btn-app>.glyphicon,.btn.btn-app>.ion{font-size:20px;display:block}.btn.btn-app:hover{background:#f4f4f4;color:#444;border-color:#aaa}.btn.btn-app:active,.btn.btn-app:focus{box-shadow:inset 0 3px 5px rgba(0,0,0,.125)}.btn.btn-app>.badge{position:absolute;top:-3px;right:-10px;font-size:10px;font-weight:400}textarea{padding:10px;vertical-align:top;width:200px}textarea:focus{outline-style:solid;outline-width:2px}.btn_{display:inline-block;padding:3px 9px;margin-bottom:0;font-size:14px;line-height:20px;text-align:center;vertical-align:middle;cursor:pointer;color:#333;text-shadow:0 1px 1px rgba(255,255,255,.75);background-color:#f5f5f5;background-image:linear-gradient(to bottom,#fff,#e6e6e6);background-repeat:repeat-x;filter:progid: DXImageTransform.Microsoft.gradient(enabled=false);border:1px solid #ccc;border-bottom-color:#b3b3b3;border-radius:4px;box-shadow:inset 0 1px 0 rgba(255,255,255,.2),0 1px 2px rgba(0,0,0,.05)}.bs-glyphicons{margin:0 -10px 20px;overflow:hidden}.bs-glyphicons-list{padding-left:0;list-style:none}.bs-glyphicons li{float:left;width:25%;height:115px;padding:10px;font-size:10px;line-height:1.4;text-align:center;background-color:#f9f9f9;border:1px solid #fff}.bs-glyphicons .glyphicon{margin-top:5px;margin-bottom:10px;font-size:24px}.bs-glyphicons .glyphicon-class{display:block;text-align:center;word-wrap:break-word}.bs-glyphicons li:hover{color:#fff;background-color:#1ABB9C}@media (min-width:768px){.bs-glyphicons{margin-right:0;margin-left:0}.bs-glyphicons li{width:12.5%;font-size:12px}}.tagsinput{border:1px solid #CCC;background:#FFF;padding:6px 6px 0;width:300px;overflow-y:auto}span.tag{-moz-border-radius:2px;-webkit-border-radius:2px;display:block;float:left;padding:5px 9px;background:#1ABB9C;color:#F1F6F7;margin-right:5px;font-weight:500;margin-bottom:5px;font-family:helvetica}span.tag a{color:#F1F6F7!important}.tagsinput span.tag a{font-weight:700;color:#82ad2b;font-size:11px}.tagsinput input{width:80px;margin:0;font-family:helvetica;font-size:13px;border:1px solid transparent;padding:3px;background:0 0;color:#000;outline:0}.tagsinput div{display:block;float:left}.social-body,.social-sidebar,ul.bar_tabs.right li{float:right}.tags_clear{clear:both;width:100%;height:0}.not_valid{background:#FBD8DB!important;color:#90111A!important}ul.bar_tabs{overflow:visible;background:#F5F7FA;height:25px;margin:21px 0 14px;padding-left:14px;position:relative;z-index:1;width:100%;border-bottom:1px solid #E6E9ED}ul.bar_tabs>li{border:1px solid #E6E9ED;color:#333!important;margin-top:-17px;margin-left:8px;background:#fff;border-bottom:none;border-radius:4px 4px 0 0}ul.bar_tabs>li.active{border-right:6px solid #D3D6DA;border-top:0;margin-top:-15px}ul.bar_tabs>li a{padding:10px 17px;background:#F5F7FA;margin:0;border-top-right-radius:0}ul.bar_tabs>li a:hover{border:1px solid transparent}ul.bar_tabs>li.active a{border-bottom:none}ul.bar_tabs.right{padding-right:14px}a:focus{outline:0}ul.timeline li{position:relative;border-bottom:1px solid #e8e8e8;clear:both}.timeline .block{margin:0 0 0 105px;border-left:3px solid #e8e8e8;overflow:visible;padding:10px 15px}.timeline.widget{min-width:0;max-width:inherit}.timeline.widget .block{margin-left:5px}.timeline .tags{position:absolute;top:15px;left:0;width:84px}.timeline .tag{display:block;height:30px;font-size:13px;padding:8px}.timeline .tag span{display:block;overflow:hidden;width:100%;white-space:nowrap;text-overflow:ellipsis}.tag{line-height:1;background:#1ABB9C;color:#fff!important}.tag:after{content:\" \";height:30px;width:0;position:absolute;left:100%;top:0;margin:0;pointer-events:none;border-top:14px solid transparent;border-bottom:14px solid transparent;border-left:11px solid #1ABB9C}.timeline h2.title{position:relative;font-size:16px;margin:0}.timeline h2.title:before{content:\"\";position:absolute;left:-23px;top:3px;display:block;width:14px;height:14px;border:3px solid #d2d3d2;border-radius:14px;background:#f9f9f9}.timeline .byline{padding:.25em 0}.byline{font-size:.9375em;line-height:1.3;color:#aab6aa}ul.social li{border:0}.social-sidebar{background:#EDEDED;width:22%}.social-body{border:1px solid #ccc;width:78%}.thumb img{width:50px;height:50px;border-radius:50%}.chat .thumb img{width:27px;height:27px;border-radius:50%}.chat .status{float:left;margin:16px 0 0 -16px;font-size:14px;font-weight:700;width:12px;height:12px;display:block;border:2px solid #FFF;z-index:12312;border-radius:50%}.chart,.percent{display:inline-block}.chat .status.online{background:#1ABB9C}.chat .status.away{background:#F39C12}.chat .status.offline{background:#ccc}.chat .media-body{padding-top:5px}.dashboard_graph .x_title{padding:5px 5px 7px}.dashboard_graph .x_title h3{margin:0;font-weight:400}.chart{position:relative;width:110px;height:110px;margin-top:5px;margin-bottom:5px;text-align:center}.chart canvas{position:absolute;top:0;left:0}.percent{line-height:110px;z-index:2;font-size:18px}.percent:after{content:'%';margin-left:.1em;font-size:.8em}.angular{margin-top:100px}.angular .chart{margin-top:0}.widget{min-width:250px;max-width:310px}.widget_tally_box .btn-group button{text-align:center;color:inherit;font-weight:500;background-color:#f5f5f5;border:1px solid #e7e7e7}ul.widget_tally,ul.widget_tally li{width:100%}ul.widget_tally li{padding:2px 10px 4px;border-bottom:1px solid #ECECEC}ul.widget_tally .month{width:70%;float:left}ul.widget_tally .count{width:30%;float:left;text-align:right}.pie_bg{border-bottom:1px solid rgba(101,204,182,.16);border-radius:4px;filter:progid: DXImageTransform.Microsoft.gradient(startColorstr='#ffffffff', endColorstr='#ffe6e6e6', GradientType=0);filter:progid: DXImageTransform.Microsoft.gradient(enabled=false);padding-bottom:10px;box-shadow:0 4px 6px -6px #222}.widget_tally_box .flex{display:-ms-flexbox;display:flex}ul.widget_profile_box{width:100%;height:42px;padding:3px;background:#ececec;margin-top:40px;margin-left:1px}ul.widget_profile_box li:first-child{width:25%;float:left}ul.widget_profile_box li:first-child a{float:left}ul.widget_profile_box li:last-child{width:25%;float:right}ul.widget_profile_box li:last-child a{float:right}ul.widget_profile_box li a{font-size:22px;text-align:center;width:35px;height:35px;border:1px solid rgba(52,73,94,.44);display:block;border-radius:50%;padding:0}ul.widget_profile_box li a:hover{color:#1ABB9C!important;border:1px solid #26b99a}ul.widget_profile_box li .profile_img{width:85px;height:85px;margin:-28px 0 0}.widget_tally_box p,.widget_tally_box span{text-align:center}.widget_tally_box .name{text-align:center;margin:25px}.widget_tally_box .name_title{text-align:center;margin:5px}.widget_tally_box ul.legend{margin:0}.widget_tally_box ul.legend p,.widget_tally_box ul.legend span{text-align:left}.widget_tally_box ul.legend li .icon{font-size:20px;float:left;width:14px}.widget_tally_box ul.legend li .name{font-size:14px;margin:5px 0 0 14px;text-overflow:ellipsis;float:left}.widget_tally_box ul.legend p{display:inline-block;margin:0}.widget_tally_box ul.verticle_bars li{height:140px;width:23%}.widget .verticle_bars li .progress.vertical.progress_wide{width:65%}ul.count2{width:100%;margin-left:1px;border:1px solid #ddd;border-left:0;border-right:0;padding:10px 0}ul.count2 li{width:30%;text-align:center}ul.count2 li h3{font-weight:400;margin:0}ul.count2 li span{font-weight:300}.divider{border-bottom:1px solid #ddd;margin:10px}.divider-dashed{border-top:1px dashed #e7eaec;background-color:#fff;height:1px;margin:10px 0}ul.messages{padding:0;list-style:none}.tasks li,ul.messages li{border-bottom:1px dotted #e6e6e6;padding:8px 0}img.avatar,ul.messages li img.avatar{height:32px;width:32px;float:left;display:inline-block;border-radius:2px;padding:2px;background:#f7f7f7;border:1px solid #e6e6e6}ul.messages li .message_date{float:right;text-align:right}ul.messages li .message_wrapper{margin-left:50px;margin-right:40px}ul.messages li .message_wrapper h4.heading{font-weight:600;margin:0 0 10px;cursor:pointer;line-height:100%}ul.messages li .message_wrapper blockquote{padding:0 10px;margin:0;border-left:5px solid #eee}ul.user_data li{margin-bottom:6px}ul.user_data li p{margin-bottom:0}ul.user_data li .progress{width:90%}.project_progress .progress{margin-bottom:3px!important;margin-top:5px}.projects .list-inline{margin:0}.profile_title{background:#F5F7FA;border:0;padding:7px 0;display:-ms-flexbox;display:flex}ul.stats-overview{border-bottom:1px solid #e8e8e8;padding-bottom:10px;margin-bottom:10px}ul.stats-overview li{display:inline-block;text-align:center;padding:0 15px;width:30%;font-size:14px;border-right:1px solid #e8e8e8}ul.stats-overview li:last-child{border-right:0}ul.stats-overview li .name{font-size:12px}ul.stats-overview li .value{font-size:14px;font-weight:700;display:block}ul.stats-overview li:first-child{padding-left:0}ul.project_files li{margin-bottom:5px}ul.project_files li a i{width:20px}.project_detail p{margin-bottom:10px}.project_detail p.title{font-weight:700;margin-bottom:0}.avatar img{border-radius:50%;max-width:45px}.pricing{background:#fff}.pricing .title{background:#1ABB9C;height:110px;color:#fff;padding:15px 0 0;text-align:center}.pricing .title h2{text-transform:capitalize;font-size:18px;border-radius:5px 5px 0 0;margin:0;font-weight:400}.notifications a,.tabbed_notifications h2,.view .tools,.view a.info{text-transform:uppercase}.pricing .title h1{font-size:30px;margin:12px}.pricing .title span{background:rgba(51,51,51,.28);padding:2px 5px}.pricing_features{background:#FAFAFA;padding:20px 15px;min-height:230px;font-size:13.5px}.pricing_features ul li{margin-top:10px}.pricing_footer{padding:10px 15px;background-color:#f5f5f5;border-top:1px solid #ddd;text-align:center;border-bottom-right-radius:3px;border-bottom-left-radius:3px}.pricing_footer p{font-size:13px;padding:10px 0 2px;display:block}.ui-ribbon-container{position:relative}.ui-ribbon-container .ui-ribbon-wrapper{position:absolute;overflow:hidden;width:85px;height:88px;top:-3px;right:-3px}.ui-ribbon-container.ui-ribbon-primary .ui-ribbon{background-color:#5b90bf}.ui-ribbon-container .ui-ribbon{position:relative;display:block;text-align:center;font-size:15px;font-weight:700;color:#fff;transform:rotate(45deg);padding:7px 0;left:-5px;top:15px;width:120px;line-height:20px;background-color:#555;box-shadow:0 0 3px rgba(0,0,0,.3)}.ui-ribbon-container.ui-ribbon-primary .ui-ribbon:after,.ui-ribbon-container.ui-ribbon-primary .ui-ribbon:before{border-top:2px solid #5b90bf}.ui-ribbon-container .ui-ribbon:before{left:0;bottom:-1px;right:0}.ui-ribbon-container .ui-ribbon:after,.ui-ribbon-container .ui-ribbon:before{position:absolute;content:\" \";line-height:0;border-top:2px solid #555;border-left:2px solid transparent;border-right:2px solid transparent}.thumbnail .image{height:120px;overflow:hidden}.caption{padding:9px 5px;background:#F7F7F7}.caption p{margin-bottom:5px}.thumbnail{height:190px;overflow:hidden}.view{overflow:hidden;position:relative;text-align:center;box-shadow:1px 1px 2px #e6e6e6;cursor:default}.dropdown-menu a,.voiceBtn{cursor:pointer}.view .content,.view .mask{position:absolute;width:100%;overflow:hidden;top:0;left:0}.sideways,.view .tools,.view img,.view p{position:relative}.view img{display:block}.view .tools{color:#fff;text-align:center;font-size:17px;padding:3px;background:rgba(0,0,0,.35);margin:43px 0 0}.mask.no-caption .tools{margin:90px 0 0}.view .tools a{display:inline-block;color:#FFF;font-size:18px;font-weight:400;padding:0 4px}.view p{font-family:Georgia,serif;font-size:12px;color:#fff;padding:10px 20px 20px;text-align:center}.view a.info{display:inline-block;padding:7px 14px;background:#000;color:#fff;box-shadow:0 0 1px #000}.view-first img{transition:all .2s linear}.view-first .mask{opacity:0;background-color:rgba(0,0,0,.5);transition:all .4s ease-in-out}.view-first .tools{transform:translateY(-100px);opacity:0;transition:all .2s ease-in-out}.view-first p{transform:translateY(100px);opacity:0;transition:all .2s linear}.view-first:hover img{transform:scale(1.1)}.view-first:hover .mask{opacity:1}.view-first:hover .tools,.view-first:hover p{opacity:1;transform:translateY(0)}.view-first:hover p{transition-delay:.1s}.form-group.has-feedback span{display:block!important}.form-group .btn{margin-bottom:-6px}.input-group-btn .btn{margin-bottom:0} .input-group.date .input-group-addon{border-radius:0px!important;background-color:#ffff!important}/*!\n" +
                " * bootstrap-vertical-tabs - v1.2.1\n" +
                " * https://dbtek.github.io/bootstrap-vertical-tabs\n" +
                " * 2014-11-07\n" +
                " * Copyright (c) 2014 İsmail Demirbilek\n" +
                " * License: MIT\n" +
                " */.tabs-left,.tabs-right{border-bottom:none;padding-top:2px}.tabs-left{border-right:1px solid #F7F7F7}.tabs-right{border-left:1px solid #F7F7F7}.tabs-left>li,.tabs-right>li{float:none;margin-bottom:2px}.alignleft,.left{float:left}.tabs-left>li{margin-right:-1px}.tabs-left>li>a,.tabs-right>li>a{margin-right:0;background:#F7F7F7;overflow:hidden;text-overflow:ellipsis}.tabs-right>li{margin-left:-1px}.tabs-left>li.active>a,.tabs-left>li.active>a:focus,.tabs-left>li.active>a:hover{border-bottom-color:#F7F7F7;border-right-color:transparent}.tabs-right>li.active>a,.tabs-right>li.active>a:focus,.tabs-right>li.active>a:hover{border-bottom:1px solid #F7F7F7;border-left-color:transparent}.tabs-left>li>a{border-radius:4px 0 0 4px;display:block}.tabs-right>li>a{border-radius:0 4px 4px 0}.sideways{margin-top:50px;border:none}.sideways>li{height:20px;width:120px;margin-bottom:100px}.sideways>li>a{border-bottom:1px solid #ddd;border-right-color:transparent;text-align:center;border-radius:4px 4px 0 0}.sideways>li.active>a,.sideways>li.active>a:focus,.sideways>li.active>a:hover{border-bottom-color:transparent;border-right-color:#ddd;border-left-color:#ddd}.sideways.tabs-left{left:-50px}.sideways.tabs-right{right:-50px}.sideways.tabs-right>li{transform:rotate(90deg)}.sideways.tabs-left>li{transform:rotate(-90deg)}.morris-hover{position:absolute;z-index:1000}.morris-hover.morris-default-style{padding:6px;color:#666;background:rgba(243,242,243,.8);border:2px solid rgba(195,194,196,.8);font-family:sans-serif;font-size:12px;text-align:center}.morris-hover.morris-default-style .morris-hover-row-label{font-weight:700;margin:.25em 0}.morris-hover.morris-default-style .morris-hover-point{white-space:nowrap;margin:.1em 0}.price{font-size:40px;font-weight:400;color:#26B99A;margin:0}.prod_title{border-bottom:1px solid #DFDFDF;padding-bottom:5px;margin:30px 0;font-size:20px;font-weight:400}.product-image img{width:90%}.prod_color li{margin:0 10px}.prod_color li p{margin-bottom:0}.prod_size li{padding:0}.prod_color .color{width:25px;height:25px;border:2px solid rgba(51,51,51,.28)!important;padding:2px;border-radius:50px}.product_gallery a{width:100px;height:100px;float:left;margin:10px;border:1px solid #e5e5e5}.product_gallery a img{width:100%;margin-top:15px}.product_price{margin:20px 0;padding:5px 10px;background-color:#FFF;text-align:left;border:2px dashed #E0E0E0}.price-tax{font-size:18px}.product_social{margin:20px 0}.product_social ul li a i{font-size:35px}.login{background:#F7F7F7}.login .fa-paw{font-size:26px}a.hiddenanchor{display:none}.login_wrapper{right:0;margin:5% auto 0;max-width:350px;position:relative}.cropper .docs-cropped .modal-body>canvas,.cropper .docs-cropped .modal-body>img,.cropper .img-container>img,.cropper .img-preview>img{max-width:100%}.login_form,.registration_form{position:absolute;top:0;width:100%}.registration_form{z-index:21;opacity:0;width:100%}.login_form{z-index:22}#signin:target~.login_wrapper .login_form,#signup:target~.login_wrapper .registration_form{z-index:22;animation-name:fadeInLeft;animation-delay:.1s}#signin:target~.login_wrapper .registration_form,#signup:target~.login_wrapper .login_form{animation-name:fadeOutLeft}.animate{-webkit-animation-duration:.5s;-webkit-animation-timing-function:ease;-webkit-animation-fill-mode:both;-moz-animation-duration:.5s;-moz-animation-timing-function:ease;-moz-animation-fill-mode:both;-o-animation-duration:.5s;-o-animation-timing-function:ease;-o-animation-fill-mode:both;-ms-animation-duration:.5s;-ms-animation-timing-function:ease;-ms-animation-fill-mode:both;animation-duration:.5s;animation-timing-function:ease;animation-fill-mode:both}.login_box{padding:20px;margin:auto}.alignleft{margin-right:15px}.alignright{float:right;margin-left:15px}.clearfix:after,form:after{content:\".\";display:block;height:0;clear:both;visibility:hidden}.login_content{margin:0 auto;padding:25px 0 0;position:relative;text-align:center;text-shadow:0 1px 0 #fff;min-width:280px}.login_content a:hover{text-decoration:underline}.login_content h1{font:400 25px Helvetica,Arial,sans-serif;letter-spacing:-.05em;line-height:20px;margin:10px 0 30px}.login_content h1:after,.login_content h1:before{content:\"\";height:1px;position:absolute;top:10px;width:20%}.login_content h1:after{background:#7e7e7e;background:linear-gradient(left,#7e7e7e 0,#fff 100%);right:0}.login_content h1:before{background:#7e7e7e;background:linear-gradient(right,#7e7e7e 0,#fff 100%);left:0}.login_content form{margin:20px 0;position:relative}.login_content form input[type=text],.login_content form input[type=email],.login_content form input[type=password]{border-radius:3px;-ms-box-shadow:0 1px 0 #fff,0 -2px 5px rgba(0,0,0,.08) inset;-o-box-shadow:0 1px 0 #fff,0 -2px 5px rgba(0,0,0,.08) inset;box-shadow:0 1px 0 #fff,0 -2px 5px rgba(0,0,0,.08) inset;border:1px solid #c8c8c8;color:#777;margin:0 0 20px;width:100%}.login_content form input[type=text]:focus,.login_content form input[type=email]:focus,.login_content form input[type=password]:focus{-ms-box-shadow:0 0 2px #ed1c24 inset;-o-box-shadow:0 0 2px #ed1c24 inset;box-shadow:0 0 2px #A97AAD inset;background-color:#fff;border:1px solid #A878AF;outline:0}#username{background-position:10px 10px!important}#password{background-position:10px -53px!important}.login_content form div a{font-size:12px;margin:10px 15px 0 0}.reset_pass{margin-top:10px!important}.login_content div .reset_pass{margin-top:13px!important;margin-right:39px;float:right}.separator{border-top:1px solid #D8D8D8;margin-top:10px;padding-top:10px}.button{background:#f7f9fa;background:linear-gradient(top,#f7f9fa 0,#f0f0f0 100%);filter:progid: DXImageTransform.Microsoft.gradient( startColorstr='#f7f9fa', endColorstr='#f0f0f0', GradientType=0);-ms-box-shadow:0 1px 2px rgba(0,0,0,.1) inset;-o-box-shadow:0 1px 2px rgba(0,0,0,.1) inset;box-shadow:0 1px 2px rgba(0,0,0,.1) inset;border-radius:0 0 5px 5px;border-top:1px solid #CFD5D9;padding:15px 0}#content form .submit,.login_content form input[type=submit]{float:left;margin-left:38px}.button a{background:url(http://cssdeck.com/uploads/media/items/8/8bcLQqF.png) 0 -112px no-repeat;color:#7E7E7E;font-size:17px;padding:2px 0 2px 40px;text-decoration:none;transition:all .3s ease}.button a:hover{background-position:0 -135px;color:#00aeef}header{width:100%}#nprogress .bar{background:#1ABB9C}#nprogress .peg{box-shadow:0 0 10px #1ABB9C,0 0 5px #1ABB9C}#nprogress .spinner-icon{border-top-color:#1ABB9C;border-left-color:#1ABB9C}.editor-wrapper{min-height:250px;background-color:#fff;border-collapse:separate;border:1px solid #ccc;padding:4px;box-sizing:content-box;box-shadow:rgba(0,0,0,.07451) 0 1px 1px 0 inset;overflow:scroll;outline:0;border-radius:3px}.voiceBtn{width:20px;color:transparent;background-color:transparent;transform:scale(2,2);-webkit-transform:scale(2,2);-moz-transform:scale(2,2);border:transparent;box-shadow:none;-webkit-box-shadow:none}div[data-role=editor-toolbar]{-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}.select2-container--default .select2-selection--multiple,.select2-container--default .select2-selection--single{background-color:#fff;border:1px solid #ccc;border-radius:0;min-height:38px}.select2-container--default .select2-selection--single .select2-selection__rendered{color:#73879C;padding-top:5px}.select2-container--default .select2-selection--multiple .select2-selection__rendered{padding-top:3px}.select2-container--default .select2-selection--single .select2-selection__arrow{height:36px}.select2-container--default .select2-selection--multiple .select2-selection__choice,.select2-container--default .select2-selection--multiple .select2-selection__clear{margin-top:2px;border:none;border-radius:0;padding:3px 5px}.select2-container--default.select2-container--focus .select2-selection--multiple{border:1px solid #ccc}.switchery{width:32px;height:20px}.switchery>small{width:20px;height:20px}fieldset{border:none;margin:0;padding:0}.cropper .img-container,.cropper .img-preview{background-color:#f7f7f7;width:100%;text-align:center}.cropper .img-container{min-height:200px;max-height:516px;margin-bottom:20px}.cropper .docs-data>.input-group,.cropper .docs-toggles>.btn,.cropper .docs-toggles>.btn-group,.cropper .docs-toggles>.dropdown,.cropper .img-preview{margin-bottom:10px}@media (min-width:768px){.cropper .img-container{min-height:516px}}.cropper .docs-preview{margin-right:-15px}.cropper .img-preview{float:left;margin-right:10px;overflow:hidden}.cropper .preview-lg{width:263px;height:148px}.cropper .preview-md{width:139px;height:78px}.cropper .preview-sm{width:69px;height:39px}.cropper .preview-xs{width:35px;height:20px;margin-right:0}.cropper .docs-data>.input-group>label{min-width:80px}.cropper .docs-data>.input-group>span{min-width:50px}.cropper .docs-buttons>.btn,.cropper .docs-buttons>.btn-group,.cropper .docs-buttons>.form-control{margin-right:5px;margin-bottom:10px}.cropper .docs-tooltip{display:block;margin:-6px -12px;padding:6px 12px}.cropper .docs-tooltip>.icon{margin:0 -3px;vertical-align:top}.cropper .tooltip-inner{white-space:normal}.cropper .btn-toggle .tooltip-inner,.cropper .btn-upload .tooltip-inner{white-space:nowrap}.cropper .btn-toggle{padding:6px}.cropper .btn-toggle>.docs-tooltip{margin:-6px;padding:6px}@media (max-width:400px){.cropper .btn-group-crop{margin-right:-15px!important}.cropper .btn-group-crop>.btn{padding-left:5px;padding-right:5px}.cropper .btn-group-crop .docs-tooltip{margin-left:-5px;margin-right:-5px;padding-left:5px;padding-right:5px}}.cropper .docs-options .dropdown-menu{width:100%}.cropper .docs-options .dropdown-menu>li{padding:3px 20px}.cropper .docs-options .dropdown-menu>li:hover{background-color:#f7f7f7}.cropper .docs-options .dropdown-menu>li>label{display:block}.cropper .docs-cropped .modal-body{text-align:center}.cropper .docs-diagram .modal-dialog{max-width:352px}.cropper .docs-cropped canvas{max-width:100%}.form_wizard .stepContainer{display:block;position:relative;margin:0;padding:0;border:0 solid #CCC;overflow-x:hidden}.wizard_horizontal ul.wizard_steps{display:table;list-style:none;position:relative;width:100%;margin:0 0 20px}.wizard_horizontal ul.wizard_steps li{display:table-cell;text-align:center}.wizard_horizontal ul.wizard_steps li a,.wizard_horizontal ul.wizard_steps li:hover{display:block;position:relative;-moz-opacity:1;filter:alpha(opacity=100);opacity:1;color:#666}.wizard_horizontal ul.wizard_steps li a:before{content:\"\";position:absolute;height:4px;background:#ccc;top:20px;width:100%;z-index:4;left:0}.wizard_horizontal ul.wizard_steps li a.disabled .step_no{background:#ccc}.wizard_horizontal ul.wizard_steps li a .step_no{width:40px;height:40px;line-height:40px;border-radius:100px;display:block;margin:0 auto 5px;font-size:16px;text-align:center;position:relative;z-index:5}.step_no,.wizard_horizontal ul.wizard_steps li a.selected:before{background:#34495E;color:#fff}.wizard_horizontal ul.wizard_steps li a.done .step_no,.wizard_horizontal ul.wizard_steps li a.done:before{background:#1ABB9C;color:#fff}.wizard_horizontal ul.wizard_steps li:first-child a:before{left:50%}.wizard_horizontal ul.wizard_steps li:last-child a:before{right:50%;width:50%;left:auto}.wizard_verticle .stepContainer{width:80%;float:left;padding:0 10px}.actionBar{width:100%;border-top:1px solid #ddd;padding:10px 5px;text-align:right;margin-top:10px}.actionBar .buttonDisabled{cursor:not-allowed;pointer-events:none;opacity:.65;filter:alpha(opacity=65);box-shadow:none}.actionBar a{margin:0 3px}.wizard_verticle .wizard_content{width:80%;float:left;padding-left:20px}.wizard_verticle ul.wizard_steps{display:table;list-style:none;position:relative;width:20%;float:left;margin:0 0 20px}.wizard_verticle ul.wizard_steps li{display:list-item;text-align:center}.wizard_verticle ul.wizard_steps li a{height:80px}.wizard_verticle ul.wizard_steps li a:first-child{margin-top:20px}.wizard_verticle ul.wizard_steps li a,.wizard_verticle ul.wizard_steps li:hover{display:block;position:relative;-moz-opacity:1;filter:alpha(opacity=100);opacity:1;color:#666}.wizard_verticle ul.wizard_steps li a:before{content:\"\";position:absolute;height:100%;background:#ccc;top:20px;width:4px;z-index:4;left:49%}.wizard_verticle ul.wizard_steps li a.disabled .step_no{background:#ccc}.wizard_verticle ul.wizard_steps li a .step_no{width:40px;height:40px;line-height:40px;border-radius:100px;display:block;margin:0 auto 5px;font-size:16px;text-align:center;position:relative;z-index:5}.progress.progress_sm,.progress.progress_sm .progress-bar{height:10px!important}.step_no,.wizard_verticle ul.wizard_steps li a.selected:before{background:#34495E;color:#fff}.wizard_verticle ul.wizard_steps li a.done .step_no,.wizard_verticle ul.wizard_steps li a.done:before{background:#1ABB9C;color:#fff}.wizard_verticle ul.wizard_steps li:first-child a:before{left:49%}.wizard_verticle ul.wizard_steps li:last-child a:before{left:49%;left:auto;width:0}.form_wizard .loader,.form_wizard .msgBox{display:none}.progress{border-radius:0}.progress-bar-info{background-color:#3498DB}.progress-bar-success{background-color:#26B99A}.progress_summary .progress{margin:5px 0 12px!important}.progress_summary .row{margin-bottom:5px}.progress_summary .row .col-xs-2{padding:0}.progress_summary .data span,.progress_summary .more_info span{text-align:right;float:right}.progress_summary p{margin-bottom:3px;width:100%}.progress_title .left{float:left;text-align:left}.progress_title .right{float:right;text-align:right;font-weight:300}.progress.progress_sm{border-radius:0;margin-bottom:18px}.dashboard_graph p{margin:0 0 4px}ul.verticle_bars{width:100%}ul.verticle_bars li{width:23%;height:200px;margin:0}.progress.vertical.progress_wide{width:35px}.alert-success{color:#fff;background-color:rgba(38,185,154,.88);border-color:rgba(38,185,154,.88)}.alert-info{color:#E9EDEF;background-color:rgba(52,152,219,.88);border-color:rgba(52,152,219,.88)}.alert-warning{color:#E9EDEF;background-color:rgba(243,156,18,.88);border-color:rgba(243,156,18,.88)}.alert-danger,.alert-error{color:#E9EDEF;background-color:rgba(231,76,60,.88);border-color:rgba(231,76,60,.88)}.ui-pnotify.dark .ui-pnotify-container{color:#E9EDEF;background-color:rgba(52,73,94,.88);border-color:rgba(52,73,94,.88)}.custom-notifications{position:fixed;margin:15px;right:0;float:right;width:400px;z-index:4000;bottom:0}ul.notifications{float:right;display:block;margin-bottom:7px;padding:0;width:100%}.notifications li{float:right;margin:3px;width:36px;box-shadow:3px 3px 3px rgba(0,0,0,.3)}.notifications li:last-child{margin-left:0}.notifications a{display:block;text-align:center;text-decoration:none;padding:9px 8px}.tabbed_notifications .text{padding:5px 15px;height:140px;border-radius:7px;box-shadow:6px 6px 6px rgba(0,0,0,.3)}.tabbed_notifications div p{display:inline-block}.tabbed_notifications h2{font-weight:700;width:80%;float:left;height:20px;text-overflow:ellipsis;overflow:hidden;display:block}.tabbed_notifications .close{padding:5px;color:#E9EDEF;float:right;opacity:1}.fc-state-default{background:#f5f5f5;color:#73879C}.fc-state-active,.fc-state-down{color:#333;background:#ccc}.dropzone{min-height:300px;border:1px solid #e5e5e5}.main_menu .label{line-height:11px;margin-top:4px}@media (max-width:460px){.dataTables_wrapper .col-sm-6{width:100%;margin-bottom:5px}.dataTables_wrapper .col-sm-6 .dataTables_filter{float:none}}@media (max-width:767px){.dataTables_length{float:none}}.daterangepicker.xdisplay{width:228px}.dataTables_wrapper>.row{overflow:auto!important}\n</style>"+
                "    <script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js\"></script>\n" +
                "    <script src=\"http://cdnjs.cloudflare.com/ajax/libs/raphael/2.1.0/raphael-min.js\"></script>\n" +
                "    <script src=\"http://cdnjs.cloudflare.com/ajax/libs/morris.js/0.5.1/morris.min.js\"></script>\n" +
                "    <!-- Bootstrap -->\n" +
                "    <link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
                "    <!-- Font Awesome -->\n" +
                "    <link href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\" rel=\"stylesheet\">\n" +
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



        sb.append("<div role=\"main\">\n" +
                "<!-- top tiles -->\n" +
                "<div class=\"row tile_count\" style=\"\n" +
                "    margin-left: 0%;\n" +
                "\">\n" +
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


        JSONArray chromeTestsFile = data.getModuleWiseData(buildHistory, "chrome");
        JSONArray firefoxTestsFile = data.getModuleWiseData(buildHistory, "firefox");
        JSONArray operaTestsFile = data.getModuleWiseData(buildHistory, "opera");
        JSONArray ieTestsFile = data.getModuleWiseData(buildHistory, "ie");
        JSONArray safariTestsFile = data.getModuleWiseData(buildHistory, "safari");


        sb = generatePerModuleSummary(sb, chromeTestsFile, "Chrome");
        sb = generatePerModuleSummary(sb, firefoxTestsFile, "Firefox");
        sb = generatePerModuleSummary(sb, ieTestsFile, "IE");
        sb = generatePerModuleSummary(sb, operaTestsFile, "Opera");
        sb = generatePerModuleSummary(sb, safariTestsFile, "Safari");


        return sb;
    }


    public StringBuffer generatePerModuleSummary(StringBuffer sb, JSONArray testsFileArray, String browserName) {

        try {


            if (testsFileArray.size() > 0) {

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


                for (int i = 0; i < testsFileArray.size(); i++) {

                    sb.append(" <tr>\n" +
                            " <th scope=\"row\">" + (i + 1) + "</th>\n" +
                            " <td>" + ((JSONObject) testsFileArray.get(i)).get("testsFileName") + "</td>\n" +
                            " <td>" + (Integer.parseInt(((JSONObject) testsFileArray.get(i)).get("totalFailed").toString()) + Integer.parseInt(((JSONObject) testsFileArray.get(i)).get("totalPassed").toString())) + "</td>\n" +
                            " <td>" + ((JSONObject) testsFileArray.get(i)).get("totalPassed") + "</td>\n" +
                            " <td>" + ((JSONObject) testsFileArray.get(i)).get("totalFailed") + "</td>\n" +
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


            //array of the all the tests file


            JSONArray testsFileList = ((JSONArray) ((JSONObject) ((JSONObject) singleBrowser).get(browser)).get("testsFile"));


            sb.append("<div id=\"" + browser + "\" class=\"panel-collapse collapse\">\n");


            for (Object testsFile : testsFileList)

            {


                sb.append("<!-- TestsFile : " + ((JSONObject) testsFile).get("testsFileName") + "-->");


                JSONArray testList = (JSONArray) ((JSONObject) testsFile).get("tests");
                sb.append(

                        "<div class=\"x_panel\" class=\"panel-collapse collapse\"\n" +
                                "role=\"tabpanel\"\n" +
                                "aria-labelledby=\"headingOne\">\n" +
                                "<div class=\"x_title\">\n" +
                                "<h2><i class=\"fa fa-align-left\"></i> " + ((JSONObject) testsFile).get("testsFileName") + "\n" +
                                "</h2>\n" +
                                " <div class=\"nav navbar-right\" style=\"padding-top : 5px \">\n" +
                                "<font>Total  : <b>" + (Double.parseDouble(((JSONObject) testsFile).get("totalPassed").toString()) + Double.parseDouble(((JSONObject) testsFile).get("totalFailed").toString())) + "</b> |</font>\n" +
                                "<font>Passed : <b>" + ((JSONObject) testsFile).get("totalPassed") + "</b> |</font>\n" +
                                "<font>Failed : <b>" + ((JSONObject) testsFile).get("totalFailed") + "</b>  |</font>\n" +
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
                        osName = "Win10";
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



                    if (isTestFailed) {

                        try {
                            stacktrace = ((JSONObject) test).get("fullStackTrace").toString();
                        } catch (Exception e) {
                            tesboLogger.errorLog("StackTrace Not Found");

                        }
                        try {
                            screenShotpath = ((JSONObject) test).get("screenShot").toString();

                        } catch (Exception e) {
                            tesboLogger.errorLog("Screenshot Not Found");

                        }
                    }
                    String suiteOrTagName="";
                    if(!testDetails.get("suiteName").toString().equals("")){
                        suiteOrTagName="<font color=\"#fff\" data-toggle=\"tooltip\" title=\"Suite File\" class=\"nev navbar-right\" style=\"margin-right:1%;font-size: 12px;padding: 5px 10px;background: #007bff;border-radius: 7px;\">\n" +
                                testDetails.get("suiteName").toString() +
                                "</font>\n";
                    }
                    if(!testDetails.get("tagName").toString().equals("")){
                        suiteOrTagName="<font color=\"#fff\" data-toggle=\"tooltip\" title=\"Tag Name\" class=\"nev navbar-right\" style=\"margin-right:1%;font-size: 12px;padding: 5px 10px;background: #17a2b8;border-radius: 7px;\">\n" +
                                testDetails.get("tagName").toString() +
                                "</font>\n";
                    }

                    Random rand = new Random();
                    int randomNumber = rand.nextInt();
                    sb.append("<!-- start accordion -->\n" +
                            "<div class=\"accordion\" id=\"" + browser + testDetails.get("testName")+randomNumber + "\" role=\"tablist\"\n" +
                            "aria-multiselectable=\"true\">\n" +
                            "<div class=\"panel\">\n" +
                            "<a class=\"panel-heading\" role=\"tab\"\n" +
                            "data-toggle=\"collapse\"\n" +
                            "data-parent=\"#accordion\" href=\"#" + browser +randomNumber + "\"\n" +
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
                            suiteOrTagName+
                            "</h4>\n" +

                            "</a>\n" +
                            "<div id=\"" + browser +randomNumber + "\" class=\"panel-collapse collapse\"\n" +
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
                                        "<img src=\"../"+ screenShotpath + "\" \n" +
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
                "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js\"></script>\n" +
                "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js\"></script>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n");

        return sb;
    }


    public void startThread() {
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
