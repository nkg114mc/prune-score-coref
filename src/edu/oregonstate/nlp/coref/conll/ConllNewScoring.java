package edu.oregonstate.nlp.coref.conll;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConllNewScoring {
	
	public static String ScoringScriptPath = "scripts/reference-coreference-scorers/v8.01/scorer.pl";
	
	public static final String[] accScNames = { "muc", "bcub", "ceafe" };
	
	public static List<ConllScore> parseScorerOutput(String str) {
		
		str = str.replaceAll("METRIC", "OOOOOOOOOOOOOOOOOOOOOO METRIC");
		String[] segs = str.split("OOOOOOOOOOOOOOOOOOOOOO");
		
		HashSet<String> accNameSet = new HashSet<String>(Arrays.asList(accScNames));
		ArrayList<ConllScore> scs = new ArrayList<ConllScore>();
		
		for (String seg : segs) {
			seg = seg.trim();
			if (seg.startsWith("METRIC")) {
				ConllScore sc = parseScore(seg);
				if (accNameSet.contains(sc.name)) {
					scs.add(sc);
				}
			}
		}
		
		printAvg(scs);
		
		return scs;
	}
	
	public static void printAvg(ArrayList<ConllScore> scs) {
		assert (scs.size() == 3);
		double sum = 0;
		for (ConllScore sc : scs) {
			sum += sc.f1;
			System.out.println(sc.text);
			System.out.println(sc.name + " = " + sc.f1);
		}
		double avg = sum / 3.00;
		
		ConllScore conll = new ConllScore();
		conll.name = "CoNLL";
		conll.f1 = avg;
		scs.add(conll);
		
		System.out.println("CoNLL Score: " + avg);
	}
	
	public static ConllScore parseScore(String seg) {
		
		ConllScore sc = new ConllScore();
		sc.text = seg;
		
		seg = seg.replaceAll(":", "")
				 .replaceAll("%", "")
				 .replaceAll("\\(", "")
		         .replaceAll("\\)", "");
		String[] ss = seg.split("\\s+");
		
		
		int flag = 0;
		for (int i = 0; i < ss.length; i++) {
			//System.err.println(ss[i]);
			if (ss[i].equals("METRIC")) {
				sc.name = ss[i + 1];
			} else if (ss[i].equals("--------------------------------------------------------------------------")) {
				flag = (flag + 1) % 2;
			} else {
				if (flag > 0) {
					if (ss[i].contains("F1")) {
						sc.f1 = Double.parseDouble(ss[i + 1]);
					} else if (ss[i].contains("Recall")) {
						sc.rec = Double.parseDouble(ss[i + 4]);
					} else if (ss[i].contains("Precision")) {
						sc.pre = Double.parseDouble(ss[i + 4]);
					}
				}
			}
		}
		
		return sc;
	}
	
	
	public static List<ConllScore> runScorer(File goldFile, File predFile) {
		String conllEvalScriptPath = ScoringScriptPath;
	    String cmd = ("perl " + conllEvalScriptPath + " all " + goldFile.getAbsolutePath() + " " + predFile.getAbsolutePath() + " none");
	    System.err.println(cmd);
	    return runScript(cmd);
	}
	
	public static List<ConllScore> runScript(String cmd) {
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor(1, TimeUnit.MINUTES);
			
			// outputs
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ( (line = br.readLine()) != null) {
			   builder.append(line);
			   builder.append(System.getProperty("line.separator"));
			}
			String result = builder.toString();
			
			List<ConllScore> scs = ConllNewScoring.parseScorerOutput(result);
			//System.out.println(result);
			return scs;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	
	public static void main(String[] args) {
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get("/home/mc/workplace/rand_search/coref/t1.txt"));
			String s = new String(encoded);
			//System.err.println(s);
			parseScorerOutput(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
