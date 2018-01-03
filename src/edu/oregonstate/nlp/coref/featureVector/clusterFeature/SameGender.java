package edu.oregonstate.nlp.coref.featureVector.clusterFeature;

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
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C the two np's agree in gender I if they disagree NA if the gender information for either cannot be
 * determined
 */

public class SameGender
    extends NominalClusterFeature {

public SameGender() {
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
  FeatureUtils.GenderEnum gen1 = (GenderEnum)c1.getProperty(Gender.getInstance());
  FeatureUtils.GenderEnum gen2 = (GenderEnum)c2.getProperty(Gender.getInstance()); 
  if (gen1.equals(FeatureUtils.GenderEnum.UNKNOWN) || gen1.equals(FeatureUtils.GenderEnum.AMBIGUOUS) || gen1.equals(GenderEnum.EITHER)|| gen1.equals(GenderEnum.NEUTER)) return INCOMPATIBLE;
  if (gen1.equals(gen2)) return COMPATIBLE;
  return INCOMPATIBLE;
}


}
