del inspired.war

call ant war >build.txt

rd "G:\opt\openfire\plugins\inspired" /q /s
del "G:\opt\openfire\plugins\inspired.war"
copy inspired.war "G:\opt\openfire\plugins"

del "G:\opt\openfire\logs\*.*"

pause