@echo off
javac -cp "lib/*" -d out src/main/java/com/stmarys/library/app/*.java src/main/java/com/stmarys/library/dao/*.java src/main/java/com/stmarys/library/db/*.java src/main/java/com/stmarys/library/model/*.java src/main/java/com/stmarys/library/service/*.java src/main/java/com/stmarys/library/ui/*.java src/main/java/com/stmarys/library/util/*.java
java -cp "out;lib/*" com.stmarys.library.app.Main
pause
