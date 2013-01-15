
class SCM {
	
	Object scm
	File workDir
	String host
	
	SCM(scm,workDir,host) {
		
		this.scm 		= scm
		this.workDir	= workDir
		this.host 		= host
	}
	
	
	/**
	 * Checks out the sources, or performs an update
	 */
	public void checkout() {
		
		
		println "*I SCM Working dir: ${this.workDir} "
		
		// URL
		//-----------
		def url = scm.url.text()
		def handler = scm.'@handler' ? scm.'@handler'[0] : null
		
		if ((handler && handler == "svn") || url ==~ /svn.*/) {
			handleSVN(url)
		} else if ((handler && handler == "git") || url ==~ /svn.*/) {
			handleGIT(url)
		} else {
		
			println "*W SCM ist not handled with URL: $url, not doing anything";
		
		}
		
	}
	
	private void handleSVN(url) {
		
		//-- Info
		println("\t\t*I Using SVN SCM handler")
		
		//-- Already checkedout?
		//---------------------------------
		
		def p = "ssh ${this.host} svn info ${this.workDir}".execute()
		p.consumeProcessOutput()
		p.waitFor()
		
		if (p.exitValue()==1) {
			
			//-- Do checkout
			println("\t\t*I Doing checkout from $url")
			def coProcess = "ssh ${this.host} svn co $url ${this.workDir}".execute()
			coProcess.in.eachLine { line -> println line }
			coProcess.err.eachLine { line -> println line }
			coProcess.waitFor()
			
		} else {
		
			//-- Update otherwise
			println("\t\t*I Updating")
			def coProcess = "ssh ${this.host} svn up ${this.workDir}".execute()
			coProcess.in.eachLine { line -> println line }
			coProcess.err.eachLine { line -> println line }
			coProcess.waitFor()
		
		}
		
	}
	
	private void handleGIT(url) {
		
		
		//-- Info
		println "*I: Using git SCM Handler"
		
		// Already Cloned ?
		//------------------------------
		def p = "ssh ${this.host} cd ${this.workDir} && git status".execute()
		def res = ""
		p.in.eachLine { line -> res+=line }
		p.err.eachLine { line -> res+=line }
		//def res = p.getText();
		//p.waitForProcessOutput()
		//p.consumeProcessOutput()
		p.waitFor()
		println "-> "+res
		if (res.contains("fatal")) {
			
			//-- Clone
			//throw new Exception("Not Cloned");
			println "*I: Cloning from $url ..."
			def cloneProcess = "ssh ${this.host} git clone $url ${this.workDir}".execute()
			cloneProcess.in.eachLine { line -> println line }
			cloneProcess.err.eachLine { line -> println line }
			cloneProcess.waitFor()

			
		} else {
		
			//-- Pull otherwise
			println "*I: Pulling..."
			//throw new Exception("Pulling to update...");
			def cloneProcess = "ssh ${this.host} cd ${this.workDir} && git pull -v --rebase $url master".execute()
			cloneProcess.in.eachLine { line -> println line }
			cloneProcess.err.eachLine { line -> println line }
			cloneProcess.waitFor()
		}
		
	}
	
}