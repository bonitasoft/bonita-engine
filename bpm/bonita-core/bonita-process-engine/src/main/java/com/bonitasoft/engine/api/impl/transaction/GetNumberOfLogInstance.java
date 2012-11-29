/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.services.BusinessLoggerService;

/**
 * @author Bole Zhang
 */
public class GetNumberOfLogInstance implements TransactionContentWithResult<Integer> {

    private final BusinessLoggerService loggerService;

    private int number;

    private final String instanceName;

    public GetNumberOfLogInstance(final String instanceName, final BusinessLoggerService loggerService) {
        this.instanceName = instanceName;
        this.loggerService = loggerService;
    }

    @Override
    public void execute() throws SBonitaException {
        if (instanceName.equals("getNumberOfLogs")) {
            number = loggerService.getNumberOfLogs();
        }
    }

    @Override
    public Integer getResult() {
        return number;
    }

}
