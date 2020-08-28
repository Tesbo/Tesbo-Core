package framework;

import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetLocator {

    TesboLogger tesboLogger = new TesboLogger();
    String json=".json";
    String locator="Locator '";
    CommonMethods commonMethods=new CommonMethods();
    GetConfiguration config = new GetConfiguration();
    boolean flag=false;

    private static final Logger log = LogManager.getLogger(GetLocator.class);


    /**
     *
     * @param testsFileName
     * @param locatorName
     * @return
     */

    public String getLocatorValue(String testsFileName, String locatorName) {
        verifyLocatorNameIsNotEmpty(locatorName);
        JSONArray locatorFileList = new JSONArray();
        JSONObject main=null;
        if(locatorName.contains(".")){
            String fileName=locatorName.split("\\.")[0];
             main = Utility.loadJsonFile(config.getLocatorDirectory() + "/" + fileName + json);
        }
        else {
            try (Stream<Path> paths = Files.walk(Paths.get(config.getLocatorDirectory()))) {
                flag=verifyThatLocatorDirectoryHasOnlyJsonFile(locatorFileList, paths);
            } catch (Exception e) {
                if(flag) {
                    commonMethods.throwTesboException("Please create only '.json' file in Locator directory.", log);
                }
                else {
                    tesboLogger.testFailed("Message : Please Enter valid directory path for locators.");
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    tesboLogger.testFailed(sw.toString());
                    log.error(sw.toString());
                }
            }
            main = Utility.loadJsonFile(config.getLocatorDirectory() + "/" + testsFileName.split(".tests")[0] + json);
        }

        String locatorsName = null;
        locatorsName=getLocatorValueFromJson(locatorName,testsFileName,main);
        handelExceptionWhenLocatorsNameIsNull(locatorsName, locatorName);
        return locatorsName;
    }

    /**
     *
     * @param locatorsName
     * @param actualLocatorName
     */
    public void handelExceptionWhenLocatorsNameIsNull(String locatorsName, String actualLocatorName){
        if(locatorsName==null){
            IfStepParser.isIfError=true;
            commonMethods.throwTesboException(locator+actualLocatorName + "' is Empty.",log);
        }
    }

    /**
     *
     * @param testsFileName
     * @param actualLocatorName
     * @return
     */
    public String getLocatorFromOthersFileIfExist(String testsFileName,String actualLocatorName){

        String locatorsName = null;
        File[] files = new File(config.getLocatorDirectory()).listFiles();
        boolean isLocator=false;
        for(File file1:files){
            if(!(config.getLocatorDirectory() + "/" + testsFileName.split(".tests")[0] + json).equalsIgnoreCase(file1.toString())){
                JSONObject main = Utility.loadJsonFile(file1.toString());
                try{
                    locatorsName= main.get(actualLocatorName).toString();
                    if(!isLocator){
                        isLocator=true;
                    }
                    else{
                        commonMethods.throwTesboException("Multiple Locator is found '"+actualLocatorName + "'.",log);
                    }
                }catch (NullPointerException ex) {log.error("");}
            }
        }
        handelExceptionWhenLocatorsNameIsNull(locatorsName, actualLocatorName);
        return locatorsName;
    }

    /**
     *
     * @param locatorFileList
     * @param paths
     * @return
     */
    public boolean verifyThatLocatorDirectoryHasOnlyJsonFile(JSONArray locatorFileList,Stream<Path> paths) {
        StringBuilder file = new StringBuilder();
        locatorFileList.addAll(paths
                .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
        for(Object locatorPath:locatorFileList) {
            String[] locators=locatorPath.toString().split("\\.");
            if (locators.length == 2 && (!locators[1].equalsIgnoreCase("json"))) {
                flag=true;
                if(file.length()==0)
                    file.append("'."+locators[1]+"'");
                else
                    file.append(", '."+locators[1]+"'");

            }
        }
        if(flag){
            String errorMsg=file+" file found";
            tesboLogger.errorLog(errorMsg);
            commonMethods.throwTesboException(errorMsg,log);
        }
        return flag;
    }

    /**
     *
     * @param actualLocatorName
     * @param main
     * @return
     */
    public String getLocatorFromSameAsTestFile(String actualLocatorName,JSONObject main){
        String locatorsName = null;
        if(actualLocatorName.contains(".")) {
            locatorsName= main.get(actualLocatorName.split("\\.")[1]).toString();
        }
        else {
            locatorsName= main.get(actualLocatorName).toString();
        }
        return locatorsName;
    }

    /**
     *
     * @param locatorName
     * @param testsFileName
     * @param main
     * @return
     */

    public String getLocatorValueFromJson(String locatorName,String testsFileName, JSONObject main){
        String locatorsName = null;

        try{
            locatorsName=getLocatorFromSameAsTestFile(locatorName,main);

        }catch (NullPointerException e)
        {
            locatorsName=getLocatorFromOthersFileIfExist(testsFileName,locatorName);

        }
        return locatorsName;
    }

    /**
     *
     * @param locatorName
     */
    public void verifyLocatorNameIsNotEmpty(String locatorName){
        if(locatorName.equals("")) {
            commonMethods.throwTesboException("Locator is not define.",log);
        }
    }


}

