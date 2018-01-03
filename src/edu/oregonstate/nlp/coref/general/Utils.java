/**
 * 
 */
package edu.oregonstate.nlp.coref.general;

import gov.llnl.text.util.FileUtils;
import gov.llnl.text.util.InputStreamLineIterable;
import gov.llnl.text.util.LineIterable;
import gov.llnl.text.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;

import org.apache.commons.configuration.ConfigurationException;

import edu.oregonstate.nlp.coref.SystemConfig;
import edu.oregonstate.nlp.coref.data.Annotation;

/**
 * @author ves General utility methods that are used for processing
 */
public class Utils {

	public static String SEPARATOR = System.getProperty("file.separator");
	//private static String DATA_DIR = null;
	//private static String SCRIPT_DIR = null;
	//private static String WORK_DIR = null;
//private static SystemConfig CONFIGURATION = null;

public static String getText(String dirName)
{

  /*
   * Find the raw.txt file in the same directory as the filename 
   */

  // we assume the raw text file will be called "raw.txt";
  String rawTextFileName = dirName + SEPARATOR + "raw.txt";

  return getTextFromFile(rawTextFileName);
}

public static String getTextFromFile(String fileName)
{
  return getTextFromFile(new File(fileName));
}

public static String getTextFromFile(File file)
{
  try {
    return getTextFromFile(new FileInputStream(file));
  }
  catch (FileNotFoundException e) {
    throw new RuntimeException(e);
  }
}

public static String getTextFromFile(InputStream file)
{

  /*
   * Note this
   * file assumes UNIX style line SEPARATORs. If the raw.txt has the
   * Windows style SEPARATORs \r\n those are replaced with \n since
   * offsets in manual summaries were computed using UNIX SEPARATORs
   */

  String text = "";

  try {
    text = FileUtils.readFile(file);
  }
  catch (FileNotFoundException e) {
    throw new RuntimeException(e);
  }
  catch (IOException e) {
    throw new RuntimeException(e);
  }
  return text;
}

public static String getAnnotText(Annotation a, String text)
{
  return getAnnotText(a.getStartOffset(), a.getEndOffset(), text);
}

public static String getAnnotTextClean(Annotation a, String text)
{
  return getAnnotTextClean(a.getStartOffset(), a.getEndOffset(), text);
}

public static String getAnnotText(int start, int end, String text)
{
  String result = null;

  try {
    result = text.substring(start, end);
  }
  catch (StringIndexOutOfBoundsException siobe) {
    int st = text.length() - 20;
    for (int i = st; i < text.length(); i++) {
      System.err.println(i + ":" + text.charAt(i));

    }
    throw new RuntimeException(siobe);
  }
  return result;
}

public static String getAnnotTextClean(int start, int end, String text)
{
  String result = getAnnotText(start, end, text);
  if (result == null) return null;
  result = result.trim().replaceAll("(\\s|\\n)+", " ");
  return result;
}

public static String getExtToolsDirectory()
{
  String dir = "";

  try {
    File dir1 = new File(".");
    dir = dir1.getCanonicalPath();

    return dir + SEPARATOR + "externalTools";
  }
  catch (IOException ioe) {
    throw new RuntimeException(ioe);
  }
  // return dir;
}


public static InputStream getStopwords()
{
  return Utils.class.getResourceAsStream("/common_words");
}

public static InputStream getFemaleNames()
{
  return Utils.class.getResourceAsStream("/person-female-names.txt");
}

public static InputStream getMaleNames()
{
  return Utils.class.getResourceAsStream("/person-male-names.txt");
}

public static InputStream getDemonymFile()
{
  return Utils.class.getResourceAsStream("/demonyms.txt");
}

public static InputStream getCapitalsFile()
{
  return Utils.class.getResourceAsStream("/capitals.txt");
}

public static TreeSet<String> readStringsSet(BufferedReader in)
{
  String line;
  TreeSet<String> res = new TreeSet<String>();
  try {
    while ((line = in.readLine()) != null) {
      line = line.trim();
      res.add(line.toLowerCase());
    }
  }
  catch (IOException ioe) {
    throw new RuntimeException(ioe);
  }

  return res;
}

public static HashMap<String,Integer> readStringsHashMap(InputStream infile)
{
  BufferedReader in = new BufferedReader(new InputStreamReader(infile));
  String line;
  HashMap<String,Integer> res = new HashMap<String, Integer>();
  try {
    Integer next = 0;
    while ((line = in.readLine()) != null) {
      if(!line.startsWith("#")){
        next++;
        line = line.trim();
        String[] words = line.split("\\t+\\s*");
        for(String w:words)
          res.put(w, next);
      }
    }
  }
  catch (IOException ioe) {
    throw new RuntimeException(ioe);
  } 
  return res;
}

public static HashMap<String,String> readStringCorrespondance(InputStream infile)
{
  BufferedReader in = new BufferedReader(new InputStreamReader(infile));
  String line;
  HashMap<String,String> res = new HashMap<String, String>();
  try {
    Integer next = 0;
    while ((line = in.readLine()) != null) {
      if(!line.startsWith("#")){
        next++;
        line = line.trim();
        String[] words = line.split("\\s+-\\s+");
        res.put(words[0], words[1]);
      }
    }

    //for(String k:res.keySet())
    //  System.out.println(k+"-->"+res.get(k));
  }catch (IOException ioe) {
    throw new RuntimeException(ioe);
  } 
  return res;
}

public static TreeSet<String> readStringsSet(InputStream in)
{
  return readStringsSet(new BufferedReader(new InputStreamReader(in)));
}

public static TreeSet<String> readStringsSet(File in)
{
  try {
    return readStringsSet(new BufferedReader(new FileReader(in)));
  }
  catch (IOException e) {
    throw new RuntimeException(e);
  }
}
public static String[] readStrings(BufferedReader in)
{
  String line;
  ArrayList<String> res = new ArrayList<String>();
  try {
    while ((line = in.readLine()) != null) {
      line = line.trim();
      res.add(line);
    }
  }
  catch (IOException ioe) {
    throw new RuntimeException(ioe);
  }

  return res.toArray(new String[0]);
}


public static boolean isSubset(String[] set1, String[] set2)
{
  if (set1 == null || set2 == null) return true;

  Set<String> s1 = new HashSet<String>();
  Set<String> s2 = new HashSet<String>();

  for (String s : set1) {
    s1.add(s.toLowerCase());
  }

  for (String s : set2) {
    s2.add(s.toLowerCase());
  }

  return s2.containsAll(s1);
}

public static boolean isAnySubset(String[] set1, String[] set2)
{

  if (set1 == null || set2 == null) return false;

  String[] smaller;
  TreeSet<String> bigger = new TreeSet<String>();
  if (set1.length <= set2.length) {
    smaller = set1;
    for (String s : set2) {
      bigger.add(s.toLowerCase());
    }
  }
  else {
    smaller = set2;
    for (String s : set1) {
      bigger.add(s.toLowerCase());
    }
  }

  for (String s : smaller) {
    if (!bigger.contains(s.toLowerCase())) return false;
  }

  return true;
}

public static String replace(String str, String pattern, String replace)
{

  int s = 0;
  int e = 0;
  StringBuffer newString = new StringBuffer();

  while ((e = str.indexOf(pattern, s)) >= 0) {
    newString.append(str.substring(s, e));
    newString.append(replace);
    s = e + pattern.length();
  }

  newString.append(str.substring(s));
  return newString.toString();
}

// Pretty print a property
public static String printProperty(Annotation annot)
{
  String result;
  result = Integer.toString(annot.getStartOffset()) + "," + Integer.toString(annot.getEndOffset());
  return result;
}

// Pretty print a property
public static String printProperty(String p)
{
  String result;
  String newP = p;
  result = newP.replaceAll("\n", " ");
  return result;
}

// Pretty print a property
public static String printProperty(String[] strArray)
{
  StringBuilder result = new StringBuilder("[");
  for (String s : strArray) {
    result.append(s).append(" ");
  }
  result.append("]");
  return result.toString();
}

// Pretty print a property
public static String printProperty(Object p)
{
  if (p instanceof Annotation) {
    return printProperty((Annotation)p);
  }
  else if (p instanceof String[]) {
    return printProperty((String[])p);
  }
  else if (p instanceof Synset[]) {
    return printProperty((Synset[]) p);
  }
  else { // default
    String result;
    if (p == null) {
      result = "nil";
    }
    else {
      result = p.toString().replaceAll("\n", " ");
    }
    return result;
  }
}

// Pretty print a property
public static String printProperty(Synset[] newP)
{
  StringBuilder result = new StringBuilder();
  for (Synset s : newP) {
    Word[] words = s.getWords();
    result.append("{");
    for (Word w : words) {
      result.append(w.getLemma()).append(" ");
    }
    result.append("}");
  }
  return result.toString();
}

/**
 * returns a list of complete pathnames to the directories containing files to be processed, e.g,
 */
public static List<String> getDirectoryList(String baseDir, String filelist)
    throws IOException
{
  ArrayList<String> fileNames = new ArrayList<String>();
  for (String file : LineIterable.iterateOverCommentedLines(new File(filelist))) {
    file = file.trim();
    fileNames.add(baseDir + SEPARATOR + file);
  }
  return fileNames;
}

/* 
 * Some utilities for file and directory manipulation
 */
public static void createDirectory(String dirName)
{
  boolean success = (new File(dirName)).mkdirs();
  if (!success) {
    System.err.println("Could not create directory " + dirName);
  }
}
/*
public static boolean isConfigured()
{
  return CONFIGURATION != null;
}
*/

public static SystemConfig getDefaultConfig()
{
  try {
    URL res = Utils.class.getResource("/default.config");
    SystemConfig defaultConfig = new SystemConfig(res.toURI().toString());
    return defaultConfig;
  }
  catch (ConfigurationException e) {
    e.printStackTrace();
    throw new RuntimeException("System is not configured");
  }
  catch (URISyntaxException e) {
    e.printStackTrace();
    throw new RuntimeException(e);
  }
}

public static boolean exists(String file){
	File f = new File(file);
	return f.exists();
}

public static String lowercaseIfNec(String file){
	return exists(file)?file:file.toLowerCase();
}
public static InputStream getResourceStream(Object caller, String resourceName)
{
  return getResourceStream(caller.getClass(), resourceName);
}

public static InputStream getResourceStream(String resourceName)
{
  return getResourceStream(Utils.class, resourceName);
}

public static InputStream getResourceStream(Class<?> c, String resourceName)
{
  return c.getResourceAsStream(resourceName);
}

public static Reader getResourceReader(Object caller, String resourceName)
{
  return getResourceReader(caller.getClass(), resourceName);
}

public static Reader getResourceReader(String resourceName)
{
  return getResourceReader(Utils.class, resourceName);
}

public static Reader getResourceReader(Class<?> c, String resourceName)
{
  return new BufferedReader(new InputStreamReader(c.getResourceAsStream(resourceName)));
}

public static ArrayList<String> runExternalCaptureOutput(String command)
    throws IOException, InterruptedException
{
  System.out.print("Running " + command + "...");
  Process p = Runtime.getRuntime().exec(command);
  ArrayList<String> lines = new ArrayList<String>();

  for (String line : InputStreamLineIterable.iterate(p.getInputStream())) {
    // System.out.println(line);
    lines.add(line);
  }

  p.waitFor();
  if (p.exitValue() != 0) {
    for (String line : InputStreamLineIterable.iterate(p.getErrorStream())) {
      System.err.println(line);
    }
    throw new RuntimeException("Couldn't run " + command);
  }

  System.out.println("Finished running...");
  return lines;
}

public static void runExternal(String command)
    throws IOException, InterruptedException
{
  runExternal(command, true);
}

public static void runExternal(String command, boolean output)
    throws IOException, InterruptedException
{
  runExternal(command, output, System.out);
}

public static void runExternal(String command, boolean output, PrintStream outBuffer)
    throws IOException, InterruptedException
{
  if (output) {
    System.out.print("Running " + command + "...");
  }
  Process p = Runtime.getRuntime().exec(command);

  for (String line : InputStreamLineIterable.iterate(p.getInputStream())) {
    if (output) {
      outBuffer.println(line);
    }
  }
  if (output) {
    outBuffer.flush();
  }
  p.waitFor();
  if (p.exitValue() != 0) {
    for (String line : InputStreamLineIterable.iterate(p.getErrorStream())) {
      System.err.println(line);
    }
    throw new RuntimeException("Couldn't run " + command);
  }
  if (output) {
    System.out.println("Finished running...");
  }
}

public static void runExternal(String command[], boolean output, PrintStream outBuffer)
    throws IOException, InterruptedException
{
  if (output) {
    System.out.print("Running " + StringUtil.toString(command) + "...");
  }
  Process p = Runtime.getRuntime().exec(command);

  for (String line : InputStreamLineIterable.iterate(p.getInputStream())) {
    if (output) {
      outBuffer.println(line);
    }
  }
  if (output) {
    outBuffer.flush();
  }
  p.waitFor();
  if (p.exitValue() != 0) {
    for (String line : InputStreamLineIterable.iterate(p.getErrorStream())) {
      System.err.println(line);
    }
    throw new RuntimeException("Couldn't run " + StringUtil.toString(command));
  }
  if (output) {
    System.out.println("Finished running...");
  }
}

public static void runExternal(String command, File dir)
    throws IOException, InterruptedException
{
  runExternal(command, dir, true);
}

public static void runExternal(String command, File dir, boolean output)
    throws IOException, InterruptedException
{
  if (output) {
    System.out.println("In directory " + dir.getCanonicalPath());
  }
  if (output) {
    System.out.print("Running " + command + "...");
  }
  Process p = Runtime.getRuntime().exec(command, null, dir);

  for (String line : InputStreamLineIterable.iterate(p.getInputStream())) {
    if (output) {
      System.out.println(line);
    }
  }

  for (String line : InputStreamLineIterable.iterate(p.getErrorStream())) {
    System.err.println(line);
  }
  int exit = p.waitFor();
  if (exit != 0) {
    System.err.println("EXIT VALUE=" + exit);

    throw new RuntimeException("Couldn't run " + command);
  }
  if (output) {
    System.out.println("Finished running...");
  }

}
}
