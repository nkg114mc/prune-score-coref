package edu.oregonstate.nlp.coref.featureVector.mentionPropertyFeature;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.MentionPropertyFeature;
import edu.oregonstate.nlp.coref.featureVector.NumericMentionPropertyFeature;
import edu.oregonstate.nlp.coref.featureVector.individualFeature.PairClassifierScore;
import edu.oregonstate.nlp.coref.general.Constants;

/**
 * Return the length of mention in terms of words
 * 
 * @author machao
 */
public class MentionClassifierScore extends NumericMentionPropertyFeature {
	
	private HashMap<Integer,Double> cachedMentScs = new HashMap<Integer,Double>();

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
					
					if ((glbiIdx < 0) || (glbjIdx >= 0)) {
						cachedMentScs.put(glbjIdx, sc);
					} else if ((glbiIdx >= 0) || (glbjIdx < 0)) {
						cachedMentScs.put(glbiIdx, sc);
					}
					
					lineCnt++;
				}
				
				line = br.readLine();
			}

			br.close();
			reader.close();
			
			System.out.println("Total line = " + lineCnt);
			System.out.println("Loaded ment scores = " + cachedMentScs.size());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public MentionClassifierScore() {
		loadFromFile(PairClassifierScore.pairScoringFilePath);
	}
	
	@Override
	public String produceValue(Annotation mention, Document doc, Map<MentionPropertyFeature, String> featVector) {
		String result = "0";
		String globalIndxStr = mention.getAttribute(Constants.GLOABAL_ID);
		if (globalIndxStr != null) {
			if (StringUtils.isNumeric(globalIndxStr)) {
				int gidx = Integer.parseInt(globalIndxStr);
				if (cachedMentScs.containsKey(gidx)) {
					double sc = cachedMentScs.get(gidx);
					result = String.valueOf(sc);
				} else {
					// do nothing
					System.err.println(gidx + " has no corresponding score!");
					result = "0";
				}
			} else {
				// do nothing
			}
		} else {
			// do nothing
		}
		return result;
	}
	



}
