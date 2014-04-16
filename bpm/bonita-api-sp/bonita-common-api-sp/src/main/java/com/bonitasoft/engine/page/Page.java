/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;

/**
 * A Page is a way to store, amongst other things, a binary content.
 * It is used by Bonita BPM Portal to display specific custom content
 * 
 * @author Laurent Leseigneur
 * 
 */
public interface Page extends BaseElement {

    /**
     * Gets the name of this <code>Page</code>.
     * 
     * @return the logical name of this <code>Page</code>.
     */
    String getName();

    /**
     * Gets the display name of this <code>Page</code>.
     * 
     * @return the display name of this <code>Page</code>.
     */
    String getDisplayName();

    /**
     * Is this page provided by default.
     * 
     * @return true if this page is provided by default, false otherwise.
     */
    boolean isProvided();

    /**
     * Gets the description of this <code>Page</code>.
     * 
     * @return the description of this <code>Page</code>.
     */
    String getDescription();

    /**
     * Get the date when this page was initially installed into the Engine.
     * 
     * @return the date when this page was initially installed into the Engine.
     */
    Date getInstallationDate();

    /**
     * Gets the ID of the user that installed the page.
     * 
     * @return the ID of the user that installed the page, or -1 if it is a default page.
     */
    long getInstalledBy();

    /**
     * Get the date when this page was last modified.
     * 
     * @return the date when this page was last modified.
     */
    Date getLastModificationDate();

    /**
     * Get the userId of the user that last updated this page.
     * 
     * @return the user id of the user that last updated this page.
     */
    long getLastUpdatedBy();

    /**
     * Gets the name of the zip file.
     * 
     * @return the name of the zip file of this <code>Page</code>.
     */
    String getContentName();

}
