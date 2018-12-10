javac *.java
rc=$?
if [ $rc -ne 0 ]
then
   read -rsp $'Compile error\n' -n1 key
   exit
fi
java -cp "mysql-connector-java-5.1.18-bin.jar:./"  InnReservations
