package org.hobbit.sdk.iotpipeline_bm;


/**
 * @author Bushra Nazir
 */

public class Constants {

	public static String GIT_USERNAME = "bushran";
	public static String GIT_REPO_PATH = "git.project-hobbit.eu:4567/"+GIT_USERNAME+"/";

    public static String PROJECT_NAME =  "iotpipe-benchmark" ;   //project at hobbit gitlab to store bm and system images
    //public static String PROJECT_DIR_NAME = "sdk-example-benchmark";
    public static final String SYSTEM_PROJECT_NAME =  "iotpipe-system" ;

    public static final String BENCHMARK_URI = "http://project-hobbit.eu/"+PROJECT_NAME;

    public static final String SYSTEM_URI ="http://project-hobbit.eu/"+ SYSTEM_PROJECT_NAME; // + "/system";

    public static final String BENCHMARK_PARAM_URI_PREFIX = BENCHMARK_URI + "/";
    public static final String SYSTEM_PARAM_URI_PREFIX = SYSTEM_URI + "/";


    // usage in ExampleDockersBuilder
    public static final String SDK_BUILD_DIR_PATH = ".";  //build directory, temp docker file will be created there
    public static final String SDK_WORK_DIR_PATH = "/" + PROJECT_NAME; // used by    dockerWorkDir(SDK_WORK_DIR_PATH)  while creating docker files in ExampleDockersBuilder

    //use these constants within BenchmarkController
    public static final String BENCHMARK_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"/benchmark-controller";
    public static final String DATAGEN_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"/datagen";
    public static final String TASKGEN_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"/taskgen";
    public static final String EVAL_STORAGE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"/eval-storage";
    public static final String EVALMODULE_IMAGE_NAME = GIT_REPO_PATH+PROJECT_NAME +"/eval-module";

    public static final String EVALMODULE_IMAGE_NAME_2 = GIT_REPO_PATH+PROJECT_NAME +"/eval-module_2";



    public static final String BENCHMARK_PREFIX_IMAGE_URI = GIT_REPO_PATH+PROJECT_NAME +"/";

    public static final String SYSTEM_IMAGE_NAME = GIT_REPO_PATH+SYSTEM_PROJECT_NAME +"/system-adapter";
    public static final String ZOOKEEPER_IMAGE_NAME = GIT_REPO_PATH+SYSTEM_PROJECT_NAME +"/zookeeper";
    public static final String KAFKA_IMAGE_NAME = GIT_REPO_PATH+SYSTEM_PROJECT_NAME +"/kafka";
    public static final String STORM_NIMBUS_IMAGE_NAME = GIT_REPO_PATH+SYSTEM_PROJECT_NAME +"/storm-nimbus";
    public static final String STORM_SUPERVISOR_IMAGE_NAME = GIT_REPO_PATH+SYSTEM_PROJECT_NAME +"/storm-supervisor";
    public static final String STORM_UI_IMAGE_NAME = GIT_REPO_PATH+SYSTEM_PROJECT_NAME +"/storm-ui";
    //public static final String CARBON_GRAPHITE_IMAGE_NAME = GIT_REPO_PATH+SYSTEM_PROJECT_NAME +"/carbon-graphite";
    public static final String GRAPHITE_IMAGE_NAME = "kamon/grafana_graphite";



    // ===============  QUEUE NAMES =============== //


  // for now, in CommandsAndQueues.java



    // =============== benchmark.ttl  =============== //
    /**
     * KPIS
     */
    public static final String AVG_EXECUTION_TIME_T1 = "avgExecutionTimeT1";
    public static final String AVG_EXECUTION_TIME = "avgExecutionTime";
    public static final String THROUGHPUT = "throughput";
    public static final String NUM_OF_TASK_FAILURES = "numOfTaskFailures";


    /**
     * Feature & Configurable Parameters
     */
    public static final String BENCHMARK_MODE = "benchmarkMode";

  /*  public static final String HAS_ZOOKEEPER = "hasZookeeper";
    public static final String HAS_KAFKA = "hasKafka";
    public static final String HAS_STORM = "hasStorm";
    public static final String HAS_HBASE  = "hasHbase";*/

    public static final String DATA_INPUT_FORMAT = "dataInputFormat";

    public static final String PREGEN_DATASETS = "pregenDataSets";
    public static final String EXTERNAL_DATA_GEN = "externalDataGen";

    public static final String DATA_SIZE_SCALE_FACTOR = "dataSizeScaleFactor";
    //public static final String NUM_OF_SENSORS= "numOfSensor";
    //public static final String EVENT_INTERVAL_IN_SECS= "eventIntervalInSecs";

    public static final String NUM_OF_TASKS = "numOfTasks";
    public static final String NUM_OF_OPS = "numOfOps";
    public static final String TASKS = "tasks";
    public static final String SEQUENTIAL_TASKS = "hasSequentialTasks";

    public static final String INTERVAL = "interval";
    public static final String SEED = "seed";
    public static final String TIMEOUT_MINUTES = "timeoutMinutes";






    // =============== SYSTEM CONSTANTS / system.ttl =============== //
    /**
     * Feature & Configurable Parameters
     */
    public static final String PIPELINE_SIZE = "pipelineSize";
    public static final String REPLICATION_FACTOR = "replicationFactor";
    public static final String PIPELINE_DEPLOYMENT_TYPE = "pipelineDeploymentType";

    public static final String ZOOKEEPER_INIT_STEP = "zookeeper_init_step";
    public static final String KAFKA_INIT_STEP = "kafka_init_step";
    public static final String STORM_INIT_STEP = "storm_init_step";
    public static final String HBASE_INIT_STEP = "hbase_init_step";

    public static final String DATASET_FOLDER_NAME = "datasetFolderName";

    public static final String ZOOKEEPER_CLUSTER_SIZE = "zookeeperClusterSize";
    public static final String ZOOKEEPER_ENSEMBLE = "zookeeperEnsembel";
    public static final String ZOO_CLIENT_PORTS = "zooClientPort";
    public static final String ZOO_FOLLOWER_PORTS = "zooFollowerPorts";
    public static final String ZOO_ELECTION_PORTS = "zooElectionPorts";

    public static final String KAFKA_CLUSTER_SIZE = "kafkaClusterSize";
    public static final String KAFKA_INSIDE_PORTS = "kafkaInsidePorts";
    public static final String KAFKA_OUTSIDE_PORTS = "kafkaOutsidePorts";
    public static final String KAFKA_ZOOKEEPER_CONNECT = "kafkaZookeeperConnect";
    public static final String KAFKA_HOSTNAME_COMMAND = "kafkaHostnameCommand";
    public static final String KAFKA_CREATE_TOPICS = "kafkaCreateTopics";

    public static final String NUM_OF_STORM_SUPERVISORS = "numOfStormSupervisors";

    public static final String EXECUTION_TIME = "executionTime";
    public static final String EXECUTION_TIME_THRESHOLD = "executionTimeThreshold";



    public static final String HBASE_CLUSTER_SIZE = "hbaseClusterSize";


    // ===============EVALUATION  CONSTANTS  =============== //




}
