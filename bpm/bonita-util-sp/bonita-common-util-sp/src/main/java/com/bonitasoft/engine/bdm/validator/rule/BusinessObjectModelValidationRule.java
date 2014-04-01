/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 *
 */
package com.bonitasoft.engine.bdm.validator.rule;

import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * 
 * @author Romain Bioteau
 *
 */
public class BusinessObjectModelValidationRule implements ValidationRule {

	@Override
	public boolean appliesTo(Object modelElement) {
		return modelElement instanceof BusinessObjectModel;
	}

	@Override
	public ValidationStatus checkRule(Object modelElement) {
		if(!appliesTo(modelElement)){
			throw new IllegalArgumentException(BusinessObjectModelValidationRule.class.getName() +" doesn't handle validation for "+modelElement.getClass().getName());
		}
		BusinessObjectModel bom = (BusinessObjectModel) modelElement;
		ValidationStatus status = new ValidationStatus();
		if(bom.getBusinessObjects().isEmpty()){
			status.addError("Business object model must have at least one business object declared");
		}
		return status;
	}
}
