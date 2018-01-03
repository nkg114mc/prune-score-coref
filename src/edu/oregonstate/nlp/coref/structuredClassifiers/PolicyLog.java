package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PolicyLog {
	
	public static class PolicyRankingSample {
		public int nSampls;
		public ArrayList<Action> candidateAction = new ArrayList<Action>();
		public HashMap<Action, Double> actionPredScores = new HashMap<Action, Double>();
		public HashMap<Action, Double> actionTrueScores = new HashMap<Action, Double>();
		public HashMap<Action, Double[]> actionFeatures = new HashMap<Action, Double[]>();
		public HashMap<Action, CorefChain> actionMergeInChain = new HashMap<Action, CorefChain>();
		
		public PolicyRankingSample() // clear all
		{
			nSampls = 0;
			candidateAction.clear();
			actionPredScores.clear();
			actionTrueScores.clear();
			actionFeatures.clear();
		}
	}

	public ActionList actions;
	public HashMap<Integer, CorefChain> chains;
	public double reward;
	
	public HashMap<Integer, HashSet<Integer>> mergeHistory = new HashMap<Integer, HashSet<Integer>>();
	public HashMap<Integer, CorefChain> bestMerge = new HashMap<Integer, CorefChain>();
	public HashMap<Integer, ArrayList<CorefChain>> nonbestMerges = new HashMap<Integer, ArrayList<CorefChain>>();
	
	public HashMap<Integer, Double> policyActionConfidence = new HashMap<Integer, Double>();
	
	public HashMap<Integer, Double> predictActionScore = new HashMap<Integer, Double>();
	
	// about actions
	public HashMap<Integer, Action> bestActions = new HashMap<Integer, Action>(); // best actions
	public HashMap<Integer, PolicyRankingSample> candidateChoices = new HashMap<Integer, PolicyRankingSample>();
	
	public int nMentions = 0;
	
	
	// about feature names
	ArrayList<String> featNames = new ArrayList<String>();
	HashMap<String, Double> featWeights = new HashMap<String, Double>();
	
	// construction
	public PolicyLog()
	{
		
	}
	
	
}