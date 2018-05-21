package pl.edu.agh.rmsp.analysis.critical;

/**
 * Created by michal on 12.12.2015.
 */
public class AvgCriticalAnalyzer implements ICriticalAnalyzer {
    private double[] data;
    private double avg;
    private double variance;

    private double k;

    public AvgCriticalAnalyzer() {
        this(1.0);
    }

    public AvgCriticalAnalyzer(double k) {
        this.k = k;
    }

    public void setK(double k) {
        this.k = k;
    }

    public void setDefault() {
        this.k = 1.0;
    }

    public double getHighLimit() {
        return avg + k * variance;
    }

    public double getLowLimit() {
        return avg - k * variance;
    }

    @Override
    public int analyze(double[] values) {
        boolean max = false;
        boolean min = false;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > avg + k * variance) {
                max = true;
            }
            if (values[i] < avg - k * variance) {
                min = true;
            }
        }
        if (max) {
            return 1;
        }
        if (min) {
            return -1;
        }
        return 0;
    }

    @Override
    public void setData(double[] values) {
        this.data = values;
        this.avg = 0;
        this.variance = 0;
        for (int i = 0; i < data.length; i++) {
            avg += data[i];
        }
        avg /= data.length;
        for (int i = 0; i < data.length; i++) {
            variance += (data[i] - avg) * (data[i] - avg);
        }
        variance /= data.length;
    }
}
