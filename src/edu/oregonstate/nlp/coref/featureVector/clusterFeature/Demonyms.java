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
import edu.oregonstate.nlp.coref.features.properties.Conjunction;
import edu.oregonstate.nlp.coref.features.properties.Gender;
import edu.oregonstate.nlp.coref.features.properties.InfWords;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.Utils;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C is the two NPs are proper names and have any word in common
 */

public class Demonyms
    extends NominalClusterFeature {

public Demonyms() {
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
  if(c1.isConjunction()||c2.isConjunction())
    return INCOMPATIBLE;
  HashSet<String> n1 = (HashSet<String>) c1.getProperty(CorefChain.ALL_NAMES);
  HashSet<String> n2 = (HashSet<String>) c2.getProperty(CorefChain.ALL_NAMES);

  HashSet<String> all1 = new HashSet<String>();
  if(n1!=null) all1.addAll(n1);
  HashSet<String> all2 = new HashSet<String>();
  if(n2!=null) all2.addAll(n2);
  HashSet<String> h1 = (HashSet<String>) c1.getProperty(CorefChain.HEADS);
  if(h1!=null) all1.addAll(h1);
  HashSet<String> h2 = (HashSet<String>) c2.getProperty(CorefChain.HEADS);
  if(h2!=null) all2.addAll(h2);
  for(String s1: all1)
    for(String s2: all2){
      if(!s1.equalsIgnoreCase(s2)){
        Integer d1 = FeatureUtils.getDemonymNumber(s1);
        Integer d2 = FeatureUtils.getDemonymNumber(s2);

        if (d1!=null && d1.equals(d2)){
          //System.out.println("DEMONYM"+c1.toString(doc)+"\n"+c2.toString(doc));
          return COMPATIBLE;
        }
      }
    }
  return INCOMPATIBLE;
}


}
