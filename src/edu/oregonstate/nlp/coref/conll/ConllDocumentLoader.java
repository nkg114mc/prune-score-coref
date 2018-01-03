package edu.oregonstate.nlp.coref.conll;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.inout.InputConllFormat;

public class ConllDocumentLoader {
	


	public ArrayList<ConllDocument> conllDocList = null;
	public ArrayList<Document> reconDocList = null;
	
	public ArrayList<ConllDocument> getConllList() {
		return conllDocList;
	}
	public ArrayList<Document> getReconList() {
		return reconDocList;
	}
	
	public void clear() { // clear all list
		conllDocList = null;
		reconDocList = null;
		System.gc();
	}
	
	// load file from names ...
	public void loadConllDocumentList(List<File> fileNames) {
		
		conllDocList = new ArrayList<ConllDocument>();
		reconDocList = new ArrayList<Document>();

		for (File conllFilePath : fileNames) {
			
			System.out.println("Conll doc Index: " + conllDocList.size());
			
			ConllDocument conllDoc = loadConllDocument(conllFilePath);
			// global list
			reconDocList.addAll(conllDoc.getReconDocList());
			conllDocList.add(conllDoc);
		}

		System.out.println("Total conll docs: " + conllDocList.size());
		System.out.println("Total reconcile docs: " + reconDocList.size());
	}
	
	
	public ConllDocument loadConllDocument(File conllFile) {
		
		
		assert(conllFile.exists());

		ConllDocument returnConllDoc = new ConllDocument(conllFile); // set the conll file path

		//returnConllDoc.setDocName(conllFile.getName());

		// load annotation
		AnnotationSet tmpConllAnnoSet = OntoNotesConllReader.extractConllAnnotationSet(conllFile.getAbsolutePath(), Constants.CONLL_ANNO);
		returnConllDoc.setAnnotationSet(tmpConllAnnoSet);
		// doc name?
		AnnotationSet nameAnno = tmpConllAnnoSet.get(OntoNotesConllReader.OFFICIAL_DOCNAME);
		String officialName = nameAnno.getFirst().getAttribute("DocName");
		returnConllDoc.setDocName(officialName);
		//OntoNotesFileConvertor.writeAnnotationSet(new File(conllFilePath.getParent(), Constants.CONLL_ANNO), tmpConllAnnoSet);

		// part num
		int nPart = OntoNotesConllReader.getTotalPartNum(tmpConllAnnoSet);
		returnConllDoc.setPartNum(nPart);

		InputConllFormat conllInputer = new InputConllFormat();
		conllInputer.inputConllFormat(returnConllDoc, false);
		
		return returnConllDoc;
	}
	
	private void initConllDocument() {
		
	}
	

	
	public static ArrayList<ConllDocument> connectAllConllList(List<ConllDocument> ... doclist) {
		ArrayList<ConllDocument> completeList = new ArrayList<ConllDocument>();
		for (List<ConllDocument> singleList : doclist) {
			if (singleList != null) {
				completeList.addAll(singleList);
			}
		}
		return completeList;
	}
	public static ArrayList<Document> connectAllReconList(List<Document> ... doclist) {
		ArrayList<Document> completeList = new ArrayList<Document>();
		for (List<Document> singleList : doclist) {
			if (singleList != null) {
				completeList.addAll(singleList);
			}
		}
		return completeList;
	}

}


/*
// load file from names ...
public void loadConllDocumentList(List<File> fileNames) {
	
	conllDocList = new ArrayList<ConllDocument>();
	reconDocList = new ArrayList<Document>();

	for (File conllFilePath : fileNames) {
		
		assert(conllFilePath.exists());

		ConllDocument returnConllDoc = new ConllDocument(conllFilePath.getParentFile()); // set the folder that contains this conll file as the root dir

		// doc name?
		returnConllDoc.setDocName(conllFilePath.getName());

		// load annotation
		AnnotationSet tmpConllAnnoSet = OntoNotesConllReader.extractConllAnnotationSet(conllFilePath.getAbsolutePath(), Constants.CONLL_ANNO);
		returnConllDoc.setAnnotationSet(tmpConllAnnoSet);
		//OntoNotesFileConvertor.writeAnnotationSet(new File(conllFilePath.getParent(), Constants.CONLL_ANNO), tmpConllAnnoSet);

		// part num
		int nPart = OntoNotesConllReader.getTotalPartNum(tmpConllAnnoSet);
		returnConllDoc.setPartNum( nPart );

		// load part documents
		for (int i = 0; i < nPart; i++) {
			String partDocName = new String("docpart" + i);
			File reconDocFile = new File(conllFilePath.getParentFile(), partDocName);
			if (!reconDocFile.exists()) {
				throw new RuntimeException("Part doc "+reconDocFile.getAbsolutePath() +" was not found. You may need to preprocess the doc.");
			}

			Document reconDoc = new Document(reconDocFile);
			returnConllDoc.addDocument(reconDoc);

			// global list
			reconDocList.add(reconDoc);
		}
		
		// global list
		conllDocList.add(returnConllDoc);
	}

}
*/

/*
// static method that loading a document with a conll file path
public static ConllDocument loadConllDocumet(File conllFilePath) {
	
	assert(conllFilePath.exists());
	
	OntoNotesConllReader conllReader = new OntoNotesConllReader("");
	ConllDocument returnConllDoc = new ConllDocument(conllFilePath.getParentFile()); // set the folder that contains this conll file as the root dir
	
	// doc name?
	returnConllDoc.setDocName(conllFilePath.getName());
	
	// load annotation
	AnnotationSet tmpConllAnnoSet = conllReader.extractConllAnnotationSet(conllFilePath.getAbsolutePath(), Constants.CONLL_ANNO);
	returnConllDoc.setAnnotationSet(tmpConllAnnoSet);
	
	// part num
	int nPart = conllReader.getTotalPartNum(tmpConllAnnoSet);
	returnConllDoc.setPartNum( nPart );
	
	// load part documents
	for (int i = 0; i < nPart; i++) {
		String partDocName = new String("docpart" + i);
		File reconDocFile = new File(conllFilePath.getParentFile(), partDocName);
		if (!reconDocFile.exists()) {
			throw new RuntimeException("Part doc "+reconDocFile.getAbsolutePath() +" was not found. You may need to preprocess the doc.");
		}

		Document reconDoc = new Document(reconDocFile);
		returnConllDoc.addDocument(reconDoc);
	}

	return returnConllDoc;
}
*/
