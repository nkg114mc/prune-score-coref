package edu.oregonstate.nlp.coref.features.properties;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.SyntaxUtils;


public class Conjunction
    extends Property {

private static Property ref = null;

public static Property getInstance()
{
  if (ref == null) {
    ref = new Conjunction(true, true);
  }
  return ref;
}

public static Boolean getValue(Annotation np, Document doc)
{
  return (Boolean) getInstance().getValueProp(np, doc);
}

private Conjunction(boolean whole, boolean cached) {
  super(whole, cached);
}

@Override
public Object produceValue(Annotation np, Document doc)
{
  Boolean value = false;
  Annotation head = HeadNoun.getValue(np, doc);
  AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
  AnnotationSet dep = doc.getAnnotationSet(Constants.DEP);
  Annotation d = SyntaxUtils.getDepNode(head, dep);
  if (d != null) {
    String type = d.getType();
    if (type.equalsIgnoreCase("conj")) {
      value = true;
    }
  }
  if(value){
    AnnotationSet contained = nps.getContained(np.getStartOffset(), head.getEndOffset());
    if (contained.size() > 0 && np.getStartOffset() < head.getStartOffset()) {
      String words[] = FeatureUtils.getWords(doc.getAnnotText(np.getStartOffset(), head.getStartOffset()));
      value = FeatureUtils.memberArray("and", words);
    }else{
      value = false;
    }
  }
  return value;
}

}
