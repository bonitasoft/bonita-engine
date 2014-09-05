/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.core.process.instance.model.builder.SPendingActivityMappingLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

/**
 * @author Baptiste Mesta
 */
public class SPendingActivityMappingLogBuilderImpl extends CRUDELogBuilder implements SPendingActivityMappingLogBuilder {

    private static final String PENDING_ACTIVITY_MAPPING = "PENDING_ACTIVITY_MAPPING";

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        queriableLogBuilder.numericIndex(1, objectId);
        return this;
    }

    @Override
    public SPendingActivityMappingLogBuilder activityInstanceId(final long activityInstanceId) {
        queriableLogBuilder.numericIndex(0, activityInstanceId);
        return this;
    }

    @Override
    protected String getActionTypePrefix() {
        return PENDING_ACTIVITY_MAPPING;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL) {
            if (log.getNumericIndex(0) == 0L) {
                throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "Flow Node Instance Id");
            }
        }
    }

}
