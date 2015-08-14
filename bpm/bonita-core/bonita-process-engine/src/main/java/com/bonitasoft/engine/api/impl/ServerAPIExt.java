/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.ServerAPIImpl;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.service.APIAccessResolver;

import com.bonitasoft.engine.exception.LicenseErrorException;
import com.bonitasoft.manager.ManagerIllegalStateException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ServerAPIExt extends ServerAPIImpl {

    public ServerAPIExt() {
        super();
    }

    public ServerAPIExt(final boolean cleanSession) {
        super(cleanSession);
    }

    public ServerAPIExt(final boolean cleanSession, final APIAccessResolver accessResolver) {
        super(cleanSession, accessResolver);
    }

    @Override
    protected BonitaRuntimeException wrapThrowable(final Throwable cause) {
        if(cause instanceof ManagerIllegalStateException) {
            return new LicenseErrorException(cause.getMessage());
        }
        return super.wrapThrowable(cause);
    }
}
