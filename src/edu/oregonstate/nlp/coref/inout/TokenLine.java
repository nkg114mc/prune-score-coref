package edu.oregonstate.nlp.coref.inout;

import java.io.Serializable;

import edu.oregonstate.nlp.coref.data.Annotation;

public class TokenLine implements Serializable{
	public String docID;
	public String token;
	public int partId;
	public int sentId;
	public int tokenId;
	public String pos;
	public String ner;
	public String parse;
	public String coref;
	
	public String speaker;
	public String lemma;
	public String frameset;
	public String wordsense;
	public String predicates;
	
	public int startOff;
	public int endOff;
	public Annotation reconToken;
	
	public int index;
	
	public TokenLine() {
		docID = "-";
		partId = -1;
		sentId = -1;
		tokenId = -1;
		pos = "-";
		token = "-";
		ner = "*";
		parse = "-";
		coref = "-";
		speaker = "-";
		startOff = -1;
		endOff = -1;
		reconToken = null;
		
		index = -1;
	}
}