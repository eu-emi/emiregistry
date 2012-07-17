#!/bin/bash

#
# Startup script for EMIR Server
#

#
# Read basic configuration parameters
#
. /etc/emi/emir/startup.properties


#
# check whether the server might be already running
#
if [ -e $PID ] 
 then 
  if [ -d /proc/$(cat $PID) ]
   then
     echo "A EMIR instance may be already running with process id "$(cat $PID)
     echo "If this is not the case, delete the file $INST/$PID and re-run this script"
     exit 1
   fi
fi

#
# setup classpath
#
CP=`ls $LIB/*.jar | tr '\n' :`

PARAM=$*
if [ "$PARAM" = "" ]
then
  PARAM=${CONF}/emir.config
fi

#
# go
#

CLASSPATH=$CP ; export CLASSPATH

nohup $JAVA ${MEM} ${OPTS} ${DEFS} eu.emi.emir.EMIRServer ${PARAM} EMIR > $STARTLOG 2>&1  & echo $! > $PID

echo "EMIR starting..."
