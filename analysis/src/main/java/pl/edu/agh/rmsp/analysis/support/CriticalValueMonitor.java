package pl.edu.agh.rmsp.analysis.support;

import java.util.Date;

public class CriticalValueMonitor {

	private double min;
	private double max;
	private double[] set;
	
	public CriticalValueMonitor(double min, double max, double[] set)
	{
		this.setMin(min);
		this.setMax(max);
		this.setSet(set);		
	}

	public double[] getSet() {
		return set;
	}

	public void setSet(double[] set) {
		this.set = set;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}
}
