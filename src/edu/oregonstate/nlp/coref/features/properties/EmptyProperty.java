package edu.oregonstate.nlp.coref.features.properties;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;

public class EmptyProperty
    extends Property {

public EmptyProperty(String name, boolean whole, boolean cached) {
  super(whole, cached);
  this.name = name;
}

@Override
public Object produceValue(Annotation np, Document doc)
{
  return np.getProperty(this);
}

}
