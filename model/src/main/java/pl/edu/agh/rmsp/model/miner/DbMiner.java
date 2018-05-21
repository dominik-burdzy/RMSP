package pl.edu.agh.rmsp.model.miner;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import pl.edu.agh.rmsp.model.commons.Measurement;
import pl.edu.agh.rmsp.model.commons.TimePeriod;
import pl.edu.agh.rmsp.model.commons.Value;
import pl.edu.agh.rmsp.model.db.DatabaseManager;


public class DbMiner extends Miner {

	private DatabaseManager manager;

	public DbMiner(String resource, TimePeriod timePeriod) {
		super(resource, timePeriod);

		this.manager = new DatabaseManager();
	}

	@Override
	public Map<Date, Value> putValuesIntoDates(Value[] rawValues) {
		Map<Date, Value> result = new LinkedHashMap<Date, Value>();
		Date now = new Date();
		for (int i = 0; i < rawValues.length; i++) {
			Date calculatedDate = super.getTimePeriod().getNextDate(now, i);
			result.put(calculatedDate, rawValues[i]);
		}
		return result;
	}

	public double[] getValuesUpTo(int nrOfValues, int indexOfLastValueToGet) {
		Date now = new Date();
		Date endDate = getTimePeriod().getPreviousDate(now, indexOfLastValueToGet + 1);
		Date startDate = getTimePeriod().getPreviousDate(now, nrOfValues + indexOfLastValueToGet + 1);
		return getValues(startDate, endDate,nrOfValues);
	}

	private double[] getValues(Date startDate, Date endDate, int nrOfValues) {
		double[] values = new double[nrOfValues];

		LinkedList<Measurement> data = manager.fetchData(getResource(), startDate, endDate);
		if (data != null && !data.isEmpty()) {
			// We have raw data, now we need to portion it properly
			int globalCounter = 0;
			Date currentPosition = data.get(0).getDate();
			Date NextPart = getTimePeriod().getNextDate(startDate, 1);

			for (int i = 0; i < nrOfValues; i++) {

				double sum = 0.0;
				int elements = 0;

				while (currentPosition.before(NextPart) && globalCounter < data.size()) {
					sum += data.get(globalCounter).getValue();
					currentPosition = data.get(globalCounter).getDate();
					globalCounter++;
					elements++;
				}

				if (elements != 0)
					values[i] = sum / (double) elements;
				else
					values[i] = DataShaper.NO_DATA;

				NextPart = getTimePeriod().getNextDate(NextPart, 1);
			}
		}

		return DataShaper.getFixedResultSet(values);
	}

	@Override
	public double[] getValues() {
		Date now = new Date();
		Date startDate = getTimePeriod().getPreviousDate(now, 1);
		return getValues(startDate, now,this.getNrOfObservedValues());
	}

}
