package Execution;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Created by QAble on 10/3/2018.
 */
public class SetCommandLineArgument {

    //SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();

    @Parameter(names={"--configFile", "-c"})
    public static String configFile;
    @Parameter(names={"--baseUrl", "-u"})
    public static String baseUrl;
    @Parameter(names={"--browser", "-b"})
    public static String browser;
    @Parameter(names={"--platform", "-p"})
    public static String platform;
    @Parameter(names={"--IsCBT", "-i"})
    public static String IsCBT;
    @Parameter(names={"--Environment", "-e"})
    public static String Environment;

    public void setArgument(String[] args) {
        SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();
        JCommander.newBuilder()
                .addObject(setCommandLineArgument)
                .build()
                .parse(args);
    }

}
