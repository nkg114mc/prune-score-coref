package edu.oregonstate.nlp.coref.oregonstate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.OutputStream;
import java.util.TreeMap;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureExtractor.Annotator;

import com.google.common.collect.Maps;

import edu.oregonstate.nlp.coref.Constructor;
import edu.oregonstate.nlp.coref.SystemConfig;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationComparatorNestedLast;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.FeatureWriter;
import edu.oregonstate.nlp.coref.featureVector.FeatureWriterARFF;
import edu.oregonstate.nlp.coref.features.properties.*;
import edu.oregonstate.nlp.coref.features.properties.Number;
import edu.oregonstate.nlp.coref.filter.AllPairs;
import edu.oregonstate.nlp.coref.filter.PairGenerator;
import edu.oregonstate.nlp.coref.general.AnnotationSetting;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.Utils;
import edu.oregonstate.nlp.coref.scorers.Matcher;
import edu.oregonstate.nlp.coref.scorers.Matcher.MatchStyleEnum;



public class OregonStateConllPreprocess {

	private static PairGenerator mPairGen = null;
	
	private static Map<String, String[]> mElSetNames = null;
	private static Map<String, Annotator> mElements = null;
	
	
	private static SystemConfig config;
	private static Property allProperties[] = null;

	public OregonStateConllPreprocess(SystemConfig systemConfig) {
		config = systemConfig;
		
		getPairGenerator();
		getElements();
	}
	
	private static PairGenerator getPairGenerator()
	{
		if (mPairGen == null) {
			mPairGen = new AllPairs();
		}
		return mPairGen;
	}

	private static void getElements() {
		mElSetNames = AnnotationSetting.getPreprocessingElSetNames();
		List<String> elNames = AnnotationSetting.getPreprocessingElements();
		
		// Initialize the element
		if (mElements == null) {
			mElements = Maps.newHashMap();
		}

		for (String el : elNames) {
			Annotator a = mElements.get(el);
			if (a == null) {
				a = Constructor.createInternalAnnotator(el);
				mElements.put(el, a);
			}
		}
	}
	
///////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////
	
	public void preprocess(Document doc, boolean overwrite)
	{
		throw new RuntimeException("Not implemented ...");
		/*
		for (int j = 0; j < elNames.size(); j++) {
			String name = elNames.get(j);
			System.out.println("EleNames["+j+"]="+name);
			Annotator element = elements.get(name);
			element.run(doc, elSetNames.get(name), overwrite);
		}*/
	}


///////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////


	public static HashMap<Feature, String> makeVector(Annotation np1, Annotation np2, List<Feature> featureList, Document doc)
	{
		HashMap<Feature, String> result = new HashMap<Feature, String>();

		for (Feature feat : featureList) {
			feat.getValue(np1, np2, doc, result);
		}

		return result;
	}
/*
	public static void numberAnnotations(AnnotationSet an)
	{
		Annotation[] ordered = an.getOrderedAnnots(new AnnotationComparatorNestedLast());
		int counter = 1;
		for (Annotation a : ordered) {
			a.setAttribute(Constants.CE_ID, Integer.toString(counter));
			counter++;
		}
	}
*/
	public static HashMap<Feature, String> makeVectorTimed(Annotation np1, Annotation np2, List<Feature> featureList, Document doc)
	{
		HashMap<Feature, String> result = new HashMap<Feature, String>();
		for (Feature feat : featureList) {
			feat.getValue(np1, np2, doc, result);
		}
		return result;
	}


	private static void outputNPProperties(Document doc, AnnotationSet nps)
	{
		// Output all the NP properties that were computed
		AnnotationSet properties = new AnnotationSet(Constants.PROPERTIES_FILE_NAME);

		for (Annotation np : nps) {
			Map<String, String> npProps = new TreeMap<String, String>();
			Map<Property, Object> props = np.getProperties();
			if (props != null && props.keySet() != null) {
				for (Property p : props.keySet()) {
					npProps.put(p.toString(), Utils.printProperty(props.get(p)));
				}
			}

			String num = np.getAttribute(Constants.CE_ID);
			npProps.put(Constants.CE_ID, num);
			npProps.put("Text", doc.getAnnotText(np));
			properties.add(np.getStartOffset(), np.getEndOffset(), "np", npProps);
		}

		//doc.writeAnnotationSet(properties);

	}

/*
	public static void makeFeaturesBatch(Iterable<Document> docs, boolean training)
	{
		PairGenerator pairGen = mPairGen;//Constructor.makePairGenClass(pairGenName);

		
		int i = 0;
		int numNPs = 0;

		for (Document doc : docs) {
			doc.loadAnnotationSets();
			AnnotationSet basenp = makeFeatures(training, featureList, pairGen, i, doc);
			numNPs += basenp.size();

			System.out.println("MMMatching NPs ...");
			AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
			AnnotationSet gsnps = doc.getAnnotationSet(Constants.GS_NP);
			System.out.println("Matching Ontonotes document ...");
			Matcher.matchAnnotationSets(gsnps, nps, MatchStyleEnum.ONTO, doc);
		}

		System.out.println("Markables: " + numNPs + " found, -- " + Matcher.totalNPsMatched + " matched");
		System.out.println("Markables: " + Matcher.totalKey + " in key, -- " + Matcher.numMatchedKey + " matched");
		System.out.println("Markables: " + Matcher.doubleMatches + " double matches.");

		Matcher.nullifyCounters();
	}


	private static AnnotationSet makeFeatures(boolean training, List<Feature> featureList, PairGenerator pairGen, int i, Document doc)
	{
		OutputStream output = doc.writeFeatureFile();
		FeatureWriter writer;
		//if (write_binary) {
		//	writer = new FeatureWriterARFFBinarized(featureList, output);
		//} else {
			writer = new FeatureWriterARFF(featureList, output);
		//}

		writer.printHeader();
		// revised by Chao
		AnnotationSet basenp = doc.getAnnotationSet(Constants.NP);
		if (basenp == null) {
			throw new RuntimeException("Did not find NP annotation set ...");
		}

		fillProperty(basenp, doc);
		outputNPProperties(doc, basenp);

		Annotation[] basenpArray = basenp.toArray();
		System.out.println("Document " + (i + 1) + ": " + doc.getAbsolutePath() + " (" + basenpArray.length + " nps)");

		// Initialize the pair generator with the new document
		pairGen.initialize(basenpArray, doc, training);

		while (pairGen.hasNext()) {
			Annotation[] pair = pairGen.nextPair();
			Annotation np1 = pair[0], np2 = pair[1];
			HashMap<Feature, String> values = makeVectorTimed(np1, np2, featureList, doc);
			writer.printInstanceVector(values);
		}

		return basenp;
	}
*/
	
	public static void fillProperty(Document doc) {
		AnnotationSet nps = doc.getPredictMentionSet();//.getAnnotationSet(Constants.NP);
		fillProperty(nps, doc);
	}
	public static void fillProperty(AnnotationSet nps, Document doc) {
		if (allProperties == null) prepareProperty();
		for (Annotation np : nps) {
			for (Property p : allProperties) {
				Object v = p.getValueProp(np, doc);
				np.setProperty(p, v);
			}
		}
	}

	public static void prepareProperty() {
		String propertNames[] = {
				"AllGramRole","Conjunction","Definite","Gender","InQuote","NPSemanticType","Pronoun",
				//"Property",
				"SubsumesNumber","WNSemClass","AllModifiers","ContainsAcronym","Demonstrative",
				"GramRole","MaximalNP","Number","ProperName","SentNum","Synsets","Words",
				"Animacy","ContainsProperName","Embedded","HeadNoun","Modifier","ParNum",
				"ProperNameType","SoonWords","Title","ClosestCompliment",//"CorefID","EmptyProperty",
				"InfWords",//"NormalizedID",
				"PostModifier","ProperNoun","Stopword","UniqueWords"	
		};

		Property[] props = new Property[propertNames.length];

		props[0] = AllGramRole.getInstance();
		props[1] = Conjunction.getInstance();
		props[2] = Definite.getInstance();
		props[3] = Gender.getInstance();
		props[4] = InQuote.getInstance();
		props[5] = NPSemanticType.getInstance();
		props[6] = Pronoun.getInstance();
		props[7] = SubsumesNumber.getInstance();
		props[8] = WNSemClass.getInstance();
		props[9] = AllModifiers.getInstance();
		props[10] = ContainsAcronym.getInstance();
		props[11] = Demonstrative.getInstance();
		props[12] = GramRole.getInstance();
		props[13] = MaximalNP.getInstance();
		props[14] = Number.getInstance();
		props[15] = ProperName.getInstance();
		props[16] = SentNum.getInstance();
		props[17] = Synsets.getInstance();
		props[18] = Words.getInstance();
		props[19] = Animacy.getInstance();
		props[20] = ContainsProperName.getInstance();
		props[21] = Embedded.getInstance();
		props[22] = HeadNoun.getInstance();
		props[23] = Modifier.getInstance();
		props[24] = ParNum.getInstance();
		props[25] = ProperNameType.getInstance();
		props[26] = SoonWords.getInstance();
		props[27] = Title.getInstance();
		props[28] = ClosestCompliment.getInstance();
		props[29] = InfWords.getInstance();
		props[30] = PostModifier.getInstance();
		props[31] = ProperNoun.getInstance();
		props[32] = Stopword.getInstance();
		props[33] = UniqueWords.getInstance();

		allProperties = props;
	}
}
