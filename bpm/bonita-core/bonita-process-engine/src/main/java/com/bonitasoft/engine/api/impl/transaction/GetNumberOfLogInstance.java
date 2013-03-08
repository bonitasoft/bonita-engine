/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Bole Zhang
 */
public class GetNumberOfLogInstance implements TransactionContentWithResult<Integer> {

    private final QueriableLoggerService loggerService;

    private int number;

    private final String instanceName;

    public GetNumberOfLogInstance(final String instanceName, final QueriableLoggerService loggerService) {
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
