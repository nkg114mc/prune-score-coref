package edu.oregonstate.nlp.coref.features.properties;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.FeatureUtils;


public class NormalizedID
    extends Property {

private static Property ref = null;

public static Property getInstance()
{
  if (ref == null) {
    ref = new NormalizedID(false, true);
  }
  return ref;
}

public static Boolean getValue(Annotation np, Document doc)
{
  return (Boolean) getInstance().getValueProp(np, doc);
}

private NormalizedID(boolean whole, boolean cached) {
  super(whole, cached);
}

@Override
public Object produceValue(Annotation np, Document doc)
{
  Boolean value;
  String s = doc.getAnnotText(np);
  value = FeatureUtils.getTitles().contains(s.toLowerCase()) && FeatureUtils.isCapitalized(s);
  return value;

}

}
