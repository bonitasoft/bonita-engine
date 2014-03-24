/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 *
 */
package com.bonitasoft.engine.bdm.validator.rule;

import java.util.List;

import javax.lang.model.SourceVersion;

import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.UniqueConstraint;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * 
 * @author Romain Bioteau
 *
 */
public class BusinessObjectValidationRule implements ValidationRule {

	@Override
	public boolean appliesTo(Object modelElement) {
		return modelElement instanceof BusinessObject;
	}

	@Override
	public ValidationStatus checkRule(Object modelElement) {
		if(!appliesTo(modelElement)){
			throw new IllegalArgumentException(BusinessObjectValidationRule.class.getName() +" doesn't handle validation for "+modelElement.getClass().getName());
		}
		BusinessObject bo = (BusinessObject) modelElement;

		ValidationStatus status = new ValidationStatus();
		String qualifiedName = bo.getQualifiedName();
		if(qualifiedName == null){
			status.addError("A Business Object must have a qualified name");
			return status;
		}
		if (!SourceVersion.isName(qualifiedName)) {
			status.addError(qualifiedName + " is not a valid Java qualified name");
			return status;
		}
		if (bo.getFields().isEmpty()) {
			status.addError(qualifiedName + " must have at least one field declared");
		}
		if(bo.getUniqueConstraints() != null && !bo.getUniqueConstraints().isEmpty()){
			for(UniqueConstraint uc : bo.getUniqueConstraints()){
				for(String fName : uc.getFieldNames()){
					Field field = getField(bo, fName);
					if(field == null){
						status.addError("The field named " + fName + " does not exist in "+ bo.getQualifiedName());
					}
				}
			}
		}

		return status;
	}

	private Field getField(final BusinessObject bo , final String name) {
		Field found = null;
		int index = 0;
		List<Field> fields = bo.getFields();
		while (found == null && index < fields.size()) {
			final Field field = bo.getFields().get(index);
			if (field.getName().equals(name)) {
				found = field;
			}
			index++;
		}
		return found;
	}
}
