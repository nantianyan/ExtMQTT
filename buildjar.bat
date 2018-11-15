md mytmp\cm
md mytmp\p2p
md mytmp\paho
7z x cloudmedia/build/outputs/aar/cloudmedia-debug.aar -omytmp/cm
7z x p2pmqtt/build/outputs/aar/p2pmqtt-debug.aar -omytmp/p2p
xcopy libs mytmp\paho
cd mytmp
jar -xvf cm/classes.jar
jar -xvf p2p/classes.jar
jar -xvf paho/org.eclipse.paho.android.service-1.1.1.jar
jar -xvf paho/org.eclipse.paho.client.mqttv3-1.1.0.jar
jar -cvf cmsdk.jar com org
move cmsdk.jar ..
cd ..
rd /s /q mytmp

pause
