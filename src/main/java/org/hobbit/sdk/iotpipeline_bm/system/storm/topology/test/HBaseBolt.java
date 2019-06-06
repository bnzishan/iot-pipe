package org.hobbit.sdk.iotpipeline_bm.system.storm.topology.test;

/**
 * Created by bushranazir on 10.05.2019.
 *
 *
public class HBaseBolt extends BaseBasicBolt{

	private HBaseClient hbaseClient;
	// private DB stormMeetupDB;
	private HBase table;


	@Override
	public void prepare(Map stormConf, TopologyContext context) {

		try {
			hbaseClient = new MongoClient("localhost", 27017);
			stormMeetupDB = mongoClient.getDB("storm-meetup");
			consCompStatsColl = stormMeetupDB.getCollection("conscompstats");
		} catch (UnknownHostException e) {
			System.out
					.println("###############$$$$ -- Didn't get the connection to MongoDB");
		}

	}


	public void execute(Tuple input, BasicOutputCollector collector) {
	String product = input.getStringByField("product");
	String subproduct = input.getStringByField("subproduct");
	Integer count = input.getIntegerByField("count");
	Double mean = input.getDoubleByField("mean");
	Double stddev = input.getDoubleByField("stddev");
	Double skewness = input.getDoubleByField("skewness");
	Double kurtosis = input.getDoubleByField("kurtosis");

	if (consCompStatsColl != null) {
	// Create the new object to be inserted
	BasicDBObject basicDBObject = new BasicDBObject("product", product)
	.append("subproduct", subproduct)
	.append("count", count)
	.append("rollingstats",
	new BasicDBObject("mean", mean)
	.append("stddev", stddev)
	.append("skewness", skewness)
	.append("kurtosis", kurtosis))
	.append("timestamp", new Date());
	// Add the object to MongoDB collection
	WriteResult writeResult = consCompStatsColl.insert(basicDBObject,
	WriteConcern.UNACKNOWLEDGED);
	// TODO - Replace by log4j
	System.out
	.println("########################$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"
	+ writeResult.getN());
	}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	// TODO Auto-generated method stub

	}
}

 */