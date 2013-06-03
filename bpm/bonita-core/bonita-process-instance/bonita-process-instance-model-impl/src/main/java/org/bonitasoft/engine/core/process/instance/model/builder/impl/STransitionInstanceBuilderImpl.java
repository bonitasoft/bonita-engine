/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.STransitionInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.impl.STransitionInstanceImpl;

/**
 * @author Zhao Na
 * @author Baptiste Mesta
 */
public class STransitionInstanceBuilderImpl extends SFlowElementInstanceBuilderImpl implements STransitionInstanceBuilder {

    private static final String ID_KEY = "id";

    private static final String NAME_KEY = "name";

    private static final String SOURCE_KEY = "source";

    private static final String TARGET_KEY = "target";

    private static final String TRANSITION_TYPE_KEY = "state";

    private STransitionInstanceImpl entity;

    public STransitionInstanceBuilderImpl() {
    }

    @Override
    public STransitionInstanceBuilder createNewInstance(final String name, final long rootProcessInstanceId, final long processDefinitionId,
            final long parentProcessInstanceId) {
        entity = new STransitionInstanceImpl();
        entity.setName(name);
        entity.setParentContainerId(parentProcessInstanceId);
        entity.setRootContainerId(rootProcessInstanceId);
        entity.setLogicalGroup(PROCESS_DEFINITION_INDEX, processDefinitionId);
        entity.setLogicalGroup(ROOT_PROCESS_INSTANCE_INDEX, rootProcessInstanceId);
        entity.setLogicalGroup(PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        entity.setTerminal(false);
        entity.setStable(false);
        entity.setStateCategory(SStateCategory.NORMAL);
        return this;
    }

    @Override
    public STransitionInstanceBuilder setRootContainerId(final long rootContainerId) {
        entity.setRootContainerId(rootContainerId);
        return this;
    }

    @Override
    public STransitionInstanceBuilder setParentContainerId(final long parentContainerId) {
        entity.setParentContainerId(parentContainerId);
        return this;
    }

    @Override
    public STransitionInstanceBuilder setSource(final long source) {
        entity.setSource(source);
        return this;
    }

    @Override
    public STransitionInstanceBuilder setProcessDefinitionId(final long processDefinitionId) {
        entity.setLogicalGroup(PROCESS_DEFINITION_INDEX, processDefinitionId);
        return this;
    }

    @Override
    public STransitionInstanceBuilder setRootProcessInstanceId(final long processInstanceId) {
        entity.setLogicalGroup(ROOT_PROCESS_INSTANCE_INDEX, processInstanceId);
        return this;
    }

    @Override
    public STransitionInstanceBuilder setParentProcessInstanceId(final long parentProcessInstanceId) {
        entity.setLogicalGroup(PARENT_PROCESS_INSTANCE_INDEX, parentProcessInstanceId);
        return this;
    }

    @Override
    public STransitionInstanceBuilder setTokenRefId(final Long tokenRefId) {
        entity.setTokenRefId(tokenRefId);
        return this;
    }

    @Override
    public STransitionInstance done() {
        return entity;
    }

    @Override
    public String getIdKey() {
        return ID_KEY;
    }

    @Override
    public String getNameKey() {
        return NAME_KEY;
    }

    @Override
    public String getTransitionTypeKey() {
        return TRANSITION_TYPE_KEY;
    }

    @Override
    public String getSourceKey() {
        return SOURCE_KEY;
    }

    @Override
    public String getTargetKey() {
        return TARGET_KEY;
    }
}
