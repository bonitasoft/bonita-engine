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
package org.bonitasoft.engine.profile.model.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.profile.model.SProfileEntry;

/**
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
public class SProfileEntryImpl implements SProfileEntry {

    private long id;
    private long tenantId;
    private long profileId;
    private long parentId;
    private String name;
    private String description;
    private long index;
    private String type;
    private String page;
    private boolean custom;

    public SProfileEntryImpl(final String name, final long profileId) {
        this.name = name;
        this.profileId = profileId;
    }

    public SProfileEntryImpl(final SProfileEntry profileEntry) {
        super();
        id = profileEntry.getId();
        name = profileEntry.getName();
        description = profileEntry.getDescription();
        profileId = profileEntry.getProfileId();
        parentId = profileEntry.getParentId();
        index = profileEntry.getIndex();
        page = profileEntry.getPage();
        type = profileEntry.getType();
        custom = profileEntry.isCustom();
    }
}
