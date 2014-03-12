/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.page;

/**
 * @author Laurent Leseigneur
 */
public class SPageBuilderImpl implements SPageBuilder {

    private final SPageImpl report;

    public SPageBuilderImpl(final SPageImpl report) {
        super();
        this.report = report;
    }

    @Override
    public SPageBuilder setDescription(final String description) {
        report.setDescription(description);
        return this;
    }

    @Override
    public SPage done() {
        return report;
    }

}
