/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.system.CommandWithParameters;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public abstract class TenantCommand extends CommandWithParameters {

    @Override
    public final Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        if (serviceAccessor instanceof com.bonitasoft.engine.service.TenantServiceAccessor) {
            return execute(parameters, (com.bonitasoft.engine.service.TenantServiceAccessor) serviceAccessor);
        } else {
            throw new SCommandExecutionException("The tenant service accessor is not the SP one");
        }
    }

    public abstract Serializable execute(final Map<String, Serializable> parameters, final com.bonitasoft.engine.service.TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException;

}
