package org.hobbit.sdk.iotpipeline_bm;


import org.hobbit.core.run.ComponentStarter;
import org.hobbit.sdk.docker.builders.DynamicDockerFileBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hobbit.sdk.iotpipeline_bm.Constants.SDK_BUILD_DIR_PATH;
import static org.hobbit.sdk.iotpipeline_bm.Constants.SDK_WORK_DIR_PATH;

/**
 * @author Pavel Smirnov
 */

public class ExampleDockersBuilder extends DynamicDockerFileBuilder {   // DynamicDockerFileBuilder  extends BuildBasedDockersBuilder

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkTest.class);

    public ExampleDockersBuilder(Class runnerClass, String imageName, String filePath) throws Exception {
        super("ExampleDockersBuilder");

        logger.debug(" with docker file ");

        dockerfilePath(filePath); //added

        imageName(imageName);
        //name for searching in logs
        containerName(runnerClass.getSimpleName());
        //temp docker file will be created there
        buildDirectory(SDK_BUILD_DIR_PATH);
        //should be packaged will all dependencies (via 'mvn package -DskipTests=true' command)
        jarFilePath(System.getProperty("sdkJarFilePath"));    // used in pom.xml set in pom.xml   <sdkJarFilePath>
        //will be placed in temp dockerFile
        dockerWorkDir(SDK_WORK_DIR_PATH);                      //
        //will be placed in temp dockerFile
        runnerClass(ComponentStarter.class, runnerClass);
    }

    public ExampleDockersBuilder(Class runnerClass, String imageName) throws Exception {
        super("ExampleDockersBuilder");

        imageName(imageName);
        //name for searching in logs
        containerName(runnerClass.getSimpleName());
        //temp docker file will be created there
        buildDirectory(SDK_BUILD_DIR_PATH);
        //should be packaged will all dependencies (via 'mvn package -DskipTests=true' command)
        jarFilePath(System.getProperty("sdkJarFilePath"));    // used in pom.xml set in pom.xml   <sdkJarFilePath>
        //will be placed in temp dockerFile
        dockerWorkDir(SDK_WORK_DIR_PATH);                      //
        //will be placed in temp dockerFile
        runnerClass(ComponentStarter.class, runnerClass);
    }

}
