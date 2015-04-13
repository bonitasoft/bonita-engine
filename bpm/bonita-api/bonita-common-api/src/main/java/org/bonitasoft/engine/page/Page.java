/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.page;

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;

/**
 * A Page is a way to store, amongst other things, a binary content.
 * It is used by Bonita BPM Portal to display specific custom content
 * 
 * @author Laurent Leseigneur
 */
public interface Page extends BaseElement {

    /**
     * Get the name of this <code>Page</code>.
     * 
     * @return the logical name of this <code>Page</code>.
     */
    String getName();

    /**
     * Get the display name of this <code>Page</code>.
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
     * Get the description of this <code>Page</code>.
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
     * Get the ID of the user that installed the page.
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
     * Get the name of the zip file.
     * 
     * @return the name of the zip file of this <code>Page</code>.
     */
    String getContentName();

    /**
     * Get the type of this <code>Page</code>.
     *
     * @return the type of this <code>Page</code>. see {@link ContentType} for available values
     * @since 7.0
     */
    String getContentType();

    /**
     * Get the process definition ID of this <code>Page</code>.
     *
     * @return the process definition ID of this <code>Page</code>, or null if not set.
     * @since 7.0
     */
    Long getProcessDefinitionId();



}
