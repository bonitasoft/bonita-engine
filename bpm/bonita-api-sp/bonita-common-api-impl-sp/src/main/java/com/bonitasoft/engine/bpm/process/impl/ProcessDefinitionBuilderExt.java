/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;

/**
 * Bonita BPM Subscription Edition specific builder for process definition.
 * It gives access to Subscription Edition specific features:
 * <ul>
 * <li>Parameters</li>
 * <li>StringIndexes</li>
 * <li>BusinessData</li>
 * </ul>
 *
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @version 6.3.5
 * @since 6.0.0
 */
public final class ProcessDefinitionBuilderExt extends ProcessDefinitionBuilder {

    @Override
    public ProcessDefinitionBuilderExt createNewInstance(final String name, final String version) {
        super.createNewInstance(name, version);
        return this;
    }

    /**
     * Add a parameter on this process.
     *
     * @param parameterName
     *        The name of the new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     * @param type
     *        The type of the new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition} (complete class name)
     * @return The {@link ParameterDefinitionBuilder} containing the new {@link org.bonitasoft.engine.bpm.parameter.ParameterDefinition}
     * @deprecated use {@link org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder#addParameter(String, String)}
     */
    public ParameterDefinitionBuilder addParameter(final String parameterName, final String type) {
        return new ParameterDefinitionBuilder(this, process, parameterName, type);
    }

    /**
     * Set the process string index at the given position.
     *
     * @param index
     *        The position to be set. Valid values are between 1 and 5 (inclusive)
     * @param label
     *        The label to be displayed for this position
     * @param initialValue
     *        The expression representing the initial index value
     */
    public void setStringIndex(final int index, final String label, final Expression initialValue) {
        process.setStringIndex(index, label, initialValue);
    }

}
