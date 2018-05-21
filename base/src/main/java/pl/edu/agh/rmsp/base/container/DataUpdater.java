package pl.edu.agh.rmsp.base.container;

import pl.edu.agh.rmsp.model.miner.DbMiner;

public class DataUpdater implements Runnable {

	DataContainer dataContainer;
	DbMiner miner;

	public DataUpdater(){
		super();
	}
	
	public DataUpdater(DataContainer dataContainer) {
		super();
		this.dataContainer = dataContainer;
		this.miner=new DbMiner(dataContainer.getResourcesName(),
				dataContainer.getTimePeriod());
	}

	public void run() {
		try {
			double[] values = getMiner().getValues();
			if (somethingChanged(values)) {
				updateValues(values);
			}
		} catch (Exception e) {
			// if any runtime exceptions are throwed in scheduler, nothing
			// happens. One must explicitly define error catching
			e.printStackTrace();
		}
	}

	public boolean somethingChanged(double[] values) {
		// check if anything changed in probes data
		boolean somethingChanged = false;
		if (dataContainer.getProbes() != null) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] - dataContainer.getProbes()[i] != 0) {
					somethingChanged = true;
					break;
				}
			}
		} else {
			somethingChanged = true;
		}
		return somethingChanged;
	}

	public void updateValues(double[] values) throws Exception {
		dataContainer.setProbes(values);
		dataContainer.update();
	}

	public DbMiner getMiner() {
		return miner;
	}

	public void setMiner(DbMiner miner) {
		this.miner = miner;
	}

}
