package edu.oregonstate.nlp.coref.featureVector.clusterFeature;

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

public class ProResolveRuleR1
    extends NominalClusterFeature {

public ProResolveRuleR1() {
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
//  if (Constructor.createClusterFeature("ProResolveCl").getValue(c1, c2, doc, featVector).equals(COMPATIBLE)) {
//    return "R1".equals(c2.getProperty(Property.RULE_NUM))?COMPATIBLE:INCOMPATIBLE;
//  }
//  return INCOMPATIBLE;
    return ProResolveCl.isRuleApplicable(c1, c2, 1, doc)?COMPATIBLE:INCOMPATIBLE;
}

}
