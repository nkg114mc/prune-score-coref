package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;


/*
 * This feature is: I if one or both NPs is a title C otherwise
 */

public class Title
    extends NominalFeature {

public Title() {
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
  if (edu.oregonstate.nlp.coref.features.properties.Title.getValue(np1, doc) || edu.oregonstate.nlp.coref.features.properties.Title.getValue(np2, doc))
    return INCOMPATIBLE;
  return COMPATIBLE;
}

}
