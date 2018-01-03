package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;


/*
 * This feature is: C if both NPs are pronouns and the strings match I otherwise
 */

public class ProStr
    extends NominalFeature {

public ProStr() {
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

  if (!FeatureUtils.isPronoun(np1, doc) || !FeatureUtils.isPronoun(np2, doc)) return INCOMPATIBLE;

  return doc.getAnnotText(np1).equalsIgnoreCase(doc.getAnnotText(np2)) ? COMPATIBLE : INCOMPATIBLE;
}

}
