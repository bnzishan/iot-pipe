package org.hobbit.sdk.iotpipeline_bm.system.kafka.models;

/**
 * Created by bushranazir on 23.04.2019.
 */

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;

public class SensorData implements Serializable{

	private static final long serialVersionUID = 1L;


	private final long timeStamp;
	private final long  aggregate;
	private final int fridge;
	private final int freezer;
	private final int washerDryer;
	private final int washingMachine;
	private final int toaster;
	private final int computer;
	private final int televisionSite;
	private final int microwave;
	private final int kettle;


	private SensorData(final Builder builder) {
		this.timeStamp = builder.timeStamp;
		this.aggregate = builder.aggregate;
		this.fridge = builder.fridge;
		this.freezer = builder.freezer;
		this.washerDryer = builder.washerDryer;
		this.washingMachine = builder.washingMachine;
		this.toaster = builder.toaster;
		this.computer = builder.computer;
		this.televisionSite = builder.televisionSite;
		this.microwave = builder.microwave;
		this.kettle = builder.kettle;
	}



	@Override
	public int hashCode() {
		return Objects.hash(timeStamp, aggregate, fridge, freezer,washerDryer, washingMachine,toaster,computer, televisionSite,microwave, kettle  );
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final SensorData other = (SensorData) obj;
		return Objects.equals(this.timeStamp, other.timeStamp)
				&& Objects.equals(this.aggregate, other.aggregate)
				&& Objects.equals(this.fridge, other.fridge)
				&& Objects.equals(this.freezer, other.freezer)
				&& Objects.equals(this.washerDryer, other.washerDryer)
				&& Objects.equals(this.washingMachine, other.washingMachine)
				&& Objects.equals(this.toaster, other.toaster)
				&& Objects.equals(this.computer, other.computer)
				&& Objects.equals(this.televisionSite, other.televisionSite)
				&& Objects.equals(this.microwave, other.microwave)
				&& Objects.equals(this.kettle, other.kettle);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("timeStamp", timeStamp)
				.add("aggregate", aggregate)
				.add("fridge", fridge)
				.add("freezer", freezer)
				.add("washerDryer", washerDryer)
				.add("washingMachine", washingMachine)
				.add("toaster", toaster)
				.add("computer", computer)
				.add("televisionSite", televisionSite)
				.add("microwave", microwave)
				.add("kettle", kettle)
				.toString();
	}

	public static class Builder{
		private  long timeStamp;
		private  long  aggregate;
		private  int fridge;
		private  int freezer;
		private  int washerDryer;
		private  int washingMachine;
		private  int toaster;
		private  int computer;
		private  int televisionSite;
		private  int microwave;
		private  int kettle;

		public void setTimeStamp(long timeStamp) {
			this.timeStamp = timeStamp;
		}

		public void setAggregate(long aggregate) {
			this.aggregate = aggregate;
		}

		public void setFridge(int fridge) {
			this.fridge = fridge;
		}

		public void setFreezer(int freezer) {
			this.freezer = freezer;
		}

		public void setWasherDryer(int washerDryer) {
			this.washerDryer = washerDryer;
		}

		public void setWashingMachine(int washingMachine) {
			this.washingMachine = washingMachine;
		}

		public void setToaster(int toaster) {
			this.toaster = toaster;
		}

		public void setComputer(int computer) {
			this.computer = computer;
		}

		public void setTelevisionSite(int televisionSite) {
			this.televisionSite = televisionSite;
		}

		public void setMicrowave(int microwave) {
			this.microwave = microwave;
		}

		public void setKettle(int kettle) {
			this.kettle = kettle;
		}

		public long getTimeStamp(final long timeStamp) {
			this.timeStamp = timeStamp;
			return timeStamp;
		}

		public long getAggregate(final long aggregate) {
			this.aggregate = aggregate;
			return aggregate;
		}

		public int getFridge(final int fridge) {
			this.fridge = fridge;
			return fridge;
		}
		public int getFreezer(final int freezer) {
			this.freezer = freezer;
			return freezer;
		}

		public int getWasherDryer(final int washerDryer) {
			this.washerDryer = washerDryer;
			return washerDryer;
		}

		public int getWashingMachine(final int washingMachine) {
			this.washingMachine = washingMachine;
			return washingMachine;
		}


		public int getToaster(final int toaster) {
			this.toaster = toaster;
			return toaster;
		}


		public int getComputer(final int computer) {
			this.computer = computer;
			return computer;
		}


		public int getTelevisionSite(final int televisionSite) {
			this.televisionSite = televisionSite;
			return televisionSite;
		}


		public int getMicrowave(final int microwave) {
			this.microwave = microwave;
			return microwave;
		}


		public int getwashingKettle(final int kettle) {
			this.kettle = kettle;
			return kettle;
		}

	}


}

