/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class SReportBuilderImpl implements SReportBuilder {

    private final SReportImpl report;

    public SReportBuilderImpl(final SReportImpl report) {
        super();
        this.report = report;
    }

    @Override
    public SReportBuilder setDescription(final String description) {
        report.setDescription(description);
        return this;
    }

    @Override
    public SReportBuilder setScreenshot(final byte[] screenshot) {
        report.setScreenshot(screenshot);
        return this;
    }

    @Override
    public SReport done() {
        return report;
    }

}
