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
import edu.oregonstate.nlp.coref.features.FeatureUtils.NumberEnum;
import edu.oregonstate.nlp.coref.features.properties.Gender;
import edu.oregonstate.nlp.coref.features.properties.Number;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C the two np's agree in gender I if they disagree NA if the gender information for either cannot be
 * determined
 */

public class SameNumber
    extends NominalClusterFeature {

public SameNumber() {
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
  NumberEnum num1 = (NumberEnum) c1.getProperty(Number.getInstance());
  NumberEnum num2 = (NumberEnum) c2.getProperty(Number.getInstance());
  if (num1.equals(NumberEnum.PLURAL) && num2.equals(NumberEnum.PLURAL)) return COMPATIBLE;
  if (num2.equals(NumberEnum.SINGLE) && num1.equals(NumberEnum.SINGLE)) return COMPATIBLE;
  return INCOMPATIBLE;
}


}
