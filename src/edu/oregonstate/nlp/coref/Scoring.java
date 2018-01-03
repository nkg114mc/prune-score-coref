package edu.oregonstate.nlp.coref;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.scorers.DocumentPair;
import edu.oregonstate.nlp.coref.scorers.InternalScorer;
import edu.oregonstate.nlp.coref.scorers.Matcher;
import edu.oregonstate.nlp.coref.scorers.Matcher.MatchStyleEnum;
import edu.oregonstate.nlp.coref.scorers.Scorer;

import com.google.common.collect.Lists;


public class Scoring {

	
/**
 * @param args
 */
/*
public static void score(boolean printIndividualScores, Iterable<Document> files, PrintWriter pw, String[] scNames, SystemConfig cfg)
{
  // Initialize the scorers
  ArrayList<reconcile.scorers.Scorer> scorers = intitializeScorers(scNames);

  try {
    List<DocumentPair> docs = Lists.newArrayList();

    for (Document doc : files) {
      AnnotationSet keyAnnots = doc.getAnnotationSet(Constants.GS_NP);
      AnnotationSet responseAnnots = doc.getAnnotationSet(Constants.NP);
      
      // We have to match key and response CEs.
      if (cfg.getAnnotationSetName(Constants.GS_NP).equals(cfg.getAnnotationSetName(Constants.NP))) {
        // Case 1: key and response are the same set of CEs
    	//System.out.println("[Scorer] Key and response are the same set of CEs");
        Matcher.exactMatchAnnotationSets(keyAnnots, responseAnnots);
      }
      else {
        // Need to read in all annotations and the text since some of them are used for matching
        MatchStyleEnum matchStyle;
        // Match automatic to gs nps
        if (cfg.getDataset().toLowerCase().startsWith("ace")) {
          matchStyle = MatchStyleEnum.ACE;
        }
        else if (cfg.getDataset().toLowerCase().startsWith("uw")) {
          matchStyle = MatchStyleEnum.UW;
        }
        else if (cfg.getDataset().toLowerCase().startsWith("ontonotes")) {
          matchStyle = MatchStyleEnum.ONTO;
        }
        else if (cfg.getDataset().toLowerCase().startsWith("muc")) {
          matchStyle = MatchStyleEnum.MUC;
        } else {
          throw new RuntimeException("Unknown matching style!");	
        }
        Matcher.matchAnnotationSets(keyAnnots, responseAnnots, matchStyle, doc, false);
      }
      docs.add(DocumentPair.makeFromMatchedAnnots(keyAnnots, responseAnnots));
    }

    // init score log
    double[] menDetectScore = null;
    double[][] scoreRaw = new double[16][5];
    String[]  scoreName = new String[16];
    int count = 0;
    
    menDetectScore = mentionDetectionAccuracy(files);
    
    for (reconcile.scorers.Scorer sc : scorers) {
      double[] score;

        // score = ((InternalScorer) sc).score(docs, printIndividualScores);
        // scoreRaw[count] = ((InternalScorer) sc).totalNumeraterDenominater();
        // scoreName[count] =  sc.getName();
    	InternalScorer interSC = (InternalScorer) sc;
      	score = interSC.score(docs, printIndividualScores);
      	scoreRaw[count] = score;//interSC.totalNumeraterDenominater();
      	scoreName[count] =  interSC.getName();
      Scorer.printScore(sc.getName(), score);
      if (pw != null) {
        Scorer.printFileScore(sc.getName(), score, pw);
      }
      count++;
    }
    
    // print score log
    String scorelogPath = cfg.getScoreLogPath();
    printScoreLog(scorelogPath, scoreRaw, scoreName, count);
  }
  catch (Exception e) {
    throw new RuntimeException(e);
  }
}
*/
	public static void score(boolean printIndividualScores, Iterable<Document> files, PrintWriter pw, String[] scNames, SystemConfig cfg)
	{
		// Initialize the scorers
		ArrayList<Scorer> scorers = intitializeScorers(scNames);

		try {
			List<DocumentPair> docs = Lists.newArrayList();

			for (Document doc : files) {
				AnnotationSet keyAnnots = doc.getAnnotationSet(Constants.GS_NP);
				AnnotationSet responseAnnots = doc.getAnnotationSet(Constants.NP);

				// Need to read in all annotations and the text since some of them are used for matching
				MatchStyleEnum matchStyle;
				// Match automatic to gs nps
				if (cfg.getDataset().toLowerCase().startsWith("ace")) {
					matchStyle = MatchStyleEnum.ACE;
				}
				else if (cfg.getDataset().toLowerCase().startsWith("uw")) {
					matchStyle = MatchStyleEnum.UW;
				}
				else if (cfg.getDataset().toLowerCase().startsWith("ontonotes")) {
					matchStyle = MatchStyleEnum.ONTO;
				}
				else if (cfg.getDataset().toLowerCase().startsWith("muc")) {
					matchStyle = MatchStyleEnum.MUC;
				} else {
					throw new RuntimeException("Unknown matching style!");	
				}
				Matcher.matchAnnotationSets(keyAnnots, responseAnnots, matchStyle, doc, false);

				docs.add(DocumentPair.makeFromMatchedAnnots(keyAnnots, responseAnnots));
			}

			// init score log
			double[] menDetectScore = null;
			double[][] scoreRaw = new double[16][5];
			String[]  scoreName = new String[16];
			int count = 0;

			menDetectScore = mentionDetectionAccuracy(files);

			for (Scorer sc : scorers) {
				double[] score;

				// score = ((InternalScorer) sc).score(docs, printIndividualScores);
				// scoreRaw[count] = ((InternalScorer) sc).totalNumeraterDenominater();
				// scoreName[count] =  sc.getName();
				InternalScorer interSC = (InternalScorer) sc;
				score = interSC.score(docs, printIndividualScores);
				scoreRaw[count] = score;//interSC.totalNumeraterDenominater();
				scoreName[count] =  interSC.getName();
				Scorer.printScore(sc.getName(), score);
				if (pw != null) {
					Scorer.printFileScore(sc.getName(), score, pw);
				}
				count++;
			}

			// print score log
			String scorelogPath = cfg.getScoreLogPath();
			printScoreLog(scorelogPath, scoreRaw, scoreName, count);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
private static ArrayList<Scorer> intitializeScorers(String[] ElNames)
{
  ArrayList<Scorer> result = new ArrayList<Scorer>();
  for (String el : ElNames) {
    result.add(Constructor.createScorer(el));
  }
  return result;
}

private static void printScoreLog(String filename, double[][] scoreVal, String[] scoreName, int nScorer)
{
    String logPath = filename;
	PrintWriter logPrinter;
    try {
      FileOutputStream fos = new FileOutputStream(logPath);
	  logPrinter = new PrintWriter(new OutputStreamWriter(fos), true);
    } catch(FileNotFoundException ex) {
      throw new RuntimeException("Can not find the score log:"+logPath +"!");
    }
    
    // begin to print
    logPrinter.println("<SCORE>");
    // ndoc
    logPrinter.print("NDoc ");
    logPrinter.println(scoreVal[0][Scorer.NDOC]); // ndoc
    
    for (int i = 0; i < nScorer; i++) {
       // score itself
       logPrinter.print(scoreName[i]+"Precison ");	
       logPrinter.println(scoreVal[i][Scorer.PRECISION]);
       logPrinter.print(scoreName[i]+"Recall ");	
       logPrinter.println(scoreVal[i][Scorer.RECALL]);
       logPrinter.print(scoreName[i]+"F1 ");	
       logPrinter.println(scoreVal[i][Scorer.F]);
       // Numerator and denominator	
       logPrinter.print(scoreName[i]+"PrecisonNum ");
       logPrinter.println(scoreVal[i][Scorer.PRENUM]);  // precision numerator
       logPrinter.print(scoreName[i]+"PrecisonDen ");
       logPrinter.println(scoreVal[i][Scorer.PREDEN]);  // precision denominator
       logPrinter.print(scoreName[i]+"RecallNum ");
       logPrinter.println(scoreVal[i][Scorer.RECNUM]);  // recall numerator
       logPrinter.print(scoreName[i]+"RecallDen ");
       logPrinter.println(scoreVal[i][Scorer.RECDEN]);  // recall denominator
       logPrinter.print(scoreName[i]+"F1Num ");
       logPrinter.println(scoreVal[i][Scorer.FNUM]);  // f1 numerator (Only works for CEAF)
       logPrinter.print(scoreName[i]+"F1Den ");
       logPrinter.println(scoreVal[i][Scorer.FDEN]);  // f1 denominator (Only works for CEAF)
    }
    logPrinter.println("</SCORE>");
    
    logPrinter.close();
}

public static double[] mentionDetectionAccuracy(Iterable<Document> docs) {
	double score[] = new double[3];
	
	double pre = 0, rec = 0, f1 = 0;
	double overlap = 0, ngold = 0, npred = 0;
	
	for (Document doc : docs) {
		AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
		AnnotationSet gnps = doc.getAnnotationSet(Constants.GS_NP);
		
		double overThisDoc = 0;
		for (Annotation ce : nps) {
			int id = Integer.parseInt(ce.getAttribute(Constants.CE_ID));
			Integer matchID = (Integer)ce.getProperty(Property.MATCHED_CE);
			if (matchID != null && matchID.intValue() != -1) {
				overThisDoc++;
			}
		}
		
		overlap += overThisDoc;
		ngold += gnps.size();
		npred += nps.size();
	}
	
	rec = (100.00 * overlap) / ngold;
	pre = (100.00 * overlap) / npred;
	f1 = (2 * pre * rec) / (pre + rec);
	
	System.out.println("////////////////////////////////////////////////////////////////////////");
	System.out.println("///////////////////     Reconcile Scores     ///////////////////////////");
	System.out.println("////////////////////////////////////////////////////////////////////////");
	
	System.out.println(" Reconcile Mention Detection:");
	System.out.println("========================================================================");
	System.out.print("| Recall: " + overlap + " / " + ngold + " = " + rec);
	System.out.print(" Precision: " + overlap + " / " + npred + " = " + pre);
	System.out.println(" F-1: " + f1);
	System.out.println("========================================================================");
	return score;
	
}

}
