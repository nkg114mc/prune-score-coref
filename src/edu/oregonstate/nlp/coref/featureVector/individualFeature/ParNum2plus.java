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

public class ParNum2plus
    extends NominalFeature {

public ParNum2plus() {
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
  return Math.abs(ParNum.getValue(np1, doc)-ParNum.getValue(np2, doc))>=2?COMPATIBLE:INCOMPATIBLE;
}

}
