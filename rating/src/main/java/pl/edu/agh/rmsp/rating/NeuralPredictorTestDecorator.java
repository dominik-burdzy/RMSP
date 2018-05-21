package pl.edu.agh.rmsp.rating;

import java.util.ArrayList;

import pl.edu.agh.rmsp.analysis.neural.NeuralPredictor;
import pl.edu.agh.rmsp.analysis.predictors.Predictor;
import pl.edu.agh.rmsp.model.commons.Value;
import pl.edu.agh.rmsp.model.miner.DbMiner;

public class NeuralPredictorTestDecorator implements Predictor {

	private NeuralPredictor neuralPredictor;

	public static int NR_OF_ITERATIONS = 100;

	public NeuralPredictorTestDecorator(int input, int chartId) {
		neuralPredictor = new NeuralPredictor(input);
	}

	public void initLearningRule(double learningRate, int nMaxIter, double momentum, double maxErrorIncrease,
			double maxError) {
		neuralPredictor.initLearningRule(learningRate, nMaxIter, momentum, maxErrorIncrease, maxError);
	}

	public void initLearningData(DbMiner miner, int nrOfRecordsToLearnFrom, int nrOfTestingRecords) {
		double[] records = miner.getValuesUpTo(nrOfRecordsToLearnFrom, nrOfTestingRecords);
		ArrayList<Double> recordsArrayList = new ArrayList<Double>();
		for (int i = 0; i < records.length; i++) {
			recordsArrayList.add(records[i]);
		}
		neuralPredictor.getNetwork().setData(recordsArrayList);
	}

	/*
	 * learnNetworkForTest() must be invoked before that function
	 */
	// synchronized is added here, so that a testing thread will wait for
	// learnNetworkForTest to end
	public synchronized Value[] predict(double[] rawValues, int nrOfValuesToPredict) throws Exception {
		Value[] result = neuralPredictor.predict(rawValues, nrOfValuesToPredict);
		neuralPredictor.getNetwork().setShouldBeLearning(true);
		if (neuralPredictor.getNetwork().shouldBeLearning()) {
			neuralPredictor.getNetwork().resumeLearning(true);
		}
		neuralPredictor.getNetwork().pauseLearning(true);
		neuralPredictor.getNetwork().setShouldBeLearning(false);
		return result;

	}

	public String getName() {
		return neuralPredictor.getName();
	}

	public synchronized void teachNetworkForTest(int nrOfIterations) {
		neuralPredictor.getNetwork().setShouldBeLearning(true);
		neuralPredictor.getNetwork().startLearning(false);
		int iteration = 0;
		neuralPredictor.getNetwork().startLearning(false);
		while (iteration < nrOfIterations) {
			if (neuralPredictor.getNetwork().shouldBeLearning()) {
				neuralPredictor.getNetwork().pauseLearning(true);
			}
			neuralPredictor.getNetwork().pauseLearning(true);
			iteration += neuralPredictor.getNetwork().getLearningRule().getCurrentIteration();
			if (neuralPredictor.getNetwork().shouldBeLearning()) {
				neuralPredictor.getNetwork().resumeLearning(true);
			}
		}
		neuralPredictor.getNetwork().setShouldBeLearning(false);
	}

	public void addRecordToNetworkData(double[] values) {
		neuralPredictor.getNetwork().addRecords(values);
	}

}
