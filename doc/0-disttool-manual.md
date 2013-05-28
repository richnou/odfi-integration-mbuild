Distribution Tool User Manual
=======================================


# Summary


* A top makefile that:
** Calls "make dist -f Makefile.dist" in subfolders (also called modules)
** Packages all the resulting packages of each module in a single package
** Creates an install script to be used to perform installation of all the packages

* An install script:
** Unpacks all submodules' packages
** Runs "make install -f Makefile.dist" in all unpacked submodules
** Propagates a PREFIX variable containing the installation prefix
	
* The modules  makefiles:
** Can include a makefile provided by this utility for helping methods
** Contain a "dist" target doing the packaging work
*** Must produce in the module folder a modulename-....-xx.tar.gz
*** The tar.gz will unpack during installation to folder named the same way as the provided
*** If the file name ends in -xx.tar.gz, the xx could be interpreted as a version name
** Contains an install target called by the installer script




