import com.google.gson.JsonArray;
import org.apache.xalan.lib.sql.ObjectArray;
import org.junit.Assert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;


public class SuiteTester {



    @Test
    public void VerifyGetSuitesForSuiteExist() throws IOException {
        SuiteParser parser = new SuiteParser();
        JSONArray listOfsuite= parser.getSuites("./test/java/Suites");
        boolean flag=false;
        for(Object objSuite: listOfsuite)
        {
            if(objSuite.toString().contains("Dashboard"))
            {
                flag=true;
            }
        }

        Assert.assertTrue(flag);


    }

    @Test
    public void VerifyGetSuitesForWrongPath() throws IOException {
        SuiteParser parser = new SuiteParser();
        JSONArray listOfsuite= parser.getSuites("./test/java/Suitess");
        //When set Wrong path or path not found throw error


    }

    @Test
    public void VerifyGetSuitesForOnlySuiteFileFound() throws IOException {
        SuiteParser parser = new SuiteParser();
        JSONArray listOfsuite= parser.getSuites("./test/java/Suites");
        // System.out.println("Suite Result: "+listOfsuite);
        boolean flag=false;
        for(Object objSuite: listOfsuite)
        {
            if(!objSuite.toString().contains(".suite"))
            {
                flag=true;
            }
        }
        //Only get .suite File
        Assert.assertTrue(!flag);

    }

    @Test
    public void VerifyReadSuiteFileForEmptyFile()
    {
        SuiteParser parser = new SuiteParser();
        StringBuffer suiteData= parser.readSuiteFile("EmptyFile.suite");
        boolean flag=false;
        if(suiteData.toString().contentEquals(""))
        {
            flag=true;

        }
        //When File is Empty throw error or valid massage.
        Assert.assertTrue(!flag);
    }

    @Test
    public void VerifyReadSuiteFileForGetFileData()
    {
        SuiteParser parser = new SuiteParser();
        StringBuffer suiteData= parser.readSuiteFile("Dashboard.suite");

        Assert.assertNotNull(suiteData);
    }

    @Test
    public void VerifyReadSuiteFileForOtherExtensionFile()
    {
        SuiteParser parser = new SuiteParser();
        StringBuffer suiteData= parser.readSuiteFile("test.java");

        //should not accept other extension file
        Assert.assertEquals(suiteData.toString()," Enter only suite file");
    }

    @Test
    public void VerifyReadSuiteFileForFileNotExist()
    {
        SuiteParser parser = new SuiteParser();
        StringBuffer suiteData = parser.readSuiteFile("EmptyFil.suite");
        //Set massage like "File Not Exist"
        Assert.assertEquals(suiteData.toString(),"File Not Exist");
    }


    //By Two Arguments
    @Test
    public void VerifyGetTestNameByTagForValidValidAndUpperLowerAndCamelCase()
    {
        SuiteParser parser = new SuiteParser();
        StringBuffer suiteData = parser.readSuiteFile("Dashboard.suite");
        JSONArray testName= parser.getTestNameByTag("GtU",suiteData);
        int testCount=0;
        for(Object test:testName)
        {

            if(test.toString().equalsIgnoreCase("Verify Task List Grid is Display") || test.toString().equalsIgnoreCase("Verify Number of Registered User is Display"))
                testCount++;
        }

        // Not Get all test name that has "gtu" tag from suite
        Assert.assertEquals(testCount,2);

    }

    @Test
    public void VerifyGetTestNameByTagForWrongTagName()
    {
        SuiteParser parser = new SuiteParser();
        StringBuffer suiteData = parser.readSuiteFile("Dashboard.suite");
        JSONArray testName= parser.getTestNameByTag("abc",suiteData);

        Assert.assertNull(testName);

    }

    @Test
    public void VerifyGetTestNameByTagForEmptyBufferString()
    {
        SuiteParser parser = new SuiteParser();
        StringBuffer suiteData = parser.readSuiteFile("EmptyFILE.suite");
        JSONArray testName= parser.getTestNameByTag("gtu",suiteData);
        Assert.assertNull(testName);

    }

    @Test
    public void VerifyGetTestNameByTagForNullBufferString()
    {
        SuiteParser parser = new SuiteParser();
        JSONArray testName= parser.getTestNameByTag("gtu",null);

        //When pass null StringBuffer Set valid massage for that
        Assert.assertEquals(testName.toString(),"String Buffer is null");

    }

    @Test
    public void VerifyGetTestNameByTagForVelidTag() throws Exception {
        SuiteParser parser = new SuiteParser();
        JSONObject testName= parser.getTestNameByTag("gtu");

        int testCount=0;
        for(Object suite:testName.keySet())
        {
            for(Object test:((JSONArray) testName.get(suite)))
            {
                if(test.toString().equalsIgnoreCase("Verify Task List Grid is Display") || test.toString().equalsIgnoreCase("Verify Number of Registered User is Display"))
                    testCount++;
            }
        }

        // Not Get all test name that has "gtu" tag from suite
        Assert.assertEquals(testCount,3);
    }

    @Test
    public void VerifyGetTestNameByTagForWrongTag() throws Exception {
        SuiteParser parser = new SuiteParser();
        JSONObject testName= parser.getTestNameByTag("aa");
        System.out.println(testName);

        //When Result not found then pass JSONObject Null
       Assert.assertNull(testName);
    }

    @Test
    public void VerifyGetTestStepBySuiteAndTestCaseNameForVelidParameter() throws Exception {
        SuiteParser parser = new SuiteParser();
        int totalStep=0;
        JSONObject suteNameAndTagName= parser.getTestNameByTag("gtu");
        System.out.println(suteNameAndTagName);

        for(Object suite:suteNameAndTagName.keySet())
        {
            for(Object test:((JSONArray) suteNameAndTagName.get(suite)))
            {
                JSONArray testSteps= parser.getTestStepBySuiteandTestCaseName(suite.toString(),test.toString());
                    if(test.toString().equalsIgnoreCase("Verify Number of Registered User is Display"))
                        totalStep+=testSteps.size();

                    if(test.toString().equalsIgnoreCase("Verify Task List Grid is Display"))
                        totalStep+=testSteps.size();

            }

        }
        // getTestNameByTag() method is not get all test name who has GTU tag
        Assert.assertEquals(totalStep,7);
    }

    @Test
    public void VerifyGetTestStepBySuiteAndTestCaseNameForDuplicateTest() throws Exception {
        SuiteParser parser = new SuiteParser();
        int totalStep=0;
        JSONObject suteNameAndTagName= parser.getTestNameByTag("DubTest");
        System.out.println(suteNameAndTagName);

        for(Object suite:suteNameAndTagName.keySet())
        {
            for(Object test:((JSONArray) suteNameAndTagName.get(suite)))
            {
                if(test.toString().equalsIgnoreCase("Verify Registered User is Display"))
                    totalStep++;

            }


        }
        // if find same name of test in one suite then send validation like "Duplicate test found"
        Assert.assertEquals(totalStep,1);
    }

    @Test
    public void VerifyGetTestStepBySuiteAndTestCaseNameForPassBlankParameter() throws Exception {
        SuiteParser parser = new SuiteParser();
        int totalStep=0;
        JSONArray testSteps= parser.getTestStepBySuiteandTestCaseName("Dashboard.suite","");
        System.out.println(testSteps);

        //When pass blank parameter should display warning massage
        Assert.assertEquals(testSteps.toString(),"Please enter 'Tag' name");
    }

    @Test
    public void VerifyGetTestStepBySuiteAndTestCaseNameForParameterAreCaseSensitive() throws Exception {
        SuiteParser parser = new SuiteParser();
        int totalStep=0;

        //Test Name parameter value is case sensitive
        JSONArray testSteps= parser.getTestStepBySuiteandTestCaseName("Dashboard.suite","verify Task list Grid is Display");
        totalStep+=testSteps.size();

        Assert.assertEquals(totalStep,3);
    }


    @Test
    public void VerifyGetGroupTestStepBySuiteandTestCaseNameForPassVelidParameter()
    {
        SuiteParser parser = new SuiteParser();
        int totalStep=0;
        JSONArray testSteps= parser.getGroupTestStepBySuiteandTestCaseName("Dashboard.suite","login");
        totalStep+=testSteps.size();
        Assert.assertEquals(totalStep,4);
    }

    @Test
    public void VerifyGetGroupTestStepBySuiteAndTestCaseNameForPassBlankParameter()
    {
        SuiteParser parser = new SuiteParser();
        int totalStep=0;
        JSONArray testSteps= parser.getGroupTestStepBySuiteandTestCaseName("Dashboard.suite","");

        //When pass blank parameter should display warning massage
        Assert.assertEquals(testSteps.toString(),"Please enter 'Group' name");
    }

    @Test
    public void VerifyGetGroupTestStepBySuiteandTestCaseNameForParameterAreCaseSensitiv()
    {
        SuiteParser parser = new SuiteParser();
        int totalStep=0;
        JSONArray testSteps= parser.getGroupTestStepBySuiteandTestCaseName("Dashboard.suite","LOgin");
        totalStep+=testSteps.size();
        Assert.assertEquals(totalStep,4);
    }

    @Test
    public void VerifyGetGroupNameForGetCorrectResult() throws IOException {
        SuiteParser parser = new SuiteParser();
        int totalGroups=0;
        JSONArray suiteName= parser.getSuites("./test/java/Suites");
        for(Object suite : suiteName)
        {
            String[] sName=suite.toString().split("\\\\");
            JSONArray groupName= parser.getGroupName(parser.readSuiteFile(sName[4]));
            if(suite.toString().contains("Dashboard.suite") || suite.toString().contains("login.suite"))
                for(Object group:groupName)
                {
                    if(group.toString().equalsIgnoreCase("dashboard1") || group.toString().equalsIgnoreCase("dashboard") || group.toString().equalsIgnoreCase("login") || group.toString().equalsIgnoreCase("login1"))
                    {
                        totalGroups++;
                    }
                }
        }

        Assert.assertEquals(totalGroups,4);
    }

    @Test
    public void VerifyGetGroupNameForDuplicateGroupName(){
        SuiteParser parser = new SuiteParser();
        JSONArray groupName= parser.getGroupName(parser.readSuiteFile("UserDepartment.suite"));
        //System.out.println(groupName);
        // If duplicate group name is found then stop the execution and pass massage like "Found Duplicate Group Name".
        Assert.assertEquals(groupName.toString(),"Found Duplicate Group Name");
    }

    @Test
    public void VerifyGetGroupNameForNullSuiteFile(){
        SuiteParser parser = new SuiteParser();
        JSONArray groupName= parser.getGroupName(parser.readSuiteFile("EmptyFile.suite"));
        Assert.assertEquals(groupName,null);
    }

    @Test
    public void VerifyGetGroupNameForNoGroupExistInSuite(){
        SuiteParser parser = new SuiteParser();
        JSONArray groupName= parser.getGroupName(parser.readSuiteFile("Navigation.suite"));
        Assert.assertEquals(groupName,null);
    }

    @Test
    public void VerifyGetTestNameBySuiteForGetAllTestNameFromSuite() throws Exception {
        SuiteParser parser = new SuiteParser();
        int count=0;
        JSONObject testName= parser.getTestNameBySuite("UserDepartment");
        for(Object suite:testName.keySet())
        {
            for(Object test:((JSONArray) testName.get(suite)))
            {
                if(test.toString().equalsIgnoreCase("Create a new Department") || test.toString().equalsIgnoreCase("Verify Department is Created"))
                    count++;
            }
        }
        Assert.assertEquals(count,2);
    }

    @Test
    public void VerifyGetTestNameBySuiteForDuplicateTestNameExistInSuite() throws Exception {
        SuiteParser parser = new SuiteParser();
        JSONObject testName= parser.getTestNameBySuite("dashboard");

        // If duplicate test name is found then stop the execution and pass massage like "Found Duplicate Test Name".
        Assert.assertEquals(testName.toString(),"Found Duplicate Test Name");
    }

    @Test
    public void VerifyGetTestNameBySuiteForBlankString() throws Exception {
        SuiteParser parser = new SuiteParser();
        int count=0;
        JSONObject testName= parser.getTestNameBySuite("");

        //When pass blank string then get warning massage like "Enter valid suite name" ".
        Assert.assertEquals(testName.toString(),"Enter valid suite name");
    }

    @Test
    public void VerifyGetGroupTestStepBySuiteandTestCaseNameForDuplicateGroup()
    {
        SuiteParser parser = new SuiteParser();
        JSONArray testSteps= parser.getGroupTestStepBySuiteandTestCaseName("UserDepartment.suite","login1");

        Assert.assertEquals(testSteps,"Duplicate Group Found");
    }


}
