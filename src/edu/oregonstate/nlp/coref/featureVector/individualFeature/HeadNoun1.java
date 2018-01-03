package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.StringFeature;
import edu.oregonstate.nlp.coref.features.properties.HeadNoun;


/*
 * The head noun of the first np
 */

public class HeadNoun1
    extends StringFeature {

public HeadNoun1() {
  name = this.getClass().getSimpleName();
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  Annotation head = HeadNoun.getValue(np1, doc);
  return doc.getAnnotText(head);
}

}
