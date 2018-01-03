
package edu.oregonstate.nlp.coref.conll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureExtractor.InternalAnnotator;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.Utils;
import edu.oregonstate.nlp.coref.inout.DocPart;
import edu.oregonstate.nlp.coref.inout.MySentence;
import edu.oregonstate.nlp.coref.inout.TokenLine;

public class OntoNotesConllReader extends InternalAnnotator {
	
	public static final String OFFICIAL_DOCNAME = "OfficialDocName";
	public static final String TOTAL_PARTNUM = "TotalPartNum";
	
	private OntoNotesConllReader() {
	}
	
	private static ConllComment commentStandardize(String comment) {
		String orgcom = new String(comment);
		comment = comment.replace("#", "");
		comment = comment.replace(";", " ; ");

		String[] arr1 = comment.split("\\s+");
		String comment2 = new String("");
		for (String tok : arr1) {
			//if (tok.equals(";")) {
			//	tok = new String(" ");
			//}
			if (comment2.length() != 0) {
				comment2 += "@";
			}
			comment2 += tok;
		}

		ConllComment comStruct = new ConllComment();
		comStruct.originComment = orgcom;
		String[] statems = comment2.split(";");
		for (String stam : statems) {
			String[] words = stam.split("@");
			if (words[0].equals("begin") && words[1].equals("document")) {
				String dname = "";
				dname = words[2].replace("(", "");
				dname = dname.replace(")", "");
				System.out.println("Doc name:" + dname);
				comStruct.docID = dname;
				comStruct.isBegin = true;
			} else if (words[0].equals("end") && words[1].equals("document")) {
				comStruct.isEnd = true;
			} else if (words[0].equals("part")) {
				int partnum = Integer.parseInt(words[1]);
				comStruct.partNum = partnum;
			}
		}
		
		System.out.println("Comment Standrd:" + comment2);
		return comStruct;
	}
	
	public void run(Document doc, String[] annSetNames)
	{
		/*
		String line;
		//boolean ontonotes = Utils.getConfig().getString("DATASET").equals("ontonotes") ? true : false;
		boolean ontonotes = true;
		
		String inputFile = doc.getAbsolutePath() + Utils.SEPARATOR + prefix + "conll";
		
		//conll = new AnnotationSet(Constants.CONLL_ANNO);
		conll = extractConllAnnotationSet(inputFile, Constants.CONLL_ANNO);

		// write on the disk and add it into the doc
		doc.writeAnnotationSet(conll); 
		*/
		
		throw new RuntimeException("Not implemented ...");
	}
	
	public static AnnotationSet extractConllAnnotationSet(String inputFile, String annoName) {
		String line;
		FileReader reader;
		BufferedReader br;
		AnnotationSet annoSet = null;
		
		try {
			reader = new FileReader(inputFile);
			br = new BufferedReader(reader);
			annoSet = new AnnotationSet(annoName);
			
			int partCnt = 0;
			int lineCnt = 0;
			boolean inDocFlag = false;
			ConllComment comStruct = null;
			String officialDname = "";
			
			while ((line = br.readLine()) != null) {
				
				if (line.startsWith("#")) {

					System.out.println("Comment line:" + line);
					comStruct = commentStandardize(line);
					if (comStruct.isBegin) {
						officialDname = comStruct.docID;
						inDocFlag = true;
						partCnt++;
					} else if (comStruct.isEnd) {
						inDocFlag = false;
					}
					
				} else {
					
					String docPartTag = new String("DocPart"+String.valueOf(partCnt - 1));
					
					if (inDocFlag) {
					
						String[] itemsInLine = line.split("\\s+");
						if (line.isEmpty() || line.trim().equals("") || line.trim().equals("\n")) { // empty line
							
						} else {
							/*System.out.print("Table line:");
							for (String item : itemsInLine) {
								System.out.print(item);
							}
							System.out.println();*/
							//Annotation anno = new Annotation(lineCnt, -1, -1, "CoNLLToken");
							Annotation anno = new Annotation(lineCnt, -1, -1, docPartTag);
							anno.setAttribute("ID", String.valueOf(lineCnt));
							
							
							//Conll_DocID
							anno.setAttribute(Constants.CONLL_DOCID, itemsInLine[0]);
							
							//Conll_PartNumber
							anno.setAttribute(Constants.CONLL_PARTNUM, itemsInLine[1]);
							
							//Conll_WordNumber
							anno.setAttribute(Constants.CONLL_WORDNUM, itemsInLine[2]);
							
							//Conll_WordItself
							anno.setAttribute(Constants.CONLL_WORDSTR, itemsInLine[3]);
							
							//Conll_PartofSpeech
							anno.setAttribute(Constants.CONLL_POS, itemsInLine[4]);
							
							//Conll_ParseBit
							anno.setAttribute(Constants.CONLL_PARSEBIT, itemsInLine[5]);
							
							//Conll_PredicateLemma
							anno.setAttribute(Constants.CONLL_LEMMA, itemsInLine[6]);
							
							//Conll_PredicateFramesetID
							anno.setAttribute(Constants.CONLL_FRAMESET, itemsInLine[7]);
							
							//Conll_WordSense
							anno.setAttribute(Constants.CONLL_WORD_SENSE, itemsInLine[8]);
							
							//Conll_SpeakerAuthor
							anno.setAttribute(Constants.CONLL_SPEAKER, itemsInLine[9]);
							
							//Conll_NamedEntities
							anno.setAttribute(Constants.CONLL_NE, itemsInLine[10]);
							
							//Conll_PredicateArguments
							String predArg = new String("");
							for (int k = 11; k < (itemsInLine.length - 1); k++) {
								if (predArg.length() != 0) {
									predArg += "_";
								} predArg += itemsInLine[k];
							}
							anno.setAttribute(Constants.CONLL_PREDICT_ARG, predArg);
							
							//Conll_Coreference
							anno.setAttribute(Constants.CONLL_COREF, itemsInLine[itemsInLine.length - 1]);
							
							
							annoSet.add(anno);
							lineCnt++;
							
						}
					}
					
				}
				
			}
			
			
			// number of parts
			Annotation partanno = new Annotation(lineCnt, -1, -1, TOTAL_PARTNUM);
			partanno.setAttribute("PartNum", String.valueOf(partCnt)); // number of parts of this document
			annoSet.add(partanno);
			lineCnt++;
			
			System.out.println("Number of parts: " + partCnt);
			
			// official name of the doc
			Annotation docNameAnno = new Annotation(lineCnt, -1, -1, OFFICIAL_DOCNAME);
			docNameAnno.setAttribute("DocName", officialDname); // number of parts of this document
			annoSet.add(docNameAnno);
			lineCnt++;
			
			System.out.println("official Conll Docname: " + officialDname);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return annoSet;
	}
	
	
	public static ArrayList<DocPart> constructDocParts(String inputFile, String docName) {

		ArrayList<DocPart> partDocList = new ArrayList<DocPart>();

		////////////////////////////////////////////////////////////

		String line;
		FileReader reader;
		BufferedReader br;
		AnnotationSet annoSet = null;

		try {
			reader = new FileReader(inputFile);
			br = new BufferedReader(reader);

			int partCnt = 0;
			int lineCnt = 0;
			boolean inDocFlag = false;
			ConllComment comStruct = null;
			String officialDname = "";

			ArrayList<ArrayList<MySentence>> sentListLists = new ArrayList<ArrayList<MySentence>>();
			ArrayList<MySentence> sentList = null;//new ArrayList<MySentence>();
			MySentence curSent = new MySentence();

			while ((line = br.readLine()) != null) {

				if (line.startsWith("#")) {

					System.out.println("Comment line:" + line);
					comStruct = commentStandardize(line);
					if (comStruct.isBegin) {
						officialDname = comStruct.docID;
						inDocFlag = true;
						partCnt++;
						sentList = new ArrayList<MySentence>();
					} else if (comStruct.isEnd) {
						inDocFlag = false;
						sentListLists.add(sentList);
					}

				} else {

					String docPartTag = new String("DocPart"+String.valueOf(partCnt - 1));

					if (inDocFlag) {

						String[] itemsInLine = line.split("\\s+");
						if (line.isEmpty() || line.trim().equals("") || line.trim().equals("\n")) { // empty line

							// add to sent list
							if (!curSent.isEmpty()) {
								sentList.add(curSent);
							}
							// ready for next sent
							curSent = new MySentence();

						} else {

							// construct token line ////////////////
							TokenLine tline = new TokenLine();
							tline.index = lineCnt;

							//Conll_DocID
							tline.docID = itemsInLine[0];

							//Conll_PartNumber
							tline.partId = Integer.parseInt(itemsInLine[1]);

							//Conll_WordNumber
							tline.tokenId = Integer.parseInt(itemsInLine[2]);

							//Conll_WordItself
							tline.token = itemsInLine[3];

							//Conll_PartofSpeech
							tline.pos = itemsInLine[4];

							//Conll_ParseBit
							tline.parse = itemsInLine[5];
							
							//Conll_PredicateLemma
							tline.lemma =  itemsInLine[6];  // anno.setAttribute(Constants.CONLL_LEMMA, itemsInLine[6]);

							//Conll_PredicateFramesetID
							tline.frameset =  itemsInLine[7]; //anno.setAttribute(Constants.CONLL_FRAMESET, itemsInLine[7]);

							//Conll_WordSense
							tline.wordsense = itemsInLine[8]; // anno.setAttribute(Constants.CONLL_WORD_SENSE, itemsInLine[8]);

							//Conll_SpeakerAuthor
							tline.speaker = itemsInLine[9]; //anno.setAttribute(Constants.CONLL_SPEAKER, itemsInLine[9]);

							//Conll_NamedEntities
							tline.ner = itemsInLine[10]; // anno.setAttribute(Constants.CONLL_NE, itemsInLine[10]);

							//Conll_PredicateArguments
							StringBuilder predArg = new StringBuilder("");
							for (int k = 11; k < (itemsInLine.length - 1); k++) {
								if (predArg.length() != 0) {
									predArg.append("_");
								} predArg.append(itemsInLine[k]);
							}
							tline.predicates = predArg.toString(); // anno.setAttribute(Constants.CONLL_PREDICT_ARG, predArg);
							
							//Conll_Coreference
							tline.coref = itemsInLine[itemsInLine.length - 1];

							////////////////////////////////////////

							curSent.addTokenLine(tline);
							lineCnt++;
						}
					}

				}

			}


			assert (partCnt == sentListLists.size());
			System.out.println("Number of parts: " + partCnt);

			for (int partID = 0; partID < partCnt; partID++) {
				DocPart partdoc = new DocPart(sentListLists.get(partID), partID);
				computeTextAndTokenOffsets(partdoc);
				partdoc.docName = docName;
				partDocList.add(partdoc);
			}
			
			
			

			System.out.println("official Conll Docname: " + officialDname);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return partDocList;




	}
	
	
	private static void computeTextAndTokenOffsets(DocPart docpart) {
		
		StringBuilder sbtext = new StringBuilder();
		
		
		int startIdx = 0;
		ArrayList<MySentence> sentList = docpart.sentList;
		for (int i = 0; i < sentList.size(); i++) {
			
			MySentence curSent = sentList.get(i);
			
			//int sentId = i;
			curSent.index = i;
			
			ArrayList<TokenLine> tokenList = curSent.tokenList;
			for (int j = 0; j < tokenList.size(); j++) {

				// construct token line ////////////////
				TokenLine tline = tokenList.get(j);
				
				String tk = tline.token;
				
				// token
				tline.startOff = startIdx;
				tline.endOff = startIdx + tk.length();
				
				//System.out.println(tline.startOff + "-"+ tline.endOff + " = " + tline.token);
				//System.out.println(tline.ner);
				//System.out.println(tline.coref);
				
				sbtext.append(tk);
				
				// space
				sbtext.append(" ");
				
				startIdx += (tk.length() + 1);
				////////////////////////////////////////

			}

		}
		
		//System.out.println(sbtext);
		
		docpart.docText = sbtext.toString();
		
	}

	
	public static String getPartTagStr(int partID) {
		String docPartTag = new String("DocPart"+String.valueOf(partID));
		return docPartTag;
	}
	
	public static int getTotalPartNum(AnnotationSet conllSet) {
		if (conllSet == null) {
			return 0;
		}
		int partNum = Integer.parseInt(conllSet.get("TotalPartNum").getFirst().getAttribute("PartNum"));
		return partNum;
	}
	
	public static void writeConllFormatPart(AnnotationSet conllSet, PrintWriter outWriter, int part, String docName) {
		int lastTokenID = -1;
		int curTokenID = 0;
			
		System.out.println("Writing document "+ docName +" part " + part);

		// write head begin
		//outWriter.printf("%s%03d\n", ("#begin document " + "(" + docName + "); part "), part);
		outWriter.printf("%s%03d\n", ("#begin document " + "(" + docName + "); part "), 0);
		
		String partName = new String("DocPart"+String.valueOf(part));
		AnnotationSet partSet = conllSet.get(partName);
		for (Annotation anno : partSet.getOrderedAnnots()) {
			
			//Conll_DocID
			String docid = anno.getAttribute(Constants.CONLL_DOCID);
			
			//Conll_PartNumber
			String partid = anno.getAttribute(Constants.CONLL_PARTNUM);
			
			//Conll_WordNumber
			String wordid = anno.getAttribute(Constants.CONLL_WORDNUM);
			curTokenID = Integer.parseInt(wordid);
			
			//Conll_WordItself
			String wordstr = anno.getAttribute(Constants.CONLL_WORDSTR);
			
			//Conll_PartofSpeech
			String pos = anno.getAttribute(Constants.CONLL_POS);
			
			//Conll_ParseBit
			String parsebit = anno.getAttribute(Constants.CONLL_PARSEBIT);
			
			//Conll_PredicateLemma
			String lemma = anno.getAttribute(Constants.CONLL_LEMMA);
			
			//Conll_PredicateFramesetID
			String frameid = anno.getAttribute(Constants.CONLL_FRAMESET);
			
			//Conll_WordSense
			String sense = anno.getAttribute(Constants.CONLL_WORD_SENSE);
			
			//Conll_SpeakerAuthor
			String speeker = anno.getAttribute(Constants.CONLL_SPEAKER);
			
			//Conll_NamedEntities
			String ne = anno.getAttribute(Constants.CONLL_NE);
			
			//Conll_PredicateArguments
			String predArg = anno.getAttribute(Constants.CONLL_PREDICT_ARG);
			predArg = predArg.replace("_", "\t");
			
			//Conll_Coreference
			String coref = anno.getAttribute(Constants.CONLL_COREF);
			
			// write into the file
			/// =========================== ///
			if (curTokenID <= lastTokenID) {
				// a new sentence, output an extra "\n"
				outWriter.println("");
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
			outWriter.print(predArg + "\t");
			outWriter.print(coref);
			outWriter.println("");
			/// =========================== ///

			lastTokenID = curTokenID;
		}
		
		// write file end
		outWriter.println("\n#end document");
		outWriter.flush();
	}
	
	public static void writeConllFormatPart(AnnotationSet conllSet, String dir, String fname, int part) {
		File out = new File(dir, fname);
		PrintWriter outWriter = null;
		try {
			outWriter = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String docName = (dir+Utils.SEPARATOR+fname);
		writeConllFormatPart(conllSet, outWriter, part, docName);
	}
	
	
	public static void writeConllFormat(AnnotationSet conllSet, String dir, String fname) {
		String docName = (dir + Utils.SEPARATOR + fname);
		writeConllFormat(conllSet, dir, fname, docName);
	}
	public static void writeConllFormat(AnnotationSet conllSet, String dir, String fname, String docName) {
		
		//System.out.println(conllSet.get("TotalPartNum").size());
		
		int partNum = Integer.parseInt(conllSet.get("TotalPartNum").getFirst().getAttribute("PartNum"));
		System.out.println("Number of part: " + partNum);
		
		// writer constructor
		File out = new File(dir, fname);
		PrintWriter outWriter = null;
		try {
			outWriter = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// go through every part of the document
		//String docName = (dir+Utils.SEPARATOR+fname);
		for (int partID = 0; partID < partNum; partID++) {
			writeConllFormatPart(conllSet, outWriter, partID, docName);
			/**
			 * Due the script reason, we output each part with partID "0"
			 * Because otherwise, the script will not output the head "#begin document ..."
			 * for the parts whose ID is greater than 000
			 */
			//writeConllFormatPart(conllSet, outWriter, 0, docName);
		}
	}
}
