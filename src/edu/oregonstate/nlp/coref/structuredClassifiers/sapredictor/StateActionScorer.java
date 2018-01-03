package edu.oregonstate.nlp.coref.structuredClassifiers.sapredictor;

import edu.oregonstate.nlp.coref.structuredClassifiers.Action;
import edu.oregonstate.nlp.coref.structuredClassifiers.PartialOutputState;

public abstract class StateActionScorer {
	
	public abstract double scoringStateActionGivenFeatureVec(double[] featVec);
	
	public abstract double scoringStateAction(PartialOutputState state, Action act);
}
