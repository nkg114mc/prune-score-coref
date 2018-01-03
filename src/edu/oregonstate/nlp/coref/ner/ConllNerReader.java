package edu.oregonstate.nlp.coref.ner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.oregonstate.nlp.coref.conll.ConllComment;
import edu.oregonstate.nlp.coref.conll.ConllDocument;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.Utils;

/**
 * 
 * A new Conll Document loader that reformed the 2014 version (ConllDocumentLoader)
 * @author machao
 *
 */
public class ConllNerReader {
	
	//private AnnotationSet conll;
	
	public ArrayList<ConllDocument> conllDocList = null;
	public ArrayList<Document> reconDocList = null;
	
	public ArrayList<ConllDocument> getConllList() {
		return conllDocList;
	}
	public ArrayList<Document> getReconList() {
		return reconDocList;
	}
	
	public void clear() { // clear all list
		conllDocList = null;
		reconDocList = null;
	}
	
	// load file from names ...
	public void loadConllDocumentList(List<File> fileNames) {
		conllDocList = new ArrayList<ConllDocument>();
		reconDocList = new ArrayList<Document>();
	}
	
	
	public ConllNerReader() {
		
	}
	
	
	private ConllComment commentStandardize(String comment) {
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
	
/*
	public void run(Document doc, String[] annSetNames)
	{
		//String line;
		//boolean ontonotes = Utils.getConfig().getString("DATASET").equals("ontonotes") ? true : false;
		
		String inputFile = doc.getAbsolutePath() + Utils.SEPARATOR + prefix + "conll";
		conll = extractConllAnnotationSet(inputFile, Constants.CONLL_ANNO);

		// write on the disk and add it into the doc
		doc.writeAnnotationSet(conll); 
	}
*/
	
	
	// main interface
	public ConllDocument readConllDoc(File conllFilePath) {
		// set the folder that contains this conll file as the root dir
		ConllDocument returnConllDoc = new ConllDocument(conllFilePath.getParentFile()); 

		// doc name?
		returnConllDoc.setDocName(conllFilePath.getName());

		// load annotation
		AnnotationSet tmpConllAnnoSet = extractConllAnnotationSet(conllFilePath.getAbsolutePath(), Constants.CONLL_ANNO);
		returnConllDoc.setAnnotationSet(tmpConllAnnoSet);

		// part num
		int nPart = getTotalPartNum(tmpConllAnnoSet);
		returnConllDoc.setPartNum( nPart );
		
		return returnConllDoc;
	}
	// main interface batch
	public ArrayList<ConllDocument> readConllDocBatch(ArrayList<File> fileNames) {
		conllDocList = new ArrayList<ConllDocument>();
		for (File f : fileNames) {
			System.out.println("Reading file: " + f.getAbsolutePath().toString());
			ConllDocument conllDoc = readConllDoc(f);
			conllDocList.add(conllDoc);
			System.out.println("Done file: " + f.getAbsolutePath().toString());
		}
		System.out.println("Reader input " + conllDocList.size() + " in total!");
		return conllDocList;
	}
	
	
	public AnnotationSet extractConllAnnotationSet(String inputFile, String annoName) {
		String line;
		AnnotationSet annoSet = null;
		
		try {
			FileReader reader = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(reader);
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
			Annotation partanno = new Annotation(lineCnt, -1, -1, "TotalPartNum");
			partanno.setAttribute("PartNum", String.valueOf(partCnt)); // number of parts of this document
			annoSet.add(partanno);
			lineCnt++;
			
			System.out.println("Number of parts: " + partCnt);
			
			// official name of the doc
			Annotation docNameAnno = new Annotation(lineCnt, -1, -1, "OfficialDocName");
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
	
	public static String getPartTagStr(int partID) {
		String docPartTag = new String("DocPart"+String.valueOf(partID));
		return docPartTag;
	}
	
	public int getTotalPartNum(AnnotationSet conllSet) {
		if (conllSet == null) {
			return 0;
		}
		int partNum = Integer.parseInt(conllSet.get("TotalPartNum").getFirst().getAttribute("PartNum"));
		return partNum;
	}
	
	public void writeConllFormatPart(AnnotationSet conllSet, PrintWriter outWriter, int part, String docName) {
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
	
	public void writeConllFormatPart(AnnotationSet conllSet, String dir, String fname, int part) {
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
	
	
	public void writeConllFormat(AnnotationSet conllSet, String dir, String fname) {
		String docName = (dir + Utils.SEPARATOR + fname);
		writeConllFormat(conllSet, dir, fname, docName);
	}
	public void writeConllFormat(AnnotationSet conllSet, String dir, String fname, String docName) {
		
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
