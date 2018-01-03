package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.Definite;
import edu.oregonstate.nlp.coref.features.properties.ProperName;


/*
 * Encodes the type of the pair
 */

public class PairType
    extends NominalFeature {

public PairType() {
  name = this.getClass().getSimpleName();
}

static String[] npTypes = { "n", "p", "d", "i" };

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

public static char getType(Annotation np, Document doc)
{
  if (ProperName.getValue(np, doc)) return 'n';
  if (FeatureUtils.isPronoun(np, doc)) return 'p';
  if (Definite.getValue(np, doc)) return 'd';
  return 'i';
}

public static int getTypeNumber(Annotation np, Document doc){
  char type = getType(np, doc);
  switch(type){
  case 'n':
    return 1;
  case 'p':
    return 2;
  case 'd':
    return 3;
  case 'i':
    return 4;
  }
  return 0;
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{

  return Character.toString(getType(np1, doc)) + Character.toString(getType(np2, doc));
}

}
