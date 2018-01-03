package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.Stopword;


/*
 * This feature is: C if both NPs are non-pronominal and the strings match I otherwise
 * 
 * This feature agrees with the description, but differs slightly from Vincent's implementation.
 */

public class WordsStr
    extends NominalFeature {

public WordsStr() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return IC;
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  // if(AllFeatures.makeFeature("WordNetClass").getValue(np1, np2, annotations, text, featVector).equals(INCOMPATIBLE))
  // return INCOMPATIBLE;
  if (FeatureUtils.isPronoun(np1, doc) || FeatureUtils.isPronoun(np2, doc)) return INCOMPATIBLE;
  if (Stopword.getValue(np1, doc) || Stopword.getValue(np2, doc)) return INCOMPATIBLE;
  // if(FeatureUtils.subsumesNumber(str1) || FeatureUtils.subsumesNumber(str2))
  // return INCOMPATIBLE;
  String str1 = doc.getAnnotText(np1);
  String str2 = doc.getAnnotText(np2);
  return str1.equalsIgnoreCase(str2) ? COMPATIBLE : INCOMPATIBLE;
}

}
