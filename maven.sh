#!/bin/bash
APP_NAME=$1
HOST_NAME=$2

#sh maven.sh NTFM_INFOSHARE 172.28.33.3,172.28.33.4

array=(${HOST_NAME//,/ })

/home/users/webdev/tools/apache-maven/apache-maven-3.6.1/bin/mvn -U clean package -DskipTests

if [ $? -eq 0 ];then

for element in ${array[@]}
do
    echo $element

    #scp -r /home/atc/weblib/* $element:/home/atc/weblib/

    echo "scp target/${APP_NAME}.jar $element:/home/atc/bin/${APP_NAME}"
    scp target/${APP_NAME}.jar $element:/home/atc/bin/${APP_NAME}

    echo "chmod -R 777 /home/atc/bin/${APP_NAME}"
    ssh $element "chmod -R 777 /home/atc/bin/${APP_NAME}"

    echo "sh /home/atc/shell/startup.sh ${APP_NAME} restart"
    ssh $element "sh /home/atc/shell/startup.sh ${APP_NAME} restart"
#    sh /home/atc/shell/startup_debug.sh ${APP_NAME} status
    sleep 5s

done
    scp target/${APP_NAME}.jar 172.28.33.11:/home/atc/bin/${APP_NAME}
else
    echo "打包失败！"
fi