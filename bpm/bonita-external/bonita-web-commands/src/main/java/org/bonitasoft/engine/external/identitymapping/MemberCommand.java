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
package org.bonitasoft.engine.external.identitymapping;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.system.CommandWithParameters;
import org.bonitasoft.engine.identity.MemberType;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class MemberCommand extends CommandWithParameters {

    protected MemberType getMemberType(final Long userId, final Long groupId, final Long roleId) throws SCommandParameterizationException {
        MemberType memberType = null;
        if (userId != null) {
            memberType = MemberType.USER;
        } else if (groupId != null && roleId == null) {
            memberType = MemberType.GROUP;
        } else if (roleId != null && groupId == null) {
            memberType = MemberType.ROLE;
        } else if (roleId != null && groupId != null) {
            memberType = MemberType.MEMBERSHIP;
        } else {
            final StringBuilder stb = new StringBuilder("Parameters map must contain at least one of entries: ");
            stb.append(ProfileMemberUtils.USER_ID);
            stb.append(", ");
            stb.append(ProfileMemberUtils.GROUP_ID);
            stb.append(", ");
            stb.append(ProfileMemberUtils.ROLE_ID);
            throw new SCommandParameterizationException(stb.toString());
        }
        return memberType;
    }

    protected Long getProfileIdParameter(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        return getLongMandadoryParameter(parameters, ProfileMemberUtils.PROFILE_ID);
    }

    protected Long getUserIdParameter(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        final String message = "Error while retrieving the parameter " + ProfileMemberUtils.USER_ID + " (long value).";
        return getParameter(parameters, ProfileMemberUtils.USER_ID, message);
    }

    protected Long getGroupIdParameter(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        final String message = "Error while retrieving the parameter " + ProfileMemberUtils.GROUP_ID + " (long value).";
        return getParameter(parameters, ProfileMemberUtils.GROUP_ID, message);
    }

    protected Long getRoleIdParameter(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        final String message = "Error while retrieving the parameter " + ProfileMemberUtils.ROLE_ID + " (long value).";
        return getParameter(parameters, ProfileMemberUtils.ROLE_ID, message);
    }

    protected String getQuerySuffix(final MemberType memberType) {
        String suffix = null;
        switch (memberType) {
            case USER:
                suffix = "ForUser";
                break;

            case GROUP:
                suffix = "ForGroup";
                break;

            case ROLE:
                suffix = "ForRole";
                break;

            case MEMBERSHIP:
                suffix = "ForRoleAndGroup";
                break;
            default:
                throw new IllegalStateException();
        }
        return suffix;
    }

}
