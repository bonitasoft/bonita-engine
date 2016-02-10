/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.exception.ServerAPIException;

/**
 * @author Matthieu Chaffotte
 */
public class LocalServerAPIFactory {

    private static Class<?> forName = null;

    static {
        try {
            Class.forName("org.bonitasoft.engine.api.impl.ServerAPIFactory");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static ServerAPI getServerAPI() throws ServerAPIException {
        try {
            return (ServerAPI) forName.getMethod("getServerAPIImplementation").invoke(null);
        } catch (final Exception e) {
            throw new ServerAPIException(e);
        }
    }

    public static ServerAPI getServerAPI(final boolean cleanSession) throws ServerAPIException {
        try {
            return (ServerAPI) forName.getMethod("getServerAPIImplementation").invoke(cleanSession);
        } catch (final Exception e) {
            throw new ServerAPIException(e);
        }
    }

}
