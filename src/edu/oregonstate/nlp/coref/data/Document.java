package edu.oregonstate.nlp.coref.data;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.oregonstate.nlp.coref.features.properties.Property;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.mentions.MentionPrecdiction;

import com.google.common.collect.Maps;


public class Document {


	/**
	 * Static member variables and functions	
	 */
	public static final Pattern pLeadingNonWord = Pattern.compile("\\A\\W*");
	public static final Pattern pNonWordSplit = Pattern.compile("\\W+|\\-");
	public static final Pattern pNonWordSplit1 = Pattern.compile("\\s+");


	/**
	 * member variables and functions	
	 */
	protected File mDir;
	protected String mText;
	protected String mDocId;
	protected Map<String, AnnotationSet> mAnnotationSets;
	
	private AnnotationSet predMentionSet = null;
	private AnnotationSet goldMentionSet = null;


	
	//// Constructors

	public Document(File dir) {
		mDir = dir;
		mAnnotationSets = Maps.newHashMap();
		mText = null;
		mDocId = dir.getName();
	}

	public Document(String docID) {
		mDir = null;
		mAnnotationSets = Maps.newHashMap();
		mText = null;
		mDocId = docID;
	}
	
	public Document(String docID, File dir) {
		mDir = dir;
		mAnnotationSets = Maps.newHashMap();
		mText = null;
		mDocId = docID;
	}

	
	public String getDocumentId()
	{
		return mDocId;
	}
	
	public void setDocumentId(String id)
	{
	  mDocId = id;
	}

	/**
	 * add an annotation set to the document directory with the given name. Depending on the <code>write</code> parameter,
	 * the annotation set may be written to disk, or just stored in memory
	 * 
	 * @param set
	 * @param annotationSetName
	 *          This name will be checked against the config file to see if it needs to be mapped to a different name
	 * @param write
	 *          A flag determining whether the annotation set should be written to disk or just cached in memory
	 * @throws IOException
	 */
	public void addAnnotationSet(AnnotationSet set, String annotationSetName, boolean replaceIfExist)
	{
		String annSetName = getCannonicalAnnotationSetName(annotationSetName);
		//System.out.println("Doc.addAnnotationSet: " + annotationSetName);

		mAnnotationSets.put(annSetName, set);
		
		if (annSetName.equals(Constants.NP)) { // set predict mention pointer
			predMentionSet = set;
		} else if (annSetName.equals(Constants.GS_NP)) { // set predict mention pointer
			goldMentionSet = set;
		}
	}
	
	public void addAnnotationSet(AnnotationSet set)
	{
		addAnnotationSet(set, set.getName(), true);
	}


	public AnnotationSet getAnnotationSet(String annotationSetName, boolean needRegenrateIfNotExist)
	{
			//String annSetName = getCannonicalAnnotationSetName(annotationSetName);
			AnnotationSet set = mAnnotationSets.get(annotationSetName);
			/*
			if (set == null) {
				File annFile = new File(getAnnotationDir(), annSetName);
				if (annFile.exists()) {
					// load from file
					FileInputStream in = new FileInputStream(annFile);
					set = AnReader.read(in, annSetName);
					mAnnotationSets.put(annotationSetName, set);
				} else {
					// re-generate if needed
					if (needRegenrateIfNotExist) {
						//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
					} else {
						throw new RuntimeException("Annotation file does not exist: " + annFile.getAbsolutePath() + " " +
								annotationSetName + " CannonicalAnnotationSet: " + getAnnotationDir() + " " + annSetName);
					}
				}
			}
			*/
			return set;
	}

	public AnnotationSet getAnnotationSet(String annotationSetName) {
		AnnotationSet as = getAnnotationSet(annotationSetName, true);
		return as;
	}
	
	
	public void setPredictMentions(AnnotationSet set) {
		String nm = set.getName();
		if (nm.equals(Constants.NP)) {
			addAnnotationSet(set); // ok
		} else {
			throw new RuntimeException("Predict mention set name is incorrect: " + nm + " should be " + Constants.NP);
		}
	}
	
	public void setGoldMentions(AnnotationSet set) {
		String nm = set.getName();
		if (nm.equals(Constants.GS_NP)) {
			addAnnotationSet(set); // ok
		} else {
			throw new RuntimeException("Gold mention set name is incorrect: " + nm + " should be " + Constants.GS_NP);
		}
	}

	public String getText() {
		return mText;
	}
	
	public void setText(String text) {
		mText = text;
	}

	public Set<String> getAnnotationSetNames()
	{
		return mAnnotationSets.keySet();
	}

	/**
	 * Given an annotation set name, check to see if there is a cannonicalization for it, and if so, return that
	 * canonicalization
	 * 
	 * @param annotationSetName
	 * @return
	 */

	public static String getCannonicalAnnotationSetName(String annotationSetName)
	{
		return annotationSetName;
	}

	/// Get the raw string of the annotation
	public String getAnnotString(Annotation a)
	{
		if (a.strContent == null) {
			a.strContent = getAnnotString(a.getStartOffset(), a.getEndOffset());
		}
		return a.strContent;
	}


	/// Get the raw string of the span
	public String getAnnotString(int start, int end)
	{
		String result = null;

		try {
			result = getText().substring(start, end);
		}
		catch (StringIndexOutOfBoundsException siobe) {
			System.out.println("start "+start+" end "+end);
			int st = mText.length() - 20;
			for (int i = st; i < mText.length(); i++) {
				System.err.println(i + ":" + mText.charAt(i));

			}
			throw new RuntimeException(siobe);
		}
		return result;
	}
/*
	public String getAnnotText(Annotation a)
	{
		return getAnnotString(a);
	}

	public String getAnnotText(int start, int end)
	{
		return getAnnotString(start, end);
	}
*/
	/// Get a cleaned-up version of the text in the annotation span
	public String getAnnotText(Annotation a)
	{
		if (a.textContent == null) {
			String result = getAnnotString(a);
			if (result == null) return null;
			result = result.trim().replaceAll("(\\s|\\n)+", " ");
			result = result.replaceAll("\\A[\\s\\\"'`\\[\\(\\-]+", "").replaceAll("\\W+\\z", "");
			a.textContent = result;
		}
		return a.textContent;
	}

	/// Get a cleaned-up version of the text in the span
	public String getAnnotText(int start, int end)
	{
		String result = getAnnotString(start, end);
		if (result == null) return null;
		result = result.trim().replaceAll("(\\s|\\n)+", " ");
		result = result.replaceAll("\\A[\\s\\\"'`\\[\\(\\-]+", "").replaceAll("\\W+\\z", "");
		return result;
	}

	public String[] getWords(Annotation a)
	{
		if (a.words == null) {
			a.words = getWords(getAnnotString(a));
		}
		return a.words;
	}


	public String[] getWords(int start, int end)
	{
		String textSpan = getAnnotString(start, end);
		return getWords(textSpan);
	}


	public void loadAnnotationSets() {
		throw new RuntimeException("Not implemented ...");
	}

	public void loadAnnotationSetsByName(String[] annotSets)
	{
		for (String anSetKey : annotSets) {
			getAnnotationSet(anSetKey);
		}
	}

	public void loadCachedFeatures()
	{
		throw new RuntimeException("Will implement ...");
	}
	
	public AnnotationSet getPredictMentionSet() {
		return predMentionSet;
	}
	
	public AnnotationSet getGoldMentionSet() {
		return goldMentionSet;
	}
	
	
	public boolean existAnnotationSet(String annoName) {
		if (mAnnotationSets.containsKey(annoName)) {
			return true;
		}
		return false;
	}
	
	public Map<String, AnnotationSet> getAllAnnotationSets() {
		return mAnnotationSets;
	}


	// by Chao Ma 2013-2-9 ------------------------------------------

	/** Output coreference result as an long string in the conll format */
	public String goldAnnotationToStr() {

		StringBuilder sb = new StringBuilder();
		MentionPrecdiction corefResultToConll = new MentionPrecdiction();
		int totalTokenCnt = 0;

		// doc begin comment
		sb.append("#begin document " + "(" + mDir.getAbsolutePath() + "); part 000\n");

		corefResultToConll.printCorefMentions(this, true); // it is not only printing, but also align the coref results according to the tokens

		// gold mention annotations
		AnnotationSet standardTk = this.getAnnotationSet(Constants.TOKEN); // no

		int len = standardTk.size();
		for (int i = 0; i < len; i++) {
			Annotation t1 = standardTk.get(totalTokenCnt + 1);
			String tokenStr = t1.getAttribute(Constants.CONLL_WORDSTR);
			String goldCorefAnno = t1.getAttribute(Constants.CONLL_COREF);

			String thisLine = new String((i+1) + "\t" + tokenStr + "\t" + goldCorefAnno + "\n");
			sb.append(thisLine);

			//System.out.print(thisLine);
			totalTokenCnt++;
		}

		// doc end comment
		sb.append("#end document\n");

		return sb.toString();
	}

	/** Output coreference result as an long string in the conll format */
	public String predAnnotationToStr() {

		StringBuilder sb = new StringBuilder();
		MentionPrecdiction corefResultToConll = new MentionPrecdiction();
		int totalTokenCnt = 0;

		// doc begin comment
		sb.append("#begin document " + "(" + mDir.getAbsolutePath() + "); part 000\n");
		//sb.append("\n# DOC " + docName + "\n");

		corefResultToConll.printCorefMentions(this, false); // it is not only printing, but also align the coref results according to the tokens

		// predict mention annotations
		AnnotationSet ourTk = this.getAnnotationSet(Constants.TOKEN);

		int len = ourTk.size();
		for (int i = 0; i < len; i++) {
			Annotation t2 = ourTk.get(i+1);

			String tokenStr = t2.getAttribute(Constants.CONLL_WORDSTR);
			String predCorefAnno = t2.getAttribute(Constants.CONLL_COREF);

			String thisLine = new String((i+1) + "\t" + tokenStr + "\t" + predCorefAnno + "\n");
			sb.append(thisLine);

			//System.out.print(thisLine);
			totalTokenCnt++;
		}

		// doc end comment
		sb.append("#end document\n");

		return sb.toString();
	}


	public String getVisiableCorefResult()
	{
		String wholeText = this.getText();
		System.out.print(wholeText);

		AnnotationSet tokens = this.getAnnotationSet(Constants.TOKEN);
		Annotation tokenArr[] = (Annotation[]) tokens.getOrderedAnnots().toArray(new Annotation[0]);
		int  tokenSent[] = new int[tokenArr.length];

		AnnotationSet mens = this.getAnnotationSet(Constants.NP);
		AnnotationSet goldCes = this.getAnnotationSet(Constants.GS_NP);
		//System.out.println("Ces names: "+goldCes.getName()+" and "+mens.getName());
		//for (Annotation men : mens.getOrderedAnnots()) {
		//	System.out.println(men.start+" "+men.end);
		//}
		AnnotationSet sents = this.getAnnotationSet(Constants.SENT);
		Annotation sentArr[] = (Annotation[]) sents.getOrderedAnnots().toArray(new Annotation[0]);

		for (int k = 0; k < tokenArr.length; k++) {		
			//System.out.println();
			for (int zs = 0; zs < sentArr.length; zs++) {
				Annotation sen = sentArr[zs];
				//System.out.println(sen.start+" "+sen.end);
				if (tokenArr[k].start > sen.start - 2 && tokenArr[k].end < sen.end + 2) {
					tokenSent[k] = zs;
					break;
				}
			}
			//System.out.println("token "+k+" in sent "+tokenSent[k]);
		}


		int lastSentID = 0;
		int last_end = -1;
		int next_start = Integer.MAX_VALUE;
		for (int j = 0; j < tokenArr.length; j++) {
			Annotation tt = tokenArr[j];
			if (j > 0) {
				last_end = tokenArr[j - 1].end;
			} else {
				last_end = -1;
			}
			if (j < tokenArr.length - 1) {
				next_start = tokenArr[j + 1].start;
			} else {
				next_start = Integer.MAX_VALUE;
			}

			// a new sentence?
			if (tokenSent[j] != lastSentID) {
				System.out.println(" ");
			}

			for (Annotation men : mens.getOrderedAnnots()) {
				if (men.start == tt.start || (men.start <= tt.start && men.start > last_end)) {
					int mid = Integer.parseInt(men.getAttribute(Constants.CE_ID));
					String cidStr = men.getAttribute(Constants.CLUSTER_ID);
					if (cidStr == null) cidStr = "-1";
					int ourCid = Integer.parseInt(cidStr);
					int gCid = -1;
					Integer match = (Integer)(men.getProperty(Property.MATCHED_CE));
					//System.out.println("MatchedId= "+ curId + " " + matchId);
					if (match == null) match = -1;
					if (match != -1) {
						Annotation goldMatchCe = goldCes.getAnnotationByNO(match);// matched ces
						//CorefChain cur = new CorefChain(curId, ce, doc);
						gCid = Integer.parseInt(goldMatchCe.getAttribute(Constants.CLUSTER_ID));
						//System.out.println("Gold Cluster ID = "+goldCID);
						//cur.setProcessed(false);
						//chains.put(curId,cur);
					}

					//int gCid = 
					System.out.print(" ["+mid+"("+ourCid+")<"+ gCid +"> ");
				}
			}
			//----------------------------------------------
			String words[] = this.getWords(tt.start, tt.end);
			for (int i = 0; i < words.length; i++) {
				System.out.print(words[i]);
				System.out.print(" ");
			}
			//----------------------------------------------

			Annotation reverseArr[] = (Annotation[]) mens.getOrderedAnnots().toArray(new Annotation[0]);
			for (int r = reverseArr.length - 1; r >= 0; r--) {
				Annotation men = reverseArr[r];
				if (men.end == tt.end || (men.end >= tt.end && men.end < next_start)) {
					int mid = Integer.parseInt(men.getAttribute(Constants.CE_ID));
					System.out.print(" "+mid+"] ");
				}
			}

			// sentence
			lastSentID = tokenSent[j];
		}


		return " ";
	}

	public String getHTMLCorefResult()
	{
		String wholeText = this.getText();
		//System.out.print(wholeText);
		AnnotationSet tokens = this.getAnnotationSet(Constants.TOKEN);
		Annotation tokenArr[] = (Annotation[]) tokens.getOrderedAnnots().toArray(new Annotation[0]);
		int  tokenSent[] = new int[tokenArr.length];

		AnnotationSet mens = this.getAnnotationSet(Constants.NP);
		AnnotationSet goldCes = this.getAnnotationSet(Constants.GS_NP);

		AnnotationSet sents = this.getAnnotationSet(Constants.SENT);
		Annotation sentArr[] = (Annotation[]) sents.getOrderedAnnots().toArray(new Annotation[0]);

		for (int k = 0; k < tokenArr.length; k++) {		
			//System.out.println();
			for (int zs = 0; zs < sentArr.length; zs++) {
				Annotation sen = sentArr[zs];
				//System.out.println(sen.start+" "+sen.end);
				if (tokenArr[k].start > sen.start - 2 && tokenArr[k].end < sen.end + 2) {
					tokenSent[k] = zs;
					break;
				}
			}
			//System.out.println("token "+k+" in sent "+tokenSent[k]);
		}


		int lastSentID = 0;
		int last_end = -1;
		int next_start = Integer.MAX_VALUE;
		for (int j = 0; j < tokenArr.length; j++) {
			Annotation tt = tokenArr[j];
			if (j > 0) {
				last_end = tokenArr[j - 1].end;
			} else {
				last_end = -1;
			}
			if (j < tokenArr.length - 1) {
				next_start = tokenArr[j + 1].start;
			} else {
				next_start = Integer.MAX_VALUE;
			}

			// a new sentence?
			if (tokenSent[j] != lastSentID) {
				System.out.println(" ");
			}

			for (Annotation men : mens.getOrderedAnnots()) {
				if (men.start == tt.start || (men.start <= tt.start && men.start > last_end)) {
					int mid = Integer.parseInt(men.getAttribute(Constants.CE_ID));
					String cidStr = men.getAttribute(Constants.CLUSTER_ID);
					if (cidStr == null) cidStr = "-1";
					int ourCid = Integer.parseInt(cidStr);
					int gCid = -1;
					Integer match = (Integer)(men.getProperty(Property.MATCHED_CE));
					//System.out.println("MatchedId= "+ curId + " " + matchId);
					if (match != -1) {
						Annotation goldMatchCe = goldCes.getAnnotationByNO(match);// matched ces
						//CorefChain cur = new CorefChain(curId, ce, doc);
						gCid = Integer.parseInt(goldMatchCe.getAttribute(Constants.CLUSTER_ID));
						//System.out.println("Gold Cluster ID = "+goldCID);
						//cur.setProcessed(false);
						//chains.put(curId,cur);
					}

					//int gCid = 
					System.out.print(" ["+mid+"("+ourCid+")<"+ gCid +"> ");
				}
			}
			//----------------------------------------------
			String words[] = this.getWords(tt.start, tt.end);
			for (int i = 0; i < words.length; i++) {
				System.out.print(words[i]);
				System.out.print(" ");
			}
			//----------------------------------------------

			Annotation reverseArr[] = (Annotation[]) mens.getOrderedAnnots().toArray(new Annotation[0]);
			for (int r = reverseArr.length - 1; r >= 0; r--) {
				Annotation men = reverseArr[r];
				if (men.end == tt.end || (men.end >= tt.end && men.end < next_start)) {
					int mid = Integer.parseInt(men.getAttribute(Constants.CE_ID));
					System.out.print(" "+mid+"] ");
				}
			}

			// sentence
			lastSentID = tokenSent[j];
		}


		return " ";
	}
	
	public String getAbsolutePath() {
		if (mDir != null) {
			return mDir.getAbsolutePath();
		} else {
			throw new RuntimeException("Not correponding folder was found!");
		}
	}
	
	

	public int length()
	{
		return getText().length();
	}
	

	/// Return all tokens contained in the annotation
	public static String[] getWords(String textSpan)
	{
		// remove leading non-word characters
		textSpan = pLeadingNonWord.matcher(textSpan).replaceAll("");
		return pNonWordSplit.split(textSpan);
	}

	/// Return all tokens contained in the annotation
	public static String[] getWordsPN(String textSpan)
	{
		// remove leading non-word characters
		textSpan = pLeadingNonWord.matcher(textSpan).replaceAll("");
		return pNonWordSplit1.split(textSpan);
	}

}
