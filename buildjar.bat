md mytmp\cm
md mytmp\p2p
7z x cloudmedia/build/outputs/aar/cloudmedia-debug.aar -omytmp/cm
7z x p2pmqtt/build/outputs/aar/p2pmqtt-debug.aar -omytmp/p2p
cd mytmp
jar -xvf cm/classes.jar
jar -xvf p2p/classes.jar
jar -cvf cmmq.jar com
move cmmq.jar ..
cd ..
rd /s /q mytmp

pause
