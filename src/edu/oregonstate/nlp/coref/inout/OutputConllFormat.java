package edu.oregonstate.nlp.coref.inout;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.berkeley.nlp.syntax.Tree;
import edu.oregonstate.nlp.coref.conll.ConllDocument;
import edu.oregonstate.nlp.coref.conll.OntoNotesConllReader;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.Utils;

public class OutputConllFormat implements Serializable {

	public void ouputConllFormat(ConllDocument conlldoc, boolean writeFile) {

		ArrayList<DocPart> dpartList = new ArrayList<DocPart>();
		ArrayList<Document> reconList = conlldoc.getReconDocList();
		for (int i = 0; i < reconList.size(); i++) {
			Document part = reconList.get(i);
			DocPart dpart = outputOneReconcileDoc(part, i, conlldoc.getDocName());
			dpartList.add(dpart);
		}
		//conlldoc.setDocPartList(dpartList);

		// construct annotation
		AnnotationSet anno = constructConllAnnotationSet(dpartList, conlldoc.getDocName());
		conlldoc.setAnnotationSet(anno);
/*
		// construct berkeley compatible docs
		for (int j = 0; j < reconList.size(); j++) {
			Document reconPart = reconList.get(j);
			DocPart partdoc = conlldoc.getDocPartList().get(j);
			partdoc.berkDoc = constructBerkeleyConllDoc(conlldoc, reconPart, partdoc);
		}
*/
		// write to file
		if (writeFile) {
			// printConllAnnoSet(conlldoc);
			printConllAnnoSetBerkeleyACE(conlldoc);
		}
	}

	public void printConllAnnoSet(ConllDocument conlldoc) {

		String conllFilePath = conlldoc.getFile() + Utils.SEPARATOR + "tmp.conll";
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(conllFilePath);
			int npart = conlldoc.getPartNum();
			for (int i = 0; i < npart; i++) {
				OntoNotesConllReader.writeConllFormatPart(conlldoc.getAnnotationSet(), writer, i,
						conlldoc.getDocName());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void printConllAnnoSetBerkeleyACE(ConllDocument conlldoc) {
		String conllFilePath = conlldoc.getFile() + Utils.SEPARATOR + "tmp.conll";
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(conllFilePath);
			int npart = conlldoc.getPartNum();
			for (int i = 0; i < npart; i++) {
				writeConllFormatPartBerkeleyACE(conlldoc.getAnnotationSet(), writer, i, conlldoc.getDocName());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	


	public DocPart outputOneReconcileDoc(Document reconcileDoc, int partID, String docname) {
		ArrayList<MySentence> sentList = getTokenList(reconcileDoc, docname);
		getIidLabels(reconcileDoc, sentList);
		getParseTree(reconcileDoc, sentList);
		//getSpeaker(reconcileDoc, sentList);
		
		// ner tags
		getNer(reconcileDoc, sentList);
		// mentions
		getCorefAndMentions(reconcileDoc, sentList, false);
		DocPart partdoc = new DocPart(sentList, partID);

		return partdoc;
	}

	// token
	public ArrayList<MySentence> getTokenList(Document reconcileDoc, String docname) {

		ArrayList<MySentence> sentList = new ArrayList<MySentence>();
		int globalLineIdx = 0;

		int sentId = 0;
		AnnotationSet sents = reconcileDoc.getAnnotationSet(Constants.SENT);
		AnnotationSet allTokens = reconcileDoc.getAnnotationSet(Constants.TOKEN);

		for (Annotation sent : sents.getOrderedAnnots()) {

			MySentence curSent = new MySentence();
			curSent.index = sentId;
			curSent.tokenList.clear();
			curSent.reconSent = sent;
			curSent.tlineIndexing = MySentence.getEmptyTkLineMap();

			//System.out.println("---");
			int tkid = 0;
			AnnotationSet tokens = allTokens.getContained(sent);
			for (Annotation token : tokens.getOrderedAnnots()) {

				String tokenSpan = reconcileDoc.getAnnotString(token);
				// String posTag = allPos.
				//System.out.println(sentId + " " + tkid + " " + tokenSpan);

				// construct token line ////////////////
				TokenLine tline = new TokenLine();
				tline.docID = docname;
				tline.index = globalLineIdx;
				tline.reconToken = token;
				tline.token = tokenSpan;
				tline.sentId = sentId;
				tline.startOff = token.getStartOffset();
				tline.endOff = token.getEndOffset();
				tline.tokenId = tkid;
				curSent.tokenList.add(tline);
				////////////////////////////////////////

				int annoIdx = token.getId();
				curSent.tlineIndexing.put(annoIdx, tline);

				globalLineIdx++;
				tkid++;
			}
			//System.out.println("===");

			sentList.add(curSent);
			sentId++;
		}

		return sentList;
	}

	// iid labels: pos, sent_id, token_id
	public void getIidLabels(Document reconcileDoc, ArrayList<MySentence> sentList) {

		AnnotationSet allPos = reconcileDoc.getAnnotationSet(Constants.POS);
		for (int i = 0; i < sentList.size(); i++) {
			MySentence curSent = sentList.get(i);
			for (int j = 0; j < curSent.tokenList.size(); j++) {
				TokenLine tline = curSent.tokenList.get(j);
				Annotation token = tline.reconToken;
				Annotation posTag = allPos.getContained(token).getFirst();
				tline.pos = posTag.getType();
			}
		}

	}

	// parse tree
	public void getParseTree(Document reconcileDoc, ArrayList<MySentence> sentList) {

		// ArrayList<Tree<String>> treeList = new ArrayList<Tree<String>>();
		AnnotationSet parses = reconcileDoc.getAnnotationSet(Constants.PARSE);

		// fina all tree node
		AnnotationSet allRoots = ParseTreeUtils.getAllRootsAsAnnoSet(parses);

		for (int i = 0; i < sentList.size(); i++) {
			MySentence curSent = sentList.get(i);

			Annotation sent = curSent.reconSent;
			AnnotationSet sentRootSet = allRoots.getContained(sent);
			if (sentRootSet.size() != 1) {
				throw new RuntimeException("Parse tree has not root!" + sentRootSet.size());
			}
			Annotation sentRoot = null;
			for (Annotation srt : sentRootSet.getOrderedAnnots()) {
				sentRoot = srt;
			}

			// parse the tree
			Tree<String> tr = ParseTreeUtils.constructTreeFromAnnotationSet(sentRoot, parses);
			curSent.ptrees = tr;

			/////////////////////////////////////////////////

			// Flatten tree for conll format output...

		}

	}

	// ner
	public void getNer(Document reconcileDoc, ArrayList<MySentence> sentList) {
/*
		AnnotationSet allTk = reconcileDoc.getAnnotationSet(Constants.TOKEN);
		AnnotationSet allNe = reconcileDoc.getAnnotationSet(Constants.NE);
		AnnotationSet allMents = reconcileDoc.getAnnotationSet(Constants.NP);
		
		for (int i = 0; i < sentList.size(); i++) {
			MySentence curSent = sentList.get(i);

			Annotation sent = curSent.reconSent;
			ArrayList<MyChunk<String>> nechunks = new ArrayList<MyChunk<String>>();
			AnnotationSet neInSent = allNe.getContained(sent);
			for (Annotation ne : neInSent.getOrderedAnnots()) {

				String neLabel = standardizeNERLabels(ne.getType()); // NER label
				Annotation neMen = allMents.getByOffset(ne.getStartOffset(), ne.getEndOffset());
				if (neMen == null) {
					throw new RuntimeException("no such NE mention for offset (" + ne.getStartOffset() + ","  +ne.getEndOffset()+")");
				}
				String menTyp = neMen.getAttribute(Constants.NP_TYPE);
				if (menTyp == null) {
					throw new RuntimeException("NE conrresponding mention has no type");
				}
				String label =  (neLabel + "-" + menTyp);
				AnnotationSet tksInNe = allTk.getContained(ne);

				int starttk = -1, endtk = -1;
				if (tksInNe.size() == 1) {
					String tlne = ("(" + label + ")");
					for (Annotation tk : tksInNe.getOrderedAnnots()) {
						int idx = tk.getId();
						TokenLine tline = curSent.tlineIndexing.get(idx);
						tline.ner = tlne;
						starttk = tline.tokenId;
						endtk = tline.tokenId + 1;
					}
				} else if (tksInNe.size() > 1) {
					int cnt = 0;
					int total = tksInNe.size();
					for (Annotation tk : tksInNe.getOrderedAnnots()) {
						cnt++;
						int idx = tk.getId();
						TokenLine tline = curSent.tlineIndexing.get(idx);
						if (cnt == 1) { // first
							tline.ner = "(" + label + "*";
							starttk = tline.tokenId;
						} else if (cnt == total) { // last
							tline.ner = "*)";
							endtk = tline.tokenId + 1;
						} else {
							tline.ner = "*";
						}
					}
				} else {
					String neStr = reconcileDoc.getAnnotString(ne);
					System.out.println("NE(" + ne.getStartOffset() + "," + ne.getEndOffset() + "):" + neStr);
					System.out.println(reconcileDoc.getAbsolutePath());
					throw new RuntimeException("No token in NE!");
				}

				MyChunk<String> nechunk = new MyChunk<String>(starttk, endtk);
				nechunk.setLabel(label);
				nechunks.add(nechunk);
			}

			curSent.neChunk = nechunks;
		}
*/
	}

/*
	// speaker (mainly for discussion forum)
	public void getSpeaker(Document reconcileDoc, ArrayList<MySentence> sentList) {
		
		AnnotationSet allSegs = reconcileDoc.getAnnotationSet(Constants.SEGM);
		
		for (int i = 0; i < sentList.size(); i++) {
			MySentence curSent = sentList.get(i);

			Annotation sent = curSent.reconSent;
			String spkr = "-";
			
			for (Annotation seg : allSegs) {
				if (seg.getType().equals("post") && seg.covers(sent)) { // found the covered sentence
					spkr = "_" + seg.getAttribute(SpeakerEdlDiscussionForum.POST_AUTHOR).replaceAll(" ", "_") + "_";
					break;
				}
			}
			
			for (int j = 0; j < curSent.tokenList.size(); j++) {
				TokenLine tline = curSent.tokenList.get(j);
				tline.speaker = spkr;
			}
		}

	}
*/
	
	// maybe coref mentions? gold or predict mentions
	public void getCorefAndMentions(Document reconcileDoc, ArrayList<MySentence> sentList, boolean useGold) {

		HashMap<Integer, HashSet<Integer>> beginTokenOfMention = new HashMap<Integer, HashSet<Integer>>();
		HashMap<Integer, HashSet<Integer>> endTokenOfMention = new HashMap<Integer, HashSet<Integer>>();
		HashMap<Integer, HashSet<Integer>> begendTokenOfMention = new HashMap<Integer, HashSet<Integer>>();
		HashMap<Integer, Annotation> menidMap = new HashMap<Integer, Annotation>();

		// predict mentions or gold mentions?
		AnnotationSet mens = reconcileDoc.getAnnotationSet(Constants.NP);
		//if (useGold) {
		//	mens = reconcileDoc.getAnnotationSet(Constants.GS_NP);
		//}

		for (Annotation men : mens.getOrderedAnnots()) {
			Integer mid = Integer.parseInt(men.getAttribute(Constants.CE_ID));
			Integer cid = Integer.parseInt(men.getAttribute(Constants.CLUSTER_ID));
			//if (cid == null) {
			//	int tmpCID = mid + 5000;
			//	System.out.println("Mentoin[" + mid + "]'s tmpCID = " + tmpCID);
			//	men.setAttribute(Constants.CLUSTER_ID, String.valueOf(tmpCID));
			//}
			menidMap.put(mid.intValue(), men);
		}

		// String docName = doc.getAbsolutePath();
		// writer.println("#begin document (" + docName + ")");

		int last_end = -1;
		int next_start = Integer.MAX_VALUE;

		ArrayList<Annotation> tkolist = new ArrayList<Annotation>();
		ArrayList<TokenLine> lines = new ArrayList<TokenLine>();
		for (int i2 = 0; i2 < sentList.size(); i2++) {
			MySentence curSent = sentList.get(i2);
			for (int j2 = 0; j2 < curSent.tokenList.size(); j2++) {
				TokenLine tline = curSent.tokenList.get(j2);
				Annotation token = tline.reconToken;
				tkolist.add(token);
				lines.add(tline);
			}
		}


		TokenLine[] lineArr = lines.toArray(new TokenLine[0]);
		Annotation[] tokenArr = tkolist.toArray(new Annotation[0]);
		
		// compute beg and end token ids
		computeBeginEndTokenIds(tokenArr, mens, reconcileDoc);

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

/*
			for (Annotation men : mens.getOrderedAnnots()) {
				if (men.getStartOffset() == tt.getStartOffset()
						|| (men.getStartOffset() <= tt.getStartOffset() && men.getStartOffset() > last_end)) {
					mids = Integer.parseInt(men.getAttribute(Constants.CE_ID));
					cids = Integer.parseInt(men.getAttribute(Constants.CLUSTER_ID));
					// token j is the starting token of mention mids
					beginFromThisToken.add(mids);
					mentionClusterID.put(mids, cids);
					men.setAttribute("BeginTokenID", String.valueOf(tt.getId()));
				}
			}
			Annotation reverseArr[] = (Annotation[]) mens.getOrderedAnnots().toArray(new Annotation[0]);
			for (int r = reverseArr.length - 1; r >= 0; r--) {
				Annotation men = reverseArr[r];
				if (men.getEndOffset() == tt.getEndOffset()
						|| (men.getEndOffset() >= tt.getEndOffset() && men.getEndOffset() < next_start)) {
					mide = Integer.parseInt(men.getAttribute(Constants.CE_ID));
					cide = Integer.parseInt(men.getAttribute(Constants.CLUSTER_ID));
					// token j is the ending token of mention mide
					endFromThisToken.add(mide);
					mentionClusterID.put(mide, cide);
					men.setAttribute("EndTokenID", String.valueOf(tt.getId()));
				}
			}
*/
			for (Annotation men : mens.getOrderedAnnots()) {
				//if (men.getStartOffset() == tt.getStartOffset()
				//		|| (men.getStartOffset() <= tt.getStartOffset() && men.getStartOffset() > last_end)) {
				int btkid = Integer.parseInt(men.getAttribute("BeginTokenID"));
				if (btkid == tt.getId()) {
					mids = Integer.parseInt(men.getAttribute(Constants.CE_ID));
					cids = Integer.parseInt(men.getAttribute(Constants.CLUSTER_ID));
					// token j is the starting token of mention mids
					beginFromThisToken.add(mids);
					mentionClusterID.put(mids, cids);
					//men.setAttribute("BeginTokenID", String.valueOf(tt.getId()));
				}
			}
			Annotation reverseArr[] = (Annotation[]) mens.getOrderedAnnots().toArray(new Annotation[0]);
			for (int r = reverseArr.length - 1; r >= 0; r--) {
				Annotation men = reverseArr[r];
				//if (men.getEndOffset() == tt.getEndOffset()
				//		|| (men.getEndOffset() >= tt.getEndOffset() && men.getEndOffset() < next_start)) {
				int etkid = Integer.parseInt(men.getAttribute("EndTokenID"));
				if (etkid == tt.getId()) {
					mide = Integer.parseInt(men.getAttribute(Constants.CE_ID));
					cide = Integer.parseInt(men.getAttribute(Constants.CLUSTER_ID));
					// token j is the ending token of mention mide
					endFromThisToken.add(mide);
					mentionClusterID.put(mide, cide);
					//men.setAttribute("EndTokenID", String.valueOf(tt.getId()));
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
				// int mentionCID = beginMentionID;
				sb2.append("(" + String.valueOf(mentionCID));
			}
			for (Integer beginendMID : finalBeginEndFromThisToken) {
				if (sb2.length() > 0) {
					sb2.append("|");
				}
				int mentionCID = mentionClusterID.get(beginendMID);
				// int mentionCID = beginendMID;
				sb2.append("(" + String.valueOf(mentionCID) + ")");
			}
			for (Integer endMentionID : finalEndFromThisToken) {
				if (sb2.length() > 0) {
					sb2.append("|");
				}
				int mentionCID = mentionClusterID.get(endMentionID);
				// int mentionCID = endMentionID;
				sb2.append(String.valueOf(mentionCID) + ")");
			}
			if (sb2.length() == 0) {
				sb2.append("-");
			}

			// set the coref str as a token annotation attribute
			tt.setAttribute(Constants.CONLL_COREF, sb2.toString());
			lineArr[j].coref = sb2.toString(); // coref

			// Output mention according to tokens
			// String tokenStr = doc.getAnnotString(tt);
			// tt.setAttribute(Constants.CONLL_WORDSTR, tokenStr);

			// StringBuilder finalSb = new StringBuilder();
			/// finalSb.append(docName + "\t");
			// finalSb.append("0\t");

			// finalSb.append((j+1) + "\t"); // token id must start from 1, NOT
			// 0!
			// finalSb.append(tokenStr + "\t");

			// finalSb.append(sb2.toString());
			// System.out.println(finalSb.toString());
			// writer.println(finalSb.toString());

		}
		
		
		for (Annotation men : mens.getOrderedAnnots()) {
			int beg1 = Integer.parseInt(men.getAttribute("BeginTokenID"));
			int end1 = Integer.parseInt(men.getAttribute("EndTokenID"));
			System.out.println(men.getId() + " " + reconcileDoc.getAnnotString(men) + " " + beg1 + " - " + end1);
		}
		
		// writer.println("#end document");
		// writer.flush();

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
					System.out.println((j + 1) + "\t" + mstr + "\t" + corefstr);
				}
				Annotation errorMention = menidMap.get(menid);
				String menStr = errorMention.getSpanString(reconcileDoc);
				throw new RuntimeException("Mention [" + menStr + "] was not closed: " + btk + " " + etk);
			}
		}
		System.out.println("Document " + reconcileDoc.getAbsolutePath() + " coref result passed the checking.");
		////////////////////////////////////////////////////////////

		//// Coref Chunks ////////////////
		// AnnotationSet sentSet =
		//// reconcileDoc.getAnnotationSet(Constants.SENT);
		for (int i2 = 0; i2 < sentList.size(); i2++) {
			MySentence curSent = sentList.get(i2);
			ArrayList<MyChunk<Integer>> corefChunks = new ArrayList<MyChunk<Integer>>();
			Annotation reconSentence = curSent.reconSent;
			AnnotationSet sentMents = mens.getContained(reconSentence);
			for (Annotation ce : sentMents.getOrderedAnnots()) {

				String idstr1 = ce.getAttribute("BeginTokenID");
				String idstr2 = ce.getAttribute("EndTokenID");
				if (idstr1 == null || idstr2 == null) {
					throw new RuntimeException(idstr1 + " " + idstr2);
				}
				int startTkgId = Integer.parseInt(idstr1);
				int endTkgId = Integer.parseInt(idstr2);
				int startTkId = curSent.tlineIndexing.get(startTkgId).tokenId;
				int endTkId = curSent.tlineIndexing.get(endTkgId).tokenId + 1; // [beg,
																				// end
																				// )
				MyChunk<Integer> corefchunk = new MyChunk<Integer>(startTkId, endTkId);
				int cid = Integer.parseInt(ce.getAttribute(Constants.CLUSTER_ID));
				corefchunk.setLabel(cid);

				corefChunks.add(corefchunk);
			}
			curSent.menChunk = corefChunks;
		}
		//////////////////////////////////

	}
	
	public void computeBeginEndTokenIds(Annotation[] tokenArr, AnnotationSet mentions, Document reconDoc) {
		AnnotationSet tempTokens = new AnnotationSet("temp_tk");
		for (Annotation tk : tokenArr) {
			tempTokens.add(tk);
		}
		
		///////////////////////////////////////
		for (Annotation men : mentions) {
			AnnotationSet tkSet = tempTokens.getContained(men);
			if (tkSet == null || tkSet.size() == 0) {
				throw new RuntimeException("Mention " + men.getId() + " does not have corresponding tokens!");
			}

			Annotation begtk = tkSet.getFirst();
			Annotation endtk = tkSet.getLast();
			men.setAttribute("BeginTokenID", String.valueOf(begtk.getId()));
			men.setAttribute("EndTokenID", String.valueOf(endtk.getId()));
			
			String menStr = reconDoc.getAnnotString(men);
			String begTkStr = reconDoc.getAnnotString(begtk);;
			String endTkStr = reconDoc.getAnnotString(endtk);;
			//System.out.println(menStr + ", " + begTkStr + " - " + endTkStr);
		}
	}

	// About output ///////////////////////////////////////////////////////

	public AnnotationSet constructConllAnnotationSet(ArrayList<DocPart> dpartList, String dName) {

		int partIndex = -1, lineCnt = -1;
		int partCnt;
		// String line;
		AnnotationSet annoSet = new AnnotationSet(Constants.CONLL_ANNO);

		lineCnt = 0;
		for (partIndex = 0; partIndex < dpartList.size(); partIndex++) {

			String docPartTag = new String("DocPart" + String.valueOf(partIndex));
			ArrayList<MySentence> sentList = dpartList.get(partIndex).sentList;

			for (int i = 0; i < sentList.size(); i++) {
				MySentence curSent = sentList.get(i);
				for (int j = 0; j < curSent.tokenList.size(); j++) {

					TokenLine tline = curSent.tokenList.get(j);

					Annotation anno = new Annotation(lineCnt, -1, -1, docPartTag);
					anno.setAttribute("ID", String.valueOf(lineCnt));

					// Conll_DocID
					anno.setAttribute(Constants.CONLL_DOCID, tline.docID);
					// Conll_PartNumber
					anno.setAttribute(Constants.CONLL_PARTNUM, String.valueOf(partIndex));
					// Conll_WordNumber
					anno.setAttribute(Constants.CONLL_WORDNUM, String.valueOf(tline.tokenId));
					// Conll_WordItself
					anno.setAttribute(Constants.CONLL_WORDSTR, tline.token);
					
					//anno.setAttribute(Constants.CONLL_STARTOFF, String.valueOf(tline.startOff));
					//anno.setAttribute(Constants.CONLL_ENDOFF, String.valueOf(tline.endOff));
					
					
					// Conll_PartofSpeech
					anno.setAttribute(Constants.CONLL_POS, tline.pos);
					// Conll_ParseBit
					anno.setAttribute(Constants.CONLL_PARSEBIT, tline.parse);
					// Conll_PredicateLemma
					anno.setAttribute(Constants.CONLL_LEMMA, "-");
					// Conll_PredicateFramesetID
					anno.setAttribute(Constants.CONLL_FRAMESET, "-");
					// Conll_WordSense
					anno.setAttribute(Constants.CONLL_WORD_SENSE, "-");
					// Conll_SpeakerAuthor
					anno.setAttribute(Constants.CONLL_SPEAKER, tline.speaker);
					// Conll_NamedEntities
					anno.setAttribute(Constants.CONLL_NE, tline.ner);
					// Conll_PredicateArguments
					String predArg = new String("-");
					anno.setAttribute(Constants.CONLL_PREDICT_ARG, predArg);
					// Conll_Coreference
					anno.setAttribute(Constants.CONLL_COREF, tline.coref);

					annoSet.add(anno);
					lineCnt++;
				}
			}
		}

		partCnt = dpartList.size();

		// number of parts
		Annotation partanno = new Annotation(lineCnt, -1, -1, "TotalPartNum");
		partanno.setAttribute("PartNum", String.valueOf(partCnt)); // number of
																	// parts of
																	// this
																	// document
		annoSet.add(partanno);
		lineCnt++;

		System.out.println("Number of parts: " + partCnt);

		// official name of the doc
		Annotation docNameAnno = new Annotation(lineCnt, -1, -1, "OfficialDocName");
		docNameAnno.setAttribute("DocName", dName); // number of parts of this
													// document
		annoSet.add(docNameAnno);
		lineCnt++;

		System.out.println("official Conll Docname: " + dName);
		return annoSet;
	}

	public void writeConllFileDocPart(ArrayList<DocPart> dpartList, String fileName) {

	}

	public void writeConllFileAnnoSet(AnnotationSet conllAnnoSet) {

	}

	public static void writeConllFormatPartBerkeleyACE(AnnotationSet conllSet, PrintWriter outWriter, int part,
			String docName) {
		int lastTokenID = -1;
		int curTokenID = 0;

		System.out.println("Writing document " + docName + " part " + part);
		// write head begin
		// outWriter.printf("%s%03d\n", ("#begin document " + "(" + docName +
		// "); part "), part);
		outWriter.printf("%s%03d\n", ("#begin document " + "(" + docName + "); part "), 0);

		String partName = new String("DocPart" + String.valueOf(part));
		AnnotationSet partSet = conllSet.get(partName);
		if (partSet == null) {throw new RuntimeException("partSet null!");}
		for (Annotation anno : partSet.getOrderedAnnots()) {

			// Conll_DocID
			String docid = anno.getAttribute(Constants.CONLL_DOCID);
			// Conll_PartNumber
			String partid = anno.getAttribute(Constants.CONLL_PARTNUM);
			// Conll_WordNumber
			String wordid = anno.getAttribute(Constants.CONLL_WORDNUM);
			curTokenID = Integer.parseInt(wordid);
			// Conll_WordItself
			String wordstr = anno.getAttribute(Constants.CONLL_WORDSTR);
			// Conll_PartofSpeech
			String pos = anno.getAttribute(Constants.CONLL_POS);
			// Conll_ParseBit
			String parsebit = anno.getAttribute(Constants.CONLL_PARSEBIT);
			// Conll_PredicateLemma
			String lemma = anno.getAttribute(Constants.CONLL_LEMMA);
			// Conll_PredicateFramesetID
			String frameid = anno.getAttribute(Constants.CONLL_FRAMESET);
			// Conll_WordSense
			String sense = anno.getAttribute(Constants.CONLL_WORD_SENSE);
			// Conll_SpeakerAuthor
			String speeker = anno.getAttribute(Constants.CONLL_SPEAKER);
			// Conll_NamedEntities
			String ne = anno.getAttribute(Constants.CONLL_NE);
			// Conll_PredicateArguments
			String predArg = anno.getAttribute(Constants.CONLL_PREDICT_ARG);
			predArg = predArg.replace("_", "\t");
			// Conll_Coreference
			String coref = anno.getAttribute(Constants.CONLL_COREF);

			// write into the file
			/// =========================== ///
			if (curTokenID <= lastTokenID) {
				outWriter.println(""); // a new sentence, output an extra "\n"
			}
			outWriter.print(docid + "\t");
			outWriter.print(partid + "\t");
			outWriter.print(wordid + "\t");
			outWriter.print(wordstr + "\t");
			outWriter.print(pos + "\t");
			outWriter.print(parsebit + "\t");
			outWriter.print(lemma + "\t");
			outWriter.print(frameid + "\t");
			outWriter.print(sense + "\t");
			outWriter.print(speeker + "\t");
			outWriter.print(ne + "\t");
			// outWriter.print(predArg + "\t");
			outWriter.print(coref);
			outWriter.println("");
			/// =========================== ///
			lastTokenID = curTokenID;
		}

		// write file end
		outWriter.println("\n#end document");
		outWriter.flush();
	}

	////////////////////////////////////////////////////////////////

	public BerkeleyCompatibleConllDoc constructBerkeleyConllDoc(ConllDocument conllDoc, Document reconDoc,
			DocPart docPart) {

		BerkeleyCompatibleConllDoc berkcdoc = new BerkeleyCompatibleConllDoc();

		// init variables
		berkcdoc.initAllValues();

		// name, part?
		berkcdoc.docName = conllDoc.getDocName();
		berkcdoc.docPartID = docPart.partIndex;

		// all other annotations
		ArrayList<MySentence> mysentences = docPart.sentList;
		for (int i = 0; i < mysentences.size(); i++) {
			MySentence mys = mysentences.get(i);
			// tokens, pos, speakers
			ArrayList<String> sentToken = new ArrayList<String>();
			ArrayList<String> sentPos = new ArrayList<String>();
			ArrayList<String> sentSpeaker = new ArrayList<String>();
			ArrayList<String> sentOffsets = new ArrayList<String>();
			
			for (TokenLine tl : mys.tokenList) {
				sentToken.add(tl.token);
				sentPos.add(tl.pos);
				sentSpeaker.add(tl.speaker);
				sentOffsets.add(tl.startOff + "-" + tl.endOff);
			}
			berkcdoc.spans.add(sentToken);
			berkcdoc.pos.add(sentPos);
			berkcdoc.speakers.add(sentSpeaker);
			berkcdoc.tokOffsets.add(sentOffsets);

			// parse tree
			AnnotationSet depSet = reconDoc.getAnnotationSet(Constants.DEP);
			BerkeleyDepTree deptree = depParseTree(mys, depSet);

			berkcdoc.dtrees.add(deptree);

			// chunks
			berkcdoc.nerChunks.add(mys.neChunk);
			berkcdoc.corefChunks.add(mys.menChunk);

		}

		// coref mentions (gold, or predict)
		AnnotationSet goldMens = reconDoc.getAnnotationSet(Constants.GS_NP);
		if (goldMens == null) {
			goldMens = reconDoc.getAnnotationSet(Constants.NP);
		}
		berkcdoc.goldMentions = goldMens; // reconDoc.getAnnotationSet(Constants.GS_NP);
		berkcdoc.predictMentions = reconDoc.getAnnotationSet(Constants.NP);

		// return
		docPart.berkDoc = berkcdoc;
		System.out.println("Done converting a berkeley compitable document for " + berkcdoc.docName + "!");
		return berkcdoc;
	}

	public BerkeleyDepTree depParseTree(MySentence mysent, AnnotationSet depSet) {

		// words, pos
		ArrayList<String> w = new ArrayList<String>();
		ArrayList<String> poses = new ArrayList<String>();
		for (TokenLine tl : mysent.tokenList) {
			w.add(tl.token);
			poses.add(tl.pos);
		}

		// dep map
		HashMap<Integer, Integer> tokenIdMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> depMap = new HashMap<Integer, Integer>();
		AnnotationSet sentDeps = depSet.getContained(mysent.reconSent); // deps
																		// in
																		// this
																		// sentence
		ArrayList<Annotation> sentTokens = new ArrayList<Annotation>();
		for (int i = 0; i < mysent.tokenList.size(); i++) {
			sentTokens.add(mysent.tokenList.get(i).reconToken);
			tokenIdMap.put(mysent.tokenList.get(i).reconToken.getId(), mysent.tokenList.get(i).tokenId);
		}
		ArrayList<Annotation> dep = new ArrayList<Annotation>();
		List<Annotation> depList = sentDeps.getOrderedAnnots();
		HashMap<Integer, Integer> depIdMap = new HashMap<Integer, Integer>();

		for (int j2 = 0; j2 < depList.size(); j2++) {
			Annotation depAnno = depList.get(j2);
			depIdMap.put(depAnno.getId(), j2);
			if (depAnno.getStartOffset() != sentTokens.get(j2).getStartOffset()
					|| depAnno.getEndOffset() != sentTokens.get(j2).getEndOffset()) {
				throw new RuntimeException("dep offset inequal!");
			}
		}
		if (depList.size() != sentTokens.size()) {
			throw new RuntimeException(
					"depList.size() != sentTokens.size(): " + depList.size() + "!=" + sentTokens.size());
		}
		for (int j = 0; j < depList.size(); j++) {
			Annotation depAnno = depList.get(j);
			String parentStr = depAnno.getAttribute("GOV_ID");

			// String childStr = depAnno.getAttribute("CHILD_IDS");
			// ArrayList<Integer> childs = new ArrayList<Integer>();
			// if (childStr == null) {
			// leaf
			// } else {
			// childs = parseChildrenID(childStr);
			// }

			if (parentStr != null) { // non-root
				int parentId = Integer.parseInt(parentStr);
				int head = depIdMap.get(parentId);
				int tail = j;
				depMap.put(tail, head);
			} else { // root
				int head = -1; // no parent
				int tail = j;
				depMap.put(tail, head);
			}
		}

		if (depMap.keySet().size() != sentTokens.size()) {
			throw new RuntimeException("depMap.keySet().size() != sentTokens.size(): " + depMap.keySet().size() + "!="
					+ sentTokens.size());
		}

		// parse tree
		// construction
		BerkeleyDepTree bdtr = new BerkeleyDepTree(mysent.ptrees, w, poses, depMap);

		return bdtr;
	}

	private ArrayList<Integer> parseChildrenID(String childID) {
		ArrayList<Integer> children = new ArrayList<Integer>();
		String[] cstr = childID.split(",");
		for (String c : cstr) {
			children.add(Integer.parseInt(c));
		}
		return children;
	}

	//////////////

	// utils
	public static String standardizeNERLabels(String reconNElabel) {
		// ("CARDINAL", "DATE", "EVENT", "FAC", "GPE", "LANGUAGE", "LAW", "LOC",
		// "MONEY",
		// "NORP", "ORDINAL", "ORG", "PERCENT", "PERSON", "PRODUCT", "QUANTITY",
		// "TIME", "WORK_OF_ART")
		String entityType = reconNElabel;// "????";
		if (reconNElabel.equals("ORGANIZATION")) {
			entityType = "ORG";
		} else if (entityType.equals("LOCATION")) {
			entityType = "GPE";
		} else if (entityType.equals("PERSON")) {
			entityType = "PER";
		} else if (entityType.equals("VEHICLE")) {
			entityType = "VEH";
		}
		return entityType;
	}

}
