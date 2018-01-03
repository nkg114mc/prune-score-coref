package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.berkeley.nlp.futile.fig.basic.Indexer;
import edu.berkeley.nlp.futile.util.Counter;
import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.berkeley.BerkeleyFeatureGenerator;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.ClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.MentionPropertyFeature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;
import edu.oregonstate.nlp.coref.features.Binarizer;
import edu.oregonstate.nlp.coref.features.FeatureUtils;
import edu.oregonstate.nlp.coref.general.Constants;

public class StateActionFeaturizer {

	private String clusterFeatures[]={"SameGender", "CompatibleGender", "IncompatibleGender", "SameNumber", "CompatibleNumber", "IncompatibleNumber",
			"IncompatiblePersonName","SameAnimacy", "CompatibleAnimacy", "IncompatibleAnimacy", "SameSemType", "CompatibleSemType",// "IncompatibleSemType",
			"HeadMatch", "ProperName", "PNStr", "PNSubstr", "PNIncomp", "ContainsPN"
			,"Constraints"
			//, "ChainSize1", "ChainSize2", "CombinedSize"
			//      ,"ChainSize1GT5", "ChainSize1LTE1", "ChainSize1LTE2", "ChainSize1LTE3", "ChainSize1LTE5"
			//, "ChainSize1LTE1", "ChainSize2LTE1"
			//      ,"ChainSize2GT5", "ChainSize2LTE1", "ChainSize2LTE2", "ChainSize2LTE3", "ChainSize2LTE5"
			//,"CombinedSizeGT20","CombinedSizeLTE2","CombinedSizeLTE3","CombinedSizeLTE4","CombinedSizeLTE5","CombinedSizeLTE10","CombinedSizeLTE20"
			//,"NormChainSize1","NormChainSize2"
			//      ,"ParSizeLTE1","ParSizeLTE2","ParSizeLTE3","ParSizeLTE5","ParSizeGT5"
			//      ,"ParSize2LTE1","ParSize2LTE2","ParSize2LTE3","ParSize2LTE5","ParSize2GT5"
			,"PairTypeEE","PairTypeEL","PairTypeEP","PairTypeLL","PairTypeOE"
			,"PairTypeNE","PairTypeNL","PairTypeNP","PairTypeNO","PairTypeNN"
			,"PairTypeOL","PairTypeOO","PairTypeOP","PairTypePP","PairTypeLP"
			,"NormCombinedSize", "PossibleAnte"
			//,"ProResolveCl"
			,"ProResolveRuleR1","ProResolveRuleR2","ProResolveRuleR3","ProResolveRuleR5","ProResolveRuleR6","ProResolveRuleR7","ProResolveRuleR8"
			,"Demonyms", "CountryCapital", "WordSubstr", "HeadWordSubstr", "Modifier", "PostModifier"//, "Confidence","Confidence1","Confidence2"
	};

	private String pairwiseFeatures[] = {
			"ProComp", 
			"SoonStr", "Modifier", "PostModifier", "WordsSubstr",
			//WordsStr, WordOverlap, , ExactStrMatch
			//FEATURE_NAMES=PNStr, PNSubstr
			"Pronoun1", "Pronoun2", "Definite1", "Definite2", "Demonstrative2",	"Embedded1", "Embedded2", "InQuote1", "InQuote2",
			"BothProperNouns", "BothEmbedded", "BothInQuotes", "BothPronouns", "BothSubjects", 
			"Subject1", "Subject2", "Appositive", "RoleAppositive", "MaximalNP", "IwithinI",
			"SentNum0", "SentNum1", "SentNum2", "SentNum3", "SentNum4plus", "ParNum0", "ParNum1", "ParNum2plus" ,
			"Acronym", "Alias", "IAntes", "WeAntes", "BothYou", "Span", "Binding", "Contraindices", "Syntax", "ClosestComp" ,
			"Indefinite", "Indefinite1", "Prednom", "Pronoun", "ProperNoun" ,
			//ContainsPN
			//, ProperName
			"WordNetClass", "WordNetDist", "WordNetSense", "Subclass", "AlwaysCompatible",
			//FEATURE_NAMES=RuleResolve 
			//FEATURE_NAMES=SameSentence, ConsecutiveSentences
			"WNSynonyms",
			//FEATURE_NAMES=ProResolve, ProResolveRule 
			"Quantity",
			//FEATURE_NAMES=HeadMatch 
			"WhoResolve", "WhichResolve",
			"PairType",
			"DeterminerHeadMatch", "Longer2", "LongerPN2", "ShorterPN2", "InOfRelation",//,"__PairType__SentNum"
			"PairClassifierScore"
	};
	
	private String mentionFeatures[] = {
			"MentionClassifierScore", "NOPBias"
	};
	
	private String NOP_Bias = "NOP_Bias";
	
	//////////////////////////
	//public Counter<Integer> fcounter = new Counter<Integer>();
	public Indexer<String> featureIndxer;
	
	// entity feature generator
	BerkeleyFeatureGenerator bkleyFeatureGen = new BerkeleyFeatureGenerator();

	private Binarizer binarizer;
	private List<ClusterFeature> clFeatures;
	private List<Feature> localFeatures;
	private List<MentionPropertyFeature> mentFeatures;
	private int numLocalFeatures;
	private int numClusterFeatures;
	private int numMentFeatures;

	public StateActionFeaturizer() {
		featureIndxer = new Indexer<String>();
		clFeatures = Constructor.createClusterFeatures(clusterFeatures);
		localFeatures = Constructor.createFeatures(pairwiseFeatures);
		binarizer = new Binarizer(localFeatures);
		mentFeatures = Constructor.createMentionFeatures(mentionFeatures); 
		
		String[] localF = binarizer.getFeatureNames(localFeatures);
		numLocalFeatures = localF.length;
		numClusterFeatures = clFeatures.size();
		numMentFeatures = mentFeatures.size();
		System.out.println("MentionPair Feature = " + numLocalFeatures);
		System.out.println("Cluster     Feature = " + numClusterFeatures);
		//System.out.println("         Feature = " + 1);
		System.out.println("NOP&Mention Feature = " + numMentFeatures);
	}
	
	public void initFeatureIndexer() {
		
		// mention pair features
		for (Feature f : localFeatures) {
			List<String> vals = new ArrayList<String>();
			if (f.isNominal()) {
				String fname = f.getName();
				String featPrefix = fname + "=";
				String[] values = ((NominalFeature) f).getValues();
				if (values.length <= 2) {
					if (FeatureUtils.memberArray(NominalFeature.COMPATIBLE,values))
						vals.add(NominalFeature.COMPATIBLE);
					else if(FeatureUtils.memberArray("Y",values))
						vals.add("Y");
					else
						vals.add(values[0]);
				} else {
					for (String val : values) {
						vals.add(val);
					}
				}
			} else{
				//numBinaryFeatures++;
			}

		}
			    
		// cluster features
		for (ClusterFeature clf : clFeatures) {
			
		}

	}

	private int getFeatureDimension()
	{
		int d = numLocalFeatures + numClusterFeatures + numMentFeatures;
		return d;
	}
	
	private int getPairwiseFeatureDimension()
	{
		return numLocalFeatures;
	}
	
	private int getClusterFeatureDimension()
	{
		return numClusterFeatures;
	}
	
	private int getSingleMentFeatureDimension()
	{
		return numMentFeatures;
	}
	
	public double[] featurizeStateAction(PartialOutputState state, Action act) {
		double[] fvec = genActionFeatVec(state.getChains(), state.getDoc(), act);
		return fvec;
	}

	private double[] getFeatureVector(CorefChain c1, CorefChain c2, Document doc, boolean inclStructured) {
		List<ClusterFeature> feats = clFeatures;
		double[] result = new double[feats.size()];
		HashMap<ClusterFeature, String> featVector = new HashMap<ClusterFeature, String>();
		for (int i = 0; i < feats.size(); i++) {
			result[i] = Double.parseDouble(feats.get(i).getValue(c1, c2, doc, featVector));
		}
		return result;
	}

	private double[] getLocalFeatureVector(Annotation np1, Annotation np2, Document doc) {
		List<Feature> feats = localFeatures;
		HashMap<Feature, String> featVector = new HashMap<Feature, String>();
		for (int i = 0; i < feats.size(); i++) {
			feats.get(i).getValue(np1, np2, doc, featVector);
		}
		double[] fv = binarizer.binarize(featVector);
		return fv;
	}
	
	private double[] getSingleMentionFeatureVector(Annotation np, Document doc) {
		List<MentionPropertyFeature> mfeats = mentFeatures;
		double[] result = new double[mfeats.size()];
		HashMap<MentionPropertyFeature, String> featVector = new HashMap<MentionPropertyFeature, String>();
		for (int i = 0; i < mfeats.size(); i++) {
			result[i] = Double.parseDouble(mfeats.get(i).getValue(np, doc, featVector));
		}
		return result;
	}

	private double[] genActionFeatVec(HashMap<Integer,CorefChain> chainState, Document doc, Action act) // weightVec
	{
		double[] emtpyArr = new double[256];
		double featVector[] = emtpyArr;
		int ACT_MERGE = 1;
		int ACT_NOP = 0; 

		double nMentionPair = 0;
		double insideClusterEdge[] = new double[256];
		double betweenClusterEdge[] = new double[256];
		int insideLength = 0;
		int outsideLength = 0;

		// clear zero
		vectorClearZero(insideClusterEdge);
		vectorClearZero(betweenClusterEdge);

		// FEATURE - FEATURE - FEATURE - FEATURE - FEATURE - FEATURE - FEATURE
		// ----------------------------------------------------------------
		int actType = act.getActName();
		// 0 -> 1
		if (actType == ACT_MERGE) {
			Annotation involvedMen = null;
			CorefChain involvedClust = null;
			CorefChain singletonClust = null;

			// involved cluster
			involvedClust = chainState.get(act.second);
			// singleton cluster
			singletonClust = chainState.get(act.first);

			// involved mention
			involvedMen = singletonClust.getFirstCe();//menClust.getFirstCe();

			// mention pairs ---------------------------
			for (Annotation ce2 : involvedClust.getCes()) {
				Integer thisMid = Integer.parseInt(ce2.getAttribute(Constants.CE_ID));
				Integer involovedMid = Integer.parseInt(involvedMen.getAttribute(Constants.CE_ID));
				if (thisMid != involovedMid) {
					Annotation first = involvedMen;
					Annotation second = ce2;
					if (ce2.compareSpan(involvedMen) < 0) {
						first = ce2;
						second = involvedMen;
					}
					double tmparr[] = getLocalFeatureVector(first, second, doc);
					// - \phi_0 + \phi_1
					// Merge action concern the similarity btween mention and entity, 
					// we use positive score to represent similarity
					for (int i = 0; i < tmparr.length; i++) {
						insideClusterEdge[i] += (tmparr[i]);
					}
					insideLength = tmparr.length;
					nMentionPair++;
				}
			}

			// cluster features ----------------------------------
			// Merge action concern the similarity between mention and entity, 
			// we use positive score to represent similarity
			CorefChain firstCl = involvedClust;
			CorefChain secondCl = singletonClust;
			if (singletonClust.before(involvedClust)) {
				firstCl = singletonClust;
				secondCl = involvedClust;
			}
			double tmparr2[] = getFeatureVector(firstCl, secondCl, doc, true);
			// merge measure the similarity
			for (int i = 0; i < tmparr2.length; i++) {
				betweenClusterEdge[i] += (1 * tmparr2[i]);
			}
			outsideLength = tmparr2.length;

			// =======================================
			int totalLength = insideLength + outsideLength + getSingleMentFeatureDimension();
			int trueDem     = getFeatureDimension();
			if (totalLength != trueDem) {
				throw new RuntimeException("Wrong feature vector demension! "+totalLength+" which should be "+trueDem);
			}

			featVector = new double[totalLength];
			vectorClearZero(featVector);
			for (int j = 0; j < insideLength; j++) { // inside vector
				featVector[j] = (insideClusterEdge[j] / nMentionPair); // average of all mention pairs between a metion all other mentions in a cluster
			}
			for (int k = 0; k < outsideLength; k++) { // outside vector
				int k2 = k + insideLength;
				featVector[k2] = betweenClusterEdge[k];
			}
			// mention features
			//featVector[totalLength - 1] = 0; // this is not a no-op
			// =======================================
			// 1 -> 0
		} else if (actType == ACT_NOP) {
			int totalLength = getFeatureDimension();//getBinarizer().getNumBinaryFeatures()+getClusterFeatures().size() + 1;
			featVector = new double[totalLength];
			vectorClearZero(featVector);
			
			// involved mention
			CorefChain singletonClust = chainState.get(act.first);
			Annotation involvedMen = singletonClust.getFirstCe();
			
			double[] singleMentsFeats = getSingleMentionFeatureVector(involvedMen, doc);
			// threshold 
			// bias
			int startm = getPairwiseFeatureDimension() + getClusterFeatureDimension();
			for (int l = 0; l < singleMentsFeats.length; l++) { // outside vector
				int l2 = l + startm;
				featVector[l2] = singleMentsFeats[l];
			}
			//featVector[totalLength - 1] = 1; // this is a no-op
		} else {
			throw new RuntimeException("huerPhi: Unknown action type!");
		}
		// ----------------------------------------------------------------
		// FEATURE - FEATURE - FEATURE - FEATURE - FEATURE - FEATURE - FEATURE		

		//printVector(featVector);
		//showVector(insideClusterEdge);
		return featVector;
	}
	
/*
	// new policy feature vector (2013-8-4)
	private double[] genActionFeatVec2(HashMap<Integer,CorefChain> chainState, Document doc, AnnotationSet ces, Action act) // weightVec
	{
		double[] emtpyArr = new double[256];
		double featVector[] = emtpyArr;
		int ACT_MERGE = 1;
		int ACT_NOP = 0; 

		double insideClusterEdge[] = new double[256];
		double betweenClusterEdge[] = new double[256];
		getLocalFeatures().size();
		getClusterFeatures().size();
		int insideLength = 0;
		int outsideLength = 0;

		// clear zero
		vectorClearZero(insideClusterEdge);
		vectorClearZero(betweenClusterEdge);

		// FEATURE - FEATURE - FEATURE - FEATURE - FEATURE - FEATURE - FEATURE
		// ----------------------------------------------------------------
		int actType = act.getActName();
		// 0 -> 1
		if (actType == ACT_MERGE) {
			Annotation involvedMen = null;
			CorefChain involvedClust = null;
			CorefChain singletonClust = null;

			// involved cluster
			involvedClust = chainState.get(act.second);
			// singleton cluster
			singletonClust = chainState.get(act.first);

			// involved mention
			involvedMen = singletonClust.getFirstCe();//menClust.getFirstCe();

			// mention pairs ---------------------------
			Annotation bestLinkMention = null;
			bestLinkMention = involvedClust.getFirstCe();

			Annotation first = involvedMen;
			Annotation second = bestLinkMention;
			if (bestLinkMention.compareSpan(involvedMen) < 0) {
				first = bestLinkMention;
				second = involvedMen;
			}
			insideClusterEdge = getLocalFeatureVector(first, second, doc);
			insideLength = insideClusterEdge.length;


			// cluster features ----------------------------------
			// Merge action concern the similarity between mention and entity, 
			// we use positive score to represent similarity
			CorefChain firstCl = involvedClust;
			CorefChain secondCl = singletonClust;
			if (singletonClust.before(involvedClust)) {
				firstCl = singletonClust;
				secondCl = involvedClust;
			}
			double tmparr2[] = getFeatureVector(firstCl, secondCl, doc, true);
			// merge measure the similarity
			for (int i = 0; i < tmparr2.length; i++) {
				betweenClusterEdge[i] += (tmparr2[i]);
			}
			outsideLength = tmparr2.length;

			// =======================================
			int totalLength = insideLength + outsideLength + 1;
			//int trueDem     = getBinarizer().getNumBinaryFeatures()+getClusterFeatures().size() + 1;
			//if (totalLength != trueDem) {
			//	throw new RuntimeException("Wrong feature vector demension! "+totalLength+" which should be "+trueDem);
			//}

			featVector = new double[totalLength];
			for (int j = 0; j < insideLength; j++) { // inside vector
				featVector[j] = insideClusterEdge[j]; // average of all mention pairs between a metion all other mentions in a cluster
			}
			for (int k = 0; k < outsideLength; k++) { // outside vector
				int k2 = k + insideLength;
				featVector[k2] = betweenClusterEdge[k];
			}
			// threhold
			featVector[totalLength - 1] = 0; // this is not a no-op
			// =======================================
			// 1 -> 0
		} else if (actType == ACT_NOP) {
			int totalLength = getBinarizer().getNumBinaryFeatures()+getClusterFeatures().size() + 1;
			//int totalLength = getBinarizer().getNumBinaryFeatures() + 1;
			featVector = new double[totalLength];

			// threhold
			featVector[totalLength - 1] = 1; // this is a no-op
		} else {
			throw new RuntimeException("huerPhi: Unknown action type!");
		}
		// ----------------------------------------------------------------
		// FEATURE - FEATURE - FEATURE - FEATURE - FEATURE - FEATURE - FEATURE		

		//showVector(insideClusterEdge);
		//return featVector; 
		return featVector;
	}
*/
	public static void printVector(double var[]) {
		for (int i = 0; i < var.length; i++) {
			System.out.print(var[i] + " ");
		}
		System.out.println("");
	}
	
	 public static  void vectorClearZero(double var[])
	 {
		 for (int i = 0; i < var.length; i++) {
			 var[i] = 0;
		 }
	 }
	 
	 public void printFeatNames() {
		String[] name1 = binarizer.getFeatureNames(localFeatures);
		String[] name2 = new String[clFeatures.size()];
		for (int i = 0; i < name2.length; i++) {
			name2[i] = clFeatures.get(i).getName();
		}
		
		System.out.println("===================");
		// feature names
		int total = 0;
		for (int j = 0; j < name1.length; j++) {
			total++;
			System.out.println("f" + total + ": " + name1[j]);
		}
		for (int j = 0; j < name2.length; j++) {
			total++;
			System.out.println("f" + total + ": " + name2[j]);
		}
		total++;
		System.out.println("f" + total + ": " + "NOPBit");
		System.out.println("===================");
	 }
}
