package edu.oregonstate.nlp.coref.features;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;

public class VicentNgBinarizer {
  private HashMap<Feature, ArrayList<String>> valueMap;
  private List<Feature> featureList;
  private int numBinaryFeatures;

  public VicentNgBinarizer(List<Feature> ftrs){
    featureList = ftrs;
    numBinaryFeatures=0;
    valueMap = new HashMap<Feature, ArrayList<String>>();
    for (Feature f : ftrs) {

      if (f.isNominal()) {
        String[] values = ((NominalFeature) f).getValues();
        ArrayList<String> vals = new ArrayList<String>();
        if (values.length <= 2) {
          numBinaryFeatures++;
          if(FeatureUtils.memberArray(NominalFeature.COMPATIBLE,values))
            vals.add(NominalFeature.COMPATIBLE);
          else if(FeatureUtils.memberArray("Y",values))
            vals.add("Y");
          else
            vals.add(values[0]);
        }
        else {
          for (String val : values) {
            vals.add(val);
            numBinaryFeatures++;
          }
        }
        valueMap.put(f, vals);
      }/*else{
        numBinaryFeatures++;
      }*/
    }

  }
  public double[] binarize(HashMap<Feature, String> vals){
    if (vals == null || vals.size() < 1) throw new RuntimeException("Empty feature value list");
    double[] result = new double[numBinaryFeatures];
    for (int i = 0, ind=0; i < featureList.size(); i++) {
      Feature f = featureList.get(i);
      String s = vals.get(f);
      if (featureList.get(i).isNominal()) {
        ArrayList<String> nomValues = valueMap.get(featureList.get(i));
        for (String nVal : nomValues) {
          if (s.equalsIgnoreCase(nVal)) {
            result[ind++]=1.0;
          }
          else {
            result[ind++]=0.0;
          }
        }
      }else {
        if (featureList.get(i).isString()) {
          throw new RuntimeException("Can't handle string features");
        }
        result[ind++]=Double.parseDouble(s);

      }
    }
    return result;
  }
  
  public double[] binarizeOnlyOne(Feature feature, String value){
	  //if (vals == null || vals.size() < 1) throw new RuntimeException("Empty feature value list");
	  double[] result = new double[0];
	  int ind = 0;
	  if (feature.isNominal()) {
		  ArrayList<String> nomValues = valueMap.get(feature);
		  result = new double[nomValues.size()];
		  for (String nVal : nomValues) {
			  if (value.equalsIgnoreCase(nVal)) {
				  result[ind++]=1.0;
			  } else {
				  result[ind++]=0.0;
			  }
		  }
	  }
	  return result;
  }
  
  public double[] VNbinarize(HashMap<Feature, String> vals){
	  /*
	    if (vals == null || vals.size() < 1) throw new RuntimeException("Empty feature value list");
	    double[] result = new double[numBinaryFeatures];
	    for (int i = 0, ind=0; i < featureList.size(); i++) {
	      Feature f = featureList.get(i);
	      String s = vals.get(f);
	      if (featureList.get(i).isNominal()) {
	        ArrayList<String> nomValues = valueMap.get(featureList.get(i));
	        for (String nVal : nomValues) {
	          if (s.equalsIgnoreCase(nVal)) {
	            result[ind++]=1.0;
	          }
	          else {
	            result[ind++]=0.0;
	          }
	        }
	      }else {
	        if (featureList.get(i).isString()) {
	          throw new RuntimeException("Can't handle string features");
	        }
	        result[ind++]=Double.parseDouble(s);

	      }
	    }
	    return result;*/
	  return null;
  }
  
  public double[] VNbinarizeOnlyOne(Feature feature, HashSet<String> values){
	  double[] result = new double[0];
	  ArrayList<String> nomValues = valueMap.get(feature);
	  int[] valueCount = new int[nomValues.size()];
	  // clear zero
	  for (int i = 0; i < valueCount.length; i++) {
		  valueCount[i] = 0;
	  }

	  for (String eachVal : values) {
		  double[] binaryVector = binarizeOnlyOne(feature, eachVal);
		  for (int i = 0; i < valueCount.length; i++) {
			  if (binaryVector[i] != 0) {
				  valueCount[i]++;
			  }
		  }
	  }

	  int totalLen = valueCount.length * 4;
	  int nItem = values.size();
	  int half = nItem / 2;
	  if (half % 2 != 0) half = half + 1;
	  int truebit = -1;

	  // all possible bit 
	  final int ALL_TRUE   = 0;
	  final int MOST_TRUE  = 1;
	  final int MOST_FALSE = 2;
	  final int NONE_TRUE  = 3;

	  //System.out.println("FeatureName: "+feature.getName());
	  
	  result = new double[totalLen];
	  for (int i = 0; i < valueCount.length; i++) {
		  if (valueCount[i] == 0) { // All Yes
			  truebit = ALL_TRUE;
		  } else if (valueCount[i] < nItem && valueCount[i] >= half) { // Most Yes
			  truebit = MOST_TRUE;  
		  } else if (valueCount[i] > 0 && valueCount[i] <= half) { // Most No
			  truebit = MOST_FALSE;
		  } else if (valueCount[i] == nItem) { // None Yes
			  truebit = NONE_TRUE;  
		  }

		  for (int j = 0; j < 4; j++) {
			  result[i * 4 + j] = 0;
		  }
		  if (truebit >= 0) {
			  result[i * 4 + truebit] = 1;
		  }
		  
		 // System.out.print("  "+nomValues.toString()+": ");
		 // for (int j = 0; j < 4; j++) {
		//	  System.out.print(result[i * 4 + j]+" ");
		 // }System.out.println();
	  }

	  return result;
  }
  
  public int getNumAggregateBinFeatures() {
	    return (numBinaryFeatures * 4);
	  }
  
  public int getNumBinaryFeatures() {
    return numBinaryFeatures;
  }
  public void setNumBinaryFeatures(int numBinaryFeatures) {
    this.numBinaryFeatures = numBinaryFeatures;
  }
  
  public String[] getFeatureNames(List<Feature> ftrs){
    String[] result = new String[numBinaryFeatures];
    int i = 0;
    for (Feature f : ftrs) {

      if (f.isNominal()) {
        String[] values = ((NominalFeature) f).getValues();
      
        if (values.length <= 2) {
          result[i++]=f.getName();
        }
        else {
          for (String val : values) {
            result[i++]=f.getName()+"_"+val;
          }
        }
      }else{
        result[i++]=f.getName();
      }
    }

    return result;
  }
}
