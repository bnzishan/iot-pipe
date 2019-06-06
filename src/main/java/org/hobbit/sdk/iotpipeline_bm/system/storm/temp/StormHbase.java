package org.hobbit.sdk.iotpipeline_bm.system.storm.temp;

/**
 * Created by bushranazir on 25.03.2019.
 */

/*

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import org.apache.storm.hbase.bolt.mapper.HBaseProjectionCriteria;
import org.apache.storm.hbase.bolt.mapper.HBaseValueMapper;
import org.apache.storm.hbase.topology.WordCountValueMapper;
import org.apache.storm.hbase.trident.mapper.SimpleTridentHBaseMapper;
import org.apache.storm.hbase.trident.mapper.TridentHBaseMapper;
import org.apache.storm.hbase.trident.state.HBaseQuery;
import org.apache.storm.hbase.trident.state.HBaseState;
import org.apache.storm.hbase.trident.state.HBaseStateFactory;
import org.apache.storm.hbase.trident.state.HBaseUpdater;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.core.components.CommandReceivingComponent;
import org.hobbit.core.CommandsAndQueues;

import org.hobbit.sdk.sensemark2.Constants;

public class StormSupervisor extends AbstractCommandReceivingComponent {



	String args[];
	Config config;
	TopologyBuilder builder;
	Map<String, Object> hbConf;

	@Override
	public void receiveCommand(byte command, byte[] data) {

		// read into data

		if (command == Constants.TOPOLOGY_SENT ){

			// any work can be done here if needed before task generator can start its work

			if (args.length == 0) {
				LocalCluster cluster = new LocalCluster();
				cluster.submitTopology("temp", config, builder.createTopology());
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				cluster.killTopology("temp");
				cluster.shutdown();
				System.exit(0);
			}
			else if (args.length == 1) {
				LocalCluster cluster = new LocalCluster();
				cluster.submitTopology("wordCounter", config, builder.createTopology());
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				cluster.killTopology("wordCounter");
				cluster.shutdown();
				System.exit(0);
			}
			else if(args.length == 2) {
				config.setNumWorkers(3);
				try {
					StormSubmitter.submitTopology(args[1], config, builder.createTopology());
				} catch (AlreadyAliveException e) {
					e.printStackTrace();
				} catch (InvalidTopologyException e) {
					e.printStackTrace();
				}
			} else{
				System.out.println("Usage: TridentFileTopology <hdfs url> [topology name]");
			}
		}
	}

	@Override
	public void init() throws Exception {

		// read env

		// set conf

		config = new Config();
		builder = new TopologyBuilder();

		hbConf = new HashMap<String, Object>();

		if(args.length > 0){
			hbConf.put("hbase.rootdir", args[0]);
		}
		config.put("hbase.conf", hbConf);

	}

	@Override
	public void run() throws Exception {

		this.sendToCmdQueue(1);

		config.setMaxSpoutPending(5);


	}


	@Override
	public void close() throws IOException {

	}

}
*/