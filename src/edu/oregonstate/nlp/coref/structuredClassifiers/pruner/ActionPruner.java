package edu.oregonstate.nlp.coref.structuredClassifiers.pruner;

import edu.oregonstate.nlp.coref.structuredClassifiers.Action;
import edu.oregonstate.nlp.coref.structuredClassifiers.ActionList;
import edu.oregonstate.nlp.coref.structuredClassifiers.PartialOutputState;

public abstract class ActionPruner {
	
	// this function just labels actions with "Pruned" or "Non-Pruned"
	public abstract ActionList pruneActionsForState(PartialOutputState state, ActionList orignalActions);
	
	// this function do the same thing as above, in addition it removes all pruned actions
	public ActionList pruneActionForStateRemovePruned(PartialOutputState state,
			                                          ActionList orignalActions) {
		
		ActionList scoredAndLabeledActions = pruneActionsForState(state, orignalActions);
		
		ActionList remainingActions = new ActionList();
		for (Action act : scoredAndLabeledActions) {
			if (!act.isPruned()) {
				remainingActions.add(act);
			}
		}
		return remainingActions;
	}
    
}
