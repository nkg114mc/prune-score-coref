package edu.oregonstate.nlp.coref.featureVector.clusterFeature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.ClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.FeatureUtils.GenderEnum;
import edu.oregonstate.nlp.coref.features.properties.Gender;
import edu.oregonstate.nlp.coref.general.Utils;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C the two np's agree in gender I if they disagree NA if the gender information for either cannot be
 * determined
 */

public class PostModifier
    extends NominalClusterFeature {

public PostModifier() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return IC;
}

@Override
public String produceValue(CorefChain c1, CorefChain c2, Document doc, Map<ClusterFeature, String> featVector)
{
  HashSet<String> w1 = (HashSet<String>) c1.getProperty(CorefChain.POST_MODIFIERS);
  HashSet<String> w2 = (HashSet<String>) c2.getProperty(CorefChain.POST_MODIFIERS);
  if(w1==null||w2==null)
    return INCOMPATIBLE;
  if(c1.before(c2))
    return Utils.isSubset(w2.toArray(new String[0]), w1.toArray(new String[0]))?INCOMPATIBLE:COMPATIBLE;
  else
    return Utils.isSubset(w1.toArray(new String[0]), w2.toArray(new String[0]))?INCOMPATIBLE:COMPATIBLE;
}

}
