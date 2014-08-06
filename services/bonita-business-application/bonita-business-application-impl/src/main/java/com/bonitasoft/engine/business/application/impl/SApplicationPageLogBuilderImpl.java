/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

import com.bonitasoft.engine.business.application.SApplicationPageLogBuilder;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationPageLogBuilderImpl extends CRUDELogBuilder implements SApplicationPageLogBuilder {

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        queriableLogBuilder.numericIndex(SApplicationPageLogBuilderFactoryImpl.APPLICATION_PAGE_INDEX, objectId);
        return this;
    }

    @Override
    protected String getActionTypePrefix() {
        return "APPLICATION_PAGE";
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL
                && log.getNumericIndex(SApplicationPageLogBuilderFactoryImpl.APPLICATION_PAGE_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatoryFields are missing: application page identifier");
        }
    }

}
