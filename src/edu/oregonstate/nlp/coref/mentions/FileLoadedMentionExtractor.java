package edu.oregonstate.nlp.coref.mentions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;

public class FileLoadedMentionExtractor extends AbstractMentionExtractor {

	public class MentSpan {
		public String docName;
		public int sentIdx;
		public int startIdx;
		public int endIdx;
		public int headIdx;
		public int globalMentIdx;
	}
	
	public HashMap<String, ArrayList<MentSpan>> docPredSpans;
	
	public FileLoadedMentionExtractor(String fn) {
		loadFile(fn);
	}
	
	@Override
	public AnnotationSet extractMentions(Document reconcileDoc, boolean injectGold) {
		
		String docFullName = reconcileDoc.getDocumentId();
		
		AnnotationSet result = null;
		if (docPredSpans.containsKey(docFullName)) {
			result = predictMentionWithFile(docPredSpans.get(docFullName), reconcileDoc, injectGold);
		} else {
			System.err.println("No such name:" + docFullName);
			result = new AnnotationSet(Constants.NP); // empty set
		}
		
		return result;
	}
	
	public AnnotationSet predictMentionWithFile(ArrayList<MentSpan> spans, Document rawDoc, boolean injectGold) {
		
		AnnotationSet tokenSet = rawDoc.getAnnotationSet(Constants.TOKEN);
		
		AnnotationSet sentSet = rawDoc.getAnnotationSet(Constants.SENT);
		List<Annotation> sentList = sentSet.getOrderedAnnots();
		
		ArrayList<List<Annotation>> sentTokenSorted = new ArrayList<List<Annotation>>();
		for (int sentIdx = 0; sentIdx < sentList.size(); sentIdx++) {
			Annotation sentAnno = sentList.get(sentIdx);
			List<Annotation> sentTkList = tokenSet.getContained(sentAnno).getOrderedAnnots();
			assert(sentTkList.size() > 0);
			sentTokenSorted.add(sentTkList);
		}
		
		HashSet<String> offset = new HashSet<String>();
		AnnotationSet initMentions = new AnnotationSet(Constants.NP);
		
		int cnt = 0;
		for (MentSpan mspan : spans) {
			
			List<Annotation> tokenInSent = sentTokenSorted.get(mspan.sentIdx);
			
			if (tokenInSent.size() <= (mspan.endIdx - 1) ) {
				System.err.println(mspan.docName + " " + mspan.sentIdx);
				System.err.println(tokenInSent.size() +" > "+ (mspan.endIdx - 1));
			}
			
			assert(mspan.startIdx < mspan.endIdx);
			Annotation startToken = tokenInSent.get(mspan.startIdx);
			Annotation endToken = tokenInSent.get(mspan.endIdx - 1);
			int startOffset = startToken.getStartOffset();
			int endOffset = endToken.getEndOffset();
			
			String offsetStr = BerkeleyConllMentionExtractor.getOffsetStr(startOffset, endOffset);
			if (!offset.contains(offsetStr)) {
				cnt++;
				Annotation np1 = new Annotation(cnt, startOffset, endOffset, "PredictMention");
				np1.setAttribute(Constants.CLUSTER_ID, String.valueOf(np1.getId()));
				np1.setAttribute(Constants.CE_ID, String.valueOf(np1.getId()));
				np1.setAttribute(Constants.GLOABAL_ID, String.valueOf(mspan.globalMentIdx));
				initMentions.add(np1);
				offset.add(offsetStr);
			}

		}
		
		// inject gold if needed
		if (injectGold) {
			injectGoldMentions(rawDoc, initMentions);
		}
		
		return initMentions;
	}
	
	public static void injectGoldMentions(Document doc, AnnotationSet initMents) {
		//AnnotationSet nps = initMents;
		AnnotationSet gnps = doc.getGoldMentionSet();

		if (gnps == null) {
			System.err.println("Can not inject gold mentions, no gold mention found at all!");
			return;
		}
		
		HashSet<String> offsets = new HashSet<String>();
		for (Annotation ce : initMents) {
			String offstr = BerkeleyConllMentionExtractor.getOffsetStr(ce.getStartOffset(), ce.getEndOffset());
			if (!offsets.contains(offstr)) {
				offsets.add(offstr);
			}
		}
		
		int cnt2 = initMents.size();
		// start to inject gold mentions!
		for (Annotation gce : gnps) {
			String offstr = BerkeleyConllMentionExtractor.getOffsetStr(gce.getStartOffset(), gce.getEndOffset());
			if (!offsets.contains(offstr)) {
				offsets.add(offstr);
				// insert gold
				cnt2++;
				Annotation np2 = new Annotation(cnt2, gce.getStartOffset(), gce.getEndOffset(), "InjectGoldMention");
				np2.setAttribute(Constants.CLUSTER_ID, String.valueOf(np2.getId()));
				np2.setAttribute(Constants.CE_ID, String.valueOf(np2.getId()));
				initMents.add(np2);
			}
		}
	}

	public void loadFile(String inputFile) {
		docPredSpans = new HashMap<String, ArrayList<MentSpan>>();
		
		/////////////////////////
		
		String line;
		FileReader reader;
		BufferedReader br;
		try {
			reader = new FileReader(inputFile);
			br = new BufferedReader(reader);

			int partCnt = 0;
			int lineCnt = 0;

			line = br.readLine();
			while (line != null) {
				if (!line.equals("")) {
					
					MentSpan ms = new MentSpan();
					String[] tks = line.split("\\s+");
					ms.docName = tks[0];
					ms.sentIdx = Integer.parseInt(tks[1]);
					ms.startIdx = Integer.parseInt(tks[2]);
					ms.endIdx = Integer.parseInt(tks[3]);
					ms.headIdx = Integer.parseInt(tks[4]);
					
					if (StringUtils.isNumeric(tks[5])) {
						ms.globalMentIdx = Integer.parseInt(tks[5]);
					} else {
						ms.globalMentIdx = -1;
					}
					
					lineCnt++;
					
					addMentSpan(docPredSpans, ms);
				}
				
				line = br.readLine();
			}

			br.close();
			reader.close();
			
			System.out.println("Total line = " + lineCnt);
			System.out.println("Loaded docs = " + docPredSpans.size());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addMentSpan(HashMap<String, ArrayList<MentSpan>> spanMap, MentSpan mspan) {
		if (spanMap.containsKey(mspan.docName)) {
			ArrayList<MentSpan> list = spanMap.get(mspan.docName);
			list.add(mspan);
		} else {
			ArrayList<MentSpan> list = new ArrayList<MentSpan>();
			list.add(mspan);
			spanMap.put(mspan.docName, list);
		}
	}
}
