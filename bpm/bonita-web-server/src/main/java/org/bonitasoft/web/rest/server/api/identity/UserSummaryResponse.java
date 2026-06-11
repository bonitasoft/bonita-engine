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
package org.bonitasoft.web.rest.server.api.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.web.rest.model.identity.UserItem;

/**
 * Lightweight projection of a {@link User} returned by the {@code identity/userSummary} resource:
 * only the id, username, first name, last name and job title.
 * <p>
 * JSON attribute names are kept consistent with the {@code identity/user} resource by reusing the
 * {@link UserItem} attribute constants ({@code id}, {@code userName}, {@code firstname}, {@code lastname},
 * {@code job_title}).
 * <p>
 * {@code job_title} is optional on a user and may be {@code null}.
 */
public record UserSummaryResponse(
        @JsonProperty(UserItem.ATTRIBUTE_ID) String id,
        @JsonProperty(UserItem.ATTRIBUTE_USERNAME) String userName,
        @JsonProperty(UserItem.ATTRIBUTE_FIRSTNAME) String firstName,
        @JsonProperty(UserItem.ATTRIBUTE_LASTNAME) String lastName,
        @JsonProperty(UserItem.ATTRIBUTE_JOB_TITLE) String jobTitle) {

    static UserSummaryResponse from(final User user) {
        return new UserSummaryResponse(String.valueOf(user.getId()), user.getUserName(), user.getFirstName(),
                user.getLastName(), user.getJobTitle());
    }
}
