package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;


/*
 * This feature is: Y if the second NP in the pair is a pronoun N otherwise
 */

public class Pronoun2
    extends NominalFeature {

public Pronoun2() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return YN;
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  // PRTypeEnum type = FeatureUtils.getPronounType(np2,annotations, text);
  if (FeatureUtils.isPronoun(np2, doc))
    return "Y";
  else
    return "N";
}

}
