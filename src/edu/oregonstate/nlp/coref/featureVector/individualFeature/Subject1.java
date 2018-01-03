package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.properties.GramRole;


/*
 * This feature is: Y if the first NP is a subject N otherwise
 * 
 * Not the most efficient implementation;
 */

public class Subject1
    extends NominalFeature {

public Subject1() {
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
  boolean sub = GramRole.getValue(np1, doc).equals("SUBJECT");
  return sub ? "Y" : "N";
}

}
