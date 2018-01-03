package edu.oregonstate.nlp.coref.conll;

import java.io.File;
import java.util.Iterator;

import edu.oregonstate.nlp.coref.data.Document;

/**
 * @author Chao Ma
 * 
 */
public class ConllFile2DocIterator implements Iterator<Document> 
{
	// candidate conll document files
	private Iterable<File> fIterable;
	
	// current variables
	
	// int total size
	

	public ConllFile2DocIterator(Iterable<File> docI) {
		fIterable = docI;
	}







	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public Document next() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}


}
