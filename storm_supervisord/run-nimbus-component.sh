#!/bin/sh



cd /usr/bin

#bash start-supervisor.sh $ZOOKEEPER_CONNECT $ZOOKEEPER_PORT
bash start-supervisor.sh nimbus

#if lsof -Pi :6627 -sTCP:LISTEN -t >/dev/null ; then
 #   echo "running"
#else
 #   echo "not running"
#fi


until netstat -tulpn | grep :6627 ;do
    >&2 echo "nimbus is not running - waiting"
    sleep 1
done

echo XXXXXXXXXXXX $0 OK Nimbus is running!

cd /benchmark

echo $(date +%H:%M:%S.%N | cut -b1-12)" : Running Storm Nimbus Component...................................."

java -cp main-module-1.0.0.jar org.hobbit.core.run.ComponentStarter org.hobbit.sdk.iotpipeline_bm.system.storm.StormNimbus


