package org.hobbit.sdk.iotpipeline_bm.system.kafka;

import org.hobbit.core.components.AbstractPlatformConnectorComponent;
import org.hobbit.sdk.iotpipeline_bm.Commands;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * Created by bushranazir on 24.03.2019.
 */
public class KafkaManager extends AbstractPlatformConnectorComponent {

	private static final Logger logger = LoggerFactory.getLogger(KafkaManager.class);

	String containerId;
	private Semaphore startKafkaMutex = new Semaphore(0);
	private Semaphore terminateKafkaMutex = new Semaphore(0);

	@Override
	public void init() throws Exception {
		super.init();

      	this.containerId = EnvVariables.getString("HOSTNAME");
		logger.debug( " KAFKA system created. Monitoring....."  );
		}

	@Override
	public void run() throws Exception {

		this.sendToCmdQueue(Commands.KAFKA_READY_SIGNAL);

		this.startKafkaMutex.acquire();

		checkStatus();

		this.terminateKafkaMutex.acquire();

	}


	private void checkStatus() throws Exception{
		logger.debug( " Kafka  checkStatus.  " + containerId );
	}

	@Override
	public void receiveCommand(byte command, byte[] data) {
		if(command == Commands.START_KAFKA_SIGNAL) {
			this.startKafkaMutex.release();
		}

		super.receiveCommand(command, data);
	}

	@Override
	public void close() throws IOException {
		super.close();
	}


}
