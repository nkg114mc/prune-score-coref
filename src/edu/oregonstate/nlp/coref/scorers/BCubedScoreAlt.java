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
public class BCubedScoreAlt extends InternalScorer {

  /*
   * BCubed Metric for measuring noun-phrase coreference resolution.
   */
  /**
   * An alternative way to compute BCubed score
   * @return The b-cubed score for the response.
   */
  
  @Override
  public double[][] scoreRaw(DocumentPair doc, boolean printIndividualFiles){
//    if(1>0)
//      return new double[][]{{1.0,1.0},{1.0,1.0},{1.0,1.0}};
    LeanDocument key = doc.getKey();
    LeanDocument response = doc.getResponse();
    if (DEBUG) {
      System.err.println("Scoring document " + key.getID());
    }
    double[][] result = newRawScoreArray();
    //LeanDocument partition = LeanDocument.glb(key, response);

    double totalRecall = 0;
    double totalPrecision = 0;

    //Integer[] nps = key.getNPArray();
    //int numNps = nps.length;
    Iterator<TreeMap<Integer, Integer>> goldChains = key.chainIterator();
    while(goldChains.hasNext()){
      TreeMap<Integer, Integer> chain1 = goldChains.next();
      Integer[] chainNps = chain1.values().toArray(new Integer[chain1.size()]);
      int keySize = chain1.size();
      for(Integer i=0; i<chainNps.length; i++){
        Integer np1 = chainNps[i];
        totalRecall+=1/(double)(keySize);
        for(Integer j=i+1; j<chainNps.length; j++){
          Integer np2 = chainNps[j];
          Integer twin1 = key.getMatch(np1);
          Integer twin2 = key.getMatch(np2);
          if (twin1 != null && twin2!=null && response.getElChain(twin1).equals(response.getElChain(twin2))) {
            totalRecall+=2/(double)(keySize);
          }
        }
      }
    }
//      TreeMap<Integer, Integer> chain1 = key.getElChain(np1);
//      int keySize = chain1.size();
//      chain1.values().toArray();
//      for(Integer np2:chain1.values()){
//        if(np1.equals(np2)){
//          totalRecall+=1/(double)(keySize);
//        }else{
//          Integer twin1 = key.getMatch(np1);
//          Integer twin2 = key.getMatch(np2);
//          if (twin1 != null && twin2!=null && response.getElChain(twin1).equals(response.getElChain(twin2))) {
//            totalRecall+=1/(double)(keySize);
//          }
//        }
//      }
//    }
//    Integer[] responseNps = response.getNPArray();
//    int numRespNps = responseNps.length;
//    for(int i=0;i<numRespNps;i++){
//      Integer np1 = responseNps[i];
//      TreeMap<Integer, Integer> chain1 = response.getElChain(np1);
//      int respSize = chain1.size();
//      for(Integer np2:chain1.values()){
//        if(np1.equals(np2)){
//          totalPrecision+=1/(double)(respSize);
//        }else{
//          Integer twin1 = response.getMatch(np1);
//          Integer twin2 = response.getMatch(np2);
//          if (twin1 != null && twin2!=null && key.getElChain(twin1).equals(key.getElChain(twin2))) {
//            totalPrecision+=1/(double)(respSize);
//          }
//        }
//      }
//    }
    Iterator<TreeMap<Integer, Integer>> respChains = response.chainIterator();
    while(respChains.hasNext()){
      TreeMap<Integer, Integer> chain1 = respChains.next();
      Integer[] chainNps = chain1.values().toArray(new Integer[chain1.size()]);
      int respSize = chain1.size();
      for(Integer i=0; i<chainNps.length; i++){
        Integer np1 = chainNps[i];
        totalPrecision+=1/(double)(respSize);
        for(Integer j=i+1; j<chainNps.length; j++){
          Integer np2 = chainNps[j];
          Integer twin1 = response.getMatch(np1);
          Integer twin2 = response.getMatch(np2);
          if (twin1 != null && twin2!=null && key.getElChain(twin1).equals(key.getElChain(twin2))) {
            totalPrecision+=2/(double)(respSize);
          }
        }
      }
    }

    //totalRecall+=(double)(numItems*numItems)/keySize;
    //totalPrecision+=(double)(numItems*numItems)/respSize;
    //      }
    result[0][PRECISION]= totalPrecision;
    result[1][PRECISION]= response.numNounPhrases();

    result[0][RECALL]= totalRecall;
    result[1][RECALL]= key.numNounPhrases();
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
  public double[] score(Iterable<DocumentPair> docs, boolean printIndividualFiles)
  {
    return (new BCubedScoreAlt()).microAverage(docs, printIndividualFiles);
  }

  @Override
  public double[] score(DocumentPair doc, boolean printIndividualFiles)
  {
    List<DocumentPair> docs = Lists.newArrayList(doc);
    return (new BCubedScoreAlt()).score(docs, printIndividualFiles);
  }
}
