package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.properties.Demonstrative;


/*
 * This feature is: Y if the second NP starts with a demonstrative -- "this", "these", "that", "those" N otherwise
 */

public class Demonstrative2
    extends NominalFeature {

public Demonstrative2() {
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
  if (Demonstrative.getValue(np2, doc))
    return "Y";
  else
    return "N";
}

}
