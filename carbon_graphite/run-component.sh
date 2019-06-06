#!/bin/sh




bash /run.sh


echo "graphite done"

sleep 10

cd /benchmark

echo $(date +%H:%M:%S.%N | cut -b1-12)" : Running Graphite Component ...................................."

java -cp main-module-1.0.0.jar org.hobbit.core.run.ComponentStarter org.hobbit.sdk.iotpipeline_bm.system.carbon_graphite.CarbonGraphite
