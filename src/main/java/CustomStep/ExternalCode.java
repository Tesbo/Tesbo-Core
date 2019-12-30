package CustomStep;

import framework.GetConfiguration;
import framework.StepParser;
import org.json.simple.JSONObject;
import org.openqa.selenium.WebDriver;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import Exception.*;

import java.io.File;
import java.lang.Exception;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

public class ExternalCode  {

    /**
     *
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param annotation
     * @param step
     * @throws Exception
     */
    public void runAllAnnotatedWith(Class<? extends Annotation> annotation, String step, JSONObject test, WebDriver driver) throws Exception {
        boolean flag=false;
        GetConfiguration getConfiguration=new GetConfiguration();
        StepParser stepParser=new StepParser();
        File file=new File(getConfiguration.getCustomStepDirectory());

        String tagVal=null;
        String arguments[]=null;
        try {

            tagVal=step.split(":")[1].trim();
            if(tagVal.split("\\(").length>1) {
                if (tagVal.contains("{") && tagVal.contains("}")) {
                    String[] args = tagVal.split("\\(")[1].trim().replaceAll("[()]", "").split(",");
                    int i=0;
                    String argument=null;
                    for(String arg:args){
                        if (arg.contains("{") && arg.contains("}")) {
                            if(i==0){argument=stepParser.passArgsToCode(test,arg);i++;}
                            else {argument+=","+stepParser.passArgsToCode(test,arg);i++;}
                        }
                        else{
                            if(i==0) {argument = arg;i++;}
                            else{argument += ","+arg;i++;}
                        }
                    }
                    arguments=argument.split(",");
                }
                else {
                    arguments = tagVal.split("\\(")[1].trim().replaceAll("[()]", "").split(",");
                }
            }
            tagVal=tagVal.split("\\(")[0].trim();
        } catch (Exception e) {
            throw new TesboException("Code step has no value");
        }

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(file.getName()))
                .setScanners(new MethodAnnotationsScanner()));
        Set<Method> methods = reflections.getMethodsAnnotatedWith(annotation);
        for (Method m : methods) {
            if (m.getAnnotation(Step.class) != null) {
                if (m.getAnnotation(Step.class).value().equals(tagVal)) {
                    String className = m.getDeclaringClass().toString().split("class")[1].trim();
                    try {
                        Class<?> cls = Class.forName(className);
                        Constructor<?> cons = cls.getConstructor(WebDriver.class);
                        Object myTestCode = (Object) cons.newInstance(driver);
                        flag = true;
                        if(arguments==null) {
                            m.invoke(myTestCode);
                        }else{
                            m.invoke(myTestCode,arguments);
                        }
                        break;
                    }catch (Exception e) {
                        throw e;
                    }
                }
            }
        }
        if (!flag) {
            throw new TesboException("'" + tagVal + "' is not found");
        }
    }
}
