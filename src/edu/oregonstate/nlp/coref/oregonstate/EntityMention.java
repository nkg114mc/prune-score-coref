package edu.oregonstate.nlp.coref.oregonstate;

import java.util.ArrayList;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;

public class EntityMention {
	
	ArrayList<Annotation> mentionTokens;
	
	Annotation np;
	
	String head;
	String span;
	
	int predictClusterID;
	int goldClusterID;
	
	Document belongDoc;
	
	/** 
	 * Construct method
	 **/
	public EntityMention(Annotation np, Document doc) {
		
	}
	
	/** default */
	public EntityMention() {
	}
	
	
	public static EntityMention AnnotationToMention(Annotation np, Document doc) {
		EntityMention em = new EntityMention(np, doc);
		return em;
	}
	
}
