def json = new groovy.json.JsonSlurper().parseText(System.in.getText('UTF-8'))

new groovy.text.SimpleTemplateEngine().createTemplate(new File('template.html').getText('UTF-8'))
.make(
	[
		command: json.command,
		m: [json.matches, json.helptext].transpose().collect {
	        	[
	                	start: it[0].start,
		                end: it[0].end,
				spaces: it[0].spaces.length(),
		                text: it[0].match,
		                explain: it[1][0]
		        ]
		}
	]
).writeTo(new PrintWriter(System.out))



