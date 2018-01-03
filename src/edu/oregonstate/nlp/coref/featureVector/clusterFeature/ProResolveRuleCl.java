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

public class ProResolveRuleCl
    extends NominalClusterFeature {

public ProResolveRuleCl() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return new String[]{"R1","R2","R3","R4","R5","R6","R7","R8","NONE"};
}

@Override
public String produceValue(CorefChain c1, CorefChain c2, Document doc, Map<ClusterFeature, String> featVector)
{
  if (Constructor.createClusterFeature("ProResolveCl").getValue(c1, c2, doc, featVector).equals(COMPATIBLE)) {
    
    return c2.getProperty(Property.RULE_NUM)==null?"NONE":c2.getProperty(Property.RULE_NUM).toString();
  }
  return "NONE";
}

}
