#!/bin/bash

echo "################################################"
echo "#        storm:   entrypoint-script.sh         #"
echo "################################################"

#printenv

#echo "inside entrypoint-script.sh........... "

echo " argument #1 : $1"
echo " env ZOOKEEPER_CONNECT: $ZOOKEEPER_CONNECT"
echo " env ZOOKEEPER_PORT: $ZOOKEEPER_PORT"
echo " env STORM_NIMBUS_SEEDS: $STORM_NIMBUS_SEEDS"
echo " env GRAPHITE_HOST: $GRAPHITE_HOST"


###########################
# storm.zookeeper.servers #
###########################
if ! [ -z "$ZOOKEEPER_CONNECT" ]; then
    # All ZooKeeper server IPs in an array
    IFS=', ' read -r -a ZOOKEEPER_SERVERS_ARRAY <<< "$ZOOKEEPER_CONNECT"
    sed -i -e "s/%zookeeper%/$ZOOKEEPER_SERVERS_ARRAY/g" $STORM_HOME/conf/storm.yaml
fi



##########################
# zookeeper.port #
##########################
 sed -i -e "s/\"%port%\"/$ZOOKEEPER_PORT/g" $STORM_HOME/conf/storm.yaml


#---------------------------------------


IPS=`hostname -i`
IFS=" "
read -r -a IP_ADDRESSES <<< "$IPS"
IP=${IP_ADDRESSES[0]}

"echo IP: $IP"

#IP=$(/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f
#IP=$(echo `ifconfig eth0 2>/dev/null|awk '/inet addr:/ {print $2}'|sed   's/addr://'`)
#/sbin/ip route|awk '/default/ { print $3 }'
#IP=$DOCKER_HOST   # use with -e "DOCKER_HOST=$(ip -4 addr show eth0| grep -Po 'inet \K[\d.]+')


#IP="$(ifconfig | grep -A 1 'eth0' | tail -1 | cut -d ':' -f 2 | cut -d ' ' -f 1)"   #install ifconfig to use that command
#echo $IP

#---------------------------------------



########################
# storm.local.hostname #
########################
 localHostname=`hostname`
  #sed -i -e "s/\"%local%\"/$localHostname/g" $STORM_HOME/conf/storm.yaml
   sed -i -e "s/%local%/$localHostname/g" $STORM_HOME/conf/storm.yaml


#######################
#     nimbus.seeds    #
#######################

if ! [ -z "$STORM_NIMBUS_SEEDS" ]; then
         # it is supervisor, use the env to update nimbus seeds config
         IFS=', ' read -r -a STORM_NIMBUS_SEEDS_ARRAY <<< "$STORM_NIMBUS_SEEDS"
         sed -i -e "s/%nimbus%/$STORM_NIMBUS_SEEDS_ARRAY/g" $STORM_HOME/conf/storm.yaml
    else
        sed -i -e "s/%nimbus%/$IP/g" $STORM_HOME/conf/storm.yaml
        #sed -i -e "s/%nimbus%/$localHostname/g" $STORM_HOME/conf/storm.yaml
fi


###########################
#      storm.log.dir      #
###########################
logDir="$STORM_HOME/logs"
sed -i -e  's|%stormLogDir%|'$logDir'|g' $STORM_HOME/conf/storm.yaml


###########################
# storm.logback.conf.dir  #
###########################
logbackDir="$STORM_HOME/log4j2"
sed -i -e  's|%logbackConfDir%|'$logbackDir'|g' $STORM_HOME/conf/storm.yaml

###########################
#  metrics.graphite.host #
###########################
  sed -i -e "s/%graphite%/$GRAPHITE_HOST/g" $STORM_HOME/conf/storm.yaml


###########################
#  metrics.graphite.prefix #
###########################
  sed -i -e "s/%hostname%/$localHostname/g" $STORM_HOME/conf/storm.yaml

echo "#####################################################################"
echo "#                       $STORM_HOME/conf/storm.yaml                 #"
echo "#####################################################################"


           while read line
            do
            echo "$line"
           done < "$STORM_HOME/conf/storm.yaml"


#CMD="exec bin/storm $1"
#echo "$CMD"
#eval "$CMD"
echo "Goint to run storm with daemon $1....."

#nohup bin/storm $1 &

nohup bin/storm $1 &> $STORM_HOME/startupLog.out&

# nohup bin/storm $1 > nohup.out 2>&1&
# nohup some_command > nohup2.out&
