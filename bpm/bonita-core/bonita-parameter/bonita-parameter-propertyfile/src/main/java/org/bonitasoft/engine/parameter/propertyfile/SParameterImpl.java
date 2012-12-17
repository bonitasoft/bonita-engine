/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.parameter.propertyfile;

import org.bonitasoft.engine.parameter.SParameter;

/**
 * @author Matthieu Chaffotte
 */
public class SParameterImpl implements SParameter {

    private final String name;

    private final String value;

    public SParameterImpl(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

}
