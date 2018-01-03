package edu.oregonstate.nlp.coref.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.List;

import com.google.common.collect.Lists;

import gov.llnl.text.util.FileUtils;
import edu.oregonstate.nlp.coref.general.Constants;
import edu.oregonstate.nlp.coref.general.Utils;

public class DocumentFileDumper {

	protected File mDir;
	private File mFeatureFile;
	private File mFeatureDir;
	private File mAnnotationDir;

	public final static String RAW_TXT = "raw.txt";
	protected static boolean DEBUG = true;
	protected static final String FEATURE_FORMAT = "FEATURE_FORMAT";

	public static final AnnotationReaderBytespan AnReader = new AnnotationReaderBytespan();
	public static final AnnotationWriterBytespan AnWriter = new AnnotationWriterBytespan();
	
	public String getAbsolutePath()
	{
		return mDir.getAbsolutePath();
	}

	File getAnnotationDir()
	{
		if (mAnnotationDir == null) {
			mAnnotationDir = new File(mDir, Constants.ANNOT_DIR_NAME);
			if (!mAnnotationDir.exists()) {
				try {
					FileUtils.mkdir(mAnnotationDir);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return mAnnotationDir;
	}


	//////////////////////////// Create ///////////////////////////


	
	/**
	 * @param inputTextFile
	 * @throws IOException
	 */
	public void setRawText(File inputTextFile)
			throws IOException
	{
		try {
			FileUtils.write(getRawFile(), new FileInputStream(inputTextFile));
		}
		catch (RuntimeException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param articleNoTags
	 * @throws IOException
	 */
	public void setRawText(String text)
	{
		try {
			FileUtils.write(getRawFile(), text);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public OutputStream writeAnnotationDirFile(String filename)
	{
		try {
			File f = getAnnotationSetFile(filename);
			return new FileOutputStream(f);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeAnnotationDirFile(String filename, String content)
	{
		File f = getAnnotationSetFile(filename);
		writeFile(f, content);
	}

	public void writeAnnotationSet(AnnotationSet anSet, String annSetName)
	{
		try {
			//addAnnotationSet(anSet, anSet.getName(), true);
			File f = new File(getAnnotationDir(), annSetName);
			System.out.println("Writing Document.addAnnotationSet: " + f);
			PrintWriter out = new PrintWriter(f);
			AnWriter.write(anSet, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public OutputStream writeFeatureDirFile(String filename)
	{
		try {
			File f = new File(getFeatureDir(), filename);
			return new FileOutputStream(f);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeFeatureDirFile(String filename, String content)
	{
		File f = new File(getFeatureDir(), filename);
		writeFile(f, content);
	}

	public OutputStream writeFeatureFile()
	{
		try {
			File f = getFeatureFile();
			return new FileOutputStream(f);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeFeatureFile(String content)
	{
		File f = getFeatureFile();
		writeFile(f, content);
	}

	public void writeFile(File file, String content)
	{
		try {
			FileUtils.write(file, content);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public OutputStream writeFile(String name)
	{
		try {
			File f = new File(getRootDir(), name);
			return new FileOutputStream(f);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeFile(String name, String content)
	{
		File f = new File(getRootDir(), name);
		writeFile(f, content);
	}




	public static String getTextFromFile(String dirName)
	{
		// Find the raw.txt file in the directory
		String rawTextFileName = dirName + Utils.SEPARATOR + RAW_TXT;
		return Utils.getTextFromFile(rawTextFileName);
	}


	//////////////////////////// Load ///////////////////////////

	public InputStream readAnnotationDirFile(String filename)
	{
		try {
			File f = getAnnotationSetFile(filename);
			return new FileInputStream(f);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public InputStream readFeatureDirFile(String filename)
	{
		try {
			File f = new File(getFeatureDir(), filename);
			if (!f.exists()) return null;
			return new FileInputStream(f);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public InputStream readFeatureFile()
	{
		try {
			File f = getFeatureFile();
			if (!f.exists()) return null;
			return new FileInputStream(f);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public InputStream readFile(String name)
	{
		try {
			File f = new File(getRootDir(), name);
			if (!f.exists()) return null;
			return new FileInputStream(f);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * @return
	 */
	File getFeatureDir()
	{
		if (mFeatureDir == null) {
			String featSetName = "features";
			mFeatureDir = new File(mDir, Constants.FEAT_DIR_NAME + "." + featSetName);
			if (!mFeatureDir.exists()) {
				try {
					FileUtils.mkdir(mFeatureDir);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return mFeatureDir;
	}

	public File getFeatureFile()
	{
		if (mFeatureFile == null) {
			File dir = getFeatureDir();
			String featureFormat = "arff";//cfg.getString(FEATURE_FORMAT, "arff");

			mFeatureFile = new File(dir, Constants.FEAT_FILE_NAME + "." + featureFormat);
		}
		return mFeatureFile;
	}
	public Reader getFeatureReader(){
		try {
			return new java.io.FileReader(getFeatureFile());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * @return
	 */
	public File getRawFile()
	{
		File f = new File(mDir, RAW_TXT);
		return f;
	}

	/**
	 * 
	 * @return the directory that encapsulates all of the information for this particular document
	 */


	/**
	 * 
	 * @return the directory that encapsulates all of the information for this particular document
	 */
	public File getRootDir()
	{
		return mDir;
	}


	public String loadText()
	{
		String mText = null;
		try {
			mText = FileUtils.readFile(getRawFile());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (mText.length() == 0)
			throw new RuntimeException("raw.txt file is of length 0.  This will cause all sorts of problems. document dir: "
					+ getAbsolutePath());

		return mText;
	}

	/**
	 * Returns the file path for an annotation set. If it is a keyed annotation set from the config file (e.g. one of the
	 * X_ANNOTATION constants in this class), the name may be switched by different configuration options. Else, the given
	 * name is used.
	 * 
	 * @param annotationSetName
	 * @return File
	 * @throws IOException
	 */
	public File getAnnotationSetFile(String annotationSetName)
	{
		String annSetName = Document.getCannonicalAnnotationSetName(annotationSetName);
		File annFile = new File(getAnnotationDir(), annSetName);

		return annFile;
	}
	///////////////////////////// Delete ////////////////////////



	/**
	 * Delete all of the data in the prediction directory, feature directory, cluster directory, and annotation directory
	 * 
	 * @throws IOException
	 * 
	 */
	public int clean()
	{
		try {
			List<File> delList = Lists.newArrayList(getAnnotationDir(), getFeatureDir());
			mFeatureDir = null;
			mFeatureFile = null;
			mAnnotationDir = null;
			int count = 0;
			for (File f : delList) {
				try {
					count += Corpus.recursivelyDelete(f);
				}
				catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
			return count;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteAnnotation(String name)
			throws IOException
	{
		File f = getAnnotationDir();
		String canName = Document.getCannonicalAnnotationSetName(name);
		File df = new File(f, canName);
		try {
			FileUtils.delete(df);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * delete the cluster file associated with this document
	 * 
	 * @throws IOException
	 */

	public void deleteFeatureFile() throws IOException
	{
		File f = getFeatureFile();
		FileUtils.delete(f);
	}

	/////////////////////////////// other Utils /////////////////////////////////
/*
	public boolean existsAnnotationSetFile(String asName)
	{
		String name = getCannonicalAnnotationSetName(asName);
		File dir = getAnnotationSetFile(name);
		return dir.exists();
	}
*/
	public boolean existsFeatureFile()
	{
		File dir = getFeatureFile();
		return dir.exists();
	}

	private boolean existsFile(File f)
	{
		return f.exists();
	}

	public boolean existsFile(String name)
	{
		return existsFile(new File(getRootDir(), name));
	}


}
