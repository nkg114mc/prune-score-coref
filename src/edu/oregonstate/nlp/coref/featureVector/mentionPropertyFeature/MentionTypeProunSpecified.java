package edu.oregonstate.nlp.coref.featureVector.mentionPropertyFeature;

import java.util.ArrayList;
import java.util.HashSet;
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

// This is almost the same as MentionType.
// The only difference is that, when the given mention is a pronoun,
// This feature return the cannonical represented proun name (i, you, they etc.) 
// instead of just a "PRONOUN"

public class MentionTypeProunSpecified extends NominalMentionPropertyFeature {
	
	@Override
	public String[] getValues() {
		
		ArrayList<String> allPossibleVals = new ArrayList<String>();
		
		// non pronouns
		allPossibleVals.add(BerkeleyFeatureUtils.MentionType.NOMINAL.toString());
		allPossibleVals.add(BerkeleyFeatureUtils.MentionType.PROPER.toString());
		allPossibleVals.add(BerkeleyFeatureUtils.MentionType.PRONOMINAL.toString());

		BerkeleyPronounDict pd = BerkeleyFeatureUtils.berkeleyPronounDictionary;
		for (String v : pd.pronounsToCanonicalPronouns.values()) {
			allPossibleVals.add(v);
		}
		
		return allPossibleVals.toArray(new String[0]);
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
	    	// this is a PRONOMINAL
	    	mentionTypeStr = BerkeleyFeatureUtils.MentionType.PRONOMINAL.toString();
	    	String cannonicalPrn = pd.getCanonicalPronLc(headWord.toLowerCase());
	    	if (cannonicalPrn != null) {
	    		mentionTypeStr = cannonicalPrn;// BerkeleyFeatureUtils.MentionType.PRONOMINAL.toString();
	    	}
	    } else if (!nerString.equals("O") || langPack.getProperTags.contains(headPos)) {
	    	mentionTypeStr = BerkeleyFeatureUtils.MentionType.PROPER.toString();
	    } else {
	    	mentionTypeStr = BerkeleyFeatureUtils.MentionType.NOMINAL.toString();
	    }

		return mentionTypeStr;
	}

	
}
