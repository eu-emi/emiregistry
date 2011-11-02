#!/bin/bash

#
# Check status of EMIR
#
# before use, make sure that the "service name" used in 
# this file is the same as in the corresponding start.sh file

# service name
SERVICE=emir

#
# Installation Directory
#
dir=`dirname $0`
if [ "$dir" != "." ]
then
  INST=`dirname $dir`
else
  pwd | grep -e 'bin$' > /dev/null
  if [ $? = 0 ]
  then
    INST=".."
  else
    INST=`dirname $dir`
  fi
fi

INST=${INST:-.}

#
# Alternatively specify the installation dir here
#
#INST=

cd $INST

# PID file
PID_FILE=/var/run/emir/emir.pid

if [ ! -e $PID_FILE ]
then
 echo "EMIR not running (no PID file)"
 exit 7
fi

PID=$(cat $PID_FILE)

if ps axww | grep -v grep | grep $PID > /dev/null 2>&1 ; then
 echo "EMIR running with PID ${PID}"
 exit 0
fi

#else not running, but PID found
echo "warn: EMIR not running, but PID file $PID_FILE found"
exit 3