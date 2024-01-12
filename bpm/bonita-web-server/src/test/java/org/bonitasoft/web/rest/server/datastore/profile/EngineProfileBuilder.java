/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.profile;

import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.impl.ProfileImpl;

/**
 * @author Vincent Elcrin
 */
public class EngineProfileBuilder {

    private String name;

    private String description;

    public static EngineProfileBuilder anEngineProfile() {
        return new EngineProfileBuilder();
    }

    public EngineProfileBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public EngineProfileBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public Profile build() {
        final ProfileImpl profile = new ProfileImpl(name);
        profile.setDescription(description);
        return profile;
    }

}
