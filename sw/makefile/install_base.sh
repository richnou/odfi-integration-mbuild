#!/bin/bash

## Base Variables
#########################

## Unpack location
unpack=`pwd`

## Install prefix
export PREFIX=`pwd`


## Get Options
####################
filter=".*"
install=true ## Do we run install target?
verbose="-s" ## Calls to make won't have the -s option if -v if set on command line
list=false

ARGS=`getopt -o "p,v,l" -l "no-install,list,help,prefix:,workdir:,filter:" -- "$@"`
eval set -- "$ARGS"
while true;
do
	case $1 in

	-l|--list)

		echo "[DIST] Only listing content"
		list=true

	shift;;
	-p|--prefix)
		if [[ ! -n "$2" ]]
		then
			echo "Prefix needs to have a provided path!"
			exit -1
		fi
		export PREFIX=$2
		echo "[DIST] Setting PREFIX to ${PREFIX}"
	shift 2;;

	--filter)
		echo "[DIST] Filtering packages to regexp $2"
		filter=$2
	shift 2;;

	--workdir)

		## prepare unpack dir
		if [[ ! -d $2 ]]
		then
			mkdir -p $2
		fi


		unpack=`cd $2 && pwd`
		echo "[DIST] Setting Workdir to ${unpack}"
	shift 2;;

	-v)
		echo "[DIST] Using verbose mode"
		verbose=""
	shift;;

	--no-install)
		echo "[DIST] No install will be performed"
		install=false
	shift;;

	--help)
		echo "Usage:
./installer.run -vl [--workdir path/to/unpack/dir] [--prefix path/to/install] [--filter regexp]

With:

--workdir path/to/unpack/dir: Path to a folder where the installer content will be extracted and installation phase called
Default: current directory

--prefix path/to/install: Path to a folder where the installation results will be copied to, typically by using the install command
Default: current directory

--filter regexp: Provide a regular expression, for which only archives whose names are matching will be installed. Useful to repeat installation of one or more sub-archives, but not all of them
Default: .* (i.e: All)

-v: Activate verbose mode of make command. Useful to see full compilation commands

-l: Only list the content of the archives/sub-archives, don't extract or perform any install
"


		exit 0
	shift;;

	--)
	shift
	break;;

	esac
done

## ODFI mbuild home for include to work
export ODFI_MBUILD_HOME=${unpack}

## Processors count
cpucount=$(grep 'processor' /proc/cpuinfo | wc -l)

## Look into this script for the _ARCHIVE_BELOW_ marker to know where the archive starts
ARCHIVE=`awk '/^## __ARCHIVE_BELOW__/ {print NR + 1; exit 0; }' $0`

## Only list
if [[ $list == true ]]
then

	tail -n+$ARCHIVE $0 | tar tzv

	exit 0
fi

## Welcome and basic User interaction
###################################

echo "[DIST] Welcome to Distribution install script"
echo "[DIST] This script will unpack to ${unpack}, and then run the install"
echo "[DIST] Processor count: ${cpucount}"






## Extract
#CONTENT=`tail -n+$ARCHIVE $0`
#tar xzv -C ${unpack}
packages=`tail -n+$ARCHIVE $0 | tar xzv -C ${unpack} | grep -E 'tar.gz|run'`

## Install
##############################

echo "[DIST] Starting installation into ${PREFIX}..."

cd ${unpack}
## The extraction must have extracted some packages which we gathered in the ${packages} variable
for package in ${packages};
do
	echo "[DIST] - Package ${package}"



	if [[ ${package} =~ .*\.run ]]
	then
		echo "[DIST] - Running Subinstall"

		args="--prefix=${PREFIX} --filter=${filter}"

		if [[ ${install} == false ]]; then args="${args} --no-install"; fi

		if [[ ${verbose} == "" ]]; then args="${args} -v"; fi

		bash ${package} $args


	else

		## Filter
		if [[ ! ${package} =~ $filter ]]
		then
			continue
		fi

		## Get package folder by listing archive content
		package_folder=`tar taf ${package} | head -n1`

		## Extract
		tar axf ${package}
		#tar xzv -f ${package}

		echo "[DIST] - Extracted to ${package_folder}"

		## Find makefile
		dist_makefile=`cd ${package_folder} && find -maxdepth 1 -name "Makefile.*dist" | tail -n1`

		if [[ $install == true ]]
		then

			#### Run make dist_install in it
			if [[ ${dist_makefile} != "" ]]
			then
				make ${verbose} -C ${package_folder} install -f ${dist_makefile} -j${cpucount}
			else
				make ${verbose} -j${cpucount} -C ${package_folder} install
			fi

			#### Check Return code. Fails if not 0
			if [[ $? != 0 ]]
			then
				echo "Installing package from ${package_folder} seems to have failed, stopping"
				exit $?
			fi

			#### Install default paths:
			####  - bin
			####  - sbin
			####  - lib
			####  - usr
			if [[ -d ${package_folder}/bin ]];  then cp -Rfp ${package_folder}/bin ${PREFIX} ; fi
			if [[ -d ${package_folder}/sbin ]]; then cp -Rfp ${package_folder}/sbin ${PREFIX} ; fi
			if [[ -d ${package_folder}/lib ]];  then cp -Rfp ${package_folder}/lib ${PREFIX} ; fi
			if [[ -d ${package_folder}/usr ]];  then cp -Rfp ${package_folder}/usr ${PREFIX} ; fi
			if [[ -d ${package_folder}/tcl ]];  then cp -Rfp ${package_folder}/tcl ${PREFIX} ; fi
			if [[ -d ${package_folder}/doc ]];  then cp -Rfp ${package_folder}/doc ${PREFIX} ; fi
		fi

	fi



done



exit 0
## __ARCHIVE_BELOW__ ##
