package edu.oregonstate.nlp.coref.mentions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.oregonstate.OregonStateConllPreprocess;

public abstract class AbstractMentionExtractor {

	public abstract AnnotationSet extractMentions(Document reconcileDoc, boolean injectGold);
	
	public void extractMentionsBatch(List<Document> reconcileDocList, boolean injectGold) {
		for (Document doc : reconcileDocList) {
			AnnotationSet predictNps = extractMentions(doc, injectGold);
			doc.setPredictMentions(predictNps);
		}
	}
	
	public void extractMentionsBatch(Iterable<Document> reconcileDocList, boolean injectGold) {
		ArrayList<Document> docList = new ArrayList<Document>();
		for (Document doc : reconcileDocList) {
			docList.add(doc);
		}
		extractMentionsBatch(docList, injectGold);
	}
	
	public static void fillPropertyForPredictMentions(Document doc) {
		AnnotationSet nps = doc.getPredictMentionSet();
		OregonStateConllPreprocess.fillProperty(nps, doc);
	}
	
/*
	public static void injectGoldMentions(Document doc) {
		AnnotationSet nps = doc.getPredictMentionSet();
		AnnotationSet gnps = doc.getGoldMentionSet();

		if (gnps == null) {
			System.err.println("Can not inject gold mentions, no gold mention found at all!");
			return;
		}
		
		HashSet<String> offsets = new HashSet<String>();
		for (Annotation ce : nps) {
			String offstr = BerkeleyConllMentionExtractor.getOffsetStr(ce.getStartOffset(), ce.getEndOffset());
			if (!offsets.contains(offstr)) {
				offsets.add(offstr);
			}
		}
		
		// start to inject gold mentions!
		for (Annotation gce : gnps) {
			String offstr = BerkeleyConllMentionExtractor.getOffsetStr(gce.getStartOffset(), gce.getEndOffset());
			if (!offsets.contains(offstr)) {
				offsets.add(offstr);
				// insert gold
				nps.add()
			}
		}
	}
*/
	
	public static double[] mentionDetectionAccuracy(Iterable<Document> docs) {
		double score[] = new double[3];
		
		double pre = 0, rec = 0, f1 = 0;
		double overlap = 0, ngold = 0, npred = 0;
		
		for (Document doc : docs) {
			AnnotationSet nps = doc.getPredictMentionSet();
			AnnotationSet gnps = doc.getGoldMentionSet();

			
			double overThisDoc = 0;
			HashSet<String> offsets = new HashSet<String>();
			for (Annotation gce : gnps) {
				offsets.add(BerkeleyConllMentionExtractor.getOffsetStr(gce.getStartOffset(), gce.getEndOffset()));
			}
			for (Annotation ce : nps) {
				if (offsets.contains(BerkeleyConllMentionExtractor.getOffsetStr(ce.getStartOffset(), ce.getEndOffset()))) {
					overThisDoc++;
				}
			}
			
			overlap += overThisDoc;
			ngold += gnps.size();
			npred += nps.size();
		}
		
		rec = (100.00 * overlap) / ngold;
		pre = (100.00 * overlap) / npred;
		f1 = (2 * pre * rec) / (pre + rec);
		
		System.out.println("////////////////////////////////////////////////////////////////////////");
		System.out.println("///////////////////     Reconcile Scores     ///////////////////////////");
		System.out.println("////////////////////////////////////////////////////////////////////////");
		
		System.out.println(" Reconcile Mention Detection:");
		System.out.println("========================================================================");
		System.out.print("| Recall: " + overlap + " / " + ngold + " = " + rec);
		System.out.print(" Precision: " + overlap + " / " + npred + " = " + pre);
		System.out.println(" F-1: " + f1);
		System.out.println("========================================================================");
		return score;
		
	}
}
