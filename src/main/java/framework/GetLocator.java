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
    private static final Logger log = LogManager.getLogger(Tesbo.class);
    public String getLocatorValue(String suiteName, String LocatorName) throws Exception {
        if(LocatorName.equals("")) {
            log.error("Locator is not define.");
            throw new TesboException("Locator is not define.");
        }
        GetConfiguration config = new GetConfiguration();
        JSONArray locatorFileList = new JSONArray();
        boolean flag=false;
        String file=null;
        Utility parser = new Utility();
        JSONObject main=null;
        if(LocatorName.contains(".")){
            String fileName=LocatorName.split("\\.")[0];
             main = parser.loadJsonFile(config.getLocatorDirectory() + "/" + fileName + ".json");
        }
        else {
            try (Stream<Path> paths = Files.walk(Paths.get(config.getLocatorDirectory()))) {

                locatorFileList.addAll(paths
                        .filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new)));
                for(Object locatorPath:locatorFileList) {
                    String[] locator=locatorPath.toString().split("\\.");
                    if (locator.length == 2) {
                        if (!locator[1].equalsIgnoreCase("json")) {
                            flag=true;
                            if(file==null)
                                file="'."+locator[1]+"'";
                            else
                                file+=", '."+locator[1]+"'";
                        }
                    }
                }
                if(flag==true){
                    log.error(file+" file found");
                    tesboLogger.errorLog(file+" file found");
                    throw (new NoSuchFieldException());
                }
            } catch (Exception e) {
                if(flag==true) {
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

            main = parser.loadJsonFile(config.getLocatorDirectory() + "/" + suiteName.split(".suite")[0] + ".json");

        }

        String LocatorsName = null;
        try{
            if(LocatorName.contains(".")) {
                LocatorsName= main.get(LocatorName.split("\\.")[1]).toString();
            }
            else {LocatorsName= main.get(LocatorName).toString();}

        }catch (NullPointerException e)
        {
            File[] files = new File(config.getLocatorDirectory()).listFiles();
            boolean isLocator=false;
            for(File file1:files){
                if(!(config.getLocatorDirectory() + "/" + suiteName.split(".suite")[0] + ".json").equalsIgnoreCase(file1.toString())){
                    main = parser.loadJsonFile(file1.toString());
                    try{
                        LocatorsName= main.get(LocatorName).toString();
                        if(isLocator==false){
                            isLocator=true;
                        }
                        else{
                            log.error("Multiple Locator is found '"+LocatorName + "'.");
                            throw new TesboException("Multiple Locator is found '"+LocatorName + "'.");
                        }
                    }catch (NullPointerException ex) {}
                }
            }
            if(LocatorsName==null){
                log.error("Locator '"+LocatorName + "' is not found.");
                throw new TesboException("Locator '"+LocatorName + "' is not found.");
            }

        }
        
        return LocatorsName;
    }

}
