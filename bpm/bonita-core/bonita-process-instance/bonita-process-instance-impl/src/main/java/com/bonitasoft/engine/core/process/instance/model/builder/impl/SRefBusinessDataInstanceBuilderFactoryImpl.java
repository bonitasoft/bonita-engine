/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import com.bonitasoft.engine.core.process.instance.model.builder.SRefBusinessDataInstanceBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.SRefBusinessDataInstanceBuilderFactory;
import com.bonitasoft.engine.core.process.instance.model.impl.SRefBusinessDataInstanceImpl;

/**
 * @author Matthieu Chaffotte
 */
public class SRefBusinessDataInstanceBuilderFactoryImpl implements SRefBusinessDataInstanceBuilderFactory {

    @Override
    public SRefBusinessDataInstanceBuilder createNewInstance(final String name, final long processInstanceId, final Long dataId, final String dataClassName) {
        final SRefBusinessDataInstanceImpl entity = new SRefBusinessDataInstanceImpl();
        entity.setProcessInstanceId(processInstanceId);
        entity.setName(name);
        entity.setDataId(dataId);
        entity.setDataClassName(dataClassName);
        return new SRefBusinessDataInstanceBuilderImpl(entity);
    }

    @Override
    public String getNameKey() {
        return "name";
    }

    @Override
    public String getProcessInstanceIdKey() {
        return "processInstanceId";
    }

    @Override
    public String getDataIdKey() {
        return "dataId";
    }

    @Override
    public String getDataClassNameKey() {
        return "dataClassName";
    }

}
