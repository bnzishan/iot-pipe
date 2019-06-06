package org.hobbit.sdk.iotpipeline_bm.docker.builder;


import org.hobbit.sdk.Constants;
import org.hobbit.sdk.docker.builders.AbstractDockersBuilder;
import org.hobbit.sdk.docker.builders.BothTypesDockersBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bushranazir
 */
public class CarbonGraphiteDockerBuilder extends  BothTypesDockersBuilder {


	private static final Logger logger = LoggerFactory.getLogger(CarbonGraphiteDockerBuilder.class);


	private static final String name = "carbon-graphite";

	public CarbonGraphiteDockerBuilder(AbstractDockersBuilder builder) {
		super(builder);

		logger.debug("carbon-graphite builder name    "+ name);
	}


	public void addEnvVars(AbstractDockersBuilder ret) {
		ret.addEnvironmentVariable("HOBBIT_RABBIT_HOST", (String)System.getenv().get("HOBBIT_RABBIT_HOST"));
		ret.addEnvironmentVariable("HOBBIT_SESSION_ID", (String)System.getenv().get("HOBBIT_SESSION_ID"));
		ret.addNetworks(Constants.HOBBIT_NETWORKS);
		ret.addEnvironmentVariable("SYSTEM_PARAMETERS_MODEL", (String)System.getenv().get("SYSTEM_PARAMETERS_MODEL"));

		// creating env variables needed for storm cluster

	}


	public String getName() {
		return "carbon-graphite";
	}
}
