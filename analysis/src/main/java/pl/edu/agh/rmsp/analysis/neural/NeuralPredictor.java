package pl.edu.agh.rmsp.analysis.neural;

import pl.edu.agh.rmsp.analysis.predictors.Predictor;
import pl.edu.agh.rmsp.model.commons.Value;


/**
 * Created by michal on 21.10.2015.
 */
public class NeuralPredictor implements Predictor {

	private static final String name = "Neural Predictor";

	private Network network;

	public NeuralPredictor(int inputNeurons){
		network = new Network(inputNeurons);
	}
	
    public void initLearningRule(double learningRate, int maxIterations, double momentum,
			double maxTestErrorIncrease, double maxError){
    	network.initLearningRule(learningRate, maxIterations, momentum, maxTestErrorIncrease,maxError);
    }
	
	public Value[] predict(double[] rawValues, int nrOfValuesToPredict) throws Exception {
		if (network.shouldBeLearning()) {
			network.pauseLearning(true);
		}
		// double[] input = exampleMinerFromFile.getNextRawValues();
		double[] predicted = network.predict(rawValues, nrOfValuesToPredict);
		if (network.shouldBeLearning()) {
			network.resumeLearning(true);
		}
		Value[] values = new Value[nrOfValuesToPredict];
		for (int i = 0; i < nrOfValuesToPredict; i++) {
			values[i] = new Value(predicted[i], 0, 0);
		}
		return values;
	}

	public Network getNetwork() {
		return network;
	}

	public String getName() {
		return name;
	}
}
