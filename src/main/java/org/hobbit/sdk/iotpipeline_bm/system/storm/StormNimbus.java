package org.hobbit.sdk.iotpipeline_bm.system.storm;

import org.apache.commons.io.IOUtils;
import org.hobbit.core.components.AbstractPlatformConnectorComponent;
import org.hobbit.core.rabbit.DataHandler;
import org.hobbit.core.rabbit.DataReceiver;
import org.hobbit.core.rabbit.DataReceiverImpl;
import org.hobbit.sdk.iotpipeline_bm.Commands;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;



/**
 * Created by bushranazir
 */
public class StormNimbus extends AbstractPlatformConnectorComponent {
	private static final Logger logger = LoggerFactory.getLogger(StormNimbus.class);

	String containerId;
	private Semaphore startStormMutex = new Semaphore(0);
	private Semaphore terminateStormMutex = new Semaphore(0);
	private final int maxParallelProcessedMsgs = 1;
	String zookeeperConnectStr;

	// for data locality - topology might be run from the nimbus container
	DataReceiver topologyReceiver;

	@Override
	public void init() throws Exception {
		super.init();
		this.containerId = EnvVariables.getString("HOSTNAME");


		// for data locality - topology might be run from the nimbus container
		// receiving topology from System Adapter
		this.topologyReceiver = DataReceiverImpl.builder().dataHandler(new DataHandler() {
			public void handleData(byte[] data) {
				StormNimbus.this.runTopology(data);
			}
		}).maxParallelProcessedMsgs(this.maxParallelProcessedMsgs).queue(this.getFactoryForIncomingDataQueues(), this.generateSessionQueueName("hobbit.nimbus-topo")).build();


	}
	// TODO
	private void runTopology(byte[] data) {

	}

	@Override
	public void run() throws Exception {

		this.sendToCmdQueue(Commands.STORM_NIMBUS_READY_SIGNAL);
		logger.debug( "Storm nimbus  started with container id ->  " ,  this.containerId);

		this.startStormMutex.acquire();

		checkStatus();

		this.terminateStormMutex.acquire();

		this.topologyReceiver.closeWhenFinished();


	}


	private void checkStatus() throws Exception{
		logger.debug( "StormNimbus   health check...  " + containerId );
	}

	@Override
	public void receiveCommand(byte command, byte[] data) {
		if(command == Commands.START_STORM_NIMBUS_SIGNAL) {
			this.startStormMutex.release();
		}

		super.receiveCommand(command, data);
	}

	@Override
	public void close() throws IOException {

		IOUtils.closeQuietly(this.topologyReceiver);
		super.close();
	}
}
