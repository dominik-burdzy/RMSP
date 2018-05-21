package pl.edu.agh.rmsp.rating;

import java.util.LinkedList;
import java.util.List;

import pl.edu.agh.rmsp.analysis.predictors.Predictor;

public class Tester extends Thread {

	public static int NR_OF_RECORDS_PER_THREAD = 10;

	private double[] data;
	private int nrOfProbes;
	private int nrOfRecords;
	private int nrOfValuesToPredict;
	private TestError error;
	private Predictor predictor;
	private int chartId;
    
	public Tester(int nrOfProbes, int nrOfRecords, int nrOfValuesToPredict, Predictor predictor, int chartId) {
		this.nrOfProbes = nrOfProbes;
		this.nrOfRecords = nrOfRecords;
		this.nrOfValuesToPredict = nrOfValuesToPredict;
		this.predictor = predictor;
		this.error = new TestError();
		this.chartId = chartId;
	}

	public void setData(double[] data) {
		this.data = data;
	}



	public void run() {
		if(predictor instanceof NeuralPredictorTestDecorator){
			((NeuralPredictorTestDecorator) predictor).teachNetworkForTest(NeuralPredictorTestDecorator.NR_OF_ITERATIONS);
		}
		List<Thread> threads = new LinkedList<Thread>();
		int threadCounter=0;
		while (NR_OF_RECORDS_PER_THREAD * threadCounter < nrOfRecords) {
			threads.add( new TestingThread(NR_OF_RECORDS_PER_THREAD * threadCounter,nrOfProbes, nrOfRecords, nrOfValuesToPredict, predictor,error,data,chartId));
			threadCounter++;
		}
		for(Thread t: threads){
			t.start();
		}
		for(Thread t: threads){
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public TestError getError() {
		return error;
	}

	public Predictor getPredictor() {
		return predictor;
	}

}
