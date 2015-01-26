/*
 * *****************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 * ******************************************************************************
 */
package com.bonitasoft.engine.bpm.parameter.impl;

import com.bonitasoft.engine.bpm.parameter.ParameterInstance;

/**
 * @author Matthieu Chaffotte
 */
public class ParameterImpl extends org.bonitasoft.engine.bpm.parameter.impl.ParameterImpl implements ParameterInstance {


    public ParameterImpl(String name, String description, Object value, String type) {
        super(name, description, value, type);
    }
}
