package edu.oregonstate.nlp.coref.featureVector.clusterFeature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import opennlp.tools.coref.resolver.IsAResolver;

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
import edu.oregonstate.nlp.coref.general.Utils;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: I if the two nps contain proper names and do not overlap
 */

public class ContainsPN
    extends NominalClusterFeature {

public ContainsPN() {
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
//  HashSet<String> fn1 = (HashSet<String>) c1.getProperty(CorefChain.FIRST_NAMES);
//  HashSet<String> ln1 = (HashSet<String>) c1.getProperty(CorefChain.LAST_NAMES);
//  HashSet<String> fn2 = (HashSet<String>) c2.getProperty(CorefChain.FIRST_NAMES);
//  HashSet<String> ln2 = (HashSet<String>) c2.getProperty(CorefChain.LAST_NAMES);
  HashSet<String> n1 = (HashSet<String>) c1.getProperty(CorefChain.ALL_NAMES);
  HashSet<String> n2 = (HashSet<String>) c2.getProperty(CorefChain.ALL_NAMES);

  if(n1!=null&&n2!=null){
    for(String s1:n1){
      for(String s2:n2){
        String[] words1 = FeatureUtils.getInfWords(s1);
        String[] words2 = FeatureUtils.getInfWords(s2);
        if (FeatureUtils.overlaps(words1, words2)) return COMPATIBLE;
      }
    }
    return INCOMPATIBLE;
  }

  return INCOMPATIBLE;
}

public ArrayList<String> getWords(String s){
  ArrayList<String> result = new ArrayList<String>();
  String[] words = Document.getWords(s);
  for (String w : words) {

    if (!FeatureUtils.isUninfWord(w.toLowerCase()) && !(FeatureUtils.isAlphabetStr(w) || "I".endsWith(w))
        && !FeatureUtils.isCorpDesign(w)) {
      result.add(w);
    }
  }
  return result;
}
public String[] getWordArray(String s){
  return getWords(s).toArray(new String[0]);
}
}
