package pl.edu.agh.rmsp.model.commons;

import java.util.Date;

public class Measurement {
	
	private Date date;
	private double value;
	
	public Measurement(Date date, double value) {
		this.setDate(date);
		this.setValue(value);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	
}
