package pl.edu.agh.rmsp.base.container;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import pl.edu.agh.rmsp.analysis.neural.NeuralPredictor;
import pl.edu.agh.rmsp.base.gui.App;
import pl.edu.agh.rmsp.model.commons.TimePeriod;
import pl.edu.agh.rmsp.model.miner.DbMiner;


public class NeuralDataContainer extends DataContainer {

	public NeuralDataContainer(TimePeriod timePeriodForPrediction, TimePeriod updatingTimePeriod,
			int nrOfValuesToPredict, App app, String resourceName,int chartId) throws NumberFormatException, IOException, ParseException {
		super(timePeriodForPrediction, updatingTimePeriod, nrOfValuesToPredict);
		this.app = app;
		this.chartId = chartId;
		this.resourcesName=resourceName;
	}

	public void initLearningRule(double learningRate, int maxIterations, double momentum, double maxTestErrorIncrease,
			double maxError) {
		((NeuralPredictor) predictor).initLearningRule(learningRate, maxIterations, momentum, maxTestErrorIncrease,
				maxError);
	}

	public void startUpdating() {
		getScheduler().scheduleWithFixedDelay(new NeuralDataUpdater(this), 0, getUpdatingTimePeriod().getNrOfTimeUnits(),
				getUpdatingTimePeriod().getTimeUnit());
	}
	
	public void initNeuralData(int nrOfInitialNetworkDataRecords) {
		DbMiner miner = new DbMiner(this.getResourcesName(),
				new TimePeriod(this.timePeriod.getTimeUnit(), nrOfInitialNetworkDataRecords));
		double[] records = miner.getValues();
		ArrayList<Double> recordsArrayList = new ArrayList<Double>();
		for (int i = 0; i < records.length; i++) {
			recordsArrayList.add(records[i]);
		}
		((NeuralPredictor) predictor).getNetwork().setData(recordsArrayList); 
	}

	public void startLearning() {
		((NeuralPredictor) predictor).getNetwork().startLearning(true);
	}
	
	public void setPredictor(NeuralPredictor predictor){
		this.predictor=predictor;
	}

}
