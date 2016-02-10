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
package org.bonitasoft.engine.platform;

import java.io.Serializable;

/**
 * Contains information about the <code>Bonita BPM Platform</code>.
 * <p>
 * The <code>platform</code> is the base on which runs the <code>Bonita BPM Engine</code>. There is only one platform for a running <code>Bonita BPM
 * Engine</code>.
 * <p>
 * In order to perform actions on the platform, please, refer to the {@link org.bonitasoft.engine.api.PlatformAPI}
 *
 * @author Matthieu Chaffotte
 * @see org.bonitasoft.engine.api.PlatformAPI
 * @since 6.0.0
 */
public interface Platform extends Serializable {

    /**
     * Retrieves the <code>platform</code> version
     *
     * @return a String representing the <code>platform</code> version
     * @see #getPreviousVersion()
     * @see #getInitialVersion()
     */
    String getVersion();

    /**
     * Retrieves the <code>platform</code> previous version. This information only will be set if you have installed the <code>Bonita BPM engine</code> in a
     * previous
     * version before upgrading to a new version using the <code>Bonita BPM Migration Tool</code>. Otherwise, this method will return an empty string.
     * <p>
     * For instance, if you have migrated from version 6.3.3 to version 6.3.4, <code>getPreviousVersion</code> will return 6.3.3 and {@link #getVersion()} will
     * return 6.3.4.
     *
     * @return a String representing the <code>platform</code> previous version
     * @see #getVersion()
     * @see #getInitialVersion()
     */
    String getPreviousVersion();

    /**
     * Retrieves the <code>platform</code> initial version. That is, the Bonita BPM version in which you have initially created the platform before migrating
     * to the current version.
     * <p>
     * For instance, if you have created your platform in the version 6.1.0 and have migrated to the version 6.3.4 using the <code>Bonita BPM Migration
     * Tool</code>, {@code getInitialVersion} will return 6.1.0, {@link #getPreviousVersion()} will return 6.3.3 and {@link #getVersion()} will return 6.3.4.
     *
     * @return a String representing the <code>platform</code> initial version
     * @see #getVersion()
     * @see #getPreviousVersion()
     */
    String getInitialVersion();

    /**
     * Retrieves the timestamp at which the platform was created
     *
     * @return a long representing the timestamp at which the platform was created
     */
    long getCreated();

    /**
     * Retrieves the name of the platform technical user that created the platform
     *
     * @return a String representing the name of the platform technical user that created the platform
     */
    String getCreatedBy();

}
