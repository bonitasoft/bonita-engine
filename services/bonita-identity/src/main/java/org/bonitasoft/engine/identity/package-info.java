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
/**
 * <p>Manages information about an organization, that is, the set of users who can act in processes. Handles creation, modification, and deletion of
 * organizations, groups, roles, memberships, and users.</p>
 * <p>The identity service relies upon database access and produce database access event {@link org.bonitasoft.engine.events.EventService}</p>
 *
 * @see org.bonitasoft.engine.identity.IdentityService
 * @since 6.0.0
 */

package org.bonitasoft.engine.identity;