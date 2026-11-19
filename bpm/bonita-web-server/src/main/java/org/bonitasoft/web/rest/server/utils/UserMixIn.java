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
package org.bonitasoft.web.rest.server.utils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bonitasoft.engine.identity.User;

/**
 * Jackson mix-in patching {@link User} to serialize every long-typed id field as a JSON
 * string, to prevent JavaScript precision loss on values exceeding
 * {@code Number.MAX_SAFE_INTEGER}.
 * <p>
 * Covers all long ids exposed by {@code User}:
 * <ul>
 * <li>{@code id} — the user's own primary key (the contract {@code BaseRestElement} will
 * enforce upstream)</li>
 * <li>{@code createdBy} — id of the user who created this account</li>
 * <li>{@code managerUserId} — id of this user's manager</li>
 * <li>{@code iconId} — id of the icon row used as avatar</li>
 * </ul>
 * Date fields are left to Jackson defaults (handled by the date (de)serializers also
 * registered on {@link BonitaJacksonModuleProvider}).
 * <p>
 * This is a workaround until {@code User} extends {@code BaseRestElement} upstream in the
 * {@code bonita-organization-model} artifact, at which point the {@code id} entry can be
 * dropped from this mix-in (or the whole class, if upstream also annotates the other ids).
 * <p>
 * Registered globally via {@link BonitaJacksonModuleProvider} so the contract applies to
 * every {@code User} reaching the REST layer (e.g. embedded in delegation DTOs).
 */
abstract class UserMixIn {

    @JsonSerialize(using = ToStringSerializer.class)
    public abstract long getId();

    @JsonSerialize(using = ToStringSerializer.class)
    public abstract long getCreatedBy();

    @JsonSerialize(using = ToStringSerializer.class)
    public abstract long getManagerUserId();

    @JsonSerialize(using = ToStringSerializer.class)
    public abstract Long getIconId();
}
