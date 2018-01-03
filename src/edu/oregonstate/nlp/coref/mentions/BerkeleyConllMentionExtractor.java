package edu.oregonstate.nlp.coref.mentions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;

public class BerkeleyConllMentionExtractor extends AbstractMentionExtractor {
	
	private static class BerkeleyEnglishCorefLanguagePack {
		public static List<String> getMentionConstituentTypes() {
			String[] labels = new String[]{"NP", "NML"};
			return Arrays.asList(labels);
		}
		public static List<String> getPronominalTags() {
			String[] labels = {"PRP", "PRP$"};
			return Arrays.asList(labels);
		}
		public static List<String> getProperTags() {
			String[] labels = {"NNP"};
			return Arrays.asList(labels);
		}
	}
	
	
	@Override
	public AnnotationSet extractMentions(Document reconcileDoc, boolean injectGold) {
		AnnotationSet mentions = normalizeMentionOrdering(reconcileDoc, injectGold);
		return mentions;
	}

	public AnnotationSet normalizeMentionOrdering(Document reconcileDoc, boolean injectGold) {
		
		AnnotationSet initMents = predictMentions(reconcileDoc);
		
		// normalized mention ids
		AnnotationSet predMentions = new AnnotationSet(Constants.NP);
		List<Annotation> initMentArr = initMents.getOrderedAnnots();
		for (int mid = 0; mid < initMentArr.size(); mid++) {
			Annotation initm = initMentArr.get(mid);
			int id = mid + 1;
			Annotation np = new Annotation(id, initm.getStartOffset(), initm.getEndOffset(), "PredictMention");
			np.setAttribute(Constants.CLUSTER_ID, String.valueOf(np.getId()));
			np.setAttribute(Constants.CE_ID, String.valueOf(np.getId()));
			predMentions.add(np);
		}
		
		// inject gold if needed
		if (injectGold) {
			FileLoadedMentionExtractor.injectGoldMentions(reconcileDoc, predMentions);
		}
		
		return predMentions;
	}
	
	
	/*

  def createCorefDoc(rawDoc: ConllDoc, propertyComputer: MentionPropertyComputer): CorefDoc = {
    val (goldMentions, goldClustering) = extractGoldMentions(rawDoc, propertyComputer);
    if (goldMentions.size == 0) {
      Logger.logss("WARNING: no gold mentions on document " + rawDoc.printableDocName);
    }
    val predMentions = if (useGoldMentions) goldMentions else extractPredMentions(rawDoc, propertyComputer, goldMentions);
    new CorefDoc(rawDoc, goldMentions, goldClustering, predMentions)
  }
  
  def extractGoldMentions(rawDoc: ConllDoc, propertyComputer: MentionPropertyComputer): (Seq[Mention], OrderedClustering) = {
    CorefDocAssembler.extractGoldMentions(rawDoc, propertyComputer, langPack);
  }
  
  def extractPredMentions(rawDoc: ConllDoc, propertyComputer: MentionPropertyComputer, gms: Seq[Mention]): Seq[Mention] = {
    val protoMentionsSorted = getProtoMentionsSorted(rawDoc, gms);
    val finalMentions = new ArrayBuffer[Mention]();
    for (sentProtoMents <- protoMentionsSorted; protoMent <- sentProtoMents) {
      finalMentions += Mention.createMentionComputeProperties(rawDoc, finalMentions.size, protoMent.sentIdx, protoMent.startIdx, protoMent.endIdx, protoMent.headIdx, Seq(protoMent.headIdx), false, propertyComputer, langPack)
    }
    finalMentions;
  }
  
  private def getProtoMentionsSorted(rawDoc: ConllDoc, gms: Seq[Mention]): Seq[Seq[ProtoMention]] = {
    val mentionExtents = (0 until rawDoc.numSents).map(i => new HashSet[ProtoMention]);
    for (sentIdx <- 0 until rawDoc.numSents) {
      // Extract NE spans: filter out O, QUANTITY, CARDINAL, CHUNK
      // Throw out NE types which aren't mentions
      val filterNEsByType: Chunk[String] => Boolean = chunk => !(chunk.label == "O" || chunk.label == "QUANTITY" || chunk.label == "CARDINAL" || chunk.label == "PERCENT");
      // Extract NPs and PRPs *except* for those contained in NE chunks (the NE tagger seems more reliable than the parser)
      val posAndConstituentsOfInterest = langPack.getMentionConstituentTypes ++ langPack.getPronominalTags;
      for (label <- posAndConstituentsOfInterest) {
        mentionExtents(sentIdx) ++= rawDoc.trees(sentIdx).getSpansAndHeadsOfType(label).map(span => new ProtoMention(sentIdx, span._1, span._2, span._3));
      }
      // Add NEs if we want
      val neMentType = Driver.neMentType
      if (neMentType == "all") {
        val neProtoMents = rawDoc.nerChunks(sentIdx).filter(filterNEsByType).
            map(chunk => new ProtoMention(sentIdx, chunk.start, chunk.end, rawDoc.trees(sentIdx).getSpanHead(chunk.start, chunk.end)));
        mentionExtents(sentIdx) ++= neProtoMents
      } else if (neMentType == "nnp") {
        val spans = getMaximalNNPSpans(rawDoc.pos(sentIdx));
        val neProtoMents = spans.map(span => new ProtoMention(sentIdx, span._1, span._2, rawDoc.trees(sentIdx).getSpanHead(span._1, span._2)));
        mentionExtents(sentIdx) ++= neProtoMents
      } else {
        // Do nothing
      }
    }
    // Now take maximal mentions with the same heads
    if (Driver.filterNonMaximalNPs) {
      filterNonMaximalNPs(rawDoc, mentionExtents).map(CorefDocAssembler.sortProtoMentionsLinear(_));
    } else {
      mentionExtents.map(protoMents => CorefDocAssembler.sortProtoMentionsLinear(new ArrayBuffer[ProtoMention] ++ protoMents));
    }
  }
  
  private def filterNonMaximalNPs(rawDoc: ConllDoc, mentionExtents: Seq[HashSet[ProtoMention]]) = {
    val filteredProtoMentionsSorted = (0 until rawDoc.numSents).map(i => new ArrayBuffer[ProtoMention]);
    for (sentIdx <- 0 until mentionExtents.size) {
      val protoMentionsByHead = mentionExtents(sentIdx).groupBy(_.headIdx);
      // Look from smallest head first
      for (head <- protoMentionsByHead.keys.toSeq.sorted) {
        // Find the biggest span containing this head
        var currentBiggest: ProtoMention = null;
        for (ment <- protoMentionsByHead(head)) {
          // Overlapping but neither is contained in the other
          if (currentBiggest != null && ((ment.startIdx < currentBiggest.startIdx && ment.endIdx < currentBiggest.endIdx) || (ment.startIdx > currentBiggest.startIdx && ment.endIdx > currentBiggest.endIdx))) {
            Logger.logss("WARNING: mentions with the same head but neither contains the other");
            Logger.logss("  " + rawDoc.words(sentIdx).slice(ment.startIdx, ment.endIdx) + ", head = " + rawDoc.words(sentIdx)(head));
            Logger.logss("  " + rawDoc.words(sentIdx).slice(currentBiggest.startIdx, currentBiggest.endIdx) + ", head = " + rawDoc.words(sentIdx)(head));
          }
          // This one is bigger
          if (currentBiggest == null || (ment.startIdx <= currentBiggest.startIdx && ment.endIdx >= currentBiggest.endIdx)) {
            currentBiggest = ment;
          }
        }
        filteredProtoMentionsSorted(sentIdx) += currentBiggest;
        // ENGLISH ONLY: don't remove appositives
        for (ment <- protoMentionsByHead(head)) {
          val isNotBiggest = ment.startIdx != currentBiggest.startIdx || ment.endIdx != currentBiggest.endIdx;
          val isAppositiveLike = ment.endIdx < rawDoc.pos(sentIdx).size && (rawDoc.pos(sentIdx)(ment.endIdx) == "," || rawDoc.pos(sentIdx)(ment.endIdx) == "CC");
          if (isNotBiggest && isAppositiveLike && Driver.includeAppositives) {
            filteredProtoMentionsSorted(sentIdx) += ment;
          }
        }
      }
    }
    filteredProtoMentionsSorted;
  }
  
  private def getMaximalNNPSpans(tags: Seq[String]) = {
    var start = -1;
    var inside = false;
    val spans = new ArrayBuffer[(Int,Int)]
    for (i <- 0 until tags.size) {
      if (tags(i).startsWith("NNP") && (i == 0 || !tags(i-1).startsWith("NNP"))) {
        start = i
        inside = true;
      }
      if (inside && !tags(i).startsWith("NNP")) {
        spans += start -> i;
        start = -1;
        inside = false;
      }
    }
    spans;
  }

*/
	
	public static String getOffsetStr(int s, int e) {
		String re = String.valueOf(s) + "-" + String.valueOf(e);
		return re;
	}
	
	
	public AnnotationSet predictMentions(Document reconcileDoc) {
		
		HashSet<String> offset = new HashSet<String>();
		
		
		HashSet<String> tagSet = new HashSet<String>();
		tagSet.addAll(BerkeleyEnglishCorefLanguagePack.getMentionConstituentTypes());
		tagSet.addAll(BerkeleyEnglishCorefLanguagePack.getPronominalTags());
		
		AnnotationSet initMentions = new AnnotationSet(Constants.NP);
		
		
		AnnotationSet consituents = reconcileDoc.getAnnotationSet(Constants.PARSE);
		
		int cnt = 0;
		
		for (Annotation constituent : consituents) {
			if (tagSet.contains(constituent.getType())) {
				//System.out.println(constituent.getType());
				String offsetStr = getOffsetStr(constituent.getStartOffset(), constituent.getEndOffset());
				if (!offset.contains(offsetStr)) {
					cnt++;
					Annotation np1 = new Annotation(cnt, constituent.getStartOffset(), constituent.getEndOffset(), "TempPredictMention");
					np1.setAttribute(Constants.CLUSTER_ID, String.valueOf(np1.getId()));
					np1.setAttribute(Constants.CE_ID, String.valueOf(np1.getId()));
					initMentions.add(np1);//predMentions.add(np1);
					offset.add(offsetStr);
				}
			}
		}
		
		HashSet<String> neSet = new HashSet<String>();
		neSet.add("O"); neSet.add("QUANTITY");
		neSet.add("CARDINAL"); neSet.add("PERCENT");
		AnnotationSet nes = reconcileDoc.getAnnotationSet(Constants.NE);
		for (Annotation ne : nes) {
			if (!neSet.contains(ne.getType())) {
				String offsetStr = getOffsetStr(ne.getStartOffset(), ne.getEndOffset());
				if (!offset.contains(offsetStr)) {
					cnt++;
					Annotation np2 = new Annotation(cnt, ne.getStartOffset(), ne.getEndOffset(), "TempPredictMention");
					np2.setAttribute(Constants.CLUSTER_ID, String.valueOf(np2.getId()));
					np2.setAttribute(Constants.CE_ID, String.valueOf(np2.getId()));
					initMentions.add(np2);
					offset.add(offsetStr);
				}
			}
		}

		return initMentions;
	}

/*
	private List<Annotation> filterNonMaximalNPs(Document rawDoc, AnnotationSet mentionExtents) {//: Seq[HashSet[ProtoMention]]) {
		boolean includeAppositives = true;
		//val filteredProtoMentionsSorted = (0 until rawDoc.numSents).map(i => new ArrayBuffer[ProtoMention]);
		ArrayList<ArrayList<Annotation>> filteredProtoMentionsSorted = new ArrayList<ArrayList<Annotation>>();
		AnnotationSet sentSet = rawDoc.getAnnotationSet(Constants.SENT);
		List<Annotation> sentList = sentSet.getOrderedAnnots();
				
				
		
		
		for (int sentIdx = 0; sentIdx < sentList.size(); sentIdx++) {
			Annotation sentAnno = sentList.get(sentIdx);
			List<Annotation> sentMents = mentionExtents.getContained(sentAnno).getOrderedAnnots();
			
			val protoMentionsByHead = mentionExtents(sentIdx).groupBy(_.headIdx);
			// Look from smallest head first
			for (head <- protoMentionsByHead.keys.toSeq.sorted) {
				// Find the biggest span containing this head
				Annotation currentBiggest = null;
				for (ment <- protoMentionsByHead(head)) {
					// This one is bigger
					if (currentBiggest == null || (ment.startIdx <= currentBiggest.startIdx && ment.endIdx >= currentBiggest.endIdx)) {
						currentBiggest = ment;
					}
				}
				filteredProtoMentionsSorted(sentIdx) += currentBiggest;
				// ENGLISH ONLY: don't remove appositives
				for (ment <- protoMentionsByHead(head)) {
					boolean isNotBiggest = ((ment.startIdx != currentBiggest.startIdx) || (ment.endIdx != currentBiggest.endIdx));
					val isAppositiveLike = ment.endIdx < rawDoc.pos(sentIdx).size && (rawDoc.pos(sentIdx)(ment.endIdx) == "," || rawDoc.pos(sentIdx)(ment.endIdx) == "CC");
					if (isNotBiggest && isAppositiveLike && includeAppositives) {
						filteredProtoMentionsSorted(sentIdx) += ment;
					}
				}
			}
		}
		filteredProtoMentionsSorted;
	}
*/
	

/*
	private List<Annotation> filterNonMaximalNPs(rawDoc: ConllDoc, mentionExtents: Seq[HashSet[ProtoMention]]) {
		includeAppositives = true;
		val filteredProtoMentionsSorted = (0 until rawDoc.numSents).map(i => new ArrayBuffer[ProtoMention]);
		for (sentIdx <- 0 until mentionExtents.size) {
			val protoMentionsByHead = mentionExtents(sentIdx).groupBy(_.headIdx);
			// Look from smallest head first
			for (head <- protoMentionsByHead.keys.toSeq.sorted) {
				// Find the biggest span containing this head
				var currentBiggest: ProtoMention = null;
				for (ment <- protoMentionsByHead(head)) {
					// Overlapping but neither is contained in the other
					if (currentBiggest != null && ((ment.startIdx < currentBiggest.startIdx && ment.endIdx < currentBiggest.endIdx) || (ment.startIdx > currentBiggest.startIdx && ment.endIdx > currentBiggest.endIdx))) {
						Logger.logss("WARNING: mentions with the same head but neither contains the other");
						Logger.logss("  " + rawDoc.words(sentIdx).slice(ment.startIdx, ment.endIdx) + ", head = " + rawDoc.words(sentIdx)(head));
						Logger.logss("  " + rawDoc.words(sentIdx).slice(currentBiggest.startIdx, currentBiggest.endIdx) + ", head = " + rawDoc.words(sentIdx)(head));
					}
					// This one is bigger
					if (currentBiggest == null || (ment.startIdx <= currentBiggest.startIdx && ment.endIdx >= currentBiggest.endIdx)) {
						currentBiggest = ment;
					}
				}
				filteredProtoMentionsSorted(sentIdx) += currentBiggest;
				// ENGLISH ONLY: don't remove appositives
				for (ment <- protoMentionsByHead(head)) {
					boolean isNotBiggest = ((ment.startIdx != currentBiggest.startIdx) || (ment.endIdx != currentBiggest.endIdx));
					val isAppositiveLike = ment.endIdx < rawDoc.pos(sentIdx).size && (rawDoc.pos(sentIdx)(ment.endIdx) == "," || rawDoc.pos(sentIdx)(ment.endIdx) == "CC");
					if (isNotBiggest && isAppositiveLike && includeAppositives) {
						filteredProtoMentionsSorted(sentIdx) += ment;
					}
				}
			}
		}
		filteredProtoMentionsSorted;
	}
 
	private void getMaximalNNPSpans(List<String> tags) {
		int start = -1;
		boolean inside = false;
		val spans = new ArrayBuffer[(Int,Int)];
		for (int i = 0; i < tags.size(); i++) {
			if (tags.get(i).startsWith("NNP") && (i == 0 || !tags.get(i-1).startsWith("NNP"))) {
				start = i;
				inside = true;
			}
			if (inside && !tags.get(i).startsWith("NNP")) {
				spans += start -> i;
				start = -1;
				inside = false;
			}
		}
		spans;
	}
*/
/*
	public List<Annotation> extractPredMentions(Document rawDoc) { //(rawDoc: ConllDoc, propertyComputer: MentionPropertyComputer, gms: Seq[Mention]) {
		List<Annotation> protoMentionsSorted = getProtoMentionsSorted(rawDoc);
		List<Annotation> finalMentions = new ArrayList<Annotation>(); //ArrayBuffer[Mention]();
		//for (sentProtoMents <- protoMentionsSorted; protoMent <- sentProtoMents) {
		//	finalMentions += Mention.createMentionComputeProperties(rawDoc, finalMentions.size, protoMent.sentIdx, protoMent.startIdx, protoMent.endIdx, protoMent.headIdx, Seq(protoMent.headIdx), false, propertyComputer, langPack)
		//}
		return finalMentions;
	}
*/

}