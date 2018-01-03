package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.properties.SentNum;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.RuleResolvers;
import edu.oregonstate.nlp.coref.scorers.Matcher;
import edu.oregonstate.nlp.coref.scorers.Scorer;
import edu.oregonstate.nlp.coref.util.DocArray2DocIterable;

public class PartialOutputState {

	private Document reconDoc;
	private HashMap<Integer,CorefChain> chainState;
	private HashMap<Annotation, ArrayList<Annotation>> posessives;

	private AnnotationSet predictMentions;
	private AnnotationSet goldMentions;


	public PartialOutputState(Document recDoc) {

		reconDoc  = recDoc;

		chainState = new HashMap<Integer, CorefChain>();
		posessives = new HashMap<Annotation, ArrayList<Annotation>>();

		predictMentions = null;
		goldMentions = null;

		// extract information from doc
		initFromDoc(reconDoc);

		// initialize clustering
		initChains();
	}

	public void initFromDoc(Document recDoc) {
		reconDoc = recDoc;

		predictMentions = reconDoc.getPredictMentionSet();
		goldMentions = reconDoc.getGoldMentionSet();

		if (predictMentions == null) {
			throw new RuntimeException("There are not predict mentions in the document!");
		}

		RuleResolvers.ruleResolvePronouns(chainState, reconDoc);
		RuleResolvers.addAllPossesives(predictMentions, reconDoc, posessives);
	}

	public Document getDoc() {
		return reconDoc;
	}

	public HashMap<Integer,CorefChain> getChains() {
		return chainState;
	}

	public boolean isTerminalState() {
		boolean hasRemaining = hasUnprocessedCluster();
		if (!hasRemaining) {
			return true;
		}
		return false;
	}

	private void initChains() {

		System.out.println("Pred NP Set number = " + predictMentions.size());
		if (goldMentions != null) {
			System.out.println("Gold NP Set number = " + goldMentions.size());
		}

		for (Annotation ce: predictMentions.getOrderedAnnots()) {
			int curId = Integer.parseInt(ce.getAttribute(Constants.CE_ID));
			ce.setAttribute(Constants.CLUSTER_ID, String.valueOf(curId + 1000)); // singleton cluster

			// initialize clusters to all singleton
			CorefChain cur = new CorefChain(curId, ce, reconDoc);
			cur.setProcessed(false);
			chainState.put(curId,cur);
		}
		
		// Match predict with gold
		matchedPredGoldMent();
	}
	
	public void matchedPredGoldMent() {
		
		if (goldMentions != null) {
			for (Annotation ce: predictMentions.getOrderedAnnots()) {
				int ceId = Integer.parseInt(ce.getAttribute(Constants.CE_ID));

				int matchedGoldClustId = -1 * (ceId + 10000); // negative singleton for unmatched mention
				
				// try to mach
				Annotation matchGoldMent = Matcher.newMatchAnnotationOntoStyle(ce, goldMentions, reconDoc);
				if (matchGoldMent != null) {
					matchedGoldClustId = Integer.parseInt(matchGoldMent.getAttribute(Constants.CLUSTER_ID));
				}
				
				ce.setAttribute(Constants.MATCHED_GS_NP, String.valueOf(matchedGoldClustId)); 
			}
		}
		
	}

	public CorefChain[] getOrderedChainArr() {
		
		// build list
		List<CorefChain> chainList = getOrderedChainList();

		// cast to array and return
		CorefChain[] chainArr = chainList.toArray(new CorefChain[1]);

		return chainArr;
	}
	
	public List<CorefChain> getOrderedChainList() {
		
		// build list
		List<CorefChain> chainList = new ArrayList<CorefChain>();
		for (CorefChain chain : chainState.values()) {
			chainList.add(chain);
		}
		// sort
		Collections.sort(chainList, new ChainComparator());

		return chainList;
	}

	public CorefChain getLeftMostUnprocessedSingleton() {
		
		//CorefChain[] chainArray = getOrderedChainArr(chainState);
		List<CorefChain> chainList = getOrderedChainList();

		for (int i = 0; i < chainList.size(); i++) {
			CorefChain chain1 = chainList.get(i);
			if (chain1.getProcessed() == false) {
				return chain1;
			}
		}
		
		return null; // no unprocessed chain
	}
	
	public List<CorefChain> getAllProcessedClusters() {
		List<CorefChain> processedChainList = new ArrayList<CorefChain>();
		List<CorefChain> chainList = getOrderedChainList();
		for (int i = 0; i < chainList.size(); i++) {
			CorefChain chain1 = chainList.get(i);
			if (chain1.getProcessed()) {
				processedChainList.add(chain1);
			}
		}
		return processedChainList;
	}
	
	public List<Annotation> getAllProcessedMentions() {
		List<Annotation> processedMentList = new ArrayList<Annotation>();
		List<CorefChain> chainList = getOrderedChainList();
		for (int i = 0; i < chainList.size(); i++) {
			CorefChain chain1 = chainList.get(i);
			if (chain1.getProcessed()) {
				for (Annotation np : chain1.getCes()) {
					processedMentList.add(np);
				}
			}
		}
		return processedMentList;
	}

	// return true if there is an unprocessed cluster
	public boolean hasUnprocessedCluster()
	{
		boolean result = false;
		for (CorefChain chain : chainState.values()) {
			if (chain.getProcessed() == false) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public int getUnprocessedCount() {
		int cnt = 0;
		for (CorefChain chain : chainState.values()) {
			if (chain.getProcessed() == false) {
				cnt++;
			}
		}
		return cnt;
	}

	public int getNumClusters(AnnotationSet ces) {
		HashSet<String> clusters = new HashSet<String>();
		for (Annotation c : ces) {
			clusters.add(c.getAttribute(Constants.CLUSTER_ID));
		}
		return clusters.size(); 
	}

	
	
	
	//// Other methods that are supposed not to be in the class ...

	public static PartialOutputState executeActionNoCopy(PartialOutputState state, Action act) {
		state.performMerge(act);
		return state;
	}

	public void performMerge(Action act) {

		HashMap<Integer, CorefChain> chains = chainState;

		if (act.getActName() == Action.ACT_MERGE) { // if this is a Merge action
			// c1: to be processed
			// want to merge c1 into c2
			CorefChain c1 = chains.get(act.first);
			CorefChain c2 = chains.get(act.second);
			if (c1 == null || c2 == null) {
				System.out.println("One of the cluster is null!");
			}
			if ((c2.getProcessed() == true) && (c1.getProcessed() == false)) {
				// Ok, do nothing
			} else if (c2.getProcessed() == false && c1.getProcessed() == true) {
				// error!
				throw new RuntimeException("The operating cluster is null! "+act);
			} else {
				// error!
				throw new RuntimeException("Both clusters were porcessed or were non-processed while performing "+act);
			}
			CorefChain newC = c2.join(c1);
			chains.remove(act.first);
			chains.remove(act.second);
			newC.setProcessed(true);
			chains.put(newC.getId(), newC);

		} else if (act.getActName() == Action.ACT_NOP) {
			// just do nothing
			CorefChain newC = chains.get(act.first);
			newC.setProcessed(true);
			chains.remove(act.first);
			chains.put(newC.getId(), newC);
		}

	}

	public void writeBackClusteringResult() {
		
	}
	
	public void printStatus() {
		int leftUnprocessClId = -1;
		CorefChain leftch = getLeftMostUnprocessedSingleton();
		int processedCnt =  getUnprocessedCount();
		//int clustCnt = getNumClusters(AnnotationSet ces);
		
		System.out.println("LeftMostClusterId = " + leftch);
		System.out.println("UnprocessedCnt = " + processedCnt);
		System.out.println("ClusterCnt = " + chainState.size());

	}



	// Static methods
	///////////////////////////////////////////////////////////////
	
	public static double scoringPartialOutputWithEdgeCount(PartialOutputState state, Action action) {
		
		CorefChain c1 = state.chainState.get(action.first);
		Annotation operMention = c1.getFirstCe();
		int myGoldCl = Integer.parseInt(operMention.getAttribute(Constants.MATCHED_GS_NP));
		
		double trueScore = 0;
		if (action.actName != Action.ACT_NOP) { // is link
			
			double goodEdge = 0;
			double totalEdge = 0;
			CorefChain c2 = state.chainState.get(action.second);
			for (Annotation c2np : c2.getCes()) {
				int c2GoldCl = Integer.parseInt(c2np.getAttribute(Constants.MATCHED_GS_NP));
						if (c2GoldCl == myGoldCl) {
							goodEdge++;
						}
				totalEdge++;
			}
			
			trueScore = goodEdge / totalEdge;
			
			//String c2clustId = c2.getFirstCe().getAttribute(Constants.CLUSTER_ID);
			//operMention.setAttribute(Constants.CLUSTER_ID, c2clustId); // singleton cluster
			
		} else { // is NOP
			
			boolean hasLeftAntecedent = false;
			List<Annotation> leftMents = state.getAllProcessedMentions();
			for (Annotation leftnp : leftMents) {
				int leftGoldCl = Integer.parseInt(leftnp.getAttribute(Constants.MATCHED_GS_NP));
				if (leftGoldCl == myGoldCl) {
					hasLeftAntecedent = true;
				}
			}
			
			if (hasLeftAntecedent) {
				trueScore = 0;
			} else {
				trueScore = 2.0;
			}
		}


		return trueScore;
	}

	public static double scoringPartialOutputWithScorer(PartialOutputState state, Action action, Scorer scer) {
		
		CorefChain c1 = state.chainState.get(action.first);
		Annotation operMention = c1.getFirstCe();
		int oldClusterId = Integer.parseInt(c1.getFirstCe().getAttribute(Constants.CLUSTER_ID));

		if (action.actName != Action.ACT_NOP) {
			CorefChain c2 = state.chainState.get(action.second);
			String c2clustId = c2.getFirstCe().getAttribute(Constants.CLUSTER_ID);
			operMention.setAttribute(Constants.CLUSTER_ID, c2clustId); // singleton cluster
		}

		///////////////////////////////////////////////////////////////
		ArrayList<Document> docL = new ArrayList<Document>();
		docL.add(state.getDoc());
		double[] score = scer.score(false, new DocArray2DocIterable(docL));
		//System.out.println(score[0] + " " + score[1]);
		///////////////////////////////////////////////////////////////

		if (action.actName != Action.ACT_NOP) {
			operMention.setAttribute(Constants.CLUSTER_ID, String.valueOf(oldClusterId)); // singleton cluster
		}

		return score[Scorer.F];
	}

	public static double scoringCompleteOutputWithScorer(PartialOutputState state, Scorer scer) {
		assert (state.getDoc().getPredictMentionSet() != null);
		ArrayList<Document> docL = new ArrayList<Document>();
		docL.add(state.getDoc());
		double[] score = scer.score(false, new DocArray2DocIterable(docL));
		//System.out.println(score[0] + " " + score[1]);
		return score[Scorer.F];
	}
	
	// coreference chain comparator
	public static class ChainComparator implements Comparator<CorefChain> {
		@Override
		public int compare(CorefChain chain1, CorefChain chain2) {
			int result = chain1.compareTo(chain2);
			return result;
		}
	}

	// Modified by Chao Ma (6-23-2013)
	public boolean includePairChao(Annotation np1, Annotation np2, Document doc, Map<Annotation,ArrayList<Annotation>> posessives){
		RuleResolvers.NPType type2 = RuleResolvers.getNPtype(np2, doc, posessives);
		// int par2 = ParNum.getValue(np2, doc);
		int sen2 = SentNum.getValue(np2, doc);
		//int par2 = ParNum.getValue(np2, doc);
		boolean pn2 = type2.equals(RuleResolvers.NPType.PROPER_NAME);
		boolean pron2 = type2.equals(RuleResolvers.NPType.PRONOUN);
		RuleResolvers.NPType type1 = RuleResolvers.getNPtype(np1, doc, posessives);
		// int par1 = ParNum.getValue(np1, doc);
		// int parNum = Math.abs(par1 - par2);
		int sen1 = SentNum.getValue(np1, doc);
		Math.abs(sen1 - sen2);
		//int parNum = Math.abs(par1 - par2);
		boolean pron1 = type1.equals(RuleResolvers.NPType.PRONOUN);
		boolean pn1 = type1.equals(RuleResolvers.NPType.PROPER_NAME);
		boolean includePair = false;

		/*

			if (pn1 && pn2 && senNum <= maxDistance) {
				if(senNum<=3)
					includePair = true;
				String[] words1 = Words.getValue(np1, doc);
				String[] words2 = Words.getValue(np2, doc);
				if (FeatureUtils.overlaps(words1, words2))
					includePair = true;
			}
			else if (person2 && specPronoun1&& senNum <= maxDistance) {
				includePair = true;
			}
			else if (specPronoun1 && (specPronoun2 || person2)&& senNum <= maxDistance) {
				includePair = true;
			}
			else if (specPronoun2 && (specPronoun1 || person1)&& senNum <= maxDistance) {
				includePair = true;
			}
			else if (def2 && !pron1 && (senNum <= 5)) {
				includePair = true;
			}else if (pron2){
				if(senNum<=3)
					includePair = true;
			}else if (senNum <= 3) {
				includePair = true;
			}*/
		return includePair;
	}

}
