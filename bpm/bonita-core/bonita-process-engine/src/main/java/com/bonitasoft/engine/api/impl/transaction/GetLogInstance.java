/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Bole Zhang
 */
public class GetLogInstance implements TransactionContentWithResult<SQueriableLog> {

    private final long logId;

    private final QueriableLoggerService loggerService;

    private SQueriableLog sLog;

    public GetLogInstance(final long logId, final QueriableLoggerService loggerService) {
        this.logId = logId;
        this.loggerService = loggerService;
    }

    @Override
    public void execute() throws SBonitaException {
        sLog = loggerService.getLog(logId);
    }

    @Override
    public SQueriableLog getResult() {
        return sLog;
    }

}
