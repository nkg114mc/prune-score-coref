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
import edu.oregonstate.nlp.coref.features.properties.InfWords;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C if the two clusters contain a proper name in common
 */

public class ProperName
    extends NominalClusterFeature {

public ProperName() {
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
  ArrayList<String> w1 = new ArrayList<String>();
  ArrayList<String> w2 = new ArrayList<String>();
  HashSet<String> n1 = (HashSet<String>) c1.getProperty(CorefChain.ALL_NAMES);
  HashSet<String> n2 = (HashSet<String>) c2.getProperty(CorefChain.ALL_NAMES);

  if(n1!=null) for(String s:n1) w1.addAll(getWords(s));
  if(n2!=null) for(String s:n2) w2.addAll(getWords(s));
  String[] words1 = w1.toArray(new String[0]);
  String[] words2 = w2.toArray(new String[0]);
  int inter = FeatureUtils.intersection(words1, words2);
  if (inter < 1) // Math.min(words1.length, words2.length))
    return INCOMPATIBLE;
  return COMPATIBLE;
}

public ArrayList<String> getWords(String s){
  ArrayList<String> result = new ArrayList<String>();
  String[] words = Document.getWords(s);
  for (String w : words) {

    if (!FeatureUtils.isUninfWord(w.toLowerCase()) 
        && !FeatureUtils.isCorpDesign(w)) {
      result.add(w);
    }
  }
  return result;
}
}
