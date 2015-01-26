/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.parameter;

import org.bonitasoft.engine.exception.ExecutionException;

/**
 * @author Matthieu Chaffotte
 */
public class ImportParameterException extends ExecutionException {

    private static final long serialVersionUID = 7463213076180306458L;

    public ImportParameterException(Exception e) {
        super(e);
    }

    public ImportParameterException(String s) {
        super(s);
    }
}
