#!/bin/sh

#cp /sensemark2
#bash .sh start
#bash .sh


# wait until rabbitmq is ready
#echo $(date +%H:%M:%S.%N | cut -b1-12)" : Waiting until Server is online..."
#until grep -m 1 "Server startup complete" /sensemark2/exampleServer_run.log
#do
  #sleep 1
  #seconds_passed=$((seconds_passed+1))
  #echo $seconds_passed >> out.txt
  #if [ $seconds_passed -gt 120 ]; then
   # echo $(date +%H:%M:%S.%N | cut -b1-12)" : Could not start Server. Timeout: [2 min]"
   # break
 # fi
#done
# echo $(date +%H:%M:%S.%N | cut -b1-12)" :  Server started successfully."


#start zk

#cd /zk/bin
#bash /zk/bin/zk-cfg

mv /benchmark/zookeeper-3.5.2-alpha /benchmark/zookeeper

cd /benchmark

echo $(date +%H:%M:%S.%N | cut -b1-12)" : Running the ZKManager...................................."

CMD java -cp main-module-1.0.0.jar org.hobbit.core.run.ComponentStarter org.hobbit.sdk.iotpipeline_bm.system.zookeeper.ZKManager





