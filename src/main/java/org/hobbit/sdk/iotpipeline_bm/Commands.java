//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


/*
101 -122 free
124 - 255 free

 */


package org.hobbit.sdk.iotpipeline_bm;

public final class Commands {

	public static final byte CUSTOM_COMPONENT_READY=(byte) 255;

	//SYSTEM ADAPTER
	public static final byte  SA2ZK_CONFIG_SENT= (byte) 101;
	public static final byte  SA2ZK_HEALTHCHECK_REQUEST= (byte)102;

	public static final byte FINISH_ZK_SIGNAL=(byte) 127;
	public static final byte START_ZK_SIGNAL=(byte) 128;

	//ZOOKEEPER
	public static final byte  ZOOKEEPER_INIT_SIGNAL = (byte) 124;
	public static final byte  ZKMANAGER_READY_SIGNAL = (byte) 125;
	public static final byte ZOOKEEPER_START_SIGNAL=(byte) 126;
	public static final byte  ZOOKEEPER_CONNECT_SIGNAL = (byte) 126;


	//KAFKA
	public static final byte  KAFKA_READY_SIGNAL = (byte) 130;
	public static final byte  START_KAFKA_SIGNAL = (byte) 131;

	//STORM
	public static final byte  STORM_NIMBUS_READY_SIGNAL = (byte) 140;
	public static final byte  START_STORM_NIMBUS_SIGNAL = (byte) 141;
	public static final byte  STORM_SUPERVISOR_READY_SIGNAL = (byte) 142;
	public static final byte  START_STORM_SUPERVISOR_SIGNAL = (byte) 143;
	public static final byte  START_STORM_UI_SIGNAL = (byte) 143;

	//GRAPHITE-CARBON
	public static final byte  CARBON_GRAPHITE_READY_SIGNAL = (byte) 145;
	public static final byte  START_CARBON_GRAPHITE_SIGNAL = (byte) 146;






	/**
	 * The signal sent by the system adapter to indicate that it
	 * has started with to send data to data ingestion system eg kafka via kafka producer
	 */
	public static final byte  FORWARDING_DATA_TO_PIPELINE_START = (byte) 150;

	/**
	 * The signal sent by the system adapter to indicate that it
	 * has finished with data sending to eg kafka
	 */
	public static final byte  FORWARDING_DATA_TO_PIPELINE_FINISHED = (byte) 151;

	/**
	 * The signal sent to indicate that all
	 * data has successfully generated n sent by the data generators. ack by BC
	 */
	public static final byte  DATA_GEN_FINISHED_FROM_DATAGEN = (byte) 152;


	public static final byte  DATA_GEN_FINISHED= (byte) 153;  // sent by BC, ack by SA


	// for storm
	public static final byte TOPOLOGY_SENT = (byte) 160;


	private Commands() {
	}


}
