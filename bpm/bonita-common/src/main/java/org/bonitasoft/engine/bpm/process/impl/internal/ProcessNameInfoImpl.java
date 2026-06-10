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
package org.bonitasoft.engine.bpm.process.impl.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bonitasoft.engine.bpm.process.ProcessNameInfo;

/**
 * @author Anthony Birembaut
 */
@Getter
@EqualsAndHashCode
@ToString
public class ProcessNameInfoImpl implements ProcessNameInfo {

    private static final long serialVersionUID = 1L;

    private final String name;

    private final String displayName;

    private final List<String> versions;

    public ProcessNameInfoImpl(final String name, final String displayName, final List<String> versions) {
        this.name = name;
        this.displayName = displayName;
        this.versions = versions == null ? Collections.emptyList() : new ArrayList<>(versions);
    }

    // Explicit accessor (Lombok's @Getter skips it) to return an unmodifiable view rather than the internal list.
    @Override
    public List<String> getVersions() {
        return Collections.unmodifiableList(versions);
    }
}
