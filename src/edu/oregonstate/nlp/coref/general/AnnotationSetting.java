package edu.oregonstate.nlp.coref.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/*
  AnnotationSetNames.put(Constants.SENT, "sentence");
  AnnotationSetNames.put(Constants.PAR, "paragraph");
  AnnotationSetNames.put(Constants.POS, "postag");
  AnnotationSetNames.put(Constants.TOKEN, "token");
  AnnotationSetNames.put(Constants.PARSE, "parse");
  AnnotationSetNames.put(Constants.DEP, "dep");
  //AnnotationSetNames.put(Constants.ORIG, "gold_annots");
  AnnotationSetNames.put(Constants.NE, "ne");
  AnnotationSetNames.put(Constants.NP, "basenp");
  //AnnotationSetNames.put(Constants.COREF, "coref");
 */

public class AnnotationSetting {
	
	//private static HashMap<String, String> AnnotationSetNames;
	private static HashSet<String> AllAnnotationSetNames;
	private static HashMap<String, String> AnnotationSetNameToAnnotatorName; // run annotator on the right to get annotation set on the left
	
	private static ArrayList<String> PreprocessingElements;
	private static HashMap<String, String[]> PreprocessingElSetNames;

	private AnnotationSetting() {

		AllAnnotationSetNames = new HashSet<String>();
		AnnotationSetNameToAnnotatorName = new HashMap<String, String>(); 
		
		PreprocessingElements = new ArrayList<String>();
		PreprocessingElSetNames = new HashMap<String, String[]>();

		//
		addOneItem(new String[] { Constants.TOKEN }, "TokenizerOpenNLP" );
		addOneItem(new String[] { Constants.SENT },  "SentenceSplitterOpenNLP" );

	}
	
	private static void addOneItem(String[] annoSetName, String annotatorName) {
		// add annotator name
		PreprocessingElements.add(annotatorName);
		// add annotation set names
		for (String setName : annoSetName) {
			AllAnnotationSetNames.add(setName);
			AnnotationSetNameToAnnotatorName.put(setName, annotatorName);
		}
		// add annotator tp annotation-set name mapping
		PreprocessingElSetNames.put(annotatorName, annoSetName);
	}

/*
	public HashMap<String, String> getAnnotSetNames()
	{
		return AnnotationSetNames;
	}

	public String getAnnotationSetName(String annotSet)
	{
		return AnnotationSetNames.get(annotSet);
	}
*/

	public static ArrayList<String> getPreprocessingElements()
	{
		return PreprocessingElements;
	}

	public static HashMap<String, String[]> getPreprocessingElSetNames()
	{
		return PreprocessingElSetNames;
	}

}

