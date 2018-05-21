//package pl.edu.agh.rmsp.base.view;
//
//import static org.junit.Assert.assertTrue;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mockito;
//
//import pl.edu.agh.rmsp.analysis.predictors.Predictor;
//import pl.edu.agh.rmsp.base.testing.TestError;
//import pl.edu.agh.rmsp.base.testing.TestingThread;
//import pl.edu.agh.rmsp.model.commons.Value;
//
//
//public class SimpleTestingThreadTest {
//
//	Predictor predictor= Mockito.mock(Predictor.class);
//	double[] dataForThread;
//	double[] dataForPredictor;
//	TestError error;
//
//	private void setData(int nrOfProbes, int nrOfPredicted){
//		dataForThread = new double[nrOfProbes+nrOfPredicted];
//		for(int i=0;i<nrOfProbes+nrOfPredicted;i++){
//			dataForThread[i]=i;
//		}
//		dataForPredictor= new double[nrOfProbes];
//		for(int i=0;i<nrOfProbes;i++){
//			dataForPredictor[i]=dataForThread[i];
//		}
//	}
//
//	private double mean(double[] values){
//		double d=0;
//		for(int i=0;i<values.length;i++){
//			d+=values[i]*values[i];
//		}
//		d=d/values.length;
//		return Math.sqrt(d);
//	}
//
//	@Before
//	public void setUp() throws Exception {
//		error = new TestError();
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
//
//	@Test
//	public void oneRecordOneProbeOneValueTest1() throws Exception {
//		setData(1,1);
//		Value[] d=new Value[1];
//		d[0]=new Value(1,0,0);
//		Mockito.when (predictor.predict(dataForPredictor, 1)).thenReturn(d);
//		TestingThread t= new TestingThread(0,1, 1, 1, predictor,error,dataForThread);
//		t.start();
//		t.join();
//		// probe: 0, real :1, predicted: 1. Error should be sqrt((1-1)^2/1)/1==0
//		assertTrue(error.getAverageError()==0);
//
//	}
//
//	@Test
//	public void oneRecordOneProbeOneValueTest2() throws Exception {
//		setData(1,1);
//		Value[] d=new Value[1];
//		d[0]=new Value(0,0,0);
//		Mockito.when (predictor.predict(dataForPredictor, 1)).thenReturn(d);
//		TestingThread t= new TestingThread(0,1, 1, 1, predictor,error,dataForThread);
//		t.start();
//		t.join();
//		// probe: 0, real :1, predicted: 0. Error should be sqrt((1-0)^2/1)/1==1
//		assertTrue(error.getAverageError()==1);
//	}
//
//	@Test
//	public void oneRecordFiveProbesOneValueTest() throws Exception {
//		setData(5,1);
//		Value[] d=new Value[1];
//		d[0]=new Value(5,0,0);
//		Mockito.when (predictor.predict(dataForPredictor, 1)).thenReturn(d);
//		TestingThread t= new TestingThread(0,5, 1, 1, predictor,error,dataForThread);
//		t.start();
//		t.join();
//		// probes: 01234, real :5, predicted: 5 Error should be sqrt((5-5)^2/1)/1==0
//		assertTrue(error.getAverageError()==0);
//	}
//
//	@Test
//	public void oneRecordFiveProbesOneValueTest2() throws Exception {
//		setData(5,1);
//		Value[] d=new Value[1];
//		d[0]=new Value(0,0,0);
//		Mockito.when (predictor.predict(dataForPredictor, 1)).thenReturn(d);
//		TestingThread t= new TestingThread(0,5, 1, 1, predictor,error,dataForThread);
//		t.start();
//		t.join();
//		// probes: 01234, real :5, predicted: 5 Error should be sqrt((5-0)^2/1)/1==5
//		assertTrue(error.getAverageError()==5);
//	}
//
//	@Test
//	public void oneRecordFiveProbesFiveValuesTest1() throws Exception {
//		setData(5,5);
//		Value[] d=new Value[5];
//		for(int i=0;i<5;i++){
//			d[i]=new Value(0,0,0);
//		}
//		Mockito.when (predictor.predict(dataForPredictor, 5)).thenReturn(d);
//		TestingThread t= new TestingThread(0,5, 1, 5, predictor,error,dataForThread);
//		t.start();
//		t.join();
//		// probes: 01234, real :56789, predicted: 00000 Error should be sqrt((5)^2+6^2+7^2+8^2+9^2/5)/1==5
//		double[] v={5,6,7,8,9};
//		assertTrue(error.getAverageError()==mean(v));
//	}
//
//	@Test
//	public void oneRecordFiveProbesFiveValuesTest2() throws Exception {
//		setData(5,5);
//		Value[] d=new Value[5];
//		for(int i=0;i<5;i++){
//			d[i]=new Value(i*i,0,0);
//		}
//		Mockito.when (predictor.predict(dataForPredictor, 5)).thenReturn(d);
//		TestingThread t= new TestingThread(0,5, 1, 5, predictor,error,dataForThread);
//		t.start();
//		t.join();
//		double[] v={5,5,3,1,7};
//		assertTrue(error.getAverageError()==mean(v));
//	}
//
//	@Test
//	public void twoRecordsTwoProbesOneValueZeroErrorTest() throws Exception {
//		setData(3,1);
//		Value[] d1=new Value[1];
//		d1[0]=new Value(2,0,0);
//		Value[] d2=new Value[1];
//		d2[0]=new Value(3,0,0);
//		double[] r= new double[2];
//		r[0]=dataForThread[0];
//		r[1]=dataForThread[1];
//		double[] k= new double[2];
//		k[0]=dataForThread[1];
//		k[1]=dataForThread[2];
//		Mockito.when (predictor.predict(r, 1)).thenReturn(d1);
//		Mockito.when (predictor.predict(k, 1)).thenReturn(d2);
//		TestingThread t= new TestingThread(0,2, 1, 1, predictor,error,dataForThread);
//		t.start();
//		t.join();
//		assertTrue(error.getAverageError()==0);
//	}
//
//	@Test
//	public void twoRecordsTwoProbesOneValueErrorTest() throws Exception {
//		setData(3,1);
//		Value[] d1=new Value[1];
//		d1[0]=new Value(1,0,0);
//		Value[] d2=new Value[1];
//		d2[0]=new Value(10,0,0);
//		double[] r= new double[2];
//		r[0]=dataForThread[0];
//		r[1]=dataForThread[1];
//		double[] k= new double[2];
//		k[0]=dataForThread[1];
//		k[1]=dataForThread[2];
//		Mockito.when (predictor.predict(r, 1)).thenReturn(d1);
//		Mockito.when (predictor.predict(k, 1)).thenReturn(d2);
//		TestingThread t= new TestingThread(0,2, 2, 1, predictor,error,dataForThread);
//		t.start();
//		t.join();
//		double[] res1 ={1};
//		double[] res2 ={7};
//		assertTrue(error.getAverageError()==(mean(res1)+mean(res2))/2);
//	}
//
//	@Test
//	public void twoRecordsTwoProbesTwoValuesZeroErrorTest() throws Exception {
//		setData(3,2);
//		Value[] d1=new Value[2];
//		d1[0]=new Value(2,0,0);
//		d1[1]=new Value(3,0,0);
//		Value[] d2=new Value[2];
//		d2[0]=new Value(3,0,0);
//		d2[1]=new Value(4,0,0);
//		double[] r= new double[2];
//		r[0]=dataForThread[0];
//		r[1]=dataForThread[1];
//		double[] k= new double[2];
//		k[0]=dataForThread[1];
//		k[1]=dataForThread[2];
//		Mockito.when (predictor.predict(r, 2)).thenReturn(d1);
//		Mockito.when (predictor.predict(k, 2)).thenReturn(d2);
//		TestingThread t= new TestingThread(0,2, 2, 2, predictor,error,dataForThread);
//		t.start();
//		t.join();
//		assertTrue(error.getAverageError()==0);
//	}
//
//	@Test
//	public void twoRecordsTwoProbesTwoValuesErrorTest() throws Exception {
//		setData(3,2);
//		Value[] d1=new Value[2];
//		d1[0]=new Value(5,0,0);
//		d1[1]=new Value(5,0,0);
//		Value[] d2=new Value[2];
//		d2[0]=new Value(9,0,0);
//		d2[1]=new Value(9,0,0);
//		double[] r= new double[2];
//		r[0]=dataForThread[0];
//		r[1]=dataForThread[1];
//		double[] k= new double[2];
//		k[0]=dataForThread[1];
//		k[1]=dataForThread[2];
//		Mockito.when (predictor.predict(r, 2)).thenReturn(d1);
//		Mockito.when (predictor.predict(k, 2)).thenReturn(d2);
//		TestingThread t= new TestingThread(0,2, 2, 2, predictor,error,dataForThread);
//		t.start();
//		t.join();
//		double[] res1 ={3,2};
//		double[] res2 ={6,5};
//		assertTrue(error.getAverageError()==(mean(res1)+mean(res2))/2);
//	}
//
//}
