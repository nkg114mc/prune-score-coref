package edu.oregonstate.nlp.coref.inout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import edu.berkeley.nlp.syntax.Tree;

public class BerkeleyDepTree implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6286206010075907151L;
	public Tree<String> btree;
	public ArrayList<String> spans;
	public ArrayList<String> postags;
	public HashMap<Integer, Integer> depMap;

	
	public BerkeleyDepTree(Tree<String> t, 
			               ArrayList<String> w,
			               ArrayList<String> pos, 
			               HashMap<Integer, Integer> d) {
		btree = t;
		spans = w;
		postags = pos;
		depMap = d;
	}
	
	public boolean checkValidation() {
		
		// tree, pos, words
		int ntreeleaves = btree.getTerminals().size();
		//System.out.println(btree.ge);
		int npos = postags.size();
		int nwords = spans.size();
		
		if ((ntreeleaves !=  npos) ||
			(npos != nwords) ||
			(ntreeleaves != nwords)) {
			throw new RuntimeException("Tree Words Pos number inconsistence! " + ntreeleaves + " " + npos + " " + nwords);
		}
		
		
		return true;
	}
	
	public Integer terminalLength() {
		checkValidation();
		int l = spans.size();
		return (new Integer(l)); 
	}
	
	
	
	

			  
}