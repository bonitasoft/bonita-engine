/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.impl.SProcessInstanceBuilderFactoryImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;

import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilderFactory;

/**
 * @author Celine Souchet
 */
public class SProcessInstanceBuilderFactoryExt extends SProcessInstanceBuilderFactoryImpl implements SProcessInstanceBuilderFactory {

    static final String STRING_INDEX_1_KEY = "stringIndex1";

    static final String STRING_INDEX_2_KEY = "stringIndex2";

    static final String STRING_INDEX_3_KEY = "stringIndex3";

    static final String STRING_INDEX_4_KEY = "stringIndex4";

    static final String STRING_INDEX_5_KEY = "stringIndex5";

    @Override
    public SProcessInstanceBuilder createNewInstance(final String name, final long processDefinitionId) {
        NullCheckingUtil.checkArgsNotNull(name, processDefinitionId);
        final SProcessInstanceImpl entity = new SProcessInstanceImpl(name, processDefinitionId);
        entity.setStateCategory(SStateCategory.NORMAL);
        return new SProcessInstanceBuilderExt(entity);
    }

    @Override
    public SProcessInstanceBuilder createNewInstance(final String name, final long processDefinitionId, final String description) {
        NullCheckingUtil.checkArgsNotNull(name, processDefinitionId);
        final SProcessInstanceImpl entity = new SProcessInstanceImpl(name, processDefinitionId);
        entity.setStateCategory(SStateCategory.NORMAL);
        entity.setDescription(description);
        return new SProcessInstanceBuilderExt(entity);
    }

    @Override
    public SProcessInstanceBuilder createNewInstance(final SProcessDefinition definition) {
        return createNewInstance(definition.getName(), definition.getId(), definition.getDescription());
    }

    @Override
    public String getStringIndex1Key() {
        return STRING_INDEX_1_KEY;
    }

    @Override
    public String getStringIndex2Key() {
        return STRING_INDEX_2_KEY;
    }

    @Override
    public String getStringIndex3Key() {
        return STRING_INDEX_3_KEY;
    }

    @Override
    public String getStringIndex4Key() {
        return STRING_INDEX_4_KEY;
    }

    @Override
    public String getStringIndex5Key() {
        return STRING_INDEX_5_KEY;
    }
}
