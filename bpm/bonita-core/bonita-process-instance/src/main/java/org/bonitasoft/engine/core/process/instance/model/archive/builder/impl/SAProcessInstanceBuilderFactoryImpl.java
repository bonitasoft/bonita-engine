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
package org.bonitasoft.engine.core.process.instance.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAProcessInstanceImpl;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SAProcessInstanceBuilderFactoryImpl implements SAProcessInstanceBuilderFactory {

    private static final String ARCHIVE_DATE_KEY = "archiveDate";

    private static final String ID_KEY = "id";

    private static final String NAME_KEY = "name";

    private static final String PROCESSDEF_ID_KEY = "processDefinitionId";

    private static final String STATE_ID_KEY = "stateId";

    private static final String SOURCE_OBJECT_ID_KEY = "sourceObjectId";

    private static final String CALLER_ID = "callerId";

    private static final String END_DATE_KEY = "endDate";

    private static final String STARTED_BY_KEY = "startedBy";

    private static final String STARTED_BY_SUBSTITUTE_KEY = "startedBySubstitute";

    private static final String START_DATE_KEY = "startDate";

    private static final String LAST_UPDATE_KEY = "lastUpdate";

    @Override
    public SAProcessInstanceBuilder createNewInstance(final SProcessInstance processInstance) {
        final SAProcessInstanceImpl entity = new SAProcessInstanceImpl(processInstance);
        return new SAProcessInstanceBuilderImpl(entity);
    }

    @Override
    public String getArchiveDateKey() {
        return ARCHIVE_DATE_KEY;
    }

    @Override
    public String getProcessDefinitionIdKey() {
        return PROCESSDEF_ID_KEY;
    }

    @Override
    public String getIdKey() {
        return ID_KEY;
    }

    @Override
    public String getSourceObjectIdKey() {
        return SOURCE_OBJECT_ID_KEY;
    }

    @Override
    public String getEndDateKey() {
        return END_DATE_KEY;
    }

    @Override
    public String getStartDateKey() {
        return START_DATE_KEY;
    }

    @Override
    public String getLastUpdateKey() {
        return LAST_UPDATE_KEY;
    }

    @Override
    public String getStartedByKey() {
        return STARTED_BY_KEY;
    }

    @Override
    public String getStartedBySubstituteKey() {
        return STARTED_BY_SUBSTITUTE_KEY;
    }

    @Override
    public String getStateIdKey() {
        return STATE_ID_KEY;
    }

    @Override
    public String getNameKey() {
        return NAME_KEY;
    }

    @Override
    public String getCallerIdKey() {
        return CALLER_ID;
    }

}
