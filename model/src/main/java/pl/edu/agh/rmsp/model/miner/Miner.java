package pl.edu.agh.rmsp.model.miner;

import java.util.Date;
import java.util.Map;

import pl.edu.agh.rmsp.model.commons.TimePeriod;
import pl.edu.agh.rmsp.model.commons.Value;


public abstract class Miner {

	private String resource; // resource we are targeting
	private TimePeriod timePeriod; // Contains TimeUnit + nr of observed values

	public Miner(String resource, TimePeriod unit) {
		super();
		this.timePeriod = unit;
		this.resource = resource;
	}
	
	public int getNrOfObservedValues() {
		return this.timePeriod.getNrOfTimeUnits();
	}
	
	public void setNrOfObservedValues(int n) {
		this.timePeriod.setNrOfTimeUnits(n);
	}

	public TimePeriod getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(TimePeriod timeUnit) {
		this.timePeriod = timeUnit;
	}
	
	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public abstract Map<Date, Value> putValuesIntoDates(Value[] rawValues);

	public abstract double[] getValues();

}
