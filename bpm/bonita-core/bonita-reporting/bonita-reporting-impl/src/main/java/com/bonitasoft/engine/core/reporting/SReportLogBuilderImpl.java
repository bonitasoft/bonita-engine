/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

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
