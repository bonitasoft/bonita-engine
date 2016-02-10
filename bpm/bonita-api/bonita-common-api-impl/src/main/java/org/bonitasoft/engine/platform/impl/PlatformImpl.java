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
package org.bonitasoft.engine.platform.impl;

import org.bonitasoft.engine.platform.Platform;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformImpl implements Platform {

    private static final long serialVersionUID = -8493649294374229877L;

    private final long created;

    private final String createdBy;

    private final String initialVersion;

    private final String previousVersion;

    private final String version;

    public PlatformImpl(final String version, final String previousVersion, final String initialVersion, final String createdBy, final long created) {
        this.version = version;
        this.previousVersion = previousVersion;
        this.initialVersion = initialVersion;
        this.createdBy = createdBy;
        this.created = created;
    }

    @Override
    public long getCreated() {
        return this.created;
    }

    @Override
    public String getCreatedBy() {
        return this.createdBy;
    }

    @Override
    public String getInitialVersion() {
        return this.initialVersion;
    }

    @Override
    public String getPreviousVersion() {
        return this.previousVersion;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String toString() {
        final StringBuilder stb = new StringBuilder();
        stb.append("PlatformImpl [created=");
        stb.append(this.created);
        stb.append(", createdBy=");
        stb.append(this.createdBy);
        stb.append(", initialVersion=");
        stb.append(this.initialVersion);
        stb.append(", previousVersion=");
        stb.append(this.previousVersion);
        stb.append(", version=");
        stb.append(this.version);
        stb.append("]");

        return stb.toString();
    }

}
