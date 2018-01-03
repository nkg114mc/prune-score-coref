/**
 * 
 * @author David Golland
 * 
 */

package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;

import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;

public abstract class StructuredClassifier {


/**
 * get the name of this class
 * 
 * @return
 */
public String getName()
{
  return this.getClass().getName();
}


public void train(Iterable<Document> traindocs,  Iterable<Document> validdocs) {
	
	List<Document> trndocs = iterToList(traindocs);
	List<Document> devdocs = iterToList(validdocs);
	
	//Iterables.concat(trndocs, traindocs);
	//Iterables.concat(devdocs, validdocs);
	
	train(trndocs, devdocs);
}

public List<Document>  iterToList(Iterable<Document> docs) {
	List<Document> dlist = new ArrayList<Document>();
	for (Document d : docs) {
		dlist.add(d);
	}
	return dlist;
}

public abstract void train(List<Document> traindocs, List<Document> validdocs);

/**
 * Classifies the instances located in the test directories in the Config. Uses the model specifided in classifier
 * creation for the testing
 * 
 * @param options
 *          - a string array of various options used in testing (e.g. - where to load the model file, testing
 *          parameters, etc.)
 * @return the minimum and maximum numerical values of the classified instances
 */
public abstract AnnotationSet test(Document doc);
public abstract void testAll(Iterable<Document> docs);


}
