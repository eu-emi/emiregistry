#
# EMIR
# 

# To create the platform independent binary and source tarballs

emir-dist> mvn site package

# To create the rpm distributions

emir-dist> mvn site package -Ppackman 
	-Dpackage.type=(rpm|deb|src.tar.gz|rpm.tar.gz|deb.tar.gz|all-rpm|all-deb)	default: rpm-all 
	-Ddistribution=(Redhat|Debian) 												default: Redhat 
	-Dpackage.release=															default: from pom.xml
	-Dpackage.version=															default: from pom.xml
	-DindexJars=(true|false) 													default: false
	-Dmaven.component.version=													default: from pom.xml	
	-Dmaven.component.artifactId=												default: from pom.xml
	-Dmaven.component.groupId=													default: from pom.xml
	-Dmvn.repo.local															default: ${user.home}/.m2/repository
	-Dmvn.repo.url																default: http://unicore-dev.zam.kfa-juelich.de/maven

For more documentation, please visit https://twiki.cern.ch/twiki/bin/view/EMI/EMIRegistry

