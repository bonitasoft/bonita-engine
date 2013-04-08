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
package com.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

import com.bonitasoft.engine.core.process.instance.model.builder.SBreakpointLogBuilder;

/**
 * @author Baptiste Mesta
 */
public class SBreakpointLogBuilderImpl extends CRUDELogBuilder implements SBreakpointLogBuilder {

    private static final String BREAKPOINT = "BREAKPOINT";

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        queriableLogBuilder.numericIndex(1, objectId);
        return this;
    }

    @Override
    public String getObjectIdKey() {
        return "numericIndex2";
    }

    @Override
    protected String getActionTypePrefix() {
        return BREAKPOINT;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL) {
            if (log.getNumericIndex(0) == 0L) {
                throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "Flow Node Instance Id");
            }
        }
    }

    @Override
    public SBreakpointLogBuilder definitionId(final long definitionId) {
        queriableLogBuilder.numericIndex(0, definitionId);
        return this;
    }

    @Override
    public String getDefinitionIdKey() {
        return "numericIndex1";
    }

}
