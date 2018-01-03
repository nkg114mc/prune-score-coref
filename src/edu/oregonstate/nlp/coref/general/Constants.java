package edu.oregonstate.nlp.coref.general;

import edu.oregonstate.nlp.coref.features.properties.EmptyProperty;

public class Constants {

// A class that defines some constants used throughout the application
// Turn on and off the debug mode
public static boolean DEBUG = false;

// Strings that correspond to different annotation set names
public static final String SENT = "Sentence";
public static final String PAR = "Paragraph";
public static final String POS = "PartOfSpeech";
public static final String TOKEN = "Token";
public static final String PARSE = "Parse";
public static final String DEP = "Dependency";
public static final String PARTIAL_PARSE = "PartialParse";
public static final String ORIG = "OriginalMarkup";
public static final String NP = "MarkableNP";
public static final String STANFORD_NP = "StanfordNP";
public static final String UIUC_NP = "UiucNP";
public static final String GS_NP = "GS_CEs";
public static final String NE = "NamedEntities";

// Constants used in the file structure
public static final String ANNOT_DIR_NAME = "annotations";
public static final String FEAT_DIR_NAME = "features";
public static final String PRED_DIR_NAME = "predictions";

public static final String FEAT_FILE_NAME = "features";
public static final String PRED_FILE_NAME = "predictions";
public static final String CLUSTER_FILE_NAME = "coref_output";
public static final String PROPERTIES_FILE_NAME = "npProperties";
public static final String NO_SINGLITON_CLUSTER_FILE_NAME = "no_singleton"; // for ontonotes, no singleton cluster
public static final String GS_OUTPUT_FILE = "gsNPs";
public static final String GS_NE_OUTPUT_FILE = "gsNEs";
public static boolean PAR_NUMS_UNAVAILABLE = false;

// Constants used for different attributes
/**
 * cluster id is the identifier for the chain a coreferent entity appears in
 */
public static final String CLUSTER_ID = "CorefID";

/**
 * CE == coreferent entity
 */
public static final String CE_ID = "NO";
public static final String GLOABAL_ID = "Global_NO";

// The names of some feature that are used often in other features
public static final String APPOSITIVE = "Appositive";
public static final String PREDNOM = "Prednom";
public static final String ALIAS = "Alias";

/**
 * 
 */
public static final String RESPONSE_NPS = "responseNPs";

// Well-known Attributes
/**
 * start of the head for this NP
 */
public static final String HEAD_START = "HEAD_START";
/**
 * end of the head for this NP
 */
public static final String HEAD_END = "HEAD_END";

public static final String HEAD_STR = "HEAD_STR";

/**
 * id of the matching gold attribute
 */
public static final String MATCHED_GS_NP = "matched_gs_np";

public static final String IS_MATCHED = "is_matched";
public static final String GOLD_CATEGORY = "GoldCategory";

// annotation types
/**
 * coref type string
 */
public static final String COREF = "coref";

// for search hash code
/**
 * a random array for hash code computing
 */
public static int ZOBRIST_HASHKEY[][] = new int[1024][1024];


// for conll
public static final String CONLL_ANNO = "CoNLLAnnotation";

public static final String CONLL_DOCID = "Conll_DocID";
public static final String CONLL_PARTNUM = "Conll_PartNumber";
public static final String CONLL_WORDNUM = "Conll_WordNumber";
public static final String CONLL_WORDSTR = "Conll_WordItself";
public static final String CONLL_POS = "Conll_PartofSpeech";
public static final String CONLL_PARSEBIT = "Conll_ParseBit";
public static final String CONLL_LEMMA = "Conll_PredicateLemma";
public static final String CONLL_FRAMESET = "Conll_PredicateFramesetID";
public static final String CONLL_WORD_SENSE = "Conll_WordSense";
public static final String CONLL_SPEAKER = "Conll_SpeakerAuthor";
public static final String CONLL_NE = "Conll_NamedEntities";
public static final String CONLL_PREDICT_ARG = "Conll_PredicateArguments";
public static final String CONLL_COREF = "Conll_Coreference";

}
