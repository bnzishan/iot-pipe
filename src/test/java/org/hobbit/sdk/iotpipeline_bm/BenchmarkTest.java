package org.hobbit.sdk.iotpipeline_bm;


import com.spotify.docker.client.messages.PortBinding;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.components.Component;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.sdk.docker.AbstractDockerizer;
import org.hobbit.sdk.docker.RabbitMqDockerizer;
import org.hobbit.sdk.docker.builders.BuildBasedDockersBuilder;
import org.hobbit.sdk.docker.builders.hobbit.*;
import org.hobbit.sdk.iotpipeline_bm.benchmark.*;
import org.hobbit.sdk.iotpipeline_bm.system.SystemAdapter;
import org.hobbit.sdk.iotpipeline_bm.system.kafka.KafkaManager;
import org.hobbit.sdk.iotpipeline_bm.system.storm.StormNimbus;
import org.hobbit.sdk.iotpipeline_bm.system.storm.StormSupervisor;
import org.hobbit.sdk.iotpipeline_bm.system.zookeeper.ZKManager;
import org.hobbit.sdk.utils.CommandQueueListener;
import org.hobbit.sdk.utils.ComponentsExecutor;
import org.hobbit.sdk.utils.ModelsHandler;
import org.hobbit.sdk.utils.MultiThreadedImageBuilder;
import org.hobbit.sdk.utils.commandreactions.CommandReactionsBuilder;
import org.hobbit.vocab.HOBBIT;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.hobbit.core.Constants.*;
import static org.hobbit.sdk.Constants.HOBBIT_NETWORKS;


//import org.hobbit.sdk.docker.MultiThreadedImageBuilder;
//import MultiThreadedImageBuilder;


/**
 * @author Pavel Smirnov
 */

public class BenchmarkTest{

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkTest.class);

    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private AbstractDockerizer rabbitMqDockerizer;
    private CommandQueueListener commandQueueListener;
    private ComponentsExecutor componentsExecutor;
    //private MyComponentsExecutor componentsExecutor;


	BenchmarkDockerBuilder benchmarkBuilder;
	DataGenDockerBuilder dataGeneratorBuilder;
	TaskGenDockerBuilder taskGeneratorBuilder;
	EvalStorageDockerBuilder evalStorageBuilder;
	EvalModuleDockerBuilder evalModuleBuilder;
	SystemAdapterDockerBuilder systemAdapterBuilder;

	//dev
	// private ZookeeperDockerBuilder zookeeperBuilder;
	private BuildBasedDockersBuilder zookeeperBuildBasedDockersBuilder;  // BuildBasedDockersBuilder extends AbstractDockersBuilder
	private BuildBasedDockersBuilder kafkaBuildBasedDockersBuilder;  // BuildBasedDockersBuilder extends AbstractDockersBuilder
	private BuildBasedDockersBuilder stormNimbusBuildBasedDockersBuilder;
	private BuildBasedDockersBuilder stormSupervisorBuildBasedDockersBuilder;

	// initilize these values later, 1. as pure java components (requires org.hobbit.sdk.sensemark2.DataStorageBenchmark dependency) or 2. containerized
	Component benchmarkController;
	Component dataGen;
	Component taskGen;
	Component evalStorage;
	Component systemAdapter;
	Component evalModule;

	Component zkManager;
	Component kafkaManager;
	Component stormNimbusManager;
	Component stormSupervisorManager;


    public void init(Boolean useCachedImage) throws Exception {

        //  custom builder to create images on fly
        benchmarkBuilder = new BenchmarkDockerBuilder( new ExampleDockersBuilder(BenchmarkController.class, Constants.BENCHMARK_IMAGE_NAME ) //,"docker/benchmarkcontroller.docker" )
                                                     .dockerfilePath("docker/benchmark-controller.docker")
                                                    .useCachedImage(useCachedImage) );


        dataGeneratorBuilder = new DataGenDockerBuilder(new ExampleDockersBuilder(DataGenerator.class, Constants.DATAGEN_IMAGE_NAME) //,"docker/datagenerator.docker" )
                                                       .dockerfilePath("docker/data-generator.docker")
                                                      //  .addFileOrFolder("target/main-module-1.0.0.jar")
                                                      .useCachedImage(useCachedImage) );

        taskGeneratorBuilder = new TaskGenDockerBuilder(new ExampleDockersBuilder(TaskGenerator.class, Constants.TASKGEN_IMAGE_NAME) // , "docker/taskgenerator.docker")
                                                      .dockerfilePath("docker/task-generator.docker")
			//	.addFileOrFolder("target/main-module-1.0.0.jar")
                                                     .useCachedImage(useCachedImage) );

        evalStorageBuilder = new EvalStorageDockerBuilder(new ExampleDockersBuilder(EvalStorage.class, Constants.EVAL_STORAGE_IMAGE_NAME)
                                                 //   .dockerfilePath("docker/evaluation-storage.docker")
                                                    .useCachedImage(useCachedImage));

        evalModuleBuilder = new EvalModuleDockerBuilder(new ExampleDockersBuilder(EvalModule.class, Constants.EVALMODULE_IMAGE_NAME)
                                                //    .dockerfilePath("docker/evaluation-module.docker")
				//.addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY))
                                                    .useCachedImage(useCachedImage));

      //  systemAdapterBuilder = new SystemAdapterDockerBuilder(new ExampleDockersBuilder(SystemAdapter.class, SYSTEM_IMAGE_NAME).useCachedImage(useCachedImage));

        //using custom docker file
        systemAdapterBuilder = new SystemAdapterDockerBuilder(new ExampleDockersBuilder(SystemAdapter.class, Constants.SYSTEM_IMAGE_NAME) // ,"docker/systemadapter.docker")
                                                    .dockerfilePath("docker/system-adapter.docker")
                                                    .useCachedImage(useCachedImage));


       // zookeeperBuilder =  new ZookeeperDockerBuilder(new ExampleDockersBuilder(ZKManager.class, ZOOKEEPER_IMAGE_NAME,"docker/zookeeper.docker").useCachedImage(useCachedImage));

        // zookeeperBuilder =  new ZookeeperDockerBuilder(new ExampleDockersBuilder(ZKManager.class, ZOOKEEPER_IMAGE_NAME).useCachedImage(useCachedImage));

        zookeeperBuildBasedDockersBuilder = new ExampleDockersBuilder( ZKManager.class, Constants.ZOOKEEPER_IMAGE_NAME)
                .dockerfilePath("docker/zookeeper.docker")
                .addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY))
                .addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY))
                .addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY))
                .addEnvironmentVariable("TRY_ENV", "try_value")
                .addNetworks(HOBBIT_NETWORKS)
                .skipLogsReading(false)
                .useCachedImage(useCachedImage);

        kafkaBuildBasedDockersBuilder = new ExampleDockersBuilder( KafkaManager.class, Constants.KAFKA_IMAGE_NAME)
                .dockerfilePath("docker/kafka.docker")
                .addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY))
                .addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY))
                .addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY))
                .addNetworks(HOBBIT_NETWORKS)
                .skipLogsReading(false)
                .useCachedImage(useCachedImage);

        stormNimbusBuildBasedDockersBuilder = new ExampleDockersBuilder( StormNimbus.class, Constants.STORM_NIMBUS_IMAGE_NAME)
                .dockerfilePath("docker/storm-nimbus.docker")
                .addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY))
                .addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY))
                .addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY))
                .addNetworks(HOBBIT_NETWORKS)
                .skipLogsReading(false)
             //   .addPortBindings("3773", PortBinding.of("0.0.0.0", 49773))
             //   .addPortBindings("3772", PortBinding.of("0.0.0.0", 49772))
             //   .addPortBindings("6627", PortBinding.of("0.0.0.0", 49627))
                .addPortBindings("22", PortBinding.of("0.0.0.0", 50022))
                .useCachedImage(useCachedImage);

        stormSupervisorBuildBasedDockersBuilder = new ExampleDockersBuilder( StormSupervisor.class, Constants.STORM_SUPERVISOR_IMAGE_NAME)
                .dockerfilePath("docker/storm-supervisor.docker")
                .addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY))
                .addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY))
                .addEnvironmentVariable(HOBBIT_EXPERIMENT_URI_KEY, (String)System.getenv().get(HOBBIT_EXPERIMENT_URI_KEY))
                .addNetworks(HOBBIT_NETWORKS)
                .skipLogsReading(false)
                .addPortBindings("22", PortBinding.of("0.0.0.0", 50122))
            //    .addPortBindings("8000", PortBinding.of("0.0.0.0", 49800))
                .useCachedImage(useCachedImage);


        // pull based builder.. for already pushed images (at hobbit git lab)
//        benchmarkBuilder = new BenchmarkDockerBuilder(new PullBasedDockersBuilder(BENCHMARK_IMAGE_NAME));
//        dataGeneratorBuilder = new DataGenDockerBuilder(new PullBasedDockersBuilder(DATAGEN_IMAGE_NAME));
//        taskGeneratorBuilder = new TaskGenDockerBuilder(new PullBasedDockersBuilder(TASKGEN_IMAGE_NAME));
//        evalStorageBuilder = new EvalStorageDockerBuilder(new PullBasedDockersBuilder(EVAL_STORAGE_IMAGE_NAME));
//        evalModuleBuilder = new EvalModuleDockerBuilder(new PullBasedDockersBuilder(EVALMODULE_IMAGE_NAME));


    }


    @Test
    public void buildImages() throws Exception {

        init(false);

        MultiThreadedImageBuilder builder = new MultiThreadedImageBuilder(15);

        builder.addTask(benchmarkBuilder);
        builder.addTask(dataGeneratorBuilder);
        builder.addTask(taskGeneratorBuilder);
        builder.addTask(evalStorageBuilder);
        builder.addTask(systemAdapterBuilder);
        builder.addTask(evalModuleBuilder);

		// builder.addTask(zookeeperBuilder);
		builder.addTask(zookeeperBuildBasedDockersBuilder);
		builder.addTask(kafkaBuildBasedDockersBuilder);
		builder.addTask(stormNimbusBuildBasedDockersBuilder);
		builder.addTask(stormSupervisorBuildBasedDockersBuilder);

        builder.build();

    }

    @Test
    public void checkHealth() throws Exception {
        checkHealth(false);
    }

    @Test
    public void checkHealthDockerized() throws Exception {
        checkHealth(true);
    }

    //Flush a queue of a locally running platform
    @Test
    @Ignore
    public void flushQueue(){
        QueueClient queueClient = new QueueClient(Constants.GIT_USERNAME);
        queueClient.flushQueue();
    }

    //Submit benchmark to a queue of a locally running platform
    @Test
    @Ignore
    public void submitToQueue() throws Exception {
        QueueClient queueClient = new QueueClient(Constants.GIT_USERNAME);
        queueClient.submitToQueue(Constants.BENCHMARK_URI, Constants.SYSTEM_URI, createBenchmarkParameters());
    }


    private void checkHealth(Boolean dockerized) throws Exception {


        Boolean useCachedImages = false;
        init(useCachedImages);

        rabbitMqDockerizer = RabbitMqDockerizer.builder().useCachedContainer().build();

        //  using junit to set system wide env, for temp purposes. otherwise set by platform
        environmentVariables.set(RABBIT_MQ_HOST_NAME_KEY, "rabbit");
        environmentVariables.set(HOBBIT_SESSION_ID_KEY, "session_"+String.valueOf(new Date().getTime()));

        if(!dockerized) {
            //use java components if dockerized false
            //initilize these values to run them as pure java components (requires benchmark dependency, so import accordingly)

            benchmarkController = new BenchmarkController();
            dataGen = new DataGenerator();
            taskGen = new TaskGenerator();
            evalModule = new EvalModule();
            systemAdapter = new SystemAdapter();
            evalStorage = new EvalStorage();

			zkManager = new ZKManager();
			kafkaManager = new KafkaManager();
			stormNimbusManager = new StormSupervisor();
			stormSupervisorManager = new StormSupervisor();

		}

        //if dockerized=true,i.e. components are running in containers, then replace the components with docker builder equivalents
         if(dockerized) {


            benchmarkController = benchmarkBuilder.build();   // returns Dockerizer and stores in Component benchmarkController
            dataGen = dataGeneratorBuilder.build();
            taskGen = taskGeneratorBuilder.build();
            evalStorage = evalStorageBuilder.build();
            evalModule = evalModuleBuilder.build();
            systemAdapter = systemAdapterBuilder.build();

			//zkManager = zookeeperBuilder.build();
			zkManager = zookeeperBuildBasedDockersBuilder.build();
			kafkaManager = kafkaBuildBasedDockersBuilder.build();
			stormNimbusManager = stormNimbusBuildBasedDockersBuilder.build();
			stormSupervisorManager = stormSupervisorBuildBasedDockersBuilder.build();

		}

        commandQueueListener = new CommandQueueListener();
        componentsExecutor = new ComponentsExecutor(); //MyComponentsExecutor();


        rabbitMqDockerizer.run();

         //comment the .systemAdapter(systemAdapter) line below to use the code for running from python
        CommandReactionsBuilder commandReactionsBuilder = new CommandReactionsBuilder(componentsExecutor, commandQueueListener)
                        .benchmarkController(benchmarkController).benchmarkControllerImageName(Constants.BENCHMARK_IMAGE_NAME)              // (DockerBuilder(Dockerizer)
                        .dataGenerator(dataGen).dataGeneratorImageName(dataGeneratorBuilder.getImageName())
                        .taskGenerator(taskGen).taskGeneratorImageName(taskGeneratorBuilder.getImageName())
                        .evalStorage(evalStorage).evalStorageImageName(evalStorageBuilder.getImageName())
                        .evalModule(evalModule).evalModuleImageName(evalModuleBuilder.getImageName())
                        .systemAdapter(systemAdapter).systemAdapterImageName(Constants.SYSTEM_IMAGE_NAME)

				//   .zkManager(zkManager).zkManagerImageName(ZOOKEEPER_IMAGE_NAME)
				.customContainerImage(zkManager, zookeeperBuildBasedDockersBuilder.getImageName())
				.customContainerImage(kafkaManager, kafkaBuildBasedDockersBuilder.getImageName())
				.customContainerImage(stormNimbusManager, stormNimbusBuildBasedDockersBuilder.getImageName())
				.customContainerImage(stormSupervisorManager, stormSupervisorBuildBasedDockersBuilder.getImageName())


                      /*  .dataGenerator(dataGen).dataGeneratorImageName(DATAGEN_IMAGE_NAME)
                        .taskGenerator(taskGen).taskGeneratorImageName(TASKGEN_IMAGE_NAME)
                        .evalStorage(evalStorage).evalStorageImageName(EVAL_STORAGE_IMAGE_NAME)
                        .evalModule(evalModule).evalModuleImageName(EVALMODULE_IMAGE_NAME)
                        .systemAdapter(systemAdapter).systemAdapterImageName(SYSTEM_IMAGE_NAME)*/
                        //.customContainerImage(systemAdapter, DUMMY_SYSTEM_IMAGE_NAME)
        ;

         commandQueueListener.setCommandReactions(


                commandReactionsBuilder.containerCommandsReaction(), //comment this if you want to run containers on a platform instance (if the platform is running)
                commandReactionsBuilder.benchmarkSignalsReaction()

        );


        componentsExecutor.submit(commandQueueListener);
        commandQueueListener.waitForInitialisation();



        // Start components without sending command to queue. Components will be executed by SDK, not the running platform (if it is running)
       /*  String benchmarkContainerId = "benchmark";
        String systemContainerId = "system";

       componentsExecutor.submit(benchmarkController, benchmarkContainerId, new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+NEW_EXPERIMENT_URI,   BENCHMARK_PARAMETERS_MODEL_KEY+"="+ RabbitMQUtils.writeModel2String(createBenchmarkParameters() ) });
        componentsExecutor.submit(systemAdapter, systemContainerId, new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+NEW_EXPERIMENT_URI, SYSTEM_PARAMETERS_MODEL_KEY+"="+ RabbitMQUtils.writeModel2String (createSystemParameters() ) });
*/

        // Alternative for dockerized=true. Start components via command queue (will be executed by the platform (if running)) ::

        //String[] benchmarkParamsStr = new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+NEW_EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ RabbitMQUtils.writeModel2String(ModelsHandler.createMergedParametersModel(createBenchmarkParameters(), ModelsHandler.readModelFromFile("benchmark.ttl"))) };
       // String [] systemParamsStr = new String[]{ SYSTEM_PARAMETERS_MODEL_KEY+"="+  RabbitMQUtils.writeModel2String(ModelsHandler.createMergedParametersModel(createSystemParameters(), ModelsHandler.readModelFromFile("system.ttl"))) };


		//  String[] benchmarkParamsStr = new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+NEW_EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ RabbitMQUtils.writeModel2String(createBenchmarkParameters()) };
     //   String [] systemParamsStr = new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+NEW_EXPERIMENT_URI, SYSTEM_PARAMETERS_MODEL_KEY+"="+  RabbitMQUtils.writeModel2String(createSystemParameters()) };

		Model paramsModel = this.readModelFromFile("benchmark.ttl");
/*
		ResIterator iterator = paramsModel.listResourcesWithProperty(RDF.type, HOBBIT.Experiment);
		Resource experimentResource = iterator.nextResource();
		Property defaultValProperty = paramsModel.getProperty("http://w3id.org/hobbit/vocab#defaultValue");
		while(iterator.hasNext()) {
			try {
				Property parameter = paramsModel.getProperty(((Resource)iterator.next()).getURI());
				if(experimentResource.getProperty(parameter) == null) {
					NodeIterator objIterator = paramsModel.listObjectsOfProperty(parameter, defaultValProperty);

					while(objIterator.hasNext()) {
						Literal e = (Literal)objIterator.next();
						paramsModel.add(experimentResource, parameter, e.getString());
					}
				}
			} catch (Exception var9) {
				var9.printStackTrace();
			}
		}

*/

		NodeIterator iterator = paramsModel.listObjectsOfProperty(paramsModel
				.getProperty("http://project-hobbit.eu/iotpipe-benchmark/benchmarkMode"));
		if (iterator.hasNext()) {
			try {

				String mode = iterator.next().asLiteral().getString();
				logger.debug("mode {} ", mode);

			} catch (Exception e) {
				logger.debug("Couldn't get mode", e);
			}
		}


		 String[] benchmarkParamsStr = new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+NEW_EXPERIMENT_URI,  BENCHMARK_PARAMETERS_MODEL_KEY+"="+ RabbitMQUtils.writeModel2String(paramsModel)};
		String [] systemParamsStr = new String[]{ HOBBIT_EXPERIMENT_URI_KEY+"="+NEW_EXPERIMENT_URI, SYSTEM_PARAMETERS_MODEL_KEY+"="+  RabbitMQUtils.writeModel2String(ModelsHandler.readModelFromFile("system.ttl")) };




		String  benchmarkContainerId = commandQueueListener.createContainer(benchmarkBuilder.getImageName(), "benchmark", benchmarkParamsStr);
      String  systemContainerId = commandQueueListener.createContainer(systemAdapterBuilder.getImageName(), "system" , systemParamsStr);


        environmentVariables.set("BENCHMARK_CONTAINER_ID", benchmarkContainerId);
        environmentVariables.set("SYSTEM_CONTAINER_ID", systemContainerId);

        commandQueueListener.waitForTermination();
        commandQueueListener.terminate();
        componentsExecutor.shutdown();

        rabbitMqDockerizer.stop();

        Assert.assertFalse(componentsExecutor.anyExceptions());
    }





	public static Model readModelFromFile(String path) throws IOException {
		byte[] bytes = FileUtils.readFileToByteArray(new File(path));
		Model model = byteArrayToModel(bytes, "TTL");
		return model;
	}

	public static Model byteArrayToModel(byte[] data, String lang) {
		Model m = ModelFactory.createDefaultModel();
		m.read(new ByteArrayInputStream(data), (String)null, lang);
		return m;
	}





    public static Model createBenchmarkParameters() throws IOException {

         Model model = createDefaultModel();


        String benchmarkInstanceId = org.hobbit.core.Constants.NEW_EXPERIMENT_URI;
        Resource experimentResource = model.createResource(benchmarkInstanceId);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);


        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.BENCHMARK_MODE), "pipeline");

      /*  model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.HAS_ZOOKEEPER),"1");
        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.HAS_KAFKA),"1");
        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.HAS_STORM),"1");
        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.HAS_HBASE),"0");
*/
        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.DATA_SIZE_SCALE_FACTOR),"1");
      //  model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.NUM_OF_SENSORS), "54");
      //  model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.EVENT_INTERVAL_IN_SECS), "31");

        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.DATA_INPUT_FORMAT), ".dat");
        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.PREGEN_DATASETS), "sensors.dat");
        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.EXTERNAL_DATA_GEN), "");

     //   model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.NUM_OF_TASKS), "5");
		model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.NUM_OF_OPS), "1");

        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.TASKS), "spike_detection");
        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.SEED), "1");

        // model.add(experimentResource, model.createProperty(BENCHMARK_URI + "/" + SEQUENTIAL_TASKS), "1");
		// add task config here. currently reading from .properfies file

        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.AVG_EXECUTION_TIME), "0");
		model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.AVG_EXECUTION_TIME_T1), "0");
        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.EXECUTION_TIME_THRESHOLD), "30000");
        model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.NUM_OF_TASK_FAILURES), "0");
		model.add(experimentResource, model.createProperty(Constants.BENCHMARK_URI + "/" + Constants.SEQUENTIAL_TASKS), "true");
      //  model.add(experimentResource, model.createProperty(BENCHMARK_URI + "/" + TIMEOUT_MINUTES), "1");

        return model;

    }

    public static Model createSystemParameters() throws IOException {


        Model model = createDefaultModel();

        //Resource experimentResource = model.createResource(NEW_EXPERIMENT_URI);
        String benchmarkInstanceId = org.hobbit.core.Constants.NEW_EXPERIMENT_URI;
        Resource experimentResource = model.createResource(benchmarkInstanceId);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);

     //   model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.PIPELINE_SIZE),"3");

        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.DATASET_FOLDER_NAME), "datasets");

        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.ZOOKEEPER_INIT_STEP), "1");
        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.KAFKA_INIT_STEP), "2");
        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.STORM_INIT_STEP), "3");
        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.HBASE_INIT_STEP), "4");

        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.ZOOKEEPER_CLUSTER_SIZE),"3");
        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.ZOO_CLIENT_PORTS), "31102,31202,31302");
        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.ZOO_FOLLOWER_PORTS),"31100,31200,31300");
        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.ZOO_ELECTION_PORTS),"31101,31201,31301");

        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.KAFKA_CLUSTER_SIZE),"1");
        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.KAFKA_INSIDE_PORTS),"9092,9096,9097");
        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.KAFKA_OUTSIDE_PORTS),"9094,9098,9099");
        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.KAFKA_HOSTNAME_COMMAND),"route -n | awk '/UG[ \t]/{print $$2}'");  //  ip route show 0.0.0.0/0 dev eth0 | cut -d\  -f3 // /sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'
        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.KAFKA_CREATE_TOPICS),"Sensors:1:1:delete");

        model.add(experimentResource, model.createProperty(Constants.SYSTEM_PARAM_URI_PREFIX + Constants.NUM_OF_STORM_SUPERVISORS),"1");



/*
            //adding values only if specified to avoid Model exception
              if(System.getenv().containsKey("key1"))
            model.add(experimentResourceNode, model.createProperty(SYSTEM_PARAM_URI_PREFIX +"#key1"), System.getenv("key1"));
*/

        return model;
    }



}
