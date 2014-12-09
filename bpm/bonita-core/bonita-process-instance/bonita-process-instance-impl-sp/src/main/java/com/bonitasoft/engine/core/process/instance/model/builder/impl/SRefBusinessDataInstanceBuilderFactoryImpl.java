/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import java.util.List;

import com.bonitasoft.engine.core.process.instance.model.builder.SRefBusinessDataInstanceBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.SRefBusinessDataInstanceBuilderFactory;
import com.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeSimpleRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.core.process.instance.model.impl.SProcessMultiRefBusinessDataInstanceImpl;
import com.bonitasoft.engine.core.process.instance.model.impl.SProcessSimpleRefBusinessDataInstanceImpl;

/**
 * @author Matthieu Chaffotte
 */
public class SRefBusinessDataInstanceBuilderFactoryImpl implements SRefBusinessDataInstanceBuilderFactory {

    @Override
    public SRefBusinessDataInstanceBuilder createNewInstance(final String name, final long processInstanceId, final Long dataId, final String dataClassName) {
        final SProcessSimpleRefBusinessDataInstanceImpl entity = new SProcessSimpleRefBusinessDataInstanceImpl();
        entity.setProcessInstanceId(processInstanceId);
        entity.setName(name);
        entity.setDataId(dataId);
        entity.setDataClassName(dataClassName);
        return new SRefBusinessDataInstanceBuilderImpl(entity);
    }

    @Override
    public SRefBusinessDataInstanceBuilder createNewInstanceForFlowNode(final String name, final long flowNodeInstanceId, final Long dataId,
            final String dataClassName) {
        final SFlowNodeSimpleRefBusinessDataInstanceImpl entity = new SFlowNodeSimpleRefBusinessDataInstanceImpl();
        entity.setFlowNodeInstanceId(flowNodeInstanceId);
        entity.setName(name);
        entity.setDataId(dataId);
        entity.setDataClassName(dataClassName);
        return new SRefBusinessDataInstanceBuilderImpl(entity);
    }

    @Override
    public SRefBusinessDataInstanceBuilder createNewInstance(final String name, final long processInstanceId, final List<Long> dataIds,
            final String dataClassName) {
        final SProcessMultiRefBusinessDataInstanceImpl entity = new SProcessMultiRefBusinessDataInstanceImpl();
        entity.setProcessInstanceId(processInstanceId);
        entity.setName(name);
        entity.setDataIds(dataIds);
        entity.setDataClassName(dataClassName);
        return new SRefBusinessDataInstanceBuilderImpl(entity);
    }

}
