VERSION=1.2.1-rc2
GPGPASS=xxxxx

echo "====== Building artifacts for "$VERSION" ========"

mvn clean install -DskipTests -Dgpg.passphrase=$GPGPASS

echo "====== Publishing xmltypes-"$VERSION" ========"

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=xmltypes/target/xmltypes-$VERSION.pom -Dfile=xmltypes/target/xmltypes-$VERSION.jar -Dgpg.passphrase=$GPGPASS

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=xmltypes/target/xmltypes-$VERSION.pom -Dfile=xmltypes/target/xmltypes-$VERSION.jar -Dclassifier=sources -Dgpg.passphrase=$GPGPASS

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=xmltypes/target/xmltypes-$VERSION.pom -Dfile=xmltypes/target/xmltypes-$VERSION-javadoc.jar -Dclassifier=javadoc -Dgpg.passphrase=$GPGPASS

echo "====== Publishing emir-core-"$VERSION" ========"

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=emir-core/target/emir-core-$VERSION.pom -Dfile=emir-core/target/emir-core-$VERSION.jar -Dgpg.passphrase=$GPGPASS

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=emir-core/target/emir-core-$VERSION.pom -Dfile=emir-core/target/emir-core-$VERSION-sources.jar -Dclassifier=sources -Dgpg.passphrase=$GPGPASS

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=emir-core/target/emir-core-$VERSION.pom  -Dfile=emir-core/target/emir-core-$VERSION-javadoc.jar -Dclassifier=javadoc -Dgpg.passphrase=$GPGPASS

echo "====== Publishing emir-client-"$VERSION" ========"

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=emir-client/target/emir-client-$VERSION.pom -Dfile=emir-client/target/emir-client-$VERSION.jar -Dgpg.passphrase=$GPGPASS

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=emir-client/target/emir-client-$VERSION.pom -Dfile=emir-client/target/emir-client-$VERSION-sources.jar -Dclassifier=sources -Dgpg.passphrase=$GPGPASS

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=emir-client/target/emir-client-$VERSION.pom -Dfile=emir-client/target/emir-client-$VERSION-javadoc.jar -Dclassifier=javadoc -Dgpg.passphrase=$GPGPASS

echo "====== Creating a emiregistry parent bundle ========"
# This has to be uploaded manually on the sonatype repository manager
cd target
jar -cvf bundle.jar emiregistry-$VERSION.pom emiregistry-$VERSION.pom.asc

