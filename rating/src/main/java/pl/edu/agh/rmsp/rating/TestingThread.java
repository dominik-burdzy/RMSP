package pl.edu.agh.rmsp.rating;

import org.apache.commons.lang3.ArrayUtils;

import pl.edu.agh.rmsp.analysis.predictors.Predictor;
import pl.edu.agh.rmsp.model.commons.Value;

public class TestingThread extends Thread {

	private int startingIndex;
	private int nrOfProbes;
	private int nrOfRecords;
	private int nrOfValuesToPredict;
	private Predictor predictor;
	private double[] data;
	private TestError error;
	private int chartId;

	public TestingThread(int startingIndex,int nrOfProbes, int nrOfRecords, int nrOfValuesToPredict, Predictor predictor,
			TestError error,double[] data, int chartId) {
		this.startingIndex=startingIndex;
		this.nrOfProbes = nrOfProbes;
		this.nrOfRecords = nrOfRecords;
		this.nrOfValuesToPredict = nrOfValuesToPredict;
		this.predictor = predictor;
		this.error = error;
		this.data=data;
		this.chartId = chartId;
	}

	private void addRecordToNetworkData(double[] values){
		((NeuralPredictorTestDecorator) predictor).addRecordToNetworkData(values);
	}
	
	public void run() {
		for (int i = startingIndex; i < Math.min(startingIndex+Tester.NR_OF_RECORDS_PER_THREAD, nrOfRecords); i++) {
			ProgressbarController pinstance = ProgressbarController.getInstance();
			pinstance.testIncrement(chartId);
			double[] probes = new double[nrOfProbes];
			for (int j = 0; j < nrOfProbes; j++) {
				probes[j] = data[i + j];
			}
			double[] realValues = new double[nrOfValuesToPredict];
			for (int j = 0; j < nrOfValuesToPredict; j++) {
				realValues[j] = data[i+nrOfProbes + j];
			}
			double[] result;
			try {
				result = toDoubleArray(predictor.predict(probes, nrOfValuesToPredict));
				if(predictor instanceof NeuralPredictorTestDecorator){
					double[] row = ArrayUtils.addAll(probes, result);
					addRecordToNetworkData(row);
				}
				error.addError(realValues, result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private double[] toDoubleArray(Value[] values) {
		double[] result = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			result[i] = values[i].getValue();
		}
		return result;
	}
}
