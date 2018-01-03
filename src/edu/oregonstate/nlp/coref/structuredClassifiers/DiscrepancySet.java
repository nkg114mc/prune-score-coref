package edu.oregonstate.nlp.coref.structuredClassifiers;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Chao Ma
 * 
 * @since 2013-2-7
 * 
 */

public class DiscrepancySet {
  
	HashMap<Integer, DiscrepancyItem> allItems;

  
  /** Default constructors */
  public DiscrepancySet()
  {
	  // allocate memory for the hashmap
	  allItems = new HashMap<Integer, DiscrepancyItem>();
  }
  /** Copy constructor */
  public DiscrepancySet(DiscrepancySet src)
  {
	  // allocate memory for the hashmap
	  allItems = new HashMap<Integer, DiscrepancyItem>();
	  copyFrom(src);
  }
  
  public void copyFrom(DiscrepancySet src)
  {
	  if (allItems == null) {
		  allItems = new HashMap<Integer, DiscrepancyItem>();
	  }
	  allItems.clear();

	  for (Integer keyID : src.allItems.keySet()) {
		  DiscrepancyItem item = new DiscrepancyItem(src.allItems.get(keyID));
		  this.allItems.put(keyID, item);
	  }
  }

  public HashMap<Integer, DiscrepancyItem> getAllDiscreItems()
  {
	  return allItems;
  }
  
  public HashSet<DiscrepancyItem> getWholeDiscreSet()
  {
	HashSet<DiscrepancyItem> allDisSet = (HashSet<DiscrepancyItem>)allItems.values();
	return allDisSet;
  }
  
  public int size()
  {
	  return allItems.size();
  }
  
  public void clear()
  {
	  allItems.clear(); // clear all hash entries
  }
  
  public void add(DiscrepancyItem item, boolean enableReplace)
  {
	  int hashcode = item.hashCode();
	  if (enableReplace) {
		  if (allItems.containsKey(hashcode)) {
			  DiscrepancyItem hashItem = allItems.get(hashcode);
			  hashItem.setValue(item.getValue()); // replace its value with current one
		  } else {
			  allItems.put(hashcode, item);
		  }
	  } else {
		  if (allItems.containsKey(hashcode)) {
			  System.out.println("Can not put this item into set, it has already existed!");
		  } else {
			  allItems.put(hashcode, item);
		  }
	  }
  }
  
  public DiscrepancyItem get(int hashcode)
  {
	  if (allItems.containsKey(hashcode)) {
		  return (allItems.get(hashcode));
	  } else {
		  return null; // might cause error ...
	  }
  }
  
  public void remove(DiscrepancyItem item)
  {
	  int hashcode = item.hashCode();
	  if (allItems.containsKey(hashcode)) {
		  allItems.remove(hashcode); // remove it!
	  }
  }
  
  // remove an old item, replace it with a new one
  public void replace(DiscrepancyItem oldItem, DiscrepancyItem newItem)
  {
	  remove(oldItem);     // remove old one 
	  add(newItem, false); // add new one
  }
  
  public boolean contains(DiscrepancyItem item)
  {
	  int hashcode = item.hashCode();
	  return (allItems.containsKey(hashcode));
  }

  // return a hashmap from firstId to discreItem
  public HashMap<Integer, DiscrepancyItem> mapByFirstID()
  {
		HashSet<Integer> assignedDisMentions = new HashSet<Integer>();
		HashMap<Integer, DiscrepancyItem> disItemMap = this.getAllDiscreItems();
		HashMap<Integer, DiscrepancyItem> firstIDItemMap = new HashMap<Integer, DiscrepancyItem>();
		for (DiscrepancyItem item : disItemMap.values()) {
			Integer operMID = item.firstMenID;
			assignedDisMentions.add(operMID);
			firstIDItemMap.put(operMID, item);
		}
		return firstIDItemMap;
  }
  
  public String toString()   
  {
	  String str, all;
	  all = new String("");
	  all += new String("==== Total number of discrepancy: "+allItems.size()+"====\n");
	  all += new String("-- True number (without dual edges): "+(allItems.size()/2)+" --\n");
	  for (DiscrepancyItem item : allItems.values()) {
		  str = new String("edge("+item.firstMenID+" --> "+item.secondMenID+") = "+item.constraintVal+"\n");
		  all += str;
	  }
	  all += new String("=========================================================\n");
      return all;
  }
  
  public void printFirstIDMap()
  {
	  HashMap<Integer, DiscrepancyItem> firstIDItemMap = this.mapByFirstID();
	  System.out.println("== Discrepancy FirstID Map ====================================");
	  for (Integer firstID : firstIDItemMap.keySet()) {
		  DiscrepancyItem item = firstIDItemMap.get(firstID);
		  System.out.print("Mention ID " + firstID + " discre: ");
		  System.out.println(item.toString());
	  }
	  System.out.println("== End of Discrepancy FirstID Map =============================");
  }
}