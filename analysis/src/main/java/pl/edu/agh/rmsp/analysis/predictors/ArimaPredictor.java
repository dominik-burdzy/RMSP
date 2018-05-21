package pl.edu.agh.rmsp.analysis.predictors;

import pl.edu.agh.rmsp.analysis.utils.MathUtilities;
import pl.edu.agh.rmsp.analysis.utils.RCallerCreator;
import pl.edu.agh.rmsp.analysis.utils.RCallerWrapper;
import pl.edu.agh.rmsp.analysis.support.SeasonalityAnalizer;
import pl.edu.agh.rmsp.model.commons.Value;
import rcaller.RCode;
import rcaller.exception.ExecutionException;

/*
 *  Predictor used especially for stationary series. Method auto.arima ensures stationarization of series and it tries to fit even if input series is not stationary
 */
public class ArimaPredictor implements Predictor {

	private static final String name = "Arima Predictor";

	private RCallerWrapper caller;
	private SeasonalityAnalizer analizer;

	public ArimaPredictor() {
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
			// perform modelling
			// stepwise -If TRUE, will do stepwise selection (faster).
			// Otherwise, it
			// searches over all models. Non-stepwise selection can be very
			// slow,
			// especially for seasonal models.

			// approximation- If TRUE, estimation is via conditional sums of
			// squares
			// andthe information criteria used for model selection are
			// approximated. The final model is still computed using maximum
			// likelihood estimation. Approximation should be used for long time
			// series or a high seasonal period to avoid excessive computation
			// times.

			// seasonal-If FALSE, restricts search to non-seasonal models.

			// max.order-Maximum value of p+q+P+Q if model selection is not
			// stepwise.
			// lambda Box-Cox transformation parameter. Ignored if NULL.
			// Otherwise,
			// data transformed before model is estimated
			// rcode.addRCode("model<-auto.arima(ser,stepwise=FALSE,
			// approximation=FALSE)");

			// with seasonality:
			rcode.addRCode("ser=ts(rawValues,frequency =" + period + ")");
			rcode.addRCode("model<-auto.arima(ser)");// ,stepwise=FALSE,approximation=FALSE,max.order=10,lambda=1)");

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
