package edu.oregonstate.nlp.coref.features;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.NominalFeature;

public class Binarizer {
  private HashMap<Feature, ArrayList<String>> valueMap;
  private List<Feature> featureList;
  private int numBinaryFeatures;
  
  private HashMap<Feature, Integer> globalStartIndex;
  private HashMap<Feature, Integer> globalEndIndex;
  private HashMap<Feature, Integer> valueNum;
  
  public Binarizer(List<Feature> ftrs){
    featureList = ftrs;
    numBinaryFeatures=0;
    valueMap = new HashMap<Feature, ArrayList<String>>();
    
    globalStartIndex = new HashMap<Feature, Integer>(); 
    globalEndIndex = new HashMap<Feature, Integer>();
    valueNum = new HashMap<Feature, Integer>();
   
    for (Feature f : ftrs) {
      int startIdx = numBinaryFeatures;
      
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
      }else{
        numBinaryFeatures++;
      }
      
      int endIdx = numBinaryFeatures - 1;
      int valueN = endIdx - startIdx + 1;
      
      // insert
      globalStartIndex.put(f, startIdx);
      globalEndIndex.put(f, endIdx);
      valueNum.put(f, valueN);
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
