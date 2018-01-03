package edu.oregonstate.nlp.coref.inout;


import java.io.Serializable;
import java.util.ArrayList;


public class DocPart implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 992870380998919795L;
	
	public String docName;
	public int partIndex;
	public ArrayList<MySentence> sentList;
	public BerkeleyCompatibleConllDoc berkDoc;
	
	public String docText;
	
	public DocPart() {
		sentList = new ArrayList<MySentence>();
		partIndex = -1;
		berkDoc = null;
	}
	
	public DocPart(ArrayList<MySentence> slist, int pid) {
		sentList = slist;
		partIndex = pid;
		berkDoc = null;
	}
	
	public BerkeleyCompatibleConllDoc getBerkDoc() {
		return berkDoc;
	}
}
