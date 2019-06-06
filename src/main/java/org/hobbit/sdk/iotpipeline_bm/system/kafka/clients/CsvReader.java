package org.hobbit.sdk.iotpipeline_bm.system.kafka.clients;

/**
 * Created by bushranazir on 23.04.2019.

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

 */

import org.hobbit.sdk.iotpipeline_bm.system.kafka.models.SensorData;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;


public class CsvReader {
	private final Pattern pattern;

	public CsvReader(final String separator) {
		this.pattern = Pattern.compile(separator);
	}


	public List<SensorData> loadCsvContentToList(
			final BufferedReader bufferedReader) throws IOException
	{
		/*
		try {
			return bufferedReader.lines().skip(1).map(line -> {
				final String[] lineArray = pattern.split(line);
				return new SensorData
						.Builder()
						.setTimeStamp(Long.parseLong(lineArray[0]))
						.setAggregate(Long.parseLong(lineArray[1]))
						.setFridge(lineArray[2])
						.getFreezer(lineArray[3])
						.setWasherDryer(lineArray[4])
						.setWashingMachine(lineArray[5])
						.setToaster(lineArray[6])
						.setComputer(lineArray[7])
						.setTelevisionSite(lineArray[8])
						.setMicrowave(lineArray[9])
						.setKettle(lineArray[10])
						.build();
			}).collect(Collectors.toList());
		} finally {
			bufferedReader.close();
		}
		*/
		return null;
	}

}