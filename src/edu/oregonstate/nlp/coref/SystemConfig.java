package edu.oregonstate.nlp.coref;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import edu.oregonstate.nlp.coref.general.Utils;


/**
 * @author nathan
 * 
 */

public class SystemConfig
    extends PropertiesConfiguration {
	
public static  String SEPARATOR = Utils.SEPARATOR;//System.getProperty("file.separator");
private String DATA_DIR = null;
private String SCRIPT_DIR = null;
private String WORK_DIR = null;

// Some defaults
private String TRAIN_DIR = null;
private String TRAIN_FILELIST = null;
private String TEST_DIR = null;
private String TEST_FILELIST = null;
private String VALID_DIR = null;
private String VALID_FILELIST = null;
private boolean GENERATE_FEATURES = true;
private boolean PREPROCESS = true;
private boolean CROSS_VALIDATE = false;
private boolean VALIDATE = false;
private String CROSS_VALIDATOR = null;
private String VALIDATOR = null;
private String SCORER = null;
private boolean SCORE = true;
private String[] SCORER_SET = null;
private int NUM_FOLDS = 10;
private int RATIO = 10;
private boolean TRAIN_ONLY = false;
private boolean TRAIN = true;
private boolean TEST = true;
private boolean TESTONLY = false;
private boolean PRINTINDIESCORES = false;
private String[] FEATURE_NAMES = {};
private String PAIR_GEN_NAME = "AllPairs";
public String[] NERMODELS = {};
public boolean MUC6 = true;
public String DATASET = "muc6";
public String FEAT_SET_NAME = null;
public String CLASSIFIER;
public String CLUSTERER;
public String MODEL_NAME;
public String MUC_SCORER_PATH;
public String ANNOTDIR;
public String ANNOTFILES;
public String tagChunk;
public String tagChunkLists;
public boolean VERBOSE;

// by Chao Ma (2013-1-29) -----
public int     NUM_ITERATION = 0;
public double  LEARNING_RATE = 0.1;
public boolean TEST_AFTER_EACH_ITER = false;
public boolean ORACLE_POLICY_TEST = false;
public boolean APPLY_ACTION_PRUNING;
public boolean ACT_ORDER_RESTRICT;
public String  SCORE_LOG_PATH = "";
public String  CURVE_LOG_PATH = "";
public String  FEATURE_LOG_PATH = "";
public String  COST_LOG_PATH = "";
public String  LEARNER_NAME = "";
// by Chao (2014-5-8)
public boolean USE_GOLD_MENTIONS = false;
// ----------------------------

private String[] Scorers;

public SystemConfig()
    throws ConfigurationException {
  this(Utils.getResourceStream("default.config"));
}

public SystemConfig(InputStream is)
    throws ConfigurationException {
  super();
  load(new BufferedReader(new InputStreamReader(is)));
  init();
}

public SystemConfig(String fn)
    throws ConfigurationException {
  super(fn);
  init();
}


private void init()
{

  TRAIN_DIR = getString("TRAIN_DIR");
  TRAIN_FILELIST = getString("TRAIN_FILELIST");
  TEST_DIR = getString("TEST_DIR");
  TEST_FILELIST = getString("TEST_FILELIST");
  VALID_DIR = getString("VALID_DIR");
  VALID_FILELIST = getString("VALID_FILELIST");

  // String trainDirStr = getString("TRAIN_DIR");
  // String trainListStr = getString("TRAIN_FILELIST");
  // if (trainDirStr != null && trainListStr != null) {
  // TRAIN_DIR = new File(trainDirStr);
  // TRAIN_FILELIST = new File(trainListStr);
  // }
  // String testDirStr = getString("TEST_DIR");
  // String testListStr = getString("TEST_FILELIST");
  // if (testDirStr != null && testListStr != null) {
  // TEST_DIR = new File(testDirStr);
  // TEST_FILELIST = new File(testListStr);
  // }
  // String validDirStr = getString("VALID_DIR");
  // String validListStr = getString("VALID_FILELIST");
  // if (validDirStr != null && validListStr != null) {
  // VALID_DIR = new File(validDirStr);
  // VALID_FILELIST = new File(validListStr);
  // }

  DATASET = getString("DATASET");
  FEAT_SET_NAME = getString("FEAT_SET_NAME");
  GENERATE_FEATURES = getBoolean("RUN_FEATURE_GENERATION", GENERATE_FEATURES);
  PREPROCESS = getBoolean("PREPROCESS", PREPROCESS);
  CROSS_VALIDATE = getBoolean("CROSS_VALIDATE", CROSS_VALIDATE);
  CROSS_VALIDATOR = getString("CROSS_VALIDATOR");
  VALIDATE = getBoolean("VALIDATE", VALIDATE);
  VALIDATOR = getString("VALIDATOR");
  SCORER = getString("OPTIMIZE_SCORER");
  SCORER_SET = getStringArray("OPTIMIZE_SCORER_SET");
  NUM_FOLDS = getInt("NUM_FOLDS", NUM_FOLDS);
  SCORE = getBoolean("SCORE", SCORE);
  TRAIN_ONLY = getBoolean("TRAIN_ONLY", true);
  TRAIN = getBoolean("TRAIN", TRAIN);
  TEST = getBoolean("TEST", TEST);
  TESTONLY = getBoolean("TESTONLY", TESTONLY);
  FEATURE_NAMES = getStringArray("FEATURE_NAMES");
  MUC6 = getBoolean("MUC6", MUC6);
  VERBOSE = getBoolean("VERBOSE", VERBOSE);
  CLASSIFIER = getString("CLASSIFIER");
  MODEL_NAME = getString("MODEL_NAME");
  MUC_SCORER_PATH = getString("MUC_SCORER_PATH");
  CLUSTERER = getString("CLUSTERER", "SingleLink");
  RATIO = getInt("RATIO", RATIO);

  tagChunk = getString("TAGCHUNK");
  tagChunkLists = getString("TAGCHUNK_LISTS");
  ANNOTDIR = getString("ANNOT_DIR");
  ANNOTFILES = getString("ANNOT_FILELIST");
  PAIR_GEN_NAME = getString("INSTANCE_GENERATOR", PAIR_GEN_NAME);
  PRINTINDIESCORES = getBoolean("INDIESCORES", PRINTINDIESCORES);

  String data_dir = getString("DATA_DIR");
  if (data_dir != null && data_dir.length() > 0) {
     setDataDirectory(data_dir);
  }


  Scorers = getStringArray("SCORERS");
  
  // by Chao Ma (2013-1-29)
  NUM_ITERATION = getInt("NUM_ITERATION", 0); // number of interation in training
  TEST_AFTER_EACH_ITER = getBoolean("TEST_AFTER_EACH_ITER", false); // whether test the performance after each iteration?
  LEARNING_RATE = getDouble("LEARNING_RATE", 0.1); // define the starting default learning rate
  ORACLE_POLICY_TEST = getBoolean("ORACLE_POLICY_TEST", false); // do correct action at each step in testing, in order to count the error rate
  APPLY_ACTION_PRUNING = getBoolean("APPLY_ACTION_PRUNING", true);
  ACT_ORDER_RESTRICT = getBoolean("ACT_ORDER_RESTRICT", true);
  CURVE_LOG_PATH = getString("CURVE_LOG_PATH", "curveLog.txt");
  SCORE_LOG_PATH = getString("SCORE_LOG_PATH", "scoreRaw.log");
  FEATURE_LOG_PATH = getString("FEATURE_LOG_PATH", "./featureLog.txt");
  COST_LOG_PATH = getString("COST_LOG_PATH", "./costLog.txt");
  LEARNER_NAME = getString("LEARNER_NAME", "svmrank");
  
  // for conll
  USE_GOLD_MENTIONS = getBoolean("USE_GOLD_MENTIONS", false);
}


public void addConfig(InputStream in)
{
  try {
    PropertiesConfiguration tmp = new PropertiesConfiguration();
    tmp.load(in);
    addConfig(tmp);
  }
  catch (ConfigurationException ce) {
    throw new RuntimeException(ce);
  }

}

@SuppressWarnings("unchecked")
public void addConfig(Configuration cfg)
{
  Iterator<String> it = cfg.getKeys();
  while (it.hasNext()) {
    String s = it.next();
    clearProperty(s);
    setProperty(s, cfg.getProperty(s));
  }
  init();
}

public void addConfig(String fn)
{
  System.out.println("add filename: " + fn);
  File f = new File(fn);
  addConfig(f);
}

public void addConfig(File f)
{
  try {
    System.out.println("add file: " + f.getAbsolutePath());
    FileInputStream fin = new FileInputStream(f);
    addConfig(fin);
  }
  catch (FileNotFoundException e) {
    throw new RuntimeException(e);
  }
}


public boolean testOnly() 
{
	return TESTONLY;
}

public boolean getVerbose(){ 
	return VERBOSE;
}

public String getTagChunk()
{
  return tagChunk;
}

public String getTagChunkLists()
{
  return tagChunkLists;
}

public int getRatio() {
	return RATIO;
}

public String getAnnotDir()
{
  return ANNOTDIR;
}

public String getAnnotLst()
{
  return ANNOTFILES;
}

public boolean getScore() {
	return SCORE;
}

public String getTrLst()
{
  return TRAIN_FILELIST;
}

public String getTrDir()
{
  return TRAIN_DIR;
}

public String getTestDir()
{
  return TEST_DIR;
}

public String getValidDir()
{
  return VALID_DIR;
}

public boolean getMUC6()
{
  return MUC6;
}

public boolean getIndieScores()
{
	return PRINTINDIESCORES;
}

public String[] getNERModels(String ner)
{
  NERMODELS = getStringArray(ner);
  return NERMODELS;
}

public String getTestLst()
{
  return TEST_FILELIST;
}

public String getValidLst()
{
  return VALID_FILELIST;
}

public boolean getTrainOnly()
{
  return TRAIN_ONLY;
}

public boolean getTrain()
{
  return TRAIN;
}

public boolean getTest()
{
  return TEST;
}

public boolean getGenerateFeatures()
{
  return GENERATE_FEATURES;
}

public boolean getPreProcess()
{
  return PREPROCESS;
}

public boolean getCrossValidate()
{
  return CROSS_VALIDATE;
}

public String getCrossValidator()
{
  return CROSS_VALIDATOR;
}

public boolean getValidate()
{
  return VALIDATE;
}

public String getValidator()
{
  return VALIDATOR;
}

public String getOptimizeScorer()
{
  return SCORER;
}

public void setOptimizeScorer(String scorer)
{
  SCORER = scorer;
}

public String[] getOptimizeScorerSet()
{
  return SCORER_SET;
}

public String getPairGenName()
{
  return PAIR_GEN_NAME;
}

public String[] getScorers()
{
  return Scorers;
}

public int getNumFolds()
{
  return NUM_FOLDS;
}


public String[] getFeatureNames()
{
  return FEATURE_NAMES;
}


public String getDataset()
{
  return DATASET;
}

public String getFeatSetName()
{
  return FEAT_SET_NAME;
}

public String getClassifier()
{
  return CLASSIFIER;
}

public String getModelName()
{
  return MODEL_NAME;
}

public String getMUCScorerPath()
{
  return MUC_SCORER_PATH;
}

public String getClusterer()
{
  return CLUSTERER;
}

// by Chao Ma =============
public String getCurveLogPath()
{
  return CURVE_LOG_PATH;
}

public String getScoreLogPath()
{
  return SCORE_LOG_PATH;
}

public String getFeatureLogPath()
{
  return FEATURE_LOG_PATH;
}

public String costLogPath()
{
  return COST_LOG_PATH;
}

public String getPronounFeatLogPath()
{
	String pronounFeatPath = getString("FEATPRO_LOG_PATH", "./featLogpronoun.txt");
	return pronounFeatPath;
}

public String getLearnerName()
{
  return LEARNER_NAME;
}

public boolean getUsingGoldMentionsOrNot() {
  return USE_GOLD_MENTIONS;
}

public int getDiscrepancyComparatorType()
{
	int DISCRE_EMPTY_COMPARE  = 0;
	int DISCRE_INTELL_COMPARE = 1;
	int DISCRE_NAIVE_COMPARE  = 2;
	String comparatorName = getString("DISCRE_COMP_TYPE", "DISCRE_EMPTY_COMPARE"); // default is "no-sorting"
	if (comparatorName.equals("DISCRE_INTELL_COMPARE")) {
		return DISCRE_INTELL_COMPARE;
	} else if (comparatorName.equals("DISCRE_NAIVE_COMPARE")) {
		return DISCRE_NAIVE_COMPARE;
	}
	return DISCRE_EMPTY_COMPARE;
}

// for conll 2011 and 2012
public String getConllParseScript() {
	return getString("CONLL_PARSE_SCRIPT", "scripts/conll2012_v3_scripts/conll2parse.sh");
}
public String getConllNameScript() {
	return getString("CONLL_NAME_SCRIPT", "scripts/conll2012_v3_scripts/conll2name.sh");
}
public String getConllCorefScript() {
	return getString("CONLL_COREF_SCRIPT", "scripts/conll2012_v3_scripts/conll2coreference.sh");
}
// ========================


/**
 * @param string
 */
public void setFeatureSetName(String string)
{
  FEAT_SET_NAME = string;
  
}

public void setDataset(String dataset) {
	DATASET = dataset;
}

public void setClassifier(String classifier) {
	CLASSIFIER = classifier;
}

public void setModelName(String modelName) {
	MODEL_NAME = modelName;
	
}

public void setClusterer(String clusterer) {
	CLUSTERER = clusterer;
	
}

//////////////////////////


public String getLanguageModelDirectory()
{
  return getDataDirectory() + SEPARATOR + "OpenNLP" + SEPARATOR + "models";
}

public String getParserGrammarDirectory()
{
  return getDataDirectory() + SEPARATOR + "BerkeleyParser" + SEPARATOR + "models" + SEPARATOR;
}

public String getDataDirectory()
{
  if (DATA_DIR == null) {
    DATA_DIR = getString("DATA_DIR", "resources");
  }

  return DATA_DIR;
}

public void setDataDirectory(String dir)
{
  DATA_DIR = dir;
}

public String getScriptDirectory()
{
  if (SCRIPT_DIR == null) {
    SCRIPT_DIR = getString("SCRIPT_DIR");
    if (SCRIPT_DIR == null) {
      try {
        File dir1 = new File(".");
        String dir = dir1.getCanonicalPath();

        SCRIPT_DIR = dir + SEPARATOR + "scripts";
      }
      catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }
  }

  return SCRIPT_DIR;
}

public String getWorkDirectory()
{
  try {
    if (WORK_DIR == null) {
      WORK_DIR = getString("WORK_DIR");
      if (WORK_DIR == null || WORK_DIR.length() < 1) {
        File dir1 = new File(".");
        String dir = dir1.getCanonicalPath();

        WORK_DIR = dir + SEPARATOR + "WORK";
      }
    }
  }
  catch (IOException ioe) {
    throw new RuntimeException(ioe);
  }

  return WORK_DIR;
}

public String getWorkDirectoryForStreams()
{
  try {
    if (WORK_DIR == null) {
      WORK_DIR = getString("WORK_DIR");
      if (WORK_DIR == null || WORK_DIR.length() < 1) {
        File dir1 = new File(".");
        String dir = dir1.getCanonicalPath();

        WORK_DIR = dir + SEPARATOR + "WORK";
      }
    }
  }
  catch (IOException ioe) {
    throw new RuntimeException(ioe);
  }

  return WORK_DIR;
}

}
