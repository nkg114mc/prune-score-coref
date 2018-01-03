package edu.oregonstate.nlp.coref.mentions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.Scoring;
import edu.oregonstate.nlp.coref.SystemConfig;
import edu.oregonstate.nlp.coref.berkeley.BerkeleyErrorCounter;
import edu.oregonstate.nlp.coref.berkeley.BerkeleyFeatureGenerator;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.features.Binarizer;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.scorers.Matcher;
import edu.oregonstate.nlp.coref.scorers.Matcher.MatchStyleEnum;
import edu.oregonstate.nlp.coref.structuredClassifiers.GreedyPolicy;
import edu.oregonstate.nlp.coref.structuredClassifiers.UMassRankLib;

public class MentionFilter {
	
	private SystemConfig config = null;
	private List<Feature> localFeatures; // first level
	private List<Feature> secondFeatures; // second level
	private List<Property> mentionProperties;
	
	private Binarizer binarizer;
	private Binarizer bin2;
	
	private String tempFeatureFileName = "tmp.txt";
	
	private String svmperf_classify_path = "";
	private String svmperf_classify_exe = "";
	private String svmperf_model_file;
	private String svmperf_model_folder;
	
	// J48graft
	private String j48_model_file = "";
	private J48Classifier decisionTree;
	
	
	// best pair?
	String bestPairModelPath;
	UMassRankLib pairRanker;
	
	
	private class NPWithScore {
		public Annotation np;
		public double score;
	}
	// construct method
	public MentionFilter(SystemConfig cfg) {
		config = cfg;
		svmperf_classify_path = cfg.getString("SVMPERF_FOLDER", "./");
		svmperf_classify_exe  = cfg.getString("SVMPERF_CLASSIFY_EXE", "");
		svmperf_model_file    = cfg.getString("MENTION_FILTER_MODEL", "");
		svmperf_model_folder  = cfg.getString("MENTION_FILTER_FOLDER", "./");
		// temp feature file name for this instance
		tempFeatureFileName = getRandomFilename();
		//trainFeatureFN = cfg.getString("MENTION_FILTER_FEAT_FN", "mentionFilterFeature_train.txt");
		
		// decision tree
		j48_model_file = cfg.getString("MENTION_FILTER_J48", "./");
		String weka_feat_exampl = cfg.getString("MENTION_FILTER_WEKA_DATASET_EXAMPLE");
		decisionTree = new J48Classifier(j48_model_file, weka_feat_exampl);
		
		// best pair
		pairRanker = null;//new UMassRankLib();
		bestPairModelPath = cfg.getString("MENTION_PAIR_RANKER_MODEL", "bestp-rlmodel.txt");
	}
	
	
	
	private List<Feature> getLocalFeatures(){
		if (localFeatures == null){
			String pairwiseFeatures[] = {
				"SoonStr", "Modifier", "PostModifier", "WordsSubstr",
				//WordsStr, WordOverlap, , ExactStrMatch // Chao added
				//PNStr, PNSubstr  // Chao added
				"Pronoun1", "Pronoun2", "Definite1", "Definite2", "Demonstrative2",	"Embedded1", "Embedded2", "InQuote1", "InQuote2",
				"BothProperNouns", "BothEmbedded", "BothInQuotes", "BothPronouns", "BothSubjects", 
				"Subject1", "Subject2", "Appositive", "RoleAppositive", "MaximalNP", "IwithinI",
				"SentNum0", "SentNum1", "SentNum2", "SentNum3", "SentNum4plus", "ParNum0", "ParNum1", "ParNum2plus" ,
				"Acronym", "Alias", "IAntes", "WeAntes", "BothYou", "Span", "Binding", "Contraindices", "Syntax", "ClosestComp" ,
				"Indefinite", "Indefinite1", "Prednom", "Pronoun", "ProperNoun" ,
				//ContainsPN
				//, ProperName
				"WordNetClass", "WordNetDist", "WordNetSense", "Subclass", "AlwaysCompatible",
				//FEATURE_NAMES=RuleResolve 
				//FEATURE_NAMES=SameSentence, ConsecutiveSentences
				"WNSynonyms",
				//FEATURE_NAMES=ProResolve, ProResolveRule 
				"Quantity",
				//FEATURE_NAMES=HeadMatch 
				"WhoResolve", "WhichResolve",
				"PairType",
				"DeterminerHeadMatch", "Longer2", "LongerPN2", "ShorterPN2", "InOfRelation"//,"__PairType__SentNum"
			};
			//localFeatures=Constructor.createFeatures(Utils.getConfig().getFeatureNames());
			localFeatures = Constructor.createFeatures(pairwiseFeatures);
		}
		return localFeatures;
	}

	private Binarizer getBinarizer(){
		if (binarizer == null){
			binarizer = new Binarizer(getLocalFeatures());
		}
		return binarizer;
	}
	
	private double[] getLocalFeatureVector(Annotation np1, Annotation np2, Document doc) {
		List<Feature> feats = getLocalFeatures();
		HashMap<Feature, String> featVector = new HashMap<Feature, String>();
		for(int i = 0; i < feats.size(); i++){
			feats.get(i).getValue(np1, np2, doc, featVector);
		}
		return getBinarizer().binarize(featVector);
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	
	private List<Feature> getSecondLevelFeatures(){
		if (secondFeatures == null){
			String seoncdFeatures[] = {
				"SoonStr", "Modifier", "PostModifier", "WordsSubstr",
				"WordsStr", "WordOverlap", "ExactStrMatch", // Chao added
				//PNStr, PNSubstr  // Chao added
				"Pronoun1", "Pronoun2", "Definite1", "Definite2", "Demonstrative2",	"Embedded1", "Embedded2", "InQuote1", "InQuote2",
				"BothProperNouns", "BothEmbedded", "BothInQuotes", "BothPronouns", "BothSubjects", 
				"Subject1", "Subject2", "Appositive", "RoleAppositive", "MaximalNP", "IwithinI",
				"SentNum0", "SentNum1", "SentNum2", "SentNum3", "SentNum4plus", "ParNum0", "ParNum1", "ParNum2plus" ,
				"Acronym", "Alias", "IAntes", "WeAntes", "BothYou", "Span", "Binding", "Contraindices", "Syntax", "ClosestComp" ,
				"Indefinite", "Indefinite1", "Prednom", "Pronoun", "ProperNoun" ,
				//ContainsPN
				//, ProperName
				"WordNetClass", "WordNetDist", "WordNetSense", "Subclass", "AlwaysCompatible",
				//FEATURE_NAMES=RuleResolve 
				//FEATURE_NAMES=SameSentence, ConsecutiveSentences
				"WNSynonyms",
				//FEATURE_NAMES=ProResolve, ProResolveRule 
				"Quantity",
				"HeadMatch", // Chao 
				"WhoResolve", "WhichResolve",
				"PairType",
				"DeterminerHeadMatch", "Longer2", "LongerPN2", "ShorterPN2", "InOfRelation"//,"__PairType__SentNum"
			};
			secondFeatures = Constructor.createFeatures(seoncdFeatures);
		}
		return secondFeatures;
	}
	private double[] getSecondLevelVector(Annotation np1, Annotation np2, Document doc) {
		List<Feature> f2s = getSecondLevelFeatures();
		HashMap<Feature, String> featVector = new HashMap<Feature, String>();
		for(int i = 0; i < f2s.size(); i++){
			f2s.get(i).getValue(np1, np2, doc, featVector);
		}
		if (bin2 == null) {
			bin2 = new Binarizer(getSecondLevelFeatures());
		}
		return bin2.binarize(featVector);
		//return getBinarizer().binarize(featVector);
	}
	
	private List<Property> getMentionProperties() {
		if (mentionProperties == null) {
			String ppty[] = {
					"", ""
			};
			//mentionProperties = Constructor.
		}
		return mentionProperties;
	}
	
	//private double[] getMentionPropertyFeatureVec(Annotation np, Document doc) {
	//	return null;
	//}

	public void loadSVMModel() {
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// testing part
	////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void filterMentionsConll2012(Iterable<Document> docs) {
		for (Document doc : docs) {
			filterMentionsConll2012SVM(doc); // level 1
			filterMentionsConll2012J48(doc); // level 2
		}
	}
	
	public void filterMentionsConll2012_SVMonly(Iterable<Document> docs) {
		for (Document doc : docs) {
			filterMentionsConll2012SVM(doc); // level 1
		}
	}
	
	public void filterMentionsConll2012_J48only(Iterable<Document> docs) {
		for (Document doc : docs) {
			filterMentionsConll2012J48(doc); // level 2
		}
	}
	
	//public void filterMentionsConll2012_level2(Iterable<Document> docs) {
		//for (Document doc : docs) {
		//	filterMentionsConll2012_Pair(doc);
		//}
	//}
	
	public void filterMentionsConll2012_BestPair(Iterable<Document> docs) {
		for (Document doc : docs) {
			//filterMentionsConll2012_Pair(doc);
			filterMentionsConll2012_PairAverage(doc);
		}
	}
	
	// filter testing (single doc)
	
	//mention pair classifier approach
	/**
	 * Pick the top k "best" mention pairs, we assume that these k pairs contains
	 * the "max" pair. Then ask the classifier to predict a label for the "best" pair
	 * as the label of this mention.
	 */
	public AnnotationSet filterMentionsConll2012_PairConcatinate(Document doc) {

		AnnotationSet nps = doc.getAnnotationSet(Constants.NP);

		// only do the filter for the docs with more then 10 mentions
		if (nps.size() < 10) {
			// do nothing when doc is very short
			return nps;
		}

		System.out.println("J48 TOP-2 filter is filtering mentions for doc " + doc.getAbsolutePath());

		HashSet<Integer> shouldRemove = new HashSet<Integer>();

		if (pairRanker == null) {
			pairRanker = new UMassRankLib();
			pairRanker.loadModelFile(bestPairModelPath); // load best-pair model
		}

		//AnnotationSet tokens = doc.getAnnotationSet(Constants.TOKEN);
		//AnnotationSet gsnps = doc.getAnnotationSet(Constants.GS_NP);

		for (Annotation np1 : nps) {

			int sequenceID = np1.getId();
			int id1 = Integer.parseInt(np1.getAttribute(Constants.CE_ID));

			Annotation bestParnter = null;
			ArrayList<NPWithScore> npRanking = new ArrayList<NPWithScore>();
			double bestEdgeScore = -Double.MAX_VALUE;
			for (Annotation np2 : nps) {
				int id2 = Integer.parseInt(np2.getAttribute(Constants.CE_ID));
				if (id1 != id2) { // two 
					double[] singlePair = getSecondLevelVector(np1, np2, doc);
					double edgeScore = pairRanker.getRankerScore(singlePair);
					NPWithScore thisMenSc = new NPWithScore();
					thisMenSc.np = np2;
					thisMenSc.score = edgeScore;
					npRanking.add(thisMenSc);
					if (edgeScore > bestEdgeScore) {
						bestEdgeScore = edgeScore;
						bestParnter = np2;
					}
				}
			}

			// sort
			Collections.sort(npRanking, new EdgeComparator());

			/*
				double[] bestPair = getSecondLevelVector(np1, bestParnter, doc);
				featureWriter.print(label);
				for (int j = 0; j < bestPair.length; j++) {
					featureWriter.print(" " + (j+1) + ":" + bestPair[j]);
				}
				featureWriter.println();
			 */

			// pick top 3
			int topk = 2;
			int dimention = 0;
			double[] allFeatureInOneLine = new double[4096];
			if (npRanking.size() >= topk) { // top k
				int dem = 0;
				for (int i = 0; i < topk; i++) {
					Annotation topNp = npRanking.get(i).np;
					double[] topPairFeature = getSecondLevelVector(np1, topNp, doc);
					for (int j = 0; j < topPairFeature.length; j++) {
						//featureWriter.print(" " + (dem+1) + ":" + topPairFeature[j]);
						allFeatureInOneLine[dem] = topPairFeature[j];
						dem++;
					}
				}
				dimention =  dem;
			} else { // only top 1
				double[] bestPair = getSecondLevelVector(np1, bestParnter, doc);
				int dem = 0;
				for (int i = 0; i < topk; i++) {
					for (int j = 0; j < bestPair.length; j++) {
						//featureWriter.print(" " + (dem+1) + ":" + bestPair[j]);
						allFeatureInOneLine[dem] = bestPair[j];
						dem++;
					}
				}
				dimention =  dem;
			}

			double[] featVec = new double[dimention]; // final feature vector dimension
			for (int i = 0; i < dimention; i++) {
				featVec[i] = allFeatureInOneLine[i];
			}

			// do predict!
			double predVal = decisionTree.predict(featVec);
			//System.out.println(predVal);
			if (predVal <= 0) {
				shouldRemove.add(sequenceID);
			}
		}

		// do removing!
		for (Integer removeID : shouldRemove) {
			Annotation rnp = nps.get(removeID);
			//System.out.println("Removing mention: " + doc.getAnnotString(rnp));
			nps.remove(rnp);
		}

		return nps;
	}
	
	public AnnotationSet filterMentionsConll2012_PairAverage(Document doc) {

		AnnotationSet nps = doc.getAnnotationSet(Constants.NP);

		// only do the filter for the docs with more then 10 mentions
		if (nps.size() < 10) {
			// do nothing when doc is very short
			return nps;
		}

		System.out.println("J48 Average TOP-4 filter is filtering mentions for doc " + doc.getAbsolutePath());

		HashSet<Integer> shouldRemove = new HashSet<Integer>();

		if (pairRanker == null) {
			pairRanker = new UMassRankLib();
			pairRanker.loadModelFile(bestPairModelPath); // load best-pair model
		}

		for (Annotation np1 : nps) {

			int sequenceID = np1.getId();
			int id1 = Integer.parseInt(np1.getAttribute(Constants.CE_ID));

			Annotation bestParnter = null;
			ArrayList<NPWithScore> npRanking = new ArrayList<NPWithScore>();
			double bestEdgeScore = -Double.MAX_VALUE;
			for (Annotation np2 : nps) {
				int id2 = Integer.parseInt(np2.getAttribute(Constants.CE_ID));
				if (id1 != id2) { // two 
					double[] singlePair = getSecondLevelVector(np1, np2, doc);
					double edgeScore = pairRanker.getRankerScore(singlePair);
					NPWithScore thisMenSc = new NPWithScore();
					thisMenSc.np = np2;
					thisMenSc.score = edgeScore;
					npRanking.add(thisMenSc);
					if (edgeScore > bestEdgeScore) {
						bestEdgeScore = edgeScore;
						bestParnter = np2;
					}
				}
			}

			// sort
			Collections.sort(npRanking, new EdgeComparator());
			
			// pick top k
			int topk = 4;
			if (npRanking.size() < topk) {
				topk = npRanking.size();
			}
			double[] featVec = null;
			for (int i = 0; i < topk; i++) {
				Annotation topNp = npRanking.get(i).np;
				double[] topPairFeature = getSecondLevelVector(np1, topNp, doc);
				if (featVec == null) {
					featVec = new double[topPairFeature.length];
					GreedyPolicy.vectorClearZero(featVec);
				}
				GreedyPolicy.addArray(featVec, topPairFeature);
			}
			if (topk != 0) { // in case nps.size() = 1
				GreedyPolicy.divideArray(featVec, (double)topk);// do an average
			}
			
			// do predict!
			double predVal = decisionTree.predict(featVec);
			//System.out.println(predVal);
			if (predVal <= 0) {
				shouldRemove.add(sequenceID);
			}
		}

		
		// do removing!
		for (Integer removeID : shouldRemove) {
			Annotation rnp = nps.get(removeID);
			//System.out.println("Removing mention: " + doc.getAnnotString(rnp));
			nps.remove(rnp);
		}

		return nps;
	}
	
	// using J48
	public AnnotationSet filterMentionsConll2012J48(Document doc) {
		AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
		
		// only do the filter for the docs with more then 10 mentions
		if (nps.size() < 10) {
			// do nothing when doc is very short
			return nps;
		}
		
		System.out.println("J48 filtering mentions for doc " + doc.getAbsolutePath());
		
		HashSet<Integer> shouldRemove = new HashSet<Integer>();
		
		int instanceIndex = -1;
		for (Annotation np1 : nps) {
			instanceIndex++;
			int sequenceID = np1.getId();
			int id1 = Integer.parseInt(np1.getAttribute(Constants.CE_ID));
			double[] averageMentionPairFeautures = null;
			double   npair = 0;

			//feature vector for a mention
			for (Annotation np2 : nps) {
				int id2 = Integer.parseInt(np2.getAttribute(Constants.CE_ID));
				if (id1 != id2) { // two 
					double[] singlePair = getLocalFeatureVector(np1, np2, doc);
					if (averageMentionPairFeautures == null) {
						averageMentionPairFeautures = new double[singlePair.length];
						GreedyPolicy.vectorClearZero(averageMentionPairFeautures);
					}
					GreedyPolicy.addArray(averageMentionPairFeautures, singlePair);
					npair++;
				}
			}
			if (npair != 0) { // in case nps.size() = 1
				GreedyPolicy.divideArray(averageMentionPairFeautures, npair);// do an average
			}
			
			
			// do predict!
			double predVal = decisionTree.predict(averageMentionPairFeautures);
			//System.out.println(predVal);
			if (predVal <= 0) {
				shouldRemove.add(sequenceID);
			}
			
		}
		
		// do removing!
		for (Integer removeID : shouldRemove) {
			Annotation rnp = nps.get(removeID);
			//System.out.println("Removing mention: " + doc.getAnnotString(rnp));
			nps.remove(rnp);
		}
		
		return nps;
	}
	
	// using svm
	public AnnotationSet filterMentionsConll2012SVM(Document doc) {
		AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
		
		// only do the filter for the docs with more then 10 mentions
		if (nps.size() < 10) {
			// do nothing when doc is very short
			return nps;
		}
		
		System.out.println("SVM-perf filtering mentions for doc " + doc.getAbsolutePath());
		
		PrintWriter tmpFeatureFile = null;
		try {
			tmpFeatureFile = new PrintWriter(new File(svmperf_model_folder + "/" + tempFeatureFileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		HashSet<Integer> shouldRemove = new HashSet<Integer>();
		HashMap<Integer, Integer> instanceMentionID = new HashMap<Integer, Integer>();
		
		int instanceIndex = -1;
		for (Annotation np1 : nps) {
			instanceIndex++;
			int sequenceID = np1.getId();
			int id1 = Integer.parseInt(np1.getAttribute(Constants.CE_ID));
			double[] averageMentionPairFeautures = null;
			double   npair = 0;

			//feature vector for a mention
			for (Annotation np2 : nps) {
				int id2 = Integer.parseInt(np2.getAttribute(Constants.CE_ID));
				if (id1 != id2) { // two 
					double[] singlePair = getLocalFeatureVector(np1, np2, doc);
					if (averageMentionPairFeautures == null) {
						averageMentionPairFeautures = new double[singlePair.length];
						GreedyPolicy.vectorClearZero(averageMentionPairFeautures);
					}
					GreedyPolicy.addArray(averageMentionPairFeautures, singlePair);
					npair++;
				}
			}
			
			if (npair != 0) { // in case nps.size() = 1
				GreedyPolicy.divideArray(averageMentionPairFeautures, npair);// do an average
			}
			
			tmpFeatureFile.print("1");
			for (int j = 0; j < averageMentionPairFeautures.length; j++) {
				tmpFeatureFile.print(" " + (j+1) + ":" + averageMentionPairFeautures[j]);
			}
			tmpFeatureFile.println();
			
			
			instanceMentionID.put(instanceIndex, sequenceID);
			
			// predict value
			//double pred_value = 
			//if () {
				
			//}
		}
		tmpFeatureFile.flush();
		tmpFeatureFile.close();
		
		// run
		String exes = (svmperf_classify_path + "/"  + svmperf_classify_exe);
		String featfile = svmperf_model_folder + "/" + tempFeatureFileName;
		String modelf = svmperf_model_file;
		String args = featfile + " " + modelf;
		HashMap<Integer, Double> indexScore = runSVMperfClassify(exes, featfile + " " + modelf);
		
		// see the predict score
		for (Integer instanceID : indexScore.keySet()) {
			double predScore = indexScore.get(instanceID).doubleValue();
			if (predScore < 0) {
				int mentionID = instanceMentionID.get(instanceID).intValue();
				shouldRemove.add(mentionID);
			}
		}
		
		// do removing!
		for (Integer removeID : shouldRemove) {
			Annotation rnp = nps.get(removeID);
			//System.out.println("Removing mention: " + doc.getAnnotString(rnp));
			nps.remove(rnp);
		}
		
		return nps;
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	// Filter Training
	/////////////////////////////////////////////////////////////////////////////////////
	
	/*
	public void filterTrainingSingleDoc(Document doc) {
	}
	*/
	
	public void filterTrainingAllDocs(Iterable<Document> docs) {

		// no pruning performance with Reconcile mention extractor
		// =========================================================================================================================
		// | Recall: 17305.0 / 19764.0 = 87.55818660190245 Precision: 17305.0 / 45699.0 = 37.86734939495394 F-1: 52.869559904067955
		// =========================================================================================================================
		
		PrintWriter featureWriter = null;
		String mentionClassifierFeatureFile = "mentionFilterFeature_level2_train500.txt";///"mentionUIUCFeature_train1.txt";//"mentionFilterFeature_train.txt";
		try {
			featureWriter = new PrintWriter(new File(mentionClassifierFeatureFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (Document doc : docs) {
			System.out.println("Doc: " + doc.getDocumentId());
			
			AnnotationSet tokens = doc.getAnnotationSet(Constants.TOKEN);
			AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
			AnnotationSet gsnps = doc.getAnnotationSet(Constants.GS_NP);

			for (Annotation np1 : nps) {
				boolean isMatched = Boolean.parseBoolean(np1.getAttribute(Constants.IS_MATCHED));
				int label = -1;
				if (isMatched) label = 1;
				int id1 = Integer.parseInt(np1.getAttribute(Constants.CE_ID));
				double[] averageMentionPairFeautures = null;
				double   npair = 0;
				
				for (Annotation np2 : nps) {
					int id2 = Integer.parseInt(np2.getAttribute(Constants.CE_ID));
					if (id1 != id2) { // two 
						double[] singlePair = getLocalFeatureVector(np1, np2, doc);
						if (averageMentionPairFeautures == null) {
							averageMentionPairFeautures = new double[singlePair.length];
							GreedyPolicy.vectorClearZero(averageMentionPairFeautures);
						}
						GreedyPolicy.addArray(averageMentionPairFeautures, singlePair);
						npair++;
					}
				}
				
				// do an average
				if (npair > 0) {
					GreedyPolicy.divideArray(averageMentionPairFeautures, npair);
				}
				
				// output?
				if (npair > 0) {
					featureWriter.print(label);
					for (int j = 0; j < averageMentionPairFeautures.length; j++) {
						featureWriter.print(" " + (j+1) + ":" + averageMentionPairFeautures[j]);
					}
					featureWriter.println();
				}
			}
			
			featureWriter.flush();	
		}
	}
	
	public void filterTrainingBestEdge(Iterable<Document> docs) {
		
		UMassRankLib ranker = null;
		PrintWriter featureWriter = null, avgFeatWriter = null;;
		String mentionClassifierFeatureFile = "mentionUIUCFeature_train2.txt";//"mentionFilterFeature_train.txt";
		try {
			featureWriter = new PrintWriter(new File(mentionClassifierFeatureFile));
			avgFeatWriter = new PrintWriter(new File("avg-topk-"+mentionClassifierFeatureFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// load best-pair model
		if (ranker == null) {
			ranker = new UMassRankLib();
			ranker.loadModelFile(bestPairModelPath); // load best-pair model
		}
		
		for (Document doc : docs) {
			System.out.println("Doc: " + doc.getDocumentId());
			
			AnnotationSet tokens = doc.getAnnotationSet(Constants.TOKEN);
			AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
			AnnotationSet gsnps = doc.getAnnotationSet(Constants.GS_NP);

			for (Annotation np1 : nps) {
				boolean isMatched = Boolean.parseBoolean(np1.getAttribute(Constants.IS_MATCHED));
				int label = -1;
				if (isMatched) label = 1;
				int id1 = Integer.parseInt(np1.getAttribute(Constants.CE_ID));
				//double[] averageMentionPairFeautures = null;
				//double   npair = 0;
				
				Annotation bestParnter = null;
				ArrayList<NPWithScore> npRanking = new ArrayList<NPWithScore>();
				double bestEdgeScore = -Double.MAX_VALUE;
				for (Annotation np2 : nps) {
					int id2 = Integer.parseInt(np2.getAttribute(Constants.CE_ID));
					if (id1 != id2) { // two 
						//double[] singlePair = getLocalFeatureVector(np1, np2, doc);
						double[] singlePair = getSecondLevelVector(np1, np2, doc);
						double edgeScore = ranker.getRankerScore(singlePair);
						NPWithScore thisMenSc = new NPWithScore();
						thisMenSc.np = np2;
						thisMenSc.score = edgeScore;
						npRanking.add(thisMenSc);
						if (edgeScore > bestEdgeScore) {
							bestEdgeScore = edgeScore;
							bestParnter = np2;
						}
					}
				}
				
				// sort
				Collections.sort(npRanking, new EdgeComparator());
				
				/*
				double[] bestPair = getSecondLevelVector(np1, bestParnter, doc);
				featureWriter.print(label);
				for (int j = 0; j < bestPair.length; j++) {
					featureWriter.print(" " + (j+1) + ":" + bestPair[j]);
				}
				featureWriter.println();
				*/
				
				double[] averageMentionPairFeautures = null;
				
				// pick top k
				int topk = 4;
				if (npRanking.size() >= topk) { // pick top k
					featureWriter.print(label);
					int dem = 0;
					for (int i = 0; i < topk; i++) {
						Annotation topNp = npRanking.get(i).np;
						double[] topPairFeature = getSecondLevelVector(np1, topNp, doc);
						for (int j = 0; j < topPairFeature.length; j++) {
							featureWriter.print(" " + (dem+1) + ":" + topPairFeature[j]);
							dem++;
						}
						
						// make sum of features (for average)
						if (averageMentionPairFeautures == null) {
							averageMentionPairFeautures = new double[topPairFeature.length];
							GreedyPolicy.vectorClearZero(averageMentionPairFeautures);
						}
						GreedyPolicy.addArray(averageMentionPairFeautures, topPairFeature);
					}
					featureWriter.println();
					
					// for average feature representation
					GreedyPolicy.divideArray(averageMentionPairFeautures, (double)topk);
					avgFeatWriter.print(label);
					for (int j = 0; j < averageMentionPairFeautures.length; j++) {
						avgFeatWriter.print(" " + (j+1) + ":" + averageMentionPairFeautures[j]);
					}
					avgFeatWriter.println();
					
				} else { // only top 1
					
					double[] bestPair = getSecondLevelVector(np1, bestParnter, doc);
					featureWriter.print(label);
					int dem = 0;
					for (int i = 0; i < topk; i++) {
						for (int j = 0; j < bestPair.length; j++) {
							featureWriter.print(" " + (dem+1) + ":" + bestPair[j]);
							dem++;
						}
						
						// make sum of features (for average)
						if (i < npRanking.size()) {
							Annotation topNp = npRanking.get(i).np;
							double[] topPairFeature = getSecondLevelVector(np1, topNp, doc);
							if (averageMentionPairFeautures == null) {
								averageMentionPairFeautures = new double[topPairFeature.length];
								GreedyPolicy.vectorClearZero(averageMentionPairFeautures);
							}
							GreedyPolicy.addArray(averageMentionPairFeautures, topPairFeature);
						}
					}
					featureWriter.println();
					
					// output average feature of top k
					if (npRanking.size() > 0) {
						GreedyPolicy.divideArray(averageMentionPairFeautures, (double)npRanking.size());
						avgFeatWriter.print(label);
						for (int j = 0; j < averageMentionPairFeautures.length; j++) {
							avgFeatWriter.print(" " + (j+1) + ":" + averageMentionPairFeautures[j]);
						}
						avgFeatWriter.println();
					}
				}
				
			}
			
			featureWriter.flush();
			avgFeatWriter.flush();
		}
		
		featureWriter.close();
		avgFeatWriter.close();
	}
	
	public void filterTrainingLevel2(Iterable<Document> docs) {
		
		int maxSentDist = 0;
		int lostPartnerMatchMention = 0;
		PrintWriter feat2Writer = null;
		PrintWriter pair2Writer = null;
		String mentionClassifierFeatureFile = "pick_best_edge_feature.txt";//"mentionFilterFeature_level2_test.txt";//"mentionFilterFeature_level2_train500.txt";
		
		try {
			feat2Writer = new PrintWriter(new File(mentionClassifierFeatureFile));
			pair2Writer = new PrintWriter(new File("pairInstance.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int posPair = 0;
		int negPair = 0;
		int nmatch = 0;
		for (Document doc : docs) {
			int maxDisThisDoc = 0;
			System.out.println("Doc: " + doc.getDocumentId());
			pair2Writer.println("Doc " + doc.getDocumentId());
			
			AnnotationSet tokens = doc.getAnnotationSet(Constants.TOKEN);
			AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
			AnnotationSet gsnps = doc.getAnnotationSet(Constants.GS_NP);

			
			for (Annotation np1 : nps) {
				ArrayList<String> allnppair = new ArrayList<String>();
				ArrayList<String> edgeFeauteLines = new ArrayList<String>();
				int id1 = Integer.parseInt(np1.getAttribute(Constants.CE_ID));
				boolean isMatched1 = Boolean.parseBoolean(np1.getAttribute(Constants.IS_MATCHED));
				boolean lostPartner1 = false;
				int cid1 = -1;
				if (isMatched1) {
					Integer matchID1 = (Integer)np1.getProperty(Property.MATCHED_CE);
					Annotation match1 = gsnps.getAnnotationByNO(matchID1.intValue());
					cid1 = Integer.parseInt(match1.getAttribute(Constants.CLUSTER_ID));
					nmatch++;
				}

				int matchCount = 0;
				for (Annotation np2 : nps) {
					int id2 = Integer.parseInt(np2.getAttribute(Constants.CE_ID));
					//int cid = (Integer)np2.getProperty(Property.MATCHED_CE);
					if (id1 != id2) { // two 
						double[] singlePair = getSecondLevelVector(np1, np2, doc);
						int label = 0;
						//if (averageMentionPairFeautures == null) {
						//	averageMentionPairFeautures = new double[singlePair.length];
						//	GreedyPolicy.vectorClearZero(averageMentionPairFeautures);
						//}
						//GreedyPolicy.addArray(averageMentionPairFeautures, singlePair);
						//npair++;
						if (!isMatched1) {
							label = -1;
						} else {
							boolean isMatched2 = Boolean.parseBoolean(np2.getAttribute(Constants.IS_MATCHED));
							if (!isMatched2) {
								label = -1;
							} else {
								Integer matchID2 = (Integer)np2.getProperty(Property.MATCHED_CE);
								Annotation match2 = gsnps.getAnnotationByNO(matchID2.intValue());
								int cid2 = Integer.parseInt(match2.getAttribute(Constants.CLUSTER_ID));
								
								if (cid1 == cid2) {
									label = 1;
									matchCount++;
									int dis = FeatureUtils.getSentDistance(np1, np2, doc);
									if (dis > maxSentDist) {
										maxSentDist = dis; // max distance
									}
									if (dis > maxDisThisDoc) {
										maxDisThisDoc = dis; // max distance for this doc
									}
								} else {
									label = -1;
								}
								
							}
						}
						
						////////////////////////////////////////////
						if (label > 0) {
							posPair++;
						} else {
							negPair++;
						}
						allnppair.add("pair " + np1.getId() + " " + np2.getId() + " " + label);
						
						/*
						// svm format
						feat2Writer.print(label);
						for (int j = 0; j < singlePair.length; j++) {
							feat2Writer.print(" " + (j+1) + ":" + singlePair[j]);
						}
						feat2Writer.println();*/
						
						// ranklib format
						//if (isMatched1)
						String thisline = "";
						thisline += String.valueOf((label + 1) / 2);
						thisline += (" qid:" + String.valueOf(nmatch));
						for (int j = 0; j < singlePair.length; j++) {
							if (singlePair[j] > 0) {
								thisline += (" " + (j+1) + ":" + singlePair[j]);
							}
						}
						edgeFeauteLines.add(thisline);
					}
				}
				
				if (isMatched1 && (matchCount == 0)) {
					// lose the matched mention during prediction
					lostPartnerMatchMention++;
					lostPartner1 = true;
				}
				
				if (isMatched1){// && !lostPartner1) {
					
					for (String featline : edgeFeauteLines){
						if (lostPartner1) {
							featline.replaceAll("qid:0", "qid:1");
						}
						feat2Writer.println(featline);
					}
				}
				
				
				
				int ll = -1;
				if (isMatched1 && !lostPartner1) ll = 1;
				pair2Writer.println("np " + np1.getId() + " " + ll);
				for (String sline : allnppair) {
					pair2Writer.println(sline);
				}
			}
			
			
			double nsent = doc.getAnnotationSet(Constants.SENT).size();
			double num = maxDisThisDoc;
			double rate = num / nsent;
			System.out.println(num + " " + nsent + " " + rate);
			
			
			//feat2Writer.println(num + " " + nsent + " " + rate);
			feat2Writer.flush();
			
			// have a look after each doc
			System.out.println("Current lost partner matched mentions: " + lostPartnerMatchMention + "/" + nmatch);
			System.out.println("Current max sentence distance: " + maxSentDist);
			

			pair2Writer.flush();
		}
		
		System.out.println("Number of instances: Total: " + (posPair+negPair) + " posPair " + posPair + " negPair " + negPair);
		System.out.println("Lost partner matched mentions: " + lostPartnerMatchMention + "/" + nmatch);
		System.out.println("Max sentence distance: " + maxSentDist);
		
	}
	
	
	
	public void filterPrintAllDocs(Iterable<Document> docs) {
		int i = 0;
		BerkeleyFeatureGenerator bkfg = new BerkeleyFeatureGenerator();
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new File("matched_mention_looks_like.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (Document doc : docs) {
			writer.println("Doc: " + doc.getDocumentId());
			AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
			AnnotationSet gsnps = doc.getAnnotationSet(Constants.GS_NP);
			
			/*
			for (Annotation match : nps) {
				if (isMatched(match)) {
					writer.println("1 " + doc.getAnnotString(match));
				}
			}
			
			for (Annotation match : nps) {
				if (!isMatched(match)) {
					writer.println("0 " + doc.getAnnotString(match));
				}
			}
			*/
			
			/*
			int n1 = nps.size();
			System.out.println("nps = " + nps.size());
			MentionFilter.removeUnmatchedPredictMentions(doc);
			System.out.println("matched-nps = " + nps.size());
			nps = doc.reloadAnnotationSet(Constants.NP);
			int n2 = nps.size();
			System.out.println("reload-nps = " + nps.size());		
			assert(n1 == n2);
			
			*/
			
			// have a look at the properties
			///for (Annotation np : nps) {
			//	bkfg.printForDebug(np, doc);
			//}
			
			
			BerkeleyErrorCounter.MentionCategoryWrtGold(doc);
			
			
			writer.println("==========================================");
		}
		
		
	}
	
	public void filterTraining(Iterable<Document> docs) {
		
		// prepare for the documents
		prepareDocument(docs);
		
		// the initial score
		System.out.println("=== Very Begining Mention Dection Accuracy ====");
		double[] beforeScores = Scoring.mentionDetectionAccuracy(docs);
		System.out.println("===============================================");
		
		// match
		labelMatchedPredictMention(docs);
		
		// train (level 1)
		//filterTrainingAllDocs(docs);
		filterTrainingBestEdge(docs);
		
		// level 1 filtering (high recall level)
		//filterMentionsConll2012(docs);
		//filterMentionsConll2012_SVMonly(docs);
		
		// train (level 2)
		//filterTrainingLevel2(docs);
		//filterTrainingAllDocs(docs);
		
		
		// after process score
		System.out.println("=== After Filtering Mention Dection Accuracy ====");
		double[] afterScores = Scoring.mentionDetectionAccuracy(docs);
		System.out.println("=================================================");
	}
	
	public static void prepareDocument(Iterable<Document> docs) {
		int i = 0;
		for (Document doc : docs) {
			doc.loadAnnotationSets();

			System.out.println("MMMatching NPs ...");
			AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
			AnnotationSet gsnps = doc.getAnnotationSet(Constants.GS_NP);
			/*
			if (Utils.getConfig().getDataset().equals("muc6")) {
				System.out.println("Matching muc6 document ...");
				Matcher.matchAnnotationSets(gsnps, nps, MatchStyleEnum.MUC, doc);
			} else if (Utils.getConfig().getDataset().equals("ace04")) {
				System.out.println("Matching ace04 document ...");
				Matcher.matchAnnotationSets(gsnps, nps, MatchStyleEnum.ACE, doc);
			} else if (Utils.getConfig().getDataset().contains("ontonotes")) {
				System.out.println("Matching Ontonotes document ...");
				Matcher.matchAnnotationSets(gsnps, nps, MatchStyleEnum.ONTO, doc);
			}*/
			System.out.println("Matching Ontonotes document ...");
			Matcher.matchAnnotationSets(gsnps, nps, MatchStyleEnum.ONTO, doc);
		}
	}
	
	
	
	public static void labelMatchedPredictMention(Iterable<Document> docs) {
		prepareDocument(docs); // prepare to match mentions, if needed
		for (Document doc : docs) {
			// label the match the mentions for each doc
			labelMatchedPredictMention(doc);
		}
	}
	public static void labelMatchedPredictMention(Document doc) {
		AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
		int beforeRemoving = nps.size();
		int afterRemoving = 0;
		int nRemoved = 0;

		// count firstly
		for (Annotation predictMention : nps) {
			if (predictMention.getProperty(Property.MATCHED_CE) == null) {
				predictMention.setAttribute(Constants.IS_MATCHED, "false");
				nRemoved++;
			} else if (((Integer)predictMention.getProperty(Property.MATCHED_CE)) < 0) {
				predictMention.setAttribute(Constants.IS_MATCHED, "false");
				nRemoved++;
			} else {
				// this is a matched mention
				predictMention.setAttribute(Constants.IS_MATCHED, "true");
				afterRemoving++;
			}
		}
		
		// statistic
		System.out.println("Total predict mentions: " + beforeRemoving + ", " + afterRemoving + " are matched!");
	}
	
///////////////////////////////////////////////////////////
// Some oracle filters for development
// NOTE: DO NOT use them in formal testing!!!
///////////////////////////////////////////////////////////

	public void filterDebugRun(Iterable<Document> docs) {
		
		double dbgPrecision = config.getDouble("DEBUG_FILTER_PRE", 0.79);
		double dbgRecall = config.getDouble("DEBUG_FILTER_REC", 0.81);
		System.out.println("Debug Precision = " + dbgPrecision);
		System.out.println("Debug Recall    = " + dbgRecall);		
		
		// prepare for the documents
		prepareDocument(docs);
		
		// the initial score
		System.out.println("=== Very Begining Mention Dection Accuracy ====");
		double[] beforeScores = Scoring.mentionDetectionAccuracy(docs);
		System.out.println("===============================================");
		
		// match
		labelMatchedPredictMention(docs);
		
		// have a look at those mentions
		//filterPrintAllDocs(docs);
		
		// filtering 
		//filterMentionsConll2012(docs);
		
		//removeAccordingF1(docs, 0.79, 0.81);
		removeAccordingF1(docs, dbgPrecision, dbgRecall);
		
		// after process score
		System.out.println("=== After Filtering Mention Dection Accuracy ====");
		double[] afterScores = Scoring.mentionDetectionAccuracy(docs);
		System.out.println("=================================================");
	}
	
	// remove unmatched predict mentions
	// DO NOT allowed to use in formal testing
	public static void removeUnmatchedPredictMentions(Iterable<Document> docs){
		prepareDocument(docs); // prepare to match mentions, if needed
		for (Document doc : docs) {
			removeUnmatchedPredictMentions(doc);
		}
	}
	// remove unmatched predict mentions
	// DO NOT allowed to use in formal testing
	public static void removeUnmatchedPredictMentions(Document doc){

		AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
		int beforeRemoving = nps.size();
		int afterRemoving = 0;
		int nRemoved = 0;

		// count firstly
		HashSet<Integer> ShouldRemoveIDs = new HashSet<Integer>();
		for (Annotation predictMention : nps) {
			if (predictMention.getProperty(Property.MATCHED_CE) == null) {
				ShouldRemoveIDs.add(predictMention.getId());
				nRemoved++;
			} else if (((Integer)predictMention.getProperty(Property.MATCHED_CE)) < 0) {
				ShouldRemoveIDs.add(predictMention.getId());
				nRemoved++;
			} else {
				// this is a matched mention
				afterRemoving++;
			}
		}

		// do remove
		for (Integer removeMentionID : ShouldRemoveIDs) {
			Annotation rmen = nps.get(removeMentionID.intValue());
			nps.remove(rmen);
		}
		
		// statistic
		System.out.println("Total predict mentions: " + beforeRemoving + ", " + afterRemoving + " left, " + nRemoved + " were removed...");
		
	}
	
	// remove umatched predict mentions given a particular precision and recall
	// DO NOT allowed to use in formal testing
	public static void removeAccordingF1(Iterable<Document> docs, double precision, double recall) {
		assert(precision > 0 && precision < 1.0);
		assert(recall > 0 && recall < 1.0);
		
		
		// collect all nps
		int gid, docid;
		ArrayList<Document> doclist = new ArrayList<Document>();
		HashMap<Integer, Integer> gidToDocID = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> gidToMentionID = new HashMap<Integer, Integer>();
		
		int totalGoldSize = 0;
		ArrayList<Integer> matched = new ArrayList<Integer>();
		ArrayList<Integer> unmatched = new ArrayList<Integer>();
		
		gid = 0;
		docid = 0;
		for (Document doc : docs) {
			
			doclist.add(doc);
			
			AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
			AnnotationSet gsnps = doc.getAnnotationSet(Constants.GS_NP);
			
			totalGoldSize += gsnps.size();
				
			for (Annotation np : nps) {

				int mid = np.getId();
				gidToDocID.put(gid, docid);
				gidToMentionID.put(gid, mid);
				
				if (isMatched(np)) {
					matched.add(gid);
				} else {
					unmatched.add(gid);
				}

				gid++;
			}
			
			docid++;
		}
		
		double totalNPSize = (double)gid;
		
		// get a number of unmatched the need to be removed
		int positiveOverlap = (int) (totalGoldSize * recall);
		int matchRemoved = matched.size() - positiveOverlap;
		
		// get a number of matched that need to be removed
		int totalLeft = (int)(((double)positiveOverlap) / precision) - positiveOverlap;
		int unmatchRemoved = unmatched.size() - totalLeft;
		
		System.out.println("Recall:" + recall + " remove matched " + matchRemoved);
		System.out.println("Precision:" + precision + " remove unmatched " + unmatchRemoved);
		
		HashSet<Integer> matchRemovedSet = randomPick(matched, matchRemoved);
		HashSet<Integer> unmatchRemovedSet = randomPick(unmatched, unmatchRemoved);
		
		// remove
		for (Integer matechGID : matchRemovedSet) {
			Document dc = doclist.get(gidToDocID.get(matechGID.intValue()).intValue());
			int id = gidToMentionID.get(matechGID.intValue());
			
			AnnotationSet npset = dc.getAnnotationSet(Constants.NP);
			Annotation rnp = npset.get(id);
			npset.remove(rnp);
		}
		for (Integer unmatechGID : unmatchRemovedSet) {
			Document dc = doclist.get(gidToDocID.get(unmatechGID.intValue()).intValue());
			int id = gidToMentionID.get(unmatechGID.intValue());
			
			AnnotationSet npset = dc.getAnnotationSet(Constants.NP);
			Annotation rnp = npset.get(id);
			npset.remove(rnp);
		}
	}
	
	// random pick n numbers from numberList, return the selected set
	private static HashSet<Integer> randomPick(ArrayList<Integer> numberList, int n) {
		int count = 0;
		HashSet<Integer> result = new HashSet<Integer>();
		if (n == 0) return result;
		Random random = new Random(System.currentTimeMillis());

		while (count < n) {
			int randIndex = ((int)(random.nextDouble() * numberList.size()) % numberList.size());
			result.add(numberList.get(randIndex));
			numberList.remove(randIndex);
			count++;
		}
		return result;
	}
	
	// utils
	private  static boolean isMatched(Annotation mention) {
		String matchStr = mention.getAttribute(Constants.IS_MATCHED);
		if (matchStr != null) {
			return (Boolean.parseBoolean(matchStr));
		}
		return false;
	}
	
	private String getRandomFilename() {
		Random random = new Random(System.currentTimeMillis());
		int n1 = (int)(random.nextDouble() * 65536);
		int n2 = (int)(random.nextDouble() * 65536);
		int n3 = (int)(random.nextDouble() * 65536);
		int n4 = (int)(random.nextDouble() * 65536);
		String fn = new String(	"tmpNPSfeatures" + String.valueOf(n1) + String.valueOf(n2) + String.valueOf(n3) + String.valueOf(n4) + ".txt");
		return fn;
	}
	
	// return a hashmap which map the instance index to their testing score
	private HashMap<Integer, Double> runSVMperfClassify(String scriptPath, String scriptArg) {
		assert(scriptPath != null && 
				scriptArg != null);

		System.out.println("Running script: " + scriptPath + " "+ scriptArg);

		File scriptFile = new File(scriptPath);
		String path = scriptFile.getParent();
		String scName = scriptFile.getName();
		String resultStr = null;
		HashMap<Integer, Double> indexScore = null; 

		try {
			Process p = Runtime.getRuntime().exec(scriptPath + " " + scriptArg);
			p.waitFor(); 

			// outputs
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ( (line = br.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			resultStr = builder.toString();
			// parse it!
			indexScore = parseOutput(resultStr);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//System.out.println(resultStr);
		return indexScore;
	}
	
	private HashMap<Integer, Double> parseOutput(String resultStr) {
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		
		String[] tokens = resultStr.split("\\s+");
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].equals("[output]")) {
				if ((i + 2) < tokens.length) {
					int index = Integer.parseInt(tokens[i + 1]);
					double score = Double.parseDouble(tokens[i + 2]);
					//System.out.println(index + " " + score);
					map.put(index, score);
				}
				i = i + 2;
			}
		}
		
		return map;
	}
	
	public static class EdgeComparator implements Comparator<NPWithScore> {
		@Override
		public int compare(NPWithScore arg0, NPWithScore arg1) {
			if (arg0.score > arg1.score) return -1;
			return 1;
		}
	}
}
