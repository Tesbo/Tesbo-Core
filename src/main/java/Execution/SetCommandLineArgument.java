package Execution;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Created by Ankit Mistry on 26/2/2019.
 */
public class SetCommandLineArgument {

    @Parameter(names={"--configFile", "-c"})
    public static String configFile;
    @Parameter(names={"--baseUrl", "-u"})
    public static String baseUrl;
    @Parameter(names={"--browser", "-b"})
    public static String browser;
    @Parameter(names={"--platform", "-p"})
    public static String platform;
    @Parameter(names={"--IsGrid", "-i"})
    public static String IsGrid;
    @Parameter(names={"--Environment", "-e"})
    public static String Environment;
    @Parameter(names={"--runPastFailure", "-f"})
    public static String runPastFailure;
    @Parameter(names={"--byTag", "-t"})
    public static String byTag;
    @Parameter(names={"--buildName", "-n"})
    public static String buildName;

    public void setArgument(String[] args) {
        SetCommandLineArgument setCommandLineArgument=new SetCommandLineArgument();
        JCommander.newBuilder()
                .addObject(setCommandLineArgument)
                .build()
                .parse(args);
    }

}
