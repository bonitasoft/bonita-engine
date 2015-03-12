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
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;

/**
 * @author Lu Kai
 */
public class GetTenantsWithOrder implements TransactionContentWithResult<List<STenant>> {

    private final PlatformService platformService;

    private final int startIndex;

    private final OrderByType order;

    private final String field;

    private final int maxResults;

    private List<STenant> tenantList;

    public GetTenantsWithOrder(final PlatformService platformService, final int startIndex, final int maxResults, final OrderByType orderContent,
            final String fieldContent) {
        this.platformService = platformService;
        this.startIndex = startIndex;
        this.order = orderContent;
        this.field = fieldContent;
        this.maxResults = maxResults;
    }

    @Override
    public void execute() throws SBonitaException {
        final QueryOptions queryOptions;
        if (field == null) {
            queryOptions = new QueryOptions(startIndex, maxResults);
        } else {
            queryOptions = new QueryOptions(startIndex, maxResults, STenant.class, field, order);
        }
        tenantList = platformService.getTenants(queryOptions);
    }

    @Override
    public List<STenant> getResult() {
        return tenantList;
    }

}
