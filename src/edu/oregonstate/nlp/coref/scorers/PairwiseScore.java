/*
 * @author ves
 */

package edu.oregonstate.nlp.coref.scorers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.stanford.nlp.dcoref.CorefCluster;
import edu.stanford.nlp.dcoref.Document;
import edu.stanford.nlp.dcoref.Mention;

/*
 * BCubed Metric for measuring noun-phrase coreference resolution.
 */
public class PairwiseScore
    extends InternalScorer {

/**
 * Calculates the Pairwise score for the given range of documents. This is a recall score for the response, compared to
 * the key. To calculate precision simply pass the response as the key, and vice versa. For more information see
 * <i>Algorithms for Scoring Coreference Chains </i> by Bagga and Baldwin.
 * 
 * @param first
 *          The first document to score.
 * @param last
 *          The last document to score.
 * @param response
 *          The coreference chains in the response. Should be organized by document such that response contains a
 *          mapping from document ID (as a java.lang.Long) to a {@link LeanDocument}.
 * @param key
 *          The gold standard for the scoring. Use the same organization as response.
 * @param pairs
 *          A map from NP's in the key to the corresponding NP in the response. An unmatched NP n should satisfy
 *          pairs.get(n) == null.
 * @return The pairwise score for the response.
 */
public static double[] pwiseScore(LeanDocument key, LeanDocument response)
{
  if (DEBUG) {
    System.err.println("Scoring document " + key.getID());
  }
/*
  protected void calculateRecall(Document doc) {
    int rDen = 0;
    int rNum = 0;
    Map<Integer, Mention> predictedMentions = doc.allPredictedMentions;

    for(CorefCluster g : doc.goldCorefClusters.values()) {
      int clusterSize = g.getCorefMentions().size();
      rDen += clusterSize*(clusterSize-1)/2;
      for(Mention m1 : g.getCorefMentions()){
        Mention predictedM1 = predictedMentions.get(m1.mentionID);
        if(predictedM1 == null) {
          continue;
        }
        for(Mention m2 : g.getCorefMentions()) {
          if(m1.mentionID >= m2.mentionID) continue;
          Mention predictedM2 = predictedMentions.get(m2.mentionID);
          if(predictedM2 == null) {
            continue;
          }
          if(predictedM1.corefClusterID == predictedM2.corefClusterID){
            rNum++;
          }
        }
      }
    }
    recallDenSum += rDen;
    recallNumSum += rNum;
  }
*/
  double rDen = 0;
  double rNum = 0;
  // Calculate the total recall, treating key as the gold standard.
  // The error is calculated with respect to each NP entity; since this
  // is for single document coreference, there is no need to check pairs
  // of entities that do not come from the same document (wrt ID).
  // The basic idea is to iterate over the equivalence classes (chains) in
  // a gold standard document and for each one, see how well the
  // response's document has kept elements of the equivalence class together.

  // Hack -- there is a problem when the document contains only 1 np
  // just skip those documents

  Iterator<TreeMap<Integer, Integer>> goldChains = key.chainIterator();
  
  while (goldChains.hasNext()) {
    TreeMap<Integer, Integer> keyChain = goldChains.next();
    Iterator<Integer> nouns = keyChain.keySet().iterator();
    
    int clusterSize = keyChain.keySet().size();
    rDen += (clusterSize * (clusterSize-1)) /2;
    
    while (nouns.hasNext()) {
      Integer entity1 = nouns.next();
      Integer twin1 = key.getMatch(entity1);
      if (twin1 == null) {
    	  continue;
      }
      
      Iterator<Integer> nouns2 = keyChain.keySet().iterator();
      while (nouns2.hasNext()) {
          Integer entity2 = nouns2.next();
          if (entity1.intValue() >= entity2.intValue()) {
        	  continue;
          }
          
          Integer twin2 = key.getMatch(entity2);
          if (twin2 == null) {
        	  continue;
          }

          
          Integer twin1CID = response.getClusterNum(twin1);
          Integer twin2CID = response.getClusterNum(twin2);
          if (twin1CID != null && twin2CID != null) {
              if (twin1CID.intValue() == twin2CID.intValue()) {
            	  rNum++;
              }
          }
      }
    }
  }

  double[] pw = new double[2];
  pw[0] = rNum;
  pw[1] = rDen;
  return pw;
}

/**
 * Calculates the Pairwise score for the given range of documents. This is a recall score for the response, compared to
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
  
  double[] precision = pwiseScore(doc.getResponse(), doc.getKey());

  result[0][PRECISION] = precision[0];
  result[1][PRECISION] = precision[1];

  double[] recall = pwiseScore(doc.getKey(), doc.getResponse());

  // if(precision!=recall)
  // throw new RuntimeException("Precision "+precision+", Recall "+recall);

  result[0][RECALL] = recall[0];
  result[1][RECALL] = recall[1];
  return result;
}

@Override
public double[] score(Iterable<DocumentPair> docs, boolean printIndividualFiles)
{
  return (new PairwiseScore()).microAverage(docs, printIndividualFiles);
}

@Override
public double[] score(DocumentPair doc, boolean printIndividualFiles)
{
  List<DocumentPair> docs = Lists.newArrayList(doc);
  return (new PairwiseScore()).score(docs, printIndividualFiles);
}
}
