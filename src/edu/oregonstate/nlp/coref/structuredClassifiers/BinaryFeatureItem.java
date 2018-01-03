package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.util.ArrayList;
import edu.oregonstate.nlp.coref.featureVector.Feature;

/**
 * @author Chao Ma
 * @since August 16th, 2013
 */

public class BinaryFeatureItem {
	
	public String feautureName;
	public String value;
	
	public double currentValue;
	public double weight;
	
	public Feature myFeautre;
	public ArrayList<String> allValues;
	
	BinaryFeatureItem()
	{
		
	}
	

}