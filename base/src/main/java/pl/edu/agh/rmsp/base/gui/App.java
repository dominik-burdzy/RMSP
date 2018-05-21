package pl.edu.agh.rmsp.base.gui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javafx.scene.control.*;
import org.joda.time.DateTime;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import pl.edu.agh.rmsp.analysis.critical.AvgCriticalAnalyzer;
import pl.edu.agh.rmsp.analysis.critical.FixedCriticalAnalyzer;
import pl.edu.agh.rmsp.analysis.critical.TangentCriticalAnalyzer;
import pl.edu.agh.rmsp.analysis.neural.NeuralPredictor;
import pl.edu.agh.rmsp.analysis.predictors.ArimaPredictor;
import pl.edu.agh.rmsp.analysis.predictors.BatsPredictor;
import pl.edu.agh.rmsp.analysis.predictors.GarchPredictor;
import pl.edu.agh.rmsp.analysis.predictors.HoltWintersPredictor;
import pl.edu.agh.rmsp.analysis.predictors.Predictor;
import pl.edu.agh.rmsp.analysis.predictors.RegressionPredictor;
import pl.edu.agh.rmsp.base.testing.TestManager;
import pl.edu.agh.rmsp.model.commons.TimePeriod;
import pl.edu.agh.rmsp.model.commons.Value;
import pl.edu.agh.rmsp.model.db.DatabaseConfigurator;
import pl.edu.agh.rmsp.model.db.DatabaseManager;
import pl.edu.agh.rmsp.rating.ProgressbarController;
import pl.edu.agh.rmsp.service.DecisionService;

@SuppressWarnings("restriction")
public class App extends Application implements Initializable {

	private int currentNumberOfCharts;

	private Stage primaryStage;
	private Scene scene;
	private ArrayList<ChartThread> chartThreads;
	private DatabaseManager manager;

	private ArrayList<Double> criticalValueMin, criticalValueMax;
	private ArrayList<Boolean> criticalAlertMin, criticalAlertMax;

	private Background normalBackground = null;
	private Background criticalBackground;

	private int[] testCounter = {0,0,0,0};
	private int[] testMax = new int[4];


	@FXML
	private ArrayList<CheckBox> fixedCrit;
	@FXML
	private ArrayList<CheckBox> derivativeCrit;
	@FXML
	private ArrayList<CheckBox> varianceCrit;
	@FXML
	private ArrayList<TextField> derivativeCritValue;
	@FXML
	private ArrayList<TextField> varianceCritValue;

	private FixedCriticalAnalyzer[] fixedCriticalAnalyzers;
	private TangentCriticalAnalyzer[] tangentCriticalAnalyzers;
	private AvgCriticalAnalyzer[] avgCriticalAnalyzers;

	@FXML
	private ArrayList<TextField> testResult;

	@FXML
	private ArrayList<TextField> testRecords;

	@FXML
	private ArrayList<ProgressBar> testProgress;

	@FXML
	private ArrayList<CheckBox> testArima;

	@FXML
	private ArrayList<CheckBox> testBats;

	@FXML
	private ArrayList<CheckBox> testGarch;

	@FXML
	private ArrayList<CheckBox> testHoltWinters;

	@FXML
	private ArrayList<CheckBox> testRegression;

	@FXML
	private ArrayList<CheckBox> testNeural;

	@FXML
	private ArrayList<Slider> rangeSlider;

	@FXML
	private Button applyAll1, applyAll2, applyAll3, applyAll4, stop1, stop2, stop3, stop4, continue1, continue2,
			continue3, continue4, reset1, reset2, reset3, reset4, testStart1, testStart2, testStart3, testStart4;
	@FXML
	private Label statusLabel;

	@FXML
	private ArrayList<Label> algorithmNameLabel;

	@FXML
	private ArrayList<TextField> predictionSettingsProbes, predictionSettingsToPredict, predictionSettingsUpdate,
			critMin, critMax, nSetSize, nLearningRate, nMomentum, nMaxIter, nMaxErrorInc, nMaxError,
			nOfInitialNetworkDataRecords;

	@FXML
	private ArrayList<ChoiceBox<TimeUnit>> predictionSettingsProbesTimeUnitChoiceBox,
			predictionSettingsUpdateTimeUnitChoiceBox;

	@FXML
	private TextField rScriptPath, rExePath;

	@FXML
	private ArrayList<ChoiceBox<String>> resourceChoiceBox;

	@FXML
	ArrayList<ChoiceBox<String>> algorithmChoiceBox;

	@FXML
	private ArrayList<LineChart<Number, Number>> lineChart;

	@FXML
	private RadioMenuItem radio1, radio2, radio3, radio4;

	@FXML
	private MenuItem menuSettings;

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab tab1, tab2;

	@FXML
	private ArrayList<AnchorPane> anchor;

	private TimeUnit[] timeUnits = new TimeUnit[4];

	private DecisionService service;

	public void showTestResults(int chartId, Map<String, Double> results) {
		String bestMethod = null;
		double bestResult = 0;
		for (Map.Entry<String, Double> entry : results.entrySet()) {
			if (entry.getValue() > bestResult) {
				bestResult = entry.getValue();
				bestMethod = entry.getKey();
			}
		}
		testResult.get(chartId).setText(bestMethod.split(" ")[0]);
	}

	// CODE BELOW NEEDS TO BE UPDATED
	// SEE ALL 'TODO' SECTIONS

	private void initAction(int chartId, Boolean isUpdate) throws IOException, ParseException {
		// Resource
		String resource = resourceChoiceBox.get(chartId).getSelectionModel().getSelectedItem();

		// Algorithm
		String algorithm = algorithmChoiceBox.get(chartId).getSelectionModel().getSelectedItem();

		Boolean isRPathValid = true;
		try {
				algorithmNameLabel.get(chartId).setText(algorithm);
				Predictor predictor = this.getPredictor(chartId,
						(String) this.algorithmChoiceBox.get(chartId).getSelectionModel().getSelectedItem());
		} catch (Exception ex) {
			isRPathValid = false;
		}

		if (isRPathValid) {
			statusLabel.setText("");

			// Prediction settings
			int nrOfProbes = Integer.parseInt(predictionSettingsProbes.get(chartId).getText());
			int nrOfValuesToPredict = Integer.parseInt(predictionSettingsToPredict.get(chartId).getText());
			int updateSecond = Integer.parseInt(predictionSettingsUpdate.get(chartId).getText());

			TimeUnit probesUnit = predictionSettingsProbesTimeUnitChoiceBox.get(chartId).getSelectionModel()
					.getSelectedItem();
			timeUnits[chartId] = probesUnit;
			TimeUnit updateUnit = predictionSettingsUpdateTimeUnitChoiceBox.get(chartId).getSelectionModel()
					.getSelectedItem();

			TimePeriod predictionTimePeriod = new TimePeriod(probesUnit, nrOfProbes);
			TimePeriod updatingTimePeriod = new TimePeriod(updateUnit, updateSecond);

			// Critical values
			try {
				fixedCriticalAnalyzers[chartId].setMin(Double.parseDouble(critMin.get(chartId).getText()));
			} catch (Exception e) {
				fixedCriticalAnalyzers[chartId].unsetMin();
			}

			try {
				fixedCriticalAnalyzers[chartId].setMax(Double.parseDouble(critMax.get(chartId).getText()));
			} catch (Exception e) {
				fixedCriticalAnalyzers[chartId].unsetMax();
			}

			try {
				tangentCriticalAnalyzers[chartId]
						.setK(Double.parseDouble(derivativeCritValue.get(chartId).getText()));
			} catch (Exception e) {
				tangentCriticalAnalyzers[chartId].setDefault();
				derivativeCritValue.get(chartId).setText("1.0");
			}

			try {
				avgCriticalAnalyzers[chartId].setK(Double.parseDouble(varianceCritValue.get(chartId).getText()));
			} catch (Exception e) {
				avgCriticalAnalyzers[chartId].setDefault();
				varianceCritValue.get(chartId).setText("1.0");
			}

			// Update chart
			if (isUpdate) {
				try {
					chartThreads.get(chartId).stopThread();
					chartThreads.get(chartId).join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				chartThreads.set(chartId, new ChartThread(this, chartId, resource));
				chartThreads.get(chartId).setName("chartThread");
				chartThreads.get(chartId).setData(predictionTimePeriod, updatingTimePeriod, nrOfValuesToPredict);
				chartThreads.get(chartId).start();
			} else {
				chartThreads.add(chartId, new ChartThread(this, chartId, resource));
				chartThreads.get(chartId).setName("chartThread");
				chartThreads.get(chartId).setData(predictionTimePeriod, updatingTimePeriod, nrOfValuesToPredict);
				chartThreads.get(chartId).start();

				currentNumberOfCharts++;
			}
		} else {
			statusLabel.setText("ERROR: Invalid R Path. Go to Settings tab and setup it correctly");
		}
	}

	private void neuralStop(int chartId) {
		chartThreads.get(chartId).pauseNeuralLearning();
	}

	private void neuralContinue(int chartId) {
		chartThreads.get(chartId).continueNeuralLearning();
	}

	private void neuralReset(int chartId) {
		chartThreads.get(chartId).resetNeuralLearning();
	}

	public Predictor getPredictor(int chartId, String name) throws NumberFormatException, IOException, ParseException {
		if (name.equals("Regression")) {
			return new RegressionPredictor();
		} else if (name.equals("Arima")) {
			return new ArimaPredictor();
		} else if (name.equals("Bats")) {
			return new BatsPredictor();
		} else if (name.equals("Garch")) {
			return new GarchPredictor();
		} else if (name.equals("Neural network")) {
			return new NeuralPredictor(Integer.parseInt(predictionSettingsProbes.get(chartId).getText()));
		} else {
			return new HoltWintersPredictor();
		}
	}

	// APP CODE BELOW

	private void saveSettings() {
		DatabaseConfigurator configurator = new DatabaseConfigurator();
		configurator.setRExePath(rExePath.getText());
		configurator.setRScriptPath(rScriptPath.getText());
		try {
			for (int i = 0; i < currentNumberOfCharts; i++)
				initAction(i, true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private String toDateString(DateTime dateTime, int i, TimeUnit unit) {
		if (unit == TimeUnit.SECONDS) {
			return dateTime.plusSeconds(i).toString("HH:mm:ss");
		} else if (unit == TimeUnit.MINUTES) {
			return dateTime.plusMinutes(i).toString("HH:mm");
		} else if (unit == TimeUnit.HOURS) {
			return dateTime.plusHours(i).toString("dd.MM HH:mm");
		} else {
			return dateTime.plusDays(i).toString("dd.MM.yyyy");
		}
	}

	public void setPredictedValues(int chartId, final Value[] prediction, final double[] values) {

		Platform.runLater(new Runnable() {
			public void run() {
				if (normalBackground == null) {
					normalBackground = critMin.get(chartId).getBackground();
					criticalBackground = new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
				}
				double range = (rangeSlider.get(chartId).getValue() - rangeSlider.get(chartId).getMin()) /
						(rangeSlider.get(chartId).getMax() - rangeSlider.get(chartId).getMin());
				int[] analysis = { 0, 0, 0 };
				double[] predicted = new double[prediction.length];
				double[] toAnalyze = new double[(int)(prediction.length * range)];
				for (int i = 0; i < predicted.length; i++) {
					predicted[i] = prediction[i].getValue();
				}
				for (int i = 0; i < (int) (prediction.length * range); i++) {
					toAnalyze[i] = prediction[i].getValue();
				}
				if ((int)(prediction.length * range) != 0) {
					if (fixedCrit.get(chartId).isSelected()) {
						fixedCriticalAnalyzers[chartId].setData(values);
						analysis[0] = fixedCriticalAnalyzers[chartId].analyze(toAnalyze);
					}
					if (derivativeCrit.get(chartId).isSelected()) {
						tangentCriticalAnalyzers[chartId].setData(values);
						analysis[1] = tangentCriticalAnalyzers[chartId].analyze(toAnalyze);
					}
					if (varianceCrit.get(chartId).isSelected()) {
						avgCriticalAnalyzers[chartId].setData(values);
						analysis[2] = avgCriticalAnalyzers[chartId].analyze(toAnalyze);
					}
				}

				if (analysis[0] > 0) {
					critMax.get(chartId).setBackground(criticalBackground);
					critMax.get(chartId).setStyle("-fx-text-fill: white");
					criticalValueMax.set(chartId, fixedCriticalAnalyzers[chartId].getMax());
				} else if (analysis[0] < 0) {
					critMin.get(chartId).setBackground(criticalBackground);
					critMin.get(chartId).setStyle("-fx-text-fill: white");
					criticalValueMin.set(chartId, fixedCriticalAnalyzers[chartId].getMin());
				} else {
					critMin.get(chartId).setBackground(normalBackground);
					critMin.get(chartId).setStyle("-fx-text-fill: black");
					critMax.get(chartId).setBackground(normalBackground);
					critMax.get(chartId).setStyle("-fx-text-fill: black");
				}

				if (analysis[1] != 0) {
					derivativeCritValue.get(chartId).setBackground(criticalBackground);
					derivativeCritValue.get(chartId).setStyle("-fx-text-fill: white");
				} else {
					derivativeCritValue.get(chartId).setBackground(normalBackground);
					derivativeCritValue.get(chartId).setStyle("-fx-text-fill: black");
				}

				if (analysis[2] != 0) {
					varianceCritValue.get(chartId).setBackground(criticalBackground);
					varianceCritValue.get(chartId).setStyle("-fx-text-fill: white");
				} else {
					varianceCritValue.get(chartId).setBackground(normalBackground);
					varianceCritValue.get(chartId).setStyle("-fx-text-fill: black");
				}

				for (int i = 0; i < 3; i++) {
					if (analysis[i] > 0) {
						criticalAlertMax.set(chartId, true);
					}
					if (analysis[i] < 0)
						criticalAlertMin.set(chartId, true);
				}
				int result = 0;
				for (int i=0;i<analysis.length;i++) {
					if (analysis[i] > 0) {
						result = 1;
						break;
					} else if (analysis[i] < 0) {
						result = -1;
					}
				}
				service.setDecision(result);

				final LineChart chart = lineChart.get(chartId);

				XYChart.Series valueSeries = new XYChart.Series();
				XYChart.Series confidenceMinSeries = new XYChart.Series();
				XYChart.Series confidenceMaxSeries = new XYChart.Series();
				XYChart.Series realValueSeries = new XYChart.Series();

				valueSeries.setName("Prediction");
				confidenceMinSeries.setName("Confidence min");
				confidenceMaxSeries.setName("Confidence max");
				realValueSeries.setName("Values");

				int nrOfProbes = Integer.parseInt(predictionSettingsProbes.get(chartId).getText());

				DateTime base = DateTime.now();
				if (timeUnits[chartId] == TimeUnit.SECONDS) {
					base = base.minusSeconds(nrOfProbes);
				} else if (timeUnits[chartId] == TimeUnit.MINUTES) {
					base = base.minusMinutes(nrOfProbes);
				} else if (timeUnits[chartId] == TimeUnit.HOURS) {
					base = base.minusHours(nrOfProbes);
				} else if (timeUnits[chartId] == TimeUnit.DAYS) {
					base = base.minusDays(nrOfProbes);
				}

				for (int i = 0; i < values.length; i++) {
					realValueSeries.getData()
							.add(new XYChart.Data(toDateString(base, i, timeUnits[chartId]), values[i]));
				}

				// for (int i = 0; i < realValues.length; i++) {
				// realValueSeries.getData().add(
				// new XYChart.Data(toDateString(base, values.length + i,
				// timeUnits[chartId]), realValues[i]));
				// }
				// }

				for (int i = 0; i < prediction.length; i++) {
					valueSeries.getData().add(new XYChart.Data(
							toDateString(base, i + values.length, timeUnits[chartId]), prediction[i].getValue()));

					confidenceMinSeries.getData()
							.add(new XYChart.Data(toDateString(base, i + values.length, timeUnits[chartId]),
									prediction[i].getConfidenceMinValue()));

					confidenceMaxSeries.getData()
							.add(new XYChart.Data(toDateString(base, i + values.length, timeUnits[chartId]),
									prediction[i].getConfidenceMaxValue()));

				}

				chart.getData().clear();
				chart.getData().add(realValueSeries);
				chart.getData().add(valueSeries);
				chart.getData().add(confidenceMinSeries);
				chart.getData().add(confidenceMaxSeries);

				if (analysis[0] < 0) {
					XYChart.Series alertSeries = new XYChart.Series();
					alertSeries.setName("Critical minimum");
					alertSeries.getData().add(new XYChart.Data(toDateString(base, values.length, timeUnits[chartId]),
							criticalValueMin.get(chartId)));
					alertSeries.getData().add(new XYChart.Data(toDateString(base, values.length + (int)(range * (prediction.length - 1)), timeUnits[chartId]),
							criticalValueMin.get(chartId)));
					chart.getData().add(alertSeries);
				}

				if (analysis[0] > 0) {
					XYChart.Series alertSeries = new XYChart.Series();
					alertSeries.setName("Critical maximum");
					alertSeries.getData().add(new XYChart.Data(toDateString(base, values.length, timeUnits[chartId]),
							criticalValueMax.get(chartId)));
					alertSeries.getData().add(new XYChart.Data(toDateString(base, values.length + (int) (range * (prediction.length - 1)), timeUnits[chartId]),
							criticalValueMax.get(chartId)));
					chart.getData().add(alertSeries);
				}

				if (analysis[1] != 0) {
					XYChart.Series alertSeries = new XYChart.Series();
					alertSeries.setName("Critical tangent");
					alertSeries.getData().add(new XYChart.Data(toDateString(base, values.length, timeUnits[chartId]),
							prediction[0].getValue()));
					alertSeries.getData()
							.add(new XYChart.Data(
									toDateString(base, values.length + (int)(range * (prediction.length - 1)), timeUnits[chartId]),
									prediction[0].getValue()
											+ prediction.length * tangentCriticalAnalyzers[chartId].getK()));
					chart.getData().add(alertSeries);
					XYChart.Series alertSeries2 = new XYChart.Series();
					alertSeries2.setName("Critical tangent");
					alertSeries2.getData().add(new XYChart.Data(toDateString(base, values.length, timeUnits[chartId]),
							prediction[0].getValue()));
					alertSeries2.getData()
							.add(new XYChart.Data(
									toDateString(base, values.length + (int)(range * (prediction.length - 1)), timeUnits[chartId]),
									prediction[0].getValue()
											- prediction.length * tangentCriticalAnalyzers[chartId].getK()));
					chart.getData().add(alertSeries2);
				}

				if (analysis[2] != 0) {
					XYChart.Series alertSeries = new XYChart.Series();
					alertSeries.setName("Critical variance");
					alertSeries.getData().add(new XYChart.Data(toDateString(base, values.length, timeUnits[chartId]),
							avgCriticalAnalyzers[chartId].getLowLimit()));
					alertSeries.getData()
							.add(new XYChart.Data(
									toDateString(base, values.length + (int)(range * (prediction.length - 1)), timeUnits[chartId]),
									avgCriticalAnalyzers[chartId].getLowLimit()));
					chart.getData().add(alertSeries);
					XYChart.Series alertSeries2 = new XYChart.Series();
					alertSeries2.setName("Critical variance");
					alertSeries2.getData().add(new XYChart.Data(toDateString(base, values.length, timeUnits[chartId]),
							avgCriticalAnalyzers[chartId].getHighLimit()));
					alertSeries2.getData()
							.add(new XYChart.Data(
									toDateString(base, values.length + (int)(range * (prediction.length - 1)), timeUnits[chartId]),
									avgCriticalAnalyzers[chartId].getHighLimit()));
					chart.getData().add(alertSeries2);
				}
			}
		});
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;

		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		primaryStage.setTitle("RMSP");
		Parent root = FXMLLoader.load(loader.getResource("Scene.fxml"));

		scene = new Scene(root);
		scene.getStylesheets().add("app.css");

		primaryStage.setScene(scene);

		primaryStage.getIcons().add(new Image(loader.getResourceAsStream("app.png")));
		primaryStage.show();

	}

	public static void main(String[] args) {
		launch(args);
	}

	@FXML
	protected void handleSaveButtonAction(ActionEvent evt) {
		try {
			saveSettings();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		// initialize your logic here: all @FXML variables will have been
		// injected

		final App app = this;
		chartThreads = new ArrayList<ChartThread>();
		manager = new DatabaseManager();

		// critical values startup
		arrayInitalize();

		appInitalize(0);
		appInitalize(1);
		appInitalize(2);
		appInitalize(3);
		
		ProgressbarController.init(testCounter, testProgress, testMax);
		
		// Settings
		DatabaseConfigurator configurator = new DatabaseConfigurator();
		rScriptPath.setText(configurator.getRScriptPath());
		rExePath.setText(configurator.getRExePath());

		// Now init the Action with default no of charts = 1
		try {
			initAction(0, false);

		} catch (NumberFormatException | IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	private void appInitalize(int chartId) {
		service = DecisionService.getInstance();
		fixedCriticalAnalyzers = new FixedCriticalAnalyzer[4];
		tangentCriticalAnalyzers = new TangentCriticalAnalyzer[4];
		avgCriticalAnalyzers = new AvgCriticalAnalyzer[4];
		for (int i = 0; i < 4; i++) {
			fixedCriticalAnalyzers[i] = new FixedCriticalAnalyzer();
			tangentCriticalAnalyzers[i] = new TangentCriticalAnalyzer();
			avgCriticalAnalyzers[i] = new AvgCriticalAnalyzer();
		}
		lineChart.get(chartId).setCreateSymbols(false);
		lineChart.get(chartId).setAnimated(false);

		algorithmChoiceBox.get(chartId).setItems(FXCollections.observableArrayList("Regression", "Arima", "Bats",
				"Garch", "HoltWinters", "Neural network"));
		LinkedList<String> resources = manager.getResources();
		resourceChoiceBox.get(chartId)
				.setItems(FXCollections.observableArrayList(resources.toArray(new String[resources.size()])));

		ArrayList<TimeUnit> timeUnitsList = new ArrayList<TimeUnit>();
		timeUnitsList.addAll(Arrays.asList(TimeUnit.values()));
		timeUnitsList.remove(TimeUnit.MICROSECONDS);
		timeUnitsList.remove(TimeUnit.MILLISECONDS);
		timeUnitsList.remove(TimeUnit.NANOSECONDS);

		predictionSettingsProbesTimeUnitChoiceBox.get(chartId)
				.setItems(FXCollections.observableArrayList(timeUnitsList));
		predictionSettingsUpdateTimeUnitChoiceBox.get(chartId)
				.setItems(FXCollections.observableArrayList(timeUnitsList));

		// Setup initial values:
		predictionSettingsProbes.get(chartId).setText("20");
		predictionSettingsToPredict.get(chartId).setText("6");
		predictionSettingsUpdate.get(chartId).setText("1");
		algorithmChoiceBox.get(chartId).getSelectionModel().selectFirst();
		resourceChoiceBox.get(chartId).getSelectionModel().selectFirst();
		predictionSettingsProbesTimeUnitChoiceBox.get(chartId).getSelectionModel().selectFirst();
		predictionSettingsUpdateTimeUnitChoiceBox.get(chartId).getSelectionModel().selectFirst();
		initNeuralDefaults(chartId);
	}

	private void initNeuralDefaults(int chartId) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties prop = new Properties();
		InputStream input = null;
		input = loader.getResourceAsStream("prediction.properties");
		try {
			prop.load(input);
			nLearningRate.get(chartId).setText(prop.getProperty("nLearningRate"));
			nMomentum.get(chartId).setText(prop.getProperty("nMomentum"));
			nMaxErrorInc.get(chartId).setText(prop.getProperty("nMaxErrorInc"));
			nMaxError.get(chartId).setText(prop.getProperty("nMaxError"));
			nSetSize.get(chartId).setText(prop.getProperty("nSetSize"));
			nMaxIter.get(chartId).setText(prop.getProperty("nMaxIter"));
			nOfInitialNetworkDataRecords.get(chartId).setText(prop.getProperty("nInitialNeuralDataRecords"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void arrayInitalize() {
		criticalValueMin = new ArrayList<Double>();
		for (int i = 0; i < 4; i++)
			criticalValueMin.add(Double.MIN_VALUE);

		criticalValueMax = new ArrayList<Double>();
		for (int i = 0; i < 4; i++)
			criticalValueMax.add(Double.MAX_VALUE);

		criticalAlertMin = new ArrayList<Boolean>();
		for (int i = 0; i < 4; i++)
			criticalAlertMin.add(false);

		criticalAlertMax = new ArrayList<Boolean>();
		for (int i = 0; i < 4; i++)
			criticalAlertMax.add(false);
	}

	@FXML
	private void startTest(ActionEvent evt) {
		int chartId = 0;
		if (evt.getSource().equals(testStart1)) {
			chartId = 0;
		} else if (evt.getSource().equals(testStart2)) {
			chartId = 1;
		} else if (evt.getSource().equals(testStart3)) {
			chartId = 2;
		} else if (evt.getSource().equals(testStart4)) {
			chartId = 3;
		}

		int records;
		try {
			records = Integer.parseInt(testRecords.get(chartId).getText());
		} catch (Exception e) {
			records = 20;
		}

		int toPredict;
		try {
			toPredict = Integer.parseInt(predictionSettingsToPredict.get(chartId).getText());
		} catch (Exception e) {
			toPredict = 6;
		}

		boolean[] methods = {testArima.get(chartId).isSelected(),
				testBats.get(chartId).isSelected(),
				testGarch.get(chartId).isSelected(),
				testHoltWinters.get(chartId).isSelected(),
				testRegression.get(chartId).isSelected(),
				testNeural.get(chartId).isSelected()};

		int methodCounter = 0;
		for (boolean method : methods) {
			if (method) methodCounter++;
		}

		testMax[chartId] = records * methodCounter;
		testProgress.get(chartId).setProgress(0);
		testCounter[chartId] = 0;

		TestManager manager = new TestManager(this,
				resourceChoiceBox.get(chartId).getSelectionModel().getSelectedItem(),
				new TimePeriod(
						predictionSettingsProbesTimeUnitChoiceBox.get(chartId).getSelectionModel().getSelectedItem(),
						records),
				records, toPredict, methods, chartId);
		try {
			manager.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//manager.printTestResult();
	}

	@FXML
	protected void ApplyAllButtonHandler(ActionEvent evt) {
		if (evt.getSource().equals(applyAll1)) {
			try {
				initAction(0, true);
			} catch (IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (evt.getSource().equals(applyAll2)) {
			try {
				initAction(1, true);
			} catch (IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (evt.getSource().equals(applyAll3)) {
			try {
				initAction(2, true);
			} catch (IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (evt.getSource().equals(applyAll4)) {
			try {
				initAction(3, true);
			} catch (IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void changeChartNumber(int newNumber) {
		// Make tab visible
		SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
		selectionModel.select(tab1);

		// Adding
		while (currentNumberOfCharts < newNumber) {
			int chartId = currentNumberOfCharts;
			// Update threads
			try {
				initAction(chartId, false);
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
			// Update visibility
			anchor.get(chartId).setVisible(true);
		}

		// Removing
		while (currentNumberOfCharts > newNumber) {
			int chartId = currentNumberOfCharts - 1;
			currentNumberOfCharts--;
			// Update threads
			try {
				chartThreads.get(chartId).stopThread();
				chartThreads.get(chartId).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Update visibility
			anchor.get(chartId).setVisible(false);
		}

		// Update CSS
		// Hardcoded :(
		if (currentNumberOfCharts >= 1) {
			anchor.get(0).setPrefWidth(1220.0);
			anchor.get(0).setPrefHeight(710.0);
		}
		if (currentNumberOfCharts >= 2) {
			anchor.get(0).setPrefHeight(355.0);
			anchor.get(1).setPrefWidth(1220.0);
			anchor.get(1).setPrefHeight(355.0);
		}
		if (currentNumberOfCharts >= 3) {
			anchor.get(0).setPrefWidth(605.0);
			anchor.get(1).setPrefWidth(605.0);
		}
	}

	@FXML
	protected void StopButtonHandler(ActionEvent evt) {
		if (evt.getSource().equals(stop1)) {
			neuralStop(0);
		} else if (evt.getSource().equals(stop2)) {
			neuralStop(1);
		} else if (evt.getSource().equals(stop3)) {
			neuralStop(2);
		} else if (evt.getSource().equals(stop4)) {
			neuralStop(3);
		}
	}

	@FXML
	protected void ContinueButtonHandler(ActionEvent evt) {
		if (evt.getSource().equals(continue1)) {
			neuralContinue(0);
		} else if (evt.getSource().equals(continue2)) {
			neuralContinue(1);
		} else if (evt.getSource().equals(continue3)) {
			neuralContinue(2);
		} else if (evt.getSource().equals(continue4)) {
			neuralContinue(3);
		}
	}

	@FXML
	protected void ResetButtonHandler(ActionEvent evt) {
		if (evt.getSource().equals(reset1)) {
			neuralReset(0);
		} else if (evt.getSource().equals(reset2)) {
			neuralReset(1);
		} else if (evt.getSource().equals(reset3)) {
			neuralReset(2);
		} else if (evt.getSource().equals(reset4)) {
			neuralReset(3);
		}
	}

	@FXML
	protected void menuViewAction(ActionEvent evt) {
		if (evt.getSource().equals(radio1)) {
			changeChartNumber(1);
		} else if (evt.getSource().equals(radio2)) {
			changeChartNumber(2);
		} else if (evt.getSource().equals(radio3)) {
			changeChartNumber(3);
		} else if (evt.getSource().equals(radio4)) {
			changeChartNumber(4);
		} else if (evt.getSource().equals(menuSettings)) {
			SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
			selectionModel.select(tab2);
		}
	}

	public int getnSetSize(int chartId) {
		return Integer.parseInt(nSetSize.get(chartId).getText());
	}

	public int getnOfInitialNetworkDataRecords(int chartId) {
		return Integer.parseInt(nOfInitialNetworkDataRecords.get(chartId).getText());
	}

	public double getnLearningRate(int chartId) {
		return Double.parseDouble(nLearningRate.get(chartId).getText());
	}

	public double getnMomentum(int chartId) {
		return Double.parseDouble(nMomentum.get(chartId).getText());
	}

	public int getnMaxIter(int chartId) {
		return Integer.parseInt(nMaxIter.get(chartId).getText());
	}

	public double getnMaxErrorInc(int chartId) {
		return Double.parseDouble(nMaxErrorInc.get(chartId).getText());
	}

	public double getnMaxError(int chartId) {
		return Double.parseDouble(nMaxErrorInc.get(chartId).getText());
	}

}
