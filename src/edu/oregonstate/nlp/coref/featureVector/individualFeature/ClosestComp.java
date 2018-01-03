package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.properties.ClosestCompliment;


/*
 * 
 * This feature is: I if the one np is the closest preceding NP that is semantically compatible with it C otherwise
 */

public class ClosestComp
    extends NominalFeature {

public ClosestComp() {
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
  return ClosestCompliment.getValue(np2, doc).equals(np1) ? COMPATIBLE : INCOMPATIBLE;
}

}
