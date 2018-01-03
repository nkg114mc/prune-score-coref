package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.general.Constants;


/*
 * This feature is: I if the two NP's have the same maximal NP projection C otherwise
 */

public class MaximalNP
    extends NominalFeature {

public MaximalNP() {
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
  Annotation maxNP1 = edu.oregonstate.nlp.coref.features.properties.MaximalNP.getValue(np1, doc);
  Annotation maxNP2 = edu.oregonstate.nlp.coref.features.properties.MaximalNP.getValue(np2, doc);

  // System.err.println(Utils.getAnnotText(np2, text)+" -> "+Utils.getAnnotText(maxNP2, text));

  if (maxNP1.compareSpan(maxNP2) == 0) {
    if (Constructor.createFeature("Prednom").getValue(np1, np2, doc, featVector).equals(COMPATIBLE)) return INCOMPATIBLE;
    if (Constructor.createFeature(Constants.APPOSITIVE).getValue(np1, np2, doc, featVector).equals(COMPATIBLE))
      return INCOMPATIBLE;
    if (Constructor.createFeature("Quantity").getValue(np1, np2, doc, featVector).equals(COMPATIBLE))
      return INCOMPATIBLE;
    return COMPATIBLE;
  }
  return INCOMPATIBLE;
}

}
