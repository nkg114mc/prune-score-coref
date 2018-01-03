package edu.oregonstate.nlp.coref.conll;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.mentions.MentionPrecdiction;

/**
 * Conll Document is to read the conll files.
 * The main reason to design this class is because, some conll document contains
 * several parts. Each part can be regards as a single document, but Reconcile 
 * system did not process this (they see it as a single huge document).
 * 
 * Thus, each ConllDocument may contain several Reconcile Documents. Each corresponds
 * a "part" in the original conll document.
 * 
 * */
public class ConllDocument {

	int id;
	String docName;
	int partNum;
	
	File docDir;
	AnnotationSet conllAnnoSet;
	ArrayList<Document> reconPartDocList;
	
	public ConllDocument() {
		docName = "unknown";
		partNum = 0;
		docDir = null;
		conllAnnoSet = null;
		reconPartDocList = null;
	}
	
	public ConllDocument(File path) {
		docName = path.getName();
		partNum = 0;
		docDir = path;
		conllAnnoSet = null;
		reconPartDocList = null;
	}
	
	public int getPartNum() {
		return partNum;
	}
	public void setPartNum(int pnum) {
		partNum = pnum;
	}
	
	///public String getAbsPath() {
	//	return docDir.getParent(); // dir path
	//}
	
	public File getFile() {
		return docDir;
	}
	
	public void setDocName(String name) {
		docName = name;
	}
	
	public String getDocName() {
		return docName;
	}
	
	public void setAnnotationSet(AnnotationSet annoSet) {
		conllAnnoSet = annoSet;
	}
	public AnnotationSet getAnnotationSet() {
		return conllAnnoSet;
	}
	
	public void addDocument(Document d) {
		if (reconPartDocList == null) {
			reconPartDocList = new ArrayList<Document>();
		}
		reconPartDocList.add(d);
	}
	
	public ArrayList<Document> getReconDocList() {
		return reconPartDocList;
	}
	
	public void setReconDocList(ArrayList<Document> reconlsit) {
		reconPartDocList = reconlsit;
	}

	/*
	public void setDocPartList(ArrayList<DocPart> dpl) {
		docPartList = dpl;
	}
	public  ArrayList<DocPart> getDocPartList() {
		return docPartList;
	}
	*/
	
	// output result
	public void outputGoldCorefResult(String filePath) { // only gold

	}
	
	public void outputPredCorefResult(String filePath) { // only predict
		
	}
	
	/**
	 * A conll document may contain several parts. Since each part was treated as an independent
	 * reconcile document, the clusterID might conflict each other. This method is to resolve this
	 * conflicts.
	 * */
	public void clusterIDGlobalize() {

		// only need to do this when there are more than 1 parts in this conll doc
		if (partNum > 0) {
			
			int globalClusterID = 0;
			
			for (int part = 0; part < partNum; part++) {

				Document partDoc = reconPartDocList.get(part);
				AnnotationSet ourMentions = partDoc.getAnnotationSet(Constants.NP); // predict mentions
				
				assert(ourMentions != null);

				// all original cluster ids
				HashSet<Integer> allClusterIDs = new HashSet<Integer>();
				for (Annotation men : ourMentions) {
					int cid = Integer.parseInt(men.getAttribute(Constants.CLUSTER_ID));
					allClusterIDs.add(cid);
				}
				
				// map local to global
				HashMap<Integer, Integer> localToGlobal = new HashMap<Integer, Integer>();
				for (Integer localCID :  allClusterIDs) {
					// increase our golbal cluster ID
					globalClusterID++;
					
					// to distinguish it with mention ID, we ask cluster ID start from 1000
					int mygcid = 1000 + globalClusterID;
					localToGlobal.put(localCID, mygcid);
				}
				
				// rename the local cluster ids
				for (Annotation men : ourMentions) {
					int localCID = Integer.parseInt(men.getAttribute(Constants.CLUSTER_ID));
					int globalCID = localToGlobal.get(localCID);
					men.setAttribute(Constants.CLUSTER_ID, String.valueOf(globalCID));
					//System.out.println("Renaming cluster id: " + localCID + " --> " + globalCID);
				}
			}
		}
	}
	
	// conll post process
	public void postProcess() {
		
		// 1) remove singleton cluster
		removeSingletonCluster();
	}

	public void removeSingletonCluster() {
		
		int npart = partNum;
		
		for (int part = 0; part < npart; part++) {
			
			Document partDoc = reconPartDocList.get(part);
			
			HashMap<Integer, Annotation> menMaps = new HashMap<Integer, Annotation>();
			HashMap<Integer, HashSet<Integer>> clusters = new HashMap<Integer, HashSet<Integer>>();
			HashSet<Integer> singletonMentions = new HashSet<Integer>();
			
			// predict mention annotations
			AnnotationSet ourMentions = partDoc.getAnnotationSet(Constants.NP);

			// collect the clusters
			for (Annotation mention : ourMentions) {
				int mid = Integer.parseInt(mention.getAttribute(Constants.CE_ID));
				int cid = Integer.parseInt(mention.getAttribute(Constants.CLUSTER_ID));
				menMaps.put(mid, mention);
				if (clusters.containsKey(cid)) {
					HashSet<Integer> clust = clusters.get(cid);
					clust.add(mid);
				} else {
					HashSet<Integer> clust = new HashSet<Integer>();
					clust.add(mid);
					clusters.put(cid, clust);
				}
			}
			
			// find the singleton
			for (Integer clusterID : clusters.keySet()) {
				HashSet<Integer> eachCluster = clusters.get(clusterID);
				if (eachCluster.size() == 1) { // singleton
					System.out.print("Singleton-Cluster: { ");
					for (int eachMID : eachCluster) {
						singletonMentions.add(eachMID); // add it into removing list!
						System.out.print(eachMID + ", ");
					}
					System.out.println(" }");
				} else {
					System.out.print("Multi-Cluster: { ");
					for (int eachMID : eachCluster) {
						System.out.print(eachMID + ", ");
					}
					System.out.println(" }");
				}
			}

			// remove the singleton clusters!
			for (Integer singleMen : singletonMentions) {
				Annotation actualMention = menMaps.get(singleMen);
				ourMentions.remove(actualMention);
			}
			
			
			for (Integer clusterID : clusters.keySet()) {
				HashSet<Integer> eachCluster = clusters.get(clusterID);
				if (eachCluster.size() == 1) { // singleton
					
				} else {
					System.out.print("Multi-Cluster: { ");
					for (int eachMID : eachCluster) {
						System.out.print(eachMID + ", ");
					}
					System.out.println(" }");
				}
			}
		}
		
	}
	
	
	public void removeSingletonClusterSlient() {
		
		int npart = partNum;
		for (int part = 0; part < npart; part++) {
			Document partDoc = reconPartDocList.get(part);
			removeSingletons(partDoc);
		}
		
	}
	
	// static version remove singleton
	public static void removeSingletons(Document partDoc) {

		HashMap<Integer, Annotation> menMaps = new HashMap<Integer, Annotation>();
		HashMap<Integer, HashSet<Integer>> clusters = new HashMap<Integer, HashSet<Integer>>();
		HashSet<Integer> singletonMentions = new HashSet<Integer>();

		// predict mention annotations
		AnnotationSet ourMentions = partDoc.getAnnotationSet(Constants.NP);

		// collect the clusters
		for (Annotation mention : ourMentions) {
			int mid = Integer.parseInt(mention.getAttribute(Constants.CE_ID));
			int cid = Integer.parseInt(mention.getAttribute(Constants.CLUSTER_ID));
			menMaps.put(mid, mention);
			if (clusters.containsKey(cid)) {
				HashSet<Integer> clust = clusters.get(cid);
				clust.add(mid);
			} else {
				HashSet<Integer> clust = new HashSet<Integer>();
				clust.add(mid);
				clusters.put(cid, clust);
			}
		}

		// find the singleton
		for (Integer clusterID : clusters.keySet()) {
			HashSet<Integer> eachCluster = clusters.get(clusterID);
			if (eachCluster.size() == 1) { // singleton
				//System.out.print("Singleton-Cluster: { ");
				for (int eachMID : eachCluster) {
					singletonMentions.add(eachMID); // add it into removing list!
					//System.out.print(eachMID + ", ");
				}
				//System.out.println(" }");
			} else {
				//System.out.print("Multi-Cluster: { ");
				for (int eachMID : eachCluster) {
					//System.out.print(eachMID + ", ");
				}
				//System.out.println(" }");
			}
		}

		// remove the singleton clusters!
		for (Integer singleMen : singletonMentions) {
			Annotation actualMention = menMaps.get(singleMen);
			ourMentions.remove(actualMention);
		}

		/*
		for (Integer clusterID : clusters.keySet()) {
			HashSet<Integer> eachCluster = clusters.get(clusterID);
			if (eachCluster.size() == 1) { // singleton

			} else {
				//System.out.print("Multi-Cluster: { ");
				//for (int eachMID : eachCluster) {
				//	System.out.print(eachMID + ", ");
				//}
				//System.out.println(" }");
			}
		}
		*/
	}
	
	public String goldAnnotationToStr() {
		
		StringBuilder sb = new StringBuilder();
		MentionPrecdiction corefResultToConll = new MentionPrecdiction();
		int npart = partNum;
		int totalTokenCnt = 0;
		
		//System.out.println("Generating gold coref result for doc " + docName);
		
		for (int part = 0; part < npart; part++) {
			
			// doc begin comment
			sb.append("#begin document " + "(" + docName + "); part " + partStr(part) + "\n");
			
			Document partDoc = reconPartDocList.get(part); //assert(partDoc == null);
			corefResultToConll.printCorefMentions(partDoc); // it is not only printing, but also align the coref results according to the tokens
			
			//System.out.println("... ...");
			// gold mention annotations
			AnnotationSet standardTk = conllAnnoSet.get(OntoNotesConllReader.getPartTagStr(part));

			int len = standardTk.size();
			for (int i = 0; i < len; i++) {
				Annotation t1 = standardTk.get(totalTokenCnt);
				//Annotation t2 = ourTk.get(i+1);
				
				//System.out.println(i + " " + t1.getAttribute(Constants.CONLL_WORDSTR) + " " + partDoc.getAnnotString(t2));
				String tokenStr = t1.getAttribute(Constants.CONLL_WORDSTR);
				String goldCorefAnno = t1.getAttribute(Constants.CONLL_COREF);
				//String predCorefAnno = t2.getAttribute(Constants.CONLL_COREF);
				
				String thisLine = new String((i+1) + "\t" + tokenStr + "\t" + goldCorefAnno + "\n");
				sb.append(thisLine);
				
				//System.out.print(thisLine);
				totalTokenCnt++;
			}

			// doc end comment
			sb.append("#end document\n");
			
			//System.out.println("Done part " + part  +", Now next ...");
		}
		
		
		return sb.toString();
	}
	
	public String predAnnotationToStr() {
		
		StringBuilder sb = new StringBuilder();
		MentionPrecdiction corefResultToConll = new MentionPrecdiction();
		int npart = partNum;
		int totalTokenCnt = 0;
		
		//System.out.println("Globalize the cluster Ids in each parts, if essicary.");
		clusterIDGlobalize();
		
		//System.out.println("Generating gold coref result for doc " + docName);
		//System.out.println("DocName: " + docName +" Npart: " + npart);

		
		for (int part = 0; part < npart; part++) {
			
			// doc begin comment
			sb.append("#begin document " + "(" + docName + "); part " + partStr(part) + "\n");

			Document partDoc = reconPartDocList.get(part);
			corefResultToConll.printCorefMentions(partDoc); // it is not only printing, but also align the coref results according to the tokens
			
			// predict mention annotations
			AnnotationSet ourTk = partDoc.getAnnotationSet(Constants.TOKEN);
			List<Annotation> ourTkArr = ourTk.getOrderedAnnots();

			//int len = ourTk.size();
			for (int i = 0; i < ourTkArr.size(); i++) {
				Annotation t2 = ourTkArr.get(i);
				assert(t2 != null);
				
				String tokenStr = t2.getAttribute(Constants.CONLL_WORDSTR);
				String predCorefAnno = t2.getAttribute(Constants.CONLL_COREF);
				
				String thisLine = new String((i+1) + "\t" + tokenStr + "\t" + predCorefAnno + "\n");
				sb.append(thisLine);
				
				//System.out.print(thisLine);
				totalTokenCnt++;
			}

			// doc end comment
			sb.append("#end document\n");
			
			//System.out.println("Done part " + part  +", Now next ...");
		}
		
		return sb.toString();
	}
	
	public void outputGoldPredictTogether() { // gold + predict
		
		MentionPrecdiction corefResultTokenization = new MentionPrecdiction();
		int npart = partNum;
		int totalTokenCnt = 0;
		
		System.out.println("DocName: " + docName +" Npart: " + npart);
		
		for (int part = 0; part < npart; part++) {
			Document partDoc = reconPartDocList.get(part);
			corefResultTokenization.printCorefMentions(partDoc); // it is not only printing, but also align the coref results according to the tokens

			AnnotationSet ourTk = partDoc.getAnnotationSet(Constants.TOKEN);
			AnnotationSet standardTk = conllAnnoSet.get(OntoNotesConllReader.getPartTagStr(part));
			
			// size of two token set is equal?
			if (standardTk.size() != ourTk.size()) {
				throw new RuntimeException(docName + " N tokens: standard = " + standardTk.size() + " predict = " + ourTk.size());
			}
			
			// if their size are the same ...
			int len = standardTk.size();
			if (len > ourTk.size()) len = ourTk.size();
			
			for (int i = 0; i < len; i++) {
				//Annotation t1 = standardTk.get(i);
				Annotation t1 = standardTk.get(totalTokenCnt);
				Annotation t2 = ourTk.get(i+1);
				
				//System.out.println(i + " " + t1.getAttribute(Constants.CONLL_WORDSTR) + " " + partDoc.getAnnotString(t2));
				String tokenStr = t1.getAttribute(Constants.CONLL_WORDSTR);

				String goldCorefAnno = t1.getAttribute(Constants.CONLL_COREF);
				String predCorefAnno = t2.getAttribute(Constants.CONLL_COREF);
				
				String thisLine = new String((i+1) + "\t" + tokenStr + "\t" + goldCorefAnno + "\t" + predCorefAnno);
				//System.out.println(thisLine);
				totalTokenCnt++;
			}

			System.out.println("Done part " + part  +", Now next ...");
		}
	}

	private String partStr(int partID) {
		if (partID < 10) return ("00" + String.valueOf(partID));
		if (partID >= 10 && partID < 100) return ("0" + String.valueOf(partID)); 
		return String.valueOf(partID);
	}

}
