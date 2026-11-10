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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;

/**
 * Jackson mix-in for {@link ProcessDeploymentInfo}.
 * <p>
 * Exclusions:
 * <ul>
 * <li>{@code id} — the deployment info row id is internal plumbing; callers identify the
 * process via {@code processId} (the {@code ProcessDefinition} id). Suppressing it
 * prevents the two ids — which are often equal in single-deployment cases — from
 * leaking to consumers as if they were distinct.</li>
 * </ul>
 * Long-id aliasing (JS-precision policy — JSON string instead of number):
 * <ul>
 * <li>{@code processId} — the underlying {@code ProcessDefinition} id</li>
 * <li>{@code deployedBy} — id of the user who deployed the process</li>
 * </ul>
 * Date fields are left to Jackson defaults (handled by the date (de)serializers also
 * registered on {@link BonitaJacksonModuleProvider}).
 * <p>
 * Registered globally via {@link BonitaJacksonModuleProvider} so the contract applies to
 * every {@code ProcessDeploymentInfo} reaching the REST layer (e.g. embedded in
 * {@code DelegatedTask.rootProcess}).
 */
abstract class ProcessDeploymentInfoMixIn {

    @JsonIgnore
    public abstract long getId();

    @JsonSerialize(using = ToStringSerializer.class)
    public abstract long getProcessId();

    @JsonSerialize(using = ToStringSerializer.class)
    public abstract long getDeployedBy();
}
