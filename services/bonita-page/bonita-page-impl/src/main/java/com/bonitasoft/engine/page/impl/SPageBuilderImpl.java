/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.page.SPageBuilder;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class SPageBuilderImpl implements SPageBuilder {

    private final SPageImpl page;

    public SPageBuilderImpl(final SPageImpl page) {
        super();
        this.page = page;
    }

    @Override
    public SPageBuilder setDescription(final String description) {
        page.setDescription(description);
        return this;
    }

    @Override
    public SPage done() {
        return page;
    }

}
