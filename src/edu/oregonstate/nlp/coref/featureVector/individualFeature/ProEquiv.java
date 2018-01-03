package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;


/*
 * This feature is: I if both NPs are pronouns, agree in GENDER and NUMBER and appear in consecutive sentences C
 * otherwise
 */

public class ProEquiv
    extends NominalFeature {

public ProEquiv() {
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
  if (!featVector.get(Constructor.createFeature("BothPronouns")).equals(COMPATIBLE)) return INCOMPATIBLE;
  if (!featVector.get(Constructor.createFeature("Gender")).equals(COMPATIBLE)
      && !featVector.get(Constructor.createFeature("Gender")).equals(SAME)) return INCOMPATIBLE;
  if (!featVector.get(Constructor.createFeature("Number")).equals(COMPATIBLE)) return INCOMPATIBLE;
  if (!Constructor.createFeature("SentNum").getValue(np1, np2, doc, featVector).equals("1")) return INCOMPATIBLE;

  return COMPATIBLE;
}

}
