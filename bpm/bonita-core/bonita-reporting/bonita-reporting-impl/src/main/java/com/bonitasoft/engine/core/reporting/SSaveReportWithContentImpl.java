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
 * @author Emmanuel Duchastenier
 */
public class SSaveReportWithContentImpl extends SReportImpl implements SSaveReportWithContent {

    private static final long serialVersionUID = -5601507328546725517L;

    private byte[] content;

    public SSaveReportWithContentImpl() {
    }

    public SSaveReportWithContentImpl(final SReport sReport, final byte[] reportContent) {
        super(sReport);
        setContent(reportContent);
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    public void setContent(final byte[] content) {
        this.content = content;
    }

    @Override
    public String getDiscriminator() {
        return SSaveReportWithContent.class.getName();
    }

}
