package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.properties.ProperName;


/*
 * This feature is: C if both NPs are proper names NA if exactly one NP is a proper name I otherwise
 */

public class BothProperNames
    extends NominalFeature {

public BothProperNames() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return ICN;
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  boolean pn1 = ProperName.getValue(np1, doc);
  boolean pn2 = ProperName.getValue(np2, doc);

  if (pn1 && pn2) return COMPATIBLE;
  if (pn1 || pn2) return NA;
  return INCOMPATIBLE;
}

}
