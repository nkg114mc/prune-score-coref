package edu.oregonstate.nlp.coref.inout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.featureExtractor.ParserStanfordParser;
import edu.berkeley.nlp.syntax.Constituent;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.ling.HeadFinder;
import edu.berkeley.nlp.syntax.Trees.PennTreeRenderer;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

public class ParseTreeUtils implements Serializable{

	private static String PARENT_ATTR = "parent";
	private static String CHILD_IDS_ATTR = "CHILD_IDS";
	
	private static ModCollinsHeadFinder hfEnglish = new ModCollinsHeadFinder();
	
	// for dependency

	
	public static void FlattenParseTreeWithTokens(AnnotationSet parse, AnnotationSet tokens) {
		
	}
	
	
	private static boolean isLeaf(Annotation parseNode) {
		String childIDs = parseNode.getAttribute(CHILD_IDS_ATTR);
		if (childIDs == null) {
			return true;
		}
		return false;
	}

	
	// return a list of tree nodes in ordering
	public static ArrayList<Annotation> getAllRoots(AnnotationSet parse) {
		ArrayList<Annotation> roots = new ArrayList<Annotation>();
		for (Annotation node : parse.getOrderedAnnots()) {
			String parentStr = node.getAttribute(PARENT_ATTR);
			int parentId = Integer.parseInt(parentStr);
			if (parentId < 0) { // this is root!
				roots.add(node);
			}
		}
		return roots;
	}
	
	public static AnnotationSet getAllRootsAsAnnoSet(AnnotationSet parse) {
		ArrayList<Annotation> roots = getAllRoots(parse);
		AnnotationSet rootSet = new AnnotationSet("tmp_root");
		for (Annotation rt : roots) {
			rootSet.add(rt);
		}
		return rootSet;
	}
	
	public static ArrayList<Tree<String>> constructAllTreeFromAnnotationSet(Document doc, AnnotationSet parse) {
		ArrayList<Annotation> allSentRoots = getAllRoots(parse);
		ArrayList<Tree<String>> allTrees = new ArrayList<Tree<String>>();
		for (Annotation root : allSentRoots) {
			Tree<String> tr = constructTreeFromAnnotationSet(root, parse);
			allTrees.add(tr);
		}
		return allTrees;
	}
	
	public static ArrayList<Integer> getChildrenIDs(String childIDs) {
		String[] ids = childIDs.split(",");
		ArrayList<Integer> children = new ArrayList<Integer>();
		for (int i = 0; i < ids.length; i++) {
			Integer id = Integer.parseInt(ids[i]);
			children.add(id.intValue());
		}
		return children;
	}
	
	public static Tree<String> constructTreeFromAnnotationSet(Annotation root, AnnotationSet parse) {
		Tree<String> rt = new Tree<String>(root.getType());
		if (isLeaf(root)) { // is leaf
			// do nothing
		} else {
			String chidStr = root.getAttribute(CHILD_IDS_ATTR);
			ArrayList<Integer> childIDs = getChildrenIDs(chidStr);
			ArrayList<Tree<String>> childrenTrees = new ArrayList<Tree<String>>();
			for (int i = 0; i < childIDs.size(); i++) {
				int child = childIDs.get(i).intValue();
				Annotation childAnno = parse.get(child);
				Tree<String> childTr = constructTreeFromAnnotationSet(childAnno, parse);
				childrenTrees.add(childTr);
			}
			rt.setChildren(childrenTrees);
		}
		return rt;
	}
	
	// output
	/*
	public static ArrayList<Tree<String>> outputAllTreeToAnnotationSet(Document doc, AnnotationSet parse) {
		ArrayList<Annotation> allSentRoots = getAllRoots(parse);
		ArrayList<Tree<String>> allTrees = new ArrayList<Tree<String>>();
		for (Annotation root : allSentRoots) {
			Tree<String> tr = constructTreeFromAnnotationSet(root, parse);
			allTrees.add(tr);
		}
		return allTrees;
	}
	
	public static void outputTreeToAnnotationSet(Tree<String> tr, AnnotationSet parse) {
		Tree<String> rt = new Tree<String>(root.getType());
		if (isLeaf(root)) { // is leaf
			// do nothing
		} else {
			String chidStr = root.getAttribute(CHILD_IDS_ATTR);
			ArrayList<Integer> childIDs = getChildrenIDs(chidStr);
			ArrayList<Tree<String>> childrenTrees = new ArrayList<Tree<String>>();
			for (int i = 0; i < childIDs.size(); i++) {
				int child = childIDs.get(i).intValue();
				Annotation childAnno = parse.get(child);
				Tree<String> childTr = constructTreeFromAnnotationSet(childAnno, parse);
				childrenTrees.add(childTr);
			}
			rt.setChildren(childrenTrees);
		}
		return rt;
	}
	*/
	
	
	
	
/*
	  def assembleConstTree(words: Seq[String], pos: Seq[String], parseBits: Seq[String]): Tree[String] = {
			    var finalTree: Tree[String] = null;
			    val stack = new ArrayBuffer[String];
			    // When a constituent is closed, the guy on top of the stack will become
			    // his parent. Build Trees as we go and register them with their parents so
			    // that when we close the parents, their children are already all there.
			    val childrenMap = new IdentityHashMap[String, ArrayBuffer[Tree[String]]];
			    for (i <- 0 until parseBits.size) {
			      require(parseBits(i).indexOf("*") != -1, parseBits(i) + " " + parseBits + "\n" + words);
			      val openBit = parseBits(i).substring(0, parseBits(i).indexOf("*"));
			      val closeBit = parseBits(i).substring(parseBits(i).indexOf("*") + 1);
			      // Add to the end of the stack
			      for (constituentType <- openBit.split("\\(").drop(1)) {
			        // Make a new String explicitly so the IdentityHashMap works
			        val constituentTypeStr = new String(constituentType);
			        stack += constituentTypeStr;
			        childrenMap.put(stack.last, new ArrayBuffer[Tree[String]]());
			      }
			      // Add the POS and word, which aren't specified in the parse bit but do need
			      // to be in the Tree object
			      val preterminalAndLeaf = new Tree[String](pos(i), IndexedSeq(new Tree[String](words(i))).asJava);
			      childrenMap.get(stack.last) += preterminalAndLeaf;
			      // Remove from the end of the stack
			      var latestSubtree: Tree[String] = null;
			      for (i <- 0 until closeBit.size) {
			        require(closeBit(i) == ')');
			        val constituentType = stack.last;
			        stack.remove(stack.size - 1);
			        latestSubtree = new Tree[String](constituentType, childrenMap.get(constituentType).asJava);
			        if (!stack.isEmpty) {
			          childrenMap.get(stack.last) += latestSubtree;
			        }
			      }
			      if (stack.isEmpty) {
			        finalTree = latestSubtree;
			      }
			    }
			    require(finalTree != null, stack);
			    // In Arabic, roots appear to be unlabeled sometimes, so fix this
			    if (finalTree.getLabel() == "") {
			      finalTree = new Tree[String]("ROOT", finalTree.getChildren);
			    }
			    finalTree;
			  }
*/
	
	public static Tree<String> assembleConstTree(List<String> words, List<String> pos, List<String> parseBits) {
		
		Tree<String> finalTree = null;
		ArrayList<String> stack = new ArrayList<String>();
		// When a constituent is closed, the guy on top of the stack will become
		// his parent. Build Trees as we go and register them with their parents so
		// that when we close the parents, their children are already all there.
		IdentityHashMap<String, ArrayList<Tree<String>>> childrenMap = new IdentityHashMap<String, ArrayList<Tree<String>>>();
		for (int i = 0; i < parseBits.size(); i++) {
			assert(parseBits.get(i).indexOf("*") != -1);//, parseBits(i) + " " + parseBits + "\n" + words);
			String openBit = parseBits.get(i).substring(0, parseBits.get(i).indexOf("*"));
			String closeBit = parseBits.get(i).substring(parseBits.get(i).indexOf("*") + 1);
			// Add to the end of the stack
			//for (constituentType <- openBit.split("\\(").drop(1)) {
			String[] openBitArr = openBit.split("\\(");
			for (int k = 1; k < openBitArr.length; k++) {
				String constituentType = openBitArr[k];
				// Make a new String explicitly so the IdentityHashMap works
				String constituentTypeStr = new String(constituentType);
				stack.add(constituentTypeStr);
				childrenMap.put(stack.get(stack.size() - 1), new ArrayList<Tree<String>>());
			}
			// Add the POS and word, which aren't specified in the parse bit but do need
			// to be in the Tree object
			List<Tree<String>> singleTreeList = new ArrayList<Tree<String>>();
			singleTreeList.add(new Tree<String>(words.get(i)));
			Tree<String> preterminalAndLeaf = new Tree<String>( pos.get(i),  singleTreeList);
			childrenMap.get(stack.get(stack.size() - 1)).add(preterminalAndLeaf);
			// Remove from the end of the stack
			Tree<String> latestSubtree = null;
			for (int j = 0; j < closeBit.length(); j++) {
				assert(closeBit.charAt(j) == ')');
				String constituentType = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
				latestSubtree = new Tree<String>(constituentType, childrenMap.get(constituentType));
				if (!(stack.size() == 0)) {
					childrenMap.get(stack.get(stack.size() - 1)).add(latestSubtree);
				}
			}
			if (stack.size() == 0) {
				finalTree = latestSubtree;
			}
		}
		assert(finalTree != null);
		// In Arabic, roots appear to be unlabeled sometimes, so fix this
		if (finalTree.getLabel().equals("")) {
			finalTree = new Tree<String>("ROOT", finalTree.getChildren());
		}
		return finalTree;
	}
	
/*
	def extractDependencyStructure(constTree: Tree[String], headFinder: HeadFinder): HashMap[Int, Int] = {
    // Type created by this method is an IdentityHashMap, which is correct
    // N.B. The constituent end index is the last word of the mention, it's not on fenceposts
    val constituents = constTree.getConstituents()
    val subtreeHeads = new IdentityHashMap[Tree[String],Int];
    val trees = constTree.getPostOrderTraversal().asScala;
    require(trees.last eq constTree);
    val heads = new HashMap[Int,Int]();
    for (tree <- trees) {
      if (tree.isLeaf) {
        // Do nothing
      } else if (tree.isPreTerminal) {
        val constituent = constituents.get(tree);
        require(!subtreeHeads.containsKey(tree));
        subtreeHeads.put(tree, constituent.getStart());
      } else {
        val children = tree.getChildren();
        val head = headFinder.determineHead(tree);
        if (head == null) {
          Logger.logss("WARNING: null head: " + PennTreeRenderer.render(constTree) + "\n" + PennTreeRenderer.render(tree));
        }
        val headIdx = subtreeHeads.get(head);
        for (child <- children.asScala) {
          if (child eq head) {
            subtreeHeads.put(tree, headIdx);
          } else {
            require(!heads.contains(subtreeHeads.get(child)), "\n" + PennTreeRenderer.render(constTree) +
                      "\n" + PennTreeRenderer.render(tree) +
                      "\n" + PennTreeRenderer.render(child) +
                      "\n" + heads);
            heads(subtreeHeads.get(child)) = headIdx;
          }
        }
      }
    }
    // Set the root head
    heads(subtreeHeads.get(constTree)) = -1;
    val numLeaves = constTree.getYield.size();
    for (i <- 0 until numLeaves) {
      require(heads.contains(i), heads + "\n" + PennTreeRenderer.render(constTree));
    }
    heads;
  }
*/
	/*
	public static HashMap<Integer, Integer> extractDependencyStructure(Tree<String> constTree, HeadFinder headFinder) {
		// Type created by this method is an IdentityHashMap, which is correct
		// N.B. The constituent end index is the last word of the mention, it's not on fenceposts
		Map<Tree<String>, Constituent<String>> constituents = constTree.getConstituents();
		IdentityHashMap<Tree<String>,Integer> subtreeHeads = new IdentityHashMap<Tree<String>,Integer>();
		List<Tree<String>> trees = constTree.getPostOrderTraversal();
		require(trees.last eq constTree);
		HashMap<Integer,Integer> heads = new HashMap<Integer,Integer>();
		for (tree <- trees) {
			if (tree.isLeaf) {
				// Do nothing
			} else if (tree.isPreTerminal) {
				val constituent = constituents.get(tree);
				require(!subtreeHeads.containsKey(tree));
				subtreeHeads.put(tree, constituent.getStart());
			} else {
				val children = tree.getChildren();
				val head = headFinder.determineHead(tree);
				if (head == null) {
					Logger.logss("WARNING: null head: " + PennTreeRenderer.render(constTree) + "\n" + PennTreeRenderer.render(tree));
				}
				val headIdx = subtreeHeads.get(head);
				for (child <- children.asScala) {
					if (child eq head) {
						subtreeHeads.put(tree, headIdx);
					} else {
						require(!heads.contains(subtreeHeads.get(child)), "\n" + PennTreeRenderer.render(constTree) +
								"\n" + PennTreeRenderer.render(tree) +
								"\n" + PennTreeRenderer.render(child) +
								"\n" + heads);
						heads(subtreeHeads.get(child)) = headIdx;
					}
				}
			}
		}
		// Set the root head
		heads(subtreeHeads.get(constTree)) = -1;
		val numLeaves = constTree.getYield.size();
		for (i <- 0 until numLeaves) {
			require(heads.contains(i), heads + "\n" + PennTreeRenderer.render(constTree));
		}
		return heads;
	}*/
	
}
