package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.FeatureUtils.NPSemTypeEnum;
import edu.oregonstate.nlp.coref.features.properties.HeadNoun;
import edu.oregonstate.nlp.coref.features.properties.ProperName;
import edu.oregonstate.nlp.coref.features.properties.ProperNameType;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.Constants;

/*
 * This feature is: I if the two NP's have the same maximal NP projection C otherwise
 */

public class RoleAppositive
    extends NominalFeature {

public RoleAppositive() {
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

  Annotation maxNP1 = edu.oregonstate.nlp.coref.features.properties.MaximalNP.getValue(np1, doc);
  Annotation maxNP2 = edu.oregonstate.nlp.coref.features.properties.MaximalNP.getValue(np2, doc);

  // System.err.println(Utils.getAnnotText(np2, text)+" -> "+Utils.getAnnotText(maxNP2, text));
  FeatureUtils.NPSemTypeEnum type1 = ProperNameType.getValue(np1, doc);
  FeatureUtils.NPSemTypeEnum type2 = ProperNameType.getValue(np2, doc);

  if (maxNP1.compareSpan(np2) == 0&&type2!=null&&type2.equals(FeatureUtils.NPSemTypeEnum.PERSON)
      && ProperName.getValue(np2, doc)) {
    NPSemTypeEnum pn2t = ProperNameType.getValue(np2, doc);
    if(pn2t.equals(NPSemTypeEnum.PERSON)){
      Annotation pn2 = (Annotation) Property.LINKED_PROPER_NAME.getValueProp(np2, doc);
      int start = np1.getEndOffset(), end = pn2.getStartOffset();
      if(start>end)
        return INCOMPATIBLE;
      String inText = doc.getAnnotText(start, end);
      if(inText.matches("[\\,\\s]*")) return COMPATIBLE;
    }
  }
  if (maxNP2.compareSpan(np1) == 0&&type1!=null&&type1.equals(FeatureUtils.NPSemTypeEnum.PERSON)) {
    NPSemTypeEnum pn1t = ProperNameType.getValue(np1, doc);
    if(pn1t.equals(NPSemTypeEnum.PERSON)){
      Annotation pn1 = (Annotation) Property.LINKED_PROPER_NAME.getValueProp(np1, doc);
      int start = np2.getEndOffset(), end = pn1.getStartOffset();
      if(start>end)
        return INCOMPATIBLE;
      String inText = doc.getAnnotText(start, end);
      if(inText.matches("[\\,\\s]*")) return COMPATIBLE;
    }
  }
    return INCOMPATIBLE;
}

}
