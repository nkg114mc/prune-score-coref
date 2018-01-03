package edu.oregonstate.nlp.coref.scorers;

import java.util.Arrays;

public class ScoreValue {
	
	public double[] muc;
	public double[] bcub;
	public double[] ceafe;
	public double[] ceafm; // currently useless?
	public double conll;
	
	public ScoreValue() {
		muc    = new double[3];
		bcub   = new double[3];
		ceafe  = new double[3];
		ceafm  = new double[3];
		clear();
	}
	
	
	public void clear() {
		Arrays.fill(muc, 0);
		Arrays.fill(bcub, 0);
		Arrays.fill(ceafe, 0);
		Arrays.fill(ceafm, 0);
		conll = 0;;
	}
	
	public void copyFrom(ScoreValue sv) {
		muc[0] = sv.muc[0];
		muc[1] = sv.muc[1];
		muc[2] = sv.muc[2];
		
		bcub[0] = sv.bcub[0];
		bcub[1] = sv.bcub[1];
		bcub[2] = sv.bcub[2];
		
		ceafe[0] = sv.ceafe[0];
		ceafe[1] = sv.ceafe[1];
		ceafe[2] = sv.ceafe[2];
		
		conll =  sv.conll;
	}
	
}
