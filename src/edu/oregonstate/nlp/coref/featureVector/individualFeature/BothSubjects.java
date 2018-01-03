package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.properties.GramRole;


/*
 * This feature is: C if both NPs are subjects NA if exactly one is I otherwise
 */

public class BothSubjects
    extends NominalFeature {

public BothSubjects() {
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
  boolean sub1 = GramRole.getValue(np1, doc).equals("SUBJECT");
  boolean sub2 = GramRole.getValue(np2, doc).equals("SUBJECT");

  if (sub1 && sub2) return COMPATIBLE;
  if (sub1 || sub2) return NA;
  return INCOMPATIBLE;
}

}
