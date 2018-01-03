/*
 * @author ves
 */

package edu.oregonstate.nlp.coref.scorers;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.google.common.collect.Lists;

/*
 * BCubed Metric for measuring noun-phrase coreference resolution.
 */
public class BCubedConllv7
    extends InternalScorer {

/**
 * A different implementation of the B-cubed score. This implemenation scores all entities in the key, but out of the
 * response, it scores only those entities that have "twins" in the key.
 * 
 * 
 * Calculates the B-cubed score for the given range of documents. This is a recall score for the response, compared to
 * the key. To calculate precision simply pass the response as the key, and vice versa. For more information see
 * <i>Algorithms for Scoring Coreference Chains </i> by Bagga and Baldwin.
 * 
 * @return The b-cubed score for the response.
 */
public static double[] bCubedScore(LeanDocument key, LeanDocument response, boolean isRecall)
{
  if (DEBUG) {
    System.err.println("Scoring document " + key.getID());
  }

  double totalRecall = 0;
  double totalPrecision = 0;
  // Calculate the total recall, treating key as the gold standard.
  // The error is calculated with respect to each NP entity; since this
  // is for single document coreference, there is no need to check pairs
  // of entities that do not come from the same document (wrt ID).
  // The basic idea is to iterate over the equivalence classes (chains) in
  // a gold standard document and for each one, see how well the
  // response's document has kept elements of the equivalence class together.

  double predictMenSize = 0;
  
  Iterator<TreeMap<Integer, Integer>> goldChains = key.chainIterator();
  Iterator<TreeMap<Integer, Integer>> predChains = response.chainIterator();

  while (predChains.hasNext()) {
    TreeMap<Integer, Integer> responseChain = predChains.next();
    Iterator<Integer> nouns = responseChain.keySet().iterator();
    while (nouns.hasNext()) {
      Integer men = nouns.next();
      Integer twin = response.getMatch(men);
      //int numIntersect = 0;//1;
      if (twin != null) {
        // Get the chain in the response for twin.
        TreeMap<Integer, Integer> keyChain = key.getChain(key.getClusterNum(twin));
        if (keyChain == null) {
          //System.out.println("Key:\n" + key + "\n*************************\nResponse:\n" + response);
          //throw new RuntimeException("null key for " + men + " twin " + twin);
          keyChain = new TreeMap<Integer, Integer>();
          keyChain.clear();
        }
        // Get the intersection of the key's chain and
        // the response's chain.
        // TreeMap correct = intersect(keyChain, responseChain);
        double numIntersect = (double)numIntersect(response, responseChain, key, keyChain);
        //if (numIntersect == 0) throw new RuntimeException("NumIntersect=0");
        // Calculate the recall with respect to the entity.
        // double recall = (double)correct.size() / (double)keyChain.size();
        
        double recall = numIntersect / (double) keyChain.size();
        double precision = numIntersect / (double) responseChain.size();
        // Add to the total recall so that each entity is given
        // equal weight.
         // System.err.println(numIntersect+"/"+keyChain.size()+"="+recall+"("+totalRecall+")");
        totalRecall += recall;
        totalPrecision += precision;
        //numNPs += 1;
      } else {
        totalRecall += 0;
        totalPrecision += 0;
        //numNPs += 1;
      }
    }
    
    predictMenSize += ((double)(responseChain.keySet().size()));
  }

  double numerator = totalPrecision;
  double numNPs = predictMenSize;//response.numNounPhrases();
  if (isRecall) {
	  numerator = totalRecall;
	  numNPs = key.numNounPhrases();
  }
  double[] result = new double[] { numerator, numNPs };

  return result;
}

/**
 * Calculates the B-cubed score for the given range of documents. This is a recall score for the response, compared to
 * the key. To calculate precision simply pass the response as the key, and vice versa. For more information see
 * <i>Algorithms for Scoring Coreference Chains </i> by Bagga and Baldwin.
 * 
 * @param response
 *          The coreference chains in the response. Should be organized by document such that response contains a
 *          mapping from document ID (as a java.lang.Long) to a {@link LeanDocument}.
 * @param key
 *          The gold standard for the scoring. Use the same organization as response.
 * @return The b-cubed score for the response.
 */
@Override
public double[][] scoreRaw(DocumentPair doc, boolean printIndividualFiles)
{
  double[][] result = newRawScoreArray();
  double[] precision = bCubedScore(doc.getKey(), doc.getResponse(),  false);
  // System.out.println("Key"+doc.getKey());
  // System.out.println("Response"+doc.getResponse());
  result[0][PRECISION] = precision[0];
  result[1][PRECISION] = precision[1];

  double[] recall = bCubedScore(doc.getKey(), doc.getResponse(), true);

  result[0][RECALL] = recall[0];
  result[1][RECALL] = recall[1];
  // System.out.println(" - Precision "+precision[0]+" / "+precision[1]);
  // System.out.println(" - Recall "+recall[0]+" / "+recall[1]);
  return result;
}

@Override
public double[] score(Iterable<DocumentPair> docs, boolean printIndividualFiles)
{
  return (new BCubedConllv7()).microAverage(docs, printIndividualFiles);
}

@Override
public double[] score(DocumentPair doc, boolean printIndividualFiles)
{
  List<DocumentPair> docs = Lists.newArrayList(doc);
  return (new BCubedConllv7()).score(docs, printIndividualFiles);
}
}
