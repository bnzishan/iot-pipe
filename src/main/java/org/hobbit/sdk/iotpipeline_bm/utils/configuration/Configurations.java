package org.hobbit.sdk.iotpipeline_bm.utils.configuration;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * A holder for all the benchmark configuration parameters.
 *
 * A client is expected to instantiate this class, which will provide values
 * (defaults or blank) for all configuration parameters, and then to save this
 * to a file (to create a template configuration file) or to load it from a file
 * (which is the usual case).
 *
 */

public class Configurations {
		public static final String DATASETS_PATH = "datasetsPath";
		public static final String TASKS_PATH = "tasksPath";
		public static final String TASKS_CONFIG_DIR = "tasksConfigDir";
		public static final String DATA_FORMAT = "dataFormat";
		public static final String DATE_FORMAT = "dateFormat";
		public static final String DATABASE = "database";
		private final Properties properties = new Properties();

/**
 * Initialise and set default values for parameters that make sense.
 */
public Configurations() {
		properties.setProperty(DATASETS_PATH, "./datasets");
		properties.setProperty(TASKS_PATH, "./tasks");
		properties.setProperty(TASKS_CONFIG_DIR, "tasks/bs4dsps/config");
		properties.setProperty(DATA_FORMAT, "csv");
		properties.setProperty(DATE_FORMAT, "yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
		properties.setProperty(DATABASE, "hbase");
}
/**
 * Load the configuration from the given file (java configuration format).
 *
 * @param filename A readable file on the file system.
 * @throws IOException
 */
public void loadFromFile(String filename) throws IOException {

		InputStream input = new FileInputStream(filename);
		try {
		properties.load(input);
		} finally {
		input.close();
		}
		}

/**
 * Save the configuration to a text file (java configuration format).
 *
 * @param filename
 * @throws IOException
 */
public void saveToFile(String filename) throws IOException {
		OutputStream output = new FileOutputStream(filename);
		try {
		properties.store(output, "");
		} finally {
		output.close();
		}
		}

/**
 * Read a configuration parameter's value as a string
 *
 * @param key
 * @return
 */
public String getString(String key) {
		String value = properties.getProperty(key);

		if (value == null) {
		throw new IllegalStateException("Missing configuration parameter: " + key);
		}
		return value;
		}

/**
 * Read a configuration parameter's value as a boolean
 *
 * @param key
 * @return
 */
public boolean getBoolean(String key) {
		String value = getString(key);

		if (value.equalsIgnoreCase("true") || value.equals("1") || value.equalsIgnoreCase("y")) {
		return true;
		}
		if (value.equalsIgnoreCase("false") || value.equals("0") || value.equalsIgnoreCase("n")) {
		return false;
		}
		throw new IllegalStateException("Illegal value for boolean configuration parameter: " + key);
		}

/**
 * Read a configuration parameter's value as an int
 *
 * @param key
 * @return
 */
public int getInt(String key) {
		String value = getString(key);

		try {
		return Integer.parseInt(value);
		} catch (NumberFormatException e) {
		throw new IllegalStateException("Illegal value for integer configuration parameter: " + key);
		}
		}

/**
 * Read a configuration parameter's value as an double
 *
 * @param key
 * @return
 */
public double getDouble(String key) {
		String value = getString(key);

		try {
		return Double.parseDouble(value);
		} catch (NumberFormatException e) {
		throw new IllegalStateException("Illegal value for double configuration parameter: " + key);
		}
		}

/**
 * Read a configuration parameter's value as a long
 *
 * @param key
 * @return
 */
public long getLong(String key) {
		String value = getString(key);

		try {
		return Long.parseLong(value);
		} catch (NumberFormatException e) {
		throw new IllegalStateException("Illegal value for long integer configuration parameter: " + key);
		}
		}

/**
 * Read a configuration parameter's value as a array
 *
 * @param key
 * @return
 */
public Collection<? extends String> getArray(String key) {
		String value = properties.getProperty(key);
		ArrayList<String> array = new ArrayList<String>();
		value = value.replace(" ", "");
		if (value.equals("")) {
		//System.out.println("We will transform all classes. ");
		} else {
		//System.out.println("We will transform those classes: " +value);
		Collections.addAll(array, value.split(","));
		}
		return array;
		}

public Properties getProperties() {
		return this.properties;
		}

public void setIntProperty(String key, int value) {
		properties.setProperty(key, Integer.toString(value));
		}

public void setStringProperty(String key, String value) {
		properties.setProperty(key, value);
		}

}