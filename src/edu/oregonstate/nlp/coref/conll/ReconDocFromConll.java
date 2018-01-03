package edu.oregonstate.nlp.coref.conll;

import gov.llnl.text.util.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import edu.oregonstate.nlp.coref.SystemConfig;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureExtractor.CEExtractor;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.Utils;
import edu.oregonstate.nlp.coref.oregonstate.OregonStateConllPreprocess;
import edu.oregonstate.nlp.coref.stanford.StanfordSystemInterface;
import edu.oregonstate.nlp.coref.uiuc.DocCoNLL;

/**
 * Extract reconcile annotation sets from a conll file.
 * This step does not include mention extraction.
 * 
 * @author mc
 *
 */
public class ReconDocFromConll {
	
/*
	private OregonStateConllPreprocess reconPreprocessor = null;
	private boolean overwrite = false;
	
	
	private SystemConfig config = null;
	String version_perfix = null;
	String mention_prefix = null;
	// script
	private String parseScript;
	private String nameScript;
	private String corefScript;
	// config annotation converting
	BufferedWriter mWriter = null;
	String[] annSetNames;
	
	private String outputDirPath = null;
	private String inputDirPath = null;

	private ArrayList<ConllDocument> conllDocList = null;
	private ArrayList<Document> reconDocList = null;
	private ArrayList<String> conllDocNames = null;
	private ArrayList<String> reconDocNames = null;
	
	// stanford system conll file processor
	private boolean useStanfordMentions;
	private StanfordSystemInterface stanfordProcessor = null;
	
	// uiuc system conll file processor
	private boolean useUIUCMentions;
	private UIUCConllDocLoader uiucProcessor = null;
	

	
	// configuration
	public void setCfg(SystemConfig cfg) {
		config = cfg;
		
		// reconcile preprocessor
		reconPreprocessor = new OregonStateConllPreprocess(cfg);
		overwrite = cfg.getBoolean("OVERWRITE_FILES", true);
		
		// stanford processor
		useStanfordMentions = cfg.getBoolean("USE_STANFORD_MENTIONS", false);
		if (useStanfordMentions) {
			stanfordProcessor = new StanfordSystemInterface(cfg);
		}
		
		// uiuc pro
		useUIUCMentions = cfg.getBoolean("USE_UIUC_MENTIONS", false);
		if (useUIUCMentions) {
			uiucProcessor = new UIUCConllDocLoader();
		}
		
		version_perfix = cfg.getString("CONLL_VERSION_PREFIX");
		mention_prefix = cfg.getString("CONLL_MENTION_PREFIX");
		parseScript = cfg.getConllParseScript();
		nameScript = cfg.getConllNameScript();
		corefScript = cfg.getConllCorefScript();
		
		File scriptfile = new File(parseScript);
		assert(scriptfile.exists());
		scriptfile = new File(nameScript);
		assert(scriptfile.exists());
		scriptfile = new File(corefScript);
		assert(scriptfile.exists());
		
		// input and output dir
		inputDirPath = cfg.getString("CONLL_INPUT_DIR");
		outputDirPath = cfg.getString("CONLL_OUTPUT_DIR");
		
		HashMap<String, String[]> elSetNames = cfg.getPreprocessingElSetNames();
		Collection<String[]> values = elSetNames.values();
		ArrayList<String> annSetNamesList = new ArrayList<String>(); 
		for (String[] strings : values) {
			annSetNamesList.addAll(Arrays.asList(strings));
		}
		annSetNames = annSetNamesList.toArray(new String[2]);
		for (String setname : annSetNames) {
			System.out.println("Preprocess set name: " + setname);
		}
		
		mWriter = null;
	}
	
	public void clear() {
		// clear the list!
		conllDocList = null;
		reconDocList = null;
		
	}
	
	public void setOutputDir(String outputFolder) {
		outputDirPath = outputFolder;
	}
	public void setInputDir(String inputFolder) {
		inputDirPath = inputFolder;
	}
	
	// docs
	public ArrayList<ConllDocument> getConllDocList() {
		return conllDocList;
	}
	public ArrayList<Document> getReconDocList() {
		return reconDocList;
	}
	// docnames
	public ArrayList<String> getConllDocNames() {
		return conllDocNames;
	}
	public ArrayList<String> getReconDocNames() {
		return reconDocNames;
	}
	
	
	public void prepareAll() // train, valid, test
	{
		
	}
	
	
	private String getExtension(String fileName) {
		int index = fileName.lastIndexOf("."); // to separate filename and its extension.
		String basename = fileName.substring(0, index); // base name
		String extension = fileName.substring(index + 1); // ext name
		return extension;
	}
	
	// almost the same as 
	public void prepare(String input_dir_path) {
		
		inputDirPath = input_dir_path;
		assert(inputDirPath != null);
		assert(outputDirPath != null);
		
		int count = 0;
		ArrayList<String> allConllFiles = new ArrayList<String>();
		
		File annotationsDir = new File(inputDirPath, "annotations");
		File[] categoryDirs = annotationsDir.listFiles();
		for (File categoryDir : categoryDirs) {
			File[] subCategories = categoryDir.listFiles();
			for (File subCategory : subCategories) {
				if(!subCategory.isDirectory())
					continue;
				
				System.out.println(subCategory);
				File[] directories = subCategory.listFiles();
				
				if(directories == null) {
					System.err.println("directory " + subCategory + " is empty");
					continue;
				}
				
				for (File iDir : directories) {

					System.out.println("Converting " + iDir + " to reconcile format");
					
					HashMap<String, Integer> filesCopied = new HashMap<String, Integer>();
					ArrayList<File> oDirs = new ArrayList<File>();
					String[] files = iDir.list();
					for (String file : files) {
						System.out.println("Found a file: " + iDir + Utils.SEPARATOR + file + "\n");
						int index = file.lastIndexOf("."); // to separate filename and its extension.
						
						String filename = file;
						String extension = "";
						if (index > 0) {
							filename = file.substring(0, index);
							extension = file.substring(index + 1);
						}
						
						int id;
						if(filesCopied.containsKey(filename)) {
							id = filesCopied.get(filename);
						} else {
							id = count++;
							//mWriter.write(id + "\t" + iDir + Utils.SEPARATOR + filename + "\n");
						}
						
						// is this a conll file?
						if (file.contains("_conll")) {
							// this is a conll file
							if (file.contains(mention_prefix)) {
								// and this conll file is the one with the correct mention setting (gold or auto)
								String completePath = iDir.getAbsolutePath() + Utils.SEPARATOR + file;
								allConllFiles.add(completePath);
								
								System.out.println("Detected "+ id + "th"+" conll file: " + completePath);
							}
						}

						filesCopied.put(filename, id);
					}
					
					filesCopied.clear();
					filesCopied = null;
					files = null;
				}	
			}
		}
		
		assert(config != null);
		
		// begin to preprocess
		System.out.println("Number of conll files: " +  allConllFiles.size());
		for (String conllPath : allConllFiles) {
			File conllf = new File(conllPath);
			preprocess(conllf);
		}
		
		// done
		System.out.println("Preprocessed " +  allConllFiles.size() + " conll files in total.");
	}

	

	public void preprocessAll(List<File> allFilenames) {
		for (File collFilename : allFilenames) {
			preprocess(collFilename);
		}
	}
	
	public void preprocess(File conllFilename) {

		assert(conllFilename != null);
		assert(outputDirPath != null);
		
		// init list of names
		if (conllDocNames == null) conllDocNames = new ArrayList<String>();
		if (reconDocNames == null) reconDocNames = new ArrayList<String>();

		ConllDocument conllDoc = new ConllDocument(); // a new conll2011 or conll2012 document
		try {
			
			// record the file list
			if (mWriter == null) {
				File mapFile = new File(outputDirPath, "map.txt");
				mWriter = new BufferedWriter(new FileWriter(mapFile));
			}

			// 1) copy the file to output dir
			if (!conllFilename.exists()) {
				throw new RuntimeException("Input Directory : " + conllFilename.getAbsolutePath().toString() + " does not exist.");
			}
			File outputDir = new File(outputDirPath);
			if (!outputDir.exists()) {
				System.out.println(outputDir + " does not exist. Creating a new one");
			} else { 
				System.out.println("Overwriting " + outputDir);
			}

			String fileName = conllFilename.getName(); // file name
			int index = fileName.lastIndexOf("."); // to separate filename and its extension.
			String basename = fileName.substring(0, index); // base name
			String extension = fileName.substring(index + 1); // ext name
			String pathName = conllFilename.getParent(); // file dir
			System.out.println("Conll directory: " + pathName);
			System.out.println("Conll filename: " + basename + " . " + extension);

			// file dir
			File outputFileDir = new File(outputDirPath, basename);
			if(!outputFileDir.exists()) {
				outputFileDir.mkdir();
			}

			// copy file
			File newConllFile = new File(outputFileDir, fileName); // newConllFile is new copied the conll that located in our folder
			FileUtils.cp(conllFilename, newConllFile);
			mWriter.write(newConllFile.getAbsolutePath() + "\n");

			// 2) read conll file and construct conllDocument
			assert(version_perfix != null);
			assert(mention_prefix != null);

			String prefix = version_perfix + mention_prefix;
			
			// read it with our own reader firstly
			OntoNotesConllReader reader = new OntoNotesConllReader();
			AnnotationSet annoSet = reader.extractConllAnnotationSet(newConllFile.getAbsolutePath(), Constants.CONLL_ANNO);
			
			// ask the stanford reader to have a try also
			HashMap<Integer, edu.stanford.nlp.dcoref.Document> stanfordPartDocs = null;
			if (useStanfordMentions) {
				System.out.println("Stanford system start reading file: " + newConllFile.getAbsolutePath());
				stanfordPartDocs = stanfordProcessor.getConllMentionExtractor().loadConllDocumentByStanford(newConllFile.getAbsolutePath());
			}
			
			/// start processing each part doc

			// how many parts are there in this document?
			int partNum = reader.getTotalPartNum(annoSet);
			ArrayList<Document> reconPartDocList = new ArrayList<Document>(); // part documents for this conll-doc
			//ArrayList<String> partDoc
			for (int j = 0; j < partNum; j++) {
				
				// 3) split them into parts as the conll file defined
				
				//String partDirName = newConllFile + Utils.SEPARATOR + String.valueOf(j);
				File partDirFile = new File(outputFileDir.getAbsoluteFile(), "docpart" + String.valueOf(j));
				if(!partDirFile.exists()) {
					partDirFile.mkdir(); // make part directory
				}
				
				String partName = "part" + String.valueOf(j) + "." + extension;
				reader.writeConllFormatPart(annoSet, partDirFile.getAbsolutePath(), partName, j);
				String partAbsPath = partDirFile.getAbsolutePath() + Utils.SEPARATOR + partName; // abs path of the PART conll file
			
				// 4) run script on the 

				runScript(parseScript, partDirFile.getAbsolutePath());//partAbsPath);
				runScript(nameScript, partDirFile.getAbsolutePath());//partAbsPath);
				runScript(corefScript, partDirFile.getAbsolutePath());//partAbsPath);
				
				// copy the files and rename them with only their extension
				File[] allFiles = partDirFile.listFiles();
				for (File everyfile : allFiles) {
					if (everyfile.getName().contains(".") && !everyfile.isDirectory()) {
						String myext = getExtension(everyfile.getName());
						FileUtils.cp(everyfile, new File(everyfile.getParent(), myext));
						System.out.println("copying " + everyfile.getName() + " -> " + myext);
					}
				}

				try {
				    Thread.sleep(1000);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
				
				// 5) construct reconDoc
				Document doc = new Document(partDirFile);
				System.out.println("New document: " + partDirFile.getAbsolutePath());
				
				// 6) convert other annotation sets
				System.out.println("Mention prefix string: " + prefix);
				OntoNotesNPSRawFileExtractor rawFileExtractor = new OntoNotesNPSRawFileExtractor(prefix);
				OntoNotesNEExtractor neExtractor = new OntoNotesNEExtractor(prefix);
				OntoNotesConllReader conllReader = new OntoNotesConllReader();
				
				// Creates raw.txt and key for each of the doc.
				rawFileExtractor.run(doc, annSetNames);
				
				// Create gold standard ne file for each doc.
				neExtractor.run(doc, annSetNames);
				
				File ontoNotesParse = new File(doc.getAbsolutePath(), prefix + "parse");
				File reconcileParse = 
					new File(doc.getAbsolutePath() + Utils.SEPARATOR + Constants.ANNOT_DIR_NAME, "parse");
				File textFile = new File(doc.getAbsolutePath() + Utils.SEPARATOR + "raw.txt");
				File posTagFile = new File(doc.getAbsolutePath() + Utils.SEPARATOR + Constants.ANNOT_DIR_NAME, "postag");
				File tokenFile = new File(doc.getAbsolutePath() + Utils.SEPARATOR + Constants.ANNOT_DIR_NAME, "token");
				
				File sentFile = new File(doc.getAbsolutePath() + Utils.SEPARATOR + Constants.ANNOT_DIR_NAME, "sent");
				File parFile = new File(doc.getAbsolutePath() + Utils.SEPARATOR + Constants.ANNOT_DIR_NAME, "par");
				File depFile = new File(doc.getAbsolutePath() + Utils.SEPARATOR + Constants.ANNOT_DIR_NAME, "dep");
				
				String[] conllSetName = {"conll"}; 
				conllReader.run(doc, conllSetName);

				// Convert OntoNotes' Parse file to Reconcile's Parse File.
				OntoNotesFileConvertor convertor = 
					new OntoNotesFileConvertor(ontoNotesParse, reconcileParse, posTagFile, tokenFile, textFile,
							                   sentFile, parFile, depFile);
				// parse, dep, token, 
				convertor.convert();
				
				String[] tokenSetNames = { Constants.TOKEN };
				doc.loadAnnotationSetsByName(tokenSetNames);
				
				
				// reconcile predict mentions
				String[] anSetNames = {"nps"}; 
				CEExtractor ceExtractor = new CEExtractorOntoNotes();
				ceExtractor.run(doc, anSetNames);
				
				// stanford predict mentions
				if (useStanfordMentions) {
					String[] stanfordMentionNames = { Constants.STANFORD_NP }; 
					CEExtractorConll2011Stanford stanfordMetnionExtractor = new CEExtractorConll2011Stanford();
					stanfordMetnionExtractor.run(doc, stanfordMentionNames, stanfordPartDocs.get(j), true); // get part j stanford document
				}

				// uiuc predict mentions
				if (useUIUCMentions) {
					String[] uiucMentionSetNames = { Constants.UIUC_NP }; 
					DocCoNLL uiucDoc = uiucProcessor.getPartDoc(partAbsPath);
					CEExtractorConllUIUC uiucMetnionExtractor = new CEExtractorConllUIUC();
					uiucMetnionExtractor.run(doc, uiucMentionSetNames, uiucDoc, true);
				}
				
				// 7) Run the original reconcile preprocess on this partDoc
				reconPreprocessor.preprocess(doc, overwrite);
				
				// 8)
				reconPartDocList.add(doc);
				//reconDocList.add(doc);
				reconDocNames.add(doc.getAbsolutePath());
			}

			// output conll annotation set?
			OntoNotesFileConvertor.writeAnnotationSet(new File(outputFileDir.getAbsolutePath(), Constants.CONLL_ANNO), annoSet);
			
			// 8) construct conll document
			conllDoc.docName = newConllFile.getAbsolutePath(); //conllFilename.toString();
			conllDoc.partNum = partNum;
			conllDoc.conllAnnoSet = annoSet;
			conllDoc.reconPartDocList = reconPartDocList;

			//conllDocList.add(conllDoc);
			conllDocNames.add(newConllFile.getAbsolutePath());

			
			mWriter.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
*/
	private String getExtension(String fileName) {
		int index = fileName.lastIndexOf("."); // to separate filename and its extension.
		String basename = fileName.substring(0, index); // base name
		String extension = fileName.substring(index + 1); // ext name
		return extension;
	}
	
	public void constructReconDocFromConll(ConllDocument conllDoc) {
		
	}
	
}
