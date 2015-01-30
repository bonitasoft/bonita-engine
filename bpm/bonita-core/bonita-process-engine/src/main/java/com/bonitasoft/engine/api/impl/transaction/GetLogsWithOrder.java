/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class GetLogsWithOrder implements TransactionContentWithResult<List<SQueriableLog>> {

    private final QueriableLoggerService loggerService;

    private final int startIndex;

    private final int maxResults;

    private final OrderByType orderContent;

    private final String fieldContent;

    private List<SQueriableLog> sLogsList;

    public GetLogsWithOrder(final QueriableLoggerService loggerService, final int startIndex, final int maxResults, final OrderByType orderContent,
            final String fieldContent) {
        this.maxResults = maxResults;
        this.orderContent = orderContent;
        this.loggerService = loggerService;
        this.fieldContent = fieldContent;
        this.startIndex = startIndex;
    }

    @Override
    public void execute() throws SBonitaException {
        sLogsList = loggerService.getLogs(startIndex, maxResults, fieldContent, orderContent);
    }

    @Override
    public List<SQueriableLog> getResult() {
        return sLogsList;
    }

}
