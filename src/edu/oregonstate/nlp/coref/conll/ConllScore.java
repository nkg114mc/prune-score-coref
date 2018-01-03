package edu.oregonstate.nlp.coref.conll;

public class ConllScore {
	
	public String name;
	public double pre;
	public double rec;
	public double f1;

	public String text;

	public void fill3Arr(double[] f1scs) {
		f1scs[0] = pre;
		f1scs[1] = rec;
		f1scs[2] = f1;
	}
}
