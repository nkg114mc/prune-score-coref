package edu.oregonstate.nlp.coref.berkeley;

import java.util.ArrayList;
import java.util.HashMap;

public class BerkeleyPronounDict {

	String[] firstPersonPronouns = { "i", "me", "myself", "mine", "my", "we", "us", "ourself", "ourselves", "ours", "our"};
	String[] secondPersonPronouns = { "you", "yourself", "yours", "your", "yourselves"};
	String[] thirdPersonPronouns = { "he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "it", "itself", "its", "one", "oneself", "one's", "they", "them", "themself", "themselves", "theirs", "their", "they", "them", "'em", "themselves"};
	String[] otherPronouns = { "who", "whom", "whose", "where", "when","which"};

	// Borrowed from Stanford
	String[] singularPronouns = { "i", "me", "myself", "mine", "my", "yourself", "he", "him", "himself", "his", "she", "her", "herself", "hers", "her", "it", "itself", "its", "one", "oneself", "one's"};
	String[] pluralPronouns = { "we", "us", "ourself", "ourselves", "ours", "our", "yourself", "yourselves", "they", "them", "themself", "themselves", "theirs", "their"};
	String[] malePronouns = { "he", "him", "himself", "his"};
	String[] femalePronouns = { "her", "hers", "herself", "she"};
	String[] neutralPronouns = {"it", "its", "itself", "where", "here", "there", "which"};


	public ArrayList<String> allPronouns =  new ArrayList<String>();

	// Constructed based on Stanford's Dictionaries class
	public HashMap<String, String> pronounsToCanonicalPronouns = new HashMap<String, String>();



	@SuppressWarnings("unchecked")
	public BerkeleyPronounDict() {
		pronounsToCanonicalPronouns.put("i", "i");
		pronounsToCanonicalPronouns.put("me", "i");
		pronounsToCanonicalPronouns.put("my", "i");
		pronounsToCanonicalPronouns.put("myself", "i");
		pronounsToCanonicalPronouns.put("mine", "i");
		pronounsToCanonicalPronouns.put("you", "you");
		pronounsToCanonicalPronouns.put("your", "you");
		pronounsToCanonicalPronouns.put("yourself", "you");
		pronounsToCanonicalPronouns.put("yourselves", "you");
		pronounsToCanonicalPronouns.put("yours", "you");
		pronounsToCanonicalPronouns.put("he", "he");
		pronounsToCanonicalPronouns.put("him", "he");
		pronounsToCanonicalPronouns.put("his", "he");
		pronounsToCanonicalPronouns.put("himself", "he");
		pronounsToCanonicalPronouns.put("she", "she");
		pronounsToCanonicalPronouns.put("her", "she");
		pronounsToCanonicalPronouns.put("herself", "she");
		pronounsToCanonicalPronouns.put("hers", "she");

		pronounsToCanonicalPronouns.put("we", "we");
		pronounsToCanonicalPronouns.put("us", "we");
		pronounsToCanonicalPronouns.put("our", "we");
		pronounsToCanonicalPronouns.put("ourself", "we");
		pronounsToCanonicalPronouns.put("ourselves", "we");
		pronounsToCanonicalPronouns.put("ours", "we");
		pronounsToCanonicalPronouns.put("they", "they");
		pronounsToCanonicalPronouns.put("them", "they");
		pronounsToCanonicalPronouns.put("their", "they");
		pronounsToCanonicalPronouns.put("themself", "they");
		pronounsToCanonicalPronouns.put("themselves", "they");
		pronounsToCanonicalPronouns.put("theirs", "they");
		pronounsToCanonicalPronouns.put("'em", "they");
		pronounsToCanonicalPronouns.put("it", "it");
		pronounsToCanonicalPronouns.put("itself", "it");
		pronounsToCanonicalPronouns.put("its", "it");
		pronounsToCanonicalPronouns.put("one", "one");
		pronounsToCanonicalPronouns.put("oneself", "one");
		pronounsToCanonicalPronouns.put("one's", "one");

		pronounsToCanonicalPronouns.put("that", "that");
		pronounsToCanonicalPronouns.put("which", "which");
		pronounsToCanonicalPronouns.put("who", "who");
		pronounsToCanonicalPronouns.put("whom", "who");
		//  pronounsToCanonicalPronouns.put("where", "where");
		//  pronounsToCanonicalPronouns.put("whose", "whose");
		// This entry is here just to make results consistent with earlier ones
		// on our very small dev set
		pronounsToCanonicalPronouns.put("thy", "thy");
		pronounsToCanonicalPronouns.put("y'all", "you");
		pronounsToCanonicalPronouns.put("you're", "you");
		pronounsToCanonicalPronouns.put("you'll", "you");
		pronounsToCanonicalPronouns.put("'s", "'s");


		////////////////////////////////////////
		for (String str : firstPersonPronouns) {
			allPronouns.add(str);
		}
		for (String str : secondPersonPronouns) {
			allPronouns.add(str);
		}
		for (String str : thirdPersonPronouns) {
			allPronouns.add(str);
		}
		for (String str : otherPronouns) {
			allPronouns.add(str);
		}
	}


	public boolean isPronLc(String str) {
		return allPronouns.contains(str.toLowerCase());
	}

	public String getCanonicalPronLc(String str) {
		if (!pronounsToCanonicalPronouns.containsKey(str.toLowerCase())) {
			return "";
		} else {
			return pronounsToCanonicalPronouns.get(str.toLowerCase());
		}
	}

	public static void main(String[] args) {
		BerkeleyPronounDict pd = new BerkeleyPronounDict();
		//System.out.println(pd.pronounsToCanonicalPronouns("'em"));
		System.out.println(pd.isPronLc("them"));
		System.out.println(pd.isPronLc("Them"));
		System.out.println(pd.isPronLc("NotThem"));
	}

}
