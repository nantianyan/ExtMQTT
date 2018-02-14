#!/bin/sh
mkdir mytmp
unzip cloudmedia/build/outputs/aar/cloudmedia-release.aar -d mytmp/cm
unzip p2pmqtt/build/outputs/aar/p2pmqtt-release.aar -d mytmp/p2p
cd mytmp
jar -xvf cm/classes.jar
jar -xvf p2p/classes.jar
jar -cvf cmsdk.jar com   #cloud media SDK
cd ..
mv mytmp/cmsdk.jar .
rm -rf mytmp

