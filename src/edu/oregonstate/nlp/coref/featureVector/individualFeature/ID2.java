package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NumericFeature;
import edu.oregonstate.nlp.coref.general.Constants;


/*
 * This feature is: the number of the document from which the nps came.
 */

public class ID2
    extends NumericFeature {

public ID2() {
  name = this.getClass().getSimpleName();
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  return np2.getAttribute(Constants.CE_ID);
}

}
