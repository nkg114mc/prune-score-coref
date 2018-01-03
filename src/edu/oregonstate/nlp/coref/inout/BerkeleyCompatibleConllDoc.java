package edu.oregonstate.nlp.coref.inout;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.oregonstate.nlp.coref.data.AnnotationSet;

public class BerkeleyCompatibleConllDoc implements Serializable{

	public String docName;
	public int docPartID;
	public ArrayList<ArrayList<String>> tokOffsets;
	public ArrayList<ArrayList<String>> spans;
	public ArrayList<ArrayList<String>> pos;
	public ArrayList<BerkeleyDepTree> dtrees;
	public ArrayList<ArrayList<MyChunk<String>>> nerChunks;//: Seq[Seq[Chunk[String]]],
	public ArrayList<ArrayList<MyChunk<Integer>>> corefChunks;//: Seq[Seq[Chunk[Int]]],
	public ArrayList<ArrayList<String>> speakers;


    public AnnotationSet goldMentions;
	public AnnotationSet predictMentions;
	
	public BerkeleyCompatibleConllDoc() {
		
	}

	public void initAllValues() {
		
		docName = "????";
		docPartID = -1;
		
		tokOffsets = new ArrayList<ArrayList<String>>();
		spans = new ArrayList<ArrayList<String>>();
		pos = new ArrayList<ArrayList<String>>();
		dtrees = new ArrayList<BerkeleyDepTree>();
		nerChunks = new ArrayList<ArrayList<MyChunk<String>>>();//: Seq[Seq[Chunk[String]]],
		corefChunks = new ArrayList<ArrayList<MyChunk<Integer>>>();//: Seq[Seq[Chunk[Int]]],
		speakers = new ArrayList<ArrayList<String>>();
				
	    goldMentions = null;
		predictMentions = null;
		
	}
	
	public boolean checkConsistency() {
		System.out.println("Berk doc = " + docName);
		return true;
	}
	
}
