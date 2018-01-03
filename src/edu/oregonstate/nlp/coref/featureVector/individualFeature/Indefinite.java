package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.general.Constants;


/*
 * This feature is: I if the second NP is an indefinite and is not an appositive C otherwise
 */

public class Indefinite
    extends NominalFeature {

public Indefinite() {
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
    return INCOMPATIBLE;
  if (FeatureUtils.isIndefinite(np2, doc)) return COMPATIBLE;
  return INCOMPATIBLE;
}

}
