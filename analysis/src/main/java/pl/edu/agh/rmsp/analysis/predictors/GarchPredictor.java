package pl.edu.agh.rmsp.analysis.predictors;

import pl.edu.agh.rmsp.analysis.utils.MathUtilities;
import pl.edu.agh.rmsp.analysis.utils.RCallerCreator;
import pl.edu.agh.rmsp.analysis.utils.RCallerWrapper;
import pl.edu.agh.rmsp.analysis.support.SeasonalityAnalizer;
import pl.edu.agh.rmsp.model.commons.Value;
import rcaller.RCode;
import rcaller.exception.ExecutionException;

/*
 * ARCH model is a model for the variance of a time series. ARCH models are used to describe a changing, possibly volatile variance, GARCH is generalization of ARCH
 */
public class GarchPredictor implements Predictor {

	private static final String name = "Garch Predictor";

	private RCallerWrapper caller;
	private SeasonalityAnalizer analizer;

	public GarchPredictor() {
		caller = RCallerCreator.getRCaller();
		analizer = new SeasonalityAnalizer();
	}

	public Value[] predict(double[] values, int nrOfValuesToPredict) {
		int period = analizer.calculatePeriod(values);
		synchronized (caller) {
			caller.cleanRCode();
			RCode rcode = new RCode();
			rcode.clear();
			rcode.addRCode("library(fGarch)");
			rcode.addRCode("library(forecast)");
			rcode.addDoubleArray("rawValues", values);
			rcode.addRCode("ser=ts(rawValues,frequency =" + period + ")");

			boolean logPerformed = false;
			if (MathUtilities.logOperationPossible(values)) {
				rcode.addRCode("rawValues<-log(rawValues)");
				logPerformed = true;
			}

			// perform modelling
			// rcode.addRCode("model<-garchFit(~arma(0, 2) + garch(1, 1), data =
			// logValues, trace = FALSE)");
		//	rcode.addRCode("armod<-auto.arima(ser)");
		//	rcode.addRCode("s<-substitute(~arma(p,q)+garch(1,1), list(p=armod$arma[1], q=armod$arma[2]))");
			rcode.addRCode("model<-garchFit(data = rawValues)");
			// calculate the predicted values
			String v = "result<-predict(model, n.ahead=" + nrOfValuesToPredict + ", plot=TRUE)";
			rcode.addRCode(v);
			// invert log operation
			v = "predMatrix<-as.matrix(result)";
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
