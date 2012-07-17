#!/bin/bash

#
# Shutdown script for EMIR Server
#


#
# Read basic configuration parameters
#
. /etc/emi/emir/startup.properties


if [ ! -e $PID ]
then
 echo "No PID file found, server probably already stopped."
 exit 0
fi


cat $PID | xargs kill -SIGTERM

#
# wait for shutdown
# 
P=$(cat $PID)
echo "Waiting for EMIR Server to stop..."
stopped="no"
until [ "$stopped" = "" ]; do
  stopped=$(ps -p $P | grep $P)
  if [ $? != 0 ] 
  then
    stopped=""
  fi
  sleep 2
done

echo "EMIR Server stopped!"

rm -f $PID
