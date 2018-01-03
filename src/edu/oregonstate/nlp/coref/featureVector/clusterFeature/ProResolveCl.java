package edu.oregonstate.nlp.coref.featureVector.clusterFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

public class ProResolveCl
    extends NominalClusterFeature {

public ProResolveCl() {
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
  int rule =50;
  //System.out.println("PronounResolveCl "+c1.getId()+ " and "+c2.getId());

//  if(!c1.getPossibleAntesChain().contains(c2)&&!c2.getPossibleAntesChain().contains(c1)){
//    return INCOMPATIBLE;
//  }
//  //System.out.println("Trying chains "+c1.getId()+ " and "+c2.getId());
//  HashMap<Annotation,HashMap<String,ArrayList<CorefChain>>> antes2 = c2.getAllProAntecedents();
//  int rule1 = getRule(c1,antes2);
//  HashMap<Annotation,HashMap<String,ArrayList<CorefChain>>> antes1 = c1.getAllProAntecedents();
//  int rule2 = getRule(c2,antes1);
  int rule1=50,rule2=50;
  if(c2.before(c1)&&c1.isPronoun()){
    rule1 = getRuleNum(c1, c2, doc);
  }
  if(c1.before(c2)&&c2.isPronoun()){
    rule2 = getRuleNum(c2, c1, doc);
  }
  rule = rule1<rule2?rule1:rule2;
  if(rule<50){
    c2.setProperty(Property.RULE_NUM, "R"+rule);
    c2.setProperty(Property.RULE_COREF_ID, c1.getId());
    //System.out.println("ResolvedP "+c1.getId()+" and "+c2.getId()+" rule "+rule);
    return COMPATIBLE;
  }
  return INCOMPATIBLE;
}

  int getRule(CorefChain c, HashMap<Annotation,HashMap<String,ArrayList<CorefChain>>> ant){
    int result = 50;
    for(HashMap<String,ArrayList<CorefChain>> ant1:ant.values()){
      for(String rule:ant1.keySet()){
        ArrayList<CorefChain> ant2 = ant1.get(rule);
        if(ant2.size()==1 && ant2.contains(c)){
          //Match -- check the rule number
          //System.out.println("Matched "+c+ " -- rule -- "+rule);
          int ruleNum = Integer.parseInt(rule);
          result = ruleNum<result?ruleNum:result;
        }
      }
    }
    return result;
  }
  
  int getRuleNum(CorefChain second, CorefChain first, Document doc){
    int rule1=50;
    //System.out.println("Matching "+c1.toString(doc)+" and "+c2.toString(doc)+"\nc2Id="+c2.getId());
    Annotation an1 = second.getFirstCe();
    HashMap<String,HashSet<Integer>> ant1 = second.getProAntecedents(an1);
    for(String r:ant1.keySet()){
      HashSet<Integer> ant2 = ant1.get(r);
      //clean up the antecedent set
      if(ant2.size()==1){
        //System.out.println("FORRULE"+r+" -- "+ant2.iterator().next());
        if(ant2.contains(first.getId())){
          //Match -- check the rule number
          //System.out.println("Matched "+c1.getId()+" and "+c2.getId()+ " -- rule -- "+r);
          int ruleNum = Integer.parseInt(r);
          rule1 = ruleNum<rule1?ruleNum:rule1;
        }
      }
    }
    return rule1;
  }
  public static boolean isRuleApplicable(CorefChain c1, CorefChain c2, int rule, Document doc){
    CorefChain first = c1.before(c2)?c1:c2;
    CorefChain second = c1.before(c2)?c2:c1;
    if(!second.isPronoun())
      return false;
    Annotation an1 = second.getFirstCe();
    HashMap<String,HashSet<Integer>> ant1 = second.getProAntecedents(an1);
    HashSet<Integer> ruleAnte = ant1.get(Integer.toString(rule));
    if(ruleAnte!=null && ruleAnte.size()==1){
      //System.out.println("FORRULE"+r+" -- "+ant2.iterator().next());
      if(ruleAnte.contains(first.getId())){
        return true;
      }
    }
    return false;
  }
}
