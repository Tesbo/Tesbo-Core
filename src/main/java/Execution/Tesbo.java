package Execution;

import framework.GetConfiguration;
import logger.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Tesbo {

    public void run() {

        GetConfiguration configuration = new GetConfiguration();

        configuration.getCloudIntegration();



        TestExecutionBuilder builder = new TestExecutionBuilder();
        Logger logger=new Logger();
        try {
            builder.startExecution();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.testFailed(sw.toString());
        }


    }





}
