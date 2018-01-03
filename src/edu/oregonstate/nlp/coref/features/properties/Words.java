package edu.oregonstate.nlp.coref.features.properties;

import java.util.ArrayList;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.FeatureUtils;


public class Words
    extends Property {

private static Property ref = null;

public static Property getInstance()
{
  if (ref == null) {
    ref = new Words(false, true);
  }
  return ref;
}

public static String[] getValue(Annotation np, Document doc)
{
  return (String[]) getInstance().getValueProp(np, doc);
}

private Words(boolean whole, boolean cached) {
  super(whole, cached);
}

@Override
public Object produceValue(Annotation np, Document doc)
{
  String[] words = doc.getWords(np);

  return words;
}

}
