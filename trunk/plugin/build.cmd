del inspired.war

call ant war >build.txt

rd "C:\Program Files\Openfire\plugins\inspired" /q /s
del "C:\Program Files\Openfire\plugins\inspired.war"
copy inspired.war "C:\Program Files\Openfire\plugins"

del "C:\Program Files\Openfire\logs\*.*"

pause