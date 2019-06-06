package org.hobbit.sdk.iotpipeline_bm.benchmark;

import org.apache.commons.io.IOUtils;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.iotpipeline_bm.utils.configuration.Configurations;
import org.hobbit.utils.EnvVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static org.hobbit.sdk.iotpipeline_bm.Commands.DATA_GEN_FINISHED;
import static org.hobbit.sdk.iotpipeline_bm.Commands.DATA_GEN_FINISHED_FROM_DATAGEN;
import static org.hobbit.sdk.iotpipeline_bm.Constants.*;

public class DataGenerator extends AbstractDataGenerator {

	private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);

	private Semaphore generateTasks = new Semaphore(0);

	private String inputFormat;
	private int numOfTasks;
	private int numOfOps;
	private String[] taskNames;

	private String datasetDir;
	private String externalDataGen;
	private int var = 2; // data locality case

	private int dataSizeScaleFactor;
	private int numOfSensors;
	private int eventIntervalInSec;

	private String[] datasetsNameArr;
	private String datasetDirPath;
	private String datasetNamesFilePath;

	private static Configurations configurations;

	public DataGenerator() {

	}

	@Override
	public void init() throws Exception {
		// Always init the super class first!
		super.init();
		logger.info("Initializing Data Generator............ ");
		// Your initialization code comes here...

		String sessionId = EnvVariables.getString(Constants.HOBBIT_SESSION_ID_KEY, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS);

		internalInit();

	}

	private void internalInit() {

		configurations = new Configurations();

		Map<String, String> env = System.getenv();


		logger.info(" Initialzing Data Generartors... ");


		if (!env.containsKey(DATA_INPUT_FORMAT)) {
			logger.info ("Data input format missing... ");
			//System.exit(1);
			inputFormat = "dat";
		} else {
			inputFormat = env.get(DATA_INPUT_FORMAT);
			logger.info("Data input format ... {}", inputFormat);
		}


		if (!env.containsKey(PREGEN_DATASETS)) {
			logger.info("Dataset names are missing. Checking for external data gen.");
			if (!env.containsKey(EXTERNAL_DATA_GEN)) {
				logger.error("No local/remote datasets and No external data generator found ... Aborting.");
				System.exit(1);
			} else {
				externalDataGen = env.get(EXTERNAL_DATA_GEN);
				var = 1;
			}
		} else {
			datasetsNameArr = env.get(PREGEN_DATASETS).split(",");
			logger.info("Dataset names {}  ", datasetsNameArr[0] );
		}


		if (!env.containsKey(DATA_SIZE_SCALE_FACTOR)) {
			logger.error("Data size scale factor missing... Aborting.");
			System.exit(1);
		} else {
			dataSizeScaleFactor = Integer.parseInt(env.get(DATA_SIZE_SCALE_FACTOR));
		}


		if (!env.containsKey(NUM_OF_OPS)) {
			logger.info("Number of ops per task missing... Aborting.");
			System.exit(1);
		} else {
			numOfOps = Integer.parseInt(env.get(NUM_OF_OPS));
			logger.info("Number of operations per task  {} ", numOfOps);
		}


		if (!env.containsKey(TASKS)) {
			logger.info(" Task name missing... Aborting.");
			System.exit(1);
		} else {
			taskNames = env.get(TASKS).split(",");
			numOfTasks = taskNames.length;  // initial support for one task

			logger.info("Task Name {} ", numOfOps);
		}



	}
// ------------------------------------------------------------------------------------------------------------------


	@Override
	protected void generateData() throws Exception {
		// Create your data inside this method. You might want to use the
		// id of this data generator and the number of all data generators
		// running in parallel.

		int dataGeneratorId = getGeneratorId();
		int numberOfGenerators = getNumberOfGenerators();

		logger.info("Data Generator is up and running. Generating Data now.......");

		switch (var) {
			case 1:
				runExternalDataGenerator(externalDataGen);
				break;

			case 2:
					sendDataFromLocalContext();


				break;

			case 3:
				downloadFileAndSendData();
				break;
		}

		generateTasks.acquire();


		// the data can be sent to the task generator(s) ...
		//logger.debug("sendDataToTaskGenerator()->{}", data);
		//sendDataToTaskGenerator(data.getBytes());
		// an to system adapter
		//logger.debug("sendDataToSystemAdapter()->{}",data);
		//sendDataToSystemAdapter(data.getBytes());

	}

	private void runExternalDataGenerator(String externalDataGen) {

		String name = externalDataGen;
		// TODO
	//	switch (name){}
	}


	private void sendDataToTaskGenRequiredForTaskGeneration() {

		logger.info(" sending data necessary for task generations to  task gen....");

		//	send metadata file
		String metadataDir = "data" + File.separator + "sf" + dataSizeScaleFactor + File.separator + "metadata" + File.separator;
		String metadataFile = metadataDir + "metadata.txt";

		try {
			Path filePath = Paths.get(metadataFile);
			InputStream inputStream = Files.newInputStream(filePath);
			int len = (int) Files.size(filePath);
			//	logger.info("file size  --------- " + len);
			byte[] data = new byte[len];
			inputStream.read(data);

			//byte[] data = IOUtils.toByteArray(inputStream);

			String fileNameWithoutPath = "dataHeader.txt";
			logger.info("File to be sent {} : {} of size {} ", fileNameWithoutPath, metadataFile, len);
			byte[] dataForSending = RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(fileNameWithoutPath)}, data);


			/*for (int i = 0; i < numOfTasks; i++) {   	//  creating multiple instances of the same task for testing purposes, initially one task
				for (int j = 0; i < numOfOps; i++) {
					sendDataToTaskGenerator(dataForSending);
				}
			}*/

			sendDataToTaskGenerator(dataForSending);

			inputStream.close();

			logger.info("File " + fileNameWithoutPath + " has been successfully sent to task gen.");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// TODO can send multiple files depending on task to be generated via byte[][]
	}


	private void sendDataFromLocalContext() {
		logger.debug("sendDataFromLocalContext() :: sending files with data from local context to  system adapter...");

		datasetDirPath = "data" + File.separator + "sf" + dataSizeScaleFactor + File.separator + "dataset" + File.separator;
		datasetNamesFilePath = datasetDirPath + "dataset_files.txt";

		try {
			InputStream is = new FileInputStream(datasetNamesFilePath);
			String[] files = IOUtils.toString(is).split("\n");
			is.close();

			for (int i = 0; i < files.length; i++) {

				String fileNameWithPath = files[i];
				fileNameWithPath = datasetDirPath + fileNameWithPath;
				//logger.info("File name with path .... " + fileNameWithPath);

				Path filePath = Paths.get(fileNameWithPath);
				InputStream inputStream = Files.newInputStream(filePath);
				int len = (int) Files.size(filePath);
				//	logger.info("file size  --------- " + len);
				byte[] data = new byte[len];
				inputStream.read(data);

				//byte[] data = IOUtils.toByteArray(inputStream);

				String fileNameWithoutPath = fileNameWithPath.replaceFirst(".*/", "");
				logger.info("Data File to be sent {} : {} of size {} ", fileNameWithoutPath, fileNameWithPath, len);
				byte[] dataForSending = RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(fileNameWithoutPath)}, data);

				sendDataToSystemAdapter(dataForSending);

				inputStream.close();

				logger.info("File " + fileNameWithoutPath + " has been downloaded successfully and sent.");
			}

			ByteBuffer buffer = ByteBuffer.allocate(5);
			buffer.putInt(files.length);
			buffer.put((byte) 1);
			sendToCmdQueue(DATA_GEN_FINISHED_FROM_DATAGEN, buffer.array());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/*
		Download data files from remote site, via https url, and send to system adapter
	 */
	private void downloadFileAndSendData() {

		String directory = "URI"+ "data" + File.separator + "sf" + dataSizeScaleFactor + File.separator + "dataset" + File.separator ;
		String datasetNamesFilePath = directory + "dataset_files.txt";

		try {
			InputStream is = new URL(datasetNamesFilePath).openStream();
			String[] files = IOUtils.toString(is).split("\n");
			is.close();

			for (int i = 0; i < files.length; i++) {
				String remoteFile = files[i];
				remoteFile = directory + remoteFile;
				logger.info("Downloading file " + remoteFile);
				InputStream inputStream = new URL(remoteFile).openStream();

				String graphUri = remoteFile.replaceFirst(".*/", "");
				byte[] data = IOUtils.toByteArray(inputStream);
				;
				byte[] dataForSending = RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(graphUri)}, data);
				sendDataToSystemAdapter(dataForSending);
				inputStream.close();

				logger.info("File " + remoteFile + " has been downloaded successfully and sent.");
			}

			ByteBuffer buffer = ByteBuffer.allocate(5);
			buffer.putInt(files.length);
			buffer.put((byte) 1);
			//	sendToCmdQueue(BULK_LOAD_DATA_GEN_FINISHED, buffer.array());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
// ------------------------------------------------------------------------------------------------------------------


	/**
	 * A generic method for initialize benchmark parameters from environment
	 * variables
	 *
	 * @param env       a map of all available environment variables
	 * @param parameter the property that we want to get
	 * @param paramType a dummy parameter to recognize property's type
	 */
	@SuppressWarnings("unchecked")
	private <T> T getFromEnv(Map<String, String> env, String parameter, T paramType) {
		if (!env.containsKey(parameter)) {
			logger.error(
					"Environment variable \"" + parameter + "\" is not set. Aborting.");
			throw new IllegalArgumentException(
					"Environment variable \"" + parameter + "\" is not set. Aborting.");
		}
		try {
			if (paramType instanceof String) {
				return (T) env.get(parameter);
			} else if (paramType instanceof Integer) {
				return (T) (Integer) Integer.parseInt(env.get(parameter));
			} else if (paramType instanceof Long) {
				return (T) (Long) Long.parseLong(env.get(parameter));
			} else if (paramType instanceof Double) {
				return (T) (Double) Double.parseDouble(env.get(parameter));
			} else if (paramType instanceof Float) {
				return (T) (Float) Float.parseFloat(env.get(parameter));
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Couldn't get \"" + parameter + "\" from the environment. Aborting.", e);
		}
		return paramType;
	}

// ------------------------------------------------------------------------------------------------------------------

	public static Configurations getConfigurations() {
		return configurations;
	}

	public static void setConfigurations(Configurations conf) {
		configurations = conf;
	}

	public static void loadPropertiesFile(String file) throws IOException {
		configurations.loadFromFile(file);
	}
// ------------------------------------------------------------------------------------------------------------------


	@Override
	public void receiveCommand(byte command, byte[] data) {

		if (command == DATA_GEN_FINISHED) {
			// any work can be done here if needed before task generator can start its work
			for(int i=0; i<numOfOps; i++){
				sendDataToTaskGenRequiredForTaskGeneration();
			}


			generateTasks.release();
		}
		super.receiveCommand(command, data);

	}

// ------------------------------------------------------------------------------------------------------------------

	@Override
	public void close() throws IOException {
		// Free the resources you requested here
		logger.debug("close()");
		// Always close the super class after yours!
		super.close();
	}

}