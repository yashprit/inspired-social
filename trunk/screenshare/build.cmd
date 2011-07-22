del screenshare.jar

call ant jar

C:\"Program Files"\Java\jdk1.6.0_01\bin\jarsigner.exe -keystore developer.keystore -storepass password screenshare.jar screenshare

copy screenshare.jar "..\plugin\video"

pause