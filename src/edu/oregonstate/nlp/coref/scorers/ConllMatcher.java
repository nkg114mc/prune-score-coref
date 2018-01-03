/*
 * This class contains different methods for matching automatically extracted CE's to gold-standard CEs
 */
package edu.oregonstate.nlp.coref.scorers;

import java.util.HashMap;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.Constants;

public class ConllMatcher {


	public static int numMatchedKey = 0;
	public static int totalKey = 0;
	public static int totalNPsMatched = 0;
	public static int doubleMatches = 0;
	
	public static void nullifyCounters()
	{
		numMatchedKey = 0;
		totalKey = 0;
		totalNPsMatched = 0;
		doubleMatches = 0;
	}

	public static void exactMatchAnnotationSets(AnnotationSet gsNps, AnnotationSet nps)
	{
		for (Annotation a : gsNps) {
			a.setProperty(Property.MATCHED_CE, Integer.parseInt(a.getAttribute(Constants.CE_ID)));
		}
		for (Annotation a : nps) {
			a.setProperty(Property.MATCHED_CE, Integer.parseInt(a.getAttribute(Constants.CE_ID)));
		}
	}


	public static void matchAnnotationSets(AnnotationSet gsNps, AnnotationSet nps, Document doc)
	{
		int numMatched = 0;

		HashMap<Annotation, Annotation> matched = new HashMap<Annotation, Annotation>();

		for (Annotation a : nps.getOrderedAnnots()) {

			Annotation match = newMatchAnnotationOntoStyle(a, gsNps, doc);

			if (match != null) {
				numMatched++;
				if (matched.containsKey(match)) {
					doubleMatches++;
					Annotation oldMatch = matched.get(match);

					boolean conjOldMatch = FeatureUtils.memberArray("and", doc.getWords(oldMatch));
					boolean conjNewMatch = FeatureUtils.memberArray("and", doc.getWords(a));
					if ((oldMatch.getLength() >= a.getLength() && (!conjOldMatch || conjNewMatch)) || (conjNewMatch && !conjOldMatch)) {
						a.setProperty(Property.MATCHED_CE, -1);
					} else {
						oldMatch.setProperty(Property.MATCHED_CE, -1);
						match.setProperty(Property.MATCHED_CE, Integer.parseInt(a.getAttribute(Constants.CE_ID)));
						a.setProperty(Property.MATCHED_CE, Integer.parseInt(match.getAttribute(Constants.CE_ID)));
						matched.put(match, a);
					}
				} else {
					matched.put(match, a);
					match.setProperty(Property.MATCHED_CE, Integer.parseInt(a.getAttribute(Constants.CE_ID)));
					a.setProperty(Property.MATCHED_CE, Integer.parseInt(match.getAttribute(Constants.CE_ID)));
				}
			} else {
				a.setProperty(Property.MATCHED_CE, -1);
			}
		}



		numMatchedKey += matched.size();
		int gsNpsSize = gsNps == null ? 0 : gsNps.size();

		totalKey += gsNpsSize;
		totalNPsMatched += numMatched;

		if (Constants.DEBUG && gsNps != null) {
			for (Annotation a : gsNps) {
				if (!matched.containsKey(a)) {
					System.err.println("Not matched key: " + doc.getAnnotText(a) + " -- " + a.getAttribute("ID"));
				}
			}
		}
	}


	public static Annotation newMatchAnnotationOntoStyle(Annotation a, AnnotationSet gsNps, Document doc)
	{
		Annotation match = null;
		AnnotationSet overlapCoref = gsNps.getOverlapping(a);

		if (overlapCoref != null) {
			for (Annotation cur : overlapCoref) {
				if (cur.compareSpan(a) == 0) {
					match = cur;
					break;
				}

			}
		}
		return match;
	}



}
