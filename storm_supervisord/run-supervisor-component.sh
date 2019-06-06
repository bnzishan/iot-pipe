#!/bin/sh



cd /usr/bin

bash start-supervisor.sh supervisor

cd /benchmark

echo $(date +%H:%M:%S.%N | cut -b1-12)" : Running Storm Supervisor ...................................."

java -cp main-module-1.0.0.jar org.hobbit.core.run.ComponentStarter org.hobbit.sdk.iotpipeline_bm.system.storm.StormSupervisor







