package pl.edu.agh.rmsp.analysis.predictors;

import pl.edu.agh.rmsp.analysis.utils.MathUtilities;
import pl.edu.agh.rmsp.analysis.utils.RCallerCreator;
import pl.edu.agh.rmsp.analysis.utils.RCallerWrapper;
import pl.edu.agh.rmsp.analysis.support.SeasonalityAnalizer;
import pl.edu.agh.rmsp.model.commons.Value;
import rcaller.RCode;
import rcaller.exception.ExecutionException;

/*
 *  Predictor used especially to predict data with significant seasonality 
 */
public class HoltWintersPredictor implements Predictor {

	private static final String name = "Holt-Winters Predictor";

	private RCallerWrapper caller;
	private SeasonalityAnalizer analizer;

	public HoltWintersPredictor() {
		caller = RCallerCreator.getRCaller();
		analizer = new SeasonalityAnalizer();
	}

	public Value[] predict(double[] values, int nrOfValuesToPredict) {
		int period = analizer.calculatePeriod(values);
		synchronized (caller) {
			caller.cleanRCode();
			RCode rcode = new RCode();
			rcode.clear();
			rcode.addRCode("library(forecast)");
			rcode.R_require("forecast");
			rcode.addDoubleArray("rawValues", values);

			boolean logPerformed = false;
			if (MathUtilities.logOperationPossible(values)) {
				rcode.addRCode("rawValues<-log(rawValues)");
				logPerformed = true;
			}
			rcode.addRCode("ser=ts(rawValues,frequency =" + period + ")");

			// rcode.addRCode("lag=frequency(rawValues)");
			// perform modelling
			// beta = FALSE - function will do exponential smoothing
			// gamma=FALSE - non-seasonal model is fitted
			if (period == 1) {
				rcode.addRCode("model<-HoltWinters(ser,beta = FALSE,gamma=FALSE)");
			} else {
				rcode.addRCode("model<-HoltWinters(ser,beta = FALSE)");
			}
			caller.setRCode(rcode);
			caller.runAndReturnResultOnline("model");
			// calculate the predicted values
			String v = "result<-forecast(model," + nrOfValuesToPredict + ")";
			rcode.addRCode(v);
			// project result to matrix
			v = "predDF<-summary(result)";
			rcode.addRCode(v);
			v = "predMatrix<-as.matrix(predDF)";
			rcode.addRCode(v);
			if (logPerformed) {
				v = "predMatrix<-exp(predMatrix)";
				rcode.addRCode(v);
			}
			//remove -Inf/Inf from result if necessary
			v = "predMatrix[predMatrix==Inf]<-.Machine$double.xmax";
			rcode.addRCode(v);
			v = "predMatrix[predMatrix==-Inf]<--.Machine$double.xmax";
			rcode.addRCode(v);
			caller.setRCode(rcode);
			try {
				caller.runAndReturnResultOnline("predMatrix");
				double[][] result = caller.getParser().getAsDoubleMatrix("predMatrix", 5, nrOfValuesToPredict);
				// get data
				Value[] predictedValues = new Value[nrOfValuesToPredict];
				double res, min, max;
				for (int i = 0; i < nrOfValuesToPredict; i++) {
					res = result[0][i];
					min = result[3][i];
					max = result[4][i];
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

	public String getName() {
		return name;
	}
}
