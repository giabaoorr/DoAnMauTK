@echo off
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Users\HELLO\.jdk\jdk-21.0.10"
)
set "PATH=C:\Users\HELLO\.jdk\jdk-21.0.10\bin;C:\Users\HELLO\maven\bin;%PATH%"
"C:\Users\HELLO\maven\bin\mvn.cmd" %*
