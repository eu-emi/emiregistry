#!/bin/sh

#
#Startup script for the EMI Registry Server
#

#
#Installation Directory
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
#Alternatively specify the installation dir here
#
#INST=


cd $INST

#
#Java command 
#
#JAVA="java -javaagent:lib/aspectjweaver-1.5.3.jar"


#
#Options to the Java VM
#

#set this one if you have ssl problems and need debug info
#OPTS=$OPTS" -Djavax.net.debug=ssl,handshake"


#
#enable JMX (use jconsole to connect)
#
#OPTS=$OPTS" -Dcom.sun.management.jmxremote"

#
#Memory for the VM
#
MEM=-Xmx256m

#
#put all jars in lib/ on the classpath
#
JARS=lib/*.jar
CP=.
for JAR in $JARS ; do 
    CP=$CP:$JAR
done

cd $INST

PARAM=$*
if [ "$PARAM" = "" ]
then
  PARAM=conf/dsr.config
fi

#
#go
#

if [ ! -d  logs ]
then
  mkdir -p logs
fi

nohup java ${MEM} ${OPTS} ${DEFS} -cp ${CP} eu.emi.dsr.DSRServer ${PARAM} > logs/startup.log 2>&1 & echo $! > LAST_PID
