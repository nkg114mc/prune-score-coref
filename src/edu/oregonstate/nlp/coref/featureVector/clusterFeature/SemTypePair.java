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
import edu.oregonstate.nlp.coref.features.FeatureUtils.NPSemTypeEnum;
import edu.oregonstate.nlp.coref.features.properties.Definite;
import edu.oregonstate.nlp.coref.features.properties.Gender;
import edu.oregonstate.nlp.coref.features.properties.ProperName;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C the two np's agree in gender I if they disagree NA if the gender information for either cannot be
 * determined
 */

public class SemTypePair
    extends NominalClusterFeature {

public SemTypePair() {
  name = this.getClass().getSimpleName();
}

static String[] npTypes = { "p", "o", "l", "e", "n", "d" };
@Override
public String[] getValues()
{
  String[] result = new String[npTypes.length * npTypes.length];
  int count = 0;
  for (String t1 : npTypes) {
    for (String t2 : npTypes) {
      result[count++] = t1 + t2;
    }
  }

  return result;
}

public static char getType(NPSemTypeEnum type)
{
  switch(type){
  case PERSON:
    return 'p';
  case ORGANIZATION:
    return 'o';
  case LOCATION:
  case GPE:
    return 'l';
  case NOTPERSON:
    return 'n';
  case DATE:
    return 'd';
  default:
    return 'e';

  }
}


@Override
public String produceValue(CorefChain c1, CorefChain c2, Document doc, Map<ClusterFeature, String> featVector)
{

  return Character.toString(getType(c1.getSemType())) + Character.toString(getType(c2.getSemType()));
}

}
