package edu.oregonstate.nlp.coref.conll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.oregonstate.nlp.coref.SystemConfig;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.scorers.ScoreValue;

public class ConllScriptScorer {
	
	/** current script is v8.1 */
	private String VERSION = "v8.1";
	
	private String scriptPath = null;
	private static final String DEFAULT_SCRIPT_PATH = "scripts/reference-coreference-scorers/v8.01";
	
	private String resultFN = "coref_final_result";
	
	
	private static ScoreValue currentValue;
	
	
	/** System config */
	private SystemConfig config = null;
	
	
	public ConllScriptScorer(SystemConfig cfg) {
		currentValue = new ScoreValue();
		setConfig(cfg);
	}

	
	public void setScorerScript(String scriptFolderPath) {
		scriptPath = scriptFolderPath;
	}
	
	public String getResultName() {
		resultFN = config.getString("CONLL_RESULT_FN", "coref_final_result");
		return resultFN;
	}
	
	public void setConfig(SystemConfig cfg) {
		config = cfg;
		setScorerScript(config.getString("CONLL_SCORER_PATH", DEFAULT_SCRIPT_PATH));
	}
	
	public ScoreValue scoreConllDocBatch(List<ConllDocument> allConllDocs, String scoreType) {
		
		String fname = "coref_final_result";
		
		// 1) get result file 
		String goldName = goldName(fname); // gold name
		String predName = predName(fname); // pred name
		collectAllCorefResultToFile(allConllDocs, fname);
		
		// 2) collect the options
		String scriptExe = scriptPath + "/" + "scorer.pl";
		String scriptArgs = "all" + " " + goldName + " " + predName + " " + "none";
		runPerlScript(scriptExe, scriptArgs);
		
		
		// copy the score value from the cached class
		ScoreValue sval = new ScoreValue();
		sval.copyFrom(currentValue);
		
		return sval;
	}
	
	
	// scoring with the conll file
	public ScoreValue scoreConllDocBatchFile(String goldFile, String predFile, String scoreType) {
		
		// 2) collect the options
		String scriptExe = scriptPath + "/" + "scorer.pl";
		String scriptArgs = "all" + " " + goldFile + " " + predFile + " " + "none";
		runPerlScript(scriptExe, scriptArgs);
		
		// copy the score value from the cached class
		ScoreValue sval = new ScoreValue();
		sval.copyFrom(currentValue);
		
		return sval;
	}
	
	
	// result to file
	public static void collectAllCorefResultToFile(List<ConllDocument> allConllDocs, String fbasename) {
		
		///////////////////////////// gold //////////////////////////////
		// gold name
		String goldName = goldName(fbasename);
		PrintWriter goldFileWriter = getWriter(goldName);
		
		String gcontent = collectAllCorefResult(allConllDocs, true);
		
		// write into the file
		//goldFileWriter.print("#begin document 1\n");
		goldFileWriter.print(gcontent + "\n");
		//goldFileWriter.print("\n#end document\n");
		goldFileWriter.flush();
		/////////////////////////////////////////////////////////////////
		
		///////////////////////////// pred //////////////////////////////
		// pred name
		String predName = predName(fbasename);
		PrintWriter predFileWriter = getWriter(predName);
		
		String pcontent = collectAllCorefResult(allConllDocs, false);
		
		// write into the file
		//predFileWriter.print("#begin document 1");
		predFileWriter.print(pcontent + "\n");
		//predFileWriter.print("\n#end document\n");
		predFileWriter.flush();
		/////////////////////////////////////////////////////////////////
	}
	
	public static String collectAllCorefResult(List<ConllDocument> allConllDocs, boolean isGold) {
		
		StringBuilder sb = new StringBuilder();
		
		// gold
		if (isGold) {
			for (ConllDocument cdoc : allConllDocs) {
				sb.append(cdoc.goldAnnotationToStr());
				sb.append("\n");
			}
		} else {
			for (ConllDocument cdoc : allConllDocs) {
				sb.append(cdoc.predAnnotationToStr());
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}
	
	private static String goldName(String filename) {
		String goldName = filename + ".gold.txt";
		return goldName;
	}
	private static String predName(String filename) {
		String predName = filename + ".pred.txt";
		return predName;
	}
	
	/// Score the Reconcile documents ///////////////////////////////////
	
	public void scoreReconcileDocBatch(ArrayList<Document> allDocs, String scoreType) {
		scoreReconcileDocBatch(allDocs, "reconcile_coref_final_result", scoreType);
	}
	
	public void scoreReconcileDocBatch(ArrayList<Document> allDocs, String outputfn, String scoreType) {
		
		String fname = outputfn;
		
		// 1) get result file 
		String goldName = goldName(fname); // gold name
		String predName = predName(fname); // pred name
		collectReconcileResultToFile(allDocs, fname);
		
		// 2) collect the options
		String scriptExe = scriptPath + "/" + "scorer.pl";
		String scriptArgs = "all" + " " + goldName + " " + predName + " " + "none";//"conll_score.txt";
		runPerlScript(scriptExe, scriptArgs); // run scorer script
	}
	
	// result to file
	public void collectReconcileResultToFile(ArrayList<Document> docs, String fbasename) {
		
		///////////////////////////// gold //////////////////////////////
		// gold name
		String goldName = goldName(fbasename);
		PrintWriter goldFileWriter = getWriter(goldName);
		
		String gcontent = collectReconcileResult(docs, true);
		
		// write into the file
		goldFileWriter.print(gcontent + "\n");
		goldFileWriter.flush();
		/////////////////////////////////////////////////////////////////
		
		///////////////////////////// pred //////////////////////////////
		// pred name
		String predName = predName(fbasename);
		PrintWriter predFileWriter = getWriter(predName);
		
		String pcontent = collectReconcileResult(docs, false);
		
		// write into the file
		predFileWriter.print(pcontent + "\n");
		predFileWriter.flush();
		/////////////////////////////////////////////////////////////////
	}
	
	public String collectReconcileResult(ArrayList<Document> docs, boolean isGold) {
		
		StringBuilder sb = new StringBuilder();
		// gold
		if (isGold) {
			for (Document doc : docs) {
				sb.append(doc.goldAnnotationToStr());
				sb.append("\n");
			}
		} else {
			for (Document doc : docs) {
				sb.append(doc.predAnnotationToStr());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	private void runPerlScript(String scriptPath, String scriptArg) {
		assert(scriptPath != null && 
			   scriptArg != null);
		
		System.out.println("Running script: " + scriptPath + " "+ scriptArg);
		
		File scriptFile = new File(scriptPath);
		String path = scriptFile.getParent();
		String scName = scriptFile.getName();
		String result = "";
		
		//String cmd = "bash -c \"cd " + path + "; ./" + scName +" "+ scriptArg+"\"";
		//System.out.println("Running cmd: " + cmd);
		try {
			Process p = Runtime.getRuntime().exec("perl " + scriptPath + " " + scriptArg);
			p.waitFor(); 
			
			// outputs
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ( (line = br.readLine()) != null) {
			   builder.append(line);
			   builder.append(System.getProperty("line.separator"));
			}
			result = builder.toString();
			// parse it!
			//parseScoreOutput(result);
			List<ConllScore> scs = ConllNewScoring.parseScorerOutput(result);
			printScoreGivenConllScores(scs);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		
		//System.out.println(result);
	}
	
	private static PrintWriter getWriter(String fn) {
		File out = new File(fn);
		PrintWriter outWriter = null;
		try {
			outWriter = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return outWriter;
	}
	
	public void showVersion() {
		System.out.println("Version: " + VERSION);
		System.out.println("Script:  " + scriptPath);
		System.out.println("ResultFn:" + resultFN);
	}
	
	private static void printScoreGivenConllScores(List<ConllScore> scs) {

		double[] mdetect = new double[3];
		double[] bcube = new double[3];
		double[] muc = new double[3];
		double[] ceafe = new double[3];
		
		for (ConllScore conllsc : scs) {
			String name = conllsc.name.toLowerCase();
			if (name.contains("muc")) {
				conllsc.fill3Arr(muc);
			} else if (name.contains("bcub")) {
				conllsc.fill3Arr(bcube);
			} else if (name.contains("ceafe")) {
				conllsc.fill3Arr(ceafe);
			} 
		}
		
		System.out.println("=========================================");
		System.out.println("  muc-f1 = " + muc[2] + " %");
		System.out.println("bcube-f1 = " + bcube[2] + " %");
		System.out.println("ceafe-f1 = " + ceafe[2] + " %");
		System.out.println("-----------------------------------------");
		//                              p        r        f1
		System.out.println("  muc : " + muc[1] + " " + muc[0]+ " "  + muc[2]);
		System.out.println("bcube : " + bcube[1]+ " "  + bcube[0]+ " "  + bcube[2]);
		System.out.println("ceafe : " + ceafe[1]+ " "  + ceafe[0]+ " "  + ceafe[2]);
		
		System.out.println("-----------------------------------------");
		// conll score
		double conll_avg = ((muc[2] + bcube[2] + ceafe[2]) / 3.00);
		System.out.println("conll_avg = " + conll_avg + " %");
		System.out.println("=========================================");
		
		// fill score value
		fillScoreValue(muc, bcube, ceafe, conll_avg);
	}

	private static void parseScoreOutput(String output) {

		String[] tks1 = output.split("\\s+");

		double[] mdetect = new double[3];
		double[] bcube = new double[3];
		double[] muc = new double[3];
		double[] ceafe = new double[3];


		for (int i = 0; i < tks1.length; i++) {
			tks1[i] = tks1[i].replace('(', ' ');
			tks1[i] = tks1[i].replace(')', ' ');
			tks1[i] = tks1[i].replace('%', ' ');
		}


		String[] tks = new String[tks1.length];
		int k = 0; // actual length
		for (int j = 0; j < tks1.length; j++) {
			if (tks1[j].equals("Repe:")) {
				j += 3;
			} else {
				tks[k] = tks1[j];
				//System.out.println("token = " + tks[k]);
				k++;
			}
		}
		int len = k;
		
		//Repe: 656, 656 7290

		int currenBlockIndex = 0;
		for (int i = 0; i < len; i++) {
			String s = tks[i];

			if (s.contains("METRIC")) {
				currenBlockIndex = 0;

				String scoreName = tks[i + 1];
				double[] precision = new double[3]; // num/den/faction
				double[] recall = new double[3]; // num/den/faction
				double[] score = new double[3]; // pre/rec/f1

				mdetect[0] = Double.parseDouble(tks[i + 17]);
				mdetect[1] = Double.parseDouble(tks[i + 12]);
				mdetect[2] = Double.parseDouble(tks[i + 19]);

				score[0] = Double.parseDouble(tks[i + 26]);
				score[1] = Double.parseDouble(tks[i + 31]);
				score[2] = Double.parseDouble(tks[i + 33]);

				if (scoreName.contains("muc")) {
					muc[0] = score[0]; // r
					muc[1] = score[1]; // p
					muc[2] = score[2]; // f1
				} else if (scoreName.contains("bcub")) {
					bcube[0] = score[0];
					bcube[1] = score[1];
					bcube[2] = score[2];
				} else if (scoreName.contains("ceafe")) {
					ceafe[0] = score[0];
					ceafe[1] = score[1];
					ceafe[2] = score[2];
				} else if (scoreName.contains("ceafm")) {
					// do nothing
				}

			}

			//System.out.println(s);
		}

		
		System.out.println("=========================================");
		System.out.println("  muc-f1 = " + muc[2] + " %");
		System.out.println("bcube-f1 = " + bcube[2] + " %");
		System.out.println("ceafe-f1 = " + ceafe[2] + " %");
		System.out.println("-----------------------------------------");
		//                              p        r        f1
		System.out.println("  muc : " + muc[1] + " " + muc[0]+ " "  + muc[2]);
		System.out.println("bcube : " + bcube[1]+ " "  + bcube[0]+ " "  + bcube[2]);
		System.out.println("ceafe : " + ceafe[1]+ " "  + ceafe[0]+ " "  + ceafe[2]);
		
		System.out.println("-----------------------------------------");
		// conll score
		double conll_avg = ((muc[2] + bcube[2] + ceafe[2]) / 3.00);
		System.out.println("conll_avg = " + conll_avg + " %");
		System.out.println("=========================================");
		
		// fill score value
		fillScoreValue(muc, bcube, ceafe, conll_avg);
		/*
		System.out.println("=========================================");
		System.out.println("  muc-f1 = " + muc[2] + " %");
		System.out.println("bcube-f1 = " + bcube[2] + " %");
		System.out.println("ceafe-f1 = " + ceafe[2] + " %");
		System.out.println("-----------------------------------------");
		// conll score
		double conll_avg = ((muc[2] + bcube[2] + ceafe[2]) / 3.00);
		System.out.println("conll_avg = " + conll_avg + " %");
		System.out.println("=========================================");
		*/
	}
	
	
	private static void fillScoreValue(double[] mucArr, double[] bcubArr, double[] ceafeArr, double conllValue) {
		currentValue.muc[0] = mucArr[0];
		currentValue.muc[1] = mucArr[1];
		currentValue.muc[2] = mucArr[2];
		
		currentValue.bcub[0] = bcubArr[0];
		currentValue.bcub[1] = bcubArr[1];
		currentValue.bcub[2] = bcubArr[2];
		
		currentValue.ceafe[0] = ceafeArr[0];
		currentValue.ceafe[1] = ceafeArr[1];
		currentValue.ceafe[2] = ceafeArr[2];
		
		currentValue.conll = conllValue;
	}
	
	
	public static void main(String[] args) {
		
		String testCase = "METRIC muc:\n\n====== TOTALS =======\nIdentification of Mentions: Recall: (15494 / 16291) 95.1%	Precision: (15494 / 15494) 100%	F1: 97.49%\n--------------------------------------------------------------------------\nCoreference: Recall: (10341 / 12365) 83.63%	Precision: (10341 / 11910) 86.82%	F1: 85.19%\n--------------------------------------------------------------------------\n\nMETRIC bcub:\n\n====== TOTALS =======\nIdentification of Mentions: Recall: (15494 / 16291) 95.1%	Precision: (15494 / 15494) 100%	F1: 97.49%\n--------------------------------------------------------------------------\nCoreference: Recall: (12126.0754431455 / 16291) 74.43%	Precision: (11762.6331584883 / 15494) 75.91%	F1: 75.16%\n--------------------------------------------------------------------------\n\nMETRIC ceafm:\n\n====== TOTALS =======\nIdentification of Mentions: Recall: (15494 / 16291) 95.1%	Precision: (15494 / 15494) 100%	F1: 97.49%\n--------------------------------------------------------------------------\nCoreference: Recall: (11800 / 16291) 72.43%	Precision: (11800 / 15494) 76.15%	F1: 74.24%\n--------------------------------------------------------------------------\n\nMETRIC ceafe:\n\n====== TOTALS =======\nIdentification of Mentions: Recall: (15494 / 16291) 95.1%	Precision: (15494 / 15494) 100%	F1: 97.49%\n--------------------------------------------------------------------------\nCoreference: Recall: (2840.06776102473 / 3926) 72.33%	Precision: (2840.06776102473 / 3584) 79.24%	F1: 75.63%\n--------------------------------------------------------------------------\n";
		
		parseScoreOutput(testCase);
	}
}
