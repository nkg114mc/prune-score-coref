package edu.oregonstate.nlp.coref.features.properties;

import java.util.ArrayList;
import java.util.List;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.general.Constants;


public class PostModifier
    extends Property {

private static Property ref = null;

public static Property getInstance()
{
  if (ref == null) {
    ref = new PostModifier(false, true);
  }
  return ref;
}

public static String[] getValue(Annotation np, Document doc)
{
  return (String[]) getInstance().getValueProp(np, doc);
}

private PostModifier(boolean whole, boolean cached) {
  super(whole, cached);
}

@Override
public Object produceValue(Annotation np, Document doc)
{
  String[] value = null;
  if (Conjunction.getValue(np, doc)) return new String[0];
  AnnotationSet posAnnots = doc.getAnnotationSet(Constants.POS);
  // AnnotationSet ne = annotations.get(Constants.NE);
  Annotation head = HeadNoun.getValue(np, doc);
  // AnnotationSet overlap = ne.getContained(np);
  // Annotation namedEntity;
  // if (overlap == null || overlap.isEmpty())
  // namedEntity = null;
  // else
  // namedEntity = overlap.getLast();
  Annotation namedEntity = (Annotation) Property.LINKED_PROPER_NAME.getValueProp(np, doc);
  ArrayList<String> result = new ArrayList<String>();

  // Loop over all tokens
  int start = head.getEndOffset();
  int end = np.getEndOffset();
  if (namedEntity != null && namedEntity.getEndOffset() > start) {
    start = namedEntity.getEndOffset();
    // System.err.println(head);
    // System.err.println(np);
    // System.err.println("st"+start+" end"+end);
  }

  List<Annotation> contained = posAnnots.getOverlapping(start, end).getOrderedAnnots();
  for (Annotation a : contained) {
    String word = doc.getAnnotString(a);
    if (FeatureUtils.isNumeral(word) || FeatureUtils.isCardinalNumber(a) || FeatureUtils.isAdjective(a)
        || FeatureUtils.isAdverb(a) || (!FeatureUtils.isStopword(word) && !FeatureUtils.isUninfWord(word))) {
      result.add(word);
    }
  }

  value = result.toArray(new String[0]);
  if (value == null) {
    value = new String[0];
  }
  return value;

}

}
