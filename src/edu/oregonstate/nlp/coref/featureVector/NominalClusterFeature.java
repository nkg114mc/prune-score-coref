package edu.oregonstate.nlp.coref.featureVector;


public abstract class NominalClusterFeature
    extends ClusterFeature {

@Override
public boolean isNominal()
{
  return true;
}

public abstract String[] getValues();

/*
 * A few basic feature values used throughout
 */
public static final String COMPATIBLE = "1";
public static final String INCOMPATIBLE = "0";
protected static final String[] IC = { INCOMPATIBLE, COMPATIBLE };
}
