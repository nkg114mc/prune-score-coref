package edu.oregonstate.nlp.coref.featureVector.clusterFeature;

import java.util.ArrayList;
import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.ClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.NominalClusterFeature;
import edu.oregonstate.nlp.coref.features.properties.ParNum;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C the two np's agree in gender I if they disagree NA if the gender information for either cannot be
 * determined
 */
public class ParSize2LTE1
    extends NominalClusterFeature {

public static final double MAX_SIZE=20;

public ParSize2LTE1() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return IC;
}
@Override
public boolean structuredOnly()
{
  return true;
}
@Override
public String produceValue(CorefChain c1, CorefChain c2, Document doc, Map<ClusterFeature, String> featVector)
{
  int size = ParSize2LTE1.parSize(c1, c2, doc);
  return size>0&&size<=1?COMPATIBLE:INCOMPATIBLE;//Double.toString(Math.log(size));
}

public static int parSize(CorefChain c1, CorefChain c2, Document doc){
  CorefChain first = c1.before(c2)?c1:c2;
  CorefChain second = c1.before(c2)?c2:c1;

  ArrayList<Annotation> ces = second.getCes();
  Annotation np2 = first.getFirstCe();
  Integer par = ParNum.getValue(np2, doc), next = par+1;
  int size =0;
  for(Annotation ce:ces)
    if(ParNum.getValue(ce, doc)==par||ParNum.getValue(ce, doc)==next)
      size++;
  return size;
}

}
