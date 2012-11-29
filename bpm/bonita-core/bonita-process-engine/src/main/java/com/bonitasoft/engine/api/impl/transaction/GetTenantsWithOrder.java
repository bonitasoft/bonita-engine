/*
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl.transaction;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;

/**
 * @author Lu Kai
 */
public class GetTenantsWithOrder implements TransactionContentWithResult<List<STenant>> {

    private final PlatformService platformService;

    private final int pageIndex;

    private final OrderByType orderContent;

    private final String fieldContent;

    private final int numberPerPage;

    private List<STenant> tenantList;

    public GetTenantsWithOrder(final PlatformService platformService, final int pageIndex, final OrderByType orderContent, final String fieldContent,
            final int numberPerPage) {
        this.platformService = platformService;
        this.pageIndex = pageIndex;
        this.orderContent = orderContent;
        this.fieldContent = fieldContent;
        this.numberPerPage = numberPerPage;
    }

    @Override
    public void execute() throws SBonitaException {
        tenantList = platformService.getTenants(pageIndex, numberPerPage, fieldContent, orderContent);
    }

    @Override
    public List<STenant> getResult() {
        return tenantList;
    }

}
