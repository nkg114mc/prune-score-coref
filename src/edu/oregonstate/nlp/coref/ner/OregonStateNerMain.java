package edu.oregonstate.nlp.coref.ner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class OregonStateNerMain {

	
	public static void main(String[] args) {
		
		Conll2012Main(args);
		
	}
	
	public static void ACE2005Main(String[] args) {
		
	}
	
	public static void Conll2012Main(String[] args) {
		
		ConllNerReader reader = new ConllNerReader();
		
		// file folder
		File conllDir = new File("/scratch/coref/berkeley-coref/berkeleycoref/conll2012-flattened/dev-flattened");
		ArrayList<File> allfiles = new ArrayList<File>(Arrays.asList(conllDir.listFiles()));
		
		reader.readConllDocBatch(allfiles);
		
		
		
		
	}
}
