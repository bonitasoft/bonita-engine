/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.exception;

import java.text.MessageFormat;

import org.bonitasoft.engine.exception.ObjectNotFoundException;

import com.bonitasoft.engine.bpm.model.ParameterInstance;

/**
 * @author Matthieu Chaffotte
 */
public class ParameterNotFoundException extends ObjectNotFoundException {

    private static final long serialVersionUID = -5548436489951596184L;

    public ParameterNotFoundException(final Throwable cause) {
        super(cause, ParameterInstance.class);
    }

    public ParameterNotFoundException(final long processDefinitionId, final String parameterName) {
        super(MessageFormat.format("the parameter with name {0} and process with id {1}", parameterName, processDefinitionId), ParameterInstance.class);
    }

}
