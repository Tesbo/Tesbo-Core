package framework;

import logger.TesboLogger;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import exception.*;

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
    public  void throwAssertException(String errorMsg, Logger log){
        log.error(errorMsg);
        throw new AssertException(errorMsg);
    }
    public  void printStepInfo(String infoMsg, Logger log){
        log.info(infoMsg);
        tesboLogger.stepLog(infoMsg);
    }
    public void logErrorMsg(String errorMsg,Logger log){
        log.error(errorMsg);
        tesboLogger.testFailed(errorMsg);
    }
}
