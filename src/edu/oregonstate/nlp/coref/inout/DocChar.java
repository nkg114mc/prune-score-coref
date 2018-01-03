package edu.oregonstate.nlp.coref.inout;

import java.io.Serializable;
import java.util.ArrayList;

public class DocChar implements Serializable {

	public char c;
	public int originOffset;
	public boolean inTag;

	public DocChar(char ch, int offset) {
		c = ch;
		originOffset = offset;
		inTag = false;
	}

	public DocChar(DocChar dc) {
		c = dc.c;
		originOffset = dc.originOffset;
		inTag = dc.inTag;
	}

	public String getString() {
		return String.valueOf(c);

	}

	public static String getActualString(ArrayList<DocChar> charList) {
		if (charList == null) {
			return "";
		}
		if (charList.size() == 0) {
			return "";
		}
		char charArr[] = new char[charList.size()];
		int i = 0;
		for (i = 0; i < charList.size(); i++) {
			charArr[i] = charList.get(i).c;
		}
		//System.out.println("Last = [" + charArr[charList.size() - 1] + "]");
		return (new String(charArr));
	}
}
