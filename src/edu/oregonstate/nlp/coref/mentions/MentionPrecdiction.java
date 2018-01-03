package edu.oregonstate.nlp.coref.mentions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.Utils;

public class MentionPrecdiction {
	
	public void printMentionPrediction(Document doc) {
		AnnotationSet mens = doc.getAnnotationSet(Constants.NP);
		for (Annotation men : mens.getOrderedAnnots()) {
			int mids = Integer.parseInt(men.getAttribute(Constants.CE_ID));
			System.out.println(mids + " " + doc.getAnnotText(men.getStartOffset(), men.getEndOffset()));
		}
	}
	
	public void printGoldMentions(Document doc) {
		AnnotationSet goldCes = doc.getAnnotationSet(Constants.GS_NP);
		for (Annotation men : goldCes.getOrderedAnnots()) {
			int mids = Integer.parseInt(men.getAttribute(Constants.CE_ID));
			System.out.println(mids + " " + doc.getAnnotText(men.getStartOffset(), men.getEndOffset()));
		}
	}

	private PrintWriter constructPrinter(String path) {
		PrintWriter writer = null;
		try {
			FileOutputStream featos = new FileOutputStream(path);
			writer = new PrintWriter(new OutputStreamWriter(featos),true);
		} catch(FileNotFoundException ex) {
		}
		return writer;
	}

	public void printCorefMentions(Document doc) {
		//String path = doc.getAbsolutePath() + Utils.SEPARATOR + Constants.ANNOT_DIR_NAME + Utils.SEPARATOR + "conll_coref_output";
		printCorefMentions(doc, null, false);
	}
	public void printCorefMentions(Document doc, boolean useGold) {
		//String path = doc.getAbsolutePath() + Utils.SEPARATOR + Constants.ANNOT_DIR_NAME + Utils.SEPARATOR + "conll_coref_output";
		printCorefMentions(doc, null, useGold);
	}
	
	// gold + predict
	public void printCorefMentions(Document doc, String path, boolean useGold) {

		/////////////////////////////PrintWriter writer = constructPrinter(path);

		HashMap<Integer, HashSet<Integer>> beginTokenOfMention = new HashMap<Integer, HashSet<Integer>>();
		HashMap<Integer, HashSet<Integer>> endTokenOfMention = new HashMap<Integer, HashSet<Integer>>();
		HashMap<Integer, HashSet<Integer>> begendTokenOfMention = new HashMap<Integer, HashSet<Integer>>();
		HashMap<Integer, Annotation> menidMap = new HashMap<Integer, Annotation>();
		
		AnnotationSet tokens = doc.getAnnotationSet(Constants.TOKEN);
		Annotation tokenArr[] = (Annotation[]) tokens.getOrderedAnnots().toArray(new Annotation[0]);
		
		// predict mentions or gold mentions?
		AnnotationSet mens = doc.getAnnotationSet(Constants.NP);
		if (useGold) {
			mens = doc.getAnnotationSet(Constants.GS_NP);
		}

		for (Annotation men : mens.getOrderedAnnots()) {
			int mid = Integer.parseInt(men.getAttribute(Constants.CE_ID));
			int cid = Integer.parseInt(men.getAttribute(Constants.CLUSTER_ID));
			if (men.getAttribute(Constants.CLUSTER_ID) == null) {
				int tmpCID = mid + 5000;
				System.out.println("Mentoin["+mid+"]'s tmpCID = " + tmpCID);
				men.setAttribute(Constants.CLUSTER_ID, String.valueOf(tmpCID));
			}
			menidMap.put(mid, men);
		}
		
		//////////////////////////////////////String docName =  doc.getAbsolutePath();
		//////////////////////////////////////writer.println("#begin document (" + docName + ")");
		
		int last_end = -1;
		int next_start = Integer.MAX_VALUE;
		for (int j = 0; j < tokenArr.length; j++) {
			Annotation tt = tokenArr[j];
			if (j > 0) {
				last_end = tokenArr[j - 1].getEndOffset();
			} else {
				last_end = -1;
			}
			if (j < tokenArr.length - 1) {
				next_start = tokenArr[j + 1].getStartOffset();
			} else {
				next_start = Integer.MAX_VALUE;
			}
			
			ArrayList<Integer> beginFromThisToken = new ArrayList<Integer>();
			ArrayList<Integer> endFromThisToken = new ArrayList<Integer>();
			
			// for predict mentions =====================
			int mids = -1, mide = -1;
			int cids = -1, cide = -1;
			HashMap<Integer, Integer> mentionClusterID = new HashMap<Integer, Integer>();
			
			for (Annotation men : mens.getOrderedAnnots()) {
				if (men.getStartOffset() == tt.getStartOffset() ||
					(men.getStartOffset() <= tt.getStartOffset() && men.getStartOffset() > last_end)) {
					mids = Integer.parseInt(men.getAttribute(Constants.CE_ID));
					cids = Integer.parseInt(men.getAttribute(Constants.CLUSTER_ID));
					// token j is the starting token of mention mids
					beginFromThisToken.add(mids);
					mentionClusterID.put(mids, cids);
				}
			}
			Annotation reverseArr[] = (Annotation[]) mens.getOrderedAnnots().toArray(new Annotation[0]);
			for (int r = reverseArr.length - 1; r >= 0; r--) {
				Annotation men = reverseArr[r];
				if (men.getEndOffset() == tt.getEndOffset() ||
					(men.getEndOffset() >= tt.getEndOffset() && men.getEndOffset() < next_start)) {
					mide = Integer.parseInt(men.getAttribute(Constants.CE_ID));
					cide = Integer.parseInt(men.getAttribute(Constants.CLUSTER_ID));
					// token j is the ending token of mention mide
					endFromThisToken.add(mide);
					mentionClusterID.put(mide, cide);
				}
			}
			// ==========================================
			
			ArrayList<Integer> finalBeginFromThisToken = new ArrayList<Integer>();
			ArrayList<Integer> finalEndFromThisToken = new ArrayList<Integer>();
			HashSet<Integer> finalBeginEndFromThisToken = new HashSet<Integer>();
			HashSet<Integer> beginSet = new HashSet<Integer>();
			HashSet<Integer> endSet = new HashSet<Integer>();
			for (Integer beginMentionID : beginFromThisToken) {
				for (Integer endMentionID : endFromThisToken) {
					if (beginMentionID.intValue() == endMentionID.intValue()) {
						finalBeginEndFromThisToken.add(beginMentionID);
					}
				}
			}
			for (Integer beginMentionID : beginFromThisToken) {
				if (!finalBeginEndFromThisToken.contains(beginMentionID)) {
					finalBeginFromThisToken.add(beginMentionID);
					beginSet.add(beginMentionID);
				}
			}
			for (Integer endMentionID : endFromThisToken) {
				if (!finalBeginEndFromThisToken.contains(endMentionID)) {
					finalEndFromThisToken.add(endMentionID);
					endSet.add(endMentionID);
				}
			}
			
			beginTokenOfMention.put(j, beginSet);
			endTokenOfMention.put(j, endSet);
			begendTokenOfMention.put(j, finalBeginEndFromThisToken);
	
			
			// coref result
			StringBuilder sb2 = new StringBuilder();
			for (Integer beginMentionID : finalBeginFromThisToken) {
				if (sb2.length() > 0) {
					sb2.append("|");
				}
				int mentionCID = mentionClusterID.get(beginMentionID);
				//int mentionCID = beginMentionID;
				sb2.append("(" + String.valueOf(mentionCID));
			}
			for (Integer beginendMID : finalBeginEndFromThisToken) {
				if (sb2.length() > 0) {
					sb2.append("|");
				}
				int mentionCID = mentionClusterID.get(beginendMID);
				//int mentionCID = beginendMID;
				sb2.append("(" + String.valueOf( mentionCID) + ")");
			}
			for (Integer endMentionID : finalEndFromThisToken) {
				if (sb2.length() > 0) {
					sb2.append("|");
				}
				int mentionCID = mentionClusterID.get(endMentionID);
				//int mentionCID = endMentionID;
				sb2.append(String.valueOf(mentionCID) + ")");
			}
			if (sb2.length() == 0) {
				sb2.append("-");
			}
			
			// set the coref str as a token annotation attribute
			tt.setAttribute(Constants.CONLL_COREF, sb2.toString());
			
			// Output mention according to tokens
			String tokenStr = doc.getAnnotString(tt);
			tt.setAttribute(Constants.CONLL_WORDSTR, tokenStr);
			
			StringBuilder finalSb = new StringBuilder();
			///finalSb.append(docName + "\t");
			//finalSb.append("0\t");
			
			finalSb.append((j+1) + "\t"); // token id must start from 1, NOT 0!
			finalSb.append(tokenStr + "\t");
			
			finalSb.append(sb2.toString());
			//System.out.println(finalSb.toString());
			///////////////////////////////////writer.println(finalSb.toString());
			
		}
		//////////////////////////////writer.println("#end document");
		///////////////////////////////writer.flush();
		
		////////////////////////////////////////////////////////////
		// check mention close
		HashSet<Integer> allMenid = new HashSet<Integer>();
		HashMap<Integer, Integer> menbeginTk = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> menendTk = new HashMap<Integer, Integer>();
		for (int j = 0; j < tokenArr.length; j++) {
			HashSet<Integer> allbb = beginTokenOfMention.get(j);
			HashSet<Integer> allee = endTokenOfMention.get(j);
			HashSet<Integer> allbe = begendTokenOfMention.get(j);
			for (Integer bb : allbb) {
				allMenid.add(bb);
				menbeginTk.put(bb, j);
			}
			for (Integer ee : allee) {
				allMenid.add(ee);
				menendTk.put(ee, j);
			}
			for (Integer be : allbe) {
				allMenid.add(be);
				menbeginTk.put(be, j);
				menendTk.put(be, j);
			}
		}
		for (int menid : allMenid) {
			Integer btk = menbeginTk.get(menid);
			Integer etk = menendTk.get(menid);
			if (btk == null || etk == null) {
				for (int j = 0; j < tokenArr.length; j++) {
					Annotation tt = tokenArr[j];
					String mstr = tt.getAttribute(Constants.CONLL_WORDSTR);
					String corefstr = tt.getAttribute(Constants.CONLL_COREF);
					System.out.println((j+1) + "\t" + mstr + "\t" + corefstr);
				}
				Annotation errorMention = menidMap.get(menid);
				String menStr = errorMention.getSpanString(doc);
				throw new RuntimeException("Mention ["+menStr+"] was not closed: " + btk + " " + etk);
			}
		}
		//System.out.println("Document " + doc.getDocumentId() + " coref result passed the checking.");
		////////////////////////////////////////////////////////////
	}
	
	
	public void outputCoNLLPredictCorefResult(Document doc, String path) {
		
	}

	public void outputCoNLLGoldCorefResult(Document doc, String path) {
		
	}
	
}