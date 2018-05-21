package pl.edu.agh.rmsp.analysis.predictors;

import pl.edu.agh.rmsp.model.commons.Value;

public interface Predictor {

	public Value[] predict(double[] rawValues, int nrOfValuesToPredict) throws Exception;
	public String getName();
}
