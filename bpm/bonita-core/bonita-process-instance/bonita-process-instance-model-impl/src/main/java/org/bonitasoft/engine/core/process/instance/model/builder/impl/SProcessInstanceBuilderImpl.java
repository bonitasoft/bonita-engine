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

import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProcessInstanceBuilderImpl implements SProcessInstanceBuilder {

    private static final String ID_KEY = "id";

    private static final String NAME_KEY = "name";

    private static final String PROCESSDEF_ID_KEY = "processDefinitionId";

    private static final String STATE_ID_KEY = "stateId";

    private static final String STATE_CATEGORY_KEY = "stateCategory";

    private static final String CONTAINER_ID_KEY = "containerId";

    private static final String MIGRATION_PLAN_ID = "migrationPlanId";

    private static final String END_DATE_KEY = "endDate";

    private static final String STARTED_BY_KEY = "startedBy";

    private static final String STARTED_BY_DELEGATE_KEY = "startedByDelegate";

    private static final String START_DATE_KEY = "startDate";

    private static final String CALLER_ID = "callerId";

    static final String LAST_UPDATE_KEY = "lastUpdate";

    static final String INTERRUPTING_EVENT_ID_KEY = "interruptingEventId";

    protected SProcessInstanceImpl entity;

    @Override
    public SProcessInstanceBuilder createNewInstance(final String name, final long processDefinitionId) {
        NullCheckingUtil.checkArgsNotNull(name, processDefinitionId);
        entity = new SProcessInstanceImpl(name, processDefinitionId);
        entity.setStateCategory(SStateCategory.NORMAL);
        return this;
    }

    @Override
    public SProcessInstanceBuilder createNewInstance(final String name, final long processDefinitionId, final String description) {
        NullCheckingUtil.checkArgsNotNull(name, processDefinitionId);
        entity = new SProcessInstanceImpl(name, processDefinitionId);
        entity.setStateCategory(SStateCategory.NORMAL);
        entity.setDescription(description);
        return this;
    }

    @Override
    public SProcessInstanceBuilder createNewInstance(final SProcessDefinition definition) {
        return createNewInstance(definition.getName(), definition.getId(), definition.getDescription());
    }

    @Override
    public SProcessInstance done() {
        return entity;
    }

    @Override
    public SProcessInstanceBuilder setName(final String name) {
        entity.setName(name);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setStartedBy(final long startedBy) {
        entity.setStartedBy(startedBy);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setStartedByDelegate(long startedByDelegate) {
        entity.setStartedByDelegate(startedByDelegate);
        return this;
    }

    @Deprecated
    @Override
    public SProcessInstanceBuilder setContainerId(final long id) {
        entity.setContainerId(id);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setCallerId(final long callerId, final SFlowNodeType callerType) {
        entity.setCallerId(callerId);
        entity.setCallerType(callerType);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setMigrationPlanId(final long migrationPlanId) {
        entity.setMigrationPlanId(migrationPlanId);
        return this;
    }

    @Override
    public SProcessInstanceBuilder setRootProcessInstanceId(final long rootProcessInstanceId) {
        entity.setRootProcessInstanceId(rootProcessInstanceId);
        return this;
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
    public String getProcessDefinitionIdKey() {
        return PROCESSDEF_ID_KEY;
    }

    @Override
    public String getStateIdKey() {
        return STATE_ID_KEY;
    }

    @Override
    public String getStateCategoryKey() {
        return STATE_CATEGORY_KEY;
    }

    @Override
    public String getStartDateKey() {
        return START_DATE_KEY;
    }

    @Override
    public String getStartedByKey() {
        return STARTED_BY_KEY;
    }

    @Override
    public String getStartedByDelegateKey() {
        return STARTED_BY_DELEGATE_KEY;
    }

    @Override
    public String getEndDateKey() {
        return END_DATE_KEY;
    }

    @Override
    public String getLastUpdateKey() {
        return LAST_UPDATE_KEY;
    }

    @Override
    public String getContainerIdKey() {
        return CONTAINER_ID_KEY;
    }

    @Override
    public String getInterruptingEventIdKey() {
        return INTERRUPTING_EVENT_ID_KEY;
    }

    @Override
    public String getMigrationPlanIdKey() {
        return MIGRATION_PLAN_ID;
    }

    @Override
    public String getCallerIdKey() {
        return CALLER_ID;
    }

}
