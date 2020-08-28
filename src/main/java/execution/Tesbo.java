package execution;

import logger.TesboLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Tesbo {

    private static final Logger log = LogManager.getLogger(Tesbo.class);

    /**
     *
     * @param arguments
     */
    public void run(String[] arguments) {

        log.info("**************************************** New Build Start ******************************************");
        String infoMsg="List of arguments: "+arguments;
        log.info(infoMsg);
        TestExecutionBuilder builder = new TestExecutionBuilder();
        TesboLogger logger=new TesboLogger();
        try {
            builder.startExecution(arguments);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
            log.error(sw.toString());
        }


    }


}
