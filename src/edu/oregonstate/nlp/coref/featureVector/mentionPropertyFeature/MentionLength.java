package edu.oregonstate.nlp.coref.featureVector.mentionPropertyFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.MentionPropertyFeature;
import edu.oregonstate.nlp.coref.featureVector.NumericMentionPropertyFeature;

/**
 * Return the length of mention in terms of words
 * 
 * @author machao
 */
public class MentionLength extends NumericMentionPropertyFeature {

	@Override
	public String produceValue(Annotation mention, Document doc, Map<MentionPropertyFeature, String> featVector) {
		String[] words = doc.getWords(mention.getStartOffset(), mention.getEndOffset());
		int lengthInWords = words.length;
		return String.valueOf(lengthInWords);
	}

}
