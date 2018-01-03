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

public class SameSemType
    extends NominalClusterFeature {

public SameSemType() {
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
  NPSemTypeEnum type1 = (NPSemTypeEnum) c1.getProperty(NPSemanticType.getInstance());
  NPSemTypeEnum type2 = (NPSemTypeEnum) c2.getProperty(NPSemanticType.getInstance());
  if (type1.equals(NPSemTypeEnum.UNKNOWN) || type1.equals(NPSemTypeEnum.AMBIGUOUS)|| type1.equals(NPSemTypeEnum.NOTPERSON)) return INCOMPATIBLE;
  if (type1.equals(type2)) return COMPATIBLE;
  return INCOMPATIBLE;
}


}
