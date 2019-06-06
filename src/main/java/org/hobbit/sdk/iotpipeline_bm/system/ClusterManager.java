package org.hobbit.sdk.iotpipeline_bm.system;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bushranazir on 07.03.2019.
 */
public interface ClusterManager {


	Map<String, String> params = new HashMap<>();


	public void createCluster(Map<String, String> params);
}
