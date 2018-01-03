package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.util.List;

import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.mentions.MentionPrecdiction;


/**
 * @author ves
 * 
 */

public class MentionCount extends StructuredClassifier {

	private int totalMentionNumber = 0;
	private MentionPrecdiction menDetect = new MentionPrecdiction();
	//private MentionFilter menFilter = new MentionFilter(Utils.getConfig());
	
	public void testAll(Iterable<Document> docs, String modelInputFile, String[] options){
		//menFilter.filterPrintAllDocs(docs);
	}
	
	public AnnotationSet test(Document doc, String modelInputFile, String[] options){
		AnnotationSet ces = doc.getAnnotationSet(Constants.NP);
		
		// have a look at the mentions
		mentionShow(doc);

		return ces;
	}

	public void mentionShow(Document doc) {
		
		menDetect.printCorefMentions(doc, "predictMentoins.txt", false);
		
	}

	public void resolve(Document doc, AnnotationSet ces, double[] w, double[] aveW, boolean train, double iterNum)
	{

	}

	public void mentionCount(Document doc) {
		AnnotationSet ces = doc.getAnnotationSet(Constants.NP);

		int numberMentionOnThisDoc = ces.size();
		totalMentionNumber += numberMentionOnThisDoc;

		System.out.println("single doc mention number: " + numberMentionOnThisDoc);
		System.out.println("Total mention number: " + totalMentionNumber);
	}
	
	public static void showDocumentInfo(Document doc) {
		int numPredictMen = 0;
		int numGoldMen = 0;
		String docName = "";
		String spliter = " ";
		
		// extract information...
		docName = doc.getAbsolutePath();
		AnnotationSet gsNp = doc.getAnnotationSet(Constants.GS_NP);
		numGoldMen = gsNp.size();
		AnnotationSet predNp = doc.getAnnotationSet(Constants.NP);
		numPredictMen = predNp.size();

		System.out.println(docName + spliter + numGoldMen + spliter + numPredictMen);
	}

	@Override
	public void train(List<Document> traindocs, List<Document> validdocs) {
		
	}

	@Override
	public AnnotationSet test(Document doc) {
		return null;
	}

	@Override
	public void testAll(Iterable<Document> docs) {
		
	}

}
