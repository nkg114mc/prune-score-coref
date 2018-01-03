/*
 * Copyright (c) 2008, Lawrence Livermore National Security, LLC. Produced at the Lawrence Livermore National
 * Laboratory. Written by David Buttler, buttler1@llnl.gov CODE-400187 All rights reserved. This file is part of
 * RECONCILE
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License (as published by the Free Software Foundation) version 2, dated June 1991. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the IMPLIED WARRANTY OF MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the terms and conditions of the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA For full text see license.txt
 * 
 * Created on Jan 26, 2009
 * 
 */
package edu.oregonstate.nlp.coref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.oregonstate.nlp.coref.featureExtractor.InternalAnnotator;
import edu.oregonstate.nlp.coref.featureVector.AllFeatures;
import edu.oregonstate.nlp.coref.featureVector.ClusterFeature;
import edu.oregonstate.nlp.coref.featureVector.Feature;
import edu.oregonstate.nlp.coref.featureVector.MentionPropertyFeature;
import edu.oregonstate.nlp.coref.scorers.Scorer;

/**
 * This is a class to centralize all of the reflection inside of Reconcile
 * 
 * @author David Buttler
 * 
 */
public class Constructor {
	
private static String highRoot = "edu.oregonstate.nlp.coref.";
	
private static String InternalAnnotatorParent = highRoot + "featureExtractor.";
private static String IndividualFeatureParent = highRoot + "featureVector.individualFeature.";
private static String ClusterFeatureParent = highRoot + "featureVector.clusterFeature.";
private static String MentionFeatureParent = highRoot + "featureVector.mentionPropertyFeature.";
private static String ScorerParent = highRoot + "scorers.";

	
public static InternalAnnotator createInternalAnnotator(String className)
{
  String origClassName = className;
  // Class names can be specified either by the full java name
  if (!className.contains(".")) {
    // otherwise, assume the class is in FeatureExtractor directory
    className = InternalAnnotatorParent + className;
  }
  try {
    // System.out.println(className);
    Class<?> featClass = Class.forName(className);
    InternalAnnotator result = (InternalAnnotator) featClass.newInstance();
    result.setName(origClassName);
    return result;
  }
  catch (Exception e) {
	System.out.println("While creating "+className);
    throw new RuntimeException(e);
  }
}


public static Feature createFeature(String name)
{
  if (AllFeatures.featMap == null) {
    AllFeatures.featMap = new HashMap<String, Feature>();
  }

  Feature feat = AllFeatures.featMap.get(name);
  if (feat == null) {
    try {
      String className = name;
      if (!className.contains(".")) {
        className = IndividualFeatureParent + name;
      }

      Class<?> featClass = Class.forName(className);
      feat = (Feature) featClass.newInstance();
      AllFeatures.featMap.put(name, feat);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  return feat;
}

public static ClusterFeature createClusterFeature(String name)
{
  if (AllFeatures.clustFeatMap == null) {
    AllFeatures.clustFeatMap = new HashMap<String, ClusterFeature>();
  }

  ClusterFeature feat = AllFeatures.clustFeatMap.get(name);
  if (feat == null) {
    try {
      String className = name;
      if (!className.contains(".")) {
        className = ClusterFeatureParent + name;
      }

      Class<?> featClass = Class.forName(className);
      feat = (ClusterFeature) featClass.newInstance();
      AllFeatures.clustFeatMap.put(name, feat);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  return feat;
}

public static MentionPropertyFeature createMentFeature(String name)
{
  if (AllFeatures.mentFeatMap == null) {
    AllFeatures.mentFeatMap = new HashMap<String, MentionPropertyFeature>();
  }

  MentionPropertyFeature feat = AllFeatures.mentFeatMap.get(name);
  if (feat == null) {
    try {
      String className = name;
      if (!className.contains(".")) {
        className = MentionFeatureParent + name;
      }
      Class<?> featClass = Class.forName(className);
      feat = (MentionPropertyFeature) featClass.newInstance();
      AllFeatures.mentFeatMap.put(name, feat);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  return feat;
}


public static List<Feature> createFeatures(String[] features)
{
  ArrayList<Feature> result = new ArrayList<Feature>();

  for (String feat : features) {
    Feature newFeat = createFeature(feat);
    result.add(newFeat);
  }
  return result;
}

public static List<ClusterFeature> createClusterFeatures(String[] features)
{
  ArrayList<ClusterFeature> result = new ArrayList<ClusterFeature>();

  for (String feat : features) {
    ClusterFeature newFeat = createClusterFeature(feat);
    result.add(newFeat);
  }
  return result;
}

public static List<MentionPropertyFeature> createMentionFeatures(String[] features)
{
  ArrayList<MentionPropertyFeature> result = new ArrayList<MentionPropertyFeature>();
  for (String feat : features) {
    MentionPropertyFeature newFeat = createMentFeature(feat);
    result.add(newFeat);
  }
  return result;
}

public static Scorer createScorer(String name)
{
  // Class names can be specified either by the full java name
  if (!name.contains(".")) {
    // otherwise, assume the class is in FeatureExtractor directory
    name = ScorerParent + name;
  }
  try {
    // System.out.println(className);
    Class<?> featClass = Class.forName(name);
    Scorer result = (Scorer) featClass.newInstance();
    return result;
  }
  catch (Exception e) {
    throw new RuntimeException(e);
  }

}


}
