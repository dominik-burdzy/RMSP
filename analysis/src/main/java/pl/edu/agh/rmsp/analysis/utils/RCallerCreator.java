package pl.edu.agh.rmsp.analysis.utils;

import pl.edu.agh.rmsp.model.db.DatabaseConfigurator;
import rcaller.RCaller.FailurePolicy;

public class RCallerCreator {

	public static RCallerWrapper getRCaller() {
		RCallerWrapper caller = RCallerWrapper.getInstance();
		setCaller(caller);		
		return caller;
	}
	
	public static void setCaller(RCallerWrapper caller){
		DatabaseConfigurator configurator = new DatabaseConfigurator();

		caller.setRExecutable(configurator.getRExePath());
		caller.setRscriptExecutable(configurator.getRScriptPath());
		caller.setMaxWaitTime(5000); // max time in milliseconds before killing
										// thread
		caller.setFailurePolicy(FailurePolicy.RETRY_1);
	}

}
