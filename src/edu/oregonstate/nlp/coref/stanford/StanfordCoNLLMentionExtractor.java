//
// StanfordCoreNLP -- a suite of NLP tools
// Copyright (c) 2009-2010 The Board of Trustees of
// The Leland Stanford Junior University. All Rights Reserved.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// For more information, bug reports, fixes, contact:
//    Christopher Manning
//    Dept of Computer Science, Gates 1A
//    Stanford CA 94305-9010
//    USA
//

package edu.oregonstate.nlp.coref.stanford;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.nlp.classify.LogisticClassifier;
//import edu.stanford.nlp.dcoref.CoNLL2011DocumentReader;
import edu.stanford.nlp.dcoref.CoNLL2011DocumentReader.Document;
import edu.stanford.nlp.dcoref.Constants;
import edu.stanford.nlp.dcoref.Dictionaries;
import edu.stanford.nlp.dcoref.Mention;
import edu.stanford.nlp.dcoref.MentionExtractor;
import edu.stanford.nlp.dcoref.RuleBasedCorefMentionFinder;
import edu.stanford.nlp.dcoref.Semantics;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.util.CollectionValuedMap;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
 * Extracts coref mentions from a CoNLL2011 data files
 * @author Angel Chang
 */
public class StanfordCoNLLMentionExtractor extends MentionExtractor {

  private final StanfordCoNLL2011DocReader reader;
  private final String corpusPath;
  private final boolean replicateCoNLL;

  public StanfordCoNLLMentionExtractor(Dictionaries dict, Properties props, Semantics semantics) throws Exception {
    super(dict, semantics);

    // Initialize reader for reading from CONLL2011 corpus
    corpusPath = props.getProperty(Constants.CONLL2011_PROP);
    replicateCoNLL = Boolean.parseBoolean(props.getProperty(Constants.REPLICATECONLL_PROP, "false"));

    StanfordCoNLL2011DocReader.Options options = new StanfordCoNLL2011DocReader.Options();
    options.annotateTokenCoref = false;
    options.annotateTokenSpeaker = Constants.USE_GOLD_SPEAKER_TAGS || replicateCoNLL;
    options.annotateTokenNer = Constants.USE_GOLD_NE || replicateCoNLL;
    options.annotateTokenPos = Constants.USE_GOLD_POS || replicateCoNLL;
    if (Constants.USE_CONLL_AUTO) options.setFilter(".*_auto_conll$");
    reader = new StanfordCoNLL2011DocReader(corpusPath, options);

    stanfordProcessor = loadStanfordProcessor(props);
  }
  
  public StanfordCoNLLMentionExtractor(Dictionaries dict, Properties props, Semantics semantics,
      LogisticClassifier<String, String> singletonModel) throws Exception {
    this(dict, props, semantics);
    singletonPredictor = singletonModel;
  }

  private final boolean collapse = true;
  private final boolean ccProcess = false;
  private final boolean includeExtras = false;
  private final boolean lemmatize = true;
  private final boolean threadSafe = true;


  public void resetDocs() {
    super.resetDocs();
    reader.reset();
  }

  @Override
  public edu.stanford.nlp.dcoref.Document nextDoc() throws Exception {
/*
    List<List<CoreLabel>> allWords = new ArrayList<List<CoreLabel>>();
    List<Tree> allTrees = new ArrayList<Tree>();

    CoNLL2011DocumentReader.Document conllDoc = reader.getNextDocument();
    if (conllDoc == null) {
      return null;
    }

    Annotation anno = conllDoc.getAnnotation();
    List<CoreMap> sentences = anno.get(CoreAnnotations.SentencesAnnotation.class);
    for (CoreMap sentence:sentences) {
      if (!Constants.USE_GOLD_PARSES && !replicateCoNLL) {
        // Remove tree from annotation and replace with parse using stanford parser
        sentence.remove(TreeCoreAnnotations.TreeAnnotation.class);
      } else {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        // generate the dependency graph
        try {
          SemanticGraph deps = SemanticGraphFactory.makeFromTree(tree,
              collapse, ccProcess, includeExtras, lemmatize, threadSafe);
          SemanticGraph basicDeps = SemanticGraphFactory.makeFromTree(tree,
              !collapse, ccProcess, includeExtras, lemmatize, threadSafe);
          sentence.set(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class, basicDeps);
          sentence.set(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class, deps);
        } catch(Exception e) {
          //logger.log(Level.WARNING, "Exception caught during extraction of Stanford dependencies. Will ignore and continue...", e);
        	throw e;
        }
      }
    }

    String preSpeaker = null;
    String curSpeaker = null;
    int utterance = -1;
    for (CoreLabel token:anno.get(CoreAnnotations.TokensAnnotation.class)) {
      if (!token.containsKey(CoreAnnotations.SpeakerAnnotation.class))  {
        token.set(CoreAnnotations.SpeakerAnnotation.class, "");
      }
      curSpeaker = token.get(CoreAnnotations.SpeakerAnnotation.class);
      if(!curSpeaker.equals(preSpeaker)) {
        utterance++;
        preSpeaker = curSpeaker;
      }
      token.set(CoreAnnotations.UtteranceAnnotation.class, utterance);
    }

    // Run pipeline
    stanfordProcessor.annotate(anno);

    for (CoreMap sentence:anno.get(CoreAnnotations.SentencesAnnotation.class)) {
      allWords.add(sentence.get(CoreAnnotations.TokensAnnotation.class));
      allTrees.add(sentence.get(TreeCoreAnnotations.TreeAnnotation.class));
    }

    // Initialize gold mentions
    List<List<Mention>> allGoldMentions = extractGoldMentions(conllDoc);

    List<List<Mention>> allPredictedMentions;
    if (Constants.USE_GOLD_MENTIONS) {
      //allPredictedMentions = allGoldMentions;
      // Make copy of gold mentions since mentions may be later merged, mentionID's changed and stuff
      allPredictedMentions = makeCopy(allGoldMentions);
    } else if (Constants.USE_GOLD_MENTION_BOUNDARIES) {
      allPredictedMentions = ((RuleBasedCorefMentionFinder) mentionFinder).filterPredictedMentions(allGoldMentions, anno, dictionaries);
    } else {
      allPredictedMentions = mentionFinder.extractPredictedMentions(anno, maxID, dictionaries);
    }

    try {
      recallErrors(allGoldMentions,allPredictedMentions,anno);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    edu.stanford.nlp.dcoref.Document doc = arrange(anno, allWords, allTrees, allPredictedMentions, allGoldMentions, true);
    doc.conllDoc = conllDoc;
    return doc;
*/
	return null;
  }
  
  /** Wrote by Chao Ma */
  public HashMap<Integer, edu.stanford.nlp.dcoref.Document> loadConllDocumentByStanford(String conllFilePath) {
	
	  //ArrayList<edu.stanford.nlp.dcoref.Document> allPartDocs = new ArrayList<edu.stanford.nlp.dcoref.Document>();
	  HashMap<Integer, edu.stanford.nlp.dcoref.Document> partIDMap = new HashMap<Integer, edu.stanford.nlp.dcoref.Document>();
	  
	  // 1) load all doc in a temp format
	  reader.initIteratorWithSingleConllFile(conllFilePath);
	  
	  // 2) convert then into stanford doc format
	  StanfordCoNLL2011DocReader.Document partDoc = reader.getNextDocumentFromSingleConllFile();
	  while (partDoc != null) {
	
	  	  int partID = parsePartID(partDoc.getPartNo());
		  System.out.println("Stanford system reading part " + partID + " document.");
		  edu.stanford.nlp.dcoref.Document stanfordDoc = loadPartDoc(partDoc);
		  partIDMap.put(partID, stanfordDoc);
		  
		  // next doc
		  partDoc = reader.getNextDocumentFromSingleConllFile();
	  }
	  
	  return partIDMap;
  }
  
  private int parsePartID(String str) {
	  return Integer.parseInt(str);
  }
  
  public edu.stanford.nlp.dcoref.Document loadPartDoc(StanfordCoNLL2011DocReader.Document conllDoc) {
	  List<List<CoreLabel>> allWords = new ArrayList<List<CoreLabel>>();
	  List<Tree> allTrees = new ArrayList<Tree>();

	  //CoNLL2011DocumentReader.Document conllDoc = reader.getNextDocument();
	  if (conllDoc == null) {
		  return null;
	  }
	  Annotation anno = conllDoc.getAnnotation();
	  List<CoreMap> sentences = anno.get(CoreAnnotations.SentencesAnnotation.class);
	  for (CoreMap sentence:sentences) {
		  if (!Constants.USE_GOLD_PARSES && !replicateCoNLL) {
			  // Remove tree from annotation and replace with parse using stanford parser
			  sentence.remove(TreeCoreAnnotations.TreeAnnotation.class);
		  } else {
			  Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
			  // generate the dependency graph
			  try {
				  //SemanticGraph deps = SemanticGraphFactory.makeFromTree(tree,
				//		  collapse, ccProcess, includeExtras, lemmatize, threadSafe);
				  //SemanticGraph basicDeps = SemanticGraphFactory.makeFromTree(tree,
					//	  !collapse, ccProcess, includeExtras, lemmatize, threadSafe);
				  SemanticGraph deps = SemanticGraphFactory.makeFromTree(tree,
			              SemanticGraphFactory.Mode.COLLAPSED, includeExtras, lemmatize, threadSafe);
			          SemanticGraph basicDeps = SemanticGraphFactory.makeFromTree(tree,
			              SemanticGraphFactory.Mode.BASIC, includeExtras, lemmatize, threadSafe);
				  sentence.set(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class, basicDeps);
				  sentence.set(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class, deps);
			  } catch (Exception e) {
				  //logger.log(Level.WARNING, "Exception caught during extraction of Stanford dependencies. Will ignore and continue...", e);
				  //throw  StackTraceElement(e);
				  System.err.println("Exception caught during extraction of Stanford dependencies. Will ignore and continue...");
			  }
		  }
	  }
	  String preSpeaker = null;
	  String curSpeaker = null;
	  int utterance = -1;
	  for (CoreLabel token:anno.get(CoreAnnotations.TokensAnnotation.class)) {
		  if (!token.containsKey(CoreAnnotations.SpeakerAnnotation.class))  {
			  token.set(CoreAnnotations.SpeakerAnnotation.class, "");
		  }
		  curSpeaker = token.get(CoreAnnotations.SpeakerAnnotation.class);
		  if(!curSpeaker.equals(preSpeaker)) {
			  utterance++;
			  preSpeaker = curSpeaker;
		  }
		  token.set(CoreAnnotations.UtteranceAnnotation.class, utterance);
	  }
	  // Run pipeline
	  stanfordProcessor.annotate(anno);

	  for (CoreMap sentence:anno.get(CoreAnnotations.SentencesAnnotation.class)) {
		  allWords.add(sentence.get(CoreAnnotations.TokensAnnotation.class));
		  allTrees.add(sentence.get(TreeCoreAnnotations.TreeAnnotation.class));
	  }

	  // Initialize gold mentions
	  List<List<Mention>> allGoldMentions = extractGoldMentions(conllDoc);

	  List<List<Mention>> allPredictedMentions;
	  if (Constants.USE_GOLD_MENTIONS) {
		  // Make copy of gold mentions since mentions may be later merged, mentionID's changed and stuff
		  allPredictedMentions = makeCopy(allGoldMentions);
	  } else if (Constants.USE_GOLD_MENTION_BOUNDARIES) {
		  allPredictedMentions = ((RuleBasedCorefMentionFinder) mentionFinder).filterPredictedMentions(allGoldMentions, anno, dictionaries);
	  } else {
		  allPredictedMentions = mentionFinder.extractPredictedMentions(anno, maxID, dictionaries);
	  }

	  edu.stanford.nlp.dcoref.Document doc = null;
	  try {
		  recallErrors(allGoldMentions,allPredictedMentions,anno);
		  doc = arrange(anno, allWords, allTrees, allPredictedMentions, allGoldMentions, true);
		  System.out.println("Generated new stanford doc! " + doc);
	  } catch (IOException e) {
		  throw new RuntimeException(e);
	  } catch (Exception e) {
		e.printStackTrace();
	}
	  
	  doc.conllDoc = conllDoc;
	  return doc;
  }

  public List<List<Mention>> makeCopy(List<List<Mention>> mentions) {
    List<List<Mention>> copy = new ArrayList<List<Mention>>(mentions.size());
    for (List<Mention> sm:mentions) {
      List<Mention> sm2 = new ArrayList<Mention>(sm.size());
      for (Mention m:sm) {
        Mention m2 = new Mention();
        m2.goldCorefClusterID = m.goldCorefClusterID;
        m2.mentionID = m.mentionID;
        m2.startIndex = m.startIndex;
        m2.endIndex = m.endIndex;
        m2.originalSpan = m.originalSpan;
        m2.dependency = m.dependency;
        sm2.add(m2);
      }
      copy.add(sm2);
    }
    return copy;
  }

  private static void recallErrors(List<List<Mention>> goldMentions, List<List<Mention>> predictedMentions, Annotation doc) throws IOException {
    List<CoreMap> coreMaps = doc.get(CoreAnnotations.SentencesAnnotation.class);
    int numSentences = goldMentions.size();
    for (int i=0;i<numSentences;i++){
      CoreMap coreMap = coreMaps.get(i);
      List<CoreLabel> words = coreMap.get(CoreAnnotations.TokensAnnotation.class);
      Tree tree = coreMap.get(TreeCoreAnnotations.TreeAnnotation.class);
      List<Mention> goldMentionsSent = goldMentions.get(i);
      List<Pair<Integer,Integer>> goldMentionsSpans = extractSpans(goldMentionsSent);

      for (Pair<Integer,Integer> mentionSpan: goldMentionsSpans){
        //logger.finer("RECALL ERROR\n");
        //logger.finer(coreMap + "\n");
        for (int x = mentionSpan.first; x < mentionSpan.second; x++){
          //logger.finer(words.get(x).value() + " ");
        }
        //logger.finer("\n"+tree + "\n");
      }
    }
  }

  private static List<Pair<Integer,Integer>> extractSpans(List<Mention> listOfMentions) {
    List<Pair<Integer,Integer>> mentionSpans = new ArrayList<Pair<Integer,Integer>>();
    for (Mention mention: listOfMentions){
      Pair<Integer,Integer> mentionSpan = new Pair<Integer,Integer>(mention.startIndex,mention.endIndex);
      mentionSpans.add(mentionSpan);
    }
    return mentionSpans;
  }

  public List<List<Mention>> extractGoldMentions(StanfordCoNLL2011DocReader.Document conllDoc) {
    List<CoreMap> sentences = conllDoc.getAnnotation().get(CoreAnnotations.SentencesAnnotation.class);
    List<List<Mention>> allGoldMentions = new ArrayList<List<Mention>>();
    CollectionValuedMap<String,CoreMap> corefChainMap = conllDoc.getCorefChainMap();
    for (int i = 0; i < sentences.size(); i++) {
      allGoldMentions.add(new ArrayList<Mention>());
    }
    int maxCorefClusterId = -1;
    for (String corefIdStr:corefChainMap.keySet()) {
      int id = Integer.parseInt(corefIdStr);
      if (id > maxCorefClusterId) {
        maxCorefClusterId = id;
      }
    }
    int newMentionID = maxCorefClusterId + 1;
    for (String corefIdStr:corefChainMap.keySet()) {
      int id = Integer.parseInt(corefIdStr);
      int clusterMentionCnt = 0;
      for (CoreMap m:corefChainMap.get(corefIdStr)) {
        clusterMentionCnt++;
        Mention mention = new Mention();

        mention.goldCorefClusterID = id;
        if (clusterMentionCnt == 1) {
          // First mention in cluster
          mention.mentionID = id;
          mention.originalRef = -1;
        } else {
          mention.mentionID = newMentionID;
          mention.originalRef = id;
          newMentionID++;
        }
        if(maxID < mention.mentionID) maxID = mention.mentionID;
        int sentIndex = m.get(CoreAnnotations.SentenceIndexAnnotation.class);
        CoreMap sent = sentences.get(sentIndex);
        mention.startIndex = m.get(CoreAnnotations.TokenBeginAnnotation.class) - sent.get(CoreAnnotations.TokenBeginAnnotation.class);
        mention.endIndex = m.get(CoreAnnotations.TokenEndAnnotation.class) - sent.get(CoreAnnotations.TokenBeginAnnotation.class);

        // will be set by arrange
        mention.originalSpan = m.get(CoreAnnotations.TokensAnnotation.class);

        // Mention dependency is collapsed dependency for sentence
        mention.dependency = sentences.get(sentIndex).get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);

        allGoldMentions.get(sentIndex).add(mention);
      }
    }
    return allGoldMentions;
  }

}