package org.hobbit.sdk.iotpipeline_bm.system.storm;

import org.hobbit.core.components.AbstractPlatformConnectorComponent;
import org.hobbit.sdk.iotpipeline_bm.Commands;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * Created by bushranazir on 24.04.2019.
 */
public class StormSupervisor extends AbstractPlatformConnectorComponent {
	private static final Logger logger = LoggerFactory.getLogger(StormSupervisor.class);

	String containerId;
	private Semaphore startStormSupervisorMutex = new Semaphore(0);
	private Semaphore terminateStormSupervisorMutex = new Semaphore(0);

	@Override
	public void init() throws Exception {
		super.init();

		this.containerId = EnvVariables.getString("HOSTNAME");

	}

	@Override
	public void run() throws Exception {

		this.sendToCmdQueue(Commands.STORM_SUPERVISOR_READY_SIGNAL);
		logger.debug( "Storm supervisor started. Monitoring...  " + this.containerId);

		this.startStormSupervisorMutex.acquire();

		checkStatus();

		this.terminateStormSupervisorMutex.acquire();

	}


	private void checkStatus() throws Exception{
		logger.debug( " >>>>>>>>>>>>>>>>>>>>>>>>>>> StormSupervisor   checkStatus  " + containerId );
	}

	@Override
	public void receiveCommand(byte command, byte[] data) {
		if(command == Commands.START_STORM_SUPERVISOR_SIGNAL) {
			this.startStormSupervisorMutex.release();
		}

		super.receiveCommand(command, data);
	}

	@Override
	public void close() throws IOException {
		super.close();
	}
}
