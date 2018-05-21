package pl.edu.agh.rmsp.analysis.predictors;

import pl.edu.agh.rmsp.analysis.utils.MathUtilities;
import pl.edu.agh.rmsp.analysis.utils.RCallerCreator;
import pl.edu.agh.rmsp.analysis.utils.RCallerWrapper;
import pl.edu.agh.rmsp.analysis.support.SeasonalityAnalizer;
import pl.edu.agh.rmsp.model.commons.Value;
import rcaller.RCode;
import rcaller.exception.ExecutionException;

/*
 * Exponential smoothing state space model with Box-Cox transformation, ARMA errors, Trend and Seasonal components
 */
public class BatsPredictor implements Predictor {

	private static final String name = "Bats Predictor";

	private RCallerWrapper caller;
	private SeasonalityAnalizer analizer;
	private RCode rcode;

	public BatsPredictor() {
		caller = RCallerCreator.getRCaller();
		analizer = new SeasonalityAnalizer();
		rcode = new RCode();
	}

	public Value[] predict(double[] values, int nrOfValuesToPredict) {
		int period = analizer.calculatePeriod(values);
		synchronized (caller) {
			caller.cleanRCode();
			rcode.addRCode("library(forecast)");
			rcode.addDoubleArray("rawValues", values);

			boolean logPerformed = false;
			if (MathUtilities.logOperationPossible(values)) {
				rcode.addRCode("rawValues<-log(rawValues)");
				logPerformed = true;
			}
			// set seasonality
			rcode.addRCode("ser=ts(rawValues,frequency =" + period + ")");

			// perform modelling
			rcode.addRCode("model<-bats(ser,use.box.cox=TRUE,use.parallel=TRUE)");
			// calculate the predicted values
			String v = "result<-forecast(model," + nrOfValuesToPredict + ")";
			rcode.addRCode(v);
			// project result to matrix
			v = "predDF<-summary(result)";
			rcode.addRCode(v);
			v = "predMatrix<-as.matrix(predDF)";
			rcode.addRCode(v);
			// invert log operation

			if (logPerformed) {
				v = "predMatrix<-exp(predMatrix)";
				rcode.addRCode(v);
			}
			// remove -Inf/Inf from result if necessary
			v = "predMatrix[predMatrix==Inf]<-.Machine$double.xmax";
			rcode.addRCode(v);
			v = "predMatrix[predMatrix==-Inf]<--.Machine$double.xmax";
			rcode.addRCode(v);
			caller.setRCode(rcode);
			try {
				caller.runAndReturnResultOnline("predMatrix");
				// get data
				double[][] result = caller.getParser().getAsDoubleMatrix("predMatrix", 5, nrOfValuesToPredict);
				caller.cleanRCode();
				return MathUtilities.convertMatrixToValues(result, nrOfValuesToPredict, 0, 3, 4);
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
