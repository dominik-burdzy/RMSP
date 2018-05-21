package pl.edu.agh.rmsp.analysis.neural;

import java.util.List;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.learning.error.MeanSquaredError;
import org.neuroph.nnet.comp.layer.InputLayer;
import org.neuroph.nnet.comp.neuron.BiasNeuron;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.ConnectionFactory;
import org.neuroph.util.NeuralNetworkFactory;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;

/**
 * Created by michal on 21.10.2015.
 */
public class Network {
	private int inputNeurons;
	private int hiddenNeurons;
	private int outputNeurons = 1;

	private double maxError = 0.000005;
	private double learningRate = 0.7;
	private int maxIterations = 10;
	private double momentum = 0.1;

	private int learningBestIterationNr = 0;
	private int cycleIterationNr = 0;
	// used for learning and testing
	private NeuralNetwork<MomentumBackpropagation> neuralNetwork;
	// used for prediction
	private NeuralNetwork<MomentumBackpropagation> bestNeuralNetwork;

	private NetworkDataSets networkDataSets;
	private double maxTestErrorIncrease = 0.005; // value which stops learning
													// to avoid overfitting
	private double minTestError = Double.MAX_VALUE; // best result

	private MomentumBackpropagation learningRule;

	private boolean shouldBeLearning = true;

	public Network(int inputNeurons) {
		super();
		this.inputNeurons = inputNeurons;
		hiddenNeurons = (int) Math.sqrt(inputNeurons * outputNeurons);
		networkDataSets = new NetworkDataSets();
		neuralNetwork = new NeuralNetwork<MomentumBackpropagation>();
		bestNeuralNetwork = new NeuralNetwork<MomentumBackpropagation>();
		createNetwork(neuralNetwork);
		createNetwork(bestNeuralNetwork);
	}

	public void initLearningRule(double learningRate, int maxIterations, double momentum, double maxTestErrorIncrease,
			double maxError) {
		this.learningRate = learningRate;
		this.maxIterations = maxIterations;
		this.momentum = momentum;
		this.maxTestErrorIncrease = maxTestErrorIncrease;
		this.maxError = maxError;
		learningRule = new MomentumBackpropagation();
		learningRule.setMaxError(maxError);
		learningRule.setLearningRate(learningRate);
		learningRule.setMaxIterations(maxIterations);
		learningRule.setTrainingSet(networkDataSets.getLearningDataSet());
		learningRule.setMomentum(momentum);
		neuralNetwork.setLearningRule(learningRule);
	}

	private void copyWeights(NeuralNetwork from, NeuralNetwork to) {
		for (int i = 0; i < from.getLayersCount(); i++) {
			Neuron[] fromNeurons = from.getLayers()[i].getNeurons();
			Neuron[] toNeurons = to.getLayers()[i].getNeurons();
			for (int j = 0; j < fromNeurons.length; j++) {
				for (int k = 0; k < toNeurons[j].getWeights().length; k++) {
					toNeurons[j].getWeights()[k].setValue(fromNeurons[j].getWeights()[k].getValue());
				}
			}
		}
	}

	private void createNetwork(NeuralNetwork<MomentumBackpropagation> network) {
		// create layers
		InputLayer inputLayer = new InputLayer(inputNeurons);
		inputLayer.addNeuron(new BiasNeuron());

		NeuronProperties neuronProperties = new NeuronProperties();
		neuronProperties.setProperty("transferFunction", TransferFunctionType.SIGMOID);

		Layer hiddenLayer = new Layer(hiddenNeurons, neuronProperties);
		hiddenLayer.addNeuron(new BiasNeuron());

		Layer outputLayer = new Layer(outputNeurons, neuronProperties);

		// make connections
		ConnectionFactory.fullConnect(inputLayer, hiddenLayer);
		ConnectionFactory.fullConnect(hiddenLayer, outputLayer);

		// add layers- oreder is important here - input must be the first added
		// layer, and output the last one
		network.addLayer(inputLayer);
		network.addLayer(hiddenLayer);
		network.addLayer(outputLayer);

		// define inputLAyer as input, and output as output
		NeuralNetworkFactory.setDefaultIO(network);

	}

	public void setData(List<Double> dat) {
		networkDataSets.putData(dat, inputNeurons, outputNeurons);
		learningRule.setTrainingSet(networkDataSets.getLearningDataSet());
	}

	private double[] calculate(double[] input) {
		double[] result;
		synchronized (bestNeuralNetwork) {
			bestNeuralNetwork.setInput(networkDataSets.normalize(input));
			bestNeuralNetwork.calculate();
			result = bestNeuralNetwork.getOutput();
		}
		result = networkDataSets.denormalize(result);
		return result;
	}

	public double[] predict(double[] in, int n) {
		double[] input = in.clone();
		double[] result = new double[n];
		for (int i = 0; i < n; i++) {
			double[] output = calculate(input);

			for (int j = 0; j < outputNeurons; j++) {
				if (i + j < n) {
					result[i + j] = output[j];
				}
			}
			for (int j = 0; j < inputNeurons - outputNeurons; j++) {
				input[j] = input[j + outputNeurons];
			}
			for (int j = 0; j < outputNeurons; j++) {
				input[inputNeurons - outputNeurons + j] = output[j];
			}
		}
		String k = "Input: ";
		for (double d : in) {
			k += " " + d + " ";

		}
		System.out.println(k);
		String l = "Output: ";
		for (double d : result) {
			l += " " + d + " ";

		}
		System.out.println(l);
		// return exp(result);
		return result;
	}

	public void startLearning(boolean newThread) {
		if (newThread) {
			neuralNetwork.learnInNewThread(networkDataSets.getLearningDataSet());
		}
		else{
			neuralNetwork.learn(networkDataSets.getLearningDataSet());
		}
	}

	private void printStatus(double errorOnTestData) {
		System.out.println("##### Best error: " + minTestError + " after iteration: " + learningBestIterationNr);
		System.out.println("##### Iteration: " + neuralNetwork.getLearningRule().getCurrentIteration());
		System.out.println("##### Error on learning data: " + neuralNetwork.getLearningRule().getTotalNetworkError());
		System.out.println("##### Error on testing data: " + errorOnTestData);
	}

	public void pauseLearning(boolean printStatus) {
		cycleIterationNr++;
		neuralNetwork.pauseLearning();
		minTestError = getErrorFromTestData(bestNeuralNetwork);
		double errorOnTestData = getErrorFromTestData(neuralNetwork);
		if (printStatus) {
			printStatus(errorOnTestData);
		}
		if (isProgress(errorOnTestData) || cycleIterationNr < 5) {
			minTestError = errorOnTestData;
			// save best network
			copyWeights(neuralNetwork, bestNeuralNetwork);
			learningBestIterationNr += learningRule.getCurrentIteration();
		}
		if (shouldStopLearning(errorOnTestData)) {
			// keep best result
			copyWeights(bestNeuralNetwork, neuralNetwork);
		}

	}

	public void resumeLearning(boolean newThread) {
		if (neuralNetwork.getLearningRule().getCurrentIteration() >= maxIterations) {
			neuralNetwork.stopLearning();
			if (newThread) {
				neuralNetwork.learnInNewThread(networkDataSets.getLearningDataSet());
			} else {
				neuralNetwork.learn(networkDataSets.getLearningDataSet());
			}
		} else {
			neuralNetwork.resumeLearning();
		}
	}

	public void stopLearning() {
		neuralNetwork.stopLearning();
	}

	private double getErrorFromTestData(NeuralNetwork neuralNetwork) {
		double[] outputSingleErrors = new double[outputNeurons];
		synchronized (networkDataSets) {
			MeanSquaredError meanSquaredError = new MeanSquaredError(networkDataSets.getTestData().getRows().size());
			for (DataSetRow row : networkDataSets.getTestData().getRows()) {
				neuralNetwork.setInput(row.getInput());
				neuralNetwork.calculate();
				double[] output = neuralNetwork.getOutput();
				for (int i = 0; i < output.length; i++) {
					outputSingleErrors[i] = output[i] - row.getDesiredOutput()[i];
				}
				meanSquaredError.addOutputError(outputSingleErrors);
			}
			return meanSquaredError.getTotalError();
		}
	}

	private boolean isProgress(double error) {
		return (error < minTestError);
	}

	private boolean shouldStopLearning(double error) {
		if (Math.abs(error - minTestError) > maxTestErrorIncrease) {
			return true;
		} else
			return false;
	}

	/*
	 * public double[] log(double[] d) { double[] k = new double[d.length]; for
	 * (int i = 0; i < d.length; i++) { k[i] = Math.log(d[i]); } return k; }
	 * 
	 * public double[] exp(double[] d) { double[] k = new double[d.length]; for
	 * (int i = 0; i < d.length; i++) { k[i] = Math.exp(d[i]); } return k; }
	 */

	public void addRecords(double[] values) {
		networkDataSets.addRecord(values, inputNeurons, outputNeurons);
	}

	public NeuralNetwork<MomentumBackpropagation> getNeuralNetwork() {
		return neuralNetwork;
	}

	public boolean shouldBeLearning() {
		return shouldBeLearning;
	}

	public void setShouldBeLearning(boolean t) {
		shouldBeLearning = t;
	}

	public NetworkDataSets getNetworkDataSets() {
		return networkDataSets;
	}

	public void setMaxError(double maxError) {
		this.maxError = maxError;
	}

	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public void setMomentum(double momentum) {
		this.momentum = momentum;
	}

	public void setMaxTestErrorIncrease(double maxTestErrorIncrease) {
		this.maxTestErrorIncrease = maxTestErrorIncrease;
	}

	public MomentumBackpropagation getLearningRule() {
		return learningRule;
	}
}
