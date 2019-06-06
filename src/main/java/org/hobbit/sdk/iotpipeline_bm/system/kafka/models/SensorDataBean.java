package org.hobbit.sdk.iotpipeline_bm.system.kafka.models;

/**
 * Created by bushranazir on 23.04.2019.
 */

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;

public class SensorDataBean implements Serializable{

	//private static final long serialVersionUID = 1L;


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
		final SensorDataBean other = (SensorDataBean) obj;
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



	public long getTimeStamp() {
		return timeStamp;
	}

	public long getAggregate() {
		return aggregate;
	}

	public int getFridge() {
		return fridge;
	}

	public int getFreezer() {
		return freezer;
	}

	public int getWasherDryer() {
		return washerDryer;
	}

	public int getWashingMachine() {
		return washingMachine;
	}

	public int getToaster() {
		return toaster;
	}

	public int getComputer() {
		return computer;
	}

	public int getTelevisionSite() {
		return televisionSite;
	}

	public int getMicrowave() {
		return microwave;
	}

	public int getKettle() {
		return kettle;
	}

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
}

