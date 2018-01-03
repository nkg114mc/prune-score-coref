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
 * This feature is: Y if the second NP is the subject of the main clause N otherwise
 * 
 * Not the most efficient implementation;
 */

public class MCSubject2
    extends NominalFeature {

public MCSubject2() {
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
  boolean sub = GramRole.getValue(np2, doc).equals("SUBJECT");
  return sub && SyntaxUtils.isMainClause(np2, doc.getAnnotationSet(Constants.PARSE)) ? "Y" : "N";
}

}
