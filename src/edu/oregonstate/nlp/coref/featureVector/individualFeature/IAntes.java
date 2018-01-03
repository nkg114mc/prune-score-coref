package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.FeatureUtils.PersonPronounTypeEnum;
import edu.oregonstate.nlp.coref.features.properties.HeadNoun;
import edu.oregonstate.nlp.coref.features.properties.Number;
import edu.oregonstate.nlp.coref.features.properties.Property;


/*
 * This feature is: Y if the first NP is part of quoted string N otherwise
 * 
 * Not the most efficient implementation;
 */

public class IAntes
    extends NominalFeature {

public IAntes() {
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
  if (FeatureUtils.isPronoun(np1, doc) && FeatureUtils.getPronounPerson(doc.getAnnotText(np1)) == PersonPronounTypeEnum.FIRST
      && FeatureUtils.NumberEnum.SINGLE.equals(Number.getValue(np1, doc))) {
    Annotation author = (Annotation) Property.AUTHOR.getValueProp(np1, doc);
    if (HeadNoun.getValue(np2, doc).overlaps(author)) return COMPATIBLE;
  }
  if (FeatureUtils.isPronoun(np2, doc) && FeatureUtils.getPronounPerson(doc.getAnnotText(np2)) == PersonPronounTypeEnum.FIRST
      && FeatureUtils.NumberEnum.SINGLE.equals(Number.getValue(np2, doc))) {
    Annotation author = (Annotation) Property.AUTHOR.getValueProp(np2, doc);
    if (HeadNoun.getValue(np1, doc).overlaps(author)) return COMPATIBLE;
  }
  return INCOMPATIBLE;

}

}
