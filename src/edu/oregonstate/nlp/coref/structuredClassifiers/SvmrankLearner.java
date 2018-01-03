package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import de.schlichtherle.io.FileInputStream;

public class SvmrankLearner {
	
	String svmrankExePath = "svm_rank_learn";
	String svmPerlPath = "model2weight";
	String m2wExePath = "model2weight";
	//String 
	
	
	public void setSvmrankPath(String path)
	{
		svmrankExePath = path;
	}
	
	public void setSvmPerlPath(String path)
	{
		svmPerlPath = path;
	}
	
	public void setM2WPath(String path)
	{
		m2wExePath = path;
	}
	
	
	
	public void runSvmrankLearn(String featFilePath, double c, String modelPath, String weightPath)
	{
		// realC c * nqid
		double nqid = (double)(qidCount(featFilePath));
		double realC = c * nqid;
		
		// run svmrank to get the model
		runSvmrank(featFilePath, realC,  modelPath);
		
		// convert the weight
		convertWeight(modelPath, weightPath);
	}
	
	private void runSvmrank(String featFilePath, double realc, String modelPath)
	{
		String cmd1 = " -e 0.01 ";
		String cmd2 = "-c " + String.valueOf(realc) + " ";
		String cmd3 = featFilePath +" " + modelPath;
		
		String cmd = svmrankExePath + cmd1 + cmd2 + cmd3;
		System.out.println("Running svmrank learn: " + cmd);
		
		// run svmrank
		runExe(cmd);
		
		System.out.println("Done svmrank learning");
	}
	
	public void convertWeight(String modelPath, String weightPath)
	{
		//"/nfs/guille/tadepalli/students/machao/svmrank_learn/model2weight/model2weight -perl  -i " + modelname + " -o " + "searchw.heur"
		String cmd1 = " -perl " + svmPerlPath;
		String cmd2 = " -i " + modelPath;
		String cmd3 = " -o " + weightPath;
		String cmd = m2wExePath + cmd1 + cmd2 + cmd3;
		
		// run convert
		System.out.println("Running weight converting: " + cmd);
		runExe(cmd);
		System.out.println("Done converting");
	}
	
	public int qidCount(String featureFilePath)
	{
		HashSet<String> allQid = new HashSet<String>();
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(featureFilePath)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		String dataline = null;
		String qidword = "#####################";
		try {
			while((dataline = br.readLine())!=null) {
				String[] word = dataline.split(" ");
				for (int i = 0; i < word.length; i++) {
					CharSequence cs1 = "qid:";
					if (word[i].contains(cs1)) {
						allQid.add(word[i]);
						qidword = word[i];
						break;
					}
				}
				//System.out.println("Current qid: "+qidword);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Qid size: "+allQid.size());
		return allQid.size();
	}
	
	private void runExe(String arg)
	{
        Process p;
		try {
			p = Runtime.getRuntime().exec(arg);
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
