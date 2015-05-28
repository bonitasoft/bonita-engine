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

package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.profile.model.impl.SProfileImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class ProfileBuilder extends PersistentObjectBuilder<SProfileImpl, ProfileBuilder> {

    private String name;

    public static ProfileBuilder aProfile() {
        return new ProfileBuilder();
    }

    @Override
    ProfileBuilder getThisBuilder() {
        return this;
    }

    @Override
    SProfileImpl _build() {
        SProfileImpl profile = new SProfileImpl();
        profile.setName(name);
        return profile;
    }

    public ProfileBuilder withName(String name) {
        this.name = name;
        return this;
    }
}
