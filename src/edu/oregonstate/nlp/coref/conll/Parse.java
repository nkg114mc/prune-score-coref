package edu.oregonstate.nlp.coref.conll;

import java.util.ArrayList;

public class Parse {

	public static String STRING_TYPE = "string";
	public static int COUNT = 0;

	int id;
	int startOffset, endOffset;
	String word;
	String type;
	String pos;
	int parentid = -1;
	ArrayList<Integer> childids;

	public Parse(int startOffset, int endOffset, String type, String pos, String word) {
		this.id = ++COUNT;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.type = type;
		if (pos.equalsIgnoreCase("-NONE-"))
			pos = "NONE";
		else {
			int index = -1;
			if((index = pos.indexOf("-")) > 0) {
				pos = pos.substring(0, index);
			}
		}
		this.pos = pos;
		this.word = word;
	}

	public int getId() {
		return id;
	}

	public void setStartOffset(int offset) {
		this.startOffset = offset;
	}
	
	public void setEndOffset(int offset) {
		this.endOffset = offset;
	}
	
	public int getStartOffset() {
		return this.startOffset;
	}
	
	public int getEndOffset() {
		return this.endOffset;
	}
	
	public void setParent(int id) {
		this.parentid = id;
	}

	public void addChild(int id) {
		if(childids == null)
			childids = new ArrayList<Integer>();
		childids.add(id);
	}
	
	public boolean isLeaf() {
		if (childids == null) {
			return true;
		}
		return (childids.size() == 0);
	}
	
	public static void clearCount() {
		COUNT = 0;
	}
}
