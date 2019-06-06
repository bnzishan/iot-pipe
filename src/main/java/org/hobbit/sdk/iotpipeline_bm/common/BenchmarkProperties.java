package org.hobbit.sdk.iotpipeline_bm.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by bushranazir on 07.05.2019.
 */
public class BenchmarkProperties {


	private BenchmarkProperties() {
		 properties = new Properties();
	}

	Properties properties;


	public static final String DEFAULT_RECORD_COUNT = "0";

	/**
	 * The target number of operations to perform.
	 */
	public static final String OPERATION_COUNT_PROPERTY = "operationcount";

	/**
	 * The number of records to load into the database initially.
	 */
	public static final String RECORD_COUNT_PROPERTY = "recordcount";

	/**
	 * The workload class to be loaded.
	 */
	public static final String WORKLOAD_PROPERTY = "workload";

	/**
	 * The database class to be used.
	 */
	public static final String DB_PROPERTY = "db";

	/**
	 * The exporter class to be used. The default is
	 * com.yahoo.ycsb.measurements.exporter.TextMeasurementsExporter.
	 */
	public static final String EXPORTER_PROPERTY = "exporter";

	/**
	 * If set to the path of a file, YCSB will write all output to this file
	 * instead of STDOUT.
	 */
	public static final String EXPORT_FILE_PROPERTY = "exportfile";

	/**
	 * The number of YCSB client threads to run.
	 */
	public static final String THREAD_COUNT_PROPERTY = "threadcount";

	/**
	 * Indicates how many inserts to do if less than recordcount.
	 * Useful for partitioning the load among multiple servers if the client is the bottleneck.
	 * Additionally workloads should support the "insertstart" property which tells them which record to start at.
	 */
	public static final String INSERT_COUNT_PROPERTY = "insertcount";

	/**
	 * Target number of operations per second.
	 */
	public static final String TARGET_PROPERTY = "target";

	/**
	 * The maximum amount of time (in seconds) for which the benchmark will be run.
	 */
	public static final String MAX_EXECUTION_TIME = "maxexecutiontime";

	/**
	 * Whether or not this is the transaction phase (run) or not (load).
	 */
	public static final String DO_TRANSACTIONS_PROPERTY = "dotransactions";

	/**
	 * Whether or not to show status during run.
	 */
	public static final String STATUS_PROPERTY = "status";

	/**
	 * Use label for status (e.g. to label one experiment out of a whole batch).
	 */
	public static final String LABEL_PROPERTY = "label";


	public static final String propFilePath = "propfilepath";


	/**
	 * An optional thread used to track progress and measure JVM stats.
	 */
	//private static StatusThread statusthread = null;

	// HTrace integration related constants.

	/**
	 * All keys for configuring the tracing system start with this prefix.
	 */
	private static final String HTRACE_KEY_PREFIX = "htrace.";
	private static final String CLIENT_WORKLOAD_INIT_SPAN = "Client#workload_init";
	private static final String CLIENT_INIT_SPAN = "Client#init";
	private static final String CLIENT_WORKLOAD_SPAN = "Client#workload";
	private static final String CLIENT_CLEANUP_SPAN = "Client#cleanup";
	private static final String CLIENT_EXPORT_MEASUREMENTS_SPAN = "Client#export_measurements";

	public void setProps(Properties props) {
		this.properties = props;
	}

	public void setThreadCountProperty(int i) {
		properties.setProperty(THREAD_COUNT_PROPERTY, String.valueOf(i)); // number of YCSB client threads. Alternatively this may be specified on the command line. (default: 1)
	}


	public void setTaregtProperty(String str) {
		properties.setProperty(TARGET_PROPERTY, str);
	}

	/*
	public void DO_TRANSACTIONS_PROPERTY() {
		properties.setProperty(TARGET_PROPERTY, String.valueOf(true));
	}


	public void setStatusProperty() {
		properties.setProperty(STATUS_PROPERTY, String.valueOf(true));
	}
	 */

	public void setDbProperty(String str) {
		properties.setProperty(DB_PROPERTY, str); // database class to use  (default: com.yahoo.ycsb.BasicDB)
	}

	public void setWorkloadProperty(String str) {
		properties.setProperty(WORKLOAD_PROPERTY, str); // workload class to use  (e.g. com.yahoo.ycsb.workloads.CoreWorkload)
	}

	public void serLabeleProperty(String str) {
		properties.setProperty(LABEL_PROPERTY, str);
	}


	public void setExportFileProperty(String str) {
		properties.setProperty(EXPORT_FILE_PROPERTY, str); // path to a file where output should be written instead of to stdout (default: undefined/write to stdout)
	}

	public void setExporterProperty(String str) {
		properties.setProperty(EXPORTER_PROPERTY, str); // measurements exporter class to use (default: com.yahoo.ycsb.measurements.exporter.TextMeasurementsExporter)
	}


	public void setPropFilePath(String propFilePath) {
		Properties myfileprops = new Properties();
		try {
			myfileprops.load(new FileInputStream(propFilePath));
		} catch (IOException e) {
			System.out.println("Unable to open the properties file " + propFilePath);
			System.out.println(e.getMessage());
			System.exit(0);
		}
		for (Enumeration e = properties.propertyNames(); e.hasMoreElements(); ) {
			String prop = (String) e.nextElement();
			 myfileprops.setProperty(prop, properties.getProperty(prop));
		}
		  properties = myfileprops;
	}
}