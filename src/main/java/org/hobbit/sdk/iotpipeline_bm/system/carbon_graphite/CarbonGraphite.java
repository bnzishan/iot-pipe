package org.hobbit.sdk.iotpipeline_bm.system.carbon_graphite;

import org.hobbit.core.components.AbstractPlatformConnectorComponent;
import org.hobbit.sdk.iotpipeline_bm.Commands;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * Created by bushranazir
 */
public class CarbonGraphite extends AbstractPlatformConnectorComponent {

	private static final Logger logger = LoggerFactory.getLogger(CarbonGraphite.class);

	String containerId;
	private Semaphore startCarbonGraphiteMutex = new Semaphore(0);
	private Semaphore terminateCarbonGraphiteMutex = new Semaphore(0);

	@Override
	public void init() throws Exception {
		super.init();

		this.containerId = EnvVariables.getString("HOSTNAME");
		logger.debug( " Graphite+Carbon system created. Monitoring....."  );
	}

	public void run() throws Exception {

			checkStatus();
			this.sendToCmdQueue(Commands.CARBON_GRAPHITE_READY_SIGNAL);

			// this.startCarbonGraphiteMutex.acquire();
			this.terminateCarbonGraphiteMutex.acquire();

		}




	private void checkStatus() throws Exception{
		logger.debug( " carbon-graphite  checkStatus.  " + containerId );
	}

	@Override
	public void receiveCommand(byte command, byte[] data) {
		if(command == Commands.START_CARBON_GRAPHITE_SIGNAL) {
			this.startCarbonGraphiteMutex.release();
		}

		super.receiveCommand(command, data);
	}

	@Override
	public void close() throws IOException {
		super.close();
	}


}
