/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class SReportBuilderImplTest {

    @Test
    public void getNullReport() {
        final SReportBuilderImpl builder = new SReportBuilderImpl();
        Assert.assertNull(builder.done());
    }

    @Test
    public void getSimpleReport() {
        final SReportBuilderImpl builder = new SReportBuilderImpl();
        final String name = "report";
        final long installationDate = System.currentTimeMillis();
        final int installedBy = 10;
        builder.createNewInstance(name, installationDate, installedBy, false);
        final SReport report = builder.done();
        final SReportImpl expected = new SReportImpl(name, installationDate, installedBy, false);
        expected.setLastModificationDate(installationDate);
        Assert.assertEquals(expected, report);
    }

    @Test
    public void getComplexReport() {
        final SReportBuilderImpl builder = new SReportBuilderImpl();
        final String name = "report";
        final long installationDate = System.currentTimeMillis();
        final int installedBy = 10;
        final String description = "description";
        builder.createNewInstance(name, installationDate, installedBy, true).description(description);
        final SReport report = builder.done();
        final SReportImpl expected = new SReportImpl(name, installationDate, installedBy, true);
        expected.setDescription(description);
        expected.setLastModificationDate(installationDate);
        Assert.assertEquals(expected, report);
    }

}
