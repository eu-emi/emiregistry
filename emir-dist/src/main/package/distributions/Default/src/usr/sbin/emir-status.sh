#!/bin/bash

#
# Check status of EMIR Server
#
# before use, make sure that the "service name" used in 
# this file is the same as in the corresponding start.sh file

#
# Read basic configuration parameters
#
. /etc/emi/emir/startup.properties


if [ ! -e $PID ]
then
 echo "EMIR not running (no PID file)"
 exit 7
fi

PIDV=$(cat $PID)

if ps axww | grep -v grep | grep $PIDV > /dev/null 2>&1 ; then
 echo "EMIR running with PID ${PIDV}"
 exit 0
fi

#else not running, but PID found
echo "warn: EMIR not running, but PID file $PID found"
exit 3
