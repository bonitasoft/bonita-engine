/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.reporting;

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;

/**
 * A Report is a way to store, amongst other things, a binary screenshot and a binary content.
 * It is used by Bonita BPM Portal to store specific reporting behaviour.
 *
 * @author Matthieu Chaffotte
 * @see com.bonitasoft.engine.api.ReportingAPI
 */
public interface Report extends BaseElement {

    /**
     * Gets the name of this <code>Report</code>.
     *
     * @return the logical name of this <code>Report</code>.
     */
    String getName();

    /**
     * Is this report provided by default.
     *
     * @return true if this report is provided by default, false otherwise.
     */
    boolean isProvided();

    /**
     * Gets the description of this <code>Report</code>.
     *
     * @return the description of this <code>Report</code>.
     */
    String getDescription();

    /**
     * Get the date when this report was initially installed into the Engine.
     *
     * @return the date when this report was initially installed into the Engine.
     */
    Date getInstallationDate();

    /**
     * Gets the ID of the user that installed the report.
     *
     * @return the ID of the user that installed the report, or -1 if it is a default report.
     */
    long getInstalledBy();

    /**
     * Get the date when this report was last modified.
     *
     * @return the date when this report was last modified.
     */
    Date getLastModificationDate();

    /**
     * @return the screenshot associated with this report, as a binary content.
     */
    byte[] getScreenshot();

}
