package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.GramRole;


/*
 * This feature is: The grammmatical role of the first NP.
 */

public class GramRole1
    extends NominalFeature {

public GramRole1() {
  name = this.getClass().getSimpleName();
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  return GramRole.getValue(np1, doc);
}

@Override
public String[] getValues()
{
  return FeatureUtils.KNOWN_GRAM_RELATIONS;
}

}
