#!/bin/sh
mkdir mytmp
unzip cloudmedia/build/outputs/aar/cloudmedia-release.aar -d mytmp/cm
unzip p2pmqtt/build/outputs/aar/p2pmqtt-release.aar -d mytmp/p2p
cd mytmp
jar -xvf cm/classes.jar
jar -xvf p2p/classes.jar
jar -cvf cmmq.jar com   #cloud media message queue
cd ..
mv mytmp/cmmq.jar .
rm -rf mytmp

