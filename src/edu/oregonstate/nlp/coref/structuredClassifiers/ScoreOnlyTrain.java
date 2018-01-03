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

import org.apache.commons.configuration.ConfigurationException;

import ciir.umass.edu.eval.Evaluator;
import edu.oregonstate.nlp.coref.SystemConfig;
import edu.oregonstate.nlp.coref.conll.ConllDocument;
import edu.oregonstate.nlp.coref.conll.ConllDocumentLoader;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.mentions.MentionFilter;
import edu.oregonstate.nlp.coref.structuredClassifiers.sapredictor.StateActionRanklibPredictor;
import edu.oregonstate.nlp.coref.structuredClassifiers.sapredictor.StateActionXgboostPredictor;



/**
 * @author Chao Ma
 * 
 */


public class ScoreOnlyTrain {


	// interface designed to process OntoNotes (for CoNLL2011 and CoNLL2012) datasets
	public static void main(String[] args)
	{
		runScoreOnlyTraining(args);
	}
	
	public static void runScoreOnlyTraining(String[] args) {

		// load configuration
		String configFilename = "config/empty.cfg";
		
		String defaultTrainFolder = "/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/train";
		String defaultDevFolder = "/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/dev";
		String defaultTestFolder = "/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/test";

		String trnFn = defaultTrainFolder;
		String devFn = defaultTestFolder;
		
		try {
			
			SystemConfig globalCfg = new SystemConfig(configFilename);
		
			ConllDocumentLoader trainLoader = new ConllDocumentLoader();
			ConllDocumentLoader devLoader = new ConllDocumentLoader();

			// load documents

			List<File> trainFileNames = collectAllFileInFolder(trnFn, "v4_auto_conll");
			
			List<File> devFileNames = collectAllFileInFolder(devFn, "v9_auto_conll");

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

			
			
			
			String modelSavePath = "policy_scoring"; 
			String featFileName = "ontonotes5_feat_predment"; 
			String featureDumpFolder = "featDump";
			
			// Training
			GreedyPolicy pruneScorePolicy = new GreedyPolicy(modelSavePath, featFileName, featureDumpFolder);
			

			final boolean IS_TRAIN = true;
			
			// initializing trainer
			
			GreedyPolicy.checkOrCreateFolder(featureDumpFolder);
			
			pruneScorePolicy.qidClearZero();


			// dump training features
			String trnFeatFn = featureDumpFolder + "/" + featFileName + "_train.txt";
			PrintWriter trnFeatDumper = new PrintWriter(trnFeatFn);
			pruneScorePolicy.runPolicy(trainReconDocs, IS_TRAIN, trnFeatDumper, null, false, null);
			trnFeatDumper.close();

			// dump develop features
			String devFeatFn = featureDumpFolder + "/" + featFileName + "_dev.txt";
			PrintWriter devFeatDumper = new PrintWriter(devFeatFn);
			pruneScorePolicy.runPolicy(devReconDocs, IS_TRAIN, devFeatDumper, null, false, null);
			devFeatDumper.close();


			// =====================================
			// save some memory
			trainLoader.clear();
			devLoader.clear();

			//run batch training of scorer of Prune-and-Score
			//StateActionRanklibPredictor screr = new StateActionRanklibPredictor("P@1");
			StateActionXgboostPredictor screr = new StateActionXgboostPredictor();

			screr.trainRanker(modelSavePath, trnFeatFn, devFeatFn, 1);

			
			System.out.println("Done training.");
			
			
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
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
