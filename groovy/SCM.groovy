
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
		
		// URL
		//-----------
		def url = scm.url.text()
		def handler = scm.'@handler' ? scm.'@handler'[0] : null
		
		if ((handler && handler == "svn") || url ==~ /svn.*/) {
			handleSVN(url)
		} else {
		
			println("\t\t*E SCM ist not handled with URL: $url")
		
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
			coProcess.waitFor()
			
		} else {
		
			//-- Update otherwise
			println("\t\t*I Updating")
			def coProcess = "ssh ${this.host} svn up ${this.workDir}".execute()
			coProcess.in.eachLine { line -> println line }
			coProcess.waitFor()
		
		}
		
		
		
		
		
		
	}
	
}