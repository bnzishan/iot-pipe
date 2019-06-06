package org.hobbit.sdk.iotpipeline_bm.rabbit;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.hobbit.core.rabbit.RabbitQueueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by bushranazir on 12.04.2019.
 */
public class FanoutExchange {

	RabbitQueueFactory factory;
    public String exchangeName;
	public static String routingKey = "";

	private static final Logger logger = LoggerFactory.getLogger(FanoutExchange.class);

	public void createExchangeAndQueues(RabbitQueueFactory factory, String exchangeName, String[] queueNames) throws IOException {

		this.factory = factory;
		this.exchangeName = exchangeName;
		this.routingKey = routingKey;
		Connection conn = factory.getConnection();

		if(conn != null){
			Channel channel = conn.createChannel();
			channel.exchangeDeclare(exchangeName, ExchangeType.FANOUT.getExchangeName(), true);  	//"fanout-exchange-for-zk-config";

			for(int i=0; i<queueNames.length ; i++){
				String queueName =  queueNames[i];  //containerIds[i];
				logger.debug("=============== queueName " + i + "  "  + queueName);
				channel.queueDeclare(queueName, true, false, false, null);
				channel.queueBind(queueName, exchangeName, routingKey);
			}

			// throws clean close connection error, so removing it
			/*
			try {
				channel.close();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
			conn.close();
			*/
		}

	}

}
