package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.properties.GramRole;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.SyntaxUtils;


/*
 * This feature is: Y if the first NP is subject of the main clause N otherwise
 * 
 * Not the most efficient implementation;
 */

public class MCSubject1
    extends NominalFeature {

public MCSubject1() {
  name = this.getClass().getSimpleName();
}

@Override
public String[] getValues()
{
  return YN;
}

@Override
public String produceValue(Annotation np1, Annotation np2, Document doc, Map<Feature, String> featVector)
{
  boolean sub = GramRole.getValue(np1, doc).equals("SUBJECT");
  return sub && SyntaxUtils.isMainClause(np1, doc.getAnnotationSet(Constants.PARSE)) ? "Y" : "N";
}

}
