# IoTPipeBenchMark  
Benchmark IoT Pipelines based on HOBBIT platform, using Java SDK project. 
Current implementation: Evaluating Apache Storm 

This repository contains  [HOBBIT-compatible components](https://hobbit-project.github.io/) for benchmarking of Big Data systems,  and number of tests, required to debug components locally without having a running instance of the platform.

This is (`main-module`) of a multi-module maven project. Clone this repository (`https://github.com/bnzishan/iot-pipe`) and its parent maven module from  (`https://github.com/bnzishan/iot-pipe-root`). This module
has dependency on its sibiling [storm-module](https://github.com/bnzishan/iot-pipe-root/tree/master/storm-module). 

## Background Information
### HOBBIT and JAVA SDK 
HOBBIT platform is designed to benchmark Big Data Systems on a cluster (designed orginally for Linked Systems). It has several components that are designed as Docker containers. The benchmarking of a system has three phases.
1) Benchmark and system are created and initialized.
2) Benchmark starts to evaluate the system and stores the responses in evaluation storage.
3) System and other parts are terminated. The data in eveluation storage is used to evaluate the system responses.
More information on platform components, and how to create benchmark and system components can be found [here](https://hobbit-project.github.io/overview.html) 

IoTPipeBenchMark has been developed using  [Java SDK Example](https://github.com/hobbit-project/java-sdk). Java SDK provides tests that allow developers to debug components either as pure java codes or being packaged into docker containers. Fully tested docker images may be uploaded and executed in the online platform without any modifications.
The added value of the Java SDK against standart HOBBIT approach decribed [here](https://github.com/hobbit-project/java-sdk-example/blob/master/SDK_vs_Standard_Way.pdf).
The detailed description of the development and debug process with Java SDK can be found [here](https://github.com/hobbit-project/java-sdk).

### Benchmarking
The benchmark is designed to evaluate big data systems for sensor data applications.Initialization is done using configurations, which are given via BenchmarkTest class for testing, or ttl files. 
Based on these configurations, cluster is deployed using provided Docker files for zookeeper, kafka, storm nimbus and supervisor. Java components, for each system in the cluster, are found in [package](https://github.com/bnzishan/iot-pipe/tree/master/src/main/java/org/hobbit/sdk/iotpipeline_bm/system).
More systems can be integrated in similar manner. 
Current implementation provides workload and dataset to evaluate Apache Storm (integrated from [storm-application](https://github.com/mayconbordin/storm-applications) ). Workload in the form of storm topology .jar file is added in [resources directory](https://github.com/bnzishan/iot-pipe/tree/master/src/main/resources/tasks/storm_tasks/spike_detection) .
To test,extend,integrate more workloads, refer to the [storm-module](https://github.com/bnzishan/iot-pipe-root/tree/master/storm-module). Benchmark Components are needed to be extended/modified accordingly.


## Usage
1) Make sure that Oracle 1.8 (or higher) is installed (`java -version`). Or install it by the `sudo add-apt-repository ppa:webupd8team/java && sudo apt-get update && sudo apt-get install oracle-java8-installer -y`.
2) Make sure that docker (v17 and later) is installed (or install it by `sudo curl -sSL https://get.docker.com/ | sh`)
3) Make sure that maven (v3 and later) is installed (or install it by `sudo apt-get install maven`)
4) Add the `127.0.0.1 rabbit` line to `/etc/hosts` (Linux) or `C:\Windows\System32\drivers\etc\hosts` (Windows)
5) Clone this repository (`https://github.com/bnzishan/iot-pipe`) and its parent maven module from  (`https://github.com/bnzishan/iot-pipe-root`).
6) Install local dependencies into your local maven repository (`mvn validate`). (for SDK project and storm-applications)
7) Docker images for  components of the benchmark and system are found in [directory](https://github.com/bnzishan/iot-pipe/tree/master/docker).Configurations files `benchmark.ttl` and `system.ttl` reside in root directory of the module.
8) For benchmark and system, create projects on [HOBBIT Gitlab intance](https://git.project-hobbit.eu)
9) Push images to registories, and ttl files to root directory of the projects.
10) Benchmark and system will be available at [remote instance](https://keycloak.project-hobbit.eu) for testing. Select benchmark and system, and run experiments.


## benchmark 
1) Please find  basic benchmark component implementations in the [source folder](https://github.com/bnzishan/iot-pipe/tree/master/src/main/java/org/hobbit/sdk/iotpipeline_bm/benchmark). 
2) Specific DockerFiles for benchmark components (benchmark-controller, data-generator, task-generator, evaluation-module, evaluation-storage) are present in [directory](https://github.com/bnzishan/iot-pipe/tree/master/docker). 
3) Pack source codes with dependencies into jar file (via the `make package` command).
4) Run the `make build-images` or execute the `buildImages()` method from [ExampleBenchmarkTest](https://github.com/bnzishan/iot-pipe/blob/master/src/test/java/org/hobbit/sdk/iotpipeline_bm/ExampleDockersBuilder.java) to build docker images. Images will be build from jar-file you have packed on step 3, so make sure that jar-file is actual and contains all changes (if made any). 
5) Execute components as docker containers (run the `make test-dockerized-benchmark` command or `checkHealthDockerized()` method from [ExampleBenchmarkTest](https://github.com/hobbit-project/java-sdk-example/blob/master/src/test/java/org/hobbit/sdk/examples/examplebenchmark/ExampleBenchmarkTest.java) via IDE). You may see all the logs from your containers.
6) Once you have working images of your benchmark you may upload the to remote repository (by manual running the `docker login ... `, `docker push ...` commands) without any changes.
7) Upload benchmark ttl-file to project in Hobbit GitLab Instance.

## system
1) Please find  basic system component implementations in the [source folder](https://github.com/bnzishan/iot-pipe/tree/master/src/main/java/org/hobbit/sdk/iotpipeline_bm/system). 
2) Specific DockerFiles for system components (system-adapter, kafka, storm-nimbus, storm-supervisor) are present in [directory](https://github.com/bnzishan/iot-pipe/tree/master/docker). 
3) Pack source codes with dependencies into jar file (via the `make package` command).
4) Run the `make build-images` or execute the `buildImages()` method from [ExampleBenchmarkTest](https://github.com/bnzishan/iot-pipe/blob/master/src/test/java/org/hobbit/sdk/iotpipeline_bm/ExampleDockersBuilder.java) to build docker images. Images will be build from jar-file you have packed on step 3, so make sure that jar-file is actual and contains all changes (if made any). 
5) Run the `make test-dockerized-benchmark` command or execute the `checkHealthDockerized()` test from [ExampleSystemTest.java](https://github.com/bnzishan/iot-pipe/blob/master/src/test/java/org/hobbit/sdk/iotpipeline_bm/BenchmarkTest.java) to test/debug the system.  
6) Once you have working images of your system components you may upload the to remote repository (by manual running the `docker login ... `, `docker push ...` commands) without any changes.
7) Upload system ttl-file to project in Hobbit GitLab Instance.

## Additional Information
Currently, source code for benchmark and system is present in single project. That is why same jar file is copied into the images for both benchmark and system components. 
Later, the project will be restructured into two separate projects (for benchmark and system). 


