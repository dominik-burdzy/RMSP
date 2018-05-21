package pl.edu.agh.rmsp.base.testing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.agh.rmsp.analysis.predictors.ArimaPredictor;
import pl.edu.agh.rmsp.analysis.predictors.BatsPredictor;
import pl.edu.agh.rmsp.analysis.predictors.GarchPredictor;
import pl.edu.agh.rmsp.analysis.predictors.HoltWintersPredictor;
import pl.edu.agh.rmsp.analysis.predictors.Predictor;
import pl.edu.agh.rmsp.analysis.predictors.RegressionPredictor;
import pl.edu.agh.rmsp.base.gui.App;
import pl.edu.agh.rmsp.model.commons.TimePeriod;
import pl.edu.agh.rmsp.model.miner.DbMiner;
import pl.edu.agh.rmsp.rating.NeuralPredictorTestDecorator;
import pl.edu.agh.rmsp.rating.Tester;

public class TestManager extends Thread {

	private DbMiner miner;
	private TimePeriod timePeriod;
	private int nrOfTestRecords;
	private List<Tester> testers;
	private int nrOfValuesToPredict;
	private App app;
	private boolean[] methods;
	private int chartId;
	
	public TestManager(App app, String resourceName, TimePeriod timePeriod, int nrOfTestRecords,
			int nrOfValuesToPredict, boolean[] methods, int chartId) {
		this.app = app;
		this.timePeriod = timePeriod;
		this.nrOfTestRecords = nrOfTestRecords;
		this.nrOfValuesToPredict = nrOfValuesToPredict;
		this.miner = new DbMiner(resourceName, new TimePeriod(timePeriod.getTimeUnit(),
				nrOfTestRecords + timePeriod.getNrOfTimeUnits() - 1 + nrOfValuesToPredict));
		this.methods = methods;
		this.chartId = chartId;
		initTestersList();
	}

	private void initTestersList() {
		testers = new ArrayList<Tester>();
		if (methods[0]) {
			testers.add(new Tester(timePeriod.getNrOfTimeUnits(), nrOfTestRecords, nrOfValuesToPredict,
					new ArimaPredictor(), chartId));
		}
		if (methods[1]) {
			testers.add(new Tester(timePeriod.getNrOfTimeUnits(), nrOfTestRecords, nrOfValuesToPredict,
					new BatsPredictor(), chartId));
		}
		if (methods[2]) {
			testers.add(new Tester(timePeriod.getNrOfTimeUnits(), nrOfTestRecords, nrOfValuesToPredict,
					new GarchPredictor(), chartId));
		}
		if (methods[3]) {
			testers.add(new Tester(timePeriod.getNrOfTimeUnits(), nrOfTestRecords, nrOfValuesToPredict,
					new HoltWintersPredictor(), chartId));
		}
		if (methods[4]) {
			testers.add(new Tester(timePeriod.getNrOfTimeUnits(), nrOfTestRecords, nrOfValuesToPredict,
					new RegressionPredictor(), chartId));
		}
		if (methods[5]) {
			NeuralPredictorTestDecorator neuralPredictorDecorator = new NeuralPredictorTestDecorator(
					timePeriod.getNrOfTimeUnits(), chartId);
			neuralPredictorDecorator.initLearningRule(app.getnLearningRate(chartId), app.getnMaxIter(chartId),
					app.getnMomentum(chartId), app.getnMaxErrorInc(chartId), app.getnMaxError(chartId));
			neuralPredictorDecorator.initLearningData(miner, app.getnOfInitialNetworkDataRecords(chartId),
					nrOfTestRecords);
			testers.add(new Tester(timePeriod.getNrOfTimeUnits(), nrOfTestRecords, nrOfValuesToPredict,
					neuralPredictorDecorator, chartId));
		}
	}

	public void run() {
		try {
			performTesting();
		} catch (Exception e) {
			e.printStackTrace();
		}
		printTestResult();
	}

	public void performTesting() throws Exception {
		Date start = new Date();
		double[] data = miner.getValues();
		for (Tester t : testers) {
			t.setData(data);
			t.start();
		}
		for (Tester t : testers) {
			t.join();
		}
		printTestResult();
		Date end = new Date();
		long res = end.getTime() - start.getTime();
		System.out.println("Time: " + res * 0.001);
	}

	private Predictor getBestPredictor() {
		double minError = Double.MAX_VALUE;
		Predictor bestPredictor = null;
		for (Tester t : testers) {
			if (t.getError().getAverageError() < minError) {
				minError = t.getError().getAverageError();
				bestPredictor = t.getPredictor();
			}
		}
		return bestPredictor;
	}

	public void printTestResult() {
		Map<String, Double> results = new HashMap<>();
		for (Tester t : testers) {
			results.put(t.getPredictor().getName(), t.getError().getAverageError());
		}
		app.showTestResults(chartId, results);

		// System.out.println("-------------- Test results: ------------ \n");
		// System.out.println("Best predictor: " + getBestPredictor().getName()
		// + "\n\n");
	}

}
