package edu.oregonstate.nlp.coref.inout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import edu.berkeley.nlp.syntax.Tree;
import edu.oregonstate.nlp.coref.data.Annotation;

public class MySentence implements Serializable {
	public int index;
	public ArrayList<TokenLine> tokenList;
	public HashMap<Integer, TokenLine> tlineIndexing;
	public Annotation reconSent;

	// berkeley compitable documents
	public Tree<String> ptrees;
	public ArrayList<MyChunk<String>> neChunk;
	public ArrayList<MyChunk<Integer>> menChunk;
	
	
	public MySentence() {
		tokenList = new ArrayList<TokenLine>();
		tlineIndexing = new HashMap<Integer, TokenLine>();
		index = -1;
	}

	public void addTokenLine(TokenLine tkln) {
		tokenList.add(tkln);
		tlineIndexing.put(tkln.index, tkln);
	}
	
	public static HashMap<Integer, TokenLine> getEmptyTkLineMap() {
		return (new HashMap<Integer, TokenLine>());
	}
	
	public boolean isEmpty() {
		if (tokenList.size() == 0) {
			return true;
		}
		return false;
	}
	
	public int getLength() {
		return tokenList.size();
	}
}
