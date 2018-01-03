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
 * This feature is: C if the two nps are proper names and the same substing
 */

public class PNStr
    extends NominalClusterFeature {

public PNStr() {
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

  if(n1!=null&&n2!=null)
    for(String str1:n1){
      for(String str2:n2){
        String[] s1 = FeatureUtils.getWords(str1);
        String[] s2 = FeatureUtils.getWords(str2);
        if (FeatureUtils.equalsIgnoreCase(s1, s2)) return COMPATIBLE;
        String[] infWords1 = FeatureUtils.removeUninfWordsLeaveCorpDesign(s1);
        String[] infWords2 = FeatureUtils.removeUninfWordsLeaveCorpDesign(s2);
        if (FeatureUtils.equalsIgnoreCase(infWords1, infWords2)) return COMPATIBLE;
      }
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
