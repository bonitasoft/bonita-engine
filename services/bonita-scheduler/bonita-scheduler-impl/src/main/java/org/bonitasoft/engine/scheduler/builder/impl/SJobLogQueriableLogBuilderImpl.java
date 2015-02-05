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
package org.bonitasoft.engine.scheduler.builder.impl;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;
import org.bonitasoft.engine.scheduler.builder.SJobLogQueriableLogBuilder;

/**
 * @author Celine Souchet
 */
public class SJobLogQueriableLogBuilderImpl extends CRUDELogBuilder implements SJobLogQueriableLogBuilder {

    private static final int JOB_LOG_INDEX = 1;

    private static final int JOB_INDEX = 0;

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        this.queriableLogBuilder.numericIndex(JOB_LOG_INDEX, objectId);
        return this;
    }

    @Override
    protected String getActionTypePrefix() {
        return "JOB_LOG";
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL && log.getNumericIndex(JOB_LOG_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "JobLog Id");
        }
        if (log.getNumericIndex(JOB_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "JobDescriptor Id");
        }
    }

    @Override
    public SJobLogQueriableLogBuilder jogDescriptorId(final long jobDescriptorId) {
        this.queriableLogBuilder.numericIndex(JOB_INDEX, jobDescriptorId);
        return this;
    }

}
