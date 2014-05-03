import java.io.File;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*


def MAX_COMMAND_LENGTH = 40

def MAX_EXPLAIN_LENGTH = 300



def http = new HTTPBuilder('http://4.commandlinefu.sinaapp.com')

// [[id, command],...]
def commands =  http.request(GET, TEXT) {
	uri.path = '/explain_task.php'
	uri.query = [:]
	headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

	response.success = { resp, content ->
		new groovy.json.JsonSlurper().parseText content.text
	}
	
	response.failure = { resp ->
	  println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
	}
}.collect{ [ it.id,it.command ]}

def TEMPLATE = new groovy.text.SimpleTemplateEngine().createTemplate(new File('template.html').getText('UTF-8'))
def p = '/usr/bin/Xvfb :99'.execute();
def firefox = new FirefoxBinary();
firefox.setEnvironmentProperty('DISPLAY', ':99')
def driver = new FirefoxDriver(firefox, null)
driver.manage().window().size = new org.openqa.selenium.Dimension(400, 100)

def tmp = File.createTempFile('explainImage','')
tmp.deleteOnExit()

commands.each {
	if(it[1].length() > MAX_COMMAND_LENGTH){
		println "${it[0]} IGNORE"
		http.request(POST) { h ->
			uri.path = "/upload_explain.php?id=${it[0]}&status=ignore"
			response.success = { resp -> println "${it[0]} REPORT OK" }
			response.failure = { resp -> println "${it[0]} REPORT ERROR: ${resp.statusLine}" }
		}
		return
	}
	def ep = "python explain.py".execute()
	ep.withWriter { w-> w << it[1] }
	def text = ep.in.text
	if(!text) {
		println "${it[0]} ERROR PYTHON"
		http.request(POST) { h ->
			uri.path = "/upload_explain.php?id=${it[0]}&status=error"
			response.success = { resp -> println "${it[0]} REPORT OK" }
			response.failure = { resp -> println "${it[0]} REPORT ERROR: ${resp.statusLine}" }
		}
		return
	}
	new groovy.json.JsonSlurper().parseText(text).with { json ->
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
		                }.findAll { it.explain }.collect { it.explain = it.explain[0]; it }.findAll { it.explain.length() < MAX_EXPLAIN_LENGTH }
		        ]
		).writeTo(tmp.newWriter())
	}
	driver.get("file://${tmp}")	
	http.request(POST) { h ->
		uri.path = "/upload_explain.php?id=${it[0]}"
		body = driver.getScreenshotAs(OutputType.BYTES)
		requestContentType = ContentType.BINARY
		response.success = { resp -> println "${it[0]} OK" }
		response.failure = { resp -> println "${it[0]} UPLOAD ERROR: ${resp.statusLine}" }
	}
}
driver.close()
p.destroy()
