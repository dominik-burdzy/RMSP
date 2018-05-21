package pl.edu.agh.rmsp.rating;

import java.util.ArrayList;

import javafx.scene.control.ProgressBar;

public class ProgressbarController {

	private int[] testCounter = { 0, 0, 0, 0 };
	private ArrayList<ProgressBar> testProgress;
	private int[] testMax = new int[4];
	private static ProgressbarController instance;

	private ProgressbarController(int[] testCounter, ArrayList<ProgressBar> testProgress, int[] testMax) {
		super();
		this.testCounter = testCounter;
		this.testProgress = testProgress;
		this.testMax = testMax;
	}

	public static void init(int[] testCounter, ArrayList<ProgressBar> testProgress, int[] testMax) {
		instance = new ProgressbarController(testCounter, testProgress, testMax);
	}

	public static ProgressbarController getInstance() {
		return instance;
	}

	public synchronized void testIncrement(int chartId) {
		testCounter[chartId]++;
		testProgress.get(chartId).setProgress((double) testCounter[chartId] / (double) testMax[chartId]);
	}
}
