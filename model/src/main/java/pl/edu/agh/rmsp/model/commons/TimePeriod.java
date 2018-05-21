package pl.edu.agh.rmsp.model.commons;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

public class TimePeriod {

	private TimeUnit timeUnit;
	private int nrOfTimeUnits;

	public TimePeriod(TimeUnit timeUnit, int n) {
		this.timeUnit = timeUnit;
		this.nrOfTimeUnits = n;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public int getNrOfTimeUnits() {
		return nrOfTimeUnits;
	}
	
	public void setNrOfTimeUnits(int n) {
		this.nrOfTimeUnits = n;
	}
	
	public Date getPreviousDate(Date date, int offset) {
		return addToDate(date, -(nrOfTimeUnits+offset));
	}
	
	public Date getNextDate(Date date) {
		return addToDate(date, nrOfTimeUnits);
	}
	
	public Date getNextDate(Date date, int value) {
		return addToDate(date, value);
	}
	
	private Date addToDate(Date date, int value) {
		DateTime dateTime = new DateTime(date);
		switch (timeUnit) {
		case SECONDS:
			dateTime = dateTime.plusSeconds(value);
			break;
		case MINUTES:
			dateTime = dateTime.plusMinutes(value);
			break;
		case HOURS:
			dateTime = dateTime.plusHours(value);
			break;
		case DAYS:
			dateTime = dateTime.plusDays(value);
			break;		
		default:
			dateTime = dateTime.plusMinutes(value);
			break;
		}
		return dateTime.toDate();
	}

}
