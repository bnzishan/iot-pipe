package org.hobbit.sdk.iotpipeline_bm.benchmark;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.rdf.model.NodeIterator;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.hobbit.sdk.iotpipeline_bm.Constants.*;

//bnz


public class BenchmarkController4ttl extends AbstractBenchmarkController {
	private static final Logger logger = LoggerFactory.getLogger(BenchmarkController4ttl.class);

	private String benchmark_mode = null;

	private boolean has_zookeeper = false;
	private boolean has_kafka = false;
	private boolean has_storm = false;
	private boolean has_hbase = false;

	NodeIterator iterator = null;

	private String[] dataGen_env_variables = null;
	private String[] taskGen_env_variables = null;
	private String[] evalModule_env_variables = null;
	private String[] evalStorage_env_variables = null;


	private Semaphore data_loaded_mutex = new Semaphore(0);

	private long data_load_start_time = -1;
	private long data_load_finish_time;
	private long start_time = -1;
	private long finish_time;

	private boolean sequentialTasks = false;

	@Override
	public void init() throws Exception {
		super.init();


//################################################################################################ //
		logger.info("   ####################    Pipeline Integrations and Benchmark Execution with iotBenchMark   #######################");

//###############################################################################################  //

		logger.info(" Initialization begines ::: " + start_time);

		//  Initialization code comes here...


		// ===============  Load parameters from the benchmarks parameter model  ===============


		// loading benchmark mode
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
				//.getProperty(BENCHMARK_PARAM_URI_PREFIX + BENCHMARK_MODE));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/benchmarkMode"));


		benchmark_mode += iterator.next().asLiteral().getString();

		 // Sequential tasks
		if (sequentialTasks == false) {

			iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
					//.getProperty(SEQUENTIAL_TASKS));
					.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/hasSequentialTasks"));
			if (iterator.hasNext()) {
				try {
					sequentialTasks = iterator.next().asLiteral().getBoolean();
					logger.info("Sequential task: " + String.valueOf(sequentialTasks));
				} catch (Exception e) {
					logger.error("Exception while parsing parameter.", e);
				}
			}
		}


		// =============================================  Create data generators=============================================

		int numberOfDataGenerators = 1;

		//  loading  data input format parameter : from the benchmarks parameter model using local method getPropertyOrDefault()
//        String data_input_format = (String) getPropertyOrDefault(BENCHMARK_PARAM_URI_PREFIX+DATA_INPUT_FORMAT, "csv");
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
				//.getProperty(BENCHMARK_PARAM_URI_PREFIX + DATA_INPUT_FORMAT));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/dataInputFormat"));

		String data_input_format = iterator.next().asLiteral().getString();


		// in caseof pre-gen data
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
				//.getProperty(BENCHMARK_PARAM_URI_PREFIX + PREGEN_DATASETS));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/pregenDataSets"));
		String pregenDatasets = iterator.next().asLiteral().getString();

		// in caseof pre-gen data present remotely
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
				//.getProperty(BENCHMARK_PARAM_URI_PREFIX + EXTERNAL_DATA_GEN));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/externalDataGen"));
		String external_data_gen = iterator.next().asLiteral().getString();

		// loading data scale factor
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
			//	.getProperty(BENCHMARK_PARAM_URI_PREFIX + DATA_SIZE_SCALE_FACTOR));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/dataSizeScaleFactor"));
		int data_size_scale_factor = iterator.next().asLiteral().getInt();


		// num of times each task must be operated
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
				//.getProperty(BENCHMARK_PARAM_URI_PREFIX + NUM_OF_OPS));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/numOfOps"));
		int num_of_ops = iterator.next().asLiteral().getInt();


		// loading tasks
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
				//.getProperty(BENCHMARK_PARAM_URI_PREFIX + TASKS));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/tasks"));
		String tasks = iterator.next().asLiteral().getString();


		// data generators environmental values
		dataGen_env_variables = new String[]{

				DATA_INPUT_FORMAT + "=" + data_input_format,
				// NUM_OF_TASKS + "=" + num_of_tasks,
				NUM_OF_OPS + "=" + num_of_ops,
				TASKS  + "=" + tasks,
				// NUMBER_OF_DATA_GENERATORS + "=" + numberOfDataGenerators,

				PREGEN_DATASETS + "=" + pregenDatasets,
				//  EXTERNAL_DATA_GEN + "=" + external_data_gen,

				DATA_SIZE_SCALE_FACTOR + "=" + data_size_scale_factor,
			//	NUM_OF_SENSORS + "=" + num_of_sensors,
		//		EVENT_INTERVAL_IN_SECS + "=" + event_interval_in_sec
		};


		logger.debug("createDataGenerators()");
		createDataGenerators(DATAGEN_IMAGE_NAME, numberOfDataGenerators, dataGen_env_variables);


		// =============================================  Create task generators =============================================

		// loading data scale factor
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
				//.getProperty(BENCHMARK_PARAM_URI_PREFIX + DATA_SIZE_SCALE_FACTOR));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/dataSizeScaleFactor"));
		data_size_scale_factor = iterator.next().asLiteral().getInt();

		// loading num of ops per task
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
				//.getProperty(BENCHMARK_PARAM_URI_PREFIX + NUM_OF_OPS));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/numOfOps"));
		num_of_ops = iterator.next().asLiteral().getInt();

		// loading tasks
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
			//	.getProperty(BENCHMARK_PARAM_URI_PREFIX + TASKS));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/tasks"));
		tasks = iterator.next().asLiteral().getString();

		// loading seed
		iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
			//	.getProperty(BENCHMARK_PARAM_URI_PREFIX + SEED));
				.getProperty("http://project-hobbit.eu/iotpipeline-benchmark/seed"));
		int seed = iterator.next().asLiteral().getInt();

		// loading threshold
	/*	iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
				.getProperty(BENCHMARK_PARAM_URI_PREFIX + EXECUTION_TIME_THRESHOLD));
		String execTimeThreshold = iterator.next().asLiteral().getString();*/


		int numberOfTaskGenerators = 1;
		taskGen_env_variables = new String[]{
				DATA_SIZE_SCALE_FACTOR + "=" + data_size_scale_factor,
				SEED + "=" + seed,
				NUM_OF_OPS + "=" + num_of_ops,
				TASKS + "=" + tasks,
			//	EXECUTION_TIME_THRESHOLD + "=" + execTimeThreshold

		};


		logger.info("create {} task generators", "1");

     /*
        if(sequentialTasks==true){
            // TODO
        }else{}
        */

		createTaskGenerators(TASKGEN_IMAGE_NAME, numberOfTaskGenerators, taskGen_env_variables);


		// =============================================  Create evaluation storage =============================================
		logger.debug("create Evaluation Storage");
		//You can use standard evaluation storage (git.project-hobbit.eu:4567/defaulthobbituser/defaultevaluationstorage)
		//createEvaluationStorage();
		//or simplified local-one from the SDK

		evalStorage_env_variables = new String[]{
				"HOBBIT_RECEIVE_TIMESTAMP_FOR_SYSTEM_RESULTS" + "=" + "true",  //false
		};


		//TO-DO  set ACKNOWLEDGEMENT_FLAG=true for sequencing tasks
		evalStorage_env_variables = (String[]) ArrayUtils.add(DEFAULT_EVAL_STORAGE_PARAMETERS, "HOBBIT_RABBIT_HOST=" + this.rabbitMQHostName);
		//evalStorage_env_variables = (String[]) ArrayUtils.add(DEFAULT_EVAL_STORAGE_PARAMETERS, "HOBBIT_RECEIVE_TIMESTAMP_FOR_SYSTEM_RESULTS=" + "false");
		evalStorage_env_variables = (String[]) ArrayUtils.add(DEFAULT_EVAL_STORAGE_PARAMETERS, "HOBBIT_EXPERIMENT_URI" + this.experimentUri);

		if (sequentialTasks == true)
			evalStorage_env_variables = ArrayUtils.add(evalStorage_env_variables, "ACKNOWLEDGEMENT_FLAG=true");

		this.createEvaluationStorage(EVAL_STORAGE_IMAGE_NAME, evalStorage_env_variables);

		logger.info("evaluation storage created ");

		// =============================================  Create evaluation module =============================================
		// later in executeBenchmark()


		//=============================================   Wait for all components to finish their initialization ===============
		waitForComponentsToInitialize();

		logger.info("ALL BENCHMARK COMPONENTS ARE SUCCESSFULLY INITIALIZED!");
	}


//----------------------------------------------------------------------------------------------------------------------------------------------


	@Override
	public void receiveCommand(byte command, byte[] data) {

		if (command == org.hobbit.sdk.iotpipeline_bm.Commands.DATA_GEN_FINISHED_FROM_DATAGEN) { // sent by Data Generator

			data_load_start_time = System.currentTimeMillis();

			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				sendToCmdQueue(org.hobbit.sdk.iotpipeline_bm.Commands.DATA_GEN_FINISHED);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (command == org.hobbit.sdk.iotpipeline_bm.Commands.FORWARDING_DATA_TO_PIPELINE_FINISHED) { // sent by System Adapter
			data_load_finish_time = System.currentTimeMillis();  // time took by data to travel from datagen to data ingestion system in pipeline
		}
		super.receiveCommand(command, data);
	}


//----------------------------------------------------------------------------------------------------------------------------------------------


	@Override
	protected void executeBenchmark() throws Exception {
		// logger.debug("execute benchmark ");
		// give the start signals
		sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
		sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);

		start_time = System.currentTimeMillis();

		// wait for the data generators to finish their work

		logger.info("wait for Data Generartors To finish ...");
		waitForDataGenToFinish();


		// wait for the task generators to finish their work

		logger.info("wait for task gen to finish ...");
		waitForTaskGenToFinish();


		// wait for the system to terminate. Note that you can also use
		// the method waitForSystemToFinish(maxTime) where maxTime is
		// a long value defining the maximum amount of time the benchmark
		// will wait for the system to terminate.
		//taskGenContainerIds.add("system");

		logger.debug("waiting for system to  finish benchmarking tasks ...");
		waitForSystemToFinish();


		// ==============================  Create the evaluation module and Set evaluation module environmental values ==============================
/*
        // loading avg execution time
        iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
                .getProperty(BENCHMARK_PARAM_URI_PREFIX+AVG_EXECUTION_TIME));
        Long avg_execution_time = Long.parseLong(iterator.next().asLiteral().getString());

        // loading avg exec time for task 1
        iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
                .getProperty(BENCHMARK_PARAM_URI_PREFIX+AVG_EXECUTION_TIME_T1));
        int avg_execution_time_t1 = iterator.next().asLiteral().getInt();

        // loading num of tasks failure
        iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel
                .getProperty(BENCHMARK_PARAM_URI_PREFIX+NUM_OF_TASK_FAILURES));
        int num_of_tasks_failure = iterator.next().asLiteral().getInt();
*/


		logger.info("Evaluating metric :::: {}", "Execution time");

		evalModule_env_variables = new String[]{
				AVG_EXECUTION_TIME + "=" + (BENCHMARK_PARAM_URI_PREFIX + AVG_EXECUTION_TIME),               // or else use EXECUTION_TIME_T1 + "=" + EXECUTION_TIME_T1_URI,
				AVG_EXECUTION_TIME_T1 + "=" + (BENCHMARK_PARAM_URI_PREFIX + AVG_EXECUTION_TIME_T1),
				//  THROUGHPUT + "=" + throughput,
				NUM_OF_TASK_FAILURES + "=" + (BENCHMARK_PARAM_URI_PREFIX + NUM_OF_TASK_FAILURES)
				// TIMEOUT_MINUTES + "=" + timeout_minutes
		};


		evalModule_env_variables = (String[]) ArrayUtils.add(evalModule_env_variables, "HOBBIT_EXPERIMENT_URI" + this.experimentUri);

		createEvaluationModule(EVALMODULE_IMAGE_NAME, evalModule_env_variables);

		// wait for the evaluation to finish
		logger.info("Wait for the components to finish...");
		waitForEvalComponentsToFinish();


		logger.info("Evaluation finished!");

		// the evaluation module should have sent an RDF model containing the
		// results. We should add the configuration of the benchmark to this
		// model.
		// this.resultModel.add(...);


		// note execution finish time
		long currentTimeMillis = System.currentTimeMillis();
		finish_time = currentTimeMillis - start_time;

//################################################################################################ //
//#         Benchmarking execution                                                          # //
//###############################################################################################  //

		logger.info(" Total  execution time in ms :: " + start_time);

		// Send the resultModel to the platform controller and terminate
		// logger.info("Sending result model: {}", RabbitMQUtils.writeModel2String(resultModel));
		sendResultModel(resultModel);
	}


//----------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void close() throws IOException {
		logger.debug("close()");
		// Free the resources you requested here

		// Always close the super class after yours!
		super.close();
	}


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
		NodeIterator iterator = benchmarkParamModel
				.listObjectsOfProperty(benchmarkParamModel
						.getProperty(property));

		if (iterator.hasNext()) {
			try {
				if (defaultValue instanceof String) {
					if (((String) defaultValue).equals("small")) {
						logger.info("------------------ DV: small");
						Properties props = new Properties();
						try {
							props.load(ClassLoader.getSystemResource("data_size.properties").openStream());
						} catch (IOException e) {
							logger.error("Exception while parsing available data sizes.");
						}
						String str = ((String) (props.getProperty(iterator.next().asResource().getLocalName())));
						logger.info(str);
						return (T) (str);//return (T) props.getProperty(iterator.next().asResource().getLocalName());
					} else if (((String) defaultValue).equals("csv")) {
						logger.info("------------------ DV: csv");
						Properties props = new Properties();
						try {
							props.load(ClassLoader.getSystemResource("data_format.properties").openStream());
						} catch (IOException e) {
							logger.error("Exception while parsing available number of versions.");
						}
						return (T) props.getProperty(iterator.next().asResource().getLocalName());
					} else {
						logger.info("------------------ DV: getString() ");
						return (T) iterator.next().asLiteral().getString();
					}
				} else if (defaultValue instanceof Integer) {
					return (T) ((Integer) iterator.next().asLiteral().getInt());
				} else if (defaultValue instanceof Long) {
					return (T) ((Long) iterator.next().asLiteral().getLong());
				} else if (defaultValue instanceof Float) {
					return (T) ((Float) iterator.next().asLiteral().getFloat());
				} else if (defaultValue instanceof Double) {
					return (T) ((Double) iterator.next().asLiteral().getDouble());
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

//----------------------------------------------------------------------------------------------------------------------------------------------


//----------------------------------------------------------------------------------------------------------------------------------------------


//----------------------------------------------------------------------------------------------------------------------------------------------

}
