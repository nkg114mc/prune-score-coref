package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;


/*
 * This feature is: C if both NPs are subjects of the main clause NA if exactly one is I otherwise
 */

public class BothMCSubjects
    extends NominalFeature {

public BothMCSubjects() {
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
  boolean sub1 = Constructor.createFeature("MCSubject1").getValue(np1, np2, doc, featVector).equals("Y");

  boolean sub2 = Constructor.createFeature("MCSubject2").getValue(np1, np2, doc, featVector).equals("Y");

  if (sub1 && sub2) return COMPATIBLE;
  if (sub1 || sub2) return NA;
  return INCOMPATIBLE;
}

}
