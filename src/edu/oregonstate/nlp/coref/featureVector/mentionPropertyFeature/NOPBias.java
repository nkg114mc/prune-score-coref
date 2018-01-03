package edu.oregonstate.nlp.coref.featureVector.mentionPropertyFeature;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.MentionPropertyFeature;
import edu.oregonstate.nlp.coref.featureVector.NumericMentionPropertyFeature;
import edu.oregonstate.nlp.coref.featureVector.individualFeature.PairClassifierScore;
import edu.oregonstate.nlp.coref.general.Constants;

/**
 * Return the length of mention in terms of words
 * 
 * @author machao
 */
public class NOPBias extends NumericMentionPropertyFeature {
	
	@Override
	public String produceValue(Annotation mention, Document doc, Map<MentionPropertyFeature, String> featVector) {
		return "1";
	}

}
