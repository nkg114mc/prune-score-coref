/*
 * @author Chao Ma
 * @date 2013-1-29
 */

package edu.oregonstate.nlp.coref.scorers;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.google.common.collect.Lists;

/*
 * Zero-One loss in the whole graph.
 */
public class ZeroOnePairScore
    extends InternalScorer {

/**
 * Calculates the zero-one pair score for the given range of documents. Each mention pair of key set or
 * response set has a value: "corefed" or "non-corefed". This score is used to calculate the percetage that
 * of correctly value assigned pairs over the total pairs in key set. If one the mention in the pair that
 * belongs to the response set does not exist in the key set, then this pair is counted as an incorrect pair.
 * <i>Algorithms of Calculating the Zero-One Loss Score for Coreference Chains </i> by Chao Ma.
 * 
 * @param first
 *          The first document to compute loss.
 * @param last
 *          The last document to compute loss.
 * @param response
 *          The coreference chains in the response. Should be organized by document such that response contains a
 *          mapping from document ID (as a java.lang.Long) to a {@link LeanDocument}.
 * @param key
 *          The gold standard for the scoring. Use the same organization as response.
 * @param pairs
 *          A map from NP's in the key to the corresponding NP in the response. An unmatched NP n should satisfy
 *          pairs.get(n) == null.
 * @return The zero-one loss for the response.
 */
public static double ZeroOneLossScore(LeanDocument key, LeanDocument response)
{
  if (DEBUG) {
    System.err.println("Zero-One Loss of document " + key.getID());
  }

  double correctPairs = 0;
  double totalPairs = 0;
  double nPairs = 0;
  
  int match1, match2, np1, np2;
  int resCID1, resCID2, keyCID1, keyCID2;
  int true_neg, true_pos;
  int responsePair, keyPair;
  
  Integer[] npsID = response.getNPArray();
  int numMen = npsID.length;
  true_neg = 0;
  true_pos = 0;
  /*
  for (int i = 0; i < numMen; i++) {
	int c1, c2, n1, n2;
    n1 = npsID[i];
	c1 = response.getClusterNum(n1);
	n2 = response.getMatch(n1);
	c2 = key.getClusterNum(n2);
	System.out.print(n1 + "(" + c1 + ")" + " ");
  }	System.out.println();
  */
  /*
  for (int i = 0; i < numMen; i++) {
	int c1, c2, n1, n2;
    n1 = npsID[i];
	n2 = response.getMatch(n1);
	c2 = key.getClusterNum(n2);
	System.out.print(n2 + "(" + c2 + ")" + " ");
  }	System.out.println();
  */
  /*
  Integer[] npsID2 = key.getNPArray();
  for (int i = 0; i < numMen; i++) {
	int c1, c2, n1, n2;
    n2 = npsID2[i];
    c2 = key.getClusterNum(n2);
	n1 = key.getMatch(n2);
	System.out.print(n2 + "(" + c2 + ")" + " ");
  }	System.out.println();  
  */
  // for all pairs in response set
  for (int i = 0; i < numMen; i++) {
	 for (int j = 0; j < numMen; j++) {
	    np1 = npsID[i];
	    np2 = npsID[j]; 

	    if (np1 != np2) {
	      if (response.isMatched(np1) && response.isMatched(np2)) {
	    	match1 = response.getMatch(np1);
	    	match2 = response.getMatch(np2);
	    	if (match1 == match2) {
	    		throw new RuntimeException("match bu ying gai xiang deng!");
	    	}
	    	// response pair value
	    	resCID1 = response.getClusterNum(np1);
	    	resCID2 = response.getClusterNum(np2);
	    	// key pair value
	    	keyCID1 = key.getClusterNum(match1);
	    	keyCID2 = key.getClusterNum(match2);

	    	// check whether these two pairs are equivalent
	    	responsePair = 0;
	    	if (resCID1 == resCID2) {
	    		responsePair = 1;
	    	}
	    	keyPair = 0;
	    	if (keyCID1 == keyCID2) {
	    		keyPair = 1;
	    	}
	    	
	    	if (responsePair == keyPair) {
	    	  // add the count of correct pairs
	    	  if (keyPair == 1) {
	    		  true_pos++;
	    	  } else if (keyPair == 0) {
	    		  true_neg++;
	    	  }
	    	  correctPairs++;
	    	  nPairs++;
	    	} else {
	    	  nPairs++;
	    	}
	      } else { 
	    	// if one of them does not exist in key set, then count this pair as incorrect
	    	// System.out.println("You bu yi yang de!");
	    	// throw new RuntimeException("you bu yi yang!");
	      }
	      // a count for debug
	      totalPairs++;
	    }
	 }
  }
  
  //if (DEBUG) {
	 System.out.println("Zero-One Loss rate " + correctPairs + "/" + totalPairs);
	 //System.out.println("Poss / Neg " + correctPairs + "/" + nPairs);
	 System.out.println("True pos " + true_pos + ", true neg " + true_neg);
  //}
/*
  Iterator<TreeMap<Integer, Integer>> goldChains = key.chainIterator();

  while (goldChains.hasNext()) {
    TreeMap<Integer, Integer> keyChain = goldChains.next();
    Iterator<Integer> nouns = keyChain.keySet().iterator();
    while (nouns.hasNext()) {
      Integer entity = nouns.next();
      Integer twin = key.getMatch(entity);
      int numIntersect = 1;
      if (twin != null) {
        // Get the chain in the response for twin.
        TreeMap<Integer, Integer> responseChain = response.getChain(response.getClusterNum(twin));
        if (responseChain == null) {
          System.out.println("Key:\n" + key + "\n*************************\nResponse:\n" + response);
          throw new RuntimeException("null response for " + entity + " twin " + twin);
        }
        // Get the intersection of the key's chain and
        // the response's chain.
        // TreeMap correct = intersect(keyChain, responseChain);
        numIntersect = numIntersect(key, keyChain, response, responseChain);
        if (numIntersect == 0) throw new RuntimeException("NumIntersect=0");
        // Calculate the recall with respect to the entity.
        // double recall = (double)correct.size() / (double)keyChain.size();
      }
      double recall = numIntersect / (double) keyChain.size();
      // Add to the total recall so that each entity is given
      // equal weight.

      // System.err.println(numIntersect+"/"+keyChain.size()+"="+recall+"("+totalRecall+")");
      totalRecall += recall;

    }
  }
*/
  
  return correctPairs; 
}

@Override
public double[][] scoreRaw(DocumentPair doc, boolean printIndividualFiles)
{
  double[][] result = newRawScoreArray();
  
  double correctResPairs = ZeroOneLossScore(doc.getResponse(), doc.getKey());
  double mentionNumRes   = doc.getResponse().numNounPhrases();
  double totalPairsRes   = mentionNumRes * (mentionNumRes - 1);
  
  result[0][PRECISION] = correctResPairs;
  result[1][PRECISION] = totalPairsRes;

  double correctKeyPairs = ZeroOneLossScore(doc.getKey(), doc.getResponse());
  double mentionNumKey   = doc.getKey().numNounPhrases();
  double totalPairsKey   = mentionNumKey * (mentionNumKey - 1);

  result[0][RECALL] = correctKeyPairs;
  result[1][RECALL] = totalPairsKey;
  
  // If you only care about the recall, then left the two lines below,
  // Otherwise, comment them (by Chao Ma, 2013-1-29)------------------
  //result[0][PRECISION] = correctKeyPairs;
  //result[1][PRECISION] = totalPairsKey;
  // -----------------------------------------------------------------
  
  // the number of correct pairs should be the same 
  if (correctResPairs != correctKeyPairs) {
    throw new RuntimeException("correctResPairs "+correctResPairs+", correctKeyPairs "+correctKeyPairs);
  }
  //if (DEBUG) {
	 System.out.println("Zero-One Precision Loss rate " + correctKeyPairs + "/" + totalPairsKey);
	 System.out.println("Zero-One Recall    Loss rate " + correctResPairs + "/" + totalPairsRes);
  //}
  return result;
}

@Override
public double[] score(Iterable<DocumentPair> docs, boolean printIndividualFiles)
{
  return (new ZeroOnePairScore()).microAverage(docs, printIndividualFiles);
}

@Override
public double[] score(DocumentPair doc, boolean printIndividualFiles)
{
  List<DocumentPair> docs = Lists.newArrayList(doc);
  return (new ZeroOnePairScore()).score(docs, printIndividualFiles);
}
}
