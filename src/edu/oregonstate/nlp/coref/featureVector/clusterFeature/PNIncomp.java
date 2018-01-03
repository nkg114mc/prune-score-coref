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
 * This feature is: C is the two NPs are proper names and have any word in common
 */

public class PNIncomp
    extends NominalClusterFeature {

public PNIncomp() {
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

  if(n1!=null&&n2!=null&&n1.size()>0&&n2.size()>0){
    for(String s1: n1)
      for(String s2: n2)
        if (Utils.isAnySubset(FeatureUtils.getWords(s1), FeatureUtils.getWords(s2)))
          return INCOMPATIBLE;
    return COMPATIBLE;
  }
  return INCOMPATIBLE;
}


}
