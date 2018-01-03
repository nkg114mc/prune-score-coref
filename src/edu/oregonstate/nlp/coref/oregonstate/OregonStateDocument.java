package edu.oregonstate.nlp.coref.oregonstate;

import java.util.ArrayList;

/**
 * A light weight document class which specifically designed for coref problem
 * @author Chao Ma
 *
 */
public class OregonStateDocument {

	ArrayList<EntityMention> predictMentions;
	ArrayList<EntityMention> goldMentions;
	
	String doctext;
	
}
