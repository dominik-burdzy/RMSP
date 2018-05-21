package pl.edu.agh.rmsp.analysis.support;

import pl.edu.agh.rmsp.analysis.utils.RCallerCreator;
import pl.edu.agh.rmsp.analysis.utils.RCallerWrapper;
import rcaller.RCode;
import rcaller.exception.ExecutionException;

/*
 * 	Class used for testing stationarity of data
 */
public class StationarityTester {

	private RCallerWrapper caller;

	public StationarityTester() {
		caller = RCallerCreator.getRCaller();
	}

	/*
	 * perform Augmented Dickey-Fuller test for stationarity of data. Returns
	 * p-value of a test, where 0<=p<=1. P close to 0 means that data is
	 * stationary, close to 1-non-stationary
	 */
	public double getPValueFromADFTest(double[] values) throws ExecutionException {
		synchronized (caller) {
			double result;
			caller.cleanRCode();
			RCode rcode = new RCode();
			rcode.clear();
			rcode.addRCode("library(tseries)");
			rcode.addDoubleArray("rawValues", values);
			rcode.addRCode("a<-adf.test(rawValues,alternative = \"stationary\")");
			rcode.addRCode("p<-a$p.value");
			caller.setRCode(rcode);
			try {
				caller.runAndReturnResultOnline("p");
				result = caller.getParser().getAsDoubleArray("p")[0];
			} catch (ExecutionException e) {
				caller.resetRCaller();
				throw e;
			} finally {
				rcode.clear();
				caller.cleanRCode();
			}
			return result;
		}
	}
}
