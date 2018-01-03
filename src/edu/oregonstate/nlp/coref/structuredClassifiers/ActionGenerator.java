package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.features.FeatureUtils.AnimacyEnum;
import edu.oregonstate.nlp.coref.features.FeatureUtils.GenderEnum;
import edu.oregonstate.nlp.coref.features.FeatureUtils.NumberEnum;
import edu.oregonstate.nlp.coref.features.properties.Animacy;
import edu.oregonstate.nlp.coref.features.properties.Gender;
import edu.oregonstate.nlp.coref.features.properties.Number;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.Constants;

public class ActionGenerator {

	public static ArrayList<Action> actionArraytoList(Action[] actarray)
	{
		ArrayList<Action> mylist = new ArrayList<Action>();
		for (Action act : actarray) {
			mylist.add(act);
		}
		return mylist;
	}

	//private boolean isPronounAction()
	//{
	//	return false;
	//}

	public ActionList genLegalActions(PartialOutputState parState) {

		HashMap<Integer,CorefChain> chainState = parState.getChains();

		ActionList sortedActions = new ActionList();

		int n_actions = 0;

		// gen all action
		if (chainState.size() <= 0) {
			throw new RuntimeException("State should contain at least one cluster!");
		}
		
		List<CorefChain> charList = parState.getOrderedChainList();
		CorefChain chain1 = null;
		CorefChain chain2 = null;
		boolean has_nonprocecssed = parState.hasUnprocessedCluster();
		
		// chain1 is unprocessed
		// chain2 is processed
		
		
		// all has been processed
		if (has_nonprocecssed == false) {
			return sortedActions; // no action can be done now ...
		}
		
		// find the left first unprocessed chain
		chain1 = parState.getLeftMostUnprocessedSingleton();


		// try to merge it into a processed chain
		for (int j = 0; j < charList.size(); j++) {
			chain2 = charList.get(j);
			//CorefChain c1,c2;
			if (chain2.getProcessed()) { // we need chain2.processed == true
				//if (chain2.before(chain1)) {
					// correct

					n_actions++;
					Annotation operatedMen = chain1.getFirstCe();

					int clust1 = chain1.getId();//Integer.parseInt(operatedMen.getAttribute(Constants.CLUSTER_ID));
					int clust2 = chain2.getId();//Integer.parseInt(chain2.getFirstCe().getAttribute(Constants.CLUSTER_ID));
					int opmid = Integer.parseInt(operatedMen.getAttribute(Constants.CE_ID));

					Action curAction = new Action(clust1, clust2, Action.ACT_MERGE, opmid);
					curAction.setActName(Action.ACT_MERGE); // this is a merge

					sortedActions.insertAction(curAction);
				//}
			}
		}

		// NOP Action
		n_actions++;
		int singleCl = chain1.getId();//Integer.parseInt(chain1.getFirstCe().getAttribute(Constants.CLUSTER_ID));
		int operatedMid = Integer.parseInt(chain1.getFirstCe().getAttribute(Constants.CE_ID));
		Action nopAction = new Action(singleCl, -1, Action.ACT_NOP, operatedMid); // this is an action of "do nothing"
		nopAction.setActName(Action.ACT_NOP);

		sortedActions.insertAction(nopAction); // add nop action into the quee

		//System.out.println(n_actions);

/*
		for (int k = 0; k < chainState.size(); k++) {
			chain1 = chainArray[k];
			if (chain1.getProcessed() == true) { // we need chain1.processed == false
				continue; 
			}

			//System.out.println("ActionGen processing mention "+chain1.getFirstCe().getAttribute(Constants.CE_ID)+" with chainID "+chain1.id);

			// try to merge it into a processed chain
			for (int j = 0; j < chainState.size(); j++) {
				chain2 = chainArray[j];
				//CorefChain c1,c2;
				if (chain2.getProcessed() == false) { // we need chain2.processed == true
					continue;
				}

				//c1=chain1;
				//c2=chain2;
				if (chain2.before(chain1)) {
					// correct
				} else {
					//entityToStr(chain1, doc, ces);
					//entityToStr(chain2, doc, ces);
					//System.out.println("chain2 should appear before chain1!");
					continue;
					//throw new RuntimeException("chain2 should appear before chain1!");
				}

				boolean canMerge = true;
				// gen all merge actions
				if (canMerge) {
					n_actions++;
					Annotation operatedMen = chain1.getFirstCe();

					int clust1 = Integer.parseInt(operatedMen.getAttribute(Constants.CLUSTER_ID));
					int clust2 = Integer.parseInt(chain2.getFirstCe().getAttribute(Constants.CLUSTER_ID));
					int opmid = Integer.parseInt(operatedMen.getAttribute(Constants.CE_ID));

					Action curAction = new Action(clust1, clust2, Action.ACT_MERGE, opmid);
					curAction.setActName(Action.ACT_MERGE); // this is a merge

					sortedActions.insertAction(curAction);
				}// if discrepancy set allow this merge
			}

				// NOP Action
				int singleCl = Integer.parseInt(chain1.getFirstCe().getAttribute(Constants.CLUSTER_ID));
				int operatedMid = Integer.parseInt(chain1.getFirstCe().getAttribute(Constants.CE_ID));
				Action nopAction = new Action(singleCl, -1, Action.ACT_NOP, operatedMid); // this is an action of "do nothing"
				nopAction.setActName(Action.ACT_NOP);

				sortedActions.insertAction(nopAction); // add nop action into the quee
	

			if (chain1.getProcessed() == false) {
				break;
			}

		}// for chain1
*/
		// calculate scores
		return sortedActions;
	}

	/*
	 	 public ActionList genLegalActions(PartialOutputState parState)
	 {

		 HashMap<Integer,CorefChain> chainState = parState.getChains();
		 //int ceId = 
		 //Document doc

		 double[] emptyLocalVec = new double[1];
		 double   emptyLocalWgt = Double.MIN_VALUE; 

		 ActionList sortedActions = new ActionList();

		 //System.out.println("=========Policy is working on: "+doc.getDocumentId()+" at step "+ceId+"=============");
		 int n_actions = 0;
		 int ACT_MERGE = 1;
		 int ACT_NOP = 0; 
		 boolean NO_NOP = false;

		 // gen all action
		 // action generation -------------------------------------------------------
		 if (chainState.size() <= 0) {
			 throw new RuntimeException("State should contain at least one cluster!");
		 }

		 // find the first unprocessed chain
		 int unprocessChains = 0;
		 int processChains   = 0;
		 CorefChain[] chainArray = getOrderedChainArr(chainState);
		 CorefChain chain1 = chainArray[0];
		 CorefChain chain2 = null;
		 boolean has_nonprocecssed = false;


		 for (int i = 0; i < chainState.size(); i++) {
			 chain1 = chainArray[i];
			 if (chain1.getProcessed() == false) {
				 unprocessChains++;
				 has_nonprocecssed = true;
				 //break;
			 } else {
				 processChains++;
			 }
		 }

		 // all has been processed
		 if (has_nonprocecssed == false) {
			 return sortedActions; // no action can be done now ...
		 }

		// doc.getAnnotationSet(Constants.GS_NP);

		 for (int k = 0; k < chainState.size(); k++) {
			 chain1 = chainArray[k];
			 if (chain1.getProcessed() == true) { // we need chain1.processed == false
				 continue; 
			 }

			 //System.out.println("ActionGen processing mention "+chain1.getFirstCe().getAttribute(Constants.CE_ID)+" with chainID "+chain1.id);

			 // try to merge it into a processed chain
			 for (int j = 0; j < chainState.size(); j++) {
				 chain2 = chainArray[j];
				 //CorefChain c1,c2;
				 if (chain2.getProcessed() == false) { // we need chain2.processed == true
					 continue;
				 }

				 //c1=chain1;
				 //c2=chain2;
				 if (chain2.before(chain1)) {
					 // correct
				 } else {
					 //entityToStr(chain1, doc, ces);
					 //entityToStr(chain2, doc, ces);
					 //System.out.println("chain2 should appear before chain1!");
					 continue;
					 //throw new RuntimeException("chain2 should appear before chain1!");
				 }

				 boolean canMerge = true;
				 // gen all merge actions
				 if (canMerge) {
					 n_actions++;
					 Annotation operatedMen = chain1.getFirstCe();

					 int clust1 = Integer.parseInt(operatedMen.getAttribute(Constants.CLUSTER_ID));
					 int clust2 = Integer.parseInt(chain2.getFirstCe().getAttribute(Constants.CLUSTER_ID));
					 int opmid = Integer.parseInt(operatedMen.getAttribute(Constants.CE_ID));

					 Action curAction = new Action(clust1, clust2,  ACT_MERGE, opmid);
					 curAction.setActName(ACT_MERGE); // this is a merge

					 sortedActions.insertAction(curAction);
				 }// if discrepancy set allow this merge
			 }

			 if (!NO_NOP) {
				 // NOP Action
				 int singleCl = Integer.parseInt(chain1.getFirstCe().getAttribute(Constants.CLUSTER_ID));
				 int operatedMid = Integer.parseInt(chain1.getFirstCe().getAttribute(Constants.CE_ID));
				 Action nopAction = new Action(singleCl, -1, ACT_NOP, operatedMid); // this is an action of "do nothing"
				 nopAction.setActName(ACT_NOP);

				 sortedActions.insertAction(nopAction); // add nop action into the quee
			 }

			 if (chain1.getProcessed() == false) {
				 break;
			 }

		 }// for chain1

		 // calculate scores
		 return sortedActions;
	 } 
	 */

}
