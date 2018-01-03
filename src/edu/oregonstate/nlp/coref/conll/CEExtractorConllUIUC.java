package edu.oregonstate.nlp.coref.conll;

import java.util.List;
import java.util.Map;
import java.util.Set;

//import edu.illinois.cs.cogcomp.lbj.coref.ir.Mention;
//import edu.illinois.cs.cogcomp.lbj.coref.ir.docs.Doc;
import edu.oregonstate.nlp.coref.SystemConfig;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureExtractor.CEExtractor;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.uiuc.DocCoNLL;

public class CEExtractorConllUIUC  extends CEExtractor {
	
	public void init(SystemConfig cfg) {
	}
	
	@Override
	public boolean isNoun(Annotation a, String text) {
		return false;
	}

	@Override
	public boolean isNP(Annotation an, String text) {
		return false;
	}

	@Override
	public boolean addNE(Annotation a, AnnotationSet includedCEs,
			AnnotationSet baseCEs, Document doc) {
		return false;
	}

	// run the mentions from the uiuc doc to reconcile mentions
	public void run(Document ReconcileDoc, String[] annSetNames, DocCoNLL uiucDoc, boolean overwrite) {
		if (uiucDoc != null) {
			AnnotationSet uiucMentions = translate(ReconcileDoc, Constants.UIUC_NP, uiucDoc);
			ReconcileDoc.addAnnotationSet(uiucMentions);
		}
	}
	
	
	/**
	 * Translate the stanford extracted mentions into reconcile annotations.
	 * */
	public AnnotationSet translate(Document ReconcileDoc, String annSetNames, DocCoNLL uiucDoc) {

		AnnotationSet uiucMentions = new AnnotationSet(Constants.UIUC_NP);

		AnnotationSet reconcileTokens = ReconcileDoc.getAnnotationSet(Constants.TOKEN);
		
/*
		// go through all tokens
		List<String> words = uiucDoc.getWords() ;
		for (int i = 0; i < words.size(); i++) {
			
			String t1 = words.get(i);
			String t2 = ReconcileDoc.getAnnotString(reconcileTokens.get(i + 1));
			//System.out.println(i + " " + t1 + " " + t2);
			
		}
		
		
		// go through all mentions
		int uiucMentionID = 0;
		List<Mention> uiucPredictMen = uiucDoc.getPredMentions();
		for (Mention men : uiucPredictMen) {
			
			uiucMentionID++;

			// construct the mention
			Annotation reconTkStart = reconcileTokens.get(men.getExtentFirstWordNum() + 1);
			Annotation reconTkEnd = reconcileTokens.get(men.getExtentLastWordNum() + 1);
			Annotation newMention = new Annotation(uiucMentionID, reconTkStart.getStartOffset(), reconTkEnd.getEndOffset(), Constants.UIUC_NP);
			
			//String ext = "";
			//for (int j = men.getExtentFirstWordNum(); j <= men.getExtentLastWordNum(); j++) {
			//	ext += ( words.get(j) + " ");
			//}
			
			//System.out.println(uiucMentionID + " " + men.getCleanText() + " " + ReconcileDoc.getAnnotString(newMention));
			
			// property
			//newMention.setAttribute(Constants.CONLL_WORDSTR, men.getCleanText());
			newMention.setAttribute(Constants.CE_ID, String.valueOf(uiucMentionID));
			
			newMention.setAttribute(Constants.HEAD_STR, men.getHead().getText());
			newMention.setAttribute(Constants.HEAD_START, String.valueOf(reconcileTokens.get(men.getHeadFirstWordNum() + 1).getStartOffset()));			
			newMention.setAttribute(Constants.HEAD_END, String.valueOf(reconcileTokens.get(men.getHeadLastWordNum() + 1).getEndOffset()));

			uiucMentions.add(newMention);
		}
*/
		return uiucMentions;
	}

	
	
}
