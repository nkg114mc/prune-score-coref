package edu.oregonstate.nlp.coref.featureVector.clusterFeature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.ClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.RuleResolvers;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C if one NP is a pronoun and the other NP is its antecedent according to a rule-based algorithm I
 * otherwise
 */

public class PossibleAnte
    extends NominalClusterFeature {

public PossibleAnte() {
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
  if(c2.before(c1)&&c1.isPronoun()){
    if(isAnte(c1, c2, doc))
      return COMPATIBLE;
  }
  if(c1.before(c2)&&c2.isPronoun()){
    if(isAnte(c2, c1, doc))
      return COMPATIBLE;
  }
  return INCOMPATIBLE;
}

boolean isAnte(CorefChain second, CorefChain first, Document doc){
  //System.out.println("Matching "+c1.toString(doc)+" and "+c2.toString(doc)+"\nc2Id="+c2.getId());
  Annotation an1 = second.getFirstCe();
  HashMap<String,HashSet<Integer>> ant1 = second.getProAntecedents(an1);
  for(String r:ant1.keySet()){
    HashSet<Integer> ant2 = ant1.get(r);
    if(ant2.contains(first.getId())){
      return true;
    }
  }
  return false;
}
}
