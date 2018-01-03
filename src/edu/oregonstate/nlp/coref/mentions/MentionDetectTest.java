package edu.oregonstate.nlp.coref.mentions;

import java.io.File;
import java.util.List;

import edu.oregonstate.nlp.coref.OregonStateCorefConllMain;
import edu.oregonstate.nlp.coref.conll.ConllDocument;
import edu.oregonstate.nlp.coref.conll.ConllDocumentLoader;
import edu.oregonstate.nlp.coref.data.Document;

public class MentionDetectTest {

	public static void main(String[] args) {
		predictMentionsAndEval("/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/test", "v9_auto_conll");
		//predictMentionsAndEval("/home/mc/workplace/rand_search/coref/berkfiles/data/ontonotes5/train", "v4_auto_conll");
	}
	
	public static void predictMentionsAndEval(String folder, String suffix) {
		
		ConllDocumentLoader loader = new ConllDocumentLoader();
		
		List<File> fileNames = OregonStateCorefConllMain.collectAllFileInFolder(folder, suffix);//"v4_gold_conll");//"v9_auto_conll");

		// load training documents
		loader.clear(); // clear all
		loader.loadConllDocumentList(fileNames); // load document
		List<ConllDocument> trainConllDocs = loader.getConllList();
		List<Document> reconDocs = loader.getReconList();
	
		// extract mentions
		//AbstractMentionExtractor mentExtractor = new GoldConllMentionExtractor();
		//AbstractMentionExtractor mentExtractor = new BerkeleyConllMentionExtractor();
		//AbstractMentionExtractor mentExtractor = new FileLoadedMentionExtractor("/home/mc/workplace/rand_search/random_search_proj/mentDumpFull.txt");
		//AbstractMentionExtractor mentExtractor = new FileLoadedMentionExtractor("/home/mc/workplace/coref2017/HOT-coref/ims-hotcoref-2014-06-06/hotmentions-all.txt");
		//AbstractMentionExtractor mentExtractor = new FileLoadedMentionExtractor("/home/mc/workplace/rand_search/random_search_proj/mentDumpHot0.1.txt");
		AbstractMentionExtractor mentExtractor = new FileLoadedMentionExtractor("/home/mc/workplace/rand_search/random_search_proj/mentDumpHot0.2.txt");
		mentExtractor.extractMentionsBatch(reconDocs, false);
	
		// have a evaluation
		AbstractMentionExtractor.mentionDetectionAccuracy(reconDocs);
		
		//for (Document d : reconDocs) {
		//	System.out.println(d.getDocumentId());
		//}

	}

}



