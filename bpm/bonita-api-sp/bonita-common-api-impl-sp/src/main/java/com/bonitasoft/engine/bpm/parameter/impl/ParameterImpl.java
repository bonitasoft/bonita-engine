/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.parameter.impl;

import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

import com.bonitasoft.engine.bpm.parameter.ParameterInstance;

/**
 * @author Matthieu Chaffotte
 */
public class ParameterImpl extends NamedElementImpl implements ParameterInstance {

    private static final long serialVersionUID = 4096607590317516470L;

    private final String description;

    private final Object value;

    private final String type;

    public ParameterImpl(final String name, final String description, final Object value, final String type) {
        super(name);
        this.description = description;
        this.value = value;
        this.type = type;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getType() {
        return type;
    }

}
