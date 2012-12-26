del developer.keystore

"C:\Program Files\Java\jdk1.7.0_05\bin\keytool" -genkey -keystore developer.keystore -storepass password -keypass password -alias screenshare -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" -validity 5300
"C:\Program Files\Java\jdk1.7.0_05\bin\keytool" -selfcert -keystore developer.keystore -storepass password -keypass password -alias screenshare

pause
