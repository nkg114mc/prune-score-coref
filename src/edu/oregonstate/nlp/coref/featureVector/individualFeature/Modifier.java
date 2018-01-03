package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.general.Utils;


/*
 * This feature is: C if the prenominal modifiers of one np are a subset of the prenominal modifiers of the other I
 * otherwise
 */

public class Modifier
    extends NominalFeature {

public Modifier() {
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
  String[] mod1 = edu.oregonstate.nlp.coref.features.properties.Modifier.getValue(np1, doc);
  if (mod1 == null || mod1.length == 0) return INCOMPATIBLE;
  String[] mod2 = edu.oregonstate.nlp.coref.features.properties.Modifier.getValue(np2, doc);
  if (mod2 == null || mod2.length == 0) return INCOMPATIBLE;
  if (Utils.isAnySubset(mod1, mod2)) return INCOMPATIBLE;
  return COMPATIBLE;
}

}
