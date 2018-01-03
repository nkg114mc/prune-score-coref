package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;


/*
 * The class of the instance: + if the NPs are coreferent - otherwise
 */
public class unknwnClass
    extends NominalFeature {

private static String[] values = { "+", "-" };

public unknwnClass() {
  name = "class";
}

@Override
public String[] getValues()
{
  return values;
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  return "-";
}

}
