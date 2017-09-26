package model.cyverse;

import org.irods.jargon.core.connection.*;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;

public class CyVerseAOs
{
	private final IRODSAccessObjectFactory ao;
	private final IRODSAccount account;

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

	public CyVerseAOs(IRODSAccessObjectFactory ao, IRODSAccount account) throws JargonException
	{
		this.ao = ao;
		this.account = account;

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

	// Modifies permissions of collections, not sure what this does
	public CollectionAO getCollectionAO()
	{
		return collectionAO;
	}

	//
	public DataObjectAO getDataObjectAO()
	{
		return dataObjectAO;
	}

	public CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO()
	{
		return collectionAndDataObjectListAndSearchAO;
	}

	public RuleProcessingAO getRuleProcessingAO()
	{
		return ruleProcessingAO;
	}

	public DataTransferOperations getDataTransferOperations()
	{
		return dataTransferOperations;
	}

	public RemoteExecutionOfCommandsAO getRemoteExecutionOfCommandsAO()
	{
		return remoteExecutionOfCommandsAO;
	}

	public BulkFileOperationsAO getBulkFileOperationsAO()
	{
		return bulkFileOperationsAO;
	}

	public QuotaAO getQuotaAO()
	{
		return quotaAO;
	}

	public SimpleQueryExecutorAO getSimpleQueryExecutorAO()
	{
		return simpleQueryExecutorAO;
	}

	public Stream2StreamAO getStream2StreamAO()
	{
		return stream2StreamAO;
	}

	public JargonProperties getJargonProperties()
	{
		return jargonProperties;
	}

	public DataObjectAuditAO getDataObjectAuditAO()
	{
		return dataObjectAuditAO;
	}

	public CollectionAuditAO getCollectionAuditAO()
	{
		return collectionAuditAO;
	}

	public MountedCollectionAO getMountedCollectionAO()
	{
		return mountedCollectionAO;
	}

	public IRODSRegistrationOfFilesAO getRegistrationOfFilesAO()
	{
		return registrationOfFilesAO;
	}

	public TransferControlBlock getTransferControlBlock()
	{
		return transferControlBlock;
	}

	public ResourceGroupAO getResourceGroupAO()
	{
		return resourceGroupAO;
	}

	public SpecificQueryAO getSpecificQueryAO()
	{
		return specificQueryAO;
	}
}
