package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.FeatureUtils.NumberEnum;
import edu.oregonstate.nlp.coref.features.properties.HeadNoun;
import edu.oregonstate.nlp.coref.features.properties.NPSemanticType;
import edu.oregonstate.nlp.coref.features.properties.Number;
import edu.oregonstate.nlp.coref.features.properties.ProperName;
import edu.oregonstate.nlp.coref.features.properties.WNSemClass;


/*
 * This feature is: C if the two NPs form the pattern <sum> of <money> (e.g. loss of 1 million) I otherwise
 */

public class InOfRelation
    extends NominalFeature {

public InOfRelation() {
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
  //Capture expressions such as "the town of Sofia"
  if (!FeatureUtils.sameSentence(np1, np2, doc)) return INCOMPATIBLE;
  if (FeatureUtils.isPronoun(np1, doc) || FeatureUtils.isPronoun(np2, doc)) return INCOMPATIBLE;
  Annotation first, second;
  if (np1.getStartOffset() <= np2.getStartOffset()) {
    first = np1;
    second = np2;
  }
  else {
    first = np2;
    second = np1;
  }

  if (!inOfRel(first, second, doc, featVector)) return INCOMPATIBLE;
  if(!ProperName.getValue(np1, doc) && ProperName.getValue(np2, doc) && NPSemanticType.getValue(first, doc).equals(NPSemanticType.getValue(first, doc)))
    return COMPATIBLE;
  return INCOMPATIBLE;
}

private boolean inOfRel(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  if (!FeatureUtils.sameSentence(np1, np2, doc)) return false;
  Annotation head = HeadNoun.getValue(np1, doc);
  if (head.getEndOffset() >= np2.getStartOffset()) return false;
  String inBetween = doc.getAnnotString(head.getEndOffset(), np2.getStartOffset());
  if (inBetween.matches("\\W*of\\W*")) return true;
  return false;
}
}
