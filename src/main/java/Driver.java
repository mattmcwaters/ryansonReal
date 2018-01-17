import java.applet.Applet;
import java.applet.AudioClip;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.net.URISyntaxException;



public class Driver {

    public static final String ACCOUNT_SID = "AC730e167966900d8cd9b8d740355f1c11";
    public static final String AUTH_TOKEN = "1218234597a65f8e0fd0294a1316878e";


    static String OPEN = "https://cs9.admin.ryerson.ca/cs/csprd/cache/PS_CS_STATUS_OPEN_ICN_1.gif";
    static String CLOSED = "https://cs9.admin.ryerson.ca/cs/csprd/cache/PS_CS_STATUS_CLOSED_ICN_1.gif";
    static String WAITLIST = "https://cs9.admin.ryerson.ca/cs/csprd/cache/PS_CS_STATUS_WAITLIST_ICN_1.gif";

    static String[] newClassCodes = {"7935"};
    static String[] oldClassCodes = {"7928"};

    WebDriver driver = new ChromeDriver();
    WebDriverWait wait = new WebDriverWait(driver, 30);
    static int good = 0;
    /**
     * Open the test website.
     */
    public void openTestSite() {
        driver.navigate().to("https://cas.ryerson.ca/login?service=https%3A%2F%2Fmy.ryerson.ca%2FLogin");
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    /**
     *
     * @param username
     * @param Password
     *
     *            Logins into the website, by entering provided username and
     *            password
     */
    public void login(String username, String Password) throws InterruptedException {
        WebElement userName_editbox = driver.findElement(By.id("username"));
        WebElement password_editbox = driver.findElement(By.id("password"));
        WebElement submit_button = driver.findElement(By.xpath("//input[@class='btn btn-submit btn-block btn-login']"));

        userName_editbox.sendKeys(username);
        password_editbox.sendKeys(Password);
        clk(submit_button);

        WebElement Ramms_button = driver.findElement(By.xpath("//a[@title='RAMSS']"));

        clk(Ramms_button);

        String parentWindowHandler = driver.getWindowHandle(); // Store your parent window


        Set<String> handles = driver.getWindowHandles(); // get all window handles


        driver.switchTo().window((String)handles.toArray()[1]); // switch to popup window
        // perform operations on popup

        System.out.println(driver.getTitle());
         WebElement frame = driver.findElement(By.xpath("//iframe[@id='ptifrmtgtframe']"));
        driver.switchTo().frame(frame);
        WebElement shopping_cart=null;

        //Shopping cart
        Thread.sleep(2000);
        dbl_clk(loopFindXpath("//a[@name='DERIVED_SSS_SCR_SSS_LINK_ANCHOR2']"));

        //Select winter
        clk(loopFindXpath("//input[@tabindex='101']"));

        //Select continue
        clk(loopFindXpath("//input[@name='DERIVED_SSS_SCT_SSR_PB_GO']"));

        int loopCount = 0;
        int totalLoops=100000;
        int waitlistCount=0;
        while(good!=1 || loopCount < totalLoops){
            int enrolled = 0;
            //Find image sources
            WebElement baseTable = driver.findElement(By.className("PSLEVEL1GRID"));
            List<WebElement> imgElements = baseTable.findElements(By.tagName("img"));
            List<String> imgSources = new ArrayList<String>();

            for (WebElement imgEl : imgElements){
                imgSources.add(imgEl.getAttribute("src"));
            }
            int count=0;
            //Loop through images
            for (String img : imgSources){
                if ((img.equals(OPEN)) && ((count==0))){

                    System.out.println("A swap class is open, swapping!");
                    swap(count);

                    enrolled=1;
                    sms("Hi, this is your enrolBot speaking. Good news! If you are getting this message, either ECN or ACC were open so I've gone ahead and done the swap! :)");
                }
//                else if ((img.equals(OPEN)) && ((count==2))){
               if ((img.equals(OPEN))){
                    System.out.println("Class Is open, Enrolling!!");
                    enroll();
                    enrolled=1;
                    //sms("Hi, this is your enrolBot speaking. Good news! You've successfully been enrolled in QMS 202-011(7206) :)");
                }
                else if ((img.equals(OPEN))&& ((count==4))){
                    System.out.println("Test class is open, enrolling!");
                    enroll();
                    enrolled=1;

                }
                else if((img.equals(WAITLIST))){
                    waitlist(count);
                    waitlistCount++;
                    enroll();
                    enrolled=0;
                    //sms("Hi, this is your enrolBot speaking. Good news! You've successfully been waitlisted in a class :)");
                }
                count++;
            }
            //IF WE DIDNT ENROLL OR SWAP -> CLICK ON SHOPPING CART, CHECK WINTER, CLICK CONTINUE;

            loopCount+=1;
            System.out.println("Loop number: "+loopCount+"  |     Successful swaps/enrolls: "+good + "            |         Successful Waitlists: "+waitlistCount);
            Thread.sleep(30000);
            if (enrolled == 0){
                restart();
            }
        }

    }
    static public void sms(String msg){
        Message message = Message
                .creator(new PhoneNumber("+14168307787"),  // to
                        new PhoneNumber("+16474932796"),  // from
                        msg)
                .create();
        Message message2 = Message
                .creator(new PhoneNumber("+14168311622"),  // to
                        new PhoneNumber("+16474932796"),  // from
                        msg)
                .create();

        Message message3 = Message
                .creator(new PhoneNumber("+16475017261"),  // to
                        new PhoneNumber("+16474932796"),  // from
                        msg)
                .create();
    }
    public void waitlist(int count) throws InterruptedException {
        String xpath="//a[@name='P_CLASS_NAME$"+count+"']";
        clk(loopFindXpath(xpath));
        WebElement checkbox = loopFindXpath("//input[@type='checkbox']");
        if ((checkbox.getAttribute("value")).equals("N")){
            clk(checkbox);
        }

        clk(loopFindXpath("//input[@name='DERIVED_CLS_DTL_NEXT_PB']"));


    }

    public void restart() throws InterruptedException {
        clk(loopFindXpath("//a[@href='/psc/csprd/EMPLOYEE/SA/c/SA_LEARNER_SERVICES_2.SSR_SSENRL_CART.GBL?Page=SSR_SSENRL_CART&Action=A']"));
        //Select winter
        clk(loopFindXpath("//input[@tabindex='101']"));

        //Select continue
        clk(loopFindXpath("//input[@name='DERIVED_SSS_SCT_SSR_PB_GO']"));

    }
    public int swap(int classIndex) throws InterruptedException {
        good+=1;
        //clk(loopFindXpath("//a[@href='/psc/csprd/EMPLOYEE/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_CART.GBL?Page=SSR_SSENRL_CART&Action=A&ACAD_CAREER=UGRD&EMPLID=500689017&ENRL_REQUEST_ID=&INSTITUTION=RYERU&STRM=1171']"));
        clk(loopFindXpath("//a[@href='/psc/csprd/EMPLOYEE/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_CART.GBL?Page=SSR_SSENRL_CART&Action=A']"));

        clk(loopFindXpath("//a[@href='/psc/csprd/EMPLOYEE/SA/c/SA_LEARNER_SERVICES.SSR_SSENRL_SWAP.GBL?Page=SSR_SSENRL_SWAP&Action=A&ACAD_CAREER=CAR&EMPLID=500689017&ENRL_REQUEST_ID=&INSTITUTION=INST&STRM=TERM']"));

        //Select winter
        clk(loopFindXpath("//input[@tabindex='103']"));

        //Select continue
        clk(loopFindXpath("//input[@name='DERIVED_SSS_SCT_SSR_PB_GO']"));

        //Select first dropdown
        Select dropdownNew = new Select(loopFindXpath("//select[@name='DERIVED_REGFRM1_DESCR50$225$']"));
        Thread.sleep(2000);
        dropdownNew.selectByValue(oldClassCodes[classIndex]);

        //Select second dropdown
        Select dropdownOld= new Select(loopFindXpath("//select[@name='DERIVED_REGFRM1_SSR_CLASSNAME_35$183$']"));
        Thread.sleep(2000);
        dropdownOld.selectByValue(newClassCodes[classIndex]);

        //Enroll
        clk(loopFindXpath("//input[@name='DERIVED_REGFRM1_SSR_PB_ADDTOLIST1$184$']"));

        //submit
        clk(loopFindXpath("//input[@name='DERIVED_REGFRM1_SSR_PB_SUBMIT']"));

        //Shopping cart
        clk(driver.findElement(By.linkText("Shopping Cart")));

        return 0;

    }
    public int enroll() throws InterruptedException {

        WebElement baseTable = driver.findElement(By.id("SSR_REGFORM_VW$scroll$0"));
        List<WebElement> checkboxes = baseTable.findElements(By.xpath("//input[@type='checkbox']"));


        for (WebElement box : checkboxes){
            clk(box);
        }
        clk(loopFindXpath("//input[@name='DERIVED_REGFRM1_LINK_ADD_ENRL']"));
        clk(loopFindXpath("//input[@name='DERIVED_REGFRM1_SSR_PB_SUBMIT']"));
        clk(loopFindXpath("//input[@name='DERIVED_REGFRM1_SSR_LINK_STARTOVER']"));
        good+=1;
        return 0;
    }
    public WebElement loopFindXpath(String xpath){
        WebElement temp=null;
        while(temp ==null) {
            try {
                temp = driver.findElement(By.xpath(xpath));
            } catch (Exception NoSuchElementException) {
            }
        }
        return temp;
    }

    public void quit(){
        driver.quit();
    }

    public void clk(WebElement el) throws InterruptedException {
        el.click();
        jsWait(wait);
        Thread.sleep(2000);

    }

    public void dbl_clk(WebElement el) throws InterruptedException {
        el.click();
        Thread.sleep(100);
        el.click();
        jsWait(wait);
        Thread.sleep(4000);

    }

    public void jsWait(WebDriverWait wait){

        wait.until( new ExpectedCondition<Boolean>() {
                        public Boolean apply(WebDriver driver) {
                            return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                        }
                    }
        );
    }

    /**
     * Saves the screenshot
     *
     * @throws IOException
     */
    public void saveScreenshot() throws IOException {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("screenshot.png"));
    }

    public void closeBrowser() {
        driver.close();
    }

    public static void main(String[] args) throws InterruptedException {
        Boolean restart=false;
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        //sms("Hi there! bad news... I've deleted all your classes by mistake... Just kidding! You are now enrolled in QMS :).");

        //sms("Hi, this is your enrolBot speaking. It seems my programmer has taught me how to send SMS... If I sucessfully enrol you in anything, you will get a text message! :)");

        do {
            System.setProperty("webdriver.chrome.driver", "/Users/Matt/Desktop/chromedriver.exe");
            Driver webSrcapper = new Driver();
            try {
                //System.setProperty("webdriver.ie.driver", "/Users/Matt/Desktop/IEDriverServer.exe");
                webSrcapper.openTestSite();
		//REMOVED THIS LINE BECAUSE IT CONTAINED MY BROTHER'S LOGIN INFORMATION
                //webSrcapper.login("USERNAME", "PASSWORD");
                webSrcapper.quit();
            } catch (Exception e) {
                System.out.println("Caught an exception; Restarting!");
                System.out.println(e.getMessage());

                webSrcapper.quit();
                //Thread.sleep(1800000);
                good = 0;
                restart = true;
            }
        }while(restart);

    }
}