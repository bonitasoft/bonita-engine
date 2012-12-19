/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
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
