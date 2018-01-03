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

public class CountryCapital
    extends NominalClusterFeature {

public CountryCapital() {
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
  HashSet<String> n1 = (HashSet<String>) c1.getProperty(CorefChain.ALL_NAMES);
  HashSet<String> n2 = (HashSet<String>) c2.getProperty(CorefChain.ALL_NAMES);

  if(n1!=null&&n2!=null)
    for(String s1: n1)
      for(String s2: n2){
        if(!s1.equals(s2)){
          String cap1 = FeatureUtils.getCapital(s1);
          if(cap1!=null && cap1.equals(s2)){
            //System.out.println("CAPITAL"+c1.toString(doc)+"\n"+c2.toString(doc));
            return COMPATIBLE;
          }
          String cap2 = FeatureUtils.getCapital(s2);
          if(cap2!=null && cap2.equals(s1)){
            //System.out.println("CAPITAL"+c1.toString(doc)+"\n"+c2.toString(doc));
            return COMPATIBLE;
          }
        }
      }
  return INCOMPATIBLE;
}


}
