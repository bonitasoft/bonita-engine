/*******************************************************************************
 * Copyright (C) 2009, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.expression;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.impl.APIAccessorImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.impl.APIAccessorExt;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class EngineConstantExpressionExecutorStrategy extends org.bonitasoft.engine.expression.EngineConstantExpressionExecutorStrategy {

    public EngineConstantExpressionExecutorStrategy(final ActivityInstanceService activityInstanceService, final ProcessInstanceService processInstanceService,
            final SessionService sessionService, final SessionAccessor sessionAccessor) {
        super(activityInstanceService, processInstanceService, sessionService, sessionAccessor);
    }

    @Override
    protected APIAccessorImpl getApiAccessor() {
        return new APIAccessorExt();
    }

    @Override
    protected APIAccessor getConnectorApiAccessor() throws STenantIdNotSetException {
        final long tenantId = getSessionAccessor().getTenantId();
        return new ConnectorAPIAccessorExt(tenantId);
    }

}
