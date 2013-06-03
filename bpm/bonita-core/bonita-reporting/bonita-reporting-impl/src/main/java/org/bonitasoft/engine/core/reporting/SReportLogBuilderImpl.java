/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.reporting;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

/**
 * @author Matthieu Chaffotte
 */
public class SReportLogBuilderImpl extends CRUDELogBuilder implements SReportLogBuilder {

    private static final String PREFIX = "REPORT";

    public static final int REPORT_INDEX = 1;

    public static final String REPORT_INDEX_NAME = "numericIndex2";

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        queriableLogBuilder.numericIndex(REPORT_INDEX, objectId);
        return this;
    }

    @Override
    public String getObjectIdKey() {
        return REPORT_INDEX_NAME;
    }

    @Override
    protected String getActionTypePrefix() {
        return PREFIX;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL && log.getNumericIndex(REPORT_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatoryFildes are missing: report identifier");
        }
    }

}
