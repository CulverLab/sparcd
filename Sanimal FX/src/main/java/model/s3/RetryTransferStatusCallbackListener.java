package model.cyverse;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that keeps track of files that failed uploading
 * See TransferStatusCallbackListener documentation
 */
public class RetryTransferStatusCallbackListener implements TransferStatusCallbackListener {
	/**
	 * Keep track of files that failed uploading
	 */
	private List<String> failedFiles = new ArrayList<String>();
	
	/**
 	 * When specified, TransferStatusCallbackListener overrides are called
 	 */
	private TransferStatusCallbackListener transfer_cb = null;
	
	/**
	 * Constructor without reference to existing TransferStatusCallbackListener
	 */
	public RetryTransferStatusCallbackListener()
	{
	}
	
	/**
	 * Constructor with TransferStatusCallbackListener
	 *
	 * @param transfer_cb instance of transfer callback to call
	 */
	public RetryTransferStatusCallbackListener(final TransferStatusCallbackListener transfer_cb)
	{
		this.transfer_cb = transfer_cb;
	}

	/**
	 * Keeps track of files that fail upload.
	 * Defaults to continue when transfer_cb not specified (on instance construction); otherwise returns value from transfer_cb.statusCallback
	 *
	 * @param transferStatus the status of the current transfer
	 */
	@Override
	public FileStatusCallbackResponse statusCallback(TransferStatus transferStatus) throws JargonException
	{
		/*
		 * Check for failure
		 */
		if (transferStatus.getTransferState() == TransferState.FAILURE)
		{
			failedFiles.add(transferStatus.getSourceFileAbsolutePath());
		}
		 
		/*
		 * If we have a transfer callback instance, return its value
		 */
		if (this.transfer_cb != null)
		{
			return this.transfer_cb.statusCallback(transferStatus);
		}
		return FileStatusCallbackResponse.CONTINUE;
	}

	/**
	 * Overall transfer status callback
	 *
	 * @param transferStatus the status of the transfer
	 */
	@Override
	public void overallStatusCallback(TransferStatus transferStatus) throws JargonException
	{
		if (transferStatus.getTransferState() == TransferState.FAILURE)
		{
			failedFiles.add(transferStatus.getSourceFileAbsolutePath());
		}
		 
		if (this.transfer_cb != null)
		{
			this.transfer_cb.overallStatusCallback(transferStatus);
		}
	}

	/**
	 * Called for overriding an existing file on iRods.
	 * Defaults to never override when transfer_cb not specified (on instance construction); otherwise returns value from transfer_cb.transferAsksWhetherToForceOperation
	 *
	 * @param irodsAbsolutePath the existing path on iRods
	 * @param isCollection flag indicating that the path represents a collection
	 */
	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(String irodsAbsolutePath, boolean isCollection)
	{
		if (this.transfer_cb != null)
		{
			return this.transfer_cb.transferAsksWhetherToForceOperation(irodsAbsolutePath, isCollection);
		}
		return CallbackResponse.NO_FOR_ALL;
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

