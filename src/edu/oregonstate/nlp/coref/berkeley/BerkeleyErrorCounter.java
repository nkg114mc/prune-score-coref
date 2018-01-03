package edu.oregonstate.nlp.coref.berkeley;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.structuredClassifiers.Action;

/**
 * Count the number of three type errors for a policy action sequence
 * Three type of errors:
 *   FA: False Anaphor
 *   FN: False New
 *   WL: Wrong Link
 *   
 * @author machao
 *
 */
public class BerkeleyErrorCounter {

	// action type
	private final int ACT_MERGE = 1;
	private final int ACT_NOP = 0; 
	
	// mention gold-category
	public final static int GOLDCATE_SINGETON_INDEX = 0;
	public final static int GOLDCATE_NEW_ENTITY_INDEX = 1;
	public final static int GOLDCATE_ANAPHORA_INDEX = 2;
	public final static int GOLDCATE_Unknown_INDEX = -1;
	
	public enum MentionGoldCategory {
		SINGETON, NEW_ENTITY, ANAPHORA;
	}
	
	private int FAcount;
	private int FNcount;
	private int WLcount;
	private int correct;
	
	private int correctNOOP;
	private int correctMERGE;

	public BerkeleyErrorCounter() {
		clear();
	}
	
	public  void clear() {
		FAcount = 0;
		FNcount = 0;
		WLcount = 0;
		correct = 0;
		
		correctNOOP = 0;
		correctMERGE = 0;
	}

	
	public void addFA() {
		FAcount++;
	}
	public void addFN() {
		FNcount++;
	}
	public void addWL() {
		WLcount++;
	}
	public void addCorret() {
		correct++;
	}
	
	public int getFA() {
		return FAcount;
	}
	public int getFN() {
		return FNcount;
	}
	public int getWL() {
		return WLcount;
	}
	public int getCorrect() {
		return correct;
	}
	
	public void printALLError() {
		int total_steps = FAcount + FNcount + WLcount + correct;
		System.out.println("////////// ERROR COUNT BEGIN /////////////");
		System.out.println("   FA  = " + FAcount);
		System.out.println("   FN  = " + FNcount);
		System.out.println("   WL  = " + WLcount);
		System.out.println(" corct = " + correct + (" NOOP = " + correctNOOP + " + MERGE = " + correctMERGE));
		System.out.println(" total = " + total_steps);
		System.out.println("//////////// ERROR COUNT END /////////////");
	}
	
	public void checkError(Annotation currentMention, Action trueBestAct, Action ourBestAction) {
		
		// 1) is it FN?
		if (trueBestAct.getActName() == ACT_MERGE &&
			ourBestAction.getActName() == ACT_NOP) {
			addFN(); // 
			return;
		}
		
		// 2) is it FA?
		if (trueBestAct.getActName() == ACT_NOP &&
			ourBestAction.getActName() == ACT_MERGE) {
			addFA(); // 
			return;
		}
		
		// 3) is it WL?
		if (trueBestAct.getActName() == ACT_MERGE &&
			ourBestAction.getActName() == ACT_MERGE) {
			if (!trueBestAct.isSameAction(ourBestAction)) {
				addWL();
				return;
			} else {
				correctMERGE++;
			}
		}
		
		// 4) correct
		if (trueBestAct.getActName() == ACT_NOP &&
			ourBestAction.getActName() == ACT_NOP) {
			correctNOOP++;
		}
		addCorret();
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////  Mention gold category  ////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public static class AnnotationComparator implements Comparator<Annotation> {
		@Override
		public int compare(Annotation np1, Annotation np2) {
			return np1.compareSpan(np2);
		}
	}
	
	public static ArrayList<Annotation> orderAnnotationsByTexAppearence(AnnotationSet nps) {
		ArrayList<Annotation> npList = new  ArrayList<Annotation>();
		for (Annotation np : nps) {
			npList.add(np);
		}
		Collections.sort(npList, new AnnotationComparator());
		return npList;
	}
	
	// this method will classify the predicted mention into three types according the gold result:
	// 1. Singletons
	// 2. NewEntity
	// 3. Anophora
	public static void MentionCategoryWrtGold(Document doc) {
		
		AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
		AnnotationSet gnps = doc.getAnnotationSet(Constants.GS_NP);		
		
		// sort mentions wrt their text ordering
		ArrayList<Annotation> mentionList = orderAnnotationsByTexAppearence(nps);
		
		HashSet<Integer> allAppearedClusterIDs = new HashSet<Integer>();
		
		for (int i = 0; i < mentionList.size(); i++) {
			Annotation currentMention = mentionList.get(i);
		
			// gold cluster ID
			int gClusterID = -1; // unmatched
			if (currentMention.getProperty(Property.MATCHED_CE) == null) {
				gClusterID = -1; 
			} else if (((Integer)currentMention.getProperty(Property.MATCHED_CE)) < 0) {
				gClusterID = -1; 
			} else {
				// this is a matched mention
				int matchid = ((Integer)currentMention.getProperty(Property.MATCHED_CE)).intValue();
				Annotation matchGoldMen = gnps.getAnnotationByNO(matchid);
				assert(matchGoldMen != null);
				if (matchGoldMen == null) {
					gClusterID = -1; 
				} else {
					gClusterID = Integer.parseInt(matchGoldMen.getAttribute(Constants.CLUSTER_ID));
					//System.out.println(matchGoldMen.getSpanString(doc));  // (Integer)currentMention.getProperty(Property.MATCHED_CE)
				}
			}			
			
			// mention gold category
			String mcategory = "Unknown";
			if (gClusterID >= 0) { // no singleton
				if (allAppearedClusterIDs.contains(gClusterID)) {
					mcategory = MentionGoldCategory.ANAPHORA.toString();
				} else {
					mcategory = MentionGoldCategory.NEW_ENTITY.toString();
					allAppearedClusterIDs.add(gClusterID);
				}
			} else {
				mcategory = MentionGoldCategory.SINGETON.toString();
			}	
			
			// debug
			//System.out.println(currentMention.getStartOffset() + " " + currentMention.getEndOffset() + " " + mcategory);
			
			// metion gold category
			currentMention.setAttribute(Constants.GOLD_CATEGORY, mcategory);
		}
		
		// 
		
	}
	
	public static void MentionCategoryWrtGoldAll(Iterable<Document> docs) {
		for (Document doc : docs) {
			MentionCategoryWrtGold(doc);
		}
	}
	
	public static int MentionGoldCategoryIndex(String categoryName) {
		if (categoryName.equals(MentionGoldCategory.SINGETON.toString())) return GOLDCATE_SINGETON_INDEX;
		if (categoryName.equals(MentionGoldCategory.NEW_ENTITY.toString())) return GOLDCATE_NEW_ENTITY_INDEX; 
		if (categoryName.equals(MentionGoldCategory.ANAPHORA.toString())) return GOLDCATE_ANAPHORA_INDEX; 
		return GOLDCATE_Unknown_INDEX;
	}
	
	// take the mention as input, and return a index to indicate the index value of "gold-mention-category"
	public static int getMentionGoldCategoryIndex(Annotation np) {
		String mcategory = np.getAttribute(Constants.GOLD_CATEGORY);
		if (mcategory == null) {
			return -1;
		}
		int valueIndex = MentionGoldCategoryIndex(mcategory);
		return valueIndex;
	}
}
