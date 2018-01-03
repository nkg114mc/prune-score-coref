package edu.oregonstate.nlp.coref.featureExtractor;

import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Utils;


public abstract class Annotator {

//private config;
private String name;
//protected boolean outputAnnotationFiles = true;

public Annotator() {
}

public String getName()
{
  if (name == null) return this.getClass().getName();
  return name;
}

public void setName(String n)
{
  name = n;
}

/**
 * Runs the annotator. This will generate an annotation file.
 * 
 * @param doc
 *          The abstraction of the location in which the document is stored
 * @param annSetNames
 *          The names of the annotation sets that this component produces (In most cases this contains a single name)
 */
public abstract void run(Document doc, String[] annSetNames);

/**
 * Runs the annotator if either the overwrite flag is true or one of the annotation sets does not exist. Generates
 * annotation files.
 * 
 * @param dirName
 *          The location in which the document is stored
 * @param annSetNames
 *          The names of the annotation sets that this component produces (In most cases this contains a single name)
 */

/*
public void run(Document doc, String[] annSetNames, boolean overwrite)
{
  if (overwrite) {
    run(doc, annSetNames);
  }
  else {
    boolean run = false;
    for (String annSet : annSetNames) {
      if (!doc.existsAnnotationSetFile(annSet)) {
        run = true;
      }
    }
    if (run) {
      run(doc, annSetNames);
    }
    else {
      System.out.println("Annotator not run. Annotation files exist.");
    }
  }
}

public void run(Document doc, String annSetName, boolean overwrite)
{
  if (overwrite) {
    run(doc, annSetName);
  }
  else {
    if (!doc.existsAnnotationSetFile(annSetName)) {
      run(doc, annSetName);
    }
    else {
      System.out.println("Annotator not run. Annotation files exist.");
    }
  }
}
*/

public void run(Document doc, String annSetName)
{
  String[] arg = { annSetName };
  run(doc, arg);
}


protected void addResultSet(Document doc, AnnotationSet an){
	doc.addAnnotationSet(an);
}

}
