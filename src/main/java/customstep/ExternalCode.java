package customstep;

import framework.CommonMethods;
import framework.GetConfiguration;
import framework.StepParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class ExternalCode  {
    private static final Logger log = LogManager.getLogger(ExternalCode.class);


    /**
     *
     * @param annotation step annotation
     * @param step
     * @param test
     * @param driver Driver
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void runAllAnnotatedWith(Class<? extends Annotation> annotation, String step, JSONObject test, WebDriver driver) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        boolean flag=false;
        GetConfiguration getConfiguration=new GetConfiguration();
        CommonMethods commonMethods=new CommonMethods();
        File file=new File(getConfiguration.getCustomStepDirectory());

        String tagVal=null;
        String[] arguments=null;
        try {

            tagVal=step.split(":")[1].trim();
            if(tagVal.split("\\(").length>1) {
                arguments=getArgumentsOfExtCode(tagVal,test);
            }
            tagVal=tagVal.split("\\(")[0].trim();
        } catch (Exception e) {
            commonMethods.throwTesboException("Code step has no value",log);
        }

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(file.getName()))
                .setScanners(new MethodAnnotationsScanner()));
        Set<Method> methods = reflections.getMethodsAnnotatedWith(annotation);
        flag=invokeCodeMethods(driver, methods,tagVal,arguments);
        if (!flag) {
            commonMethods.throwTesboException("'" + tagVal + "' is not found",log);
        }
    }

    /**
     *
     * @param args
     * @param test
     * @return
     */
    public String[] getArgumentsOfExtCodeWhenDataSet(String[] args,JSONObject test){
        StepParser stepParser=new StepParser();
        StringBuilder argument=new StringBuilder();

        int i=0;

        for(String arg:args){
            if (arg.contains("{") && arg.contains("}")) {
                if(i==0){argument.append(stepParser.passArgsToCode(test,arg));i++;}
                else {argument.append(","+stepParser.passArgsToCode(test,arg));i++;}
            }
            else{
                if(i==0) {argument.append(arg);i++;}
                else{argument.append(","+arg);i++;}
            }
        }
           return argument.toString().split(",");
    }

    /**
     *
     * @param tagVal
     * @param test
     * @return
     */
    public String[] getArgumentsOfExtCode(String tagVal,JSONObject test){
        String[] arguments=null;
        if (tagVal.contains("{") && tagVal.contains("}")) {
            String[] args = tagVal.split("\\(")[1].trim().replaceAll("[()]", "").split(",");

            arguments=getArgumentsOfExtCodeWhenDataSet(args,test);
        }
        else {
            arguments = tagVal.split("\\(")[1].trim().replaceAll("[()]", "").split(",");
        }
        return arguments;
    }

    /**
     *
     * @param driver
     * @param methods
     * @param tagVal
     * @param arguments
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public boolean invokeCodeMethods(WebDriver driver,Set<Method> methods,String tagVal,String[] arguments) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        boolean flag=false;
        for (Method m : methods) {
            if (m.getAnnotation(Step.class) != null && m.getAnnotation(Step.class).value().equals(tagVal)) {
                    String className = m.getDeclaringClass().toString().split("class")[1].trim();
                    Class<?> cls = Class.forName(className);
                    Constructor<?> cons = cls.getConstructor(WebDriver.class);
                    Object myTestCode = cons.newInstance(driver);
                    flag = true;
                    if(arguments==null) {
                        m.invoke(myTestCode);
                    }else{
                        m.invoke(myTestCode,arguments);
                    }
                    break;

            }
        }
        return flag;
    }
}
