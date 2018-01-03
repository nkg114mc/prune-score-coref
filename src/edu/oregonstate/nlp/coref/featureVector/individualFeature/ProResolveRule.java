package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.RuleResolvers;


/*
 * This feature is: C if one NP is a pronoun and the other NP is its antecedent according to a rule-based algorithm I
 * otherwise
 */

public class ProResolveRule
    extends NominalFeature {

public ProResolveRule() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return new String[]{"I1","I2","I3","Y1","W1","W2","R1","R2","R3","R4","R5","R6","R7","R8","NONE"};
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  if (Constructor.createFeature("ProResolve").getValue(np1, np2, doc, featVector).equals(COMPATIBLE)) {
    
    return np2.getProperty(Property.RULE_NUM)==null?"NONE":np2.getProperty(Property.RULE_NUM).toString();
  }
  return "NONE";
}

}
