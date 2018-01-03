/**
 * 
 * @author David Golland
 * 
 */

package edu.oregonstate.nlp.coref.classifiers;

public abstract class Classifier {

	public static double plattScale(double prediction, double A, double B)
	{
		return 1.0 / (1.0 + Math.exp(A * prediction + B));
	}

}
