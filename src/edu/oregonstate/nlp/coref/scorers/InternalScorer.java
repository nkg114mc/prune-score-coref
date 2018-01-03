package edu.oregonstate.nlp.coref.scorers;

import java.util.List;

import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.Utils;
import edu.oregonstate.nlp.coref.scorers.Matcher.MatchStyleEnum;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


public abstract class InternalScorer extends Scorer {

private double docN = 0.0;
private double preNum = 0.0;
private double preDen = 0.0;
private double recNum = 0.0;
private double recDen = 0.0;

public abstract double[] score(Iterable<DocumentPair> docs, boolean printIndividualFiles);

public abstract double[] score(DocumentPair doc, boolean printIndividualScores);

public abstract double[][] scoreRaw(DocumentPair doc, boolean printIndividualFiles);

@Override
public double[] score(boolean printIndividualFiles, Iterable<Document> files)
{
	List<DocumentPair> docs = Lists.newArrayList();
	return score(printIndividualFiles, files, docs);
}

/*
public double[] score(boolean printIndividualFiles, Iterable<Document> files, List<DocumentPair> docs)
{

  setPrintIndividualScores(printIndividualFiles);
  try {

    for (Document doc : files) {
      AnnotationSet key = doc.getAnnotationSet(Constants.GS_NP);
      //key.setName("nps");
      //String responseName = FileStructure.getPath(file, fs.getClusterSubdir(), clusterName);
      //System.out.println("cluster anno names: " + clusterName);
     // AnnotationSet response = doc.getAnnotationSet(clusterName);
      AnnotationSet response = doc.getAnnotationSet(Constants.NP);
      
      
      
      if (doc.getCannonicalAnnotationSetName(Constants.GS_NP).equals(doc.getCannonicalAnnotationSetName(Constants.NP))) {
        // Case 1: key and response are the same set of CEs
    	//System.out.println("key and response has the same connonicalSetName");
        Matcher.exactMatchAnnotationSets(key, response);
      } else {
        // Need to read in all annotations and the text since some of them are used for matching
        MatchStyleEnum matchStyle;
        // Match automatic to gs nps
        matchStyle = MatchStyleEnum.ONTO;
        // System.out.println(key);
        Matcher.matchAnnotationSets(key, response, matchStyle, doc, false);
      }
      DocumentPair dp = DocumentPair.makeFromMatchedAnnots(key, response);
      //System.out.println(dp.getKey());
      docs.add(dp);
    }

    return score(docs, printIndividualFiles);
  }
  catch (Exception e) {
    throw new RuntimeException(e);
  }
}
*/
public double[] score(boolean printIndividualFiles, Iterable<Document> files, List<DocumentPair> docs)
{

  try {

    for (Document doc : files) {
      AnnotationSet key = doc.getPredictMentionSet();
      AnnotationSet response = doc.getGoldMentionSet();
      
      ConllMatcher.matchAnnotationSets(key, response, doc);

      DocumentPair dp = DocumentPair.makeFromMatchedAnnots(key, response);
      docs.add(dp);
    }

    return score(docs, printIndividualFiles);
  }
  catch (Exception e) {
    throw new RuntimeException(e);
  }
}

public double[] scoreInternal(DocumentPair doc, boolean printIndividualFiles)
{
  double[][] sc = scoreRaw(doc, printIndividualFiles);
  double[] result = new double[RESULT_SIZE];
  int FINAL_RESULT_SIZE = 3;
  for (int i = 0; i < FINAL_RESULT_SIZE; i++) {
    if (i != F) {
      result[i] = sc[0][i] / sc[1][i];
    }
    else {
      result[i] = sc[0][i];
    }
  }
  return result;
}

public double[] microAverage(Iterable<DocumentPair> docs, boolean printIndividualFiles)
{
  double totalPrecision = 0, totalRecall = 0;
  double totalDenumPrecsion = 0, totalDenumRecall = 0;
  int i = 0;
  for (DocumentPair doc : docs) {
    // System.out.println("Document "+docs[i].getFilename());
    double[][] score = scoreRaw(doc, printIndividualFiles);
    totalPrecision += score[0][PRECISION];
    totalRecall += score[0][RECALL];
    totalDenumPrecsion += score[1][PRECISION];
    totalDenumRecall += score[1][RECALL];
    if (printIndividualFiles) {
      double[] docScore = new double[RESULT_SIZE];
      double pNum = score[0][PRECISION], pDenum = score[1][PRECISION];
      double rNum = score[0][RECALL], rDenum = score[1][RECALL];
      docScore[PRECISION] = pNum / pDenum;
      docScore[RECALL] = rNum / rDenum;
      docScore[F] = f1(docScore[PRECISION], docScore[RECALL]);
      System.out.print("Document\t" + i + " " + (int) rNum + "/" + (int) rDenum + "\t" + (int) pNum + "/"
          + (int) pDenum);
      System.out.println("Chains/nps="+doc.getKey().numChains()+"/"+doc.getKey().numNounPhrases()+"="+((float)doc.getKey().numChains()/(float)doc.getKey().numNounPhrases()));
      printScore("", docScore);
    }
    i++;
  }
  //System.out.println("Precision: "+(int)totalPrecision+"/"+(int)totalDenumPrecsion+" recall "+(int)totalRecall+"/"+(int)totalDenumRecall);
  double precision = totalDenumPrecsion<0.001?0.0:totalPrecision / totalDenumPrecsion;
  double recall = totalDenumRecall<0.001?0.0:totalRecall / totalDenumRecall;
  double[] result = new double[RESULT_SIZE];
  result[PRECISION] = precision;
  result[RECALL] = recall;
  result[F] = f1(precision, recall);
  
  //System.out.println("Runing MicroAverage!");
  /*
  docN = i;
  preNum = totalPrecision;
  preDen = totalDenumPrecsion;
  recNum = totalRecall;
  recDen = totalDenumRecall;
  */
  result[PRENUM] = totalPrecision;
  result[PREDEN] = totalDenumPrecsion;
  result[RECNUM] = totalRecall;
  result[RECDEN] = totalDenumRecall;
  result[NDOC] = i;
  //System.out.println(docN+" "+preNum+" "+preDen+" "+recNum+" "+recDen);
  return result;
}

public double[] macroAverage(Iterable<DocumentPair> docs, boolean printIndividualFiles)
{
  double totalPrecision = 0, totalRecall = 0, totalF = 0;
  int i = 0;
  int length = Iterables.size(docs);
  for (DocumentPair doc : docs) {
    double[] score = scoreInternal(doc, printIndividualFiles);
    if (printIndividualFiles) {
      System.out.println("Document " + i++);
      printScore(getClass().getSimpleName(), score);
    }
    totalPrecision += score[PRECISION];
    totalRecall += score[RECALL];
    totalF += score[F];
    i++;
  }
  double precision = totalPrecision / length;
  double recall = totalRecall / length;
  double[] result = new double[RESULT_SIZE];
  result[PRECISION] = precision;
  result[RECALL] = recall;
  result[F] = totalF / length;

  /*
  System.out.println("Runing MicroAverage!");
  docN = i;
  preNum = totalPrecision;
  preDen = totalDenumPrecsion;
  recNum = totalRecall;
  recDen = totalDenumRecall;
  System.out.println(docN+" "+preNum+" "+preDen+" "+recNum+" "+recDen);
  */
  result[PRENUM] = totalPrecision;
  result[PREDEN] = length;
  result[RECNUM] = totalRecall;
  result[RECDEN] = length;
  result[FNUM] = totalF;
  result[FDEN] = length;
  result[NDOC] = i;
  
  return result;
}

/*
 * Average a single-number score (i.e., no precision and recall)
 */
public double[] macroAverageSingleScore(Iterable<DocumentPair> docs, boolean printIndividualFiles)
{
  double total = 0;
  int length = Iterables.size(docs);
  int i = 0;
  for (DocumentPair doc : docs) {
    double[][] score = scoreRaw(doc, printIndividualFiles);
    total += score[0][0];
    if (PRINT_SCORES) {
      System.out.println("Document " + i++);
      printScoreSingle(getClass().getSimpleName(), score[0]);
    }
  }
  double[] result = new double[RESULT_SIZE];
  result[0] = total / length;

  result[F] = result[0];
  return result;
}

public double[] totalNumeraterDenominater()
{
  double[] result = new double[5];
  result[0] = docN; // doc number
  result[1] = preNum;
  result[2] = preDen;
  result[3] = recNum;
  result[4] = recDen;

  return result;
}

}
