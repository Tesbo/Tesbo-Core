package framework;

import selenium.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import logger.TesboLogger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

public class VerifyParser {

    private static final Logger log = LogManager.getLogger(VerifyParser.class);

    String toContainText="\"> to contain: <\"";
    String butWasText="\"> but was:<\"";
    String attributeOrCssText="attributeOrCSS";
    String toBeEqualText="\"> to be equal to: <\"";
    String notToBeEqualText="\"> not to be equal to:<\"";
    String testsFileNameText="testsFileName";
    String comparisonFailureText="ComparisonFailure: expected:<\"";
    String expectingText="Expecting:<\"";
    String ignoreCaseText="ignore case";
    CommonMethods commonMethods=new CommonMethods();
    Commands cmd = new Commands();
    GetLocator locator = new GetLocator();
    StepParser stepParser = new StepParser();
    TesboLogger tesboLogger = new TesboLogger();


    /**
     *
     * @param driver
     * @param test
     * @param verify
     */
    public void parseVerify(WebDriver driver, JSONObject test, String verify) {
        boolean flag=false;
        if(!(verify.contains("{") && verify.contains("}"))) {
            String verifyStepLog=verify.replace("@", "");
            tesboLogger.stepLog(verifyStepLog);
            log.info(verifyStepLog);
        }
        WebElement element=null;
        String textOfStep=null;
        if(verify.contains("@")) {
            element = cmd.findElement(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), stepParser.parseElementName(verify)));
        }

        textOfStep = getVerifyTextFromStep(verify,test);

        //Is list size
        if (verify.toLowerCase().contains(" size ")) {
            flag=verifyListOfElementSize(driver,test,verify);

        }

        //Text verification.
        if (verify.toLowerCase().contains("text")) {
            flag=verifyElementTextValue(element,textOfStep,verify);
        }

        //Is displayed
        if (verify.toLowerCase().contains("displayed") || verify.toLowerCase().contains("present")) {
            flag=verifyElementIsDisplayed(element);

        }

        //Is Visible
        if (verify.toLowerCase().contains("visible")) {
            flag=verifyElementIsVisible(element);

        }

        if (verify.toLowerCase().contains("page title")) {
            flag=verifyPageTitle(driver,textOfStep,verify);
        }
        if (verify.toLowerCase().contains("get cookies")) {

            flag = verifyBrowserCookies(driver,test, verify);
        }
        if (verify.toLowerCase().contains("current url")) {
            flag=verifyCurrentURL(driver,test,verify);
        }

        // verify attribute value
        if(verify.toLowerCase().contains(" get attribute ")){
            flag=verifyElementAttribute(driver,test,verify);
        }

        // verify CSS value
        if(verify.toLowerCase().contains(" get css value ")){
            flag=verifyCssValue(driver,test,verify);
        }

        if(!flag) {
            commonMethods.throwTesboException("'"+verify+"' Step is not define properly.",log);
        }
        tesboLogger.testPassed("Passed");
    }

    /**
     *
     * @param verify
     * @param test
     * @return
     */
    public String getVerifyTextFromStep(String verify,JSONObject test){
        if(verify.contains("'") || (verify.contains("{") && verify.contains("}"))) {
            return stepParser.parseTextToEnter(test, verify);
        }
        return null;
    }

    /**
     *
     * @param element
     * @param textOfStep
     * @param verify
     * @return
     */
    public boolean verifyElementTextValue(WebElement element,String textOfStep,String verify){
        boolean flag=false;

        //equal
        if (verify.toLowerCase().contains("not equal")) {
            flag=verifyTextNotEqual(element,textOfStep);
        }
        else if(verify.toLowerCase().contains("equal")) {
            flag=verifyTextIsEqual(element,textOfStep,verify);
        }
        //contains
        else if (verify.toLowerCase().contains("contains")) {
            flag=verifyTextContains(element,textOfStep,verify);
        }
        else if(verify.toLowerCase().contains("start with")){
            flag=verifyTextStartWith(element,textOfStep);
        }
        else if(verify.toLowerCase().contains("end with")){
            flag=verifyTextEndWith(element,textOfStep);
        }
        else if(verify.toLowerCase().contains("number")){
            flag=verifyElementTextIsNumber(element);
        }
        else if(verify.toLowerCase().contains("alphanumeric")){
            flag=verifyElementTextIsAlphanumeric(element);
        }

        return flag;
    }

    /**
     *
     * @param driver
     * @param test
     * @param verify
     * @return
     */
    public boolean verifyListOfElementSize(WebDriver driver, JSONObject test,String verify){

        /*
         * Verify: @element has size of '10'
         */

        if(cmd.findElements(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), stepParser.parseElementName(verify))).size()!= Integer.parseInt(stepParser.parseTextToEnter(test,verify))) {
            commonMethods.throwAssertException("Element list size not equal to '"+stepParser.parseTextToEnter(test,verify)+"'",log);
        }

        return true;
    }

    /**
     *
     * @param element
     * @param textOfStep
     * @return
     */
    public boolean verifyTextNotEqual(WebElement element,String textOfStep){
        /*
         * Verify: @element text is not equal 'Text'
         */
        if(element.getText().equals(textOfStep)) {
            commonMethods.throwAssertException(expectingText+element.getText()+notToBeEqualText+textOfStep+"\">",log);
        }
        return true;
    }

    /**
     *
     * @param element
     * @param textOfStep
     * @param verify
     * @return
     */
    public boolean verifyTextIsEqual(WebElement element,String textOfStep,String verify){
        boolean flag=false;

        /*
         * Verify: @element text is equal ignore case 'Text'
         */
        if (verify.toLowerCase().contains(ignoreCaseText)) {
            if(!element.getText().equalsIgnoreCase(textOfStep)) {
                commonMethods.throwAssertException(expectingText+element.getText()+toBeEqualText+textOfStep+"\"> ignoring case considerations",log);
            }
            flag=true;
        }
        else {
            /*
             * Verify: @element text is equal 'Text'
             */
            if(!element.getText().equals(textOfStep)) {
                commonMethods.throwAssertException(comparisonFailureText+element.getText()+butWasText+textOfStep+"\">",log);
            }
            flag=true;
        }

        return flag;
    }

    /**
     *
     * @param element
     * @param textOfStep
     * @param verify
     * @return
     */
    public boolean verifyTextContains(WebElement element,String textOfStep,String verify){
        boolean flag=false;

        /*
         * Verify: @element text is contains ignore case 'Text'.
         */
        if (verify.toLowerCase().contains(ignoreCaseText)) {
            if(!element.getText().toLowerCase().contains(textOfStep.toLowerCase())) {
                commonMethods.throwAssertException(expectingText+element.getText()+toContainText+textOfStep+"\"> (ignoring case)",log);
            }
            flag=true;
        }
        else {
            /*
             * Verify: @element text is contains 'Text'.
             */
            if(!element.getText().contains(textOfStep)) {
                commonMethods.throwAssertException(expectingText+element.getText()+toContainText+textOfStep+"\">",log);
            }
            flag=true;
        }

        return flag;
    }

    /**
     *
     * @param element
     * @param textOfStep
     * @return
     */
    public boolean verifyTextStartWith(WebElement element,String textOfStep){
        /*
         * Verify: @element text is start with 'Text'.
         */

        if(!element.getText().startsWith(textOfStep)){
            commonMethods.throwAssertException(expectingText+element.getText()+"\"> to start with: <\""+textOfStep+"\">",log);
        }

        return true;
    }

    /**
     *
     * @param element
     * @param textOfStep
     * @return
     */
    public boolean verifyTextEndWith(WebElement element,String textOfStep){
        /*
         * Verify: @element text is end with 'Text'.
         */

        if(!element.getText().endsWith(textOfStep)){
            commonMethods.throwAssertException(expectingText+element.getText()+"\"> to end with: <\""+textOfStep+"\">",log);
        }
        return true;
    }

    /**
     *
     * @param element
     * @return
     */
    public boolean verifyElementTextIsNumber(WebElement element){
        /*
         * Verify: @element text should be number.
         */
        if(!isNumeric(element.getText())) {
            commonMethods.throwAssertException("ComparisonFailure: expected:<[tru]e> but was:<[fals]e>",log);
        }
        return true;
    }

    /**
     *
     * @param element
     * @return
     */
    public boolean verifyElementTextIsAlphanumeric(WebElement element){
        /*
         * Verify: @element text should be Alphanumeric.
         */
        if(!element.getText().matches("[a-zA-Z0-9 ]+")) {
            commonMethods.throwAssertException("AlphanumericComparisonFailure: expected:<[tru]e> but was:<[fals]e>",log);
        }
        return true;
    }

    /**
     *
     * @param element
     * @return
     */
    public boolean verifyElementIsDisplayed(WebElement element){

        /*
         * Verify: @element is displayed
         * Verify: @element should displayed
         * Verify: @element is present
         */
        if(!element.isDisplayed()) {
            commonMethods.throwAssertException("Element is not displayed",log);
        }

        return true;
    }

    /**
     *
     * @param element
     * @return
     */
    public boolean verifyElementIsVisible(WebElement element){
        /*
         * Verify: @element is Visible
         */
        boolean isVisibleInViewport=isVisibleInViewport(element);
        if(!isVisibleInViewport) {
            commonMethods.throwAssertException("Element is not Visible",log);
        }

        return true;
    }

    /**
     *
     * @param driver
     * @param textOfStep
     * @param verify
     * @return
     */
    public boolean verifyPageTitle(WebDriver driver,String textOfStep,String verify){
        boolean flag=false;
        //equal
        if (verify.toLowerCase().contains("equal")) {
            /*
             * Verify : Page Title is equal to ignore case 'Google search'
             */
            if (verify.toLowerCase().contains(ignoreCaseText)) {
                if(!driver.getTitle().equalsIgnoreCase(textOfStep)) {
                    commonMethods.throwAssertException(comparisonFailureText+driver.getTitle()+butWasText+textOfStep+"\">",log);
                }
                flag=true;
            }
            /*
             * Verify : Page Title is equal to 'Google search'
             */
            else {
                if(!driver.getTitle().equals(textOfStep)) {
                    commonMethods.throwAssertException(comparisonFailureText+driver.getTitle()+butWasText+textOfStep+"\">",log);
                }
                flag=true;
            }
        }

        return flag;
    }

    /**
     *
     * @param driver
     * @param test
     * @param verify
     * @return
     */
    public boolean verifyCurrentURL(WebDriver driver,JSONObject test,String verify){
        boolean flag=false;
        //equal
        if(verify.toLowerCase().contains("is equal to")) {

            /*
             * Verify: current url is equal to 'https://tesbo10.atlassian.net'
             */
            if (!cmd.getCurrentUrl(driver,stepParser.parseTextToEnter(test, verify))) {
                commonMethods.throwAssertException("current url is not match with '" + stepParser.parseTextToEnter(test, verify) + "'",log);
            }
            flag = true;
        }
        if(verify.toLowerCase().contains("is contains")) {

            /*
             * Verify: current url is contains 'https://tesbo10.atlassian.net'
             */
            if (!cmd.verifyCurrentUrlContains(driver,stepParser.parseTextToEnter(test, verify))) {
                commonMethods.throwAssertException("current url contains is not match with '" + stepParser.parseTextToEnter(test, verify) + "'",log);
            }
            flag = true;
        }
        return flag;
    }

    /**
     *
     * @param driver
     * @param test
     * @param verify
     * @return
     */
    public boolean verifyBrowserCookies(WebDriver driver,JSONObject test,String verify){
        boolean flag=false;
        if(verify.toLowerCase().contains("check") && verify.toLowerCase().contains("is available")) {

            /*
             * Step: Get cookies and check 'any cookie name' is available
             */
            if (!cmd.isCookieAvailable(driver, stepParser.parseTextToEnter(test, verify))) {
                commonMethods.throwAssertException("'" + stepParser.parseTextToEnter(test, verify) + "' cookie is not found",log);
            }
            flag = true;
        }
        return flag;
    }

    /**
     *
     * @param driver
     * @param test
     * @param verify
     * @return
     */
    public boolean verifyElementAttribute(WebDriver driver,JSONObject test,String verify){
        boolean flag=false;

        WebElement element=cmd.findElement(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), stepParser.parseElementName(verify)));

        if(element.getAttribute(getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText))==null){
            commonMethods.throwAssertException("'"+getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText)+"' attribute is not fount.",log);
        }
        String attributeValue=element.getAttribute(getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText));
        String verifyText=null;
        if(verify.contains("{") && verify.contains("}")){
            String[] stepWord=verify.split(" ");
            for(String word:stepWord){
                if(word.equalsIgnoreCase("attribute")){
                    verifyText=stepParser.parseTextToEnter(test, verify.replace(" "+word+" "," "));
                    break;
                }
            }
        }
        else{
            verifyText=getAttributeAndCSSNameAndVerifyText(verify,"text") ;
        }

        if(verify.toLowerCase().contains(" not equal to ")){
            flag=verifyElementAttributeValueNotEqualToGivenText(attributeValue,verifyText);
        }
        else if(verify.toLowerCase().contains(" is equal to ")){

            flag=verifyElementAttributeValueEqualToGivenText(attributeValue,verifyText,verify);
        }
        else if(verify.toLowerCase().contains(" contains ")){
            flag=verifyElementAttributeValueContainsIsGivenText(attributeValue,verifyText);
        }


        return flag;
    }


    /**
     *
     * @param attributeValue
     * @param verifyText
     * @param verify
     * @return
     */

    public boolean verifyElementAttributeValueEqualToGivenText(String attributeValue,String verifyText,String verify){
        boolean flag=false;
        if(verify.toLowerCase().contains(" is equal to ignore case ")){
            /*
             * Verify: Get attribute 'attribute name' of @element is equal to ignore case 'attribute value'
             * */
            if(!attributeValue.equalsIgnoreCase(verifyText)){
                commonMethods.throwAssertException(expectingText+attributeValue+toBeEqualText+verifyText+"\">",log);
            }
            flag=true;
        }
        else{
            /*
             * Verify: Get attribute 'attribute name' of @element is equal to 'attribute value'
             * */
            if(!attributeValue.equals(verifyText)){
                commonMethods.throwAssertException(comparisonFailureText+attributeValue+butWasText+verifyText+"\">",log);
            }
            flag=true;
        }
        return flag;
    }

    /**
     *
     * @param attributeValue
     * @param verifyText
     * @return
     */
     public boolean verifyElementAttributeValueNotEqualToGivenText(String attributeValue,String verifyText){
        /*
         * Verify: Get attribute 'attribute name' of @element is not equal to 'attribute value'
         * */
        if(attributeValue.equals(verifyText)){
            commonMethods.throwAssertException(expectingText+attributeValue+notToBeEqualText+verifyText+"\">",log);
        }
        return true;
    }

    /**
     *
     * @param attributeValue
     * @param verifyText
     * @return
     */


     public boolean verifyElementAttributeValueContainsIsGivenText(String attributeValue,String verifyText){
        /*
         * Verify: Get attribute 'attribute name' of @element contains is 'attribute value'
         * */
        if(!attributeValue.contains(verifyText)){
            commonMethods.throwAssertException(expectingText+attributeValue+toContainText+verifyText+"\">",log);
        }
        return true;
    }

    /**
     *
     * @param driver
     * @param test
     * @param verify
     * @return
     */
    public boolean verifyCssValue(WebDriver driver,JSONObject test,String verify){
        boolean flag=false;
        WebElement element=cmd.findElement(driver, locator.getLocatorValue(test.get(testsFileNameText).toString(), stepParser.parseElementName(verify)));

        if(element.getCssValue(getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText))==null){
            commonMethods.throwAssertException("'"+getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText)+"' CSS attribute is not fount.",log);
        }
        String cssValue=element.getCssValue(getAttributeAndCSSNameAndVerifyText(verify,attributeOrCssText));
        String verifyText=null;
        if(verify.contains("{") && verify.contains("}")){
            String[] stepWord=verify.split(" ");
            for(String word:stepWord){
                if(word.equalsIgnoreCase("css")){
                    verifyText=stepParser.parseTextToEnter(test, verify.replace(" "+word+" "," "));
                    break;
                }
            }
        }
        else{
            verifyText=getAttributeAndCSSNameAndVerifyText(verify,"text") ;
        }

        if(verify.toLowerCase().contains(" not equal to ")){
            flag=verifyElementCssValueNotEqualToGivenText(cssValue,verifyText);
        }
        else if(verify.toLowerCase().contains(" is equal to ")){

            flag=verifyElementCssValueEqualToGivenText(cssValue,verifyText,verify);
        }
        else if(verify.toLowerCase().contains(" contains ")){
            flag=verifyElementCssValueContainsIsGivenText(cssValue,verifyText);
        }
        return flag;
    }

    /**
     *
     * @param cssValue
     * @param verifyText
     * @param verify
     * @return
     */
    public boolean verifyElementCssValueEqualToGivenText(String cssValue,String verifyText,String verify){
        boolean flag=false;
        if(verify.toLowerCase().contains(" is equal to ignore case ")){
            /*
             * Verify: Get css value 'css name' of @element is equal to ignore case 'css value'
             * */
            if(!cssValue.equalsIgnoreCase(verifyText)){
                commonMethods.throwAssertException(expectingText+cssValue+toBeEqualText+verifyText+"\">",log);
            }
            flag=true;
        }
        else{
            if(!cssValue.equals(verifyText)){
                /*
                 * Verify: Get css value 'css name' of @element is equal to 'css value'
                 * */
                commonMethods.throwAssertException(comparisonFailureText+cssValue+butWasText+verifyText+"\">",log);
            }
            flag=true;
        }
        return flag;
    }

    /**
     *
     * @param cssValue
     * @param verifyText
     * @return
     */
    public boolean verifyElementCssValueNotEqualToGivenText(String cssValue,String verifyText){
        /*
         * Verify: Get css value 'css name' of @element is not equal to 'css value'
         * */
        if(cssValue.equals(verifyText)){
            commonMethods.throwAssertException(expectingText+cssValue+notToBeEqualText+verifyText+"\">",log);
        }
        return true;
    }

    /**
     *
     * @param cssValue
     * @param verifyText
     * @return
     */
    public boolean verifyElementCssValueContainsIsGivenText(String cssValue,String verifyText){
        /*
         * Verify: Get css value 'css name' of @element contains is 'css value'
         * */
        if(!cssValue.contains(verifyText)){
            commonMethods.throwAssertException(expectingText+cssValue+toContainText+verifyText+"\">",log);
        }
        return true;
    }

    /**
     *
     * @param strNum
     * @return
     */
    public static boolean isNumeric(String strNum) {
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param element
     * @return
     */
    public static Boolean isVisibleInViewport(WebElement element) {
        WebDriver driver = ((RemoteWebElement)element).getWrappedDriver();

        return (Boolean)((JavascriptExecutor)driver).executeScript(
                "var elem = arguments[0],                 " +
                        "  box = elem.getBoundingClientRect(),    " +
                        "  cx = box.left + box.width / 2,         " +
                        "  cy = box.top + box.height / 2,         " +
                        "  e = document.elementFromPoint(cx, cy); " +
                        "for (; e; e = e.parentElement) {         " +
                        "  if (e === elem)                        " +
                        "    return true;                         " +
                        "}                                        " +
                        "return false;                            "
                , element);
    }

    /**
     *
     * @param step
     * @param attributeOrText
     * @return
     */
    public String getAttributeAndCSSNameAndVerifyText(String step, String attributeOrText){
        String attributeNameOrText=null;
        String[] stepsWord=step.split("'");

        if(attributeOrText.equals(attributeOrCssText)){
            attributeNameOrText= stepsWord[1];
        }
        else if(attributeOrText.equals("text")){
            attributeNameOrText= stepsWord[3];
        }
        return attributeNameOrText;
    }
}

