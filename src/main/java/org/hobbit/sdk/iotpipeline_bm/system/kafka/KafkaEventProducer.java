package org.hobbit.sdk.iotpipeline_bm.system.kafka;


import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Created by bushranazir on 24.04.2019.
 */
public class KafkaEventProducer {

	private String fileName;
	private String colHeader;
	private Producer<String, String> producer;

	private static final Logger logger = LoggerFactory.getLogger(KafkaEventProducer.class);
	/**
	 * Initialize Kafka configuration
	 */
	public void initKafkaConfig(String[] kafkaBrokerURIsArr) {

		// Build the configuration required for connecting to Kafka
		Properties props = new Properties();
		// List of Kafka brokers. If there're multiple brokers, they're
		String kafkaBrokersConnect = String.join(",", kafkaBrokerURIsArr);
		props.put("metadata.broker.list", kafkaBrokersConnect);
		// Serializer used for sending data to kafka. Since we are sending
		// string, we are using StringEncoder.
		props.put("serializer.class", "kafka.serializer.StringEncoder");

		// We want acks from Kafka that messages are properly received.
		props.put("request.required.acks", "1");

		//props.put("producer.type", "async");


		// Create the producer instance
		ProducerConfig config = new ProducerConfig(props);

		producer = new Producer<String, String>(config);
	}

	/**
	 * Initialize configuration for file to be read (csv)
	 *
	 * @param fileNameWithRelPath
	 * @throws IOException
	 */
	public void initFileConfig(String fileNameWithRelPath) throws IOException, Exception {
		this.fileName = fileNameWithRelPath;

		try {
		//	InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(this.fileName);
		//	InputStream inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(this.fileName);


			Path filePath = Paths.get( fileName);
			InputStream inStream = Files.newInputStream(filePath);
			int len = (int) Files.size(filePath);
			logger.info("filename--------  " + fileName + "   file size--------" + len);  		///   datasets/House1.csv>

			Reader reader = new InputStreamReader(inStream);
			BufferedReader buffReader = IOUtils.toBufferedReader(reader);

			// Get the header line to initialize CSV parser
			colHeader = buffReader.readLine();
			System.out.println("File header :: " + colHeader);

			if (StringUtils.isEmpty(colHeader)) {
				throw new Exception("Column header is null, something is wrong");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

	/**
	 * Send csv file data to the named topic on Kafka broker
	 *
	 * @param topic
	 * @throws IOException
	 */
	public void sendFileDataToKafka(String topic) throws IOException {

		Iterable<CSVRecord> csvRecords = null;

		// Parse the CSV file, using the column header
		try {
			// InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(fileName);

			Path filePath = Paths.get( fileName);
			InputStream inStream = Files.newInputStream(filePath);
			int len = (int) Files.size(filePath);

			Reader reader = new InputStreamReader(inStream);

			String[] colNames = StringUtils.split(colHeader, ',');
			csvRecords = CSVFormat.DEFAULT.withHeader(colNames).parse(reader);
		} catch (IOException e) {
			System.out.println(e);
			throw e;
		}

		// We iterate over the records and send each over to Kafka broker
		// Get the next record from input file
		CSVRecord csvRecord = null;
		Iterator<CSVRecord> csvRecordItr = csvRecords.iterator();
		boolean firstRecDone = false;
		while (csvRecordItr.hasNext()) {
			try {
				csvRecord = csvRecordItr.next();
				if (!firstRecDone) {
					firstRecDone = true;
					continue;
				}
				// Get a map of column name and value for a record
				Map<String, String> keyValueRecord = csvRecord.toMap();

				// Create the message to be sent
				String message = "";
				int size = keyValueRecord.size();
				int count = 0;
				for (String key : keyValueRecord.keySet()) {
					count++;
					message = message
							+ StringUtils.replace(key, "\"", "")
							+ "="
							+ StringUtils.replace(keyValueRecord.get(key),
							"\"", "");
					if (count != size) {
						message = message + ",";
					}
				}

				// Send the message
				System.out.println(message);
				KeyedMessage<String, String> data = new KeyedMessage<String, String>(
						topic, message);
				producer.send(data);

			} catch (NoSuchElementException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * Cleanup stuff
	 */
	public void cleanup() {
		producer.close();
	}
}
