/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Holds the value of the {@value #DATA_RETENTION_ENABLED_PROPERTY} property,
 * read once at startup, for callers that are not Spring beans (e.g. API implementations).
 */
@Component
public class DataRetentionSettings {

    /**
     * Platform property enabling BDM data retention (tracking and scheduled cleanup). Defaults to {@code true}.
     */
    public static final String DATA_RETENTION_ENABLED_PROPERTY = "bonita.runtime.retention.enabled";

    private final boolean enabled;

    public DataRetentionSettings(
            @Value("${" + DATA_RETENTION_ENABLED_PROPERTY + ":true}") boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
