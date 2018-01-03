package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NumericFeature;


/*
 * This feature is: the distance between the two NPs in terms of number of paragraphs
 */

public class ParNum
    extends NumericFeature {

private static final int MAX_PAR_DISTANCE = 5;

public ParNum() {
  name = this.getClass().getSimpleName();
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  int par1 = edu.oregonstate.nlp.coref.features.properties.ParNum.getValue(np1, doc);
  int par2 = edu.oregonstate.nlp.coref.features.properties.ParNum.getValue(np2, doc);
  int distance = Math.abs(par1 - par2);
  distance = distance > MAX_PAR_DISTANCE ? MAX_PAR_DISTANCE : distance;
  return Integer.toString(distance);
}

}
