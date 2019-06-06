package org.hobbit.sdk.iotpipeline_bm.utils;

import org.hobbit.core.Constants;

/**
 * Created by bushranazir on 08.02.2019.
 */
public class CommonUtils {
	private CommonUtils(){
	}

	public static String toPlatformQueueName(String queueName) {
		return queueName + "." + System.getenv().get(Constants.HOBBIT_SESSION_ID_KEY);
	}
}

