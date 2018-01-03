package edu.oregonstate.nlp.coref.inout;

import java.io.Serializable;

// Chunk class that is compatible to Berkeley chunk
public class MyChunk<T> implements Serializable{
	
    // T stands for "Type"
	private T label;
    //public void set(T t) { this.label = t; }
    public T get() { return label; }
	
    public  int index;
	private int start;
	private int end;

    
    public MyChunk(int s, int e) {
    	start = s;
    	end = e;
    }
    
    public void setLabel(T t) {
    	label = t;
    }
    
    public int getStart() {
    	return start;
    }
    public int getEnd() {
    	return end;
    }
    public T getLabel() {
    	return label;
    }

}
