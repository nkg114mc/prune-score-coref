package edu.oregonstate.nlp.coref.featureVector.individualFeature;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NumericFeature;
import edu.oregonstate.nlp.coref.general.Constants;

public class PairClassifierScore extends NumericFeature {
	
	//public static final String pairScoringFilePath = "/home/mc/workplace/rand_search/random_search_proj/mentPairScores1.txt";
	public static final String pairScoringFilePath = "mentionDump/mentPairScores1.txt";
	
	private HashMap<String,Double> cachedPairScs = new HashMap<String,Double>();
	
	public static String getPairStr(int a, int b) {
		if (a > b) {
			return (String.valueOf(a) + "-" + String.valueOf(b));
		} else {
			return (String.valueOf(b) + "-" + String.valueOf(a));
		}
	}
	
	public void loadFromFile(String inputFile) {
		
		/////////////////////////
		
		String line;
		FileReader reader;
		BufferedReader br;
		try {
			reader = new FileReader(inputFile);
			br = new BufferedReader(reader);

			int lineCnt = 0;

			line = br.readLine();
			while (line != null) {
				if (!line.equals("")) {
					String[] tks = line.split("\\s+");
					int glbiIdx = Integer.parseInt(tks[0]);
					int glbjIdx = Integer.parseInt(tks[1]);
					double sc = Double.parseDouble(tks[2]);
					
					if ((glbiIdx >= 0) && (glbjIdx >= 0)) {
						String pairStr = getPairStr(glbiIdx,glbjIdx);
						cachedPairScs.put(pairStr, sc);
					}
					
					lineCnt++;
				}
				
				line = br.readLine();
			}

			br.close();
			reader.close();
			
			System.out.println("Total line = " + lineCnt);
			System.out.println("Loaded ment scores = " + cachedPairScs.size());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PairClassifierScore() {
		loadFromFile(pairScoringFilePath);
	}

	@Override
	public String produceValue(Annotation curr, Annotation ante, Document doc, Map<Feature, String> featVector) {
		String result = "0";
		String pairStr = checkMentionPair(curr, ante);
		if (pairStr != null) {
			if (cachedPairScs.containsKey(pairStr)) {
				double sc = cachedPairScs.get(pairStr);
				result = String.valueOf(sc);
			} else {
				// do nothing
				System.err.println("Pair (" + pairStr + ") has no corresponding score!");
				result = "0";
			}
		} else {
			// do nothing
		}
		return result;
	}
	
	public String checkMentionPair(Annotation curr, Annotation ante) {
		String currGIdx = curr.getAttribute(Constants.GLOABAL_ID);
		String anteGIdx = ante.getAttribute(Constants.GLOABAL_ID);
		if ((currGIdx != null) && (anteGIdx != null)) {
			if ((StringUtils.isNumeric(currGIdx)) && (StringUtils.isNumeric(anteGIdx))) {
				int curIdx = Integer.parseInt(currGIdx);
				int antIdx = Integer.parseInt(anteGIdx);
				String pstr = getPairStr(curIdx, antIdx);
				return pstr;
			}
		}
		
		return null; // problematic!
	}

}
