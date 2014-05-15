/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public abstract class ValidationRule<T> {

    private Class<T> classToApply;

    public ValidationRule(Class<T> classToApply) {
        this.classToApply = classToApply;
    }
    
    public boolean appliesTo(Object modelElement) {
        return modelElement != null && classToApply.isAssignableFrom(modelElement.getClass());
    }

    protected abstract ValidationStatus validate(T modelElement);
    
    @SuppressWarnings("unchecked")
    public ValidationStatus checkRule(Object modelElement) {
        if (!appliesTo(modelElement)) {
            throw new IllegalArgumentException(this.getClass().getName() + " doesn't handle validation for " + modelElement.getClass().getName());
        }
        return validate((T) modelElement);
    }

}
