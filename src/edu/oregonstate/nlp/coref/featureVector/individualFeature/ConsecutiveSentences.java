package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;


/*
 * This feature is: C if the NPs are in consecutive sentences I otherwise
 */

public class ConsecutiveSentences
    extends NominalFeature {

public ConsecutiveSentences() {
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
  if (Constructor.createFeature("SentNum").getValue(np1, np2, doc, featVector).equals("1"))
    return COMPATIBLE;
  else
    return INCOMPATIBLE;
}

}
