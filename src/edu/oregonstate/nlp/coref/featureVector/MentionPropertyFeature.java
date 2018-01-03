/*
 * A common ancestor for all mention feature types
 * 
 * by Chao Ma (2014-5-27)_
 */
package edu.oregonstate.nlp.coref.featureVector;

import java.util.Map;
import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.Document;


public abstract class MentionPropertyFeature {

	protected String name;
	protected boolean ignore = false;

	public MentionPropertyFeature() {
		name = getClass().getSimpleName();
	}

	public boolean isNominal()
	{
		return false;
	}

	public boolean isNumeric()
	{
		return false;
	}

	public boolean isString()
	{
		return false;
	}

	public abstract String produceValue(Annotation mention, Document doc, Map<MentionPropertyFeature, String> featVector);

	// A cached version of the produce value function
	public String getValue(Annotation mention, Document doc, Map<MentionPropertyFeature, String> featVector)
	{
		String val = featVector.get(this);
		if (val == null) {
			val = produceValue(mention, doc, featVector);
		}
		featVector.put(this, val);
		return val;
	}

	public String getName()
	{
		return name;
	}

	public boolean ignoreFeature()
	{
		return ignore;
	}
	public boolean structuredOnly()
	{
		return false;
	}

} // end of the file
