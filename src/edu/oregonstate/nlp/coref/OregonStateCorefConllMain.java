package edu.oregonstate.nlp.coref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

import edu.berkeley.nlp.futile.fig.basic.Option;
import edu.berkeley.nlp.futile.fig.exec.Execution;
import edu.oregonstate.nlp.coref.conll.ConllDocument;
import edu.oregonstate.nlp.coref.conll.ConllDocumentLoader;
import edu.oregonstate.nlp.coref.conll.ConllScriptScorer;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.individualFeature.PairClassifierScore;
import edu.oregonstate.nlp.coref.mentions.MentionExtractorFactory.MentExtractType;
import edu.oregonstate.nlp.coref.structuredClassifiers.GreedyPolicy;

public class OregonStateCorefConllMain implements Runnable {
	
	public static enum RunModel {
		CONLL_TRAIN, CONLL_TEST, UNKNOWN
	}
	
	@Option(gloss = "Run mode.")
	public static RunModel mode = RunModel.UNKNOWN;
	
	@Option(gloss = "Reconcile config file path.")
	public static String cfgPath = "config/empty.cfg";
	
	@Option(gloss = "Conll2012 training document folder.")
	public static String trainDocPath = "/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/train";
	@Option(gloss = "Conll2012 developing document folder.")
	public static String devDocPath = "/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/dev";
	@Option(gloss = "Conll2012 testing document folder.")
	public static String testDocPath = "/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/test";
	
	@Option(gloss = "Mention extractor type.")
	public static MentExtractType mentExtractor = MentExtractType.FileLoadedMentionExtractor;
	@Option(gloss = "Mention pair classifier score dump file path.")
	public static String mentionPairScoresFile = PairClassifierScore.pairScoringFilePath;

	@Option(gloss = "Scorer model file path.")
	public static String pruneScoreScorerPath = "xgb.model";
	@Option(gloss = "Scorer model file path.")
	public static String pruneScorePrunerPath = "xgb-pruner.model";
	
	@Override
	public void run() {
		if (mode == RunModel.CONLL_TRAIN) {
			runTraining();
		} else if (mode == RunModel.CONLL_TEST) {
			runTesting();
		} else if (mode == RunModel.UNKNOWN) {
			System.err.println("Please specify what you want to do.");
		}
	}

	// interface designed to process OntoNotes (for CoNLL2011 and CoNLL2012) datasets
	public static void main(String[] args) {
		OregonStateCorefConllMain man = new OregonStateCorefConllMain();
		Execution.run(args, man);
	}
	
	public static void runTesting() {
		// load configuration
		String configFilename = cfgPath;
				
		String defaultTrainFolder = trainDocPath; //"/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/train";
		String defaultDevFolder = devDocPath; //"/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/dev";
		String defaultTestFolder = testDocPath; //"/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/test";
				
		String testSuffix = "v9_auto_conll";

		try {

			SystemConfig globalCfg = new SystemConfig(configFilename);

			// Testing
			OregonStateCorefTestOnly.testConllDocLoading(globalCfg, defaultTestFolder, testSuffix, mentExtractor);

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

	}

	public static void runTraining() {
		
		// load configuration
		String configFilename = cfgPath;
				
		String defaultTrainFolder = trainDocPath; //"/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/train";
		String defaultDevFolder = devDocPath; //"/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/dev";
		String defaultTestFolder = testDocPath; //"/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/test";
		
		try {

			SystemConfig globalCfg = new SystemConfig(configFilename);

			GreedyPolicy pruneScorePolicy = new GreedyPolicy("policy_scoring", "ontonotes5_feat_predment", "featDump");
			//GreedyPolicy pruneScorePolicy = new GreedyPolicy("policy_scoring", "ontonotes5_feat_predgoldment", "featDump");
			//GreedyPolicy pruneScorePolicy = new GreedyPolicy("policy_scoring", "ontonotes5_feat_goldment", "featDump");
			ConllDocumentLoader trainLoader = new ConllDocumentLoader();
			ConllDocumentLoader devLoader = new ConllDocumentLoader();

			// set mention predictor
			pruneScorePolicy.setMentionExtractorType(mentExtractor);



			// load documents

			List<File> trainFileNames = collectAllFileInFolder(defaultTrainFolder, "v4_auto_conll");

			List<File> devFileNames = collectAllFileInFolder(defaultDevFolder, "v9_auto_conll");

			// load training documents
			trainLoader.clear(); // clear all
			trainLoader.loadConllDocumentList(trainFileNames); // load document
			List<ConllDocument> trainConllDocs = trainLoader.getConllList();
			List<Document> trainReconDocs = trainLoader.getReconList();


			// load  dev documents
			devLoader.clear(); // clear all
			devLoader.loadConllDocumentList(devFileNames); // load document
			List<ConllDocument> devConllDocs = devLoader.getConllList();
			List<Document> devReconDocs = devLoader.getReconList();

			// training model
			pruneScorePolicy.train(trainReconDocs, devReconDocs);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	

	
	public static List<File> collectAllFileInFolder(String folderPath, String extKeywords) {
		File folder = new File(folderPath);
		assert (folder.isDirectory());
		
		ArrayList<File> retList = new ArrayList<File>();
		
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String ext = listOfFiles[i].getName();
				if (ext.contains(extKeywords)) {
					retList.add(listOfFiles[i]);
					System.out.println("Reading: " + listOfFiles[i].getName());
				}
				
			}
		}
		
		System.out.println("Found " + retList.size() + " files.");
		return retList;
	}



}
