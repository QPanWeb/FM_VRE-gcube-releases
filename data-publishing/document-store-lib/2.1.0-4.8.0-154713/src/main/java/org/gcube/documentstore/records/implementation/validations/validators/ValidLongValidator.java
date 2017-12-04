package org.gcube.documentstore.records.implementation.validations.validators;

import java.io.Serializable;

import org.gcube.documentstore.exception.InvalidValueException;
import org.gcube.documentstore.records.Record;
import org.gcube.documentstore.records.implementation.FieldAction;

/**
 * @author Luca Frosini (ISTI - CNR)
 */
public class ValidLongValidator implements FieldAction {

	private static final String ERROR = String.format("Not Instance of %s", Integer.class.getSimpleName());
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable validate(String key, Serializable value, Record record) throws InvalidValueException {
		if(value instanceof Long){
			return value;
		}
		
		try {
			Long longObj = Long.valueOf((String) value);
			if(longObj!=null){
				return longObj.longValue();
			}
		}catch (Exception e) {}
		
		try {
			Double doubleObj = Double.valueOf((String) value);
			if(doubleObj!=null){
				return doubleObj.longValue();
			}
		}catch (Exception e) {}
		
		throw new InvalidValueException(ERROR);
	}

}
