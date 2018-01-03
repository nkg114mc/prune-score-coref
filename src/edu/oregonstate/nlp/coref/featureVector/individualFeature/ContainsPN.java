package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.InfWords;
import edu.oregonstate.nlp.coref.general.Constants;


/*
 * This feature is: I if both NPs contain proper names and contain no words in common C otherwise
 */

public class ContainsPN
    extends NominalFeature {

public ContainsPN() {
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
  if (Constructor.createFeature(Constants.APPOSITIVE).getValue(np1, np2, doc, featVector).equals(COMPATIBLE))
    return COMPATIBLE;
  if (Constructor.createFeature(Constants.ALIAS).getValue(np1, np2, doc, featVector).equals(COMPATIBLE))
    return COMPATIBLE;
  if (Constructor.createFeature(Constants.PREDNOM).getValue(np1, np2, doc, featVector).equals(COMPATIBLE))
    return COMPATIBLE;

  Annotation pn1 = FeatureUtils.getContainedProperName(np1, doc);
  Annotation pn2 = FeatureUtils.getContainedProperName(np2, doc);
  if (pn1 != null && pn2 != null) {
    String[] words1 = InfWords.getValue(pn1, doc);
    String[] words2 = InfWords.getValue(pn2, doc);
    if (!FeatureUtils.overlaps(words1, words2)) return INCOMPATIBLE;
  }
  return COMPATIBLE;
}

}
