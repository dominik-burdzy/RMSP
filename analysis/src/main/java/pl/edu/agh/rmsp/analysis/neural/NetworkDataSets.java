package pl.edu.agh.rmsp.analysis.neural;

import java.util.List;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;

public class NetworkDataSets {

	private DataSet learningDataSet;
	private DataSet testData;

	private double dataMin;
	private double dataMax;

	private int maxNrOfLearningRecords = 800;
	private int maxNrOfTestRecords = maxNrOfLearningRecords / 4;

	// counter for deciding if data should be added to learning set or testing
	// set
	private int counter = 0;

	public synchronized void addRecord(double[] values, int inputNeurons, int outputNeurons) {
		double[] newMinMax = dataMaxMinChanged(values);
		if (newMinMax != null) {
			renormalizeRecords(newMinMax[0], newMinMax[1]);
		}

		if (counter % 4 == 0) {
			addRecord(maxNrOfTestRecords, testData, values, inputNeurons, outputNeurons);

		} else {
			addRecord(maxNrOfLearningRecords, learningDataSet, values, inputNeurons, outputNeurons);
		}
		if (counter == Integer.MAX_VALUE) {
			counter = 0;
		}
		counter++;
	}

	private synchronized void renormalizeRecords(double newMin, double newMax) {
		learningDataSet = renormalize(learningDataSet, newMin, newMax);
		testData = renormalize(testData, newMin, newMax);
		dataMin = newMin;
		dataMax = newMax;
	}

	private synchronized DataSet renormalize(DataSet set, double newMin, double newMax) {
		DataSet newSet = new DataSet(set.getInputSize(), set.getOutputSize());
		for (DataSetRow r : set.getRows()) {
			double[] input = denormalize(r.getInput(), dataMin, dataMax);
			double[] output = denormalize(r.getDesiredOutput(), dataMin, dataMax);
			input = normalize(input, newMin, newMax);
			output = normalize(output, newMin, newMax);
			newSet.addRow(input, output);
		}
		return newSet;
	}

	private double[] dataMaxMinChanged(double[] values) {
		double[] newMinMax = new double[2];
		boolean somethingChanged = false;
		for (double d : values) {
			if (d > dataMax) {
				newMinMax[1] = d;
				somethingChanged = true;
			}
			if (d < dataMin) {
				newMinMax[0] = d;
				somethingChanged = true;
			}
		}
		if (somethingChanged) {
			return newMinMax;
		} else {
			return null;
		}

	}

	private void addRecord(int maxNrOfRows, DataSet set, double[] values, int inputNeurons, int outputNeurons) {
		double[] input = new double[inputNeurons];
		double[] output = new double[outputNeurons];

		for (int j = 0; j < inputNeurons; j++) {
			input[j] = normalize(values[j], dataMin, dataMax);
		}
		for (int j = 0; j < outputNeurons; j++) {
			output[j] = normalize(values[inputNeurons + j], dataMin, dataMax);
		}
		if (set.size() < maxNrOfRows) {
			set.addRow(input, output);
		} else {
			set.removeRowAt(0);
			set.addRow(input, output);
		}
	}

	public synchronized void putData(List<Double> data, int inputNeurons, int outputNeurons) {
		// Collections.reverse(data);
		// List<Double> data = log(dat);
		learningDataSet = new DataSet(inputNeurons, outputNeurons);
		testData = new DataSet(inputNeurons, outputNeurons);
		dataMin = data.get(0);
		dataMax = data.get(0);
		for (double d : data) {
			if (d > dataMax) {
				dataMax = d;
			}
			if (d < dataMin) {
				dataMin = d;
			}
		}

		for (int i = 0; i < data.size() + 1 - inputNeurons - outputNeurons; i++) {
			double[] input = new double[inputNeurons];
			double[] output = new double[outputNeurons];

			for (int j = 0; j < inputNeurons; j++) {
				input[j] = normalize(data.get(i + j), dataMin, dataMax);
			}
			for (int j = 0; j < outputNeurons; j++) {
				output[j] = normalize(data.get(i + j + inputNeurons), dataMin, dataMax);
			}
			if (i % 4 != 0) {
				learningDataSet.addRow(input, output);
			} else {
				testData.addRow(input, output);
			}
		}

	}

	public DataSet getLearningDataSet() {
		return learningDataSet;
	}

	public void setLearningDataSet(DataSet learningDataSet) {
		this.learningDataSet = learningDataSet;
	}

	public DataSet getTestData() {
		return testData;
	}

	public void setTestData(DataSet testData) {
		this.testData = testData;
	}

	public double normalize(double d, double dataMin, double dataMax) {
		return (d - dataMin) / (dataMax - dataMin);
	}

	public double denormalize(double d, double dataMin, double dataMax) {
		return d * (dataMax - dataMin) + dataMin;
	}

	public double[] normalize(double[] d, double dataMin, double dataMax) {
		double[] k = new double[d.length];
		for (int i = 0; i < d.length; i++) {
			k[i] = normalize(d[i], dataMin, dataMax);
		}
		return k;
	}

	public double[] normalize(double[] d) {
		return normalize(d, dataMin, dataMax);
	}

	public double[] denormalize(double[] d) {
		return denormalize(d, dataMin, dataMax);
	}

	public double[] denormalize(double[] d, double dataMin, double dataMax) {
		double[] k = new double[d.length];
		for (int i = 0; i < d.length; i++) {
			k[i] = denormalize(d[i], dataMin, dataMax);
		}
		return k;
	}

	public synchronized void setMaxNrOfLearningRecords(int maxNrOfLearningRecords) {
		this.maxNrOfLearningRecords = maxNrOfLearningRecords;
		this.maxNrOfTestRecords = maxNrOfLearningRecords / 4;
		if (this.testData != null) {
			while (this.testData.size() > maxNrOfTestRecords) {
				this.testData.removeRowAt(0);
			}
		}
		if (this.learningDataSet != null) {
			while (this.learningDataSet.size() > maxNrOfLearningRecords) {
				this.learningDataSet.removeRowAt(0);
			}
		}
	}

	/*
	 * public List<Double> log(List<Double> data) { List<Double> newData = new
	 * ArrayList<Double>(data); for (int i = 0; i < data.size(); i++) {
	 * newData.set(i, Math.log(data.get(i))); } return newData; }
	 */
}
