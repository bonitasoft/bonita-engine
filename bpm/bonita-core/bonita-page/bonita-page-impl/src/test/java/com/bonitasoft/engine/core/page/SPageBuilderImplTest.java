/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.page;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class SPageBuilderImplTest {


    @Test
    public void getSimpleReport() {
        final SPageBuilderFactory builderFact = BuilderFactory.get(SPageBuilderFactory.class);
        final String name = "report";
        final long installationDate = System.currentTimeMillis();
        final int installedBy = 10;
        SPageBuilder builder = builderFact.createNewInstance(name, installationDate, installedBy, false);
        final SPage report = builder.done();
        final SPageImpl expected = new SPageImpl(name, installationDate, installedBy, false);
        expected.setLastModificationDate(installationDate);
        Assert.assertEquals(expected, report);
    }

    @Test
    public void getComplexReport() {
        final SPageBuilderFactory builderFact = BuilderFactory.get(SPageBuilderFactory.class);
        final String name = "report";
        final long installationDate = System.currentTimeMillis();
        final int installedBy = 10;
        final String description = "description";
        SPageBuilder builder = builderFact.createNewInstance(name, installationDate, installedBy, true).setDescription(description);
        final SPage report = builder.done();
        final SPageImpl expected = new SPageImpl(name, installationDate, installedBy, true);
        expected.setDescription(description);
        expected.setLastModificationDate(installationDate);
        Assert.assertEquals(expected, report);
    }

}
