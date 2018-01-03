package edu.oregonstate.nlp.coref.berkeley;

import java.util.HashMap;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureVector.MentionPropertyFeature;
import edu.oregonstate.nlp.coref.featureVector.NominalMentionPropertyFeature;
import edu.oregonstate.nlp.coref.featureVector.mentionPropertyFeature.GramarRoll;
import edu.oregonstate.nlp.coref.featureVector.mentionPropertyFeature.MentionLength;
import edu.oregonstate.nlp.coref.featureVector.mentionPropertyFeature.MentionTypeProunSpecified;
import edu.oregonstate.nlp.coref.structuredClassifiers.GreedyPolicy;


/** Implement some features according to Easy-Victory paper of EMNLP2013
 *  Features in use:
 *  
 *    MentionType
 *    MentionLength
 *    MettionGramarRole
 *  
 */
public class BerkeleyFeatureGenerator {

	NominalMentionPropertyFeature menTypFeat = new MentionTypeProunSpecified();
	MentionPropertyFeature menLenFeat = new MentionLength();
	NominalMentionPropertyFeature menGramFeat = new GramarRoll();
	int maxL = 10;
	
	// congjuction of mention length and mention type
	public double[] featureVecLengthANDMentionType(Annotation mention, Document doc) {
		HashMap<MentionPropertyFeature, String> featVector = new HashMap<MentionPropertyFeature, String>();
		
		int range1 = maxL + 1;
		int range2 = menTypFeat.getValues().length;
		
		int value1 = Integer.parseInt(menLenFeat.produceValue(mention, doc, featVector));
		String value2Str = menTypFeat.produceValue(mention, doc, featVector);
		int value2 = 0;
		for (int i = 0; i < menTypFeat.getValues().length; i++) {
			String possibleV = menTypFeat.getValues()[i];
			if (possibleV.equals(value2Str)) {
				value2 = 1;
			}
		}
		
		int l = range1 * range2;
		int v = range1 * value2 + value1;
		double[] featv = new double[l];
		GreedyPolicy.vectorClearZero(featv);
		featv[v] = 1;
		
		return featv;
	}
	
	public double[] featureVecMentionType(Annotation mention, Document doc) {
		HashMap<MentionPropertyFeature, String> featVector = new HashMap<MentionPropertyFeature, String>();
		double[] featv = new double[menTypFeat.getValues().length];
		
		String myValue = menTypFeat.produceValue(mention, doc, featVector);
			
		GreedyPolicy.vectorClearZero(featv);
		for (int i = 0; i < menTypFeat.getValues().length; i++) {
			String possibleV = menTypFeat.getValues()[i];
			if (possibleV.equals(myValue)) {
				featv[i] = 1;
			}
		}
		
		return featv;
	}
	
	public double[] featureVecLength(Annotation mention, Document doc) {
		HashMap<MentionPropertyFeature, String> featVector = new HashMap<MentionPropertyFeature, String>();
		
		double[] featv = new double[maxL + 1];
		
		GreedyPolicy.vectorClearZero(featv);
		int len = Integer.parseInt(menLenFeat.produceValue(mention, doc, featVector));
		if (len <= maxL) {
			featv[len - 1] = 1;
		} else { // just say it is > 10
			featv[maxL] = 1;
		}
		
		return featv;
	}
	
	public double[] featureVecGramarRole(Annotation mention, Document doc) {
		HashMap<MentionPropertyFeature, String> featVector = new HashMap<MentionPropertyFeature, String>();
		
		String myValue = menGramFeat.produceValue(mention, doc, featVector);
		
		double[] featv = new double[menGramFeat.getValues().length];
		GreedyPolicy.vectorClearZero(featv);
		for (int i = 0; i < menGramFeat.getValues().length; i++) {
			String possibleV = menGramFeat.getValues()[i];
			if (possibleV.equals(myValue)) {
				featv[i] = 1;
			}
		}
		
		return featv;
	}

	
	public double[] featureSuitabilityAsAnaphoric(Annotation mention, Document doc) {

		// an indiecator bit feature
		double[] indicator = new double[1];
		indicator[0] = 1;
		
		double[] mlen = featureVecLength(mention, doc);
		double[] mtyp = featureVecMentionType(mention, doc);
		double[] mgrm = featureVecGramarRole(mention, doc);
		
		double[] conjunctionVec = cancatenateVector(mlen, mtyp);
		double[] conjunctionVec1 = cancatenateVector(conjunctionVec, mgrm);
		double[] finalVec = cancatenateVector(indicator, conjunctionVec1);
		return finalVec;
	}
	
	public int getFeatureVectorDimension() {
		int range1 = maxL + 1;
		int range2 = menTypFeat.getValues().length;
		int range3 = menGramFeat.getValues().length;
		int finalLen = range1 + range2 + range3 + 1; // 1 is for indicator
		return finalLen;
	}
	
	//public double[] generateFeatureForNEWaction() {
	//	return null;
	//}
	
	public double[] cancatenateVector(double[] v1, double[] v2) {
		double[] conjunctionVec = new double[v1.length + v2.length];
		int k = 0;
		for (int i = 0; i < v1.length; i++) {
			conjunctionVec[k] = v1[i];
			k++;
		}
		for (int j = 0; j < v2.length; j++) {
			conjunctionVec[k] = v2[j];
			k++;
		}
		return conjunctionVec;
	}

	
	public void printForDebug(Annotation mention, Document doc) {
		HashMap<MentionPropertyFeature, String> featVector = new HashMap<MentionPropertyFeature, String>();
		String v1 = menTypFeat.produceValue(mention, doc, featVector);
		String v2 = menLenFeat.produceValue(mention, doc, featVector);
		String v4 = menGramFeat.produceValue(mention, doc, featVector);
		
		double[] vec2 = featureVecMentionType(mention, doc);
		double[] vec1 = featureVecLength(mention, doc);
		double[] vec3 = featureVecLengthANDMentionType(mention, doc);
		
		double[] vecf = featureSuitabilityAsAnaphoric(mention, doc);
		
		assert(getFeatureVectorDimension() == vecf.length);
		assert(!allZero(vecf));
		
		printVecSparse(vecf);

		System.out.println(v1 + " " + v2 + " " + v4 + "   " + vec1.length + "-" + vec2.length + "-" + vec3.length);
	}
	
	private boolean allZero(double[] v) {
		for (int i = 0; i < v.length; i++) {
			if (v[i] != 0) {
				return false;
			}
		}
		return true;
	}
	
	private void printVecSparse(double[] v) {
		for (int i = 0; i < v.length; i++) {
			if (v[i] != 0) {
				int id = i + 1;
				System.out.print(id + ":" + v[i] + " ");
			}
		}
		System.out.println();
	}
}
