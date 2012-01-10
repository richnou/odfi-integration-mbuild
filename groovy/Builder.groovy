
class Builder extends Thread {
	
	
	Object 	target
	Object 	build
	
	String 	host
	File	workDir
	
	Builder(build,target,currentDir) {
		
		// Gather properties from target
		//------------------------------------
		this.workDir	= currentDir
		this.target		= target
		this.build		= build
		this.host		= target.host.text()
	}
	
	
	/**
	 * Implements the Build process
	 */
	void run() {
		
		println()
		println("\t\t*I Host: "+this.host)
		
		// Prepare workdir
		//--------------------------
		if (target.workdir) {
			this.workDir = new File(target.workdir.text())
		} else {
			
			// Default workdir is : currentDir/buildid/targetid/
			//--------------
			this.workDir = new File(this.workDir.absolutePath+"/"+build.'@id'+"/"+target.'@id'+"/")
			if (!this.workDir.exists())
				this.workDir.mkdirs()
		}
		println("\t\t*I Workdir: "+this.workDir.absolutePath)
		
		//-- Prepare on host
		def workdirProcess = "ssh $host mkdir -p ${this.workDir}".execute()
		workdirProcess.waitFor()
		
		// Checkout SCM
		//----------------------
		def scm = new SCM(build.scm,this.workDir,host)
		scm.checkout()
		
		// Execute prebuild commands
		//---------------------------
		println()
		target.prebuild.collect {
			
			cmd ->
			println("\t\t*I Prebuild command: ${cmd.text()}")
			def cmdProcess = "ssh $host cd ${this.workDir} && ${cmd.text()}".execute()
			cmdProcess.in.eachLine { line -> println line }
			cmdProcess.waitFor()
			
		}
		
		
		// Call Make , or use <make></make> xml
		//---------------------------
		println()
		println("\t\t*I Making")
		def cmdProcess = "ssh $host cd ${this.workDir} && make".execute()
		cmdProcess.in.eachLine { line -> println line }
		cmdProcess.waitFor()
		
		
		// Post Build
		//-----------------------
		println("\t\t*I Post building")
		//-- Sync things
		if (target.postbuild.sync) {
			//def syncValue = groovy.util.Eval.me(${target.postbuild.sync.text()})
			println("\t\t\t*I Syncing with $target.postbuild.sync")
			def rsyncProcess = "rsync -avzr $host:$workDir/* $workDir".execute()
			rsyncProcess.consumeProcessErrorStream(System.out)
			rsyncProcess.consumeProcessOutputStream(System.out)
			//rsyncProcess.in.eachLine { line -> println line }
			rsyncProcess.waitFor()
			
		}
	}
	
	
	
	
}