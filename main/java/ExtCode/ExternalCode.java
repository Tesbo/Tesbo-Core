package ExtCode;

import org.openqa.selenium.WebDriver;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class ExternalCode  {

    /**
     *
     * @auther : Ankit Mistry
     * @lastModifiedBy:
     * @param annotation
     * @param tagVal
     * @throws Exception
     */
    public void runAllAnnotatedWith(Class<? extends Annotation> annotation, String tagVal, WebDriver driver){
        boolean flag=false;

        try {

            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("ExtTestCode"))
                    .setScanners(new MethodAnnotationsScanner()));
            Set<Method> methods = reflections.getMethodsAnnotatedWith(annotation);

            System.out.println("methods :"+methods);
            for (Method m : methods) {
                if (m.getAnnotation(ExtCode.class) != null) {
                    if (m.getAnnotation(ExtCode.class).value().equals(tagVal)) {

                        System.out.println("Method :"+m.getName());
                        if(!flag) {
                            String className = m.getDeclaringClass().toString().split("class")[1].trim();
                            System.out.println("class name :"+className);
                            Class<?> cls = Class.forName(className);
                            Constructor<?> cons = cls.getConstructor(WebDriver.class);
                            Object myTestCode = (Object) cons.newInstance(driver);
                            //MyTestCode myTestCode=new MyTestCode(driver);
                            flag = true;

                            try {
                                m.invoke(myTestCode);
                            }catch(Exception e)
                            {
                                e.printStackTrace();
                            }


                        }else {
                            //        throw new Exception.TesboException("Multiple tag found :"+tagVal);
                        }
                    }
                }
            }
            if (!flag)
            {

            }
            //          throw new Exception.TesboException("'" + tagVal + "' is not found");
        }
        catch (Exception e){
            e.printStackTrace();

        }
    }

}
