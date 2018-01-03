package edu.oregonstate.nlp.coref.featureVector.mentionPropertyFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.MentionPropertyFeature;
import edu.oregonstate.nlp.coref.featureVector.NominalMentionPropertyFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.HeadNoun;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.SyntaxUtils;

public class GramarRoll  extends NominalMentionPropertyFeature {

	@Override
	public String[] getValues() {
		return FeatureUtils.KNOWN_GRAM_RELATIONS;
	}

	@Override
	public String produceValue(Annotation mention, Document doc,
			Map<MentionPropertyFeature, String> featVector) {

		// Copied from reconcile GramRole feature
		String value;
		Annotation hn = HeadNoun.getValue(mention, doc);
		AnnotationSet dep = doc.getAnnotationSet(Constants.DEP);
		Annotation d = SyntaxUtils.getDepNode(hn, dep);
		value = "NONE";
		if (d != null) {
			String type = d.getType();
			if (type.equalsIgnoreCase("conj")) {
				Annotation a = d;
				while (type.equalsIgnoreCase("conj")) {
					String[] span = (a.getAttribute("GOV")).split("\\,");
					int stSpan = Integer.parseInt(span[0]);
					int endSpan = Integer.parseInt(span[1]);
					a = dep.getContained(stSpan, endSpan).getFirst();
					if (a == null) {
						break;
					}
					type = a.getType();
				}
			}
			if (FeatureUtils.memberArray(type, FeatureUtils.KNOWN_GRAM_RELATIONS)) {
				value = type;
			}
			
			//System.out.println("type = " + type + " value = " + value);
		}
		


		return value;
	}

}
