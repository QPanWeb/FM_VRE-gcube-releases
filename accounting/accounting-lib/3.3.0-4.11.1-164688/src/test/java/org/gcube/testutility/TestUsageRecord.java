/**
 * 
 */
package org.gcube.testutility;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.gcube.accounting.datamodel.UsageRecord.OperationResult;
import org.gcube.accounting.datamodel.basetypes.AbstractStorageStatusRecord;
import org.gcube.accounting.datamodel.basetypes.AbstractStorageUsageRecord;
import org.gcube.accounting.datamodel.usagerecords.JobUsageRecord;
import org.gcube.accounting.datamodel.usagerecords.PortletUsageRecord;
import org.gcube.accounting.datamodel.usagerecords.ServiceUsageRecord;
import org.gcube.accounting.datamodel.usagerecords.StorageStatusRecord;
import org.gcube.accounting.datamodel.usagerecords.StorageUsageRecord;
import org.gcube.accounting.persistence.AccountingPersistenceFactory;
import org.gcube.documentstore.exception.InvalidValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luca Frosini (ISTI - CNR)
 *
 */
public class TestUsageRecord {

	private static final Logger logger = LoggerFactory.getLogger(TestUsageRecord.class);

	public final static String TEST_CONSUMER_ID = "name.surname";
	public final static String TEST_SCOPE = "/infrastructure/vo";
	public final static String TEST_SCOPE_2 = "/infrastructure/vo/vre";
	public final static OperationResult TEST_OPERATION_RESULT = OperationResult.SUCCESS;

	public final static String TEST_SERVICE_CLASS = "TestServiceClass";
	public final static String TEST_SERVICE_NAME = "TestServiceName";
	public final static String TEST_CALLED_METHOD = "TestCalledMethod";
	public final static String TEST_CALLER_QUALIFIER = "TestCallerQualifier";

	public final static String TEST_CALLER_HOST = "remotehost";
	public final static String TEST_HOST = "localhost";

	public final static String TEST_PROPERTY_NAME = "TestPropertyName";
	public final static String TEST_PROPERTY_VALUE = "TestPropertyValue";

	public final static String TEST_JOB_ID = UUID.randomUUID().toString();
	public final static String TEST_JOB_NAME = "TestJobName";

	public final static String TEST_PORTLET_ID = "TestPortlet";
	public final static String TEST_PORTLET_OPERATION_ID = "TestPortletOperationID";
	public final static String TEST_PORTLET_MESSAGE = "TestPortletMessage";

	private final static long MIN_DURATION = 60; // millisec
	private final static long MAX_DURATION = 1000; // millisec

	static {
		AccountingPersistenceFactory.initAccountingPackages();
	}

	/**
	 * Generate A Random long in a range between min and max.
	 * This function is internally used to set random duration.  
	 * @return the generated random long
	 */
	public static long generateRandomLong(long min, long max){
		return min + (int)(Math.random() * ((max - min) + 1));
	}

	/**
	 * Create a valid #ServiceUsageRecord with scope set automatically.
	 * @return the created #ServiceUsageRecord
	 */
	public static ServiceUsageRecord createTestServiceUsageRecord() {
		ServiceUsageRecord usageRecord = new ServiceUsageRecord();
		try {
			usageRecord.setConsumerId(TEST_CONSUMER_ID);
			usageRecord.setOperationResult(TEST_OPERATION_RESULT);
			usageRecord.setCallerHost(TEST_CALLER_HOST);
			usageRecord.setHost(TEST_HOST);
			usageRecord.setCallerQualifier(TEST_CALLER_QUALIFIER);
			usageRecord.setServiceClass(TEST_SERVICE_CLASS);
			usageRecord.setServiceName(TEST_SERVICE_NAME);
			usageRecord.setCalledMethod(TEST_CALLED_METHOD);

			usageRecord.setDuration(generateRandomLong(MIN_DURATION, MAX_DURATION));

		} catch (InvalidValueException e) {
			logger.error(" ------ You SHOULD NOT SEE THIS MESSAGE. Error Creating a test Usage Record", e);
			throw new RuntimeException(e);
		}
		return usageRecord;

	}
	public final static String TEST_RESOUCE_OWNER = "resource.owner";
	public final static String TEST_RESOUCE_SCOPE = TEST_SCOPE;

	public final static String TEST_RESOURCE_URI = "testprotocol://objectURI";
	public final static String TEST_PROVIDER_URI = "testprotocol://providerURI";

	private final static long MIN_DATA_VOLUME = 1024;
	private final static long MAX_DATA_VOLUME = 10240;

	/**
	 * Create a valid #StorageUsageRecord with scope set automatically.
	 * @return the created #StorageUsageRecord
	 */
	public static StorageUsageRecord createTestStorageUsageRecord() {
		StorageUsageRecord usageRecord = new StorageUsageRecord();
		try {
			usageRecord.setConsumerId(TEST_CONSUMER_ID);
			usageRecord.setOperationResult(TEST_OPERATION_RESULT);

			usageRecord.setResourceOwner(TEST_RESOUCE_OWNER);
			usageRecord.setResourceScope(TEST_RESOUCE_SCOPE);

			usageRecord.setResourceURI(new URI(TEST_RESOURCE_URI));
			usageRecord.setProviderURI(new URI(TEST_PROVIDER_URI));

			usageRecord.setOperationType(AbstractStorageUsageRecord.OperationType.READ);
			usageRecord.setDataType(AbstractStorageUsageRecord.DataType.STORAGE);

			usageRecord.setDataVolume(generateRandomLong(MIN_DATA_VOLUME, MAX_DATA_VOLUME));

			usageRecord.setQualifier("image/png");


		} catch (InvalidValueException | URISyntaxException e) {
			logger.error(" ------ You SHOULD NOT SEE THIS MESSAGE. Error Creating a test Usage Record", e);
			throw new RuntimeException(e);
		}
		return usageRecord;

	}

	/**
	 * Create a valid #StorageVolumeUsageRecord with scope set automatically.
	 * @return the created #StorageVolumeUsageRecord
	 */
	public static StorageStatusRecord createTestStorageVolumeUsageRecord() {
		StorageStatusRecord usageRecord = new StorageStatusRecord();
		try {
			usageRecord.setConsumerId(TEST_CONSUMER_ID);
			usageRecord.setOperationResult(TEST_OPERATION_RESULT);
			usageRecord.setDataVolume(generateRandomLong(MIN_DATA_VOLUME, MAX_DATA_VOLUME));
			usageRecord.setDataType(AbstractStorageStatusRecord.DataType.STORAGE);
			usageRecord.setDataCount(generateRandomLong(MIN_DATA_VOLUME, MAX_DATA_VOLUME));
			usageRecord.setDataServiceClass("dataServiceClass");
			usageRecord.setDataServiceName("dataServiceName");
			usageRecord.setDataServiceId("dataServiceId");
			usageRecord.setProviderId(new URI(TEST_PROVIDER_URI));
			
			
			
		} catch (InvalidValueException | URISyntaxException e) {
			logger.error(" ------ You SHOULD NOT SEE THIS MESSAGE. Error Creating a test Usage Record", e);
			throw new RuntimeException(e);
		}
		return usageRecord;

	}

	/**
	 * @return
	 */
	public static JobUsageRecord createTestJobUsageRecord() {

		JobUsageRecord usageRecord = new JobUsageRecord();
		try {
			usageRecord.setConsumerId(TEST_CONSUMER_ID);
			usageRecord.setOperationResult(TEST_OPERATION_RESULT);
			usageRecord.setHost(TEST_HOST);
			usageRecord.setCallerQualifier(TEST_CALLER_QUALIFIER);
			usageRecord.setServiceClass(TEST_SERVICE_CLASS);
			usageRecord.setServiceName(TEST_SERVICE_NAME);
			usageRecord.setJobName(TEST_JOB_NAME);
			usageRecord.setDuration(generateRandomLong(MIN_DURATION, MAX_DURATION));

		} catch (InvalidValueException e) {
			logger.error(" ------ You SHOULD NOT SEE THIS MESSAGE. Error Creating a test Usage Record", e);
		}

		return usageRecord;
	}

	/**
	 * @return
	 */
	public static PortletUsageRecord createTestPortletUsageRecord() {

		PortletUsageRecord usageRecord = new PortletUsageRecord();
		try {
			usageRecord.setConsumerId(TEST_CONSUMER_ID);
			usageRecord.setOperationResult(TEST_OPERATION_RESULT);

			usageRecord.setPortletId(TEST_PORTLET_ID);
			usageRecord.setOperationId(TEST_PORTLET_OPERATION_ID);
			usageRecord.setMessage(TEST_PORTLET_MESSAGE);

		} catch (InvalidValueException e) {
			logger.error(" ------ You SHOULD NOT SEE THIS MESSAGE. Error Creating a test Usage Record", e);
		}

		return usageRecord;
	}




}
