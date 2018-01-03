package edu.oregonstate.nlp.coref;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.morph.DefaultMorphologicalProcessor;//.Dictionary;

import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.data.relationship.AsymmetricRelationship;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNetLoadTest {

	public static void main2(String[] args) {

		String[] SUPERTYPES = { "person", "location", "organization", "time", "time_period", "date", "day",
				"money", "measure", "relation", "act", "phenomenon", "psychological_feature", "event", "group", "artifact",
				"commodity", "property", "sum", "cognitive_state",
				"male", "female", "transferred_property", "quantity", "statistic" };
		
		Dictionary wordnet = null;

		try {
			//String propsFile = "resources/WordNet3.0/dict/file_properties.xml";
			String propsFile = "resources/file_properties_orig.xml";
			FileInputStream fis = new FileInputStream(propsFile);
			assert(fis != null);
			//JWNL.initialize();//(fis);
			//wordnet = Dictionary.getInstance();
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
/*
		Synset[]  SUPERTYPE_SYNSETS = new Synset[SUPERTYPES.length];
		Synset[] classSynset;
		IndexWord iw;
		int count = 0;
		for (String type : SUPERTYPES) {
			try {
				iw = wordnet.getIndexWord(POS.NOUN, type);
			}
			catch (JWNLException e) {
				throw new RuntimeException(e);
			}
			if (iw == null) {
				System.err.println(type);
				continue;
			}

			try {
				classSynset = iw.getSenses();
			}
			catch (JWNLException e) {
				throw new RuntimeException(e);
			}
			if (classSynset.length > 1) {
				if (type.equals("abstraction")) {
					SUPERTYPE_SYNSETS[count] = classSynset[5];
				}
				else if (type.equals("measure")) {
					SUPERTYPE_SYNSETS[count] = classSynset[2];
				}
				else if (type.equals("state")) {
					SUPERTYPE_SYNSETS[count] = classSynset[3];
				}
				else if (type.equals("act")) {
					SUPERTYPE_SYNSETS[count] = classSynset[1];
				}
				else {
					SUPERTYPE_SYNSETS[count] = classSynset[0];
				}
			}
			count++;
		}
		if (wordnet == null)
			throw new RuntimeException("WordNet not intialized");
		else {
			System.out.println("Wordnet initialized " + wordnet);
		}
*/
		System.out.println("done.");
	}

	public static void loadStanfordParser() {
		
		String MODEL_NAME = "resources/Stanford/parser/englishPCFG.ser.gz";
		//public static final String MODEL_NAME = "Stanford/parser/englishFactored.ser.gz";

		LexicalizedParser lp;
		GrammaticalStructureFactory gsf;


		try {
			// set up the parser
			//InputStream in = this.getClass().getClassLoader().getResourceAsStream(MODEL_NAME);
			FileInputStream in = new FileInputStream(MODEL_NAME);

			System.out.println("Reading grammar..." + MODEL_NAME);
			Options op = new Options();
			op.doDep = true;

			lp = LexicalizedParser.loadModel();//(new ObjectInputStream(in));
			lp.setOptionFlags(new String[] { "-maxLength", "80", "-retainTmpSubcategories" });// ,"-sentences","-tokenized"});
			gsf = lp.getOp().tlpParams.treebankLanguagePack().grammaticalStructureFactory();

			System.out.println("Done reading grammar...");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}


    public static void main(String[] args) {

        try {
            // initialize JWNL (this must be done before JWNL can be used)
            //JWNL.initialize(new FileInputStream("resources/file_properties_orig.xml"));
            //new WordNetLoadTest().go();
        	
        	loadStanfordParser();
        	
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    private IndexWord ACCOMPLISH;
    private IndexWord DOG;
    private IndexWord CAT;
    private IndexWord FUNNY;
    private IndexWord DROLL;
    private IndexWord MEN;
    private IndexWord WOMEN;
    private IndexWord GONE;
    private IndexWord LED;
    private String MORPH_PHRASE = "running-away";

    public WordNetLoadTest() {
        try {
			ACCOMPLISH = Dictionary.getInstance().getIndexWord(POS.VERB, "accomplish");
	        DOG = Dictionary.getInstance().getIndexWord(POS.NOUN, "dog");
	        CAT = Dictionary.getInstance().lookupIndexWord(POS.NOUN, "cat");
	        FUNNY = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE, "funny");
	        DROLL = Dictionary.getInstance().lookupIndexWord(POS.ADJECTIVE, "droll");
	        MEN = Dictionary.getInstance().lookupIndexWord(POS.NOUN, "men");
	        WOMEN = Dictionary.getInstance().lookupIndexWord(POS.NOUN, "women");
	        GONE = Dictionary.getInstance().lookupIndexWord(POS.VERB, "gone");
	        LED = Dictionary.getInstance().lookupIndexWord(POS.VERB, "led");
		} catch (JWNLException e) {
			e.printStackTrace();
		}

    }

    public void go() throws JWNLException {
        showLemma(MEN);
        showLemma(GONE);
        showLemma(WOMEN);
        showLemma(LED);
        demonstrateMorphologicalAnalysis(MORPH_PHRASE);
        demonstrateListOperation(ACCOMPLISH);
        demonstrateTreeOperation(DOG);
        demonstrateAsymmetricRelationshipOperation(DOG, CAT);
        demonstrateSymmetricRelationshipOperation(FUNNY, DROLL);
    }

    private void showLemma(IndexWord word) {
        String lemma = word.getLemma();
        System.out.println("Word = " + word + "Lemma = " + lemma );
    }

    private void demonstrateMorphologicalAnalysis(String phrase) throws JWNLException {
        // "running-away" is kind of a hard case because it involves
        // two words that are joined by a hyphen, and one of the words
        // is not stemmed. So we have to both remove the hyphen and stem
        // "running" before we get to an entry that is in WordNet
        System.out.println("Base form for \"" + phrase + "\": " +
                Dictionary.getInstance().lookupIndexWord(POS.VERB, phrase));
    }

    private void demonstrateListOperation(IndexWord word) throws JWNLException {
        // Get all of the hypernyms (parents) of the first sense of <var>word</var>
        PointerTargetNodeList hypernyms = PointerUtils.getInstance().getDirectHypernyms(word.getSense(1));
        System.out.println("Direct hypernyms of \"" + word.getLemma() + "\":");
        hypernyms.print();
    }

    private void demonstrateTreeOperation(IndexWord word) throws JWNLException {
        // Get all the hyponyms (children) of the first sense of <var>word</var>
        PointerTargetTree hyponyms = PointerUtils.getInstance().getHyponymTree(word.getSense(1));
        System.out.println("Hyponyms of \"" + word.getLemma() + "\":");
        hyponyms.print();
    }

    private void demonstrateAsymmetricRelationshipOperation(IndexWord start, IndexWord end) throws JWNLException {
        // Try to find a relationship between the first sense of <var>start</var> and the first sense of <var>end</var>
        RelationshipList list = RelationshipFinder.getInstance().findRelationships(start.getSense(1), end.getSense(1), PointerType.HYPERNYM);
        System.out.println("Hypernym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
        for (Iterator itr = list.iterator(); itr.hasNext();) {
            ((Relationship) itr.next()).getNodeList().print();
        }
        System.out.println("Common Parent Index: " + ((AsymmetricRelationship) list.get(0)).getCommonParentIndex());
        System.out.println("Depth: " + ((Relationship) list.get(0)).getDepth());
    }

    private void demonstrateSymmetricRelationshipOperation(IndexWord start, IndexWord end) throws JWNLException {
        // find all synonyms that <var>start</var> and <var>end</var> have in common
        RelationshipList list = RelationshipFinder.getInstance().findRelationships(start.getSense(1), end.getSense(1), PointerType.SIMILAR_TO);
        System.out.println("Synonym relationship between \"" + start.getLemma() + "\" and \"" + end.getLemma() + "\":");
        for (Iterator itr = list.iterator(); itr.hasNext();) {
            ((Relationship) itr.next()).getNodeList().print();
        }
        System.out.println("Depth: " + ((Relationship) list.get(0)).getDepth());
    }
}
