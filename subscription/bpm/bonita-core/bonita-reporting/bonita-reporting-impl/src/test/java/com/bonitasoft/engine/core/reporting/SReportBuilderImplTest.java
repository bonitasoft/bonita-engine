/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class SReportBuilderImplTest {


    @Test
    public void getSimpleReport() {
        final SReportBuilderFactory builderFact = BuilderFactory.get(SReportBuilderFactory.class);
        final String name = "report";
        final long installationDate = System.currentTimeMillis();
        final int installedBy = 10;
        SReportBuilder builder = builderFact.createNewInstance(name, installationDate, installedBy, false);
        final SReport report = builder.done();
        final SReportImpl expected = new SReportImpl(name, installationDate, installedBy, false);
        expected.setLastModificationDate(installationDate);
        Assert.assertEquals(expected, report);
    }

    @Test
    public void getComplexReport() {
        final SReportBuilderFactory builderFact = BuilderFactory.get(SReportBuilderFactory.class);
        final String name = "report";
        final long installationDate = System.currentTimeMillis();
        final int installedBy = 10;
        final String description = "description";
        SReportBuilder builder = builderFact.createNewInstance(name, installationDate, installedBy, true).setDescription(description);
        final SReport report = builder.done();
        final SReportImpl expected = new SReportImpl(name, installationDate, installedBy, true);
        expected.setDescription(description);
        expected.setLastModificationDate(installationDate);
        Assert.assertEquals(expected, report);
    }

}
