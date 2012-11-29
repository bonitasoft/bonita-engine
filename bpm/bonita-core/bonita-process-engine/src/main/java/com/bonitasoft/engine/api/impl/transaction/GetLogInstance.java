/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.businesslogger.model.SBusinessLog;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.services.BusinessLoggerService;

/**
 * @author Bole Zhang
 */
public class GetLogInstance implements TransactionContentWithResult<SBusinessLog> {

    private final long logId;

    private final BusinessLoggerService loggerService;

    private SBusinessLog sLog;

    public GetLogInstance(final long logId, final BusinessLoggerService loggerService) {
        this.logId = logId;
        this.loggerService = loggerService;
    }

    @Override
    public void execute() throws SBonitaException {
        sLog = loggerService.getLog(logId);
    }

    @Override
    public SBusinessLog getResult() {
        return sLog;
    }

}
