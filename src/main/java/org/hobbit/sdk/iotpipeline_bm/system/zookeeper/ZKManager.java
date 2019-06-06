package org.hobbit.sdk.iotpipeline_bm.system.zookeeper;

import com.rabbitmq.client.*;
import org.apache.commons.io.IOUtils;
import org.hobbit.core.components.AbstractPlatformConnectorComponent;
import org.hobbit.core.rabbit.DataReceiver;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.rabbit.RabbitQueueFactory;
import org.hobbit.sdk.iotpipeline_bm.Commands;
import org.hobbit.sdk.iotpipeline_bm.Constants;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import static org.hobbit.core.Constants.*;


/**
 * Created by bushranazir on 24.03.2019.
 */
public class ZKManager  extends AbstractPlatformConnectorComponent {

	private static final Logger logger = LoggerFactory.getLogger(ZKManager.class);


	int clusterSize;
	String zookeeperEnsembleContainerIds;
	DataReceiver configReceiver;
	private int maxParallelProcessedMsgs;

	private Semaphore configMutex;
	private Semaphore waitForconfigMutex;
	private Semaphore startZkMutex;
	private Semaphore terminateZkMutex;
	private Semaphore causeMutex;
	private Exception cause;
	private CountDownLatch configLatch;
	private CountDownLatch startLatch;

	String containerId;
	private String zkId  = null;
	Boolean zookeeperStartFLag;

	private RabbitQueueFactory rabbitQueueFactory;
	private Connection connection;
	private Channel channel;
	private String defaultQueueName;



	public ZKManager() {
		super();

		this.maxParallelProcessedMsgs =1;
		zookeeperEnsembleContainerIds = null;
		configMutex = new Semaphore(0);
		waitForconfigMutex= new Semaphore(0);
		this.causeMutex = new Semaphore(1);

		this.configLatch = new CountDownLatch(1);
		this.startLatch = new CountDownLatch(1);

		this.terminateZkMutex = new Semaphore(0);
		this.startZkMutex = new Semaphore(0);
	}




	//----------------------------------------------------------------------------------------------------------------------------------------------

	public void init() throws Exception {
		super.init();
		//this.env1 = EnvVariables.getInt("ZK-KEY1");

		logger.info("Monitoring ZKManager: " + EnvVariables.getString("HOSTNAME", logger) );

		containerId = EnvVariables.getString("HOSTNAME");
		checkEnvVars();

		zookeeperStartFLag = false;

		clusterSize = Integer.parseInt(System.getenv(Constants.ZOOKEEPER_CLUSTER_SIZE));

		/*

		this.configReceiver = DataReceiverImpl.builder().dataHandler(new DataHandler() {
			public void handleData(byte[] data) {
				//final byte[] utf8Bytes = string.getBytes("UTF-8");
				//	System.out.println(utf8Bytes.length);
				//ByteBuffer buffer = ByteBuffer.wrap(data);
				String zkContIds = RabbitMQUtils.readString(data); //comma separated list of zk cont ids that make the ensemble
				zookeeperEnsembleContainerIds = zkContIds;

				logger.info(">>>>>>>>>>>>>>>>>>  zookeeperEnsembleContainerIds:: " + zookeeperEnsembleContainerIds);
				ZKManager.this.config(zookeeperEnsembleContainerIds);

			}
		}).maxParallelProcessedMsgs(this.maxParallelProcessedMsgs).queue(this.getFactoryForIncomingDataQueues(),
				this.generateSessionQueueName(SA_2_ZK_QUEUE_NAME)).build();

				*/

		zkId = EnvVariables.getString("ID");


		defaultQueueName = "zk-config-queue-" + zkId;


		try{
			rabbitQueueFactory = this.getFactoryForIncomingDataQueues();
			connection = rabbitQueueFactory.getConnection();
			if(connection != null) {
				channel = connection.createChannel();
				// Consumer reading from default queue
				Consumer consumer = new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

						String zkContIds = RabbitMQUtils.readString(body); //comma separated list of zk cont ids that make the ensemble
						zookeeperEnsembleContainerIds = zkContIds;

						config4(zookeeperEnsembleContainerIds);
					}
				};
				channel.basicConsume(defaultQueueName, true, consumer);
			}
		}catch(Exception e){
			e.printStackTrace();
		}


	}




	//----------------------------------------------------------------------------------------------------------------------------------------------


	@Override
	public void run() throws Exception {

		this.sendToCmdQueue(Commands.ZOOKEEPER_INIT_SIGNAL);     // intialization done, send ZKManager init finished signal

		this.startZkMutex.acquire();

		checkStatus();
		//this.configReceiver.closeWhenFinished();
		if(zookeeperStartFLag){
			channel.close();
			connection.close();
		}


		this.terminateZkMutex.acquire();




		/*
		try {
			this.causeMutex.acquire();
			if(this.cause != null) {
				throw this.cause;
			}

			this.causeMutex.release();
		} catch (InterruptedException var2) {
			logger.error("Interrupted while waiting to set the termination cause.");
		}*/

	}

	//----------------------------------------------------------------------------------------------------------------------------------------------


	@Override
	public void receiveCommand(byte command, byte[] data) {


		if(command == 500) { // TODO set command appropriately

			ByteBuffer buffer = ByteBuffer.wrap(data);
			String config_or_configFile = RabbitMQUtils.readString(buffer);

			// 	this.startZKManagerMutex.release(); // TODO release mutex!
		}

		else

		if (command == Commands.SA2ZK_CONFIG_SENT){
		//	configLatch.countDown();
		//	logger.debug(">>>>>>>>>>>>>>>>>> ", configLatch);
			logger.info(">>>>>>>>>>>>>>>>>>>  SA has sent the config! ");
		} else if(command == Commands.START_ZK_SIGNAL) {
			logger.info("Received signal to start.");
			this.startZkMutex.release();
		} else if(command == Commands.FINISH_ZK_SIGNAL) {
			logger.info("Received signal to finish.");
			this.terminateZkMutex.release();
		}


		super.receiveCommand(command, data);
	}
	//----------------------------------------------------------------------------------------------------------------------------------------------



	synchronized void terminate(Exception cause) {
		if(cause != null) {
			try {
				this.causeMutex.acquire();
				this.cause = cause;
				this.causeMutex.release();
			} catch (InterruptedException var3) {
				logger.error("Interrupted while waiting to set the termination cause.");
			}
		}

		this.terminateZkMutex.release();
	}

	//----------------------------------------------------------------------------------------------------------------------------------------------


	@Override
	public void close() throws IOException {

		IOUtils.closeQuietly(this.configReceiver);

		super.close();
	}
//----------------------------------------------------------------------------------------------------------------------------------------------


	private void checkEnvVars() {

		logger.debug( (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY) );
		logger.debug( (String)System.getenv().get(HOBBIT_SESSION_ID_KEY) );
		logger.debug( (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY) );
	}
	//----------------------------------------------------------------------------------------------------------------------------------------------

	private void config2(String zkContIds) {

		String scriptFilePath = System.getProperty("user.dir") + File.separator + "zookeeper"  + File.separator + "bin/zk-config";  // TODO make configurable
		String[] command = {"/bin/bash", scriptFilePath, zkContIds};

		try {
		final Process process = new ProcessBuilder(command).redirectErrorStream(true).start();

		Thread t = new Thread(new Runnable() {
			@Override
			public void run () {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = null;
				try {
					while ( (line = reader.readLine()) != null) {
						logger.info(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.setDaemon(true);
		t.start();
		process.waitFor();
		t.interrupt();
		if( process.exitValue() == 0 ){

				this.waitForconfigMutex.release();

		}
	} catch (Exception e) {
		e.printStackTrace();
	}


}
	//----------------------------------------------------------------------------------------------------------------------------------------------

	private void checkStatus(){
		logger.debug( " >>>>>>>>>>>>>>>>>>>>>>>>>>> ZKManager   checkStatus  " + containerId );

		String scriptFilePath = System.getProperty("user.dir") + File.separator + "zookeeper-3.5.2-alpha"  + File.separator + "bin/zkServer.sh";  // TODO make configurable
		String[] command = {"/bin/bash", scriptFilePath, "status"};
		Process p;
		try {
			p = new ProcessBuilder(command).redirectErrorStream(true).start();  //pb.redirectError(new File("/dev/null"));

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = null;
			while ( (line = reader.readLine()) != null) {
				logger.info(line);
			}

			p.waitFor();

			if( p.exitValue() == 0 ){

				logger.debug("process executed! ");

					}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	//----------------------------------------------------------------------------------------------------------------------------------------------


	private void config4(String zkContIds) {

		logger.debug( " >>>>>>>>>>>>>>>>>>>>>>>>>>> ZKManager   Config4() ---- zkContIds  " + zkContIds );

		String scriptFilePath = System.getProperty("user.dir") + File.separator + "zookeeper-3.5.2-alpha"  + File.separator + "bin/zk-config";  // TODO make configurable
		String[] command = {"/bin/bash", scriptFilePath, zkContIds};
		Process p;
		try {
			p = new ProcessBuilder(command).redirectErrorStream(true).start();  //pb.redirectError(new File("/dev/null"));

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = null;
			while ( (line = reader.readLine()) != null) {
				logger.info(line);
			}


				logger.debug("config done! ");

				logger.error(">>>>>>>>>>>>>>>>>>>  send ZOOKEEPER_START_SIGNAL ");
				zookeeperStartFLag = true;

				this.sendToCmdQueue(Commands.ZOOKEEPER_START_SIGNAL);    // update-configuration done, send Zookeeper is configured and running

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

//----------------------------------------------------------------------------------------------------------------------------------------------


	private void config(String zkContIds) {

		logger.debug( " >>>>>>>>>>>>>>>>>>>>>>>>>>> ZKManager   Config() ---- zkContIds  " + zkContIds );

		String scriptFilePath = System.getProperty("user.dir") + File.separator + "zookeeper-3.5.2-alpha"  + File.separator + "bin/zk-config";  // TODO make configurable
		String[] command = {"/bin/bash", scriptFilePath, zkContIds};
		Process p;
		try {
			p = new ProcessBuilder(command).redirectErrorStream(true).start();  //pb.redirectError(new File("/dev/null"));

    		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

    		String line = null;
    		while ( (line = reader.readLine()) != null) {
    			logger.info(line);
    		}

			p.waitFor();

			if( p.exitValue() == 0 ){

				logger.debug("config done! ");

				logger.error(">>>>>>>>>>>>>>>>>>>  send ZOOKEEPER_START_SIGNAL ");
				zookeeperStartFLag = true;

				this.sendToCmdQueue(Commands.ZOOKEEPER_START_SIGNAL);    // update-configuration done, send Zookeeper is configured and running

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

//----------------------------------------------------------------------------------------------------------------------------------------------


	public void waitForConfig() throws Exception {


		String errorMsg=null;
			try {
				configLatch.await();
			} catch (InterruptedException e) {
				errorMsg = "Interrupted while waiting for config.";
			}  logger.error(errorMsg);
	}



	public void waitForConfig2() throws Exception {

		String errorMsg;
		try {
			this.waitForconfigMutex.acquire();
			logger.debug(">>>>>>>>>>>>>>>>>>>  waiting for config...  waitForconfigMutex acquired! ");
		} catch (InterruptedException var1) {
			errorMsg = "Interrupted while waiting for zk container to be initialized.";
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg, var1);
		}
	}



} // end class
