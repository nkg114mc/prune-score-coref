package edu.oregonstate.nlp.coref.conll;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.dcoref.CorefCluster;
import edu.stanford.nlp.dcoref.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Generics;
import edu.oregonstate.nlp.coref.SystemConfig;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureExtractor.CEExtractor;
import edu.oregonstate.nlp.coref.general.Constants;

public class CEExtractorConll2011Stanford  extends CEExtractor {

	private boolean hasInitialized = false;
	
	public void init(SystemConfig cfg) {
		hasInitialized = true;
	}
	
	@Override
	public boolean isNoun(Annotation a, String text) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNP(Annotation an, String text) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addNE(Annotation a, AnnotationSet includedCEs,
			AnnotationSet baseCEs, Document doc) {
		// TODO Auto-generated method stub
		return false;
	}

	public void run(Document ReconcileDoc, String[] annSetNames, edu.stanford.nlp.dcoref.Document stanfordDoc, boolean overwrite) {
		if (stanfordDoc != null) {
			AnnotationSet stfdMentions = translate(ReconcileDoc, Constants.STANFORD_NP, stanfordDoc);
			ReconcileDoc.addAnnotationSet(stfdMentions);
		}
	}
	
	/**
	 * Translate the stanford extracted mentions into reconcile annotations.
	 * */
	public AnnotationSet translate(Document ReconcileDoc, String annSetNames, edu.stanford.nlp.dcoref.Document stanfordDoc) {

		// reconcile tokens
		AnnotationSet reconTokens = ReconcileDoc.getAnnotationSet(Constants.TOKEN);
		
		// stanford mentions
		AnnotationSet stfdMentions = new AnnotationSet(Constants.STANFORD_NP);

		/*
	    /////////////////////////////////////////////////////
	    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	    for (Mention m1 : document.allGoldMentions.values()) {
	          System.out.println(m1.spanToString()+" "+m1.corefClusterID);
	    }
	    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	    /////////////////////////////////////////////////////
		 */
		int globalMentionID = 0;
/*
		for (int sentI = 0; sentI < orderedMentionsBySentence.size(); sentI++) {
			List<Mention> orderedMentions = orderedMentionsBySentence.get(sentI);
			for (int mentionI = 0; mentionI < orderedMentions.size(); mentionI++) {
				Mention m1 = orderedMentions.get(mentionI);

				int id = m1.mentionID;
				int start = m1.startIndex;
				int end = m1.endIndex;
				String mspan = m1.spanToString();

				globalMentionID++;
				// construct the mention
				Annotation newMention = new Annotation(globalMentionID, start, end, Constants.STANFORD_NP);

				// set all ussfull informations if needed
				newMention.setAttribute(Constants.CONLL_WORDSTR, mspan);
				newMention.setAttribute(Constants.CE_ID, String.valueOf(id));//String.valueOf(globalMentionID));

				// ...

				// add to the set
				stfdMentions.add(newMention);
				
			}
		}
*/
		
		HashMap<Integer, Integer> beginOffsetToIndex = new HashMap<Integer, Integer>(); // beginOffset -> GlobalIndex
		HashMap<Integer, CoreLabel> stanfordTokenMap = new HashMap<Integer, CoreLabel>(); // index -> CoreLabel
 		HashMap<Integer, Annotation> ourTokenMap = new HashMap<Integer, Annotation>(); // index -> 
 		
		int partID = Integer.parseInt(stanfordDoc.conllDoc.getPartNo()); // part number
		
		edu.stanford.nlp.pipeline.Annotation anno = stanfordDoc.annotation;
		List<List<String[]>> conllDocSentences = stanfordDoc.conllDoc.getSentenceWordLists();
	    List<CoreMap> sentences = anno.get(CoreAnnotations.SentencesAnnotation.class);

	    List<List<Mention>> ordMentions = stanfordDoc.predictedOrderedMentionsBySentence;
	    
	    int tokenCnt = 0;
	    for (int sentNum = 0 ; sentNum < sentences.size() ; sentNum++){
	    	List<CoreLabel> sentence = sentences.get(sentNum).get(CoreAnnotations.TokensAnnotation.class);
	    	List<String[]> conllSentence = conllDocSentences.get(sentNum);

	    	
	    	for(int i = 0; i < sentence.size(); i++){
	    		tokenCnt++;
	    		CoreLabel label = sentence.get(i);
	    		Annotation ourToken = reconTokens.get(tokenCnt);
	    		
	    		// remember them!
	    		stanfordTokenMap.put(tokenCnt, label); // index -> CoreLabel
	     		ourTokenMap.put(tokenCnt, ourToken); // index -> token
	    		beginOffsetToIndex.put(label.beginPosition(), tokenCnt); // beginOffset -> index
	    		
	    		//System.out.println(label.beginPosition() + " " + label.endPosition() + " " + label.word() + " " + ReconcileDoc.getAnnotString(ourToken));
	    	}
	    	
	    	List<Mention> mentions = ordMentions.get(sentNum);
	    	for (Mention m1 : mentions) {
				
	    		// believe in the Stanford singleton predictor
	    		if (!m1.isSingleton) {
	    			globalMentionID++;

	    			List<CoreLabel> spans = m1.originalSpan;
	    			int id = m1.mentionID;
	    			int start = spans.get(0).beginPosition(); // stanford start
	    			int end = spans.get(spans.size() - 1).endPosition(); // stanford end
	    			String mspan = m1.spanToString();


	    			Annotation ourBeginTk = ourTokenMap.get(beginOffsetToIndex.get(spans.get(0).beginPosition()));
	    			int ourStard = ourBeginTk.getStartOffset();
	    			Annotation ourEndTk = ourTokenMap.get(beginOffsetToIndex.get(spans.get(spans.size() - 1).beginPosition()));
	    			int ourEnd = ourEndTk.getEndOffset();


	    			// construct the mention
	    			Annotation newMention = new Annotation(globalMentionID, ourStard, ourEnd, Constants.STANFORD_NP);

	    			// set all ussfull informations if needed
	    			newMention.setAttribute(Constants.CONLL_WORDSTR, mspan);
	    			newMention.setAttribute(Constants.CE_ID, String.valueOf(partID * 2000 + globalMentionID));


	    			// other properties that might be useful
	    			newMention.setAttribute("StanfordAnimacy", m1.animacy.toString()); // animacy
	    			newMention.setAttribute("StanfordGender", m1.gender.toString()); // gender
	    			newMention.setAttribute("StanfordNumber", m1.number.toString());  // number
	    			newMention.setAttribute("StanfordPerson", m1.person.toString()); // person

	    			newMention.setAttribute("StanfordHeadWord", m1.headWord.word()); // head
	    			newMention.setAttribute("StanfordNERStr", m1.nerString);
	    			newMention.setAttribute("StanfordSentNum", String.valueOf(m1.sentNum));
	    			newMention.setAttribute("StanfordParaghNum", String.valueOf(m1.paragraph));

	    			// add to the set
	    			stfdMentions.add(newMention);
	    		} else {
	    			System.out.println("Stanford system removes singleton mention: " + m1.toString());
	    		}
	    	}

	    }
		
		return stfdMentions;
	}
	
	/////////////////////////////////////////////////////
	
	
	
}
