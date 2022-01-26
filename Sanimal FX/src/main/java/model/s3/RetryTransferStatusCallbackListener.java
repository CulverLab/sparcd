package model.s3;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that keeps track of files that failed uploading
 * See TransferStatusCallbackListener documentation
 */
public class RetryTransferStatusCallbackListener {
	/**
	 * Keep track of files that failed uploading
	 */
	private List<String> failedFiles = new ArrayList<String>();
	
	/**
	 * Constructor without reference to existing TransferStatusCallbackListener
	 */
	public RetryTransferStatusCallbackListener()
	{
	}
	
	/**
	 * Adds a path to the failed files list
	 *
	 * @param filePath the path to add to the failed list
	 */
	public synchronized void addFailedFile(String filePath)
	{
		failedFiles.add(filePath);
	}
	
	/**
	 * Returns whether or not there were failed files detected
	 */
	public synchronized boolean hasFailedFiles()
	{
		return failedFiles.size() != 0;
	}
	
	/**
	 * Returns the count of any detected failed files
	 */
	public synchronized Integer getFailedFilesCount()
	{
		return failedFiles.size();
	}
	
	/**
	 * Returns the list of any detected failed files
	 */
	public synchronized List<String> getFailedFiles()
	{
		return failedFiles;
	}
	
	/**
 	 * Resets the list of failed files
 	 */
 	public synchronized void resetFailedFiles()
 	{
 		failedFiles = new ArrayList<String>();
 	}
}

