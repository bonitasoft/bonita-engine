/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.reporting;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Emmanuel Duchastenier
 */
public class ReportCreator implements Serializable {

    private static final long serialVersionUID = -6244698895635243428L;

    public enum ReportField {
        NAME, DESCRIPTION, SCREENSHOT// , PROVIDED;
    }

    private final Map<ReportField, Serializable> fields;

    public ReportCreator(final String name) {
        fields = new HashMap<ReportField, Serializable>(3);
        fields.put(ReportField.NAME, name);
    }

    // public ReportCreator setProvided(final boolean provided) {
    // fields.put(ReportField.PROVIDED, provided);
    // return this;
    // }

    public ReportCreator setDescription(final String description) {
        fields.put(ReportField.DESCRIPTION, description);
        return this;
    }

    public ReportCreator setScreenshot(final byte[] screenshot) {
        fields.put(ReportField.SCREENSHOT, screenshot);
        return this;
    }

    public Map<ReportField, Serializable> getFields() {
        return fields;
    }

}
