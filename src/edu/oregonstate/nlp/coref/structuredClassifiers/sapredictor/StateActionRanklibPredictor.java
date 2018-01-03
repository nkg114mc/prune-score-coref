package edu.oregonstate.nlp.coref.structuredClassifiers.sapredictor;

import java.util.ArrayList;

import ciir.umass.edu.eval.Evaluator;
import ciir.umass.edu.learning.DataPoint;
import ciir.umass.edu.learning.DenseDataPoint;
import ciir.umass.edu.learning.RankList;
import ciir.umass.edu.learning.Ranker;
import ciir.umass.edu.learning.RankerFactory;
import edu.oregonstate.nlp.coref.structuredClassifiers.Action;
import edu.oregonstate.nlp.coref.structuredClassifiers.PartialOutputState;
import edu.oregonstate.nlp.coref.structuredClassifiers.StateActionFeaturizer;

public class StateActionRanklibPredictor extends StateActionScorer {
	
	int rankerType = 4;
	//String trainMetric = "ERR@10";
	//String testMetric = "ERR@10";
	String lmetric = "P@1";
	//Evaluator.normalize = false;
	boolean printIndividual = false;
	
	
	private RankerFactory rFact = null;
	private Ranker ranker = null;
	
	private StateActionFeaturizer featurizer;
	
	public StateActionRanklibPredictor(String learningMetric) { // for training
		// create ranker
		lmetric = learningMetric;
		rFact = new RankerFactory();
		ranker = null;//rFact.createRanker(RANKER_TYPE.MART);
		
		featurizer = new StateActionFeaturizer();
	}
	
	public StateActionRanklibPredictor(String learningMetric, String modelFn) { // for testing, with a loaded model
		// create ranker
		lmetric = learningMetric;
		rFact = new RankerFactory();
		ranker = rFact.loadRanker(modelFn);
		
		featurizer = new StateActionFeaturizer();
	}
	
	public void loadModelFile(String modelPath)
	{
		System.out.println("Loading ranklib model file: " + modelPath);
		ranker = rFact.loadRanker(modelPath);
	}
	
	public void setFeaturizer(StateActionFeaturizer frizr) {
		featurizer = frizr;
	}

	@Override
	public double scoringStateAction(PartialOutputState state, Action act) {
		double[] fv = featurizer.featurizeStateAction(state, act);
		double sc = scoringStateActionGivenFeatureVec(fv);
		return sc;
	}
	
	@Override
	public double scoringStateActionGivenFeatureVec(double[] featVec) {
		double sc = getRankerScore(featVec);
		return sc;
	}
	
	public void trainRanker(String givenModelName, String trainFeatName, String validFeatName, int prunerBeamSize) {
		
		String corpusName = "ontonotes5";

		//// specific for lambdamart
		String lamdaMartCmd = "-sparse -tree 1000 -leaf 200 -shrinkage 0.1 -tc -1 -mls 1 -estop 75 -ranker 6 ";

		// record on the log
		//System.out.println("RankLib arg: " + lamdaMartCmd);

		// training with Ranklib
		String modelFN = null;
		modelFN = givenModelName + "_lambdamart_" + "beam" + prunerBeamSize + "_" + corpusName  + ".txt";
		String mainCmd = lamdaMartCmd;
		String rankingMetricCmd = "-metric2t " + "P@" + prunerBeamSize + " ";
		String trainCmd = "-train " + trainFeatName + " ";
		String validCmd = "-tvs 0.8 ";
		if (validFeatName != null) {
			validCmd = "-validate " + validFeatName + " ";
		}
		String modelCmd = "-save " + modelFN;
		String lamdamart_arg = mainCmd + rankingMetricCmd + trainCmd + validCmd + modelCmd;
		lamdaMARTTrain(lamdamart_arg);
	}

	private void lamdaMARTTrain(String argue)
	{
		String strArgs[] = argue.split("\\s+");
		System.out.println("RankLib running arguement: "+argue);
		for (String sr : strArgs) {
			System.out.println(sr);
		}
		Evaluator.main(strArgs);
	}
	
	public double getRankerScore(DataPoint dp)
	{
		double score = -1;
		if (ranker == null) {
			throw new RuntimeException("ranker has not be initialized yet!");
		}
		score = ranker.eval(dp);
		return score;
	}
	public double getRankerScore(double[] featVecs)
	{
		double score = -1;
		if (ranker == null) {
			throw new RuntimeException("ranker has not be initialized yet!");
		}
		DataPoint dp = featureVectorToDataPoint(featVecs);
		score = ranker.eval(dp);
		return score;
	}
	
	
	
	
	// feature vector to data point
	public static DataPoint featureVectorToDataPoint(double[] featVecs)
	{
		String sampleStr = rankSampleToStr(featVecs, 0);
		DataPoint rankSample = new DenseDataPoint(sampleStr);
/*
		System.out.println("dimention: "+rankSample.getFeatureCount());
		System.out.println("vec: ");
		for (int j = 0; j < rankSample.getFeatureCount(); j++) {
			System.out.println((j+1)+":"+rankSample.getFeatureValue(j));
		}
*/
		return rankSample;
	}

	
	public static RankList constructRankList(int rankLength, double[][] featVecs)
	{
		ArrayList<DataPoint> dplist = new ArrayList<DataPoint>();
		
		// input all feature vectors 
		for (int i = 0; i < rankLength; i++) {
			String sampleStr = rankSampleToStr(featVecs[i], 0);
			DataPoint rankSample = new DenseDataPoint(sampleStr);
			dplist.add(rankSample);
		}
		
		RankList rl = new RankList(dplist);
		return rl;
	}
	
	public static String rankSampleToStr(double[] feat, int label)
	{
		int qid = 1;
		String str = new String("");
		String lstr = new String(Integer.toString(label));
		String qstr = new String("qid:"+qid);
		
		String fstr = new String("");
		for (int i = 0; i < feat.length; i++) {
			fstr = (fstr + " " + (i+1) + ":" + feat[i]);
		}
		
		str = lstr + " " + qstr + fstr;
		return str;
	}

}
