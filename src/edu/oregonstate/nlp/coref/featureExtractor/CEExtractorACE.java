package edu.oregonstate.nlp.coref.featureExtractor;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.general.SyntaxUtils;
import edu.oregonstate.nlp.coref.general.Utils;


public class CEExtractorACE
    extends CEExtractor {

public boolean isNoun(Annotation a, String text)
{
  if (a == null) return false;
  String type = a.getType();
  // if(type.startsWith("NNP"))
  // return true;
  if (type.startsWith("NN") && FeatureUtils.isCapitalized(Utils.getAnnotText(a, text))) return true;
  if (type.equals("JJ") && FeatureUtils.isCapitalized(Utils.getAnnotText(a, text))) return true;
  if (type.startsWith("PRP")) return true;
  return false;
}



public boolean isNP(Annotation an, String text)
{
  try {
    String type = an.getType();

    if (FeatureUtils.memberArray(type, SyntaxUtils.NPType)) return true;
    if (type.equalsIgnoreCase("WHNP") && !"that".equalsIgnoreCase(Utils.getAnnotText(an, text))) return true;

  }
  catch (NullPointerException npe) {
    return false;
  }

  return false;
}

public boolean addNE(Annotation a, AnnotationSet includedCEs, AnnotationSet baseCEs, Document doc){
	return true;
}

}
