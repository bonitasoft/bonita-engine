/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.execution.transaction;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;

/**
 * Add or remove token on the flow node instance
 * 
 * @author Elias Ricken de Medeiros
 */
public class AddActivityInstanceTokenCount implements TransactionContentWithResult<Integer> {

    private final ActivityInstanceService activityInstanceService;

    private final long activityInstanceId;

    private final int tokenToAdd;

    private int tokenCount;

    public AddActivityInstanceTokenCount(final ActivityInstanceService activityInstanceService, final long activityInstanceId, final int tokenToAdd) {
        this.activityInstanceService = activityInstanceService;
        this.activityInstanceId = activityInstanceId;
        this.tokenToAdd = tokenToAdd;
    }

    @Override
    public void execute() throws SBonitaException {
        final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
        tokenCount = activityInstance.getTokenCount() + tokenToAdd;
        activityInstanceService.setTokenCount(activityInstance, tokenCount);
    }

    @Override
    public Integer getResult() {
        return tokenCount;
    }

}
