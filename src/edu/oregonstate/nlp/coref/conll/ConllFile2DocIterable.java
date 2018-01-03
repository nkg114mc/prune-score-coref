package edu.oregonstate.nlp.coref.conll;

import java.io.File;
import java.util.Iterator;

import edu.oregonstate.nlp.coref.data.Document;

/**
 * @author Chao Ma
 * 
 */
public class ConllFile2DocIterable implements Iterable<Document> 
{
	private ConllFile2DocIterator localIter = null;
	
	public ConllFile2DocIterable() {
		///localIter = 
	}
	
	@Override
	public Iterator<Document> iterator() {
		return localIter;
	}

	public int size() {
		return 0;
		
	}
}
