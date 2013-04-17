/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;

/**
 * @author Celine Souchet
 */
public interface SProcessInstanceBuilder extends org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder {

    String getStringIndex1Key();

    String getStringIndex2Key();

    String getStringIndex3Key();

    String getStringIndex4Key();

    String getStringIndex5Key();

    SProcessInstanceBuilder setStringIndex(int index, String value);

    @Override
    SProcessInstanceBuilder createNewInstance(String name, long processDefinitionId);

    @Override
    SProcessInstanceBuilder createNewInstance(String name, long processDefinitionId, String description);

    @Override
    SProcessInstanceBuilder createNewInstance(SProcessDefinition definition);

}
