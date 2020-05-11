package framework;

import logger.TesboLogger;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import Exception.TesboException;

public class CommonMethods {

    TesboLogger tesboLogger = new TesboLogger();
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
    public  void printStepInfo(String InfoMsg, Logger log){
        log.info(InfoMsg);
        tesboLogger.stepLog(InfoMsg);
    }
    public void logErrorMsg(String errorMsg,Logger log){
        log.error(errorMsg);
        tesboLogger.testFailed(errorMsg);
    }
}
