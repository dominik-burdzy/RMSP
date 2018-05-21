package pl.edu.agh.rmsp.analysis.critical;

/**
 * Created by michal on 12.12.2015.
 */
public class TangentCriticalAnalyzer implements ICriticalAnalyzer {

    private double k;

    public TangentCriticalAnalyzer() {
        this(1.0);
    }

    public TangentCriticalAnalyzer(double k) {
        this.k = k;
    }

    public void setK(double k) {
        this.k = k;
    }

    public void setDefault() {
        this.k = 1.0;
    }

    public double getK() {
        return k;
    }

    @Override
    public int analyze(double[] values) {
        double dy = values[values.length - 1] - values[0];
        double dx = values.length;
        if (Math.abs(dy / dx) > k) {
            if (dy > 0) {
                return 1;
            }
            else {
                return -1;
            }
        }
        return 0;
    }

    @Override
    public void setData(double[] values) {
        // ignore
    }
}
