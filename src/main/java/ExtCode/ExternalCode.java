package ExtCode;

import framework.GetConfiguration;
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
    public void runAllAnnotatedWith(Class<? extends Annotation> annotation, String step, WebDriver driver) throws Exception {
        boolean flag=false;
        GetConfiguration getConfiguration=new GetConfiguration();
        File file=new File(getConfiguration.getExtCodeDirectory());

        String tagVal=null;
        try {
            tagVal=step.toString().split(":")[1].trim();
        } catch (Exception e) {
            throw new TesboException("ExtCode step has no value");
        }

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(file.getName()))
                .setScanners(new MethodAnnotationsScanner()));
        Set<Method> methods = reflections.getMethodsAnnotatedWith(annotation);
        for (Method m : methods) {
            if (m.getAnnotation(ExtCode.class) != null) {
                if (m.getAnnotation(ExtCode.class).value().equals(tagVal)) {
                    String className = m.getDeclaringClass().toString().split("class")[1].trim();
                    try {
                        Class<?> cls = Class.forName(className);
                        Constructor<?> cons = cls.getConstructor(WebDriver.class);
                        Object myTestCode = (Object) cons.newInstance(driver);
                        flag = true;
                        m.invoke(myTestCode);
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
