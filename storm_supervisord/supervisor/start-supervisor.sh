echo "inside start-supervisor.sh...."


echo $1

printenv

###########################
# storm.zookeeper.servers #
###########################
if ! [ -z "$ZOOKEEPER_CONNECT" ]; then
    # All ZooKeeper server IPs in an array
   # IFS=', ' read -r -a ZOOKEEPER_SERVERS_ARRAY <<< "$ZOOKEEPER_CONNECT"
   # sed -i -e "s/%zookeeper%/$ZOOKEEPER_SERVERS_ARRAY/g" $STORM_HOME/conf/storm.yaml


        #echo $zookeeperConnectStr | tr ',' ' '
        ZKs="${ZOOKEEPER_CONNECT//,/ }"
        for i in $ZKs
            do
                 if [[ i = 0 ]];
                   then
                   ZK_SERVERS="$'\n'"
                  fi
             ZK_SERVERS+="- \"$i\" "$'\n'""
             done

            echo "  zkserversss:   $ZK_SERVERS"

fi


 sed -i -e "s/%zookeeper%/$ZOOKEEPER_CONNECT/g" $STORM_HOME/conf/storm.yaml
      #  echo "storm.zookeeper.servers: $ZK_SERVERS" >> $STORM_HOME/conf/storm.yaml
##########################
# zookeeper.port #
##########################
        if ! [ -z "$ZOOKEEPER_PORT" ]; then
       sed -i -e "s/\"%port%\"/$ZOOKEEPER_PORT/g" $STORM_HOME/conf/storm.yaml
        fi


########################
# storm.local.hostname #
########################
  localHostname=`hostname -i`
sed -i -e "s/%local%/$localHostname/g" $STORM_HOME/conf/storm.yaml


#######################
#     nimbus.seeds    #
#######################

if ! [ -z "$STORM_NIMBUS_SEEDS" ]; then
         sed -i -e "s/%nimbus%/$STORM_NIMBUS_SEEDS/g" $STORM_HOME/conf/storm.yaml
fi


echo "#####################################################################"
echo "#                       $STORM_HOME/conf/storm.yaml                 #"
echo "#####################################################################"


  while read line
    do
     echo "$line"
     done < "$STORM_HOME/conf/storm.yaml"


echo "Goint to run storm daemon $1 with supervisord ....."

/usr/sbin/sshd && nohup supervisord &> $STORM_HOME/startupLog.out&
