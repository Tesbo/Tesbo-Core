package framework;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetLocator {

    public static void main(String[] args) {
        GetLocator lc = new GetLocator();
        //lc.getLocatorValue("login.suite", "gmailLink");
    }

    public String getLocatorValue(String suiteName, String LocatorName) throws Exception {
        if(LocatorName.equals(""))
            throw new NullPointerException("Locator is not define.") ;

        GetConfiguration config = new GetConfiguration();
        JSONArray locatorFileList = new JSONArray();
        boolean flag=false;
        String file=null;
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
                System.err.println(file+" file found");
                throw (new NoSuchFieldException());
            }
        } catch (Exception e) {
            if(flag==true) {
                System.err.println("Message : Please create only '.json' file in Locator directory.");
            }
            else {
                System.err.println("Message : Please Enter valid directory path for locators.");
                e.printStackTrace();
            }
            throw e;
        }
        Utility parser = new Utility();

        JSONObject main = parser.loadJsonFile(config.getLocatorDirectory() + "/" + suiteName.split(".suite")[0] + ".json");
        String LocatorsName = null;
        try{
            LocatorsName= main.get(LocatorName).toString();
        }catch (NullPointerException e)
        {
            System.err.println("Locator '"+LocatorName + "' is not found.");
            e.printStackTrace();
        }
        
        return LocatorsName;
    }

}
