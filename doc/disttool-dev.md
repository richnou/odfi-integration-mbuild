Distribution Tool Developer Reference
=============================

## Module functions


### variable $(dist-module-work-dir)
 
This variable contains the name of the packaging work dir
 	
Example:
 	  
	dist:
		echo "Work dir: $(dist-module-work-dir)"
 	
### function $(dist-module-prepare-work)
 
This function cleans the $(dist-module-work-dir) folder and creates it so that user may start preparing the package.
Example:
		
	dist:
		$(dist-module-prepare-work)
		echo "Now the $(dist-module-work-dir)" is available
 
 
### function $(dist-module-package)

This function packages the work dir folder in a tarball
Example:

	dist:
		$(dist-module-package)

### function $(dist-module-add,path/to/file(s)*)

This function copies the provided folder content or files to the work directory.
It takes one argument that can be a wildcard thing:
	
Example:
	dist:
		$(dist-module-prepare-work)
		@$(call dist-module-add,*.c)
		@$(call dist-module-add,folder anotherfolder FILE1 FILE2)