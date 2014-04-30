def json = new groovy.json.JsonSlurper().parseText(System.in.getText('UTF-8'))

new groovy.text.SimpleTemplateEngine().createTemplate(new File('template.html').getText('UTF-8'))
.make(
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
		}.findAll { it.explain }.collect { it.explain = it.explain[0]; it }//.findAll { it.explain && it.explain.length() < 30000 }
	]
).writeTo(new PrintWriter(System.out))



