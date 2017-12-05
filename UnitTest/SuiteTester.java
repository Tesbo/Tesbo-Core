import Framework.SuiteParser;
import org.junit.Assert;
import org.junit.Test;

public class SuiteTester {



    @Test
    public void getSuiteName()
    {
        SuiteParser parser = new SuiteParser();

       String loginFileData =  parser.readSuiteFile("login").toString();
        System.out.println(loginFileData);
        //Assert.assertTrue(loginFileData.contains("SuiteName: Login"));


    }




}
