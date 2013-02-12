/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
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
