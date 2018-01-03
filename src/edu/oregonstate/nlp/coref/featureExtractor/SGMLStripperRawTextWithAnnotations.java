/*
 * stripSGML.java nathan; Mar 15,  2010
 */
package edu.oregonstate.nlp.coref.featureExtractor;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

import org.xml.sax.Attributes;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;


public class SGMLStripperRawTextWithAnnotations extends SGMLStripper {

FileWriter rawTextFile;
FileWriter originalRawTextFile;
int offset;
Stack<Annotation> anStack;

/*
 * Call the parent's constructor. 
 */
public SGMLStripperRawTextWithAnnotations() {
  super();
}

/*
 * Preprocesses the input file so things like ampersands don't break
 * the parser. 
 */
@Override
public void format(BufferedReader br, FileWriter out) throws IOException
{
}

@Override
public void run(Document doc, String[] annSetNames)
{
	//open up the annotations
	AnnotationSet annots = doc.getAnnotationSet(Constants.ORIG);	
	AnnotationSet nps = annots.get("COREF");
	AnnotationSet temp = new AnnotationSet("temp");
	
	//check for duplicates
	for (Annotation a : nps) {
		if (!temp.containsSpan(a)) {
			a.setAttribute("ID", Integer.toString(a.getId()));
			temp.add(a);
		}		
	}
   
	nps = labelClusterIDs(temp);
	nps.setName("gsNPs");
 
	int counter = 0;	
	for (Annotation np : nps) {
      np.setAttribute(Constants.CE_ID, Integer.toString(counter++));
    }
	 

    doc.addAnnotationSet(nps);
}

/*
 * Grabs the opening SGML tag. 
 */
@Override
public void startElement(String uri, String name, String qName, Attributes atts)
{

}

/*
 * Grabs the closing tag. 
 */
@Override
public void endElement(String uri, String name, String qName)
{
}

/*
 * This prints out all the text between the tags we care about to 
 * a file. 
 *
 */
@Override
public void characters(char ch[], int start, int length)
{
}
}
