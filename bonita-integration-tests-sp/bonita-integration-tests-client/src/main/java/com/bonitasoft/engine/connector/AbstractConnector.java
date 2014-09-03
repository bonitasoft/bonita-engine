/*******************************************************************************
 * Copyright (C) 2022-2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.connector;

import com.bonitasoft.engine.api.APIAccessor;

/**
 * @author Feng Hui
 * @author Romain Bioteau
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class AbstractConnector extends org.bonitasoft.engine.connector.AbstractConnector {

    public void setAPIAccessor(final APIAccessor apiAccessor) {
        this.apiAccessor = apiAccessor;
    }

    @Override
    public APIAccessor getAPIAccessor() {
        return (APIAccessor) apiAccessor;
    }
}
