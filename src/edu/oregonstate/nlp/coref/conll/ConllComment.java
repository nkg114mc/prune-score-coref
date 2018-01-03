package edu.oregonstate.nlp.coref.conll;

public class ConllComment {
	public String originComment;
	public boolean isBegin;
	public boolean isEnd;
	public String docID;
	public int    partNum;
	public ConllComment() {
		originComment = "";
		isBegin = false;
		isEnd = false;
		docID = "";
		partNum = 0;
	}
}