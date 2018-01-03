package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.properties.Definite;


/*
 * This feature is: C if both NPs start with "the" NA if exactly one starts with "the" I otherwise
 */

public class BothDefinites
    extends NominalFeature {

public BothDefinites() {
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
  boolean def1 = Definite.getValue(np1, doc) && !edu.oregonstate.nlp.coref.features.properties.ProperName.getValue(np1, doc);
  boolean def2 = Definite.getValue(np2, doc) && !edu.oregonstate.nlp.coref.features.properties.ProperName.getValue(np2, doc);
  if (def1 && def2) return COMPATIBLE;
  if (def1 || def2) return NA;
  return INCOMPATIBLE;
}

}
