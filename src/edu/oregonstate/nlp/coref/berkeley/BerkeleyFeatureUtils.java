package edu.oregonstate.nlp.coref.berkeley;

public class BerkeleyFeatureUtils {

	public enum ConjType {
		NONE, TYPE, TYPE_OR_RAW_PRON, CANONICAL, CANONICAL_NOPRONPRON, CANONICAL_ONLY_PAIR_CONJ;
	}

	public enum MentionType {
		PROPER, NOMINAL, PRONOMINAL, UNKNOWN;
	}
	
	// proun dict
	public static  BerkeleyPronounDict berkeleyPronounDictionary = new BerkeleyPronounDict();
	
	// head finder
	public static BerkeleyModCollinsHeadFinder berkeleyHeadFinder = new BerkeleyModCollinsHeadFinder();
	
	// langPack
	public static BerkeleyCorefLangPack berkeleyLangPack = new BerkeleyCorefLangPack();

}
