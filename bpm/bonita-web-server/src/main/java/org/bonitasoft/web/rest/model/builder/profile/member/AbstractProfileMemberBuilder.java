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
package org.bonitasoft.web.rest.model.builder.profile.member;

/**
 * @author Vincent Elcrin
 */
public abstract class AbstractProfileMemberBuilder<O> {

    protected Long id = 1L;
    protected Long profileId = 2L;
    protected Long userId = 3L;
    protected Long groupId = 4L;
    protected Long roleId = 5L;

    public AbstractProfileMemberBuilder<O> withId(Long id) {
        this.id = id;
        return this;
    }

    public AbstractProfileMemberBuilder<O> withProfileId(Long id) {
        this.profileId = id;
        return this;
    }

    public AbstractProfileMemberBuilder<O> withUserId(Long id) {
        this.userId = id;
        return this;
    }

    public AbstractProfileMemberBuilder<O> withGroupId(Long id) {
        this.groupId = id;
        return this;
    }

    public AbstractProfileMemberBuilder<O> withRoleId(Long id) {
        this.roleId = id;
        return this;
    }

    public abstract O build();

}
