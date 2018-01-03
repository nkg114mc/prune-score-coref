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

public class ProComp
    extends NominalFeature {

public ProComp() {
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

  if (!FeatureUtils.isPronoun(np1, doc) || !FeatureUtils.isPronoun(np2, doc)) return COMPATIBLE;
  if (Constructor.createFeature(BothInQuotes.class.getName()).getValue(np1, np2, doc, featVector).equals(NA)
      &&!PersonPronounTypeEnum.SECOND.equals(FeatureUtils.getPronounPerson(doc.getAnnotText(np1)))
      &&!PersonPronounTypeEnum.SECOND.equals(FeatureUtils.getPronounPerson(doc.getAnnotText(np2)))) 
    return COMPATIBLE;

  if (Constructor.createFeature(Gender.class.getName()).getValue(np1, np2, doc, featVector).equals(INCOMPATIBLE))
    return INCOMPATIBLE;
  if (!Constructor.createFeature(Number.class.getName()).getValue(np1, np2, doc, featVector).equals(COMPATIBLE)) return INCOMPATIBLE;
  if (FeatureUtils.getPronounPerson(doc.getAnnotText(np1)) == FeatureUtils.getPronounPerson(doc.getAnnotText(np2)))
    return COMPATIBLE;
  return INCOMPATIBLE;
}

}
