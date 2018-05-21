package pl.edu.agh.rmsp.base.gui;

import java.io.IOException;
import java.text.ParseException;

import pl.edu.agh.rmsp.analysis.neural.NeuralPredictor;
import pl.edu.agh.rmsp.analysis.predictors.Predictor;
import pl.edu.agh.rmsp.base.container.DataContainer;
import pl.edu.agh.rmsp.base.container.NeuralDataContainer;
import pl.edu.agh.rmsp.base.testing.TestManager;
import pl.edu.agh.rmsp.model.commons.TimePeriod;

public class ChartThread extends Thread {
	private App app;
	private int chartId;

	private TimePeriod predictionTimePeriod;
	private TimePeriod updatingTimePeriod;
	private int nrOfValuesToPredict;
	private Predictor predictor;
	private DataContainer dataContainer;
	private String resourceName;

	public ChartThread(App app, int chartId, String resourceName) {
		this.app = app;
		this.chartId = chartId;
		this.resourceName = resourceName;
	}

	public void destroy() {
		this.destroy();
	}

	public synchronized void setData(TimePeriod prediction, TimePeriod updating, int nrOfValues) {
		this.predictionTimePeriod = prediction;
		this.updatingTimePeriod = updating;
		this.nrOfValuesToPredict = nrOfValues;
	}

	public void run() {
		try {
			predictor = app.getPredictor(chartId,
					(String) app.algorithmChoiceBox.get(chartId).getSelectionModel().getSelectedItem());
			if (predictor instanceof NeuralPredictor) {
				dataContainer = new NeuralDataContainer(predictionTimePeriod, updatingTimePeriod, nrOfValuesToPredict,
						app, resourceName, chartId);
				((NeuralPredictor) predictor).getNetwork().getNetworkDataSets()
						.setMaxNrOfLearningRecords(app.getnSetSize(chartId));
				((NeuralDataContainer) dataContainer).setPredictor(predictor);
				((NeuralDataContainer) dataContainer).initLearningRule(app.getnLearningRate(chartId),
						app.getnMaxIter(chartId), app.getnMomentum(chartId), app.getnMaxErrorInc(chartId),
						app.getnMaxError(chartId));
				((NeuralDataContainer) dataContainer).initNeuralData(app.getnOfInitialNetworkDataRecords(chartId));
				((NeuralDataContainer) dataContainer).startLearning();
			} else {
				dataContainer = new DataContainer(predictionTimePeriod, updatingTimePeriod, nrOfValuesToPredict, app,
						predictor, resourceName, chartId);
			}

			dataContainer.startUpdating();
		} catch (NumberFormatException | IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopThread() throws InterruptedException {
		this.dataContainer.stopUpdating();
	}

	public void pauseNeuralLearning() {
		if (predictor instanceof NeuralPredictor) {
			NeuralPredictor neuralPredictor = (NeuralPredictor) predictor;
			neuralPredictor.getNetwork().setShouldBeLearning(false);
			neuralPredictor.getNetwork().pauseLearning(true);
		}
	}

	public void resetNeuralLearning() {
		dataContainer.getScheduler().shutdown();
		run();
	}

	public void continueNeuralLearning() {
		if (predictor instanceof NeuralPredictor) {
			NeuralPredictor neuralPredictor = (NeuralPredictor) predictor;
			neuralPredictor.getNetwork().setShouldBeLearning(true);
			neuralPredictor.getNetwork().resumeLearning(true);
		}
	}

}
