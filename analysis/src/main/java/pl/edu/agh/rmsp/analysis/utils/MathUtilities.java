package pl.edu.agh.rmsp.analysis.utils;

import pl.edu.agh.rmsp.model.commons.Value;

public class MathUtilities {

	/*
	 * return values taken from double matrix.
	 */
	public static Value[] convertMatrixToValues(double[][] result,
			int nrOfValuesToPredict, int predictedValueColumnNumber,
			int minConfidenceIntervalColumnNr, int maxCondidenceIntervalColumnNr) {
		Value[] predictedValues = new Value[nrOfValuesToPredict];
		double res, min, max;
		for (int i = 0; i < nrOfValuesToPredict; i++) {
			res = result[predictedValueColumnNumber][i];
			min = result[minConfidenceIntervalColumnNr][i];
			max = result[maxCondidenceIntervalColumnNr][i];
			predictedValues[i] = new Value(res, max, min);
		}
		return predictedValues;
	}

	public static boolean logOperationPossible(double[] values) {
		boolean result = true;
		for (int i = 0; i < values.length; i++) {
			if (values[i] <= 0) {
				result = false;
				break;
			}
		}
		return result;
	}

}
