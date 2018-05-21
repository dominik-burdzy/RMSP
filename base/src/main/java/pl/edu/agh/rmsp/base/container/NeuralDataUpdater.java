package pl.edu.agh.rmsp.base.container;

import pl.edu.agh.rmsp.analysis.neural.NeuralPredictor;
import pl.edu.agh.rmsp.model.commons.TimePeriod;
import pl.edu.agh.rmsp.model.miner.DbMiner;

public class NeuralDataUpdater extends DataUpdater{

	public NeuralDataUpdater(NeuralDataContainer dataContainer) {
		super();
		this.dataContainer = dataContainer;
		//+1 unit - so in miner.getValues() there will be one record to add to data set
		this.miner=new DbMiner(dataContainer.getResourcesName(),
				new TimePeriod(dataContainer.getTimePeriod().getTimeUnit(), dataContainer.getTimePeriod().getNrOfTimeUnits()+1));
	}

	@Override
	public void run() {
		try {
			//values[] have one more record than it is necessary, just to add to neural test/learning set
			double[] values = miner.getValues();
			double[] probes = new double[values.length-1];
			for(int i =0; i<values.length-1;i++){
				probes[i]=values[i+1];
			}
			if (somethingChanged(probes)) {
				updateValues(probes);
				//must stop learning thread, to avoid concurrent exception
				((NeuralPredictor)dataContainer.predictor).getNetwork().stopLearning();
				addRecordToNetworkData(values);
				((NeuralPredictor)dataContainer.predictor).getNetwork().startLearning(true);
			}			
		} catch (Exception e) {
			// if any runtime exceptions are throwed in scheduler, nothing
			// happens. One must explicitly define error catching
			e.printStackTrace();
		}
	}
	
	private void addRecordToNetworkData(double[] values){
		NeuralDataContainer c = (NeuralDataContainer) dataContainer;
		NeuralPredictor p = (NeuralPredictor)c.getPredictor();
		p.getNetwork().addRecords(values);
	}
}
