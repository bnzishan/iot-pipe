#!/bin/sh



cd /usr/share/storm


#bash entrypoint-script.sh  supervisor  $ZOOKEEPER_CONNECT  $ZOOKEEPER_PORT
bash entrypoint-script.sh  supervisor

#supervisorStatus=
#until netstat -tulpn | grep :6703 ;do
#    >&2 echo "supervisor is not running - waiting"
#    sleep 1
#done
#echo XXX $0 OK Supervisor is running!

#until tail  /usr/share/storm/logs/supervisor.log | grep --line-buffered 'Started statistics report plugin...';do
 #   >&2 echo "supervisor is not running - waiting"
  #  sleep 1
# done
# echo XXX $0 OK Supervisor is running!

# until tail child.out| fgrep -q "Server startup"; do sleep 1; done




# tail -f logfile | grep 'certain_word' | read -t 1200 dummy_var
# [ $? -eq 0 ]  && echo 'ok'  || echo 'server not up'



#until  tail -f $STORM_HOME/startupLog.out | grep --line-buffered '[INFO] Starting supervisor server';do
 #   >&2 echo "storm supervisor is not running - waiting"
 #  sleep 1
 # done
# echo XXX $0 OK "storm supervisor is running!


bash bin/storm list

cd /benchmark

echo $(date +%H:%M:%S.%N | cut -b1-12)" : Running Storm Supervisor Component ...................................... "

java -cp main-module-1.0.0.jar org.hobbit.core.run.ComponentStarter org.hobbit.sdk.iotpipeline_bm.system.storm.StormSupervisor





