package edu.oregonstate.nlp.coref;


import java.io.File;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

import edu.oregonstate.nlp.coref.conll.ConllDocument;
import edu.oregonstate.nlp.coref.conll.ConllDocumentLoader;
import edu.oregonstate.nlp.coref.conll.ConllScriptScorer;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.mentions.MentionExtractorFactory.MentExtractType;
import edu.oregonstate.nlp.coref.structuredClassifiers.GreedyPolicy;

public class OregonStateCorefTestOnly {

	// interface designed to process OntoNotes (for CoNLL2011 and CoNLL2012) datasets
	public static void main(String[] args)
	{

		// load configuration
		String configFilename = "config/empty.cfg";
		
		String defaultTrainFolder = "/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/train";
		String defaultDevFolder = "/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/dev";
		String defaultTestFolder = "/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/test";
		
		String testSuffix = "v9_auto_conll";
		
		try {
			
			SystemConfig globalCfg = new SystemConfig(configFilename);
	
			// Testing
			testConllDocLoading(globalCfg, defaultTestFolder, testSuffix, MentExtractType.FileLoadedMentionExtractor);

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

	}

	public static void testConllDocLoading(SystemConfig cfg, String tstFn, String sfix, MentExtractType me) {
		//String modelPath = "policy_predment_scoring_lambdamart_beam1_ontonotes5.txt";
		//String modelPath = "policy_scoring_lambdamart_beam1_ontonotes5.txt";
		String modelPath = "xgb.model";
		//String modelPath = "xgb-stand.model";
		//String modelPath = "xgb-gold.model";
		GreedyPolicy pruneScorePolicy = new GreedyPolicy(modelPath, null, null);
		ConllScriptScorer conllscorer = new ConllScriptScorer(cfg);
		ConllDocumentLoader loader = new ConllDocumentLoader();
		
		

		// load documents

		List<File> testFileNames = OregonStateCorefConllMain.collectAllFileInFolder(tstFn, sfix);

		loader.clear(); // clear all
		loader.loadConllDocumentList(testFileNames); // load document
		List<ConllDocument> testConllDocs = loader.getConllList();
		List<Document> testReconDocs = loader.getReconList();

		// set mention extractor type
		pruneScorePolicy.setMentionExtractorType(me);
		
		// run prediction
		pruneScorePolicy.testAll(testReconDocs);


		// post process and scoring
		
		// removing singletons for CoNLL standard
		for (ConllDocument conllDoc : testConllDocs) {
			conllDoc.removeSingletonClusterSlient();
		}
		
		// official scorer 
		conllscorer.scoreConllDocBatch(testConllDocs, "all");
		// our scorer
		//Scoring.score(printAllDocScores, testNames);

	}
}
