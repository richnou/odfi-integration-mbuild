#!/bin/bash


loc="$(dirname "$(readlink -f ${BASH_SOURCE[0]})")"

export ODFI_MBUILD_HOME=$loc