package edu.oregonstate.nlp.coref.stanford;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import edu.stanford.nlp.classify.LogisticClassifier;
import edu.stanford.nlp.dcoref.ACEMentionExtractor;
import edu.stanford.nlp.dcoref.CoNLLMentionExtractor;
import edu.stanford.nlp.dcoref.Constants;
import edu.stanford.nlp.dcoref.CorefMentionFinder;
import edu.stanford.nlp.dcoref.Dictionaries;
import edu.stanford.nlp.dcoref.MUCMentionExtractor;
import edu.stanford.nlp.dcoref.MentionExtractor;
import edu.stanford.nlp.dcoref.Semantics;
import edu.stanford.nlp.dcoref.SieveCoreferenceSystem;
import edu.stanford.nlp.pipeline.DefaultPaths;

import edu.oregonstate.nlp.coref.SystemConfig;

public class StanfordSystemInterface {
	
	/**
	 * Stanford system properties 
	 * */
	private Properties props;
	
	// Flags 
	/**
	 * If true, we do post processing.
	 */
	private boolean doPostProcessing;

	/**
	 * maximum sentence distance between two mentions for resolution (-1: no constraint on distance)
	 */
	private int maxSentDist;

	/**
	 * automatically set by looking at sieves
	 */
	private boolean useSemantics;

	/**
	 * Singleton predictor from Recasens, de Marneffe, and Potts (NAACL 2013)
	 */
	private boolean useSingletonPredictor;

	/** flag for replicating CoNLL result */
	private boolean replicateCoNLL;

	// Local Tools
	
	/**
	 * Dictionaries of all the useful goodies (gender, animacy, number etc. lists)
	 */
	private Dictionaries dictionaries;

	/**
	 * Semantic knowledge: WordNet
	 */
	private Semantics semantics;

	private LogisticClassifier<String, String> singletonPredictor;


	// for coref
	public MentionExtractor mentionExtractor;
	public StanfordCoNLLMentionExtractor conllMentionExtractor;

	



	//////////////////////////////////////////////////

	public StanfordSystemInterface() {

	}

	/** 
	 * Initialize the StanfordCoreNLp environment  with Reconcile configurations 
	 */
	public StanfordSystemInterface(SystemConfig cfg) {
		System.out.println("Initializing Stanford system...");
		initWithReconcileConfig(cfg);
	}

	public void initWithReconcileConfig(SystemConfig cfg) {
		
		props = new Properties();
		
		// set the property values	
		// corpus
		if (cfg.getDataset().equals("ace04")) props.setProperty(Constants.ACE2004_PROP, "ace04");
		if (cfg.getDataset().equals("ace05")) props.setProperty(Constants.ACE2005_PROP, "ace05");
		if (cfg.getDataset().contains("muc")) props.setProperty(Constants.MUC_PROP, "muc");
		if (cfg.getDataset().contains("ontonotes")) props.setProperty(Constants.CONLL2011_PROP, "conll");
		
		// mention setting
		//String ourMentionSet = cfg.getString("NPS", "unknown");
		//if (ourMentionSet.equals("gsNPs")) {
			
		//}
		props.setProperty(Constants.MENTION_FINDER_PROP, "edu.stanford.nlp.dcoref.RuleBasedCorefMentionFinder");
		
		// others
		// 1) singleton predictor?
		useSingletonPredictor = true;
		
		// load the system
		initStanfordSystemWithProperties(props);
	}
	
	private void initStanfordSystemWithProperties(Properties props) {

		//
		// setting post processing
		//
		doPostProcessing = Boolean.parseBoolean(props.getProperty(Constants.POSTPROCESSING_PROP, "false"));

		//
		// setting singleton predictor
		//
		useSingletonPredictor = Boolean.parseBoolean(props.getProperty(Constants.SINGLETON_PROP, "true"));

		//
		// setting maximum sentence distance between two mentions for resolution (-1: no constraint on distance)
		//
		maxSentDist = Integer.parseInt(props.getProperty(Constants.MAXDIST_PROP, "-1"));

		//
		// set useWordNet
		//
		useSemantics = false;//(sievePasses.contains("AliasMatch") || sievePasses.contains("LexicalChainMatch"));

		// flag for replicating CoNLL result
		replicateCoNLL = Boolean.parseBoolean(props.getProperty(Constants.REPLICATECONLL_PROP, "false"));
		
		//
		// load all dictionaries
		//
		try {
			dictionaries = new Dictionaries(props);
			semantics = (useSemantics)? new Semantics(dictionaries) : null;
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if(useSingletonPredictor){
			singletonPredictor = SieveCoreferenceSystem.getSingletonPredictorFromSerializedFile(props.getProperty(Constants.SINGLETON_MODEL_PROP, DefaultPaths.DEFAULT_DCOREF_SINGLETON_MODEL));
		}

		try {
			// mention extractor?
			// MentionExtractor extracts MUC, ACE, or CoNLL documents
			mentionExtractor = null;
			conllMentionExtractor = null;
			if (props.containsKey(Constants.MUC_PROP)){
				mentionExtractor = new MUCMentionExtractor(dictionaries, props,
						semantics, singletonPredictor);
			} else if(props.containsKey(Constants.ACE2004_PROP) || props.containsKey(Constants.ACE2005_PROP)) {
				mentionExtractor = new ACEMentionExtractor(dictionaries, props,
						semantics, singletonPredictor);
			} else if (props.containsKey(Constants.CONLL2011_PROP)) {
				conllMentionExtractor = new StanfordCoNLLMentionExtractor(dictionaries, props,
						semantics, singletonPredictor);
				mentionExtractor = conllMentionExtractor;
			}
			if (mentionExtractor == null){
				throw new RuntimeException("No input file specified!");
			}
			
			//if (!Constants.USE_GOLD_MENTIONS) {
			if (true) { // never use gold mentions... need its predicted mentions...
				
				// Set mention finder
				String mentionFinderClass = props.getProperty(Constants.MENTION_FINDER_PROP);
				if (mentionFinderClass != null) {
					String mentionFinderPropFilename = props.getProperty(Constants.MENTION_FINDER_PROPFILE_PROP);
					CorefMentionFinder mentionFinder;
					if (mentionFinderPropFilename != null) {
						Properties mentionFinderProps = new Properties();
						mentionFinderProps.load(new FileInputStream(mentionFinderPropFilename));
						mentionFinder = (CorefMentionFinder) Class.forName(mentionFinderClass).getConstructor(Properties.class).newInstance(mentionFinderProps);
					} else {
						mentionFinder = (CorefMentionFinder) Class.forName(mentionFinderClass).newInstance();
					}
					mentionExtractor.setMentionFinder(mentionFinder);
				}
				if (mentionExtractor.mentionFinder == null) {
					throw new RuntimeException("No mention finder specified, but not using gold mentions");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public MentionExtractor getMentionExtractor() {
		return mentionExtractor;
	}
	
	/**
	 * Only works on the conll corpus
	 * */
	public StanfordCoNLLMentionExtractor getConllMentionExtractor() {
		return conllMentionExtractor;
	}
}
