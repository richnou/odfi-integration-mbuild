#!/usr/bin/env groovy

// Resolve classpath
//------------------------
//scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent
//this.class.classLoader.rootLoader.addURL( new URL("file://"+scriptDir+"../sw/groovy/") )
//println "Adding to classpath: "+scriptDir+"../sw/groovy/"

import odfi.dev.mbuild.*

// Get The Arguments
//-----------------------------
def cli = new CliBuilder(usage: 'odfi_integration_mbuild --build [buildid]')
cli.with {
    h longOpt: 'help', 'Show usage information'
    //e longOpt: 'exclude','Comma separated list of buildId:targetIds to exclude'
    //b longOpt: 'build', 'buildid:targetid String representing the build to do'
}
cli.build(args:1,argName:"build","buildid:targetid String representing the build to do")
cli.e(args:1,argName:"exclude","Comma separated list of buildId:targetIds to exclude")

def options = cli.parse(args)
if (!options) {
    cli.usage()
    return
}
// Show usage text when -h or --help option is used.
if (options.h) {
    cli.usage()
    return
}




//-- Look for mbuild.xml
def mbuildFile = new File("mbuild.xml").absoluteFile
if (!mbuildFile.exists()) {
    println("*E: mbuild.xml in local directory was not found")
    return
}

//-- Parse XML
def builds = new XmlParser().parse(mbuildFile)


// Do Single build
//----------------------
if (options.build) {
    
    println("*I Doing Single build for : "+options.build)
    
    // Find Target component
    //--------------------------
    def comps = options.build.split(":")
    if (comps.length != 2) {
        
        println("*E The build id for single build MUST be of format buildid:targetid")
		return
        
    } else {
        println("\t*I Build: "+comps[0])
        println("\t*I Target: "+ comps[1])
    }
    def buildId = comps[0]
    def targetId = comps[1]
    
    def build = builds.build.find {it.@id==buildId}
    if (!build) {
        println("\t*E: The Provided build could not be found in build descriptor")
        return
    }
    def target = build.target.find {it.@id==targetId}
    if (!target) {
        println("\t*E: The Provided target could not be found in build descriptor")
        return
    }
    
    // Build
	//------------------
	def builder = new Builder(build,target,mbuildFile.parentFile)
	builder.start()
    
    
    
    
    return
    
}


println("*I Exclusion list: "+options.e)


// Do Global Build
//--------------------------

//---- Gather Builds
builds.build.collect {
    
    build ->
    
    println("*I Building: "+build.'@id')
    if (options.e!=false && build.'@id'.matches(options.e)) {
		println("*W Build excluded")
		return
	}
	 
	
    //---- Target
    build.target.collect {
        
        target ->
        println("\t*I Target: "+ target.'@id')
        
        //-- Check if we have to exclude
        //--------------------
        if (target.'@exclude') {
            println("\t*W Target Excluded")
        } else {
        
            //-- Do build
            //-- Execute: connect to host, prepare workdir, execute prebuild commands, call make
            //---------------------------
            def host = target.host.text()
            
            def builder = new Builder(build,target,mbuildFile.parentFile)
            //builder.start()
			builder.run()
        } 
        
        //-- A few infos
        
        
        //---- Connect to host with mbuild build command
        //def buildProcess = "ssh $host echo Hi! && cd $mbuildFile.parentFile.absolutePath && odfi-mbuild -build tt ".execute();
        //def buildProcess = "ssh $host echo Hi! && echo Hehe && cd $mbuildFile.parentFile.absolutePath && pwd".execute()
        
        //println buildProcess.consumeProcessOutput()
        //println "${buildProcess.text}"
        //buildProcess.waitFor()
        
        //---- Prepare 
        
        
    }
    
    
    
}

// Do The Specific Target Build
//-----------------------------------


