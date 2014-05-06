/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import com.bonitasoft.engine.page.SPageContentBuilder;
import com.bonitasoft.engine.page.SPageContentBuilderFactory;

/**
 * @author Emmanuel Duchastenier
 */
public class SPageContentBuilderFactoryImpl implements SPageContentBuilderFactory {

    @Override
    public SPageContentBuilder createNewInstance(final byte[] content) {
        final SPageContentImpl pageContent = new SPageContentImpl();
        pageContent.setContent(content);
        return new SPageContentBuilderImpl(pageContent);
    }

}
