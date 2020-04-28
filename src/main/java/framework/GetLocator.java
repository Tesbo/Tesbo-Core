package framework;

import Execution.Tesbo;
import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import Exception.TesboException;

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

    private static final Logger log = LogManager.getLogger(GetLocator.class);
    public String getLocatorValue(String testsFileName, String locatorName) throws Exception {
        if(locatorName.equals("")) {
            log.error("Locator is not define.");
            throw new TesboException("Locator is not define.");
        }
        GetConfiguration config = new GetConfiguration();
        JSONArray locatorFileList = new JSONArray();
        boolean flag=false;
        String file=null;
        JSONObject main=null;
        if(locatorName.contains(".")){
            String fileName=locatorName.split("\\.")[0];
             main = Utility.loadJsonFile(config.getLocatorDirectory() + "/" + fileName + json);
        }
        else {
            try (Stream<Path> paths = Files.walk(Paths.get(config.getLocatorDirectory()))) {

                locatorFileList.addAll(paths
                        .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
                for(Object locatorPath:locatorFileList) {
                    String[] locators=locatorPath.toString().split("\\.");
                    if (locators.length == 2 && (!locators[1].equalsIgnoreCase("json"))) {
                        flag=true;
                        if(file==null)
                            file="'."+locators[1]+"'";
                        else
                            file+=", '."+locators[1]+"'";
                    }
                }
                if(flag){
                    String errorMsg=file+" file found";
                    log.error(errorMsg);
                    tesboLogger.errorLog(errorMsg);
                    throw (new NoSuchFieldException());
                }
            } catch (Exception e) {
                if(flag) {
                    log.error("Message : Please create only '.json' file in Locator directory.");
                    tesboLogger.errorLog("Message : Please create only '.json' file in Locator directory.");
                }
                else {
                    log.error("Message : Please Enter valid directory path for locators.");
                    tesboLogger.errorLog("Message : Please Enter valid directory path for locators.");
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    tesboLogger.testFailed(sw.toString());
                    log.error(sw.toString());
                }
                throw e;

            }

            main = Utility.loadJsonFile(config.getLocatorDirectory() + "/" + testsFileName.split(".tests")[0] + json);

        }

        String locatorsName = null;
        try{
            if(locatorName.contains(".")) {
                locatorsName= main.get(locatorName.split("\\.")[1]).toString();
            }
            else {
                locatorsName= main.get(locatorName).toString();
            }

        }catch (NullPointerException e)
        {
            File[] files = new File(config.getLocatorDirectory()).listFiles();
            boolean isLocator=false;
            for(File file1:files){
                if(!(config.getLocatorDirectory() + "/" + testsFileName.split(".tests")[0] + json).equalsIgnoreCase(file1.toString())){
                    main = Utility.loadJsonFile(file1.toString());
                    try{
                        locatorsName= main.get(locatorName).toString();
                        if(!isLocator){
                            isLocator=true;
                        }
                        else{
                            String errorMsg="Multiple Locator is found '"+locatorName + "'.";
                            log.error(errorMsg);
                            throw new TesboException(errorMsg);
                        }
                    }catch (NullPointerException ex) {}
                }
            }
            if(locatorsName==null){
                IfStepParser.isIfError=true;
                String errorMsg=locator+locatorName + "' is not found.";
                log.error(errorMsg);
                throw new TesboException(errorMsg);
            }

        }
        if(locatorsName.trim().equals("")){
            IfStepParser.isIfError=true;
            String errorMsg=locator+locatorName + "' is Empty.";
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
        return locatorsName;
    }

}
