/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.io.Serial;

import lombok.Getter;
import lombok.ToString;
import org.bonitasoft.engine.platform.Platform;

/**
 * @author Elias Ricken de Medeiros
 */
@ToString
@Getter
public class PlatformImpl implements Platform {

    @Serial
    private static final long serialVersionUID = -8493649294374229877L;

    private final long created;

    private final String createdBy;

    private final String initialVersion;

    private final String version;

    private final boolean maintenanceEnabled;

    public PlatformImpl(final String version, final String initialVersion, final String createdBy, final long created,
            final boolean maintenanceEnabled) {
        this.version = version;
        this.initialVersion = initialVersion;
        this.createdBy = createdBy;
        this.created = created;
        this.maintenanceEnabled = maintenanceEnabled;
    }

}
