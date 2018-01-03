package edu.oregonstate.nlp.coref.structuredClassifiers;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.mentions.AbstractMentionExtractor;
import edu.oregonstate.nlp.coref.mentions.BerkeleyConllMentionExtractor;
import edu.oregonstate.nlp.coref.mentions.FileLoadedMentionExtractor;
import edu.oregonstate.nlp.coref.mentions.GoldConllMentionExtractor;
import edu.oregonstate.nlp.coref.mentions.MentionExtractorFactory;
import edu.oregonstate.nlp.coref.mentions.MentionExtractorFactory.MentExtractType;
import edu.oregonstate.nlp.coref.scorers.BCubedScore;
import edu.oregonstate.nlp.coref.scorers.Scorer;
import edu.oregonstate.nlp.coref.structuredClassifiers.pruner.ActionPruner;
import edu.oregonstate.nlp.coref.structuredClassifiers.pruner.TopkActionPruner;
import edu.oregonstate.nlp.coref.structuredClassifiers.sapredictor.StateActionRanklibPredictor;
import edu.oregonstate.nlp.coref.structuredClassifiers.sapredictor.StateActionScorer;
import edu.oregonstate.nlp.coref.structuredClassifiers.sapredictor.StateActionXgboostPredictor;


/**
 * @author Chao Ma
 * 
 */

public class GreedyPolicy extends StructuredClassifier {

	public static Random rand = new Random(System.currentTimeMillis());
	static boolean DEBUG = false;
	
	// training parameters
	public String modelSavePath = null;
	public String featFileName = "policy_feature_dump";
	private String featureDumpFolder = null;
	
	public boolean injectGold = false;
	
	public boolean runPruning = false;
	public String prunerSavePath = null;
	public int prunerBeam = -1;
	
	// for training only
	private int globleQID = 0;
	
	// mention extractor
	private AbstractMentionExtractor mentExtractor;
 
	
	public GreedyPolicy() {
		// all default
	}
	
	public GreedyPolicy(String modelPath,
			String featFn,
			String featFldr) {

		modelSavePath =  modelPath;
		featFileName = featFn;
		featureDumpFolder = featFldr;
		injectGold = false;
		
		mentExtractor = null;
		
		// no pruner
		runPruning = false;
		prunerSavePath = null;
		prunerBeam = -1;

	}
	
	// to sort the information of an action list
	class ActionListEvaluation {

		public boolean doGroungTruth;
		
		public ArrayList<Action> actions;
		public double bestPredScore;
		public double bestTrueScore;
		public Action bestPredictAction;
		public Action bestGroundTruthAction;

		public ActionListEvaluation(boolean training) {
			doGroungTruth = training;
			
			bestPredScore = -Double.MAX_VALUE;
			bestTrueScore = -Double.MAX_VALUE;
			bestPredictAction = null;
			bestGroundTruthAction = null;
			
			actions = new ArrayList<Action>();
		}
		
		public Action getAction() {
			Action besta = null;
			if (doGroungTruth) {
				besta = bestGroundTruthAction; // on-trajectory training
			} else {
				besta = bestPredictAction; // off-trajectory training or testing
			}
			assert(besta != null);
			return besta;
		}
		
		public void addAct(Action act) {
			actions.add(act);
		}
		
		public ArrayList<Action> getActList() {
			return actions;
		}
		
	}
	
	//public void setMentionExtractor(AbstractMentionExtractor mextr) {
	//	mentExtractor = mextr;
	//}
	
	public void setMentionExtractorType(MentExtractType mtyp) {
		mentExtractor = MentionExtractorFactory.createMentionExtractor(mtyp);
	}

	@Override	
	public AnnotationSet test(Document doc) {
		throw new RuntimeException("No implemented, please use the batch method testAll!");
	}

	@Override
	public void train(List<Document> traindocs, List<Document> validdocs) {
		trainPolicy(traindocs, validdocs);
	}

	@Override
	public void testAll(Iterable<Document> docs) {
		
		injectGold = false;
		
		//StateActionScorer scoringFunction = new StateActionRanklibPredictor("P@1", modelSavePath);
		StateActionScorer scoringFunction = new StateActionXgboostPredictor(modelSavePath);
		
		ActionPruner pruner = null;
		if (runPruning) {
			StateActionScorer prunerFunc = new StateActionXgboostPredictor(prunerSavePath);
			pruner = new TopkActionPruner(prunerBeam, prunerFunc);
		}
		
		//runPolicy(docs, false, null, scoringFunction, false, null);
		runPolicy(docs, false, null, scoringFunction, runPruning, pruner);
		
		
		System.out.println("Done testing.");
	}


	// add NP annotation
	public void doMentionExtract(Iterable<Document> docs, boolean injectGolds) {
		mentExtractor.extractMentionsBatch(docs, injectGolds);
		for (Document doc : docs) {
			AbstractMentionExtractor.fillPropertyForPredictMentions(doc); // fill properties values for predict mentions
		}
	}
	
	
	public void trainPolicy(List<Document> traindocs, List<Document> validdocs) {
		
		final boolean IS_TRAIN = true;
		
		// initializing trainer
		String modelPath = modelSavePath;

		checkOrCreateFolder(featureDumpFolder);
		
		// scorer of Prune-and-Score
		StateActionRanklibPredictor screr = new StateActionRanklibPredictor("P@1");

		globleQID = 0;
		
		try {

			// dump training features
			String trnFeatFn = featureDumpFolder + "/" + featFileName + "_train.txt";
			PrintWriter trnFeatDumper = new PrintWriter(trnFeatFn);
			runPolicy(traindocs, IS_TRAIN, trnFeatDumper, null, false, null);
			trnFeatDumper.close();

			// dump develop features
			String devFeatFn = featureDumpFolder + "/" + featFileName + "_dev.txt";
			PrintWriter devFeatDumper = new PrintWriter(devFeatFn);
			runPolicy(validdocs, IS_TRAIN, devFeatDumper, null, false, null);
			devFeatDumper.close();

			
			// =====================================
			// save some memory
			
			
			// run batch training
			//screr.trainRanker(modelPath, trnFeatFn, devFeatFn, 1);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done training.");
	}
	
	public void qidClearZero() {
		globleQID = 0;
	}
	
	public void runPolicy(Iterable<Document> docs, boolean isTrain, 
			              PrintWriter featDumper,
			              StateActionScorer preLoadedScorer,
			              boolean doPruning,
			              ActionPruner actionPruner) {

		Scorer gruthSupervisor = new BCubedScore();
		
		ActionGenerator actionGener = new ActionGenerator();
		StateActionFeaturizer saFeaturizer = new StateActionFeaturizer();
		
		if (actionPruner == null) {
			doPruning = false;
		}
		
		
		// mention extraction
		doMentionExtract(docs, injectGold);// isTrain);


		// error statistics
		int totalStepCnt = 0;
		int docCnt = 0;
		int n_error = 0;
		int n_decisions = 0;


		for (Document doc : docs) {
			
			docCnt++;
			//checkDocument(doc);

			PartialOutputState initState = new PartialOutputState(doc);

			int depth = 0;
			PartialOutputState curState = initState;

			while (!curState.isTerminalState()) {

				depth++;
				totalStepCnt++;

				// generate all actions
				ActionList actionsAll = actionGener.genLegalActions(curState);
				
				// pruning actions
				ActionList actions = actionsAll;
				if (doPruning) {
					actions = actionPruner.pruneActionForStateRemovePruned(curState, actionsAll);
				}

				// pick one action, true best or predict best
				ActionListEvaluation evaledList = pickBestConfigable(curState, actions, gruthSupervisor, saFeaturizer, preLoadedScorer, isTrain);
				Action bestAct = evaledList.getAction();
				//Action bestAct = evaledList.bestGroundTruthAction;
						
				curState = PartialOutputState.executeActionNoCopy(curState, bestAct);
				
				// add one training example
				if (isTrain) {
					// one step, dump to file
					if (actions.size() > 1) {
						globleQID++;
						dumpRankLibWriter(featDumper, evaledList, globleQID);
					}
				}

			}
			
			double trueScoringComplete = PartialOutputState.scoringCompleteOutputWithScorer(curState, gruthSupervisor);
			System.out.println("DocTrueScore = " + trueScoringComplete);
			
			// assign the result back to document
			curState.writeBackClusteringResult();
		}

		System.out.println("TotalDocuments = " + docCnt);
		System.out.println("TotalPolicySteps = " + totalStepCnt);
		
	}

	public ActionListEvaluation pickBestConfigable(PartialOutputState state, 
			                                       ActionList actions, 
			                                       Scorer scorer, 
			                                       StateActionFeaturizer featurizer, 
			                                       StateActionScorer actionScorer,
			                                       boolean training) {
		
		ActionListEvaluation alev = new ActionListEvaluation(training);
		
		alev.bestGroundTruthAction = null;
		alev.bestPredictAction = null;
		alev.bestTrueScore = -Double.MAX_VALUE;
		alev.bestPredScore = -Double.MAX_VALUE;
		Random rnd = new Random();
		
		for (Action act : actions) {
			
			// ground truth scoring
			//double trueScoring = PartialOutputState.scoringPartialOutputWithScorer(state, act, scorer);
			double trueScoring = PartialOutputState.scoringPartialOutputWithEdgeCount(state, act);
			//double trueScoring = rnd.nextDouble();
			act.trueScore = trueScoring;
			if (alev.bestTrueScore < trueScoring) {
				alev.bestTrueScore = trueScoring;
				alev.bestGroundTruthAction = act;
			}
			
			// prediction scoring
			double[] featVec = featurizer.featurizeStateAction(state, act);
			double predictScoring = 0;
			if (actionScorer != null) { // do actual scoring
				predictScoring = actionScorer.scoringStateActionGivenFeatureVec(featVec);
			}
			act.predScore = predictScoring;
			act.featvec = featVec;
			if (alev.bestPredScore < predictScoring) {
				alev.bestPredScore = predictScoring;
				alev.bestPredictAction = act;
			}
			
			alev.addAct(act);
			
			//System.out.println(act + ": " + trueScoring + " " + predictScoring + " " + featVec.length);
		}
		
		//System.out.println(actions.size() + " " + alev.bestGroundTruthAction);
		
		return alev;
	}
	
	public static void dumpRankLibWriter(PrintWriter featDumper, ActionListEvaluation evaledList, int qID) {
		
		ArrayList<Action> actList = evaledList.getActList();
		final double bestTrueSc = evaledList.bestTrueScore;
		
		// assume in training
		for (Action candidateAct : actList) {
			int rank = 0;
			if (bestTrueSc == candidateAct.trueScore) {
				rank = 1;
			} else {
				rank = 0;
			}
			//System.out.println("ActionAfterPruning "+candidateAct.toString()+ " "+candidateAct.trueScore+" "+candidateAct.predScore);
			double[] featVec = candidateAct.featvec;
			String svmrankLine = printForSVMRank(rank, qID, featVec, featVec.length);
			featDumper.println(svmrankLine);
		}
	}

	protected static String printForSVMRank(int rank, int qid, double[] featureVec, int vecLength) {
		
		StringBuilder sb = new StringBuilder();
		
		// rank
		sb.append(rank);
		// qid
		sb.append(" qid:"+qid);	
		// feature vector
		for (int k = 0; k < vecLength; k++) {
			if (featureVec[k] != 0) {
				sb.append(" "+(k+1)+":"+featureVec[k]);
			}
		}
		return sb.toString();
	}
	
	public static void checkDocument(Document doc) {
	
		Map<String, AnnotationSet> annos = doc.getAllAnnotationSets();
		for (String annoName : annos.keySet()) {
			System.out.println(doc.getDocumentId() + " " + annoName);
		}
		
	}

	
	// Static methods
	
	public static void checkOrCreateFolder(String folder) {
		File fd = new File(folder);
		if (fd.exists() && fd.isDirectory()) {
			// ok
		} else {
			fd.mkdir();
		}
	}
	
	public static void checkFileExist(String fn) {
		File fd = new File(fn);
		if (!fd.exists() && !fd.isDirectory()) {
			// ok
		} else {
			throw new RuntimeException("File " + fd.getAbsolutePath() + " does not exist!");
		}
	}

	public static void subArray(double[] a1, double[] a2) {
		for (int i = 0; i < a1.length; i++) {
			a1[i] -= a2[i];
		}
	}
	public static void addArray(double[] a1, double num) {
		for (int i = 0; i < a1.length; i++) {
			a1[i] += num;
		}
	}
	public static void addMultArray(double[] a1, double[] a2, double mult) {
		for (int i = 0; i < a1.length; i++) {
			a1[i] += a2[i] * mult;
		}
	}
	public static void multArray(double[] a1, double mult) {
		for (int i = 0; i < a1.length; i++) {
			a1[i] *= mult;
		}
	}
	public static void addArray(double[] a1, double[] a2) {
		for (int i = 0; i < a1.length; i++) {
			a1[i] += a2[i];
		}
	}
	public static void divideArray(double[] a1, double div) {
		for (int i = 0; i < a1.length; i++) {
			a1[i] /= div;
		}
	}
	public static  void vectorClearZero(double var[])
	{
		for (int i = 0; i < var.length; i++) {
			var[i] = 0;
		}
	}


}