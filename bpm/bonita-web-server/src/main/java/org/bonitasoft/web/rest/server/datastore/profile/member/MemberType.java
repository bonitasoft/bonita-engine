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
package org.bonitasoft.web.rest.server.datastore.profile.member;

import org.bonitasoft.web.rest.model.portal.profile.AbstractMemberItem;

/**
 * @author Vincent Elcrin
 */
public enum MemberType {

    USER(AbstractMemberItem.VALUE_MEMBER_TYPE_USER), GROUP(AbstractMemberItem.VALUE_MEMBER_TYPE_GROUP), ROLE(
            AbstractMemberItem.VALUE_MEMBER_TYPE_ROLE), MEMBERSHIP(AbstractMemberItem.VALUE_MEMBER_TYPE_MEMBERSHIP);

    private final String type;

    MemberType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static MemberType from(String type) {
        for (MemberType candidate : MemberType.values()) {
            if (candidate.getType().equals(type)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("No enum const for " + type + " found");
    }
}
