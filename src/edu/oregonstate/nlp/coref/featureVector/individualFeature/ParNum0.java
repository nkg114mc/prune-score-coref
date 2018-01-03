package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.properties.ParNum;
import edu.oregonstate.nlp.coref.features.properties.SentNum;


/*
 * This feature is: C if the two CEs are in the same sentence and 0 otherwise
 * 
 */

public class ParNum0
    extends NominalFeature {

public ParNum0() {
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
  return ParNum.getValue(np1, doc)-ParNum.getValue(np2, doc)==0?COMPATIBLE:INCOMPATIBLE;
}

}
