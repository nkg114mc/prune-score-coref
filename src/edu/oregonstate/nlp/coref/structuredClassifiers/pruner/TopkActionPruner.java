package edu.oregonstate.nlp.coref.structuredClassifiers.pruner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.oregonstate.nlp.coref.structuredClassifiers.Action;
import edu.oregonstate.nlp.coref.structuredClassifiers.ActionList;
import edu.oregonstate.nlp.coref.structuredClassifiers.PartialOutputState;
import edu.oregonstate.nlp.coref.structuredClassifiers.StateActionFeaturizer;
import edu.oregonstate.nlp.coref.structuredClassifiers.sapredictor.StateActionScorer;

public class TopkActionPruner extends ActionPruner {
	
	private int topK;
	private StateActionFeaturizer featurizer;
	private StateActionScorer scoringModel;
	
	public TopkActionPruner(int k, StateActionScorer mdl) {
		topK = k;
		scoringModel = mdl;
		featurizer = new StateActionFeaturizer();
	}
	
	public TopkActionPruner(int k, StateActionScorer mdl, StateActionFeaturizer frizr) {
		topK = k;
		scoringModel = mdl;
		featurizer = frizr;
	}
	
	public ActionList pruneActionsForState(PartialOutputState state, ActionList orignalActions) {
		if (featurizer == null) {
			throw new RuntimeException("No featurizer created!");
		}
		
		labeledTopk(state, orignalActions);
		
		return orignalActions;
	}
	
	
	public void labeledTopk(PartialOutputState state, ActionList orignalActions) {
		
		// scoring actions
		
		ArrayList<TempActionWrap> alist = new ArrayList<TempActionWrap>();
		for (Action act : orignalActions) {
			TempActionWrap tmpAct = new TempActionWrap();
			tmpAct.action = act;
			tmpAct.score = scoringModel.scoringStateAction(state, act);
			alist.add(tmpAct);
		}
		
		// sort from large to small
		
		
		// pick top-k
		for (TempActionWrap tmpAct : alist) { // all action is assumed pruned
			tmpAct.action.setPruned(true);
		}
		
		int nkept = topK;
		if (alist.size() < topK) {
			nkept = alist.size();
		}
		for (int i = 0; i < nkept; i++) { // keep top-k actions
			TempActionWrap tmpa = alist.get(i);
			tmpa.action.setPruned(false);
		}
	}
	
	public static class ActionPruningScoreComparator implements Comparator<TempActionWrap> {
		public int compare(TempActionWrap a1, TempActionWrap a2) {
			if ((a1.score - a2.score) >= 0) return -1;
			return 1;
		}
	}
	
	public static void sortScoredActions(ArrayList<TempActionWrap> tactionsList) {
		Collections.sort(tactionsList, new ActionPruningScoreComparator());
	}
	

	
}
