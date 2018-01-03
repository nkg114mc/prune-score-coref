package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ciir.umass.edu.eval.Evaluator;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.mentions.MentionFilter;



/**
 * @author Chao Ma
 * 
 */

public class PruneScoreTrain {

	// policy
	GreedyPolicy osuCorefPolicy = null;
	// pruner learner
	//PolicyPrunner prunerLearner = null;
	
	// ranklib
	Evaluator ranklibLearner;
	// svmrank
	SvmrankLearner svmrnkLearner =  new SvmrankLearner();

	

	
	// mention filter
	//MentionFilter menFilter = null;
	
	// mention setting?
	boolean useGoldMention = false;
	
	// 
	private String datasetName = "";
	
	// about pruning
	private String prunerRanker = "";
	private int prunerBeamSize = 0;

	private static String featFolderPath = "";
	private static String modelFolderPath = "";
	private static String learnerName = "";
	
	
	// pruner
	private static String prunRankerName = ""; 
	private static String prunModelPath = "";
	private static int    numberTreeLeaves = 0;
	private static double shrinkage = 0.1;
	
	// log file path
	private static String logfilePath = "logfile.log";
	
	public PruneScoreTrain() {
		// Loading the policy
		osuCorefPolicy = new GreedyPolicy();
	}

/*
	public void parsingConfig(SystemConfig cfg)
	{

		// dataset name
		datasetName = cfg.getString("DATASET", "unknown_dataset");
		
		// pruner
		prunerBeamSize  = cfg.getInteger("PRUNNER_BEAM_SIZE", 4);
		
		featFolderPath = cfg.getString("FEATURE_LOG_FOLDER", "");
		modelFolderPath = cfg.getString("POLICY_MODEL_FOLDER", "");
		learnerName = cfg.getString("POLICY_LEARNER", "svmrank"); // default learner name


		// ranklib argument
		//ranklibArg = cfg.getString("PRUN_RANKLIB_ARG", "");;
		
		// svmrank path
		svmrank_learn = cfg.getString("SVMRANK_LEARN_PATH", "svm_rank_learn");
		svmrank_perl = cfg.getString("SVMRANK_PERL_PATH", "svm2weight.pl");
		svmrank_m2w = cfg.getString("SVMRANK_MODEL_WEIGHT_PATH", "weight.txt");
		svmrnkLearner.setSvmrankPath(svmrank_learn);
		svmrnkLearner.setSvmPerlPath(svmrank_perl);
		svmrnkLearner.setM2WPath(svmrank_m2w);
		
		
		// pruner ranker
		prunRankerName = cfg.getString("PRUNNER_RANKER", "svmrank");
		if (prunRankerName.equals("lambdamart")) {
			prunModelPath = cfg.getString("PRUNNER_RANKLIB_MODEL_PATH", "");
		} else if (prunRankerName.equals("svmrank")) {
			prunModelPath = cfg.getString("PRUNNER_WEIGHT_PATH", "");
		}
		numberTreeLeaves = cfg.getInteger("POLICY_LAMBDAMART_TREE_LEAVES", 150);
		shrinkage = cfg.getDouble("POLICY_LAMBDAMART_SHRINKAGE", 0.1);
		
		// mention setting?
		useGoldMention = cfg.getUsingGoldMentionsOrNot();
		applyMentionPruner = cfg.getBoolean("USE_PREDICT_MEN_FILTER", true);
		
		// mention filter
		if (!useGoldMention) {
			menFilter = new MentionFilter(cfg);
		}

		
		
		// init logfile
		logfilePath = cfg.getString("POLICY_LOG_PATH", "logfile.txt");
	}
*/

	

	public void train(Iterable<Document> traindocs, Iterable<Document> validdocs) {
		playPolicyTrainWithPruning(traindocs, validdocs);
	}
	
	private void playPolicyTrainWithPruning(Iterable<Document> trainidocs, Iterable<Document> valididocs)
	{
		List<Document> trainDocs = DocIterateToList(trainidocs);
		List<Document> validDocs = DocIterateToList(valididocs);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// we still need a global learned pruner (using original training data, no cross-validation)
		System.out.println("//////////////////////////////////////////////////");
		System.out.println("//// Start Pruner Learning  //////////////////////");
		System.out.println("//////////////////////////////////////////////////");
		
		// generate feature files for training set (and validation set, if any)
		//String trainFeatName = featFolderPath + "/prunerFeatTraining_" + datasetName + "_beam" + prunerBeamSize + ".txt";
		//String validFeatName = featFolderPath + "/prunerFeatValidation_" + datasetName + "_beam" + prunerBeamSize + ".txt";
		//osuCorefPolicy.train(trainDocs, validDocs); // generate feature file for training
		

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		


		System.out.println("/////////////////////////////////////////////////");
		System.out.println("//// Start Scorer training  /////////////////////");
		System.out.println("/////////////////////////////////////////////////");
		
		// begin training =====================================
		// 1) feature generation
		
		// generate feature files for training set (and validation set, if any)
		String trainFeatName = featFolderPath + "/policyFeatTraining_" + datasetName + "_beam" + prunerBeamSize + ".txt";
		String validFeatName = featFolderPath + "/policyFeatValidation_" + datasetName + "_beam" + prunerBeamSize + ".txt";
		osuCorefPolicy.train(trainDocs, validDocs); // generate feature file for training
		
		// 2) run off-line learner
		// training with svmrank or Ranklib
		String modelFN = null;
		if (learnerName.equals("lambdamart")) {
			modelFN = modelFolderPath + "/policyModel_lamdamart_" + datasetName + "_" + "prunbeam" + prunerBeamSize + ".txt";
			String lamdaMartCmd = "-sparse -tree 1000 -leaf " + 
			                      String.valueOf(numberTreeLeaves) + 
			                      " -shrinkage "+ String.valueOf(shrinkage) + 
			                      " -tc -1 -mls 1 -estop 75 -ranker 6 ";
			String mainCmd = lamdaMartCmd;//"-tree 1000 -leaf 50 -shrinkage 0.1 -tc -1 -mls 1 -estop 50 -tvs 0.8 -ranker 6 ";
			String rankingMetricCmd = "-metric2t P@1 ";
			String trainCmd = "-train " + trainFeatName + " ";
			String modelCmd = "-save " + modelFN;
			// about validation
			String validCmd = "-tvs 0.8 ";
			if (validDocs.size() > 0) {
				validCmd = "-validate " + validFeatName + " ";
			}
			String lamdamart_arg = mainCmd + rankingMetricCmd + trainCmd + validCmd + modelCmd;
			lamdaMARTTrain(lamdamart_arg);
		} else if (learnerName.equals("svmrank")) {
			modelFN = modelFolderPath + "/policyModel_svmrank_" + datasetName + "_" + "prunbeam" + prunerBeamSize + ".txt";
			svmrankTrain(validDocs, trainFeatName, modelFN); // TODO validDocs
		} else {
			throw new RuntimeException("What's the learner's name???");
		}


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
	
	private void svmrankTrain(List<Document> validDocs, String featureFile, String weightPath)
	{
		double[] possibleC = { 0.0001, 0.001, 0.01, 0.1, 1.0, 10, 100 };
		ArrayList<String> allModelNames = new ArrayList<String>();
		ArrayList<String> allWeightNames = new ArrayList<String>();
		int bestIdx = -1;
		double bestC = -999999;
		double bestBCube = -Double.MAX_VALUE;
		
		// validation
		for (int i = 0; i < possibleC.length; i++) {
			double currentBCubeF1 = -Double.MAX_VALUE;
			// ==================================================
			System.out.println("Running the validation with C " + possibleC[i]);
			
			// train firstly
			System.out.println("Training ...");
			String modelName = modelFolderPath + "/model-c" + String.valueOf(possibleC[i])+".txt"; 
			String weightName = modelFolderPath + "/weight-c" + String.valueOf(possibleC[i])+".txt"; 
			allModelNames.add(modelName); // record
			allWeightNames.add(weightName); // record
			svmrnkLearner.runSvmrankLearn(featureFile, possibleC[i], modelName, weightName);
			System.out.println("Done training ...");
			
			// test on validation to get the score
			System.out.println("Tesing as the validation ...");
			String[] validOpts = { "TurnOffCandidateDiscrepancyCollection", "-useRanklib", "0", "-useSvmrank", "1", "-modelFileName", weightName };
			//osuCorefPolicy.testAll(validDocs, "",  validOpts);
			
			// scoring ...
			/*
			System.out.println("Scoring ...");
			double[] bcubeScore = bcubeScorer.score(false, new DocArray2DocIterable(validDocs));
			double bcubeF1 = bcubeScore[2];
			currentBCubeF1 = bcubeF1;
			System.out.println("C = " + possibleC[i] + " validation bcube score = " + bcubeF1);
			*/
			
			// pick best
			if (currentBCubeF1 > bestBCube) {
				bestBCube = currentBCubeF1;
				bestIdx = i;
				System.out.println("current best C = " + possibleC[i]);
				System.out.println("current bcube score = " + bestBCube);
			}
		}
		
		// f
		System.out.println("best C = " + possibleC[bestIdx] + ", best validation bcube score = " + bestBCube);
		
		// copy the best model with
		String copyCmd = "cp " + allWeightNames.get(bestIdx) + " " + weightPath;
		System.out.println("Copy the best model: " + copyCmd);
		runExe(copyCmd);
	}

	
	private void runExe(String arg)
	{
        Process p;
		try {
			p = Runtime.getRuntime().exec(arg);
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static List<Document> DocIterateToList(Iterable<Document> idocs) {
		ArrayList<Document> docList = new ArrayList<Document>();
		for (Document d : idocs) {
			docList.add(d);
		}
		return docList;
	}
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
/*
// Do we need to remove some mentions with a filter at the very beginning?
System.out.println("//////////////////////////////////////////////////");
System.out.println("//// Mention Filter Runing  //////////////////////");
System.out.println("//////////////////////////////////////////////////");

// before
System.out.println("(Before) Training docs mentions:");
Scoring.mentionDetectionAccuracy(trainDocs);
System.out.println("(Before) vad docs mentions:");
Scoring.mentionDetectionAccuracy(validDocs);

if (!useGoldMention) {
	if (applyMentionPruner) {
		MentionFilter.removeUnmatchedPredictMentions(trainDocs);
		if (validDocs.size() > 0) { // if there are really validation docs...
			MentionFilter.removeUnmatchedPredictMentions(validDocs);
		}
	}
}

// after
System.out.println("(After) Training docs mentions:");
Scoring.mentionDetectionAccuracy(trainDocs);
System.out.println("(After) vad docs mentions:");
Scoring.mentionDetectionAccuracy(validDocs);
*/
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
