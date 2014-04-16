/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import com.bonitasoft.engine.page.SPageContent;
import com.bonitasoft.engine.page.SPageContentBuilder;

/**
 * @author Emmanuel Duchastenier
 */
public class SPageContentBuilderImpl implements SPageContentBuilder {

    private final SPageContentImpl pageContent;

    public SPageContentBuilderImpl(final SPageContentImpl pageContent) {
        super();
        this.pageContent = pageContent;
    }

    @Override
    public SPageContent done() {
        return pageContent;
    }
}
