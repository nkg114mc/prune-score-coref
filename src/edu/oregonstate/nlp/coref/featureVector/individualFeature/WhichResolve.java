package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.HeadNoun;
import edu.oregonstate.nlp.coref.general.RuleResolvers;


/*
 * This feature is: C if one NP is a pronoun and the other NP is its antecedent according to a rule-based algorithm I
 * otherwise
 */

public class WhichResolve
    extends NominalFeature {

public WhichResolve() {
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
  String annotText = doc.getAnnotText(np2);

  if("which".equals(annotText)||"that".equals(annotText)||"where".equals(annotText)){
    if(!FeatureUtils.sameSentence(np1, np2, doc)) return INCOMPATIBLE;
    if(edu.oregonstate.nlp.coref.features.properties.MaximalNP.getValue(np1, doc).properCovers(np1)) return INCOMPATIBLE;
    if(edu.oregonstate.nlp.coref.features.properties.MaximalNP.getValue(np1, doc).properCovers(np2)) return COMPATIBLE;
    //System.out.println("Resolving which: "+doc.getAnnotString(np1)+ " -- "+doc.getAnnotString(np2));
    int start = np1.getEndOffset(), end = np2.getStartOffset();
    if(start>end)
      start = HeadNoun.getValue(np1, doc).getEndOffset();
    if(start>end)
      return INCOMPATIBLE;
    String inText = doc.getAnnotText(start, end);
    if(inText.matches("\\W*")) return COMPATIBLE;
  }
  return INCOMPATIBLE;
}

}
