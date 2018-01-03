package edu.oregonstate.nlp.coref.conll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureExtractor.SGMLStripper;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.Utils;

public class OntoNotesNPSRawFileExtractor
extends SGMLStripper {

	private AnnotationSet markups;
	private int skip;
	private static final String[] FIELDS_TO_SKIP = { "DOCNO", "DD", "DOCID", "CO", "IN", "SO", "NWORDS", "STORYID" }; // "DATE",
	private static final String UNTAG_START = "<UNTAG>";
	private static final String UNTAG_END = "</UNTAG>";

	// "TRAILER"
	private boolean preamble = false;
	private boolean headline = false;
	private boolean trailer = false;
	private boolean insertNewline = false;
	private boolean inText = false;
	FileWriter rawTextFile;
	FileWriter originalRawTextFile;
	int offset;
	Stack<Annotation> anStack;
	
	// like "v4_gold_", "v9_auto_"
	private String versionMentionPrefix = "v4_gold_";

	/*
	 * Call the parent's constructor. 
	 */
	public OntoNotesNPSRawFileExtractor(String prefix) {
		super();
		versionMentionPrefix = prefix;
	}

	/*
	 * Preprocesses the input file so things like ampersands don't break
	 * the parser. 
	 */
	@Override
	public void format(BufferedReader br, FileWriter out)
	throws IOException
	{
		String line;
		//boolean ontonotes = Utils.getConfig().getString("DATASET").equals("ontonotes") ? true : false;
		boolean ontonotes = true;

		try {
			boolean text = false;

			while ((line = br.readLine()) != null) {
				
				line = line.replaceAll("&", "@amp;");

				// TODO what does m_ mean? Should be allow alphanumeric IDs?
				//				if(line.indexOf("ID=\"m") > 0 || line.indexOf("ID=\"CNN_[a-zA-Z0-9_.-]*\"") > 0)
				//					System.out.println(line);

				if(line.indexOf("ID=\"m_")>=0 || line.indexOf("ID=\"CNN_")>=0)
					System.out.println();
				line = line.replaceAll("ID=\"m_", "ID=\"");
				line = line.replaceAll("ID=\"CNN_[a-zA-Z0-9_.-]*\"", "ID=\"1000\"");

				if (ontonotes) {
					if(line.startsWith("<TEXT")) { 
						text = true;
						out.write(line + "\n");
						continue;
					}
					else if(line.startsWith("</TEXT>") && text) {
						text = false;
						out.write(line + "\n");
						continue;
					} 
					else if(text && line.indexOf("<COREF") >= 0) {
						int pos = 0;
						int startTagS = 0;
						int startTagE = 0;
						int endTagS = 0;
						int endTagE = 0;

						startTagS = line.indexOf("<");
						if(startTagS >= 0) {
							if(startTagS != 0)
								out.write(UNTAG_START + line.substring(0, startTagS) + UNTAG_END);
							startTagE = line.indexOf(">", startTagS);
							out.write(line.substring(startTagS, startTagE + 1));
							pos = startTagE + 1;
							startTagS = line.indexOf("<COREF", pos);
						}
						else {
							out.write(UNTAG_START + line + UNTAG_END + "\n");
							continue;
						}

						while( pos < line.length()) {
							endTagS = line.indexOf("</COREF", pos);
							if(startTagS == -1) {
								endTagE = line.indexOf(">", endTagS);
								out.write(line.substring(pos, endTagE+1));
								endTagS = line.indexOf("</COREF", endTagE);
								if(endTagS == -1) {
									out.write(UNTAG_START + line.substring(endTagE + 1) + UNTAG_END + "\n");
									break;
								}
								else {
									pos = endTagE + 1;
									endTagS = line.indexOf("</COREF", pos);
								}
							}
							else if (endTagS < startTagS) {
								endTagE = line.indexOf(">", endTagS);
								out.write(line.substring(pos, endTagE+1));
								pos = endTagE + 1;
								endTagS = line.indexOf("</COREF", pos);
							}					
							else /*startTagS < endTagS*/{
								if(pos != startTagS) {
									out.write(UNTAG_START + line.substring(pos, startTagS) + UNTAG_END);
								}
								startTagE = line.indexOf(">", startTagS);
								out.write(line.substring(startTagS, startTagE + 1));
								pos = startTagE + 1;
								startTagS = line.indexOf("<COREF", pos);
							}
						}
					}
					else if (line.indexOf("<")==-1) {
						out.write(UNTAG_START + line + UNTAG_END + "\n");
					}
					else {
						out.write(line+"\n");
					}
				}
			}
		}
		catch (IOException ex) {
			System.err.println(ex);
		}

		out.close();
		br.close();
	}

	@Override
	public void run(Document doc, String[] annSetNames)
	{
		String inputFile = doc.getAbsolutePath() + Utils.SEPARATOR + versionMentionPrefix + "coref";
		String textFile = doc.getAbsolutePath() + Utils.SEPARATOR + "raw.txt";
		String origTextFile = doc.getAbsolutePath() + Utils.SEPARATOR + "orig.raw.txt";

		try {
			/* The new file will be called raw.txt */
			String outFile = doc.getAbsolutePath() + Utils.SEPARATOR + "raw.formatted";

			FileWriter writer = new FileWriter(outFile);
			FileReader reader = new FileReader(inputFile);

			XMLReader xmlr = XMLReaderFactory.createXMLReader();

			xmlr.setContentHandler(handler);
			xmlr.setErrorHandler(handler);

			BufferedReader br = new BufferedReader(reader);

			try {
				format(br, writer);
				reader.close();
			}
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			reader = new FileReader(outFile);
			rawTextFile = new FileWriter(textFile);
			originalRawTextFile = new FileWriter(origTextFile);
			//markups = new AnnotationSet(annSetNames[0]);
			markups = new AnnotationSet("temp_gold_nps");
			anStack = new Stack<Annotation>();

			offset = 0;
			skip = 0;

			// Parse the incoming XML file.
			xmlr.parse(new InputSource(reader));

			addResultSet(doc,markups);


			// output gold standard np and sentence annotations.
			// TODO see if this is needed.
			AnnotationSet nps = markups.get("COREF");
			if(nps != null) {
				nps = labelClusterIDs(nps);

				//nps.setName("gsNPs");
				nps.setName(Constants.GS_OUTPUT_FILE);
				int counter = 0;

				for (Annotation np : nps) {
					//System.out.println(np);
					np.setAttribute(Constants.CE_ID, Integer.toString(counter++));
				}

				addResultSet(doc,nps);
			}
			else {
				// Create an empty gsNPs File if there are no gold standard corefs are found.
				String gsNPsFile = doc.getAbsolutePath() + Utils.SEPARATOR
				+ Constants.ANNOT_DIR_NAME + Utils.SEPARATOR
				+ Constants.GS_OUTPUT_FILE;
				new File(gsNPsFile).createNewFile();
			}
			
			AnnotationSet sent = markups.get("s");
			if (sent != null) {
				sent.setName("gs_sentences");
				OntoNotesFileConvertor.writeAnnotationSet(new File(doc.getAbsolutePath() + Utils.SEPARATOR
						+ Constants.ANNOT_DIR_NAME, "gs_sentences"), sent);
			}

			rawTextFile.close();
			originalRawTextFile.close();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Grabs the opening SGML tag. 
	 */
	@Override
	public void startElement(String uri, String name, String qName, Attributes atts)
	{

		/*
		 * Skipping the following fields: DOCNO, DD, SO, IN, DATELINE
		 */

		//		if (FeatureUtils.memberArray(qName, FIELDS_TO_SKIP)) {
		//			skip++;
		//		}

		//		if(!qName.equalsIgnoreCase(Constants.COREF))
		//			return;

		//		if ("PREAMBLE".equals(qName) || "SLUG".equals(qName)) {
		//			preamble = true;
		//		}
		//		if ("TEXT".equals(qName) || "TXT".equals(qName)) {
		//			inText = true;
		//		}
		//		if ("HL".equals(qName)) {
		//			headline = false;
		//		}
		//		if ("TRAILER".equals(qName)) {
		//			trailer = true;
		//		}

		Map<String, String> attributes = new TreeMap<String, String>();

		for (int i = 0; i < atts.getLength(); i++) {
			String n = atts.getQName(i);
			String val = atts.getValue(i);
			// The min attribute refers to text, so it needs to be unescaped like
			// the rest of the text
			if (n.equals("MIN")) {
				val = unescapeText(val);
				if (preamble) {
					val = val.replaceAll("-", " ");
				}
			}
			attributes.put(n, val);
		}

		int id = markups.add(offset, 0, qName, attributes);
		Annotation cur = markups.get(id);
		anStack.push(cur);
	}

	/*
	 * Grabs the closing tag. 
	 */
	@Override
	public void endElement(String uri, String name, String qName)
	{
		Annotation top = anStack.pop();

		if (!top.getType().equals(name)) throw new RuntimeException("SGML type mismatch");
		if ("PREAMBLE".equals(qName) || "SLUG".equals(qName)) {
			preamble = false;
		}
		if ("TEXT".equals(qName) || "TXT".equals(qName)) {
			inText = false;
		}
		if ("HL".equals(qName)) {
			headline = false;
		}
		if ("SLUG".equals(qName)) {
			try {
				rawTextFile.write("\n");
				originalRawTextFile.write("\n");
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			offset++;
		}
		if ("TRAILER".equals(qName)) {
			trailer = false;
		}
		if (FeatureUtils.memberArray(qName, FIELDS_TO_SKIP)) {// ||"DATELINE".equals(qName))
			skip--;
			top.setEndOffset(offset);
		}
		else {
			top.setEndOffset(offset);
		}
	}

	/*
	 * This prints out all the text between the tags we care about to 
	 * a file. 
	 *
	 */
	@Override
	public void characters(char ch[], int start, int length)
	{
		String text = new String(ch, start, length);

		text = unescapeText(text);
		String end = "";
		if (text.endsWith("\n")) {
			end = "\n";
		}
		String[] texts = text.split("\n");
		StringBuilder text1 = new StringBuilder();
		boolean first = true;
		for (String t : texts) {

//			// TODO this is hack to fix the url in raw.txt 
//			// which end up having -AMP- instead of &
//			int index = -1;
//			while((index=t.indexOf("-AMP-",index+1))>=0) {
//				int	indexhttp=t.indexOf("http://");
//				if ( indexhttp>=0 && indexhttp < index) {
//					t=t.replace("-AMP-", "&");
//				}
//			}
//			System.out.println(t);

			if (inText && t.startsWith("@")) {
				//					t = t.replaceAll(".*", " ");
				// System.out.println(t);
			}
			if (first) {
				first = false;
			}
			else {
				text1.append("\n");
			}
			text1.append(t);
			text1.append(end);
			// if(!text.equals(text1))
			// System.out.println("!===="+text1+"-"+text);
			text = text1.toString();
		}
		// Clean up the trailer
		if (trailer && !text.matches("(\\d|\\-)+")) {
			text = text.replaceAll(".*", " ");
		}

		char[] textCh = text.toCharArray();
		// for (int i = start; i < start + length; i++) {
		for (char element : textCh) {
			try {
				/*
				 * If the current tag is one we don't care about, then 
				 * replace all characters with spaces. 
				 */

				if (skip > 0) {
					rawTextFile.write(" ");
				}
				else {
//					System.out.println(element);
					rawTextFile.write(element);
				}
				originalRawTextFile.write(element);
				offset++;
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String unescapeText(String text)
	{
		text = text.replaceAll("@amp;", "&");
		text = text.replaceAll("&MD;", "-");
		text = text.replaceAll("&AMP;", "&");
		text = text.replaceAll("&LR;", "");

		//		text = text.replaceAll("``|''", "\"");
		return text;
	}

}
