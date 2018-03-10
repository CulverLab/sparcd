package model.cyverse;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.JargonProperties;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;

/**
 * Class used to singleton-ize the Access object factor class and avoids allocating extra memory
 */
public class CyVerseAOs
{
	// The access object factory object we want to optimize
	private final IRODSAccessObjectFactory ao;
	// The account that is logged in
	private final IRODSAccount account;

	// All access object references that we "singletonize"
	private final UserAO userAO;
	private final EnvironmentalInfoAO environmentalInfoAO;
	private final IRODSGenQueryExecutor genQueryExecutor;
	private final ZoneAO zoneAO;
	private final ResourceAO resourceAO;
	private final IRODSFileSystemAO fileSystemAO;
	private final IRODSFileFactory fileFactory;
	private final UserGroupAO userGroupAO;
	private final CollectionAO collectionAO;
	private final DataObjectAO dataObjectAO;
	private final CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO;
	private final RuleProcessingAO ruleProcessingAO;
	private final DataTransferOperations dataTransferOperations;
	private final RemoteExecutionOfCommandsAO remoteExecutionOfCommandsAO;
	private final BulkFileOperationsAO bulkFileOperationsAO;
	private final QuotaAO quotaAO;
	private final SimpleQueryExecutorAO simpleQueryExecutorAO;
	private final Stream2StreamAO stream2StreamAO;
	private final JargonProperties jargonProperties;
	private final DataObjectAuditAO dataObjectAuditAO;
	private final CollectionAuditAO collectionAuditAO;
	private final MountedCollectionAO mountedCollectionAO;
	private final IRODSRegistrationOfFilesAO registrationOfFilesAO;
	private final TransferControlBlock transferControlBlock;
	private final ResourceGroupAO resourceGroupAO;
	private final SpecificQueryAO specificQueryAO;

	/**
	 * The constructor takes an access object factory and the logged in account
	 *
	 * @param ao The access object factory
	 * @param account The logged in a account
	 * @throws JargonException This gets throw if anything goes wrong in creating the access objects
	 */
	public CyVerseAOs(IRODSAccessObjectFactory ao, IRODSAccount account) throws JargonException
	{
		this.ao = ao;
		this.account = account;

		// Initialize all the access objects
		this.userAO = ao.getUserAO(account);
		this.environmentalInfoAO = ao.getEnvironmentalInfoAO(account);
		this.genQueryExecutor = ao.getIRODSGenQueryExecutor(account);
		this.zoneAO = ao.getZoneAO(account);
		this.resourceAO = ao.getResourceAO(account);
		this.fileSystemAO = ao.getIRODSFileSystemAO(account);
		this.fileFactory = ao.getIRODSFileFactory(account);
		this.userGroupAO = ao.getUserGroupAO(account);
		this.collectionAO = ao.getCollectionAO(account);
		this.dataObjectAO = ao.getDataObjectAO(account);
		this.collectionAndDataObjectListAndSearchAO = ao.getCollectionAndDataObjectListAndSearchAO(account);
		this.ruleProcessingAO = ao.getRuleProcessingAO(account);
		this.dataTransferOperations = ao.getDataTransferOperations(account);
		this.remoteExecutionOfCommandsAO = ao.getRemoteExecutionOfCommandsAO(account);
		this.bulkFileOperationsAO = ao.getBulkFileOperationsAO(account);
		this.quotaAO = ao.getQuotaAO(account);
		this.simpleQueryExecutorAO = ao.getSimpleQueryExecutorAO(account);
		this.stream2StreamAO = ao.getStream2StreamAO(account);
		this.jargonProperties = ao.getJargonProperties();
		this.dataObjectAuditAO = ao.getDataObjectAuditAO(account);
		this.collectionAuditAO = ao.getCollectionAuditAO(account);
		this.mountedCollectionAO = ao.getMountedCollectionAO(account);
		this.registrationOfFilesAO = ao.getIRODSRegistrationOfFilesAO(account);
		this.transferControlBlock = ao.buildDefaultTransferControlBlockBasedOnJargonProperties();
		this.resourceGroupAO = ao.getResourceGroupAO(account);
		this.specificQueryAO = ao.getSpecificQueryAO(account);
	}

	// Provides access to authentication and other access objects
	public IRODSAccessObjectFactory getAO()
	{
		return ao;
	}

	public IRODSAccount getAccount()
	{
		return account;
	}

	// Used to get lists of users and find users by name
	public UserAO getUserAO()
	{
		return userAO;
	}

	// Lets you get information about the CyVerse servers
	public EnvironmentalInfoAO getEnvironmentalInfoAO()
	{
		return environmentalInfoAO;
	}

	// Wrapper around JDBS, executes database queries
	public IRODSGenQueryExecutor getGenQueryExecutor()
	{
		return genQueryExecutor;
	}

	// Gets lists of zones on the cyverse instance
	public ZoneAO getZoneAO()
	{
		return zoneAO;
	}

	// Allows you to get a list of resources, no idea what they do
	public ResourceAO getResourceAO()
	{
		return resourceAO;
	}

	// Allows for manipulation of the file system and access of file permissions
	public IRODSFileSystemAO getFileSystemAO()
	{
		return fileSystemAO;
	}

	// Allows for the creation of files on the file system
	public IRODSFileFactory getFileFactory()
	{
		return fileFactory;
	}

	// Allows for the retrieval of user groups, not sure what they do yet
	public UserGroupAO getUserGroupAO()
	{
		return userGroupAO;
	}

	// Modifies permissions of collections (aka folders)
	public CollectionAO getCollectionAO()
	{
		return collectionAO;
	}

	// Modifies permissions of data objects (aka files)
	public DataObjectAO getDataObjectAO()
	{
		return dataObjectAO;
	}

	// Performs queries of collections and data objects and returns permissions
	public CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO()
	{
		return collectionAndDataObjectListAndSearchAO;
	}

	// Not sure what this does
	public RuleProcessingAO getRuleProcessingAO()
	{
		return ruleProcessingAO;
	}

	// Used to send and receive files from CyVerse
	public DataTransferOperations getDataTransferOperations()
	{
		return dataTransferOperations;
	}

	// Used to execute commands on the remote server
	public RemoteExecutionOfCommandsAO getRemoteExecutionOfCommandsAO()
	{
		return remoteExecutionOfCommandsAO;
	}

	// Used to automatically tar files and upload them to irods for maximum upload performance
	public BulkFileOperationsAO getBulkFileOperationsAO()
	{
		return bulkFileOperationsAO;
	}

	// Used to get a user's quota, only available to admins though :/
	public QuotaAO getQuotaAO()
	{
		return quotaAO;
	}

	// Used to execute queries against the database, however this is mostly used by the other function calls
	public SimpleQueryExecutorAO getSimpleQueryExecutorAO()
	{
		return simpleQueryExecutorAO;
	}

	// Used to open streams on the remote server and read from files
	public Stream2StreamAO getStream2StreamAO()
	{
		return stream2StreamAO;
	}

	// The properties on the remote irods system
	public JargonProperties getJargonProperties()
	{
		return jargonProperties;
	}

	// No idea what this is for
	public DataObjectAuditAO getDataObjectAuditAO()
	{
		return dataObjectAuditAO;
	}

	// No idea what this is for
	public CollectionAuditAO getCollectionAuditAO()
	{
		return collectionAuditAO;
	}

	// This allows for mounting a data collection (folder) locally on the system
	public MountedCollectionAO getMountedCollectionAO()
	{
		return mountedCollectionAO;
	}

	// No idea what this is for
	public IRODSRegistrationOfFilesAO getRegistrationOfFilesAO()
	{
		return registrationOfFilesAO;
	}

	// This is used to block the transfer of files to or from the irods system
	public TransferControlBlock getTransferControlBlock()
	{
		return transferControlBlock;
	}

	// Not sure what this is for
	public ResourceGroupAO getResourceGroupAO()
	{
		return resourceGroupAO;
	}

	// Not sure what this is for
	public SpecificQueryAO getSpecificQueryAO()
	{
		return specificQueryAO;
	}
}
