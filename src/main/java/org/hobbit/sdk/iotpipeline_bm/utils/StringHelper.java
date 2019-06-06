package org.hobbit.sdk.iotpipeline_bm.utils;

/**
 * Created by bushranazir on 21.11.2018.
 */

import org.apache.commons.lang.StringUtils;

//import scala.Tuple2;

public class StringHelper {

	public static boolean isNumeric(String str)
	{
		return str.matches("-?\\d+(.\\d+)?");
	}

	public static void mainTest(String[]args){
		//essex: 19419|2011-04-03 04:00:00|1.21|1.6
		//dataport: 871|2013-12-02 12:00:00-06|0.22178333333333333333
		//water: 1|2012-01-01 12:00:00|
		//water String line ="1|2012-01-01 19:00:00|0.4";
		String s= "19419\t2011-04-03 00:00:00\t2.34\t2011-04-03 01:00:00\tSun\t0\t0\t3.5";
		String[] fields = s.split("\t");
		if (fields.length>2) {
			String meterID = fields[0];
			System.out.println(StringUtils.isNotEmpty(fields[2]));
			System.out.println(StringHelper.isNumeric("0.2332"));
			if (StringUtils.isNotEmpty(fields[2]) && StringUtils.isNumeric(fields[2])) {
				Double reading = Double.parseDouble(fields[2]);
				String[] arr = fields[1].replace("-", "").split(" ");
				String ID = meterID + arr[0].substring(2); //1000 120101
				Integer hour = Integer.parseInt(arr[1].split(":")[0]);
				System.out.println(ID + ","+ hour + "," + reading);
				//list.add(new Tuple2<String, Tuple2<Integer, Double>>(ID, new Tuple2<Integer, Double>(hour, reading)));
			}
		}
	}

	public static int[] toIntArray(String[] arr) {
		int[] ints = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			ints[i] = Integer.parseInt(arr[i]);
		}
		return ints;
	}

	public static int[] parseLineToIntArray(String line) {
		return toIntArray(line.split(","));
	}


}