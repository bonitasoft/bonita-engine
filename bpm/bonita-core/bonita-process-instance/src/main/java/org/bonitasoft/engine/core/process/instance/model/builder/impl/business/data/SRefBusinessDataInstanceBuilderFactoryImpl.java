/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.core.process.instance.model.builder.impl.business.data;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.model.builder.business.data.SRefBusinessDataInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.business.data.SRefBusinessDataInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SFlowNodeSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessMultiRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessSimpleRefBusinessDataInstanceImpl;


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
