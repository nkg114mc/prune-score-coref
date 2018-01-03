package edu.oregonstate.nlp.coref.structuredClassifiers;
/**
 * @author Chao Ma
 * @since 2013-2-7
 * 
 */

public class DiscrepancyItem {
	public enum DisConstraintEnum {
		CANNOT_LINK, MUST_LINK, UNKNOWN;
	};
	
	/** First mention ID */
	public int firstMenID;

	/** Second mention ID */
	public int secondMenID;

	/** the discrepancy value for this edge */
	public int constraintVal = -1;
	
	/** weight for ranking the constraints */
	public double weightCl = 0;
	public double weightPair = 0;
	
	/** Default constructors */
	public DiscrepancyItem()
	{
		this.firstMenID = -1;
		this.secondMenID = -1;
		this.constraintVal = -1;
	}

	/** Constructors */
	public DiscrepancyItem(int first, int second, int val)
	{
		this.firstMenID = first;
		this.secondMenID = second;
		this.constraintVal = val;
	}

	/** copy constructor, copy all information from another node */
	public DiscrepancyItem(DiscrepancyItem src)
	{
		copyFrom(src);
	}

	private void copyFrom(DiscrepancyItem src)
	{
		this.firstMenID = src.firstMenID;
		this.secondMenID = src.secondMenID;
		this.constraintVal = src.constraintVal;
	}

	public void setValue(int val)
	{
		this.constraintVal = val;
	}

	public int getValue()
	{
		return (this.constraintVal);
	}

	@Override
	public int hashCode() { // rewrite hashCode
		int hashcode;
		hashcode = firstMenID * 1000 + secondMenID;
		return hashcode;
	}

	public boolean equals(Object obj)
	{
		DiscrepancyItem compare = (DiscrepancyItem) obj;
		if (this.firstMenID == compare.firstMenID &&
			this.secondMenID == compare.secondMenID &&
			this.constraintVal == compare.constraintVal) {
			return true;
		}
		return false;
	}

	public String toString()   
	{
		String str;
		str = new String("edge("+firstMenID+" "+secondMenID+") = "+constraintVal+ "  weightCl="+this.weightCl + " weightPair="+this.weightPair);
		return str;
	}  
}