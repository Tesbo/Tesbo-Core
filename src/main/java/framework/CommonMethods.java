package framework;

import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import Exception.TesboException;

public class CommonMethods {

    public  void verifyJsonArrayIsEmpty(JSONArray arrayList, String errorMsg, Logger log){

        if(arrayList.isEmpty()){
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
    }

    public  void throwTesboException(String errorMsg, Logger log){
            log.error(errorMsg);
            throw new TesboException(errorMsg);
    }
}
