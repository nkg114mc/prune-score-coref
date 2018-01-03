package edu.oregonstate.nlp.coref.berkeley;

import java.util.HashSet;

public class BerkeleyCorefLangPack {

	/*
	 * original Scala code
	trait CorefLanguagePack {
		  def getMentionConstituentTypes: Seq[String];
		  def getPronominalTags: Seq[String];
		  def getProperTags: Seq[String];
		}

		class EnglishCorefLanguagePack extends CorefLanguagePack {
		  def getMentionConstituentTypes: Seq[String] = Seq("NP");
		  def getPronominalTags: Seq[String] = Seq("PRP", "PRP$");
		  def getProperTags: Seq[String] = Seq("NNP");
		}

		class ChineseCorefLanguagePack extends CorefLanguagePack {
		  def getMentionConstituentTypes: Seq[String] = Seq("NP");
		  def getPronominalTags: Seq[String] = Seq("PN");
		  def getProperTags: Seq[String] = Seq("NR");
		}

		class ArabicCorefLanguagePack extends CorefLanguagePack {
		  def getMentionConstituentTypes: Seq[String] = Seq("NP");
		  def getPronominalTags: Seq[String] = Seq("PRP", "PRP$");
		  def getProperTags: Seq[String] = Seq("NNP");
		}
	*/
	
	// only English is avaliable here
	public HashSet<String> getMentionConstituentTypes = new HashSet<String>();
	public HashSet<String> getPronominalTags = new HashSet<String>();
	public HashSet<String> getProperTags = new HashSet<String>();
	
	
	public BerkeleyCorefLangPack() {
		// English
		initEnglishCorefLanguagePack();
		// Chinese
		
		// Arabic
	}
	
	private void initEnglishCorefLanguagePack() {
		//
		getMentionConstituentTypes.add("NP");
		//
		getPronominalTags.add("PRP");
		getPronominalTags.add("PRP$");
		//
		getProperTags.add("NNP");
	}
}
