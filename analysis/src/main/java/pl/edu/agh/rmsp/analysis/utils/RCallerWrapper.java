package pl.edu.agh.rmsp.analysis.utils;

import rcaller.RCaller;
import rcaller.RCode;
import rcaller.ROutputParser;

/**
 * Created by michal on 09.11.2015.
 */
public class RCallerWrapper {
    private static RCallerWrapper ourInstance = new RCallerWrapper();
    private static RCaller caller;
    private boolean seasonalityFunctionDefined=false;
    
    public static RCallerWrapper getInstance() {
        return ourInstance;
    }

    private RCallerWrapper() {
        caller = new RCaller();
    }

    public void setRExecutable(String path) {
        caller.setRExecutable(path);
    }

    public void setRscriptExecutable(String path) {
        caller.setRscriptExecutable(path);
    }

    public void setMaxWaitTime(long time) {
        caller.setMaxWaitTime(time);
    }

    public void setFailurePolicy(RCaller.FailurePolicy policy) {
        caller.setFailurePolicy(policy);
    }

    public synchronized void cleanRCode() {
        caller.cleanRCode();
    }

    public synchronized void setRCode(RCode rCode) {
        caller.setRCode(rCode);
    }

    public synchronized void runAndReturnResultOnline(String str) {
        caller.runAndReturnResultOnline(str);
    }

    public synchronized ROutputParser getParser() {
        return caller.getParser();
    }
    
    public synchronized void resetRCaller() {
        caller.stopStreamConsumers();
        caller = new RCaller();
        RCallerCreator.setCaller(this);
    }

	public boolean isSeasonalityFunctionDefined() {
		return seasonalityFunctionDefined;
	}

	public void setSeasonalityFunctionDefined(boolean seasonalityFunctionDefined) {
		this.seasonalityFunctionDefined = seasonalityFunctionDefined;
	}
    
    

}
