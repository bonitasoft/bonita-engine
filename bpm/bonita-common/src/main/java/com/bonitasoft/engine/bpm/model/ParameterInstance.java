/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.bpm.model;

import org.bonitasoft.engine.bpm.model.DescriptionElement;

/**
 * @author Matthieu Chaffotte
 */
public interface ParameterInstance extends DescriptionElement {

    Object getValue();

    String getType();

}
