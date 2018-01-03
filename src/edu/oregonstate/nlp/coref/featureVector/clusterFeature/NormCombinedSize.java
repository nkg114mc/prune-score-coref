package edu.oregonstate.nlp.coref.featureVector.clusterFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.ClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.featureVector.NumericClusterFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.FeatureUtils.AnimacyEnum;
import edu.oregonstate.nlp.coref.features.FeatureUtils.GenderEnum;
import edu.oregonstate.nlp.coref.features.FeatureUtils.NumberEnum;
import edu.oregonstate.nlp.coref.features.properties.Animacy;
import edu.oregonstate.nlp.coref.features.properties.Gender;
import edu.oregonstate.nlp.coref.features.properties.Number;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.structuredClassifiers.CorefChain;


/*
 * This feature is: C the two np's agree in gender I if they disagree NA if the gender information for either cannot be
 * determined
 */
public class NormCombinedSize
    extends NumericClusterFeature {

public static final double MAX_SIZE=20;

public NormCombinedSize() {
  name = this.getClass().getSimpleName();
}
@Override
public boolean structuredOnly(){
  return true;
}

@Override
public String produceValue(CorefChain c1, CorefChain c2, Document doc, Map<ClusterFeature, String> featVector)
{
  double size = c1.getCes().size()+c2.getCes().size();
  return Double.toString(size/(double)doc.getAnnotationSet(Constants.NP).size());//Double.toString(Math.log(size));
}

}
