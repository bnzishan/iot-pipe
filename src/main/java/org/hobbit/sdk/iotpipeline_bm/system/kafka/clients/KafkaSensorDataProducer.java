package org.hobbit.sdk.iotpipeline_bm.system.kafka.clients;

/**
 * Created by bushranazir on 23.04.2019.
 */
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public final class KafkaSensorDataProducer {

	final Producer<String, String> producer;
	private final String topicName;

	/// {todo} class immutability comprised due to unit temp
	/// {todo} need to set producer as mock producer for unit temp
	/// {todo} added new constructor that takes producer as input
    /*public KafkaInsuranceProducer(final Properties kafkaProperties, final String topicName) {
        producer = new KafkaProducer<>(kafkaProperties);
        this.topicName =topicName;
    }*/

	public KafkaSensorDataProducer(final Producer<String, String> producer,
								  final String topicName) {
		this.producer = producer;
		this.topicName =topicName;
	}

	public void close() {
		this.producer.close();
	}

	public void send(final String message) {
		producer.send(new ProducerRecord<>(this.topicName, message));
		this.producer.flush();
	}
}
