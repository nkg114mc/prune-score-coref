package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.general.Constants;


/*
 * This feature is: I if the second NP is an indefinite and is not an appositive C otherwise
 */

public class Indefinite1
    extends NominalFeature {

public Indefinite1() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return IC;
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  if (featVector.get(Constructor.createFeature(Constants.APPOSITIVE)).equals(COMPATIBLE)) return INCOMPATIBLE;
  if (FeatureUtils.articleType(np2, doc).equals(FeatureUtils.ArticleTypeEnum.INDEFINITE)||FeatureUtils.articleType(np2, doc).equals(FeatureUtils.ArticleTypeEnum.QUANTIFIED)) return COMPATIBLE;
  return INCOMPATIBLE;
}

}
