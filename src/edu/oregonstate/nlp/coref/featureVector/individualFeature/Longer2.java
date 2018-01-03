package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.InfWords;
import edu.oregonstate.nlp.coref.features.properties.ProperName;


/*
 * This feature is: C if the intersection of the content words of the two nps is not empty I otherwise
 */

public class Longer2
    extends NominalFeature {

public Longer2() {
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
  // String[] infWords1 = FeatureUtils.removeUninfWords(FeatureUtils.getUniqueWords( np1, annotations, text));
  // String[] infWords2 = FeatureUtils.removeUninfWords(FeatureUtils.getUniqueWords(np2, annotations, text));
  // String[] infWords1 = FeatureUtils.getWords(np1, text);
  // String[] infWords2 = FeatureUtils.getWords(np2, text);
  if (FeatureUtils.isPronoun(np1, doc) || FeatureUtils.isPronoun(np2, doc)) return INCOMPATIBLE;

  String[] infWords1 = InfWords.getValue(np1, doc);
  String[] infWords2 = InfWords.getValue(np2, doc);
  if (infWords1.length > 0 && infWords2.length > 0 && infWords2.length>infWords1.length) return COMPATIBLE;
  return INCOMPATIBLE;
}

}
