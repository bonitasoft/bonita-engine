/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
    protected String getActionTypePrefix() {
        return BREAKPOINT;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL && log.getNumericIndex(0) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "Flow Node Instance Id");
        }
    }

    @Override
    public SBreakpointLogBuilder definitionId(final long definitionId) {
        queriableLogBuilder.numericIndex(0, definitionId);
        return this;
    }

}
