"C:\Program Files\Java\jdk1.6.0_01\bin\jar" cvf inspired.war . 

rd "C:\Program Files\Openfire\plugins\inspired" /q /s
del "C:\Program Files\Openfire\plugins\inspired.war"
copy inspired.war "C:\Program Files\Openfire\plugins"

del "C:\Program Files\Openfire\logs\*.*"

pause