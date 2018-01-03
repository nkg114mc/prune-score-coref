package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.general.RuleResolvers;


/*
 * This feature is: C if one NP is a pronoun and the other NP is its antecedent according to a rule-based algorithm I
 * otherwise
 */

public class ProResolve
    extends NominalFeature {

public ProResolve() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return IC;
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  if (FeatureUtils.isPronoun(np2, doc)) {
    Annotation ant = RuleResolvers.getPronounAntecedentDoNotResolve(np2, doc);
    if (ant != null && ant.equals(np1)) return COMPATIBLE;
  }
  if (FeatureUtils.isPronoun(np1, doc)) {
    Annotation ant = RuleResolvers.getPronounAntecedentDoNotResolve(np1, doc);
    if (ant != null && ant.equals(np2)) return COMPATIBLE;
  }
  return INCOMPATIBLE;
}

}
