package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.SoonWords;
import edu.oregonstate.nlp.coref.features.properties.Stopword;
import edu.oregonstate.nlp.coref.features.properties.SubsumesNumber;


/*
 * This feature is: C if, after discarding determiners the strings of the two NPs match I otherwise
 */

public class ExactStrMatch
    extends NominalFeature {

public ExactStrMatch() {
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
  // if(AllFeatures.makeFeature("WordNetClass").getValue(np1, np2, annotations, text, featVector).equals(INCOMPATIBLE))
  // return INCOMPATIBLE;
  if (FeatureUtils.isPronoun(np1, doc) || FeatureUtils.isPronoun(np2, doc)) return INCOMPATIBLE;
  if (Stopword.getValue(np1, doc) || Stopword.getValue(np2, doc)) return INCOMPATIBLE;
  String str1 = doc.getAnnotText(np1);
  String str2 = doc.getAnnotText(np2);
  if(str1.length()<1||str2.length()<1)
    return INCOMPATIBLE;
  if (str1.equals(str2))
    return COMPATIBLE;
  StringBuffer b1 = new StringBuffer(str1);
  b1 = b1.replace(0, 1, b1.substring(0,1).toLowerCase());
  StringBuffer b2 = new StringBuffer(str2);
  b2 = b2.replace(0, 1, b2.substring(0,1).toLowerCase());
  if (b1.toString().equals(b2.toString()))
    return COMPATIBLE;
  return INCOMPATIBLE;
}
}
