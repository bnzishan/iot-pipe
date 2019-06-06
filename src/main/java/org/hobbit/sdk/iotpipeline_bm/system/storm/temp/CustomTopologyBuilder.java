package org.hobbit.sdk.iotpipeline_bm.system.storm.temp;

/**
 * Created by bushranazir on 25.03.2019.
 */

/*
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.storm.hbase.bolt.mapper.HBaseProjectionCriteria;
import org.apache.storm.hbase.bolt.mapper.HBaseValueMapper;
import org.apache.storm.hbase.topology.WordCountValueMapper;
import org.apache.storm.hbase.trident.mapper.SimpleTridentHBaseMapper;
import org.apache.storm.hbase.trident.mapper.TridentHBaseMapper;
import org.apache.storm.hbase.trident.state.HBaseQuery;
import org.apache.storm.hbase.trident.state.HBaseState;
import org.apache.storm.hbase.trident.state.HBaseStateFactory;
import org.apache.storm.hbase.trident.state.HBaseUpdater;
import storm.trident.Stream;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.state.StateFactory;
import storm.trident.testing.FixedBatchSpout;
public class CustomTopologyBuilder {


	public static StormTopology buildTopology(String hbaseRoot) {

		Fields fields = new Fields("word", "count");
		FixedBatchSpout spout = new FixedBatchSpout(fields, 4,
				new Values("storm", 1),
				new Values("trident", 1),
				new Values("needs", 1),
				new Values("javadoc", 1)
		);
		spout.setCycle(true);

		TridentHBaseMapper tridentHBaseMapper = new SimpleTridentHBaseMapper()
				.withColumnFamily("cf")
				.withColumnFields(new Fields("word"))
				.withCounterFields(new Fields("count"))
				.withRowKeyField("word");

		HBaseValueMapper rowToStormValueMapper = new WordCountValueMapper();

		HBaseProjectionCriteria projectionCriteria = new HBaseProjectionCriteria();
		projectionCriteria.addColumn(new HBaseProjectionCriteria.ColumnMetaData("cf", "count"));

		HBaseState.Options options = new HBaseState.Options()
				.withConfigKey(hbaseRoot)
				.withDurability(Durability.SYNC_WAL)
				.withMapper(tridentHBaseMapper)
				.withProjectionCriteria(projectionCriteria)
				.withRowToStormValueMapper(rowToStormValueMapper)
				.withTableName("WordCount");

		StateFactory factory = new HBaseStateFactory(options);

		TridentTopology topology = new TridentTopology();
		Stream stream = topology.newStream("spout1", spout);

		stream.partitionPersist(factory, fields, new HBaseUpdater(), new Fields());

		TridentState state = topology.newStaticState(factory);
		stream = stream.stateQuery(state, new Fields("word"), new HBaseQuery(), new Fields("columnName", "columnValue"));
		stream.each(new Fields("word", "columnValue"), new PrintFunction(), new Fields());
		return topology.build();
	}

}
*/