package edu.oregonstate.nlp.coref.conll;

import java.io.BufferedReader;
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

public class OntoNotesNEExtractor
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
	int offset;
	Stack<Annotation> anStack;
	
	// like "v4_gold_", "v9_auto_"
	private String versionMentionPrefix = "v4_gold_";

	/*
	 * Call the parent's constructor. 
	 */
	public OntoNotesNEExtractor(String prefix) {
		super();
		
		// like "v4_gold_", "v9_auto_"
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
			while ((line = br.readLine()) != null) {
				line = line.replaceAll("&", "@amp;");
				// TODO what does m_ mean? Should be allow alphanumeric IDs?
				line = line.replaceAll("ID=\"m_", "ID=\"");;
				line = line.replaceAll("ID=\"CNN_[a-zA-Z0-9_.-]*\"", "ID=\"1000\"");

				if (ontonotes) {
					if(line.indexOf("<ENAMEX") >= 0) {
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
							startTagS = line.indexOf("<ENAMEX", pos);
						}
						else {
							out.write(UNTAG_START + line + UNTAG_END + "\n");
							continue;
						}

						while( pos < line.length()) {
							endTagS = line.indexOf("</ENAMEX", pos);
							if(startTagS == -1) {
								endTagE = line.indexOf(">", endTagS);
								out.write(line.substring(pos, endTagE+1));
								endTagS = line.indexOf("</ENAMEX", endTagE);
								if(endTagS == -1) {
									out.write(UNTAG_START + line.substring(endTagE + 1) + UNTAG_END + "\n");
									break;
								}
								else {
									pos = endTagE + 1;
									endTagS = line.indexOf("</ENAMEX", pos);
								}
							}
							else if (endTagS < startTagS) {
								endTagE = line.indexOf(">", endTagS);
								out.write(line.substring(pos, endTagE+1));
								pos = endTagE + 1;
								endTagS = line.indexOf("</ENAMEX", pos);
							}					
							else /*startTagS < endTagS*/{
								if(pos != startTagS) {
									out.write(UNTAG_START + line.substring(pos, startTagS) + UNTAG_END);
								}
								startTagE = line.indexOf(">", startTagS);
								out.write(line.substring(startTagS, startTagE + 1));
								pos = startTagE + 1;
								startTagS = line.indexOf("<ENAMEX", pos);
							}
						}
					}
					else {
						out.write(line + "\n");
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
		String inputFile = doc.getAbsolutePath() + Utils.SEPARATOR + versionMentionPrefix + "name";

		try {
			/* The new file will be called raw.txt */
			String outFile = doc.getAbsolutePath() + Utils.SEPARATOR + "ne_temp";

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
			//markups = new AnnotationSet(annSetNames[0]);
			markups = new AnnotationSet("temp_gold_ne");
			anStack = new Stack<Annotation>();

			// TODO Temporary fix to take into account the <text> tag missing in name file. 
			offset = 1;
			skip = 0;

			// Parse the incoming XML file.
			xmlr.parse(new InputSource(reader));

			addResultSet(doc,markups);

			// output gold standard NE and sentence annotations.
			AnnotationSet nes = markups.get("ENAMEX");
			if (nes == null) { // ne set does not exist
				nes = new AnnotationSet(Constants.NE); // an empty set
			}
			
			correctOffsets(doc.getText(), nes);
			translateTypes(nes);
			if(nes != null) {
//				nes = labelClusterIDs(nes);

				//nps.setName("gsNPs");
				nes.setName(Constants.NE);
				int counter = 0;

				for (Annotation ne : nes) {
					// TODO
					ne.setAttribute("ID", String.valueOf(counter));
					ne.setAttribute(Constants.CE_ID, Integer.toString(counter++));
				}

				addResultSet(doc,nes);
			}
			
			// AnnotationSet sent = markups.get("s");
			// sent.setName("gs_sentences");
			// writeAnnotationSet(sent, dir);

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

		if (FeatureUtils.memberArray(qName, FIELDS_TO_SKIP)) {
			skip++;
		}

		if ("PREAMBLE".equals(qName) || "SLUG".equals(qName)) {
			preamble = true;
		}
		if ("TEXT".equals(qName) || "TXT".equals(qName)) {
			inText = true;
		}
		if ("HL".equals(qName)) {
			headline = false;
		}
		if ("TRAILER".equals(qName)) {
			trailer = true;
		}

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

		//text = unescapeText(text);
		if (headline) {
			text = text.replaceAll("@", " ");
		}

		// Clean the preamble -- remove -'s and double newlines
		if (preamble) {
			// System.out.println("Text: ("+insertNewline+") "+text);
			if (insertNewline && ch[start] == '\n') {
				text = "\n" + text;
			}
			text = text.replaceAll("-", " ");
			text = text.replaceAll("([A-Z\\)]\\s*\\n)", "$1\n");
			text = text.replaceAll("BC(\\W*)", "  $1");
			insertNewline = text.matches("(.|\\n)*[A-Z]\\W?");

			// System.out.println("next: "+ch[start+length]);
			// System.out.println("New : "+text);
		}
		else {
			insertNewline = false;
			String end = "";
			if (text.endsWith("\n")) {
				end = "\n";
			}
			String[] texts = text.split("\n");
			StringBuilder text1 = new StringBuilder();
			boolean first = true;
			for (String t : texts) {
				if (inText && t.startsWith("@")) {
					t = t.replaceAll(".*", " ");
					// System.out.println(t);
				}
				if (first) {
					first = false;
				}
				else {
					text1.append("\n");
				}
				text1.append(t);
			}
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
			offset++;
		}
	}

	public String unescapeText(String text)
	{
		text = text.replaceAll("@amp;", "&");
		text = text.replaceAll("&MD;", "-");
		text = text.replaceAll("&AMP;", "&");
		text = text.replaceAll("&LR;", "");

		text = text.replaceAll("``|''", "\"");
		return text;
	}
	
	public void correctOffsets(String text, AnnotationSet an){
		int[] trueoffset = new int[text.length()];
		int counter = 1;
		trueoffset[0]=0;
		char[] textArr = text.toCharArray();
		for(int i=0;i<textArr.length;i++){
			if(textArr[i]=='\n' && (i==0||textArr[i-1]=='\n')){
				
			}else{
				counter++;
			}
			trueoffset[counter]=i;
		}
		for(Annotation a:an){
			a.setStartOffset(trueoffset[a.getStartOffset()]);
			a.setEndOffset(trueoffset[a.getEndOffset()]);
		}
	}
	
	public void translateTypes(AnnotationSet an){
		for(Annotation a:an){
			String entityType=a.getAttribute("TYPE");
			if (entityType.equals("ORG")) {
				entityType="ORGANIZATION";
			}
			else if (entityType.equals("GPE")) {
				entityType="LOCATION";
			}
			else if (entityType.equals("PER")) {
				entityType="PERSON";
			}
			else if (entityType.equals("VEH")) {
				entityType="VEHICLE";
			}
			a.setType(entityType);
		}
	}

}
