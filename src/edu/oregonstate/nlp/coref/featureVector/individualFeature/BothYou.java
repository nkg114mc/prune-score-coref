package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.FeatureUtils.PersonPronounTypeEnum;


/*
 * This feature is: C if both NPs are comparable pronouns -- i.e., he and his I otherwise
 */

public class BothYou
    extends NominalFeature {

public BothYou() {
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

  if (!FeatureUtils.isPronoun(np1, doc) || !FeatureUtils.isPronoun(np2, doc)) return INCOMPATIBLE;
  if (PersonPronounTypeEnum.SECOND.equals(FeatureUtils.getPronounPerson(doc.getAnnotText(np1)))
      &&PersonPronounTypeEnum.SECOND.equals(FeatureUtils.getPronounPerson(doc.getAnnotText(np2)))) 
    return COMPATIBLE;

  return INCOMPATIBLE;
}

}
