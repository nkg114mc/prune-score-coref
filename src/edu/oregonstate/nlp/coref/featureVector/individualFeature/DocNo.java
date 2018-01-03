package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NumericFeature;


/*
 * This feature is: the number of the document from which the nps came.
 */

public class DocNo
    extends NumericFeature {

public static final String ID = "docNo";

public DocNo() {
  name = this.getClass().getSimpleName();
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  AnnotationSet docNo = doc.getAnnotationSet(ID);
  Annotation num = docNo.getFirst();
  return num.getAttribute(ID);
}

}
