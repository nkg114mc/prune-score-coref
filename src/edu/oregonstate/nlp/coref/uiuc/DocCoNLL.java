/**
 * contains methods to load input from files in CoNLL 2012 shared task format.
 */
package edu.oregonstate.nlp.coref.uiuc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The superclass of documents loaded from CoNLL 2011 format
 * 
 * @author Kai-Wei Chang
 */
public class DocCoNLL {
	
	private static final long serialVersionUID = 45L;
	private String m_logFN;
	private String m_logPath="log";
	//private TextAnnotation ta;
	private String m_fileRelative;
	private List<String> m_whoSaid = new ArrayList<String>();
	private boolean m_forTraining = false;

	/** Basic constructor: Not recommended. */
	public DocCoNLL() {
	}
/*
	public void write(boolean usePredictions) {
		// TODO: Generalize file name.
		this.write("predictions/" + m_docID + ".pred.apf.xml", usePredictions);
	}

	public void write(String filenameBase, boolean usePredictions) {
		// open file
		PrintStream dout;
		try {
			dout = new PrintStream(new FileOutputStream(filenameBase
					+ ".apf.xml"));
		} catch (IOException e) {
			System.err.println("Cannot open file for writing.");
			e.printStackTrace();
			return;
		}
		dout.println("<?xml version=\"1.0\"?>");
		dout.println("<!DOCTYPE source_file SYSTEM \"apf.v4.0.1.dtd\">");
		dout.println("<source_file URI=\"" + m_docID + ".sgm\" " + "SOURCE=\""
				+ m_source + "\" TYPE=\"" + m_docType + "\" " + "VERSION=\""
				+ m_version + "\" " + "AUTHOR=\"" + m_annotationAuthor + "\" "
				+ "ENCODING=\"" + m_encoding + "\">");

		dout.println("<document DOCID=\"" + m_docID + "\">");

		List<Entity> entities = new ArrayList<Entity>(getEntities());
		Collections.sort(entities);
		for (Entity e : entities) {
			// TODO: Subtypes???
			dout.println(toXMLString(e) + "\n");
		}

		dout.println("</document>");
		dout.println("</source_file>");
		// Close file
		dout.close();
	}
*/
	/**
	 * Removes the extension (including the periods) from the filename, if it
	 * has an extension. For DocAPF files, the extension is
	 * {@literal ".apf.xml"}.
	 * 
	 * @param filename
	 *            The name of the file.
	 * @return The name of the file with the extension removed.
	 */
	protected String getBaseFilename(String filename) {
		if (filename.endsWith(".apf.xml"))
			return filename.substring(0, filename.length() - 8);
		else
			return filename;
	}

	/** Converts plain text to XML safe format by escaping ampersands. */
	protected String toXMLString(String plainText) {
		return plainText.replaceAll("&", "&AMP;");
	}




} // End class Doc
