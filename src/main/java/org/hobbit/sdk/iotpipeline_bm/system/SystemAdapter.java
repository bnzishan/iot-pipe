package org.hobbit.sdk.iotpipeline_bm.system;


import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableList;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.core.rabbit.DataSender;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.rabbit.RabbitQueueFactory;
import org.hobbit.core.rabbit.SimpleFileReceiver;
import org.hobbit.sdk.iotpipeline_bm.rabbit.FanoutExchange;
import org.hobbit.sdk.iotpipeline_bm.system.kafka.KafkaEventProducer;
import org.hobbit.sdk.iotpipeline_bm.system.kafka.clients.CsvReader;
import org.hobbit.sdk.iotpipeline_bm.system.kafka.clients.KafkaSensorDataProducer;
import org.hobbit.sdk.iotpipeline_bm.system.kafka.models.SensorData;
import org.hobbit.sdk.iotpipeline_bm.system.kafka.models.SensorDataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.applications.StormRunner;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;

import static org.hobbit.core.Commands.DOCKER_CONTAINER_TERMINATED;
import static org.hobbit.sdk.iotpipeline_bm.Commands.*;
import static org.hobbit.sdk.iotpipeline_bm.Constants.*;


public class SystemAdapter extends AbstractSystemAdapter {
	private static final Logger logger = LoggerFactory.getLogger(SystemAdapter.class);

	//  private Map<String, String> parameters = new HashMap<>();


	/*
	* for reading param model
	*/
	NodeIterator iterator = null;

	/*
	* for carbon-graphite
	*/
	private String graphiteContainerID = null;
	private Semaphore graphiteReadyMutex = new Semaphore(0);


	/*
	* for dategen
	*/
	private String pathToDatasetFolder = "";


	/*
	* for taskgen
	*/
	private String taskDir = "";

	/*
	* ZOOKEEPER Variables
	*/

	int zookeeperClusterSize;
	private String[] zkEnvVariables;
	private Set<String> zooContainerIdsSet = new HashSet();
	private String[] zookeeperContainerIDs, zookeeperHostnames, zookeeperContainerNames;

	//connect string to be used by containers
	String zookeeperConnectStr = "";

	DataSender sender2ZK;

	int numOfInitializedZookeeperContainers = 0;
	int numOfRunningZookeeperContainers = 0;
	Boolean zookeeperClusterInitFlag = false;
	Boolean zookeeperClusterRunningFlag = false;

	private CountDownLatch zkInitLatch;
	private CountDownLatch zkStartLatch;
	private CountDownLatch zkClusterStartLatch = new CountDownLatch(1);
	private CountDownLatch zkClusterInitLatch = new CountDownLatch(1);

	private Semaphore zkManagerReadyMutex = new Semaphore(0);
	private Semaphore zookeeperStartMutex = new Semaphore(0);
	private Semaphore zookeeperInitMutex = new Semaphore(0);
	//private Semaphore zManagerTerminatedMutex = new Semaphore(0);


	/*
	* KAFKA Variables
	*/
	int kafkaClusterSize;
	private String[] kafkaEnvVariables;
	private Set<String> kafkaContainerIdsSet = new HashSet();
	private String[] kafkaContainerIDs = null;
	private Semaphore kafkaReadyMutex = new Semaphore(0);


	/*
	* STORM Variables
	*/
	private String[] stormEnvVariables;
	int numOfStormSupervisors;
	int numOfStormNimbus = 1;

	private String stormNimbusContainerID = null;
	//	private Set<String> stormContainerIdsSet = new HashSet();
	private String[] stormNimbusContainerIDs = null;
	private String[] stormSupervisorContainerIDs = null;

	private Semaphore stormNimbusReadyMutex = new Semaphore(0);
	private Semaphore stormSupervisorReadyMutex = new Semaphore(0);


	/*
	* for forwardToPipeline() method
	*/
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	/*
	* for storm
	*/
	private SimpleFileReceiver sourceReceiver;
	private SimpleFileReceiver targetReceiver;
	DataSender sender;
	private Semaphore alldataReceivedMutex = new Semaphore(0);
	private boolean dataLoadingFinished = false;


	//----------------------------------------------------------------------------------------------------------------------------------------------


	@Override
	public void init() throws Exception {

		long time = System.currentTimeMillis();
		super.init();
		logger.info("=======================  =======  IoT Pipeline Composition   ======= ===============  ");


//		iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + ZOOKEEPER_INIT_STEP));
		//String zookeeper_init_step = iterator.next().asLiteral().getString();
		int zookeeper_init_step = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + ZOOKEEPER_INIT_STEP, 1);


		//iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + KAFKA_INIT_STEP));
	//	String kafka_init_step = iterator.next().asLiteral().getString();
		int kafka_init_step = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + KAFKA_INIT_STEP, 2);

		//iterator = systemParamModel.listObjectsOfProperty(systemParamModel .getProperty(SYSTEM_PARAM_URI_PREFIX + HBASE_INIT_STEP));
		//String hbase_init_step = iterator.next().asLiteral().getString();
		int hbase_init_step = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + HBASE_INIT_STEP, 3);


	//	iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + STORM_INIT_STEP));
	//	String storm_init_step = iterator.next().asLiteral().getString();
		int storm_init_step = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + STORM_INIT_STEP, 4);




		// create graphite container
		/*setupCarbonGraphite();
		this.sendToCmdQueue(CARBON_GRAPHITE_READY_SIGNAL);
		logger.info(" graphite container  setup is successfully finished. ");*/

		// create graphan-graphite container
		 setupGraphite();


		//create zookeeper cluster
		setupZookeeper();
		this.sendToCmdQueue(START_ZK_SIGNAL);
		logger.info(" zookeeper cluster  setup is successfully finished. ");

		//create kafka cluster
		setupKafkaCluster();
		this.sendToCmdQueue(START_KAFKA_SIGNAL);
		logger.info(" kafka cluster  setup is successfully finished. ");

		setupStormCluster();
		logger.info(" storm cluster  setup is successfully finished. ");

		setupHbaseCluster();
		logger.info(" hbase cluster  setup is successfully finished. ");

		//broadcast zookeeper connect string
		sendToCmdQueue(ZOOKEEPER_CONNECT_SIGNAL, zookeeperConnectStr.getBytes());

		// receiving simple file
		//SimpleFileReceiver simpleFileReceiver = SimpleFileReceiver.create(this.incomingDataQueueFactory, "source_file");
		//logger.debug("------------------->>>>>>------------------ System is ready!!! Send the signal    ");
		//this.sendToCmdQueue(SYSTEM_READY_SIGNAL);


		logger.info("All systems are initialized. It took {} ms.", System.currentTimeMillis() - time);

		//	printClusterConfigForDoc();  // TODO FIX

	}


	//----------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void receiveGeneratedData(byte[] data) {
		// handle the incoming data as described in the benchmark description
		logger.info(" Receive Data From Data Generators... ");

		long time = System.currentTimeMillis();

		// ------------------  Dataset folder for data recevied from Data Generators
	//	iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty("http://project-hobbit.eu/iotpipe-system/datasetFolderName"));
	//	String dataset_folder_name = iterator.next().asLiteral().getString();
		String dataset_folder_name = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX+DATASET_FOLDER_NAME, "datasets");

				File theDir = new File(dataset_folder_name);
		if (!theDir.exists())
			theDir.mkdir();

		pathToDatasetFolder = System.getProperty("user.dir") + File.separator + dataset_folder_name;


		ByteBuffer dataBuffer = ByteBuffer.wrap(data);

		// read the file name
		String fileName = RabbitMQUtils.readString(dataBuffer);

		logger.info(" Receiving Data {} from Data Generator......", fileName);

		// read the data contents
		byte[] dataContentBytes = new byte[dataBuffer.remaining()];
		dataBuffer.get(dataContentBytes, 0, dataBuffer.remaining());

		if (dataContentBytes.length != 0) {
			try {
				if (fileName.contains("/")) {
					fileName = fileName.replaceAll("[^/]*[/]", "");
				}
				//FileUtils.writeByteArrayToFile(new File(datasetFolderName + File.separator + fileName), dataContentBytes);
				FileUtils.writeByteArrayToFile(new File(pathToDatasetFolder + File.separator + fileName), dataContentBytes);
				logger.info(" Data {} recevied from Data Generator. ", fileName);
			} catch (IOException e) {
				logger.error("Exception while writing data file", e);
			}
		}

		/*
		ByteBuffer dataBuffer = ByteBuffer.wrap(data);
    	String fileName = RabbitMQUtils.readString(dataBuffer);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream( System.getProperty("user.dir") + File.separator + "datasets" + File.separator + fileName);
			fos.write(RabbitMQUtils.readByteArray(dataBuffer));
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/


		/*
		try {
            ByteBuffer dataBuffer = ByteBuffer.wrap(data);
            // read the file path
            dataFormat = RabbitMQUtils.readString(dataBuffer);
            receivedGeneratedDataFilePath = RabbitMQUtils.readString(dataBuffer);

            String[] receivedFiles = sourceReceiver.receiveData("./datasets/SourceDatasets/");
            // LOGGER.info("receivedFiles 1 " + Arrays.toString(receivedFiles));
            receivedGeneratedDataFilePath = "./datasets/SourceDatasets/" + receivedFiles[0];
            logger.info("Received data from receiveGeneratedData. It took {}ms.", System.currentTimeMillis() - time);

        } catch (IOException | ShutdownSignalException | ConsumerCancelledException | InterruptedException ex) {
            logger.error("Got an exception while receiving generated data.", ex);
        }
        */

	}

	//----------------------------------------------------------------------------------------------------------------------------------------------


	public void receiveGeneratedTask(String taskId, byte[] data) {

		long time = System.currentTimeMillis();
		logger.info(" Receive Tasks with taskId {} From Task Generators  ", taskId, time);

		ByteBuffer buffer = ByteBuffer.wrap(data);

		String taskName = "spike_detection"; // ToDO :  remove hardcode, extract from incoming data
		// create folder to store incoming tasks
		taskDir = "";
		//taskDir = System.getProperty("user.dir") + File.separator + taskId;
		taskDir = System.getProperty("user.dir") + File.separator + taskName;

		//Files.createDirectories(Paths.get(taskDir)); // not creating
		File theDir = new File(taskDir);
		if (!theDir.exists())
			theDir.mkdir();
		logger.info("directory created : {}     ", taskDir);


		// ::  READ  task-name-string from data ::
/*
		int taskNameSize = buffer.getInt();
		byte[] taskNameInBytes = new byte[taskNameSize];
		buffer.get(taskNameInBytes);
		String taskName = taskNameInBytes.toString();
		logger.info("task name : {}     ", taskName);
*/

		switch (taskName) {

			case "spike_detection":


				// send data to kafka
				try {
					forwardDataToKafka();
				} catch (Exception e) {
					e.printStackTrace();
				}


				// :: read  config file from data ::

				// read config file size, stored in first 4 bytes by task gen
				int configFileSize = buffer.getInt();
				// read config file, using file size read in configFileSize
				byte[] configFileInBytes = new byte[configFileSize];
				buffer.get(configFileInBytes);
				try {
					FileUtils.writeByteArrayToFile(new File(taskDir + "/spike-detection.properties"), configFileInBytes);
					logger.info("task configuration file size for ({})->{}     ", taskId, Files.size(Paths.get(taskDir + "/spike-detection.properties")));

				} catch (IOException e) {
					e.printStackTrace();
				}


			// :: read  task  from data ::

				int workloadFileSize = buffer.remaining();
				byte[] workloadFileInBytes = new byte[workloadFileSize];
				buffer.get(workloadFileInBytes, 0, workloadFileSize);
				logger.info(" task  size in bytes for ({})->{}     ", taskId, workloadFileSize);
				try {
					//FileUtils.writeByteArrayToFile(new File(taskDir + "/" + taskId  + "spike_detection.jar"), workloadFileInBytes);

					String workloadFileName = taskId + "_" + "spike_detection.jar"; // TODO  :  remove name shortcut and read from data bytes instead

					FileUtils.writeByteArrayToFile(new File(taskDir + "/" + taskId  + "_spike_detection.jar"), workloadFileInBytes);

				} catch (IOException e) {
					e.printStackTrace();
				}
				//	logger.info("task size in bytes for workload: ({})->{}     ", taskId, Files.size(Paths.get(taskDir + "/" + taskName + ".jar")));

				//	String topologyJarFile = taskDir + "/spike-detection.jar";

				/*
				 Currently Submitting the task (storm topology to storm cluster remotely) via bs4dsps api
				  */
				StormRunner runner = new StormRunner();
				runner.mode = "remote";
				String configStr = null;
				try {
					configStr = new String(Files.readAllBytes(Paths.get(taskDir + "/spike-detection.properties")));
					logger.info(" Submitting task (storm topology) to the cluster  with  configuration:->  {} ", configStr);
				} catch (IOException e) {
					e.printStackTrace();
				}
				runner.configStr = configStr;
				logger.info(" Kafka Zookeeper connect string:->  {} ", zookeeperConnectStr);
				runner.zKKafkaConnectStr = zookeeperConnectStr;
				runner.application = taskName.replace("_", "-");
				runner.topologyName = taskName.replace("_", "-");
				runner.nimbusHost = stormNimbusContainerIDs[0];
				runner.thriftPort = 6627;
		//		File f = new File(taskDir + "/" + taskName + ".jar");    // in production env, this should be sent over n/w via eg SimpleFilerSender
				File f = new File(taskDir + "/" + taskId  + "_spike_detection.jar");    // in production env, this should be sent over n/w via eg SimpleFilerSender

				runner.topologyJarLocation = f.getAbsolutePath();

				long start_time = System.currentTimeMillis();

				ExecutorService executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1));
				executor.execute(new Runnable() {
					public void run() {
						try {
							runner.run();
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (backtype.storm.generated.AlreadyAliveException e) {
							e.printStackTrace();
						} catch (backtype.storm.generated.InvalidTopologyException e) {
							e.printStackTrace();
						}
					}
				});
				executor.shutdown();

			/*
				runner.setTopologyName(taskName);
				runner.setApplication(taskName);
				runner.setConfigStr( taskDir + "/spike-detection.properties"); // if not sent, default will be read by the storm-runner
				runner.setNimbusHost(stormNimbusContainerIDs[0]);
				runner.setNimbusThriftPort(String.valueOf(6627));
			*/

				long currentTimeMillis = System.currentTimeMillis();
				long finish_time = currentTimeMillis - start_time;

				//logger.info(" Total  execution time in ms :: " + finish_time );
				byte[] execTime = RabbitMQUtils.writeLong(finish_time);
				try {
					logger.info(" sending results to evaluation module.... ");
					this.sendResultToEvalStorage(taskId, execTime);
				} catch (IOException e) {
					logger.error("Got an exception while sending results.", e);
				}

				/*
				 finished : Submit the task via bs4dsps api
				  */


				//	System.out.println("Workload successfully submitted to storm cluster !    ");


		}


		/*
		//System.getProperty returns absolute path
		File f = new File(System.getProperty("user.dir")+ taskId + "spike-detection.properties");
		if(!f.getParentFile().exists()){
			f.getParentFile().mkdirs();
		}
		//Remove if clause if you want to overwrite file
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			//dir will change directory and specifies file name for writer
			File dir = new File(f.getParentFile(), f.getName());
			PrintWriter writer = new PrintWriter(dir);
			writer.print("writing anything...");
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		*/

		/*

		File dir = new File("tmp/test");
		dir.mkdirs();
		File tmp = new File(dir, "tmp.txt");
		tmp.createNewFile();

		 */

				/*
		if (taskString.contains("get data")) {
			taskString = taskString.replaceFirst("INSERT DATA", "INSERT");
			taskString += "WHERE { }\n";

		}
*/

		/*

			//load relevant workload properties
				try (BufferedReader br = new BufferedReader(
						new FileReader(getClass().getClassLoader().getResource(
								"tasks/storm_tasks/" + taskName + "/config/" + taskName + ".properties")
								.getFile()))) {
					String line;
					while ((line = br.readLine()) != null) {
						String[] parts = line.split("=");
						props.put(parts[0], parts[1]);
					}
**/

		/*
		String receiveGeneratedTaskPath = null;
		try{
			targetReceiver = SingleFileReceiver.create(this.incomingDataQueueFactory, "task_target_file");
			String[] receivedFiles = targetReceiver.receiveData("./datasets/TargetDatasets");
			receiveGeneratedTaskPath = "./datasets/TargetDatasets" + receivedFiles[0];
		}catch (Exception e){
			logger.error ("exception while trying to receive data, aborting ", e);
		}
		 */


	}

	//----------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void receiveCommand(byte command, byte[] data) {


		if (command == DATA_GEN_FINISHED) {

			try {
				//  forwardDataToPipeline();
				logger.info("data gen is finished, and all the data has been received by system adapter. ");
			} catch (Exception e) {
				e.printStackTrace();
			}
			/*
			try {

				String datasetsFolderName = System.getProperty("user.dir") + File.separator + "datasets";
				File theDir = new File(datasetsFolderName);
				FileUtils.deleteDirectory(theDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			*/

		} else if (command == ZOOKEEPER_INIT_SIGNAL) {
			logger.debug("Received ZKMANAGER_INIT_SIGNAL");
			this.zookeeperInitMutex.release();
		} else if (command == ZOOKEEPER_START_SIGNAL) {
			logger.debug("Received ZKMANAGER_START_SIGNAL");
			this.zookeeperStartMutex.release();
		} else if (command == KAFKA_READY_SIGNAL) {
			logger.debug("Received KAFKA_READY_SIGNAL");
			this.kafkaReadyMutex.release();
		} else if (command == STORM_NIMBUS_READY_SIGNAL) {
			logger.debug("Received STORM_NIMBUS_READY_SIGNAL");
			this.stormNimbusReadyMutex.release();
		} else if (command == STORM_SUPERVISOR_READY_SIGNAL) {
			logger.debug("Received STORM_SUPERVISOR_READY_SIGNAL");
			this.stormSupervisorReadyMutex.release();
		} else if (command == DOCKER_CONTAINER_TERMINATED) {
			ByteBuffer buffer = ByteBuffer.wrap(data);
			String containerName = RabbitMQUtils.readString(buffer);
			byte exitCode = buffer.get();
			this.containerTerminated(containerName, exitCode);
		}else if(command == CARBON_GRAPHITE_READY_SIGNAL)
		{
			this.graphiteReadyMutex.release();
		}


				/*


		switch(command) {
			case 101:
				logger.debug("Received ZK2SA_INIT_FINISHED");
				this.zookeeperInitMutex.release();
				break;
			case 102:
				logger.debug("Received ZK2SA_READY_SIGNAL");
				this.zookeeperReadyMutex.release();
				break;
			case 103:
			case 104:
			case 105:
			case 106:
			case 107:
			default:
				break;
			case 108:
				//this.setResultModel(RabbitMQUtils.readModel(data));
				logger.info("108: do something");
				break;
			case 109:
				logger.info("109: do something");
				ByteBuffer buffer = ByteBuffer.wrap(data);
				String str = RabbitMQUtils.readString(buffer);
				byte exitCode = buffer.get();
				this.containerTerminated(containerName, exitCode);
				break;

			case 110:
		}

		*/

		super.receiveCommand(command, data);

	}


	//----------------------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void close() throws IOException {
		// Free the resources you requested here
		logger.info("  close()   ");

		// TODO: close all the created  containers

		// Always close the super class after yours!
		super.close();
		logger.info("------------------  Pipeline is closed.");
	}


//----------------------------------------------------------------------------------------------------------------------------------------------

	private void containerTerminated(String containerName, int exitCode) {

		logger.debug("  Container terminated : (containerName={})", containerName);

		/*
		String zookeeperContainerIDsStr = String.join("," , this.zookeeperContainerIDs);

		if(zookeeperContainerIDsStr.contains(containerName)) {
			if(exitCode == 0) {
				this.zookeeperStartMutex.release();
			} else {
				this.containerCrashed(containerName);
			}
		}
		*/
	}
	//----------------------------------------------------------------------------------------------------------------------------------------------


	private void containerCrashed(String containerName) {
		logger.error("A component crashed (\"{}\"). Terminating.", containerName);

		// do something

		System.exit(1);
	}


//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------

	private void setupZookeeper() throws DockerCertificateException {

		logger.debug(" =====================================   Setup Zookeeper cluster =====================================  ");

		// loading zookeeper params----------------------------
		// cluster size:
		//iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + ZOOKEEPER_CLUSTER_SIZE));
	//	String zookeeper_cluster_size = iterator.next().asLiteral().getString();
		int zookeeper_cluster_size= getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + ZOOKEEPER_CLUSTER_SIZE, 3);

		// zookeeper client port(s):
		//iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + ZOO_CLIENT_PORTS));
		//String zoo_client_ports = iterator.next().asLiteral().getString();
		String zoo_client_ports =  getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + ZOO_CLIENT_PORTS, "31102,31202,31302");

		// zookeeper follower port(s):
		//iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + ZOO_FOLLOWER_PORTS));
		//String zoo_follower_ports = iterator.next().asLiteral().getString();
		String zoo_follower_ports =  getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + ZOO_FOLLOWER_PORTS, "31100,31200,31300");

		// zookeeper election port(s):
	//	iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + ZOO_ELECTION_PORTS));
	//	String zoo_election_ports = iterator.next().asLiteral().getString();
		String zoo_election_ports = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + ZOO_ELECTION_PORTS, "31101,31201,31301");


		zookeeperClusterSize = zookeeper_cluster_size;

		// zookeeper  environmental values
		zkEnvVariables = new String[]{
				//	ZOO_CLIENT_PORTS + "=" + zoo_client_ports,
				//	ZOO_FOLLOWER_PORTS + "=" + zoo_follower_ports,
				//	ZOO_ELECTION_PORTS + "=" + zoo_election_ports,
				ZOOKEEPER_CLUSTER_SIZE + "=" + zookeeperClusterSize
				// "ALLOW_ANONYMOUS_LOGIN" + "=" + "yes"
		};

		String[] queueNames = new String[zookeeperClusterSize];
		String queueNamePrefix = "zk-config-queue-";
		for (int c = 0; c < zookeeperClusterSize; c++) {
			int queueNamePostfix = c + 1;
			queueNames[c] = queueNamePrefix + queueNamePostfix;
		}
		RabbitQueueFactory rabbitQueueFactory1 = this.getFactoryForOutgoingDataQueues();
		String exchangeName = "fanout-exchange-for-zk-config";

		FanoutExchange fanoutExchange = new FanoutExchange();
		try {
			fanoutExchange.createExchangeAndQueues(rabbitQueueFactory1, exchangeName, queueNames);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// create containers
		zookeeperContainerIDs = createZookeeperContainers(ZOOKEEPER_IMAGE_NAME, zookeeperClusterSize, zkEnvVariables);
		logger.debug("zookeeper containers created, going to wait for initialization...");
		waitForZookeeperClusterToInit();
		logger.debug("zookeeper cluster is ready. ");

		// send ensemble information for zookeeper quorum
		String zookeeperContainerIDsStr = String.join(",", zookeeperContainerIDs);
		sendConfigToZKCluster(zookeeperContainerIDs, fanoutExchange);
		logger.debug(" zookeeper ensemble info is sent to  containers for further configuration. ");
		waitForZookeeperClusterToStart();
		logger.debug("Zookeeper cluster setup is finished. ZK containers have been started and configured successfully! ");

		createZookeeperConnect(zookeeperContainerIDs, zookeeperClusterSize, zookeeperContainerIDs, zoo_client_ports);

	}


	//----------------------------------------------------------------------------------------------------------------------------------------------
	// create  zookeeper connect string
	private void createZookeeperConnect(String[] zookeeperContainerIDs, int zookeeperClusterSize, String[] containerIDs, String zoo_client_ports) {
		String[] zooClientPorts = zoo_client_ports.split(",");
		String[] zookeeperConnectArr = new String[this.zookeeperClusterSize];
		for (int a = 0; a < this.zookeeperClusterSize; a++) {
			zookeeperConnectArr[a] = this.zookeeperContainerIDs[a] + ":" + zooClientPorts[a];
		}
		zookeeperConnectStr = String.join(",", zookeeperConnectArr);
	}


//----------------------------------------------------------------------------------------------------------------------------------------------


	private String[] createZookeeperContainers(String zookeeperImageName, int numberOfZKs, String[] envVariables) {

		logger.info("---------------- Create Zookeeper Containers  ---------------- ");

		//envVariables = (String[])ArrayUtils.add(envVariables, "HOBBIT_EXPERIMENT_URI=" + this.experimentUri);


		envVariables = (String[]) ArrayUtils.add(envVariables, "HOBBIT_RABBIT_HOST=" + this.rabbitMQHostName);

		String[] zkContainerIds = new String[numberOfZKs];
		String errorMsg;

		logger.debug("Number of zookeeper container to be created: {}   with  zookeeperImageName:  {}", numberOfZKs, zookeeperImageName);

		for (int k = 0; k < numberOfZKs; k++) { // TODO or i++ or ++i

			int id = k + 1;
			envVariables = (String[]) ArrayUtils.add(envVariables, "ID=" + id);

			String containerId = this.createContainer(zookeeperImageName, envVariables);

			if (containerId == null) {
				errorMsg = "Couldn\'t create zk component. Aborting.";
				logger.error(errorMsg);
				throw new IllegalStateException(errorMsg);
			} else {
				logger.info("zk container created with cont id: " + containerId);
				zooContainerIdsSet.add(containerId);   // unordered set
				zkContainerIds[k] = containerId; // ordered array
			}
		}


		return zkContainerIds;

	}
	//----------------------------------------------------------------------------------------------------------------------------------------------

	private void waitForZookeeperClusterToInit() {


		logger.debug("Waiting for {} zookeeper  to initialize.", Integer.valueOf(this.zookeeperClusterSize));

		String errorMsg;
		try {
			this.zookeeperInitMutex.acquire(this.zookeeperClusterSize);
		} catch (InterruptedException var4) {
			errorMsg = "Interrupted while waiting for the zookeeper to initialize.";
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg, var4);
		}

	}

//----------------------------------------------------------------------------------------------------------------------------------------------

	private void sendConfigToZKCluster(String[] zookeeperContainerIDs, FanoutExchange fanoutExchange) {

		String[] zkContIds = zookeeperContainerIDs;
		String zkContIdsStr = String.join(",", zkContIds);
		byte[] data = zkContIdsStr.getBytes();
		//	logger.debug("-------------------- data :" + data); logger.debug(Bytes.toString(data));
		byte[] dataForSending = RabbitMQUtils.writeString(zkContIdsStr);
		//	logger.debug("--------------------  dataForSending :" + dataForSending); logger.debug(Bytes.toString(dataForSending));

		try {
			RabbitQueueFactory rabbitQueueFactory2 = this.getFactoryForOutgoingDataQueues();
			Connection conn = rabbitQueueFactory2.getConnection();
			if (conn != null) {
				Channel channel = conn.createChannel();
				channel.basicPublish(fanoutExchange.exchangeName, fanoutExchange.routingKey, null, dataForSending);
				//	channel.close();
				//	conn.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			logger.debug(" System Adapter: sending signal to zookeeper containers that zk ensemble config is sent...");
			this.sendToCmdQueue(SA2ZK_CONFIG_SENT);

		} catch (IOException e) {
			logger.error(" System Adapter: error while sending config signal to zk ");
		}

	}

	//----------------------------------------------------------------------------------------------------------------------------------------------


	private void waitForZookeeperClusterToStart() {
		logger.debug(" Waiting for {} ZK containers to be started.", Integer.valueOf(this.zookeeperClusterSize));

		String errorMsg;
		try {
			this.zookeeperStartMutex.acquire(this.zookeeperClusterSize);

			logger.info("zookeeperReadyMutex acquired!!");


		} catch (InterruptedException var2) {
			errorMsg = "Interrupted while waiting for zk containers to be configured and started.";
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg, var2);
		}

	}


//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------


	private void  setupGraphite(){

		String[]  envVariables = new String[]{
				"HOBBIT_RABBIT_HOST"  + "=" +  this.rabbitMQHostName
		};

		//String containerId = this.createContainer(GRAPHITE_IMAGE_NAME,envVariables);
		String containerId = "a68073ad5f8b";
		String errorMsg="";

		if (containerId == null) {
			errorMsg = "Couldn\'t create graphite container. Aborting.";
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg);
		} else {
			logger.info(" graphite container created with cont id: " + containerId);
			graphiteContainerID = containerId;
		}

		// waitForCarbonGraphitSetup();
	}


	public void waitForGraphitSetup() {
		logger.debug(" Waiting for {} graphite  to be started.");

		String errorMsg;
		try {
			this.graphiteReadyMutex.acquire(1);

			logger.info(" graphiteReadyMutex acquired!!");


		} catch (InterruptedException var2) {
			errorMsg = "Interrupted while waiting for graphite container to be ready.";
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg, var2);
		}

	}

	//----------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------


	public void setupKafkaCluster() throws IOException {


		logger.info("=====================================    Setup Kafka cluster =====================================   ");


		// loading kafka params----------------------------
		// cluster size:
		//iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + KAFKA_CLUSTER_SIZE));
		//String kafka_cluster_size = iterator.next().asLiteral().getString();

		int kafkaClusterSize = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + KAFKA_CLUSTER_SIZE, 1);


		// zookeeper kafka hostname command:
	//	iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + KAFKA_HOSTNAME_COMMAND));
		//String kafka_hostname_command = iterator.next().asLiteral().getString();
		//String hostnameCommand = kafka_hostname_command;
		String hostnameCommand = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + KAFKA_HOSTNAME_COMMAND, "route -n | awk '/UG[ \t]/{print $$2}'");

		// zookeeper kafka create topic :
		//iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + KAFKA_CREATE_TOPICS));
		//String kafka_create_topics = iterator.next().asLiteral().getString();
		//String kafkaCreateTopics = kafka_create_topics;
		String kafkaCreateTopics = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + KAFKA_CREATE_TOPICS, "Sensors:1:1:delete");

		// add a "chroot" path which will make all kafka data for this cluster appear under a particular path.
		//String kafkaZookeeperConnect= zookeeperConnect"/kafka";  //pre generated

		String kafkaZookeeperConnect = getZookeeperConnectStr();
		// kafka environmental values
		kafkaEnvVariables = new String[]{
				//KAFKA_CLUSTER_SIZE + "=" + zookeeperClusterSize,
				"HOSTNAME_COMMAND" + "=" + hostnameCommand,
				"KAFKA_CREATE_TOPICS" + "=" + kafkaCreateTopics,
				"KAFKA_ZOOKEEPER_CONNECT" + "=" + kafkaZookeeperConnect+"/brokers"
		};


		// create kafka containers
		kafkaContainerIDs = createKafkaContainers(KAFKA_IMAGE_NAME, kafkaClusterSize, kafkaEnvVariables);

		waitForKafkaClusterSetup(kafkaClusterSize);
		logger.debug("  kafka cluster is ready with cluster size : " + kafkaContainerIDs.length);


		// check health status of kafka cluster
		try {
		//	checkKafkaStatus();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}


	//----------------------------------------------------------------------------------------------------------------------------------------------


	private String[] createKafkaContainers(String kafkaImageName, int kafkaClusterSize, String[] envVariables) {

		logger.debug(" ---------------- Create Kafka containers ----------------");

		String[] kafkaContainerIds = new String[kafkaClusterSize];
		String errorMsg;

		// zookeeper kafka inside port(s):
	//	iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + KAFKA_INSIDE_PORTS));
	//	String kafka_inside_ports = iterator.next().asLiteral().getString();
		String kafka_inside_ports = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + KAFKA_INSIDE_PORTS, "9092,9096,9097");
		String[] kafkaInsidePorts = kafka_inside_ports.split(",");

		// zookeeper kafka outside port(s):
		//iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + KAFKA_OUTSIDE_PORTS));
		//String kafka_outside_ports = iterator.next().asLiteral().getString();
		String kafka_outside_ports = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + KAFKA_OUTSIDE_PORTS, "9094,9098,9099");
		String[] kafkaOutsidePorts = kafka_outside_ports.split(",");


		envVariables = (String[]) ArrayUtils.add(envVariables, "HOBBIT_RABBIT_HOST=" + this.rabbitMQHostName);

		for (int b = 0; b < kafkaClusterSize; b++) { // TODO or i++ or ++i

			//envVariables = (String[])ArrayUtils.add(envVariables, "ID=" + i);
			envVariables = (String[]) ArrayUtils.add(envVariables, "KAFKA_ADVERTISED_LISTENERS=" + "INSIDE://:" + kafkaInsidePorts[b] + ",OUTSIDE://_{HOSTNAME_COMMAND}" + ":" + kafkaOutsidePorts[b]);
			envVariables = (String[]) ArrayUtils.add(envVariables, "KAFKA_LISTENERS=" + "INSIDE://:" + kafkaInsidePorts[b] + ",OUTSIDE://:" + kafkaOutsidePorts[b]);
			envVariables = (String[]) ArrayUtils.add(envVariables, "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=" + "INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT");
			envVariables = (String[]) ArrayUtils.add(envVariables, "KAFKA_INTER_BROKER_LISTENER_NAME=" + "INSIDE");
			envVariables = (String[]) ArrayUtils.add(envVariables, "KAFKA_ZOOKEEPER_TIMEOUT_MS=" + "36000");
			int id = b + 1;
			envVariables = (String[]) ArrayUtils.add(envVariables, "KAFKA_BROKER_ID=" + id);
			//		envVariables = (String[])ArrayUtils.add(envVariables, "KAFKA_PORT=" + kafkaInsidePorts[b]); // kafkaOutsidePorts[b]


			String containerId = this.createContainer(kafkaImageName, envVariables);

			if (containerId == null) {
				errorMsg = "Couldn\'t create kafka component. Aborting.";
				logger.error(errorMsg);
				throw new IllegalStateException(errorMsg);
			} else {
				logger.info("--# kafka container created with cont id: " + containerId);
				kafkaContainerIdsSet.add(containerId);   // unordered set
				kafkaContainerIds[b] = containerId; // ordered array
			}
		}


		return kafkaContainerIds;

	}
	//----------------------------------------------------------------------------------------------------------------------------------------------

	public void waitForKafkaClusterSetup(int kafkaClusterSize) {
		logger.debug(" Waiting for {} kafka containers to be started.", Integer.valueOf(this.kafkaClusterSize));

		String errorMsg;
		try {
			this.kafkaReadyMutex.acquire(this.kafkaClusterSize);

			logger.info(" kafkaReadyMutex acquired!!");


		} catch (InterruptedException var2) {
			errorMsg = "Interrupted while waiting for kafka containers to be ready.";
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg, var2);
		}

	}


	//----------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------

	public void setupHbaseCluster() {

	}


	//----------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------
//--


	public void setupStormCluster() throws IOException {

		logger.info("=====================================  Setting up Storm cluster... =====================================  ");


		// loading storm params----------------------------
		// cluster size:
		//iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + NUM_OF_STORM_SUPERVISORS));
		//String num_of_supervisors = iterator.next().asLiteral().getString();
		//numOfStormSupervisors = Integer.parseInt(num_of_supervisors);
		int numOfStormSupervisors = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + NUM_OF_STORM_SUPERVISORS, 1);


		// create env var kafka zookeeper connect
	//	iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + ZOOKEEPER_CLUSTER_SIZE));
//		String zookeeper_cluster_size = iterator.next().asLiteral().getString();
//		int zookeeperClusterSize = Integer.parseInt(zookeeper_cluster_size);

		int zookeeperClusterSize= getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + ZOOKEEPER_CLUSTER_SIZE, 3);
	//	iterator = systemParamModel.listObjectsOfProperty(systemParamModel .getProperty(SYSTEM_PARAM_URI_PREFIX + ZOO_CLIENT_PORTS));
	//	String zoo_client_ports = iterator.next().asLiteral().getString();
		String zoo_client_ports =  getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + ZOO_CLIENT_PORTS, "31102,31202,31302");
		String[] zooClientPorts = zoo_client_ports.split(",");

		String[] zookeeperConnectArr = new String[zookeeperClusterSize];
		for (int a = 0; a < zookeeperClusterSize; a++) {
			zookeeperConnectArr[a] = zookeeperContainerIDs[a];
		}
		String zookeeperConnect = String.join(",", zookeeperConnectArr);

		// storm environmental values
		stormEnvVariables = new String[]{
				"ZOOKEEPER_PORT"       + "=" + zooClientPorts[1],
				 //"ZOOKEEPER_CONNECT" + "=" + zookeeperConnect	// hack for java-sdk.
				"ZOOKEEPER_CONNECT"    + "=" + zookeeperConnectArr[1],  // hack for java-sdk. change to zookeeperConnect when running on hobbit platform
				"GRAPHITE_HOST"        + "=" +  graphiteContainerID,   // graphite container id to set in storm.yaml for metrics monitoring with carbon-graphite
				"GRAPHITE_PORT"		   + "=" +  "2003"
		};


		// create kafka nimbus containers
		stormNimbusContainerIDs = new String[1];  // TODO make configurable
		stormNimbusContainerID = createStormNimbusContainer(STORM_NIMBUS_IMAGE_NAME, stormEnvVariables);
		stormNimbusContainerIDs[0] = stormNimbusContainerID;

		waitForStormNimbus();
		this.sendToCmdQueue(START_STORM_NIMBUS_SIGNAL);


	// create supervisor containers
		stormEnvVariables = (String[]) ArrayUtils.add(stormEnvVariables, "STORM_NIMBUS_SEEDS=" + String.join(",", stormNimbusContainerIDs)); // commenting for testing
		stormSupervisorContainerIDs = createStormSupervisorsContainer(STORM_SUPERVISOR_IMAGE_NAME, stormEnvVariables, numOfStormSupervisors);

		waitForStormSupervisors(numOfStormSupervisors);
		this.sendToCmdQueue(START_STORM_SUPERVISOR_SIGNAL);


	// create storm ui container
	//	String stormUiContainerID = createContainer(STORM_UI_IMAGE_NAME, stormEnvVariables);

		logger.debug("---------------- storm cluster is ready with and {} supervisors", numOfStormSupervisors);



	}

//----------------------------------------------------------------------------------------------------------------------------------------------

	public String createStormNimbusContainer(String stormNimbusImageName, String[] stormEnvVariables) {

		logger.info("---------------- Create storm nimbus container... ---------------- ");


		String containerId = this.createContainer(stormNimbusImageName, stormEnvVariables);
		String errorMsg;

		if (containerId == null) {
			errorMsg = "Couldn\'t create storm nimbus component. Aborting.";
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg);
		} else {
			logger.info("-  storm nimbus container created with cont id: " + containerId);
		}
		return containerId;
	}

//----------------------------------------------------------------------------------------------------------------------------------------------

	public String[] createStormSupervisorsContainer(String stormSupervisorImageName, String[] stormEnvVariables, int numOfStormSupervisors) {

		logger.info("----------------  Create storm supervisor container ----------------");

		String[] supervisorContainerIds = new String[numOfStormSupervisors];
		String errorMsg;

		logger.debug("Creating {} supervisors with image name {} ", numOfStormSupervisors, stormSupervisorImageName);

		for (int k = 0; k < numOfStormSupervisors; k++) {
			String containerId = this.createContainer(stormSupervisorImageName, stormEnvVariables);

			if (containerId == null) {
				errorMsg = "Couldn\'t create storm supervisor component. Aborting.";
				logger.error(errorMsg);
				throw new IllegalStateException(errorMsg);
			} else {
				logger.info("- storm supervisor container created with cont id: " + containerId);
				zooContainerIdsSet.add(containerId);   // unordered set
				supervisorContainerIds[k] = containerId; // ordered array
			}
		}

		return supervisorContainerIds;
	}


//----------------------------------------------------------------------------------------------------------------------------------------------

	public void waitForStormNimbus() {
		logger.debug("  Waiting for storm nimbus to be started. ");

		String errorMsg;
		try {
			this.stormNimbusReadyMutex.acquire();

			logger.info(" stormSupervisorReadyMutex acquired!!");


		} catch (InterruptedException var2) {
			errorMsg = "Interrupted while waiting for storm containers to be ready.";
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg, var2);
		}

	}


	//----------------------------------------------------------------------------------------------------------------------------------------------

	public void waitForStormSupervisors(int numOfStormSupervisors) {

		logger.debug("----------------  Waiting for {} supervisors containers to be started.", numOfStormSupervisors);

		String errorMsg;
		try {
			this.stormSupervisorReadyMutex.acquire(numOfStormSupervisors);
			logger.info(" stormSupervisorReadyMutex acquired!!");
		} catch (InterruptedException var2) {
			errorMsg = "Interrupted while waiting for storm containers to be ready.";
			logger.error(errorMsg);
			throw new IllegalStateException(errorMsg, var2);
		}

	}


//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------

	private void forwardDataToKafka() throws Exception {
		// TODO case switch

		logger.info("=======================  ======= ===============   Forwarding data to Pipeline ================ ======= ===============  ");

		String fileName = pathToDatasetFolder + "/sensors.dat";
		String topicName = "Sensors";

		KafkaEventProducer producer = new KafkaEventProducer();

		// create kafka broker URIs string
	//	iterator = systemParamModel.listObjectsOfProperty(systemParamModel.getProperty(SYSTEM_PARAM_URI_PREFIX + KAFKA_INSIDE_PORTS));
	// 	String kafka_inside_ports = iterator.next().asLiteral().getString();
		String kafka_inside_ports = getPropertyOrDefault(SYSTEM_PARAM_URI_PREFIX + KAFKA_INSIDE_PORTS, "9092,9096,9097");


		String[] kafkaInsidePorts = kafka_inside_ports.split(",");
		String[] kafkaBrokerURIsArr = new String[kafkaClusterSize];
		for (int z = 0; z < kafkaClusterSize; z++) {
			kafkaBrokerURIsArr[z] = kafkaContainerIDs[z] + ":" + kafkaInsidePorts[z];
		}

		// Init kafka config
		producer.initKafkaConfig(kafkaBrokerURIsArr);
		// Init file config - Pass the file name as first parameter
		producer.initFileConfig(fileName);
		// Send file data to Kafka broker - Pass the topic name as second
		// parameter


		try {
			sendToCmdQueue(FORWARDING_DATA_TO_PIPELINE_START);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Starting event production");
		producer.sendFileDataToKafka(topicName);
		System.out.println("All events sent");
		try {
			sendToCmdQueue(FORWARDING_DATA_TO_PIPELINE_FINISHED);
		} catch (IOException e) {
			e.printStackTrace();
		}

		producer.cleanup();

	}
//----------------------------------------------------------------------------------------------------------------------------------------------

	// can be overriden as per usecase
	private void csvFileToBean() throws IOException {

		String fileNameWithRelPath = pathToDatasetFolder + "/House1.csv";
		CsvToBean csvToBean = new CsvToBean();
		CSVReader csvReader = null;
		try {
			csvReader = new CSVReader(new FileReader(fileNameWithRelPath), ',', '"', 1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//Set column mapping strategy
		List list = csvToBean.parse(setColumMapping(), csvReader);

		for (Object object : list) {
			SensorDataBean sensorDataBean = (SensorDataBean) object;
			System.out.println(sensorDataBean);
		}
		csvReader.close();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static ColumnPositionMappingStrategy setColumMapping() {
		ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
		strategy.setType(SensorDataBean.class);
		String[] columns = new String[]{"timeStamp", "aggregate", "fridge", "freezer", "washerDryer", "washingMachine", "age", "toaster", "age", "computer", "televisionSite", "microwave", "kettle"};
		strategy.setColumnMapping(columns);
		return strategy;
	}


//----------------------------------------------------------------------------------------------------------------------------------------------

	/*
*	getting and validating input parameters
 * and executing kafka producer to send messages
 */
	private void forwardToPipeline2() {

		String TOPIC_NAME = "";
		final String SPLITTER = ",";
		String FILE_PATH = ",";
		String SERVER = "";


		final File csvFile = new File(pathToDatasetFolder + "/House1.csv");

		if (!csvFile.exists()) {
			try {
				throw new FileNotFoundException("File not found");
			} catch (FileNotFoundException e) {
				e.printStackTrace();

			}
		}

		ImmutableList<SensorData> sensorDataList = null;
		try {
			sensorDataList = ImmutableList
					.copyOf(
							new CsvReader(SPLITTER)
									.loadCsvContentToList(new BufferedReader(new FileReader(csvFile)))
					);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (sensorDataList.size() == 0) {
			System.out.println("No Data Found in File");
			return;
		}

		final Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");

		final KafkaProducer<String, String> producer = new KafkaProducer<>(props);

		final KafkaSensorDataProducer KafkaSensorDataProducer =
				new KafkaSensorDataProducer(
						producer,
						TOPIC_NAME);
		sensorDataList.forEach(policy -> {
			try {
				KafkaSensorDataProducer.send(mapper.writeValueAsString(policy));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} finally {
				KafkaSensorDataProducer.close();
			}
		});


	}

//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------

	private void printClusterConfigForDoc() {
		// create list for container ids

		String kafkaSize = iterator.next().asLiteral().getString();

		String zkSize = iterator.next().asLiteral().getString();

		int numOfNumbisContainers = 1;

		String supSize = iterator.next().asLiteral().getString();

		logger.info("###########################################################################");
		logger.info("					zookeeper cluster size  =   {}							", zkSize);
		logger.info("					kafka     cluster size  =   {}							", kafkaSize);
		logger.info("					storm     cluster size  =   {}							", supSize);
		logger.info("					graphite  container     =   {}							", 1);
		//logger.info("					Hbase     cluster size  =   {}							",  "N/A" );
		logger.info("###########################################################################");
	}


	public String getZookeeperConnectStr() {
		return zookeeperConnectStr;
	}



//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------------


	/**
	 * A generic method for loading parameters from the benchmark parameter model
	 *
	 * @param property     the property that we want to load
	 * @param defaultValue the default value that will be used in case of an error while loading the property
	 * @return the value of requested parameter
	 */
	@SuppressWarnings("unchecked")
	private <T> T getPropertyOrDefault(String property, T defaultValue) {
		T propertyValue = null;
		NodeIterator iterator = systemParamModel
				.listObjectsOfProperty(systemParamModel
						.getProperty(property));

		if (iterator.hasNext()) {
			try {
				 if (defaultValue instanceof Integer) {
					return (T) ((Integer) iterator.next().asLiteral().getInt());
				} else if (defaultValue instanceof Long) {
					return (T) ((Long) iterator.next().asLiteral().getLong());
				} else if (defaultValue instanceof Float) {
					return (T) ((Float) iterator.next().asLiteral().getFloat());
				} else if (defaultValue instanceof Double) {
					return (T) ((Double) iterator.next().asLiteral().getDouble());
				}else if (defaultValue instanceof String) {
					return (T) iterator.next().asLiteral().getString();
				}
			} catch (Exception e) {
				logger.error("Exception while parsing parameter.");
			}
		} else {
			logger.info("Couldn't get property '" + property + "' from the parameter model. Using '" + defaultValue + "' as a default value.");
			propertyValue = defaultValue;
		}
		return propertyValue;
	}
}