/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.process;

import org.bonitasoft.engine.api.impl.transaction.process.DeleteProcess;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;

import com.bonitasoft.engine.parameter.ParameterService;
import com.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class DeleteProcessExt extends DeleteProcess {

    private final ParameterService parameterService;

    public DeleteProcessExt(final TenantServiceAccessor tenantAccessor, final long processDefinitionId) {
        super(tenantAccessor, processDefinitionId);
        parameterService = tenantAccessor.getParameterService();
    }

    @Override
    public void execute() throws SBonitaException {
        super.execute();
        try {
            parameterService.deleteAll(getProcessDefinitionId());
        } catch (final SParameterProcessNotFoundException sppnfe) {
            // ignore
        }
    }

}
