package pl.edu.agh.rmsp.base.container;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pl.edu.agh.rmsp.analysis.predictors.Predictor;
import pl.edu.agh.rmsp.base.gui.App;
import pl.edu.agh.rmsp.model.commons.TimePeriod;
import pl.edu.agh.rmsp.model.commons.Value;

public class DataContainer {

	double probes[];
	Map<Date, Value> predictedValuesMap;
	TimePeriod timePeriod;
	TimePeriod updatingTimePeriod;
	Predictor predictor;
	String resourcesName;
	int nrOfValuesToPredict;

	ScheduledExecutorService scheduler;

	App app;
	int chartId;

	public DataContainer(TimePeriod timePeriodForPrediction, TimePeriod updatingTimePeriod, int nrOfValuesToPredict) {
		setScheduler(Executors.newScheduledThreadPool(1));
		this.timePeriod = timePeriodForPrediction;
		this.nrOfValuesToPredict = nrOfValuesToPredict;
		this.updatingTimePeriod = updatingTimePeriod;
	}

	public DataContainer(TimePeriod timePeriodForPrediction, TimePeriod updatingTimePeriod, int nrOfValuesToPredict,
			App app, Predictor predictor, String resourceName,int chartId) {
		this(timePeriodForPrediction, updatingTimePeriod, nrOfValuesToPredict);
		this.app = app;
		this.chartId = chartId;
		this.predictor = predictor;
		this.resourcesName=resourceName;
	}

	public void startUpdating() {
		getScheduler().scheduleWithFixedDelay(new DataUpdater(this), 0, getUpdatingTimePeriod().getNrOfTimeUnits(),
				getUpdatingTimePeriod().getTimeUnit());
	}

	public void stopUpdating() throws InterruptedException {
		getScheduler().shutdown();
		getScheduler().awaitTermination(10,  TimeUnit.SECONDS);;
	}

	public void update() throws Exception {
		Value[] predicted = predictor.predict(probes, nrOfValuesToPredict);
		app.setPredictedValues(chartId, predicted, probes);
	}

	public TimePeriod getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(TimePeriod timePeriod) {
		this.timePeriod = timePeriod;
	}

	public Predictor getPredictor() {
		return predictor;
	}

	public void setPredictor(Predictor predictor) {
		this.predictor = predictor;
	}

	public String getResourcesName() {
		return resourcesName;
	}

	public void setResourcesName(String resourcesName) {
		this.resourcesName = resourcesName;
	}

	public double[] getProbes() {
		return probes;
	}

	public Map<Date, Value> getPredictedValuesMap() {
		return predictedValuesMap;
	}

	public void setProbes(double[] probes) {
		this.probes = probes;
	}

	public void setUpdatingTimePeriod(TimePeriod updatingTimePeriod) {
		this.updatingTimePeriod = updatingTimePeriod;
	}

	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public void setScheduler(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}

	public TimePeriod getUpdatingTimePeriod() {
		return updatingTimePeriod;
	}

}
