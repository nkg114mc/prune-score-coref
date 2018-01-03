package edu.oregonstate.nlp.coref.mentions;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;

public class GoldConllMentionExtractor extends AbstractMentionExtractor {

	@Override
	public AnnotationSet extractMentions(Document reconcileDoc, boolean injectGold) {
		
		AnnotationSet goldBoundMentions = new AnnotationSet(Constants.NP);
		
		AnnotationSet goldMentions = reconcileDoc.getGoldMentionSet();
		for (Annotation gnp : goldMentions) {
			Annotation np = new Annotation(gnp.getId(), gnp.getStartOffset(), gnp.getEndOffset(), "GOLD_MENT_BOUNDARY");
			np.setAttribute(Constants.CLUSTER_ID, String.valueOf(np.getId()));
			np.setAttribute(Constants.CE_ID, String.valueOf(np.getId()));
			goldBoundMentions.add(np);
		}
		
		return goldBoundMentions;
	}

	
	
}