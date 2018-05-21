package pl.edu.agh.rmsp.analysis.predictors;

import pl.edu.agh.rmsp.analysis.utils.RCallerCreator;
import pl.edu.agh.rmsp.analysis.utils.RCallerWrapper;
import pl.edu.agh.rmsp.model.commons.Value;
import rcaller.RCode;
import rcaller.exception.ExecutionException;

public class RegressionPredictor implements Predictor {

	private static final String name = "Regression Predictor";

	private RCallerWrapper caller;
	private int polynomialDegree = 1;
	private String expression = "y ~ x";

	public RegressionPredictor() {
		caller = RCallerCreator.getRCaller();
	}

	public Value[] predict(double[] values, int nrOfValuesToPredict) {
		synchronized (caller) {
			caller.cleanRCode();
			RCode rcode = new RCode();
			// calculate mean
			rcode.addDoubleArray("rawValues", values);
			rcode.addRCode("m<-mean(rawValues)");
			caller.setRCode(rcode);
			caller.runAndReturnResultOnline("m");
			double mean = caller.getParser().getAsDoubleArray("m")[0];
			// calculate deviation
			rcode.clear();
			rcode.addRCode("d<-sd(rawValues)");
			caller.setRCode(rcode);
			caller.runAndReturnResultOnline("d");
			double deviation = caller.getParser().getAsDoubleArray("d")[0];
			// standarization of raw data
			rcode.clear();
			rcode.addRCode("y<-scale(rawValues)");
			rcode.addRCode("x = 1:" + (values.length));
			// perform regression modelling
			rcode.addRCode("model<-lm(" + expression + ")");
			// calculate the predicted values

			String v = "result<-predict(model, data.frame(x=seq(" + values.length + ","
					+ (values.length + nrOfValuesToPredict - 1) + ",1)),interval=\"predict\")";
			rcode.addRCode(v);

			caller.setRCode(rcode);
			try {
				caller.runAndReturnResultOnline("result");
				rcode.clear();
				double result[] = caller.getParser().getAsDoubleArray("result");
				rcode.clear();
				// unstandarize data
				Value[] predictedValues = new Value[nrOfValuesToPredict];
				double res, min, max;
				for (int i = 0; i < nrOfValuesToPredict; i++) {
					res = unStandarize(result[i], mean, deviation);
					min = unStandarize(result[i + nrOfValuesToPredict], mean, deviation);
					max = unStandarize(result[i + 2 * nrOfValuesToPredict], mean, deviation);
					predictedValues[i] = new Value(res, max, min);
				}
				caller.cleanRCode();
				return predictedValues;
			} catch (ExecutionException e) {
				caller.resetRCaller();
				caller = RCallerCreator.getRCaller();
				throw e;
			} finally {
				rcode.clear();
				caller.cleanRCode();
			}
		}
	}

	private double unStandarize(double x, double mean, double deviation) {
		return x * deviation + mean;
	}

	public void setPolynomialDegree(int polynomialDegree) {
		this.polynomialDegree = polynomialDegree;
		prepareExpression();
	}

	private void prepareExpression() {
		expression = "y ~ x";
		for (int i = 2; i <= polynomialDegree; i++) {
			expression = expression + "+I(x^" + i + ")";
		}
	}

	public String getName() {
		return name;
	}
}
