package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.util.ArrayList;
import java.util.PriorityQueue;

//public class ActionList extends PriorityQueue<Action> implements Cloneable {
/*
public class ActionList extends PriorityQueue<Action> implements Cloneable {

	private static final long serialVersionUID = -6957801500902968039L;
	
	public ActionList() {
		super();
	}

	public ActionList(PriorityQueue<Action> ta) {
		super(ta);
	}

	public boolean insertAction(Action a) {
		boolean rem = super.add(a);
		return rem;
	}
	
	public boolean remove(Action a) {
		boolean rem = super.remove(a);
		return rem;
	}
	public Action popBest() {
		Action first = super.poll();
		return first;
	}
}
*/
public class ActionList extends ArrayList<Action> implements Cloneable {

	private static final long serialVersionUID = -6957801500902968039L;
	
	public ActionList() {
		super();
	}

	public boolean insertAction(Action a) {
		boolean rem = super.add(a);
		return rem;
	}

}

/*
  private static final long serialVersionUID = 6554145785409693726L;
  private Action maxPositive = null;
  private Action terminate = null;
  double[] featureSum;
  public ActionList(){
    super();
  }
  public ActionList(PriorityQueue<Action> ta){
    super(ta);
  }
  public boolean add(Action a){
    boolean add = super.add(a);
    return add;
  }
  public boolean insertAction(Action a){
	 boolean add = super.add(a);
	 return add;
  }
  public boolean remove(Action a){
    boolean rem = super.remove(a);
    if(rem){
      Z-=a.getWeight();
      subtractFeatureCounts(a);
    }
    if(size()<1){
      Z=0.0;
      setFeatureCountsZero();
    }
    return rem;
  }
  public Action pollFirst(){
    Action first = super.poll();
    Z-=first.getWeight();
    subtractFeatureCounts(first);
    return first;
  }
  public Action popBest(){
	Action first = super.poll();
	    //Z-=first.getWeight();
	    //subtractFeatureCounts(first);
	return first;
  }
  
  public double getZ(){
    return Z;
  }
  public double[] getFeatVectorSum(){
    double[] result = new double[featureSum.length];
    double denom = (double)size();//getZ();//Math.pow(getZ(),size());
    for(int i=0; i<result.length; i++){
      result[i]=featureSum[i]/denom;
    }
    return result;
//    return featureSum;
  }
  private void addFeatureCounts(Action a){
    if(a.terminate() && a.getActName() <= 0){
      featureSum[featureSum.length-1]+=1;
    }else{
      double[] feats = a.getFeatureVector();
      //double weight = 1.0;//a.getWeight();
      for(int i=0; i<feats.length; i++){
        featureSum[i]+=feats[i];//*weight;
      }
    }
  }
  private void subtractFeatureCounts(Action a){
    if(a.terminate()){
      featureSum[featureSum.length-1]-=1;
    }else{
      double[] feats = a.getFeatureVector();
      //double weight = 1.0;//a.getWeight();
      for(int i=0; i<feats.length; i++){
        featureSum[i]-=feats[i];//*weight;
      }
    }
  }
  private void setFeatureCountsZero(){
    for(int i=0; i<featureSum.length; i++){
      featureSum[i]=0.0;
    }
  }
  public ActionList clone(){
    ActionList copy = new ActionList(this);
    copy.Z = Z;
    copy.featureSum = featureSum.clone();
    return copy;
  }
  public void setMaxPositive(Action maxPositive) {
    this.maxPositive = maxPositive;
  }
  public Action getMaxPositive() {
    return maxPositive;
  }
  public void setTerminate(Action terminate) {
    this.terminate = terminate;
  }
  public Action getTerminate() {
    return terminate;
  }
}
*/