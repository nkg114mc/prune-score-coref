package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;


/*
 * This feature is: C if both NPs are non-pronominal and after discarding determiners the strings of the two NPs match I
 * otherwise
 */

public class SoonStrNonPro
    extends NominalFeature {

public SoonStrNonPro() {
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
  String[] words1 = doc.getWords(np1);
  String[] words2 = doc.getWords(np2);
  if (FeatureUtils.isPronoun(words1) || FeatureUtils.isPronoun(words2)) return INCOMPATIBLE;
  return Constructor.createFeature("SoonStr").getValue(np1, np2, doc, featVector);
}
}
