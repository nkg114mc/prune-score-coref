package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.io.IOException;
import java.util.ArrayList;

import edu.oregonstate.nlp.coref.SystemConfig;

public class FeatureFileMerger {
	
	/** Default name */
	String defaultOutput = "featureAfterMerge.txt";
	
	/** Exe paths*/
	String featMergeExePath = "mergefeat.exe";

	
	FeatureFileMerger() { }
	
	// initialization from config
	void initFromConfig(SystemConfig cfg)
	{
	}
	
	void setMergerPath(String path)
	{
		 featMergeExePath = path;
	}
	
	void mergeFeatureFiles(ArrayList<String> fileList, String outputName)
	{
		
		// exe name
		String param = featMergeExePath;
		// input
		param += " -i ";
		for (String fn : fileList) {
			param += (fn + " ");
		}
		// output
		param += " -o ";
		param += outputName;
		
		// run merger
		System.out.println("Run cmd: "  +param);
		runExe(param);
		System.out.println("Done.");
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
