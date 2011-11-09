#!/usr/bin/env python

#
# updates the spec file with the current jar files
#


import os
import sys
import socket
import shutil


#
# version and release number passed in from commandline
#
version=sys.argv[1]
release=sys.argv[2]

#
# spec file to produce
#
specfile="emi-emir.spec"

jarsource="rpm/usr/share/unicore/unicorex/lib"

jarbase="/usr/share/emi/emir/lib"

#
#loop over list of config files and do the substitution
#

try:
    print "Generating "+specfile
    jars=os.listdir(jarsource)
    s=""
    for j in jars:
        if(j[-3:]=="jar"):
            s = s + '"'+jarbase+"/"+j+'"\n'

    jars=os.listdir(jarsource+"/endorsed")
    endorsed=""
    for j in jars:
        if(j[-3:]=="jar"):
            endorsed = endorsed + '"'+jarbase+"/endorsed/"+j+'"\n'


    file = open("rpm/"+specfile+"_template")
    lines=file.readlines()
    file.close()
    
    file = open(specfile, 'w')

    for line in lines:
        line=line.replace("$jars", s);
        line=line.replace("$endorsed", endorsed);
        line=line.replace("$version", version);
        line=line.replace("$release", release);
        file.write(line)        

    file.close()

except Exception,e :
    print "Error processing",e

print "Done processing "+specfile
   



