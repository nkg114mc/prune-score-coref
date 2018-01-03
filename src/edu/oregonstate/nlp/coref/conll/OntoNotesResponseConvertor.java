package edu.oregonstate.nlp.coref.conll;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class OntoNotesResponseConvertor {

	/**
	 * Parameters to be changed 
	 * directory: which contains the all the required directories namely 0, 1, 2 ...
	 * useFileList: to run the response convertor only for a given set of files and not all.
	 * filelist: the list of files to run the response convertor on.
	 */
	public static void main(String[] args) {
		try {

			File directory = new File("G:/conll-2011/v2/data/train/data/english");
			boolean useFileList = true;
			File fileList = new File("G:/conll-2011/v2/data/train/data/english/test.filelist");

			File output = new File(directory,"final_output_conll");
			BufferedWriter writer = new BufferedWriter(new FileWriter(output)); 

			File base = new File(directory,"base_conll");
			BufferedWriter baseWriter = new BufferedWriter(new FileWriter(base));
			int count = 0;

			if (useFileList) {
				BufferedReader listReader = new BufferedReader(new FileReader(fileList));
				String str;
				while((str=listReader.readLine())!=null) {
					File dir = new File(directory, str.trim());
					count = processDir(writer, baseWriter, count, dir);
				}
			}
			else {
				File[] subDirs = directory.listFiles();
				for (File dir : subDirs) {
					count = processDir(writer, baseWriter, count, dir);
				}
			}
			writer.flush();
			writer.close();
			baseWriter.flush();
			baseWriter.close();
			System.out.println("Converted "+count+" files.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int processDir(BufferedWriter writer,
			BufferedWriter baseWriter, int count, File dir)
	throws FileNotFoundException, IOException {
		if(dir.isDirectory()) {

			try {
				Integer.parseInt(dir.getName());
			} catch (NumberFormatException e) {
				return count;
			}

			File corefFile = new File(dir, "coref_output_conll");
			if(corefFile.exists()){
				count++;
				File conllFile = new File(dir, "conll");


				BufferedReader conllR = new BufferedReader(new FileReader(conllFile));
				BufferedReader corefR = new BufferedReader(new FileReader(corefFile));

				String conllLine;
				String corefLine = corefR.readLine();
				corefLine = corefLine.replaceAll("-AMP-", "&");
				String[] corefColumns = corefLine.split("\t");
				while((conllLine=conllR.readLine()) != null) {
					baseWriter.write(conllLine+"\n");
					String[] conllColumns = conllLine.split(" ");
					conllColumns = cleanColumns(conllColumns);
					if(conllColumns.length <= 10) {
						writer.write(conllLine+"\n");
						continue;
					}
					if(conllColumns[3].equals(corefColumns[0])) {
						int indexOf = conllLine.lastIndexOf(" ");
						String newLine = conllLine.substring(0, indexOf+1) + corefColumns[1];
						writer.write(newLine + "\n");

						while((corefLine = corefR.readLine())!=null && corefLine.isEmpty());
						if(corefLine != null) { 
							corefLine = corefLine.replaceAll("-AMP-", "&");
							corefColumns = corefLine.split("\t");
						}
					}
					else {
						writer.write(conllLine+"\n");
					}
				}
				conllR.close();
				corefR.close();
			}
		}
		return count;
	}

	private static String[] cleanColumns(String[] conllColumns) {
		ArrayList<String> newColumns = new ArrayList<String>(); 
		for (String string : conllColumns) {
			if(string.isEmpty())
				continue;
			newColumns.add(string);
		}
		return newColumns.toArray(new String[2]);
	}

}
