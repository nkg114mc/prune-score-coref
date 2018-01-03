package edu.oregonstate.nlp.coref.mentions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;

import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class J48Classifier {

	//private J48graft myTree;
	private J48 myTree;
    private Instances inst = null;
    
	public J48Classifier(String path, String featureExample) {
		if (featureExample == null) {
			featureExample = "mentionFeature_example.arff";
		}
		try {
			inst = new Instances(new FileReader(featureExample));
			inst.setClassIndex(inst.numAttributes() - 1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadModel(path);
	}
	
	public void loadModel(String path) {
		File modelf = new File(path);
		if (!modelf.exists()) {
			throw new RuntimeException("Model " + path + " does not exist!");
		}
		
		//  public Object LoadModel(String file){
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
            Object classifier = (ois.readObject());
            ois.close();
            //myTree = (J48graft) classifier;//new J48graft();
            myTree = (J48) classifier;//new J48();
            System.out.println("Loaded weka classifier: " + myTree.toSummaryString());
            System.out.println("Weka model file: " + path);
        } catch(IOException e){
            e.printStackTrace();
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }
	}
	
	public double predict(double[] featureVector) {
		int i;
		double predicted = 0;
		//Instance instance = new Instance();
		Instance instance = new DenseInstance(1, featureVector);

		for (i = 0; i < featureVector.length; i++) {
			new String("feature" + (i+1));
			instance.setValue(inst.attribute(i), featureVector[i]);
		}
		//System.out.println("instantce dem = " + inst.numAttributes());
		instance.setDataset(inst);
		
		// do predicting
		try {
			predicted = myTree.classifyInstance(instance);
			return predicted;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return predicted;
	}
}
