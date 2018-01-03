package edu.oregonstate.nlp.coref.conll;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;

public class OntoNotesSingletonRemover {

	static int directoryCount = 10;
	static String dir = "";

	public static void main(String[] args) {
		try {
			
			for(int f = 0; f < directoryCount; f++) {
				String currentDir = dir+File.separator+f+File.separator+"annotations";
				File file = new File(currentDir, "responseNPs");
				File outputFile = new File(currentDir, "responseNPs2");
				
				BufferedReader reader = new BufferedReader(new FileReader(file));
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

				String line;
				int lineNo = 0;
				BitSet bitset = new BitSet();
				HashMap<Integer, String> map = new HashMap<Integer, String>();

				int corefId = -1;
				while((line = reader.readLine())!=null) {
					String[] strings = line.split("\t");
					boolean found = false;
					for(int i = 1; i < strings.length; i++) {
						String details = strings[4];
						String[] split = details.split(" ");
						for(int s = 0; s < split.length; s++) {
							if(split[s].startsWith("CorefID")) {
								int lastIndexOf = split[s].lastIndexOf("\"");
								corefId = Integer.parseInt(split[s].substring(9, lastIndexOf));
								found = true;
								break;
							}
						}
						if(found)
							break;
					}
					if(bitset.get(corefId)) {
						if(map.containsKey(corefId)) {
							writer.write(++lineNo+"\t"+map.get(corefId)+"\n");
							map.remove(corefId);
						}
						writer.write(++lineNo+"\t"+strings[1]+"\t"+strings[2]+"\t"+strings[3]+"\t"+strings[4]+"\n");
					}
					else {
						bitset.set(corefId);
						map.put(corefId, strings[1]+"\t"+strings[2]+"\t"+strings[3]+"\t"+strings[4]);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
