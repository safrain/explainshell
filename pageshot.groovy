import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;

int      DISPLAY_NUMBER  = 99;
String   XVFB            = "/usr/bin/Xvfb";
String   XVFB_COMMAND    = XVFB + " :" + DISPLAY_NUMBER;
String   URL             = "file:///home/safrain/code/explainshell/out.html";
String   RESULT_FILENAME = "/tmp/screenshot.png";

def tmpFile = File.createTempFile('picshot','')
IOUtils.copy(System.in, new FileOutputStream(tmpFile))
def url = "file:///${tmpFile}"
Process p = Runtime.getRuntime().exec(XVFB_COMMAND);
FirefoxBinary firefox = new FirefoxBinary();
firefox.setEnvironmentProperty("DISPLAY", ":" + DISPLAY_NUMBER);
WebDriver driver = new FirefoxDriver(firefox, null);
driver.manage().window().size = new org.openqa.selenium.Dimension(400, 100)
driver.get(url);
File scrFile = ( (TakesScreenshot) driver ).getScreenshotAs(OutputType.FILE)
IOUtils.copy(new FileInputStream(scrFile), System.out)

driver.close();
p.destroy();
