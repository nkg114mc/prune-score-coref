package edu.oregonstate.nlp.coref.features.properties;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.FeatureUtils;


public class ContainsAcronym
    extends Property {

private static Property ref = null;

public static Property getInstance()
{
  if (ref == null) {
    ref = new ContainsAcronym(true, true);
  }
  return ref;
}

public static Boolean getValue(Annotation np, Document doc)
{
  return (Boolean) getInstance().getValueProp(np, doc);
}

private ContainsAcronym(boolean whole, boolean cached) {
  super(whole, cached);
}

@Override
public Object produceValue(Annotation np, Document doc)
{
  String[] words = doc.getWords(np);
  for (String w : words) {
    if (FeatureUtils.isAllCaps(w)) return true;
  }
  return false;
}

}
