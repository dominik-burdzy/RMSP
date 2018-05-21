package pl.edu.agh.rmsp.analysis.support;

import pl.edu.agh.rmsp.analysis.utils.RCallerCreator;
import pl.edu.agh.rmsp.analysis.utils.RCallerWrapper;
import rcaller.RCode;
import rcaller.exception.ExecutionException;

/*
 * Class used to calculate time series period (frequency)
 */
public class SeasonalityAnalizer {

	private RCallerWrapper caller;

	public SeasonalityAnalizer() {
		caller = RCallerCreator.getRCaller();
		defineFreqFunctionInR();
	}

	public int calculatePeriod(double[] values) {
		synchronized (caller) {
			if (!caller.isSeasonalityFunctionDefined()) {
				defineFreqFunctionInR();
				if(!caller.isSeasonalityFunctionDefined()){
					return 1;
				}
			}
			caller.cleanRCode();
			RCode rcode = new RCode();
			rcode.addDoubleArray("x", values);
			rcode.addRCode("p<-find.freq(x)");
			caller.setRCode(rcode);
			try {
				caller.runAndReturnResultOnline("p");
				double result = caller.getParser().getAsDoubleArray("p")[0];
				return (int) result;
			} catch (ExecutionException e) {
				caller.resetRCaller();
				caller = RCallerCreator.getRCaller();
				return 1;
			} finally {
				rcode.clear();
				caller.cleanRCode();
			}
		}
	}

	private void defineFreqFunctionInR() {
		RCode code = new RCode();
		code.addRCode("find.freq <- function(x)\n" + "{\n" + "  n <- length(x)\n"
				+ "  spec <- spec.ar(c(na.contiguous(x)),plot=FALSE)\n" + "  if(max(spec$spec)>10) \n" + "  {\n"
				+ "    period <- round(1/spec$freq[which.max(spec$spec)])\n"
				+ "    if(period==Inf) # Find next local maximum\n" + "    {\n"
				+ "      j <- which(diff(spec$spec)>0)\n" + "      if(length(j)>0)\n" + "      {\n"
				+ "        nextmax <- j[1] + which.max(spec$spec[j[1]:500])\n"
				+ "        if(nextmax <= length(spec$freq))\n" + "          period <- round(1/spec$freq[nextmax])\n"
				+ "        else\n" + "          period <- 1\n" + "      }\n" + "      else\n" + "        period <- 1\n"
				+ "    }\n" + "  }\n" + "  else\n" + "    period <- 1\n" + "  return(period)\n" + "}\n");
		synchronized (caller) {
			caller.setRCode(code);
			try {
				caller.runAndReturnResultOnline("find.freq");
				caller.setSeasonalityFunctionDefined(true);
			} catch (ExecutionException e) {
				caller.resetRCaller();
				caller.setSeasonalityFunctionDefined(false);
				throw e;
			} finally {
				code.clear();
				caller.cleanRCode();
			}
		}
	}

}
