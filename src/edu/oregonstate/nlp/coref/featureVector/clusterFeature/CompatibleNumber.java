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
import edu.oregonstate.nlp.coref.features.FeatureUtils.NPSemTypeEnum;
import edu.oregonstate.nlp.coref.features.FeatureUtils.NumberEnum;
import edu.oregonstate.nlp.coref.features.properties.Gender;
import edu.oregonstate.nlp.coref.features.properties.NPSemanticType;
import edu.oregonstate.nlp.coref.features.properties.Number;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C the two np's agree in gender I if they disagree NA if the gender information for either cannot be
 * determined
 */

public class CompatibleNumber
    extends NominalClusterFeature {

public CompatibleNumber() {
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
  if (num1.equals(NumberEnum.AMBIGUOUS) || num2.equals(NumberEnum.AMBIGUOUS)) return INCOMPATIBLE;
  if (num1.equals(NumberEnum.UNKNOWN) || num2.equals(NumberEnum.UNKNOWN)) return COMPATIBLE;

  if (num1.equals(num2)) return COMPATIBLE;
  NPSemTypeEnum type1 = (NPSemTypeEnum) c1.getProperty(NPSemanticType.getInstance());
  NPSemTypeEnum type2 = (NPSemTypeEnum) c2.getProperty(NPSemanticType.getInstance());
  if((type1.equals(NPSemTypeEnum.ORGANIZATION)&&type2.equals(NPSemTypeEnum.PERSON))||
      (type2.equals(NPSemTypeEnum.ORGANIZATION)&&type1.equals(NPSemTypeEnum.PERSON)) )
    return COMPATIBLE;
  return INCOMPATIBLE;
}

}
