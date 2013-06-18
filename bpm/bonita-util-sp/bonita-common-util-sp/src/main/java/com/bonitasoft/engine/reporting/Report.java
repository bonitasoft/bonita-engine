/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.reporting;

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;

/**
 * @author Matthieu Chaffotte
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
