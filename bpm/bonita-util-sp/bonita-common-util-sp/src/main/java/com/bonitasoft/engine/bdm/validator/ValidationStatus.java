/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 *
 */
package com.bonitasoft.engine.bdm.validator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Romain Bioteau
 *
 */
public class ValidationStatus {

	private List<String> errorList;

	public ValidationStatus(){
		errorList = new ArrayList<String>();
	}
	
	public void addError(String errorMessage) {
		if(errorMessage == null || errorMessage.isEmpty()){
			throw new IllegalArgumentException("errorMessage cannot be null or empty");
		}
		errorList.add(errorMessage);
	}
	
	public boolean isOk(){
		return errorList.isEmpty();
	}
	
	public void addValidationStatus(ValidationStatus status){
		errorList.addAll(status.getErrors());
	}

	public List<String> getErrors() {
		return errorList;
	}

}
