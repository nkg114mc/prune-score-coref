package edu.oregonstate.nlp.coref.mentions;

public class MentionExtractorFactory {
	
	public static enum MentExtractType { 
			GoldConllMentionExtractor,
			BerkeleyConllMentionExtractor,
			FileLoadedMentionExtractor
		};
	
	public static AbstractMentionExtractor createMentionExtractor(MentExtractType mtyp) {
	
		if (mtyp == MentExtractType.GoldConllMentionExtractor) {
			return (new GoldConllMentionExtractor());
		} else if (mtyp == MentExtractType.BerkeleyConllMentionExtractor) {
			return (new BerkeleyConllMentionExtractor());
		} else if (mtyp == MentExtractType.FileLoadedMentionExtractor) {
			//AbstractMentionExtractor mentExtractor = new FileLoadedMentionExtractor("/home/mc/workplace/rand_search/random_search_proj/mentDump80.txt");
			//AbstractMentionExtractor mentExtractor = new FileLoadedMentionExtractor("/home/mc/workplace/rand_search/random_search_proj/mentDumpFull.txt");
			//AbstractMentionExtractor mentExtractor = new FileLoadedMentionExtractor("/home/mc/workplace/coref2017/HOT-coref/ims-hotcoref-2014-06-06/hotmentions-all.txt");
			//AbstractMentionExtractor mentExtractor = new FileLoadedMentionExtractor("/home/mc/workplace/rand_search/random_search_proj/mentDumpHot0.1.txt");
			AbstractMentionExtractor mentExtractor = new FileLoadedMentionExtractor("mentionDump/mentDumpHot0.2.txt");
			return mentExtractor;
		} else {
			throw new RuntimeException("Unknown extractor type: " + mtyp);
		}
		
	}

}
