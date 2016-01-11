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
package org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.business.data;

import java.util.ArrayList;

import org.bonitasoft.engine.core.process.instance.model.archive.builder.business.data.SARefBusinessDataInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.business.data.SARefBusinessDataInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.business.data.SAFlowNodeSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.business.data.SAProcessMultiRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.business.data.SAProcessSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.business.data.SARefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.business.data.SFlowNodeSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SProcessSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;

/**
 * @author Emmanuel Duchastenier
 */
public class SARefBusinessDataInstanceBuilderFactoryImpl implements SARefBusinessDataInstanceBuilderFactory {

    @Override
    public SARefBusinessDataInstanceBuilder createNewInstance(SRefBusinessDataInstance sRefBusinessDataInstance) {
        if (sRefBusinessDataInstance instanceof SFlowNodeSimpleRefBusinessDataInstance) {
            return createNewInstanceForFlowNode((SFlowNodeSimpleRefBusinessDataInstance) sRefBusinessDataInstance);
        } else if (sRefBusinessDataInstance instanceof SProcessSimpleRefBusinessDataInstance) {
            return createNewInstance((SProcessSimpleRefBusinessDataInstance) sRefBusinessDataInstance);
        } else if (sRefBusinessDataInstance instanceof SProcessMultiRefBusinessDataInstance) {
            return createNewInstance((SProcessMultiRefBusinessDataInstance) sRefBusinessDataInstance);
        } else
            return null;
    }

    @Override
    public SARefBusinessDataInstanceBuilder createNewInstance(SProcessSimpleRefBusinessDataInstance businessDataInstance) {
        final SAProcessSimpleRefBusinessDataInstanceImpl entity = new SAProcessSimpleRefBusinessDataInstanceImpl();
        setCommonAttributes(businessDataInstance, entity);
        entity.setProcessInstanceId(businessDataInstance.getProcessInstanceId());
        entity.setDataId(businessDataInstance.getDataId());
        return new SARefBusinessDataInstanceBuilderImpl(entity);
    }

    @Override
    public SARefBusinessDataInstanceBuilder createNewInstance(SProcessMultiRefBusinessDataInstance businessDataInstance) {
        final SAProcessMultiRefBusinessDataInstanceImpl entity = new SAProcessMultiRefBusinessDataInstanceImpl();
        setCommonAttributes(businessDataInstance, entity);
        entity.setProcessInstanceId(businessDataInstance.getProcessInstanceId());
        final ArrayList<Long> dataIds = new ArrayList<>(businessDataInstance.getDataIds().size());
        for (Long dataId : businessDataInstance.getDataIds()) {
            dataIds.add(dataId);
        }
        entity.setDataIds(dataIds);
        return new SARefBusinessDataInstanceBuilderImpl(entity);
    }

    @Override
    public SARefBusinessDataInstanceBuilder createNewInstanceForFlowNode(SFlowNodeSimpleRefBusinessDataInstance businessDataInstance) {
        final SAFlowNodeSimpleRefBusinessDataInstanceImpl entity = new SAFlowNodeSimpleRefBusinessDataInstanceImpl();
        setCommonAttributes(businessDataInstance, entity);
        entity.setFlowNodeInstanceId(businessDataInstance.getFlowNodeInstanceId());
        entity.setDataId(businessDataInstance.getDataId());
        return new SARefBusinessDataInstanceBuilderImpl(entity);
    }

    protected void setCommonAttributes(SRefBusinessDataInstance businessDataInstance, SARefBusinessDataInstanceImpl entity) {
        entity.setName(businessDataInstance.getName());
        entity.setDataClassName(businessDataInstance.getDataClassName());
    }

}
