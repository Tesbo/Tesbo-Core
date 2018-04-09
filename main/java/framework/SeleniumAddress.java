package framework;

import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class SeleniumAddress {

    public boolean IsCapabilities(String browserName) {
        GetConfiguration config = new GetConfiguration();
        JSONObject capabilities = null;
        boolean browser = false;
        try {
            capabilities = config.getCapabilities();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (capabilities != null) {
            Set<String> browserCaps = (Set<String>) capabilities.keySet();
            for (String browserCap : browserCaps) {
                if (browserCap.equalsIgnoreCase(browserName)) {
                    if (((ArrayList<String>) capabilities.get(browserCap)).size() == 0)
                        browser = false;
                    else
                        browser = true;
                }
            }
        }
        return browser;
    }

    public String getSeleniumAddress() {
        GetConfiguration config = new GetConfiguration();
        String seleniumAddress=null;
        try {
            seleniumAddress=config.getSeleniumAddress();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return seleniumAddress;
    }

    public ArrayList<String> getCapabilities(String browserName) {
        GetConfiguration config = new GetConfiguration();
        JSONObject capabilities=null;
        try {
            capabilities=config.getCapabilities();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> capabilitieList= (ArrayList<String>) capabilities.get(browserName);

        return capabilitieList;
    }

    public DesiredCapabilities setCapabilities(ArrayList<String> Capabilities,DesiredCapabilities capability) {
        for(Object cap:Capabilities) {
            JSONObject objCap= (JSONObject) cap;
            Set capKey=objCap.keySet();
            Collection capValue= objCap.values();
            capability.setCapability(capKey.toString(),capValue);
        }
        return capability;
    }
    public WebDriver openRemoteBrowser(String seleniumAddress,WebDriver driver,DesiredCapabilities capability) {
        try {
            driver = new RemoteWebDriver(new URL(seleniumAddress),capability);
            System.out.println("Selenium Address : "+seleniumAddress);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return driver;
    }
}
