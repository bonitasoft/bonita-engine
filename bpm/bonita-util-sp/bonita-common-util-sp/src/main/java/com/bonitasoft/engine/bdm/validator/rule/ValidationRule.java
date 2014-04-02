/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 *
 */
package com.bonitasoft.engine.bdm.validator.rule;

import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * 
 * @author Romain Bioteau
 *
 */
public interface ValidationRule {

	boolean appliesTo(Object modelElement);
	
	ValidationStatus checkRule(Object modelElement);
	
}
