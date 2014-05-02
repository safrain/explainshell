import java.io.File;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;


def commands = [
	['test1', 'ps aux'],
	['test2', 'ssh -I'],
	['test3', 'netstat -ntpl'],
]

def TEMPLATE = new groovy.text.SimpleTemplateEngine().createTemplate(new File('template.html').getText('UTF-8'))
def p = '/usr/bin/Xvfb :99'.execute();
def firefox = new FirefoxBinary();
firefox.setEnvironmentProperty('DISPLAY', ':99')
def driver = new FirefoxDriver(firefox, null)
driver.manage().window().size = new org.openqa.selenium.Dimension(400, 100)

def tmp = File.createTempFile('explainImage','')
tmp.deleteOnExit()

commands.each {
	def ep = "python explain.py".execute()
	ep.withWriter { w-> w << it[1] }
	new groovy.json.JsonSlurper().parseText(ep.in.text).with { json ->
		TEMPLATE.make(
		        [
		                command: json.command,
		                m: json.matches.collect {
		                        [
		                                start: it.start,
		                                end: it.end,
		                                spaces: it.spaces.length(),
		                                text: it.match,
		                                explain: json.helptext.find { exp -> exp[1] == it.helpclass }
		                        ]
		                }.findAll { it.explain }.collect { it.explain = it.explain[0]; it }//.findAll { it.explain.length() < 30000 }
		        ]
		).writeTo(tmp.newWriter())
	}
	driver.get("file://${tmp}")	
	new File("${it[0]}.png").bytes = driver.getScreenshotAs(OutputType.BYTES)
	println "${it[0]} OK"
}
driver.close()
p.destroy()
