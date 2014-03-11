/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.junit.Assert;
import org.junit.Test;

import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.page.SPageBuilder;
import com.bonitasoft.engine.page.SPageBuilderFactory;

/**
 * @author Matthieu Chaffotte
 */
public class SPageBuilderImplTest {

    @Test
    public void getSimplePage() {
        final SPageBuilderFactory builderFact = BuilderFactory.get(SPageBuilderFactory.class);
        final String name = "page";
        final long installationDate = System.currentTimeMillis();
        final int installedBy = 10;
        SPageBuilder builder = builderFact.createNewInstance(name, installationDate, installedBy, false);
        final SPage page = builder.done();
        final SPageImpl expected = new SPageImpl(name, installationDate, installedBy, false);
        expected.setLastModificationDate(installationDate);
        Assert.assertEquals(expected, page);
    }

    @Test
    public void getComplexPage() {
        final SPageBuilderFactory builderFact = BuilderFactory.get(SPageBuilderFactory.class);
        final String name = "page";
        final long installationDate = System.currentTimeMillis();
        final int installedBy = 10;
        final String description = "description";
        SPageBuilder builder = builderFact.createNewInstance(name, installationDate, installedBy, true).setDescription(description);
        final SPage page = builder.done();
        final SPageImpl expected = new SPageImpl(name, installationDate, installedBy, true);
        expected.setDescription(description);
        expected.setLastModificationDate(installationDate);
        Assert.assertEquals(expected, page);
    }

}
