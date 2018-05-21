package pl.edu.agh.rmsp.model.commons;

public class Value {

	double value;
	double confidenceMaxValue;
	double confidenceMinValue;

	public Value() {
	}

	public Value(double value) {
		super();
		this.value = value;
	}

	public Value(double value, double confidenceMaxValue,
			double confidenceMinValue) {
		super();
		this.value = value;
		this.confidenceMaxValue = confidenceMaxValue;
		this.confidenceMinValue = confidenceMinValue;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getConfidenceMaxValue() {
		return confidenceMaxValue;
	}

	public void setConfidenceMaxValue(double confidenceMaxValue) {
		this.confidenceMaxValue = confidenceMaxValue;
	}

	public double getConfidenceMinValue() {
		return confidenceMinValue;
	}

	public void setConfidenceMinValue(double confidenceMinValue) {
		this.confidenceMinValue = confidenceMinValue;
	}

}
