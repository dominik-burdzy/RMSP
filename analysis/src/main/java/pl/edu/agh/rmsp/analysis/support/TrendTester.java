package pl.edu.agh.rmsp.analysis.support;

import pl.edu.agh.rmsp.analysis.utils.RCallerCreator;
import pl.edu.agh.rmsp.analysis.utils.RCallerWrapper;
import rcaller.RCode;
import rcaller.exception.ExecutionException;

public class TrendTester {
	
	private RCallerWrapper caller;

	public TrendTester() {
		caller = RCallerCreator.getRCaller();
	}
	
	/*
	 * perform  Cox Stuart test  test for trend. Returns
	 * p-value of a test, where 0<=p<=1. P close to 0 means that data there is no significant trend. 
	 */
	public double getPValueCoxStuartTest(double[] values) throws ExecutionException{
		double result;
		caller.cleanRCode();
		RCode rcode = new RCode();
		rcode.clear();
		rcode.addRCode("library(randtests)");
		rcode.addDoubleArray("rawValues", values);
		rcode.addRCode("a<-cox.stuart.test(rawValues, alternative= \"two.sided\")");
		rcode.addRCode("p<-a$p.value");
		caller.setRCode(rcode);
		try {
			caller.runAndReturnResultOnline("p");
			result = caller.getParser().getAsDoubleArray("p")[0];
		} catch (ExecutionException e) {
			caller.resetRCaller();
			caller = RCallerCreator.getRCaller();
			throw e;
		} finally {
			rcode.clear();
			caller.cleanRCode();
		}
		return result;
	}

}
