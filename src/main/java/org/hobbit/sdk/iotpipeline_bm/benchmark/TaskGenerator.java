package org.hobbit.sdk.iotpipeline_bm.benchmark;


import org.apache.commons.io.IOUtils;
import org.hobbit.core.components.AbstractTaskGenerator;
//import org.hobbit.core.components.AbstractSequencingTaskGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static org.hobbit.sdk.iotpipeline_bm.Commands.ZOOKEEPER_CONNECT_SIGNAL;
import static org.hobbit.sdk.iotpipeline_bm.Constants.*;

public class TaskGenerator extends AbstractTaskGenerator {  //extends  {AbstractSequencingTaskGenerator

	private static final Logger logger = LoggerFactory.getLogger(TaskGenerator.class);

	int scaleFactor;
	int numOfTasks;
	int numOfOps;
	int seed;

	int frequencies[];

	String[] taskNames;
	String[][] params;
	String paramDir;
	String[] answers;
	Random[] rndms;

	private boolean finished = false;

	String zookeeperConnectStr = "";

	Long executionTimeThreshold = null;
	Long executionTime = null;


	// ------------------------------------------------------------------------------------------------------------------

	public TaskGenerator() {

		super();
	}


	// ------------------------------------------------------------------------------------------------------------------


	@Override
	public void init() throws Exception {
		// Always init the super class first!
		super.init();
		logger.info("Initialzing task generators.........");
		// Your initialization code comes here...

		internalInit();
		//target_receiver = SimpleFileReceiver.create(this.incomingDataQueueFactory,"file");

		logger.info("Initialization is over.");

	}

	@Override
	public void receiveCommand(byte command, byte[] data) {

		if(command == ZOOKEEPER_CONNECT_SIGNAL){

			this.zookeeperConnectStr = 	new String(data);  //, StandardCharsets.);
			logger.info ("-----------  zookeeper Connect Str {}", zookeeperConnectStr);
		}

		super.receiveCommand(command, data);
	}

	// ------------------------------------------------------------------------------------------------------------------
	// ------------------------------------------------------------------------------------------------------------------
	@Override
	protected void generateTask(byte[] data) throws Exception {

		logger.debug("  Workload creation starts");

		// Create tasks based on the incoming data inside this method.
		// You might want to use the id of this task generator and the
		// number of all task generators running in parallel.
		//logger.debug("generateTask()");

			int taskGeneratorId = getGeneratorId();
		int numberOfGenerators = getNumberOfGenerators();

		if (finished)
			return;

		// Create an ID for the task
		String taskId = getNextTaskId();


	//	if (Long.valueOf(taskId) >= numOfOps) { // currently one task only, with 1 op
		if (Long.valueOf(taskId) >= numOfTasks) {  // one task with 50 ops mean the task will be repeated 50 times. task can be a no-sql query, method, storm topology, algorithm  etc.
			finished = true;
			return;
		}

		String tasksDir = "tasks";

		//  for sequential benchmarking tasks
		/*
		String tasksListFile = tasksDir + "tasks-list.txt";
		InputStream is = new FileInputStream(tasksListFile);
		String[] files = IOUtils.toString(is).split("\n");
		is.close();
		String name;
		for (int i = 1; i <= files.length; i++) {

			name = "task" + String.valueOf(i);
			prepareTask(i, name);

			String dataString = RabbitMQUtils.readString(data);  //
			logger.debug(" dataString : {} ", dataString);


			// Create the task and the expected answer
			String taskDataStr = "task_" + taskId + "_" + dataString;
			String expectedAnswerDataStr = "result_" + taskId;


			// Send the task to the system (and s tore the timestamp)
			long timestamp = System.currentTimeMillis();
			logger.debug("sendTaskToSystemAdapter({})->{}", taskId, taskDataStr);
			sendTaskToSystemAdapter(taskId, taskDataStr.getBytes());

			// Send the expected answer to the evaluation store
			logger.debug("sendTaskToEvalStorage({})->{}", taskId, expectedAnswerDataStr);
			//	sendTaskToEvalStorage(taskId, timestamp, expectedAnswerDataStr.getBytes());
			sendTaskToEvalStorage(taskId, timestamp, null);
		}
		*/

		// Handle data sent by data gen
		String dataString = RabbitMQUtils.readString(data); // metadata file and task name and operation num

		String extractedTaskName = "spike_detection"; // TODO extract task name from dataString
		logger.debug(" generate {} task -> with taskIdString {} ", extractedTaskName, taskId) ;

		String[] taskFilesNamesWithPath = prepareTasks (extractedTaskName);

		logger.debug(" task   generated:: {} ", extractedTaskName);

		byte[][] taskFilesData = new byte[taskFilesNamesWithPath.length][];    // .properties file and task jar file for storm topology
		int configFileSize = 0;

		InputStream inputStream = null;
		for (int fp=0; fp<taskFilesNamesWithPath.length; fp++) {
			Path filePath = Paths.get(taskFilesNamesWithPath[fp]);
			inputStream = Files.newInputStream(filePath);
			int len = (int) Files.size(filePath);
			if(fp==0){
				configFileSize = len;
			}
		//	logger.info("file size  --------- " + len);
			taskFilesData[fp] = new byte[len];
			inputStream.read( taskFilesData[fp]);

				 /*
				 File file = new File(taskFilesNamesWithPath[fp]);
				byte bytes[] = FileUtils.readFileToByteArray(file);
				 */

			inputStream.close();
		}

		//inputStream.close();

		long timestamp = System.currentTimeMillis();


		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(configFileSize);
		byte[] configFileSizeInBytes = bb.array();

	/*
		byte[] fileNameInBytes =  RabbitMQUtils.writeString(extractedTaskName);
		int fileNameSizeInBytes = fileNameInBytes.length;
		int byteSize = 4 + fileNameSizeInBytes + 4 ; // for  task-name-string size + task name + config size

		ByteBuffer bb = ByteBuffer.allocate(byteSize);
		bb.putInt(fileNameSizeInBytes);
		bb.put(fileNameInBytes);
		bb.putInt(configFileSize);
		byte[] configFileSizeWithTaskNameInBytes = bb.array();

		logger.debug(" Sendig task {} to  System adapter... ", extractedTaskName);

			sendTaskToSystemAdapter(taskId, RabbitMQUtils.writeByteArrays(configFileSizeWithTaskNameInBytes , taskFilesData, null) );


*/

		logger.info("config file size  --------- " + configFileSize);

		sendTaskToSystemAdapter(taskId, RabbitMQUtils.writeByteArrays(configFileSizeInBytes , taskFilesData, null) );

		//sendTaskToEvalStorage(taskId, timestamp, RabbitMQUtils.writeString(""));
		sendTaskToEvalStorage(taskId, timestamp, RabbitMQUtils.writeLong(30000)); // threshold for 30 sec




	}


// ------------------------------------------------------------------------------------------------------------------

	private String[] prepareTasks(String taskName) throws Exception {
		logger.debug("preparing task  :: {} ", taskName);

		String[] taskFilePaths = new String[2];
		Properties props = new Properties();
		switch (taskName) {
			case "spike_detection":

				String configFilePath = "tasks/storm_tasks/" + taskName + "/config/" + "spike-detection.properties";
				//	TODO check from System.env if ->  message-system=kafka, data-type=csv, processing-system=storm, storage=Hbase
				//	and set the props accordingly

				// set zookeeper connect string
			/*	PropertiesConfiguration conf = new PropertiesConfiguration(configFilePath);
				props.setProperty("sd.kafka.zookeeper.host", zookeeperConnectStr );
				conf.save();*/


					/*
					properties.setProperty("sd.kafka.zookeeper.host",zookeeperConnectStr);
					FileOutputStream fos = new FileOutputStream(propertiesFilePath);
					properties.store(fos,null);

					*/
				logger.debug(new String(Files.readAllBytes(Paths.get("tasks/storm_tasks/" + taskName + "/config/" + "spike-detection.properties"))) );

				/*
					String propertyName = "sd.kafka.zookeeper.host";
						//first load old one:
						FileInputStream configStream = new FileInputStream(configFilePath);
						props.load(configStream);
						configStream.close();
						logger.debug("---------------------- ------------------ --------> " + zookeeperConnectStr);
						//modifies existing or adds new property
						props.setProperty("sd.kafka.zookeeper.host", zookeeperConnectStr);
						props.setProperty("sd.sink.threads", String.valueOf(1));
						//save modified property file
						FileOutputStream output = new FileOutputStream(configFilePath);
						props.store(output, null);
						output.close();
				logger.debug(new String(Files.readAllBytes(Paths.get("tasks/storm_tasks/" + taskName + "/config/" + "spike-detection.properties"))));
*/

				taskFilePaths[0] = "tasks/storm_tasks/" + taskName + "/config/" + "spike-detection.properties";
				//taskFilePaths[1] = "tasks/storm_tasks/" + taskName + "/" + taskName + ".jar";
				taskFilePaths[1] = "tasks/storm_tasks/" + taskName + "/" +  "spike_detection.jar";

				break;


			case "parse_CsvToSenML":


				//load relevant workload properties
				try (BufferedReader br = new BufferedReader(
						new FileReader(getClass().getClassLoader().getResource(
								"tasks/storm_tasks/" + taskName + "/config/" + "taskName" + ".properties")
								.getFile()))) {
					String line;
					while ((line = br.readLine()) != null) {
						String[] parts = line.split("=");
						props.put(parts[0], parts[1]);
					}

					// TODO update props acc to values in benchmark.ttl
					br.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//	MicroTopologyDriver microTopologyDriver = new MicroTopologyDriver();

				// PLUG-<expNum>  <rate as 1x,2x>

				String deploymentMode = "C";  //Local ('L') or Distributed-cluster ('C') Mode; arg[0]
				String topoName = "ETLTopology"; // topology-fully-qualified-name
				String inputDatasetPathName = "SYS-inputcsvSCTable-1spouts100mps-480sec.csv"; // Full path along with File Name ; used by CustomEventGen and Spout
				String experiRunId = "PLUG-1";// "E01-01"; // PLUG-<expNum>
				double scalingFactor = 0.001;  //Deceleration factor with respect to seconds.
				String outputDirName = "/streammark/outputLogDir/Storm";  //Path where the output log file from spout and sink has to be kept
				String tasksPropertiesFilename = "task.properties"; //tasks_TAXI.properties for DecisionTreeTrainBatch & LinearRegressionTrainBatched, tasks.properties for BlockWindowAverage)
				String tasksName = "CsvToSenML";

				break;
		}

		return taskFilePaths;
	}


	// ------------------------------------------------------------------------------------------------------------------

	@Override
	public void close() throws IOException {
		// Free the resources you requested here
		logger.debug("close()");
		// Always close the super class after yours!
		super.close();
	}


	// ------------------------------------------------------------------------------------------------------------------
	private void internalInit() {

		Map<String, String> env = System.getenv();

		scaleFactor = Integer.parseInt(env.get(DATA_SIZE_SCALE_FACTOR));
		//numOfTasks = Integer.parseInt(env.get(NUM_OF_TASKS));

		seed = 1;
	//	seed = Integer.parseInt(env.get(SEED)); // TODO FIX
		taskNames = env.get(TASKS).split(",");   // one task only for testing
		numOfTasks = taskNames.length;
		numOfOps = Integer.parseInt(env.get(NUM_OF_OPS));  // num of task repitition
	//	executionTimeThreshold= Long.valueOf(env.get(EXECUTION_TIME_THRESHOLD));


		/*
		rndms = new Random[22];
		for (int i = 1; i <= 21; i++) {
			rndms[i] = new Random(seed + i);
		}
		*/


/*			// read local params
		params = new String[numOfTasks][];

		String paramFileContent;
		for (int i = 0; i < numOfTasks; i++) {
			paramDir = "tasks/storm_tasks" + taskNames[i] + File.separator + "parameters"; // fixed  // TODO remove shortcut, should be sent by data-gen
			// for multiple param files
			//	String paramFileNames = paramDir + File.separator + "param_files.txt";
			//String paramFile = paramDir + File.separator + String.valueOf(i+1) + "param.txt";

			String paramFile = paramDir + File.separator + "param.txt";
			logger.debug("param file :: " + paramFile);
			try {
				FileHelper fileHelper = new FileHelper();
				paramFileContent = fileHelper.getResourceFileAsString(paramFile);
				logger.debug("task params :: ------------------------------ " + paramFileContent);
				if(paramFileContent != null) {
					params[i] = paramFileContent.replaceFirst(".*\n", "").split("\n");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
*/

	}


	// ------------------------------------------------------------------------------------------------------------------
	private HashMap<String, String> readMappings(String path) {
		HashMap<String, String> map = null;
		try (BufferedReader br = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(path).getFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(" - ");
				map.put(parts[0], parts[1]);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}

	// ------------------------------------------------------------------------------------------------------------------
	private void readProperties(String propFilePath) {

		frequencies = new int[numOfTasks + 1];

		String relPathFrequenciesFile = propFilePath;
		try (BufferedReader br = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(relPathFrequenciesFile).getFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("=");
				frequencies[Integer.valueOf(parts[0].replaceAll("[^0-9]", ""))] = Integer.valueOf(parts[1]); // task1=50
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------------------------------------------------------------------


	private HashMap<Long, String> readRemoteParams(String fileUrl) {

		String[][] params = new String[numOfTasks][];
		for (int i = 1; i <= numOfTasks; i++) {
			String substitutionParametersDir = fileUrl + "/sf" + scaleFactor + "/parameters";
			String remmoteParamFilePath = substitutionParametersDir + "/task_" + String.valueOf(i) + "_param.txt";
			try {
				InputStream inputStream = new URL(remmoteParamFilePath).openStream();
				String fileContent = IOUtils.toString(inputStream);
				params[i] = fileContent.replaceFirst(".*\n", "").split("\n");
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// ------------------------------------------------------------------------------------------------------------------
	private void readFrequencies() {

		frequencies = new int[numOfTasks + 1];

		String relPathFrequenciesFile = "filename";
		try (BufferedReader br = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(relPathFrequenciesFile).getFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split("=");
				frequencies[Integer.valueOf(parts[0].replaceAll("[^0-9]", ""))] = Integer.valueOf(parts[1]); // TODO recheck
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------------------------------------------------------------------


}