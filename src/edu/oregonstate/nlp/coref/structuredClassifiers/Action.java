package edu.oregonstate.nlp.coref.structuredClassifiers;

public class Action {

	public static final int ACT_MERGE = 1;
	public static final int ACT_NOP = 0;

	public int first, second;
	public int operatedMenID;
	public boolean prunned;
	public double trueScore, predScore;
	
	public double[] featvec;

	public int actName = -1; // -1: Undefined, 0: NOP, 1: merge, 2: split.

	public int getActName() {
		return actName;
	}
	public void setActName(int aName) {
		actName = aName;
	}

	public Action(int first, int second, int type, int opMid) {
		this.first = first;
		this.second = second;
		this.actName = type;
		this.operatedMenID = opMid;
		this.prunned = false;
	}
	
	public boolean isPruned() {
		return prunned;
	}
	
	public void setPruned(boolean prn) {
		prunned = prn;
	}
/*
	public int compareTo(Action o) {
		if(this.id==o.id) return 0;
		if(this.weight<o.weight) return 1;
		if(this.weight>o.weight) return -1;
		if(this.first<o.first||(this.first==o.first&&this.second<o.second))
			return 1;
		return -1;
	}
*/
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Action){
			if(this.isSameAction((Action)o)) {
				return true;
			}
		}
		return false;
	}
	@Override
	public String toString() {
		String str1 = new String("J"+ " [" + first + ", " + second + ", operMenId = "+ operatedMenID +" - ");
		return new String(str1);
	}
	

	public boolean isSameAction(Action act2) {
		if (this.actName == act2.actName) {
			if (this.first == act2.first && 
					this.second == act2.second) {
				return true;
			}
		}
		return false;
	}

	public int hashCode()
	{
		int hashc = 0;
		hashc = first + second * 1000 + operatedMenID * 1000000;
		return hashc;
	}

}
