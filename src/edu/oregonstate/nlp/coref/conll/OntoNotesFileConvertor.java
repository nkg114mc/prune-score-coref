package edu.oregonstate.nlp.coref.conll;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

import edu.oregonstate.nlp.coref.conll.Parse;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.AnnotationWriterBytespan;
import edu.oregonstate.nlp.coref.featureExtractor.ParserStanfordParser;
import edu.oregonstate.nlp.coref.general.Constants;

public class OntoNotesFileConvertor {

	protected File iParse;	// input Parse File in OntoNotes Format.
	protected File oParse;	// output Parse File in Reconcile Format.
	protected File textFile;	// raw text file.
	protected File posTagFile;
	protected File tokenFile;
//	private File npsFile;
	
	protected File sentFile;
	protected File parFile;
	protected File depFile;
	
	protected HashMap<Integer, Parse> parsedData;
	protected Stack<String> stack;
	protected StringBuffer buffer;
	
	protected TreebankLanguagePack tlp;
	protected GrammaticalStructureFactory gsf;

	/**
	 * @param iParse : is the parse file in ontonotes format
	 * @param oParse : the required parse file in reconcile format
	 * @param textFile : the raw text file whose annotations is in iParse
	 */
	public OntoNotesFileConvertor(File iParse, File oParse, File posTagFile, File tokenFile, File textFile,
			                      File sentFile, File parFile, File depFile) 
	{
		this.iParse = iParse;
		this.oParse = oParse;
		this.posTagFile = posTagFile;
		this.tokenFile = tokenFile;
//		this.npsFile = npsFile;
		this.textFile = textFile;
		parsedData = new HashMap<Integer, Parse>();
		stack = new Stack<String>();
		buffer = new StringBuffer();
		
		this.sentFile = sentFile;
		this.parFile = parFile;
		this.depFile = depFile;
		
		// for dependency extraction
		tlp = new PennTreebankLanguagePack();
		gsf = tlp.grammaticalStructureFactory();
	}

	public void convert() {
		try {
			// clear the parser node id count!
			Parse.clearCount();
			
			String txtData = createBufferFromFile();

			int startIndex = 0;
			BufferedReader reader = new BufferedReader(new FileReader(iParse));

			BufferedWriter posTagWriter = new BufferedWriter(new FileWriter(posTagFile));
			BufferedWriter tokenWriter = new BufferedWriter(new FileWriter(tokenFile));
			
			int postag_count = 0;

			String line;
			while((line = reader.readLine()) != null) {
				
				// TODO this is a hack to fix the problem in ontonotes corpus in which at 
				// times parse file contains "&" instead of "-AMP-", 
				// "<" instead of "-LAB-" and ">" instead of "-RAB-"  
				line = line.replaceAll("&","-AMP-");
				line = line.replaceAll("<", "-LAB-");
				line = line.replaceAll(">", "-RAB-");
				
				if(line.length() == 0)
					continue;

				String word = null;
				String[] strings = createTokens(line);
				for (int i = 0; i < strings.length; i++) {
					String string = strings[i].trim();
					if(string.equals(")")) {

						if(word == null) {
							word = (String) stack.pop();
							int index = txtData.indexOf(word, startIndex);
							startIndex = index + word.length();
							String pos = (String)stack.pop();
							posTagWriter.write(++postag_count + "\t" + index + "," + (index + word.length()) +"\t"+ "string" + "\t" + pos + "\n");
							//tokenWriter.write(postag_count + "\t" + index + "," + (index + word.length()) +"\t"+ "string" + "\ttoken" + "\n");
							tokenWriter.write(postag_count + "\t" + index + "," + (index + word.length()) +"\t"+ "string" + "\ttoken" + "\t" + "Conll_WordItself=\""+word+"\"" + "\n");
							int startoff = index;
							int endoff = index + word.length();
							boolean tokenOK = (startoff >= 0 && endoff >= 0 && endoff > startoff);
							if (!tokenOK) {
								System.err.println(startoff + " " + endoff + " " + word);
							}
							assert(tokenOK);
							Parse p = new Parse(index, index + word.length(), Parse.STRING_TYPE, pos, word);
							parsedData.put(p.getId(), p);
							stack.pop(); 					// would pop "("
							stack.push(String.valueOf(p.getId()));
							continue;
						}

						// If word != null
						ArrayList<Integer> childIds = new ArrayList<Integer>();
						while(!stack.peek().equals("(")) {
							String stacktop = (String) stack.pop();
							if(stack.peek().equals("(")) {
								Parse p = new Parse(0, 0, Parse.STRING_TYPE, stacktop, null);
								int startOffset, endOffset;
								startOffset = endOffset = -1;
								for (Integer childId : childIds) {
									p.addChild(childId);
									Parse childP = parsedData.get(childId);
									childP.setParent(p.getId());
									if(startOffset == -1)
										startOffset = childP.getStartOffset();
									else if(startOffset > childP.getStartOffset())
										startOffset = childP.getStartOffset();

									if(endOffset == -1)
										endOffset = childP.getEndOffset();
									else if(endOffset < childP.getEndOffset())
										endOffset = childP.getEndOffset();
								}
								p.setStartOffset(startOffset);
								p.setEndOffset(endOffset);
								parsedData.put(p.getId(), p);
								stack.pop();		// would pop "("
								stack.push(String.valueOf(p.getId()));
								break;
							}
							else
								childIds.add(Integer.parseInt(stacktop));
						}
						childIds.clear();
					}
					else
						stack.push(string);
				}
			}
			
			// place tokens in the memory
			
			
			// output parse
			writeOutput(parsedData);
			
			// dependency?
			AnnotationSet depSet = ExtractDependency(parsedData);
			writeAnnotationSet(depFile, depSet);

			// clean up
			parsedData.clear();
			stack.clear();
			posTagWriter.flush();
			posTagWriter.close();
			tokenWriter.flush();
			tokenWriter.close();
			


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * writer output file in the Reconcile's Parse file format.
	 * @param parsedData
	 * @throws IOException
	 */
	protected void writeOutput(HashMap<Integer, Parse> parsedData)
	throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(oParse)); 
		
//		BufferedWriter npsWriter = new BufferedWriter(new FileWriter(npsFile));
		int nps_count = 0;

		for (Parse parse : parsedData.values()) {
			
			if(parse.startOffset==-1 || parse.endOffset==-1) {
				System.err.println("offset is -1 in file : " + oParse);
			}
			
			String str = "\t" + parse.startOffset + "," + parse.endOffset + "\t" + parse.type + "\t" + parse.pos + "\t";
			if(parse.childids != null && parse.childids.size() > 0) {
				str += "CHILD_IDS=\"";
				for (int c = 0; c < parse.childids.size(); c++) {
					str += String.valueOf(parse.childids.get(c));
					if(c != parse.childids.size() - 1) {
						str += ",";
					}
					else 
						str += "\" ";
				}
			}
			str += "parent=\"" + parse.parentid + "\"";
			
			writer.write(parse.id + str + "\n");
//			npsWriter.write(++nps_count + str + "\t" + "NO=\"" + (nps_count-1) + "\"\n");
		}
		
		writer.flush();
		writer.close();
//		npsWriter.flush();
//		npsWriter.close();
		
		// add dependency
		//1) load parse tree fristly
	}

	/**
	 * creates a String of all the text read from a file.
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected String createBufferFromFile() throws FileNotFoundException,
	IOException {
		BufferedReader txtReader = new BufferedReader(new FileReader(textFile));
		String line;
		while((line = txtReader.readLine()) != null) {
			buffer.append(line);
			buffer.append("\n");
		}
		String txtFile = buffer.toString();
		
		// clear the buffer.
		buffer.setLength(0);
		
		return txtFile;
	}

	/**
	 * Tokenizes OntoNotes Parse file using spaces and ( ) as delimiters and does not include whitespaces.
	 * E.g. "(TOP (S (NP-SBJ-1 (DT No)" would be converted to tokens:
	 *  		"(","TOP","(","S","(","PP-TMP","(","IN","In",")",")",")",")"
	 * @param line to be tokenized.
	 * @return
	 */
	public String[] createTokens(String line) {
		StringBuffer buffer = new StringBuffer();
		Stack<String> stack = new Stack<String>();
		for(int c = 0; c < line.length(); c++) {
			char ch = line.charAt(c);
			switch(ch) {
			case '(':
				if(buffer.length() > 0)
					stack.push(buffer.toString());
				buffer.setLength(0);
				stack.push("(");
				break;
			case ')':
				if(buffer.length() > 0)
					stack.push(buffer.toString());
				buffer.setLength(0);
				stack.push(")");
				break;
			case ' ':
				if(buffer.length() > 0)
					stack.push(buffer.toString());
				buffer.setLength(0);
				break;
			default: buffer.append(ch);
			}
		}
		return stack.toArray(new String[2]);
	}
	
	
	///////// by Chao Ma
	
	public static void writeAnnotationSet(File f, AnnotationSet set) {
		//File f = new File(getAnnotationDir(), annSetName);
		AnnotationWriterBytespan AnWriter = new AnnotationWriterBytespan();
		System.out.println("Writing AnnotationSet: " + f);

		PrintWriter out;
		try {
			out = new PrintWriter(f);
			AnWriter.write(set, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public AnnotationSet ExtractDependency(HashMap<Integer, Parse> parsedData) {

		AnnotationSet depAnnots = new AnnotationSet(Constants.DEP); // dependency annotation

		int sentCnt = 0;
		for (Parse parse : parsedData.values()) {
			// is this a root? (a root = a sentence)
			if (parse.parentid < 0) { // a new sentence
				Parse troot = parse;
				sentCnt++;
				System.out.println("converting sentence " + sentCnt);

				// toekns
				Annotation[] sentenceTok = getOrderedSentenceToken(troot, parsedData);
				//for (Annotation tk : sentenceTok) {
				//	System.out.print(tk.getStrContent() + " ");
				//} System.out.println("");

				// stanford tree
				edu.stanford.nlp.trees.Tree stTree;
				stTree = constructTree(troot, parsedData);
				//traversalTree("", stTree); // error tree check, for debug

				GrammaticalStructure gs = gsf.newGrammaticalStructure(stTree);
				Collection<TypedDependency> dep = gs.typedDependencies();
				ParserStanfordParser.addDepSpans(dep, sentenceTok, depAnnots);
			}
		}
		return depAnnots;
	}
	
	/**
	 * This class will generate a stanford parsing tree using the conll parsing information
	 * @author Chao Ma
	 * */
	public edu.stanford.nlp.trees.Tree ToStanfordParseTree(HashMap<Integer, Parse> parsedData)
	{
		TreeFactory tf = new LabeledScoredTreeFactory();
		edu.stanford.nlp.trees.Tree stTree = null;

		// go through the tree
		//for (Parse parse : parsedData.values()) {
		//	stTree = constructTree(parse);
		//}
		return stTree;
	}
	/*
	public boolean stanfordParseTreeCheck(edu.stanford.nlp.trees.Tree stfdTree)
	{
		if (!stfdTree.isLeaf()) {
			for (edu.stanford.nlp.trees.Tree childTree : stfdTree.getChildrenAsList()) {
				boolean isok = traversalTree(childTree);
				if (!isok) return false;
			}
		}
		return true;
	}*/
	public static boolean traversalTree(String prefix, edu.stanford.nlp.trees.Tree stfdTree) {
		//if (stfdTree.parent() == null) {
		//	throw new RuntimeException("value " + stfdTree.value());
		//}
		
		//if (!stfdTree.isLeaf()) {
		//	for (edu.stanford.nlp.trees.Tree childTree : stfdTree.getChildrenAsList()) {
		//		boolean isok = traversalTree(childTree);
		//		if (!isok) return false;
		//	}
		//}
		
		System.out.println(prefix + stfdTree.value());// + " "+"("+stfdTree.s + ","+ +")");
		
		if (!stfdTree.isLeaf()) {
			for (edu.stanford.nlp.trees.Tree childTree : stfdTree.getChildrenAsList()) {
				boolean isok = traversalTree(prefix + "  ", childTree);
			}
		}
		
		return true;
	}
	
	private edu.stanford.nlp.trees.Tree constructTree(Parse troot, HashMap<Integer, Parse> parsedData) {
		TreeFactory tf = new LabeledScoredTreeFactory();
		Tree result;

		if (troot.isLeaf()) {
			List<Tree> newChildren = new ArrayList<Tree>();
			Tree leafWord = tf.newLeaf(troot.word);
			newChildren.add(leafWord);
			result = tf.newTreeNode(troot.pos, newChildren);
			assert(troot.word != null);
			assert(troot.pos != null);
			//System.out.println("leaf label: " + "(" + troot.word + "-" + troot.pos + ")");
		} else {
			// sort subtrees
			ArrayList<Parse> children = new ArrayList<Parse>();
			for (Integer chid : troot.childids) { 
				Parse ch = parsedData.get(chid); 
				children.add(ch);
			}
			Collections.sort(children, new ParseComparator());

			// construct subtree
			List<Tree> newChildren = new ArrayList<Tree>();
			//for (Integer childID : troot.childids) {
			//	Parse child = parsedData.get(childID);
			for (Parse child : children) {
				Tree cur = constructTree(child, parsedData);
				newChildren.add(cur);
			}
			result = tf.newTreeNode(troot.pos, newChildren);
			assert(troot.pos != null);
			//System.out.println("node label: " + "(" + troot.word + "-" + troot.pos + ")");
		}
		return result;
	}

	
	
	private Annotation[] getOrderedSentenceToken(Parse troot, HashMap<Integer, Parse> parsedData) {
		// get all leaves
		ArrayList<Parse> allLeaves = findLeaves(troot, parsedData);
		
		// order them
		Collections.sort(allLeaves, new ParseComparator());
		
		// return
		ArrayList<Annotation> sentTok = new ArrayList<Annotation>();
		for (Parse word : allLeaves) {
			Annotation anno = new Annotation(word.id, word.startOffset, word.endOffset, "dependency");
			anno.setStrContent( word.word );
			sentTok.add(anno);
		}
		return sentTok.toArray(new Annotation[0]);
	}
	
	/** parser element comparator */
	public static class ParseComparator implements Comparator<Parse> {
		@Override
		public int compare(Parse p1, Parse p2) {
			  if (p1.getStartOffset() < p2.getStartOffset()) return -1;
			  if (p1.getStartOffset() > p2.getStartOffset()) return 1;

			  // the start offsets are equal
			  if (p1.getEndOffset() < p2.getEndOffset()) return -1;
			  if (p1.getEndOffset() > p2.getEndOffset()) return 1;
			  return 0;
		}
	}
	
	private ArrayList<Parse> findLeaves(Parse troot, HashMap<Integer, Parse> parsedData) {
		if (troot.isLeaf()) {
		    //System.out.println("leaf label: " + t.getLabel());
		    ArrayList<Parse> myleaf = new ArrayList<Parse>();
		    myleaf.add(troot);
		    return myleaf;
		} else {
			ArrayList<Parse> myleaf = new ArrayList<Parse>();
			for (Integer childID : troot.childids) {
				Parse child = parsedData.get(childID);
				ArrayList<Parse> allChildLeave = findLeaves(child, parsedData);
				myleaf.addAll(allChildLeave);
			}
			return myleaf;
		}
	}
}
