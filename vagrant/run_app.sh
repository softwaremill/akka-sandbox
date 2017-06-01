#!/bin/bash

function cloneRepo {
    echo "Cloning repo"
    rm -rf akka-sandbox
    git clone https://github.com/softwaremill/akka-sandbox
}

function runApp {
    echo "Compiling repo (this may take some time, first time)"
    cd akka-sandbox
    sbt assembly

#    echo "Downloading aspectjweaver"
#   wget -q -nc http://central.maven.org/maven2/org/aspectj/aspectjweaver/1.8.10/aspectjweaver-1.8.10.jar

    APP_OPTS=" -Xmx512M -Dapi.host=$host_ip -Dakka.cluster.seed-nodes.0=$seed_node -Dapp.user-creation-lag=$user_creation_lag"
#    RUN=" -javaagent:aspectjweaver-1.8.10.jar $APP_OPTS -jar service/target/scala-2.11/sandbox-service-assembly-0.0.1-SNAPSHOT.jar"
    RUN=" $APP_OPTS -jar service/target/scala-2.11/sandbox-service-assembly-0.0.1-SNAPSHOT.jar"
    echo "Running $RUN"
    rm -f ~/nohup.out
    nohup java $RUN > ~/nohup.out 2>&1&
    sleep 10
    cat ~/nohup.out
    echo "Initialization"
    wget --retry-connrefused --waitretry=1 --read-timeout=20 --timeout=15 -t 5 http://$host_ip:9000/user/test
}

cloneRepo
runApp
echo "Provisioning done!"