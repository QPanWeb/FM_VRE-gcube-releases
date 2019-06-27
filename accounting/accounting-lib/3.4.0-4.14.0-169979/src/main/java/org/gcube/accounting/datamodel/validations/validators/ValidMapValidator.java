package org.gcube.accounting.datamodel.validations.validators;

import java.io.Serializable;
import java.util.Map;

import org.gcube.documentstore.exception.InvalidValueException;
import org.gcube.documentstore.records.Record;
import org.gcube.documentstore.records.implementation.FieldAction;

/**
 * @author Luca Frosini (ISTI - CNR)
 */
public class ValidMapValidator implements FieldAction {

	private static final String ERROR = String.format("This Map cannot be serilized properly");
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable validate(String key, Serializable value, Record record) throws InvalidValueException {
		
		try {
			if(value instanceof Map){
				
			}
			
		}catch(Exception e){
			throw new InvalidValueException(ERROR, e);
		}
		
		throw new InvalidValueException(ERROR);
	}
	

}
