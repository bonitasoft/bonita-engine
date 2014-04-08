/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 *
 */
package com.bonitasoft.engine.bdm.validator.rule;

import javax.lang.model.SourceVersion;

import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.validator.SQLNameValidator;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * 
 * @author Romain Bioteau
 *
 */
public class FieldValidationRule implements ValidationRule {

	private static final int MAX_COLUMNAME_LENGTH = 50;
	private SQLNameValidator sqlNameValidator;

	public FieldValidationRule() {
		sqlNameValidator = new SQLNameValidator(MAX_COLUMNAME_LENGTH);
	}
	
	@Override
	public boolean appliesTo(Object modelElement) {
		return modelElement instanceof Field;
	}

	@Override
	public ValidationStatus checkRule(Object modelElement) {
		if(!appliesTo(modelElement)){
			throw new IllegalArgumentException(FieldValidationRule.class.getName() +" doesn't handle validation for "+modelElement.getClass().getName());
		}
		Field field = (Field) modelElement;
		ValidationStatus status = new ValidationStatus();
		String name = field.getName();
		if (name == null || !SourceVersion.isIdentifier(name) || SourceVersion.isKeyword(name) || isForbiddenIdentifier(name)) {
			status.addError(name + " is not a valid field identifier");
			return status;
		}
		if(field.getType() == null){
			status.addError(name + " must have a type declared");
		}
		return status;
	}

	private boolean isForbiddenIdentifier(final String name) {
		return Field.PERSISTENCE_ID.equalsIgnoreCase(name) || Field.PERSISTENCE_VERSION.equalsIgnoreCase(name) || !sqlNameValidator.isValid(name);
	}
}
