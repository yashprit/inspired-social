del inspired.war

call ant war >build.txt

rd "C:\Program Files (x86)\Openfire\plugins\inspired" /q /s
del "C:\Program Files (x86)\Openfire\plugins\inspired.war"
copy inspired.war "C:\Program Files (x86)\Openfire\plugins"

del "C:\Program Files (x86)\Openfire\logs\*.*"

pause