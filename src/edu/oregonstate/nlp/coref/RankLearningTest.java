package edu.oregonstate.nlp.coref;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import ciir.umass.edu.eval.Evaluator;
import edu.oregonstate.nlp.coref.structuredClassifiers.StateActionFeaturizer;
import edu.oregonstate.nlp.coref.structuredClassifiers.sapredictor.StateActionXgboostPredictor;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoostError;

public class RankLearningTest {

	public static void main(String[] args) {
		runRanklib();
		//runXgboost();
	}
	
	public static void haveALookAtFeatures() {
		StateActionFeaturizer fzr = new StateActionFeaturizer();
		fzr.printFeatNames();
	}

	public static void runRanklib() {
		//String argue = "-sparse -tree 1000 -leaf 200 -shrinkage 0.1 -tc -1 -mls 1 -estop 75 -ranker 6 -metric2t P@1 -train featDump/ontonotes5_feature_train.txt -validate featDump/ontonotes5_feature_dev.txt -save policy_scoring_lambdamart_beam1_ontonotes5.txt";
		//String argue = "-sparse -tree 1000 -leaf 200 -shrinkage 0.1 -tc -1 -mls 1 -estop 75 -ranker 6 -metric2t P@1 -train featDump/ontonotes5_feature_gold_train.txt -validate featDump/ontonotes5_feature_gold_dev.txt -save policy_scoring_lambdamart_beam1_ontonotes5.txt";
		//String argue = "-sparse -tree 1000 -leaf 200 -shrinkage 0.1 -tc -1 -mls 1 -estop 50 -ranker 6 -metric2t P@1 -train featDump/ontonotes5_feature_predment_train.txt -validate featDump/ontonotes5_feature_predment_dev.txt -save policy_predment_scoring_lambdamart_beam1_ontonotes5.txt";
		
		//String argue = "-sparse -tree 1000 -leaf 200 -shrinkage 0.1 -tc -1 -mls 1 -estop 50 -ranker 6 -metric2t P@1 -train featDump/ontonotes5_feat_predment_train.txt -validate featDump/ontonotes5_feat_predment_dev.txt -save policy_predment_scoring_lambdamart_beam1_ontonotes5.txt";
		String argue = "-sparse -tree 1000 -leaf 200 -shrinkage 0.1 -tc -1 -mls 1 -estop 50 -ranker 6 -metric2t P@1 -train featDump/ontonotes5_feat_predment_train.txt -save policy_predment_scoring_lambdamart_beam1_ontonotes5.txt";
		String strArgs[] = argue.split("\\s+");
		System.out.println("RankLib running arguement: "+argue);
		Evaluator.main(strArgs);
	}
	
	public static void runXgboost() {
		StateActionXgboostPredictor prdr = new StateActionXgboostPredictor();
		//prdr.trainRanker("policy_scoring_xgboost_beam1_ontonotes5.txt", "featDump/ontonotes5_feat_goldment_train.txt", "featDump/ontonotes5_feat_goldment_dev.txt", 1);
		prdr.trainRanker("policy_scoring_xgboost_beam1_ontonotes5.txt", "featDump/ontonotes5_feat_predment_train.txt", "featDump/ontonotes5_feat_predment_dev.txt", 1);
		//prdr.trainRanker("policy_scoring_xgboost_beam1_ontonotes5.txt", "featDump/ontonotes5_feat_predment_dev.txt", "featDump/ontonotes5_feat_predment_dev.txt", 1);
		
		//prdr.trainRanker("policy_scoring_xgboost_beam1_ontonotes5.txt", "featDump/ontonotes5_feat_predgoldment_train.txt", "featDump/ontonotes5_feat_predgoldment_dev.txt", 1);
		//prdr.trainRanker("policy_scoring_xgboost_beam1_ontonotes5.txt", "featDump/ontonotes5_feat_predgoldment_dev.txt", "featDump/ontonotes5_feat_predgoldment_dev.txt", 1);
	}
	
/*
	public static void testBooster() {
		
		String filePath = "/home/mc/workplace/coref2017/PruneScoreCoref/featDump/train10.txt";//ontonotes5_feature_gold_dev.txt";
		StateActionXgboostPredictor prdr = new StateActionXgboostPredictor("/home/mc/workplace/coref2017/PruneScoreCoref/xgb.model");
		Booster bstr = prdr.getBooster();
		
		String line;
		FileReader reader;
		BufferedReader br;
		

		
		try {
			
			DMatrix mx1 = StateActionXgboostPredictor.loadSvmrankFileToDMatrix(filePath, true);
			float[][] predict1 = bstr.predict(mx1);
			

			System.out.println(predict1.length + " " + predict1[0].length);

			float[][] predict2 = new float[predict1.length][1];
			
			reader = new FileReader(filePath);
			br = new BufferedReader(reader);
			
			int lineCnt = 0;
			while ((line = br.readLine()) != null) {
				
				lineCnt++;
				String[] terms = line.trim().split("\\s+");
				
				double[] fv = new double[140];
				Arrays.fill(fv, 0);
				
				for (int i = 2; i < terms.length; i++) {
					int fidx = Integer.parseInt(StateActionXgboostPredictor.getLeft(terms[i]));
					double fval = Double.parseDouble(StateActionXgboostPredictor.getRight(terms[i]));
					fv[fidx - 1] = fval;
				}

				DMatrix singleVec = StateActionXgboostPredictor.featVecToDMatrix(fv, false);
				//DMatrix singleVec = StateActionXgboostPredictor.strFeatVecToDMatrix(line, true);
				float[][] predicts3 = bstr.predict(singleVec);
				
				predict2[lineCnt - 1] = new float[1];
				predict2[lineCnt - 1][0] = predicts3[0][0];//(float) prdr.scoringStateActionGivenFeatureVec(fv);

			}
			
			System.out.println(predict1.length + " == " + lineCnt);
			
			for (int i = 0; i < predict1.length; i++) {
				System.out.println(predict1[i][0] + " == " + predict2[i][0]);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XGBoostError e) {
			e.printStackTrace();
		}
	}
*/
}
