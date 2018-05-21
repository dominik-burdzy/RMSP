package pl.edu.agh.rmsp.rating;

public class TestError {

	private double errorSum = 0;;
	int n = 0;

	public synchronized void addError(double[] realValues, double[] predictedValues) {
		double error = 0;
		for (int i = 0; i < realValues.length; i++) {
			error += (realValues[i] - predictedValues[i]) * (realValues[i] - predictedValues[i]);
		}
		error = error / realValues.length;
		errorSum += Math.sqrt(error);
		n++;
	}

	public double getAverageError() {
		return errorSum / n;
	}
}
