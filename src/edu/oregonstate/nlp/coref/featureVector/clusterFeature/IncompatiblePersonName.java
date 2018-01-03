package edu.oregonstate.nlp.coref.featureVector.clusterFeature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.ClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.FeatureUtils.GenderEnum;
import edu.oregonstate.nlp.coref.features.FeatureUtils.NPSemTypeEnum;
import edu.oregonstate.nlp.coref.features.properties.Gender;
import edu.oregonstate.nlp.coref.features.properties.NPSemanticType;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C the two np's agree in gender I if they disagree NA if the gender information for either cannot be
 * determined
 */

public class IncompatiblePersonName
    extends NominalClusterFeature {

public IncompatiblePersonName() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return IC;
}

@Override
public String produceValue(CorefChain c1, CorefChain c2, Document doc, Map<ClusterFeature, String> featVector)
{
  HashSet<String> fn1 = (HashSet<String>) c1.getProperty(CorefChain.FIRST_NAMES);
  HashSet<String> ln1 = (HashSet<String>) c1.getProperty(CorefChain.LAST_NAMES);
  HashSet<String> fn2 = (HashSet<String>) c2.getProperty(CorefChain.FIRST_NAMES);
  HashSet<String> ln2 = (HashSet<String>) c2.getProperty(CorefChain.LAST_NAMES);
  NPSemTypeEnum type1 = (NPSemTypeEnum) c1.getProperty(NPSemanticType.getInstance());
  NPSemTypeEnum type2 = (NPSemTypeEnum) c2.getProperty(NPSemanticType.getInstance());
  if(type1!=NPSemTypeEnum.PERSON&&type2!=NPSemTypeEnum.PERSON)
    return INCOMPATIBLE;
  if(ln1==null||ln2==null)
    return INCOMPATIBLE;
  if(fn1==null&&fn2==null)
    return FeatureUtils.intersection(ln1.toArray(new String[0]), ln2.toArray(new String[0]))>0?INCOMPATIBLE:COMPATIBLE;
  if(fn1!=null&&fn2!=null){
    return (FeatureUtils.intersection(ln1.toArray(new String[0]), ln2.toArray(new String[0]))>0&&
        FeatureUtils.intersection(fn1.toArray(new String[0]), fn2.toArray(new String[0]))>0)?INCOMPATIBLE:COMPATIBLE;
  }
  if(fn1!=null){
    return (FeatureUtils.intersection(ln1.toArray(new String[0]), ln2.toArray(new String[0]))>0||
        FeatureUtils.intersection(fn1.toArray(new String[0]), ln2.toArray(new String[0]))>0)?INCOMPATIBLE:COMPATIBLE;
  }
  if(fn2!=null){
    return (FeatureUtils.intersection(ln1.toArray(new String[0]), ln2.toArray(new String[0]))>0||
        FeatureUtils.intersection(ln1.toArray(new String[0]), fn2.toArray(new String[0]))>0)?INCOMPATIBLE:COMPATIBLE;
  }
  return INCOMPATIBLE;
}

}
