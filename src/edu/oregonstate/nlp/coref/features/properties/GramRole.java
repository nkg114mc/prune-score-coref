package edu.oregonstate.nlp.coref.features.properties;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.SyntaxUtils;


public class GramRole
    extends Property {

private static Property ref = null;

public static Property getInstance()
{
  if (ref == null) {
    ref = new GramRole(true, true);
  }
  return ref;
}

public static String getValue(Annotation np, Document doc)
{
  return (String) getInstance().getValueProp(np, doc);
}

private GramRole(boolean whole, boolean cached) {
  super(whole, cached);
}

@Override
public Object produceValue(Annotation np, Document doc)
{
  String value;
  Annotation hn = HeadNoun.getValue(np, doc);
  AnnotationSet dep = doc.getAnnotationSet(Constants.DEP);
  Annotation d = SyntaxUtils.getDepNode(hn, dep);
  value = "NONE";
  if (d != null) {
    String type = d.getType();
    if (type.equalsIgnoreCase("conj")) {
      Annotation a = d;
      while (type.equalsIgnoreCase("conj")) {
        String[] span = (a.getAttribute("GOV")).split("\\,");
        int stSpan = Integer.parseInt(span[0]);
        int endSpan = Integer.parseInt(span[1]);
        a = dep.getContained(stSpan, endSpan).getFirst();
        if (a == null) {
          break;
        }
        type = a.getType();
      }
    }
    if (FeatureUtils.memberArray(type, FeatureUtils.KNOWN_GRAM_RELATIONS)) {
      value = type;
    }
  }

  return value;

}

}
