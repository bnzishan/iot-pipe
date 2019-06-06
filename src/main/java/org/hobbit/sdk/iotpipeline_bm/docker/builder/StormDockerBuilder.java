package org.hobbit.sdk.iotpipeline_bm.docker.builder;


import org.hobbit.sdk.Constants;
import org.hobbit.sdk.docker.builders.AbstractDockersBuilder;
import org.hobbit.sdk.docker.builders.BothTypesDockersBuilder;

/**
 * Created by bushranazir on 25.03.2019.
 */
public class StormDockerBuilder extends BothTypesDockersBuilder {

	private static final String name = "storm";

	public StormDockerBuilder(AbstractDockersBuilder builder) {
		super(builder);
	}

	@Override
	public void addEnvVars(AbstractDockersBuilder ret) {
		ret.addEnvironmentVariable("HOBBIT_RABBIT_HOST", (String)System.getenv().get("HOBBIT_RABBIT_HOST"));
		ret.addEnvironmentVariable("HOBBIT_SESSION_ID", (String)System.getenv().get("HOBBIT_SESSION_ID"));
		ret.addNetworks(Constants.HOBBIT_NETWORKS);

		// creating env variables needed for storm cluster

	}

	@Override
	public String getName() {
		return null;
	}
}
