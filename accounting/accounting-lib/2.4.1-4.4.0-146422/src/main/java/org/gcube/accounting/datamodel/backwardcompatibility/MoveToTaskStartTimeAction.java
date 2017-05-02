/**
 * 
 */
package org.gcube.accounting.datamodel.backwardcompatibility;

import java.io.Serializable;

import org.gcube.accounting.datamodel.basetypes.AbstractTaskUsageRecord;
import org.gcube.documentstore.exception.InvalidValueException;
import org.gcube.documentstore.records.Record;
import org.gcube.documentstore.records.implementation.FieldAction;

/**
 * @author Luca Frosini (ISTI - CNR)
 *
 */
public class MoveToTaskStartTimeAction implements FieldAction {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable validate(String key,
			Serializable value, Record record)
			throws InvalidValueException {
		record.setResourceProperty(AbstractTaskUsageRecord.TASK_START_TIME, value);
		return null;  //Returning null the initial key is removed from Record
	}

}
