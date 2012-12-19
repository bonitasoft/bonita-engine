/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl.transaction;

import java.util.List;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Bole Zhang
 */
public class GetLogsWithOrder implements TransactionContentWithResult<List<SQueriableLog>> {

    private final int numberPerPage;

    private final OrderByType orderContent;

    private final QueriableLoggerService loggerService;

    private final String fieldContent;

    private final int pageIndex;

    private List<SQueriableLog> sLogsList;

    public GetLogsWithOrder(final int numberPerPage, final OrderByType orderContent, final QueriableLoggerService loggerService, final String fieldContent,
            final int pageIndex) {
        this.numberPerPage = numberPerPage;
        this.orderContent = orderContent;
        this.loggerService = loggerService;
        this.fieldContent = fieldContent;
        this.pageIndex = pageIndex;
    }

    @Override
    public void execute() throws SBonitaException {
        sLogsList = loggerService.getLogs(pageIndex + 1, numberPerPage, fieldContent, orderContent);
    }

    @Override
    public List<SQueriableLog> getResult() {
        return sLogsList;
    }

}
