package edu.oregonstate.nlp.coref.featureVector.mentionPropertyFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.berkeley.BerkeleyCorefLangPack;
import edu.oregonstate.nlp.coref.berkeley.BerkeleyFeatureUtils;
import edu.oregonstate.nlp.coref.berkeley.BerkeleyPronounDict;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.MentionPropertyFeature;
import edu.oregonstate.nlp.coref.featureVector.NominalMentionPropertyFeature;
import edu.oregonstate.nlp.coref.features.properties.HeadNoun;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.Constants;

public class MentionType extends NominalMentionPropertyFeature {
	
	@Override
	public String[] getValues() {
		String[] allmenTypes = { BerkeleyFeatureUtils.MentionType.PRONOMINAL.toString(), 
								 BerkeleyFeatureUtils.MentionType.PROPER.toString(),
								 BerkeleyFeatureUtils.MentionType.NOMINAL.toString() };//,
								 //BerkeleyFeatureUtils.MentionType.UNKNOWN.toString()};
		return allmenTypes;
	}

	@Override
	public String produceValue(Annotation mention, Document doc, Map<MentionPropertyFeature, String> featVector) {
		
		BerkeleyPronounDict pd = BerkeleyFeatureUtils.berkeleyPronounDictionary;
		BerkeleyCorefLangPack langPack = BerkeleyFeatureUtils.berkeleyLangPack;
		
		AnnotationSet NE = doc.getAnnotationSet(Constants.NE);
		AnnotationSet POS = doc.getAnnotationSet(Constants.POS);		
		Annotation head = HeadNoun.getValue(mention, doc);
		
		String nerString = "O";
		AnnotationSet overlapNe = NE.getOverlapping(head.getStartOffset(), head.getEndOffset());
		if (overlapNe != null && !overlapNe.isEmpty()) {
			Annotation neAnnot = overlapNe.getLast();
			nerString = neAnnot.getType();
		}

		
		String headWord = doc.getAnnotText(HeadNoun.getValue(mention, doc));
		String headPos = (String) mention.getProperty(Property.HEAD_POS);
		if (headPos == null) {
			AnnotationSet overlapPos = POS.getOverlapping(head);
			headPos = "omitted"; // default, in the worst case
			if (overlapPos != null && !overlapPos.isEmpty()) {
				headPos = overlapPos.getFirst().getType();
			}
		}
		
	    // MENTION TYPE
		String mentionTypeStr = BerkeleyFeatureUtils.MentionType.UNKNOWN.toString(); // default
	    if (pd.isPronLc(headWord.toLowerCase()) || langPack.getPronominalTags.contains(headPos)) {
	    	mentionTypeStr = BerkeleyFeatureUtils.MentionType.PRONOMINAL.toString();
	    } else if (!nerString.equals("O") || langPack.getProperTags.contains(headPos)) {
	    	mentionTypeStr = BerkeleyFeatureUtils.MentionType.PROPER.toString();
	    } else {
	    	mentionTypeStr = BerkeleyFeatureUtils.MentionType.NOMINAL.toString();
	    }

		return mentionTypeStr;
	}

	
}
