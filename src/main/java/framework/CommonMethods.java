package framework;

import logger.TesboLogger;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import exception.*;

public class CommonMethods {

    TesboLogger tesboLogger = new TesboLogger();


    /**
     *
     * @param arrayList pass the arraylist that you want to check
     * @param errorMsg sent the error message you wanted to show
     * @param log log object to add the error message into the log
     */
    public  void verifyJsonArrayIsEmpty(JSONArray arrayList, String errorMsg, Logger log){

        if(arrayList.isEmpty()){
            log.error(errorMsg);
            throw new TesboException(errorMsg);
        }
    }

    /**
     *
     * @param errorMsg sent the error message you wanted to show
     * @param log log object to add the error message into the log
     */
    public  void throwTesboException(String errorMsg, Logger log){
            log.error(errorMsg);
            throw new TesboException(errorMsg);
    }

    /**
     *
     * @param errorMsg sent the error message you wanted to show
     * @param log log object to add the error message into the log
     */
    public  void throwAssertException(String errorMsg, Logger log){
        log.error(errorMsg);
        throw new AssertException(errorMsg);
    }

    /**
     *
     * @param infoMsg information message you want to show
     * @param log log object
     */
    public  void printStepInfo(String infoMsg, Logger log){
        log.info(infoMsg);
        tesboLogger.stepLog(infoMsg);
    }

    /**
     * 
     * @param errorMsg error message you wanted to show
     * @param log log object to print error message in to the log
     */
    public void logErrorMsg(String errorMsg,Logger log){
        log.error(errorMsg);
        tesboLogger.testFailed(errorMsg);
    }
}
