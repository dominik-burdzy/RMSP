package pl.edu.agh.rmsp.analysis.critical;

/**
 * Created by michal on 12.12.2015.
 */
public interface ICriticalAnalyzer {
    int analyze(double[] values);
    void setData(double[] values);
}
