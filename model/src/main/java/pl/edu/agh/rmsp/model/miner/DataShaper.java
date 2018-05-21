package pl.edu.agh.rmsp.model.miner;

public class DataShaper {
	
	public static double NO_DATA = 0.0;
	
	public static double[] getFixedResultSet(double[] set) {
		//Trailing zeros
		set = fixTrailingZeros(set);
			
		//End zeros
		set = fixEndZeros(set);
		
		//Gaps inside
		set = fixGapsInside(set);
		
		return set;
	}
	
	private static double[] fixTrailingZeros(double[] set) {
		if (set[0] == NO_DATA) {
			//Find the first non-zero value
			double fix = NO_DATA;
			for (int i=1; i<set.length; i++){
				if (set[i] != NO_DATA) {
					fix = set[i];
					break;
				}
			}
			//Fill the set
			int i = 0;
			while (i < set.length && set[i] == NO_DATA) {
				set[i] = fix;
				i++;
			}
		}
		return set;
	}
	
	private static double[] fixEndZeros(double[] set) {
		if (set[set.length-1] == NO_DATA) {
			//Find the last non-zero value
			double fix = NO_DATA;
			for (int i=set.length-2; i>=0; i--){
				if (set[i] != NO_DATA) {
					fix = set[i];
					break;
				}
			}
			//Fill the set
			int i = set.length-1;
			while (i >= 0 && set[i] == NO_DATA) {
				set[i] = fix;
				i--;
			}
		}
		return set;
	}
	
	private static double[] fixGapsInside(double[] set) {
		for (int i=1; i<(set.length-1); i++) {
			//Find the gap
			int startIndex = 0;
			int endIndex = 0;
			
			if (set[i] == NO_DATA) {
				//Gap start - first zero
				startIndex = i;
				while (i<(set.length-1) && set[i] == NO_DATA ) {
					endIndex = i;
					i++;
				}				
				//We captured the gap, now lets solve it
				int gapSize = (endIndex - startIndex) + 1;
				double increase = (set[endIndex+1]-set[startIndex-1]) / (gapSize + 1);
				
				for (int j=startIndex; j<=endIndex; j++)
					set[j] = set[j-1] + increase;
			}
		}
		return set;
	}
}
