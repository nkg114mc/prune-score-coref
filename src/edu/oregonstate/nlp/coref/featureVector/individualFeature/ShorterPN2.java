package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.properties.InfWords;
import edu.oregonstate.nlp.coref.features.properties.ProperName;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.Utils;


/*
 * This feature is: C if both NPs are proper names and one NP is a proper substring with respect of content words of the
 * other I otherwise
 * 
 * Slightly differs from Vincent's implementation
 */

public class ShorterPN2
    extends NominalFeature {

public ShorterPN2() {
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

  if (!ProperName.getValue(np1, doc) || !ProperName.getValue(np2, doc)) return NA;
  Annotation ne1 = (Annotation) np1.getProperty(Property.LINKED_PROPER_NAME);
  Annotation ne2 = (Annotation) np2.getProperty(Property.LINKED_PROPER_NAME);
  String[] infW1 = InfWords.getValue(ne1, doc);
  String[] infW2 = InfWords.getValue(ne2, doc);
  if (infW1 == null || infW2 == null || infW1.length < 1 || infW2.length < 1) return INCOMPATIBLE;
  if (infW1.length > infW2.length)
    return COMPATIBLE;
  else
    return INCOMPATIBLE;
}

}
