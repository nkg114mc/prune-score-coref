/*
 * A common ancestor for all feature types
 */
package edu.oregonstate.nlp.coref.featureVector;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


public abstract class ClusterFeature {

protected String name;
protected boolean ignore = false;

public ClusterFeature() {
  name = getClass().getSimpleName();
}

public boolean isNominal()
{
  return false;
}

public boolean isNumeric()
{
  return false;
}

public boolean isString()
{
  return false;
}

public abstract String produceValue(CorefChain c1, CorefChain c2, Document doc, Map<ClusterFeature, String> featVector);

// A cached version of the produce value function
public String getValue(CorefChain c1, CorefChain c2, Document doc, Map<ClusterFeature, String> featVector)
{
  String val = featVector.get(this);
  if (val == null) {
    val = produceValue(c1, c2, doc, featVector);
  }
  featVector.put(this, val);
  return val;
}

public String getName()
{
  return name;
}

public boolean ignoreFeature()
{
  return ignore;
}
public boolean structuredOnly()
{
  return false;
}

} // end of the file
