package org.hobbit.sdk.iotpipeline_bm.rabbit;

/**
 * Created by bushranazir on 12.04.2019.
 */
public enum ExchangeType {
	DIRECT("direct"),
	TOPIC("topic"),
	FANOUT("fanout"),
	HEADER("headers");

	private final String exchangeName;

	ExchangeType(String exchangeName) {
		this.exchangeName = exchangeName;
	}

	public String getExchangeName() {
		return this.exchangeName;
	}
}
