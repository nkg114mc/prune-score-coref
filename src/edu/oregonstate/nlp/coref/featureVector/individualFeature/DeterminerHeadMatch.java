package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.properties.HeadNoun;
import edu.oregonstate.nlp.coref.features.properties.SoonWords;
import edu.oregonstate.nlp.coref.features.properties.Stopword;
import edu.oregonstate.nlp.coref.features.properties.SubsumesNumber;
import edu.oregonstate.nlp.coref.features.properties.Words;


/*
 * This feature is: C if, after discarding determiners the strings of the two NPs match I otherwise
 */

public class DeterminerHeadMatch
    extends NominalFeature {

public DeterminerHeadMatch() {
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
  if (FeatureUtils.isPronoun(np1, doc) || FeatureUtils.isPronoun(np2, doc)) return INCOMPATIBLE;
  if (Stopword.getValue(np1, doc) || Stopword.getValue(np2, doc)) return INCOMPATIBLE;
  String[] words2 = Words.getValue(np2, doc);
  String[] hnWords = doc.getWords(np1.getStartOffset(),HeadNoun.getValue(np1, doc).getEndOffset());
  
  String str1 = doc.getAnnotText(np1);
  String str2 = doc.getAnnotText(np2);
  if(str1.length()<1||str2.length()<1)
    return INCOMPATIBLE;
  if (str2.equalsIgnoreCase("the "+str1))
    return COMPATIBLE;
  if(hnWords.length<1||words2.length<2||hnWords.length<words2.length)
    return INCOMPATIBLE;
  if (words2[0].equalsIgnoreCase("the")||words2[0].equalsIgnoreCase("that")){
    for(int i = words2.length-1; i>0; i--)
      if(!words2[i].equalsIgnoreCase(hnWords[i]))
        return INCOMPATIBLE;
    return COMPATIBLE;
  }
  return INCOMPATIBLE;
}
}
