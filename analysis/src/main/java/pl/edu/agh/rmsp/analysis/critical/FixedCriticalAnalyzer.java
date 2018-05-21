package pl.edu.agh.rmsp.analysis.critical;

/**
 * Created by michal on 12.12.2015.
 */
public class FixedCriticalAnalyzer implements ICriticalAnalyzer {

    private double max = 0;
    private double min = 0;

    private boolean maxSet = false;
    private boolean minSet = false;

    public FixedCriticalAnalyzer() {
    }

    public FixedCriticalAnalyzer(double max) {
        this.max = max;
        this.maxSet = true;
    }

    public FixedCriticalAnalyzer(double max, double min) {
        this.max = max;
        this.min = min;
        this.maxSet = true;
        this.minSet = true;
    }

    public void setMin(double min) {
        this.min = min;
        this.minSet = true;
    }

    public void unsetMin() {
        minSet = false;
    }

    public void setMax(double max) {
        this.max = max;
        this.maxSet = true;
    }

    public void unsetMax() {
        this.maxSet = false;
    }

    public boolean isMinSet() {
        return minSet;
    }

    public boolean isMaxSet() {
        return maxSet;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    @Override
    public int analyze(double[] values) {
        double valuesMax = Double.MIN_VALUE;
        double valuesMin = Double.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > valuesMax) {
                valuesMax = values[i];
            }
            if (values[i] < valuesMin) {
                valuesMin = values[i];
            }
        }
        if (maxSet && valuesMax > this.max) {
            return 1;
        }
        if (minSet && valuesMin < this.min) {
            return -1;
        }
        return 0;
    }

    public void setData(double[] values) {
        // ignore
    }
}
