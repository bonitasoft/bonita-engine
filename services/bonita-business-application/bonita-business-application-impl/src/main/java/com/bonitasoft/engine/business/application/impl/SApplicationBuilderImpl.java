/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.business.application.SApplicationBuilder;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationBuilderImpl implements SApplicationBuilder {

    private final SApplicationImpl application;

    public SApplicationBuilderImpl(final SApplicationImpl application) {
        this.application = application;
    }

    @Override
    public SApplication done() {
        return application;
    }

    @Override
    public SApplicationBuilder setDescription(final String description) {
        application.setDescription(description);
        return this;
    }

    @Override
    public SApplicationBuilder setIconPath(final String iconPath) {
        application.setIconPath(iconPath);
        return this;
    }

}
